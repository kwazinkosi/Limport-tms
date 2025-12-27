package com.limport.tms.application.eventhandler;

import com.limport.tms.application.event.IExternalEventHandler;
import com.limport.tms.application.event.pms.ProviderCapacityChangedEvent;
import com.limport.tms.application.service.interfaces.IDomainEventService;
import com.limport.tms.domain.event.EventTypes;
import com.limport.tms.domain.event.states.TransportRequestReMatchingTriggeredEvent;
import com.limport.tms.domain.model.entity.Assignment;
import com.limport.tms.domain.model.entity.TransportRequest;
import com.limport.tms.domain.model.enums.TransportRequestStatus;
import com.limport.tms.domain.port.repository.IAssignmentRepository;
import com.limport.tms.domain.port.repository.ITransportRequestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Handles ProviderCapacityChangedEvent from PMS.
 * 
 * When provider capacity changes, TMS may need to:
 * - Re-evaluate pending transport requests (capacity increase)
 * - Alert if assigned provider capacity drops below requirement (capacity decrease)
 */
@Component
public class ProviderCapacityChangedEventHandler implements IExternalEventHandler<ProviderCapacityChangedEvent> {
    
    private static final Logger log = LoggerFactory.getLogger(ProviderCapacityChangedEventHandler.class);
    
    private final ITransportRequestRepository transportRequestRepository;
    private final IAssignmentRepository assignmentRepository;
    private final IDomainEventService domainEventService;
    
    @Value("${tms.assignment.max-attempts:3}")
    private int maxAssignmentAttempts;
    
    public ProviderCapacityChangedEventHandler(
            ITransportRequestRepository transportRequestRepository,
            IAssignmentRepository assignmentRepository,
            IDomainEventService domainEventService) {
        this.transportRequestRepository = transportRequestRepository;
        this.assignmentRepository = assignmentRepository;
        this.domainEventService = domainEventService;
    }
    
    @Override
    @Transactional
    public void handle(ProviderCapacityChangedEvent event) {
        log.info("Handling ProviderCapacityChangedEvent for providerId={}, previousCapacity={}, newCapacity={}, reason={}",
            event.providerId(),
            event.previousCapacityKg(),
            event.newCapacityKg(),
            event.changeReason());
        
        // Business Logic:
        if (event.isCapacityIncreased()) {
            handleCapacityIncrease(event);
        } else {
            handleCapacityDecrease(event);
        }
        
        log.debug("Successfully processed ProviderCapacityChangedEvent eventId={}", event.eventId());
    }
    
    private void handleCapacityIncrease(ProviderCapacityChangedEvent event) {
        // Provider has more capacity available
        // Find pending transport requests that could now be matched with this provider
        
        log.debug("Provider {} capacity increased by {} kg",
            event.providerId(),
            event.getCapacityDelta());
        
        // Find REQUESTED transport requests with weight <= new capacity
        List<TransportRequest> pendingRequests = transportRequestRepository
            .findByStatusAndWeightLessThanEqual(TransportRequestStatus.REQUESTED, BigDecimal.valueOf(event.newCapacityKg()));
        
        if (!pendingRequests.isEmpty()) {
            log.info("Provider {} capacity increase may enable {} pending transport requests. Triggering re-evaluation.",
                event.providerId(), pendingRequests.size());
            
            // For now, we'll trigger re-matching for all pending requests
            // In a more sophisticated implementation, we could prioritize or filter based on other criteria
            for (TransportRequest request : pendingRequests) {
                // Publish event to trigger re-matching for this specific request
                // This will be picked up by PMS to potentially match this provider
                publishCapacityAvailableEvent(request, event);
            }
        } else {
            log.debug("Provider {} capacity increase does not enable any new transport request matches.", event.providerId());
        }
    }
    
    private void handleCapacityDecrease(ProviderCapacityChangedEvent event) {
        // Provider capacity reduced - check for conflicts with active assignments
        
        log.debug("Provider {} capacity decreased by {} kg",
            event.providerId(),
            Math.abs(event.getCapacityDelta()));
        
        // Find active assignments for this provider
        List<Assignment> activeAssignments = assignmentRepository.findActiveByProviderId(event.providerId());
        
        int conflictCount = 0;
        for (Assignment assignment : activeAssignments) {
            // Get the transport request to check weight
            TransportRequest request = transportRequestRepository.findById(assignment.getTransportRequestId()).orElse(null);
            if (request != null && request.getTotalWeight() != null) {
                double requiredCapacity = request.getTotalWeight().doubleValue();
                if (requiredCapacity > event.newCapacityKg()) {
                    conflictCount++;
                    log.warn("Capacity conflict detected: Provider {} (new capacity: {} kg) has active assignment {} requiring {} kg",
                        event.providerId(), event.newCapacityKg(), assignment.getId(), requiredCapacity);
                    
                    // TODO: In a production system, this might trigger:
                    // 1. Alert to operations team
                    // 2. Automatic re-assignment if possible
                    // 3. Provider notification
                    // For now, we just log the conflict
                }
            }
        }
        
        if (conflictCount > 0) {
            log.error("Provider {} capacity decrease created {} capacity conflicts with active assignments. Manual intervention required.",
                event.providerId(), conflictCount);
        } else {
            log.debug("Provider {} capacity decrease does not create any conflicts with active assignments.", event.providerId());
        }
    }
    
    private void publishCapacityAvailableEvent(TransportRequest request, ProviderCapacityChangedEvent capacityEvent) {
        // Publish event to trigger re-matching due to capacity availability
        TransportRequestReMatchingTriggeredEvent reMatchingEvent = new TransportRequestReMatchingTriggeredEvent(
            request.getId(),
            null, // userId - could be extracted from transport request if needed
            null, // previousAssignmentId - not applicable for capacity increase
            capacityEvent.providerId(),
            capacityEvent.providerName(),
            "Provider capacity increased: " + capacityEvent.changeReason(),
            request.getAssignmentAttempts(), // current attempt count
            maxAssignmentAttempts
        );
        domainEventService.publishToOutbox(reMatchingEvent, "TransportRequest", request.getId().toString());
        
        log.debug("Published re-matching event for transport request {} due to provider {} capacity increase",
            request.getId(), capacityEvent.providerId());
    }
    
    @Override
    public String getSupportedEventType() {
        return EventTypes.Provider.CAPACITY_CHANGED;
    }
    
    @Override
    public Class<ProviderCapacityChangedEvent> getEventClass() {
        return ProviderCapacityChangedEvent.class;
    }
}
