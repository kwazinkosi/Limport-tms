package com.limport.tms.application.service.impl;

import com.limport.tms.application.dto.request.AssignProviderRequest;
import com.limport.tms.application.dto.request.CancelTransportRequestRequest;
import com.limport.tms.application.dto.request.CreateTransportRequest;
import com.limport.tms.application.dto.response.TransportRequestResponse;
import com.limport.tms.application.mapper.TransportRequestMapper;
import com.limport.tms.application.service.interfaces.IDomainEventService;
import com.limport.tms.application.service.interfaces.ITransportRequestCommandService;
import com.limport.tms.domain.event.states.*;
import com.limport.tms.domain.model.entity.Assignment;
import com.limport.tms.domain.model.entity.TransportRequest;
import com.limport.tms.domain.model.enums.TransportRequestStatus;
import com.limport.tms.domain.ports.IAssignmentRepository;
import com.limport.tms.domain.ports.ITransportRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
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

    public TransportRequestCommandServiceImpl(
            ITransportRequestRepository repository,
            IAssignmentRepository assignmentRepository,
            IDomainEventService eventService,
            TransportRequestMapper mapper) {
        this.repository = repository;
        this.assignmentRepository = assignmentRepository;
        this.eventService = eventService;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public TransportRequestResponse createTransportRequest(CreateTransportRequest request) {
        // Create domain entity
        TransportRequest transportRequest = new TransportRequest();
        transportRequest.setId(UUID.randomUUID());
        transportRequest.setReference(generateReference());
        transportRequest.setCustomerId(request.getCustomerId());
        transportRequest.setOriginLocationCode(request.getOriginLocationCode());
        transportRequest.setDestinationLocationCode(request.getDestinationLocationCode());
        transportRequest.setPickupFrom(request.getPickupFrom());
        transportRequest.setPickupUntil(request.getPickupFrom().plusHours(4)); // Default 4-hour window
        transportRequest.setDeliveryFrom(request.getDeliveryUntil().minusHours(2)); // Default 2-hour window
        transportRequest.setDeliveryUntil(request.getDeliveryUntil());
        transportRequest.setTotalWeight(request.getTotalWeight());
        transportRequest.setTotalPackages(request.getTotalPackages());
        transportRequest.setStatus(TransportRequestStatus.REQUESTED);
        
        Instant now = Instant.now();
        transportRequest.setCreatedAt(now);
        transportRequest.setLastUpdatedAt(now);

        // Persist
        TransportRequest saved = repository.save(transportRequest);

        // Publish domain event
        Map<String, Object> requestDetails = new HashMap<>();
        requestDetails.put("totalWeight", request.getTotalWeight());
        requestDetails.put("totalPackages", request.getTotalPackages());
        requestDetails.put("notes", request.getNotes());

        TransportRequestCreatedEvent event = new TransportRequestCreatedEvent(
                saved.getId(),
                saved.getCustomerId(),
                saved.getOriginLocationCode(),
                saved.getDestinationLocationCode(),
                requestDetails
        );
        
        eventService.publishToOutbox(event, "TransportRequest", saved.getId().toString());

        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public TransportRequestResponse assignProvider(UUID id, AssignProviderRequest request) {
        TransportRequest transportRequest = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Transport request not found: " + id));

        // Validate state transition
        if (transportRequest.getStatus() != TransportRequestStatus.REQUESTED) {
            throw new IllegalStateException("Cannot assign provider to request in status: " + transportRequest.getStatus());
        }

        // Create assignment entity
        Assignment assignment = Assignment.createNew(
            transportRequest.getId(),
            request.getProviderId(),
            request.getVehicleId(),
            request.getScheduledPickup(),
            request.getScheduledDelivery(),
            request.getAssignmentNotes(),
            "SYSTEM" // TODO: Get from security context
        );
        assignmentRepository.save(assignment);

        // Update transport request status
        TransportRequestStatus previousStatus = transportRequest.getStatus();
        transportRequest.setStatus(TransportRequestStatus.PLANNED);
        transportRequest.setLastUpdatedAt(Instant.now());

        TransportRequest updated = repository.save(transportRequest);

        // Publish domain event
        TransportRequestAssignedEvent event = new TransportRequestAssignedEvent(
                updated.getId(),
                updated.getCustomerId(),
                previousStatus,
                request.getProviderId(),
                request.getVehicleId(),
                request.getAssignmentNotes()
        );
        
        eventService.publishToOutbox(event, "TransportRequest", updated.getId().toString());

        return mapper.toResponse(updated);
    }

    @Override
    @Transactional
    public TransportRequestResponse cancelTransportRequest(UUID id, CancelTransportRequestRequest request) {
        TransportRequest transportRequest = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Transport request not found: " + id));

        // Validate state transition
        if (transportRequest.getStatus() == TransportRequestStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel completed transport request");
        }
        if (transportRequest.getStatus() == TransportRequestStatus.CANCELLED) {
            throw new IllegalStateException("Transport request already cancelled");
        }

        TransportRequestStatus previousStatus = transportRequest.getStatus();
        transportRequest.setStatus(TransportRequestStatus.CANCELLED);
        transportRequest.setLastUpdatedAt(Instant.now());

        TransportRequest updated = repository.save(transportRequest);

        // Publish domain event
        TransportRequestCancelledEvent event = new TransportRequestCancelledEvent(
                updated.getId(),
                updated.getCustomerId(),
                previousStatus,
                request.getReason(),
                request.getCancelledBy()
        );
        
        eventService.publishToOutbox(event, "TransportRequest", updated.getId().toString());

        return mapper.toResponse(updated);
    }

    @Override
    @Transactional
    public TransportRequestResponse completeTransportRequest(UUID id) {
        TransportRequest transportRequest = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Transport request not found: " + id));

        // Validate state transition
        if (transportRequest.getStatus() != TransportRequestStatus.IN_TRANSIT) {
            throw new IllegalStateException("Can only complete requests that are in transit. Current status: " + transportRequest.getStatus());
        }

        TransportRequestStatus previousStatus = transportRequest.getStatus();
        transportRequest.setStatus(TransportRequestStatus.COMPLETED);
        transportRequest.setLastUpdatedAt(Instant.now());

        TransportRequest updated = repository.save(transportRequest);

        // Publish domain event
        TransportRequestCompletedEvent event = new TransportRequestCompletedEvent(
                updated.getId(),
                updated.getCustomerId(),
                previousStatus,
                "Transport completed successfully"
        );
        
        eventService.publishToOutbox(event, "TransportRequest", updated.getId().toString());

        return mapper.toResponse(updated);
    }

    private String generateReference() {
        return "TR-" + System.currentTimeMillis();
    }
}
