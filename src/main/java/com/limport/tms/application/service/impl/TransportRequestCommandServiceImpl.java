package com.limport.tms.application.service.impl;

import com.limport.tms.application.dto.request.AssignProviderRequest;
import com.limport.tms.application.dto.request.CancelTransportRequestRequest;
import com.limport.tms.application.dto.request.CreateTransportRequest;
import com.limport.tms.application.dto.response.TransportRequestResponse;
import com.limport.tms.application.mapper.TransportRequestMapper;
import com.limport.tms.application.service.interfaces.IDomainEventService;
import com.limport.tms.application.service.interfaces.ITransportRequestCommandService;
import com.limport.tms.domain.exception.TransportRequestNotFoundException;
import com.limport.tms.domain.model.aggregate.TransportRequestAggregate;
import com.limport.tms.domain.model.entity.Assignment;
import com.limport.tms.domain.model.entity.TransportRequest;
import com.limport.tms.domain.model.enums.TransportRequestStatus;
import com.limport.tms.domain.port.repository.IAssignmentRepository;
import com.limport.tms.domain.port.repository.ITransportRequestRepository;
import com.limport.tms.domain.port.service.IProviderMatchingClient;
import com.limport.tms.domain.port.service.IRouteValidator;
import com.limport.tms.domain.service.TransportRequestStateMachine;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Application service handling transport request commands.
 * Orchestrates domain logic, persistence, and event publishing.
 */
@Service
public class TransportRequestCommandServiceImpl implements ITransportRequestCommandService {

    private final ITransportRequestRepository repository;
    private final IAssignmentRepository assignmentRepository;
    private final IDomainEventService eventService;
    private final TransportRequestMapper mapper;
    private final IProviderMatchingClient pmsClient;
    private final IRouteValidator routeValidator;

    public TransportRequestCommandServiceImpl(
            ITransportRequestRepository repository,
            IAssignmentRepository assignmentRepository,
            IDomainEventService eventService,
            TransportRequestMapper mapper,
            IProviderMatchingClient pmsClient,
            IRouteValidator routeValidator) {
        this.repository = repository;
        this.assignmentRepository = assignmentRepository;
        this.eventService = eventService;
        this.mapper = mapper;
        this.pmsClient = pmsClient;
        this.routeValidator = routeValidator;
    }

    @Override
    @Transactional
    public TransportRequestResponse createTransportRequest(CreateTransportRequest request) {
        // Create domain aggregate (encapsulates business logic and raises events)
        Map<String, Object> requestDetails = new HashMap<>();
        requestDetails.put("totalWeight", request.getTotalWeight());
        requestDetails.put("totalPackages", request.getTotalPackages());
        requestDetails.put("pickupFrom", request.getPickupFrom());
        requestDetails.put("pickupUntil", request.getPickupFrom().plusHours(4)); // Default 4-hour window
        requestDetails.put("deliveryFrom", request.getDeliveryUntil().minusHours(2)); // Default 2-hour window
        requestDetails.put("deliveryUntil", request.getDeliveryUntil());
        requestDetails.put("notes", request.getNotes());

        TransportRequestAggregate aggregate = TransportRequestAggregate.create(
            request.getCustomerId(),
            request.getOriginLocationCode(),
            request.getDestinationLocationCode(),
            requestDetails
        );

        // Convert aggregate to entity and persist
        TransportRequest transportRequest = aggregateToEntity(aggregate);
        transportRequest.setReference(generateReference());
        TransportRequest saved = repository.save(transportRequest);

        // Publish domain events raised by the aggregate
        eventService.collectAndStore(aggregate, "TransportRequest");

        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public TransportRequestResponse assignProvider(UUID id, AssignProviderRequest request) {
        // Load existing transport request
        TransportRequest existingEntity = repository.findById(id)
                .orElseThrow(() -> new TransportRequestNotFoundException(id));

        // Reconstitute aggregate from entity
        TransportRequestAggregate aggregate = entityToAggregate(existingEntity);

        // Validate state transition using state machine
        TransportRequestStateMachine.validateTransition(
            aggregate.getStatus(),
            TransportRequestStatus.PLANNED
        );

        // TMS validates route feasibility (TMS owns route optimization)
        IRouteValidator.RouteOptimizationResult routeResult = routeValidator.optimizeRoute(
            aggregate.getId(),
            aggregate.getOrigin(),
            aggregate.getDestination()
        );

        if (!routeResult.isFeasible()) {
            throw new com.limport.tms.domain.exception.RouteNotFeasibleException(
                routeResult.getMessage()
            );
        }

        // PMS validates capacity (PMS owns provider/capacity data)
        IProviderMatchingClient.CapacityVerificationResponse capacityResult =
            pmsClient.verifyCapacity(
                request.getProviderId(),
                aggregate.getId(),
                (Double) aggregate.getDetails().getOrDefault("totalWeight", 0.0)
            );

        if (!capacityResult.isSufficient()) {
            throw new com.limport.tms.domain.exception.InsufficientCapacityException(
                capacityResult.requiredCapacity(),
                capacityResult.availableCapacity()
            );
        }

        // Create assignment entity
        Assignment assignment = Assignment.createNew(
            aggregate.getId(),
            request.getProviderId(),
            request.getVehicleId(),
            request.getScheduledPickup(),
            request.getScheduledDelivery(),
            request.getAssignmentNotes(),
            "SYSTEM" // TODO: Get from security context
        );
        assignmentRepository.save(assignment);

        // Execute domain logic through aggregate (raises domain events)
        aggregate.assignProvider(request.getProviderId(), request.getVehicleId(), request.getAssignmentNotes());

        // Convert updated aggregate back to entity and persist
        TransportRequest updatedEntity = aggregateToEntity(aggregate);
        TransportRequest saved = repository.save(updatedEntity);

        // Publish domain events raised by the aggregate
        eventService.collectAndStore(aggregate, "TransportRequest");

        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public TransportRequestResponse cancelTransportRequest(UUID id, CancelTransportRequestRequest request) {
        // Load existing transport request
        TransportRequest existingEntity = repository.findById(id)
                .orElseThrow(() -> new TransportRequestNotFoundException(id));

        // Reconstitute aggregate from entity
        TransportRequestAggregate aggregate = entityToAggregate(existingEntity);

        // Validate state transition using state machine
        TransportRequestStateMachine.validateTransition(
            aggregate.getStatus(),
            TransportRequestStatus.CANCELLED
        );

        // Execute domain logic through aggregate (raises domain events)
        aggregate.cancel(request.getReason(), request.getCancelledBy());

        // Convert updated aggregate back to entity and persist
        TransportRequest updatedEntity = aggregateToEntity(aggregate);
        TransportRequest saved = repository.save(updatedEntity);

        // Publish domain events raised by the aggregate
        eventService.collectAndStore(aggregate, "TransportRequest");

        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public TransportRequestResponse completeTransportRequest(UUID id) {
        // Load existing transport request
        TransportRequest existingEntity = repository.findById(id)
                .orElseThrow(() -> new TransportRequestNotFoundException(id));

        // Reconstitute aggregate from entity
        TransportRequestAggregate aggregate = entityToAggregate(existingEntity);

        // Validate state transition using state machine
        TransportRequestStateMachine.validateTransition(
            aggregate.getStatus(),
            TransportRequestStatus.COMPLETED
        );

        // Execute domain logic through aggregate (raises domain events)
        aggregate.complete("Transport completed successfully");

        // Convert updated aggregate back to entity and persist
        TransportRequest updatedEntity = aggregateToEntity(aggregate);
        TransportRequest saved = repository.save(updatedEntity);

        // Publish domain events raised by the aggregate
        eventService.collectAndStore(aggregate, "TransportRequest");

        return mapper.toResponse(saved);
    }

    private String generateReference() {
        return "TR-" + System.currentTimeMillis();
    }

    /**
     * Converts a TransportRequestAggregate to a TransportRequest entity for persistence.
     */
    private TransportRequest aggregateToEntity(TransportRequestAggregate aggregate) {
        TransportRequest entity = new TransportRequest();
        entity.setId(aggregate.getId());
        entity.setCustomerId(aggregate.getUserId());
        entity.setOriginLocationCode(aggregate.getOrigin());
        entity.setDestinationLocationCode(aggregate.getDestination());
        entity.setStatus(aggregate.getStatus());
        entity.setCreatedAt(aggregate.getCreatedAt());
        entity.setLastUpdatedAt(aggregate.getUpdatedAt());

        // Extract details from aggregate
        Map<String, Object> details = aggregate.getDetails();
        if (details != null) {
            Integer weight = (Integer) details.get("totalWeight");
            entity.setTotalWeight(weight != null ? BigDecimal.valueOf(weight) : null);
            entity.setTotalPackages((Integer) details.get("totalPackages"));
            entity.setPickupFrom(toLocalDateTime((Instant) details.get("pickupFrom")));
            entity.setPickupUntil(toLocalDateTime((Instant) details.get("pickupUntil")));
            entity.setDeliveryFrom(toLocalDateTime((Instant) details.get("deliveryFrom")));
            entity.setDeliveryUntil(toLocalDateTime((Instant) details.get("deliveryUntil")));
        }

        return entity;
    }

    /**
     * Converts a TransportRequest entity to a TransportRequestAggregate for domain operations.
     */
    private TransportRequestAggregate entityToAggregate(TransportRequest entity) {
        // Reconstruct details map from entity fields
        Map<String, Object> details = new HashMap<>();
        BigDecimal weight = entity.getTotalWeight();
        details.put("totalWeight", weight != null ? weight.intValue() : null);
        details.put("totalPackages", entity.getTotalPackages());
        details.put("pickupFrom", toInstant(entity.getPickupFrom()));
        details.put("pickupUntil", toInstant(entity.getPickupUntil()));
        details.put("deliveryFrom", toInstant(entity.getDeliveryFrom()));
        details.put("deliveryUntil", toInstant(entity.getDeliveryUntil()));

        return TransportRequestAggregate.reconstitute(
            entity.getId(),
            entity.getCustomerId(),
            entity.getOriginLocationCode(),
            entity.getDestinationLocationCode(),
            entity.getStatus(),
            null, // assignedProviderId - would need to be loaded separately if needed
            null, // assignedVehicleId - would need to be loaded separately if needed
            details,
            entity.getCreatedAt(),
            entity.getLastUpdatedAt()
        );
    }

    private LocalDateTime toLocalDateTime(Instant instant) {
        return instant != null ? LocalDateTime.ofInstant(instant, java.time.ZoneOffset.UTC) : null;
    }

    private Instant toInstant(LocalDateTime localDateTime) {
        return localDateTime != null ? localDateTime.toInstant(java.time.ZoneOffset.UTC) : null;
    }

    /**
     * Publishes all domain events raised by the aggregate to the outbox.
     */

}
