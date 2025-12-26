package com.limport.tms.application.eventhandler;

import com.limport.tms.application.event.IExternalEventHandler;
import com.limport.tms.application.event.pms.ProviderCapacityChangedEvent;
import com.limport.tms.domain.event.EventTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Handles ProviderCapacityChangedEvent from PMS.
 * 
 * When provider capacity changes, TMS may need to:
 * - Re-evaluate pending transport requests
 * - Alert if assigned provider capacity drops below requirement
 */
@Component
public class ProviderCapacityChangedEventHandler implements IExternalEventHandler<ProviderCapacityChangedEvent> {
    
    private static final Logger log = LoggerFactory.getLogger(ProviderCapacityChangedEventHandler.class);
    
    // TODO: Inject services for business logic
    // private final ITransportRequestRepository transportRequestRepository;
    // private final IAssignmentRepository assignmentRepository;
    
    @Override
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
        // Potential actions:
        // 1. Re-match pending transport requests with this provider
        // 2. Send notification about new availability
        
        log.debug("Provider {} capacity increased by {} kg",
            event.providerId(),
            event.getCapacityDelta());
        
        // TODO: Implement re-matching logic
        // List<TransportRequest> pendingRequests = transportRequestRepository
        //     .findByStatusAndWeightLessThanEqual(Status.REQUESTED, event.newCapacityKg());
        // 
        // pendingRequests.forEach(request -> {
        //     // Trigger re-evaluation
        // });
    }
    
    private void handleCapacityDecrease(ProviderCapacityChangedEvent event) {
        // Provider capacity reduced
        // Potential actions:
        // 1. Check if any assigned requests exceed new capacity
        // 2. Alert if conflict detected
        
        log.debug("Provider {} capacity decreased by {} kg",
            event.providerId(),
            Math.abs(event.getCapacityDelta()));
        
        // TODO: Implement conflict detection
        // List<Assignment> activeAssignments = assignmentRepository
        //     .findActiveByProviderId(event.providerId());
        // 
        // activeAssignments.forEach(assignment -> {
        //     if (assignment.getRequiredCapacity() > event.newCapacityKg()) {
        //         // Alert: capacity conflict!
        //     }
        // });
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
