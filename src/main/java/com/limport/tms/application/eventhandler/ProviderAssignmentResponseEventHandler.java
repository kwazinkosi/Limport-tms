package com.limport.tms.application.eventhandler;

import com.limport.tms.application.event.IExternalEventHandler;
import com.limport.tms.application.event.pms.ProviderAssignmentResponseEvent;
import com.limport.tms.application.event.pms.ProviderAssignmentResponseEvent.AssignmentResponse;
import com.limport.tms.domain.event.EventTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Handles ProviderAssignmentResponseEvent from PMS.
 * 
 * When a provider responds to an assignment (accept/reject/timeout),
 * TMS updates the transport request status accordingly.
 */
@Component
public class ProviderAssignmentResponseEventHandler implements IExternalEventHandler<ProviderAssignmentResponseEvent> {
    
    private static final Logger log = LoggerFactory.getLogger(ProviderAssignmentResponseEventHandler.class);
    
    // TODO: Inject services for business logic
    // private final IAssignmentRepository assignmentRepository;
    // private final ITransportRequestCommandService commandService;
    
    @Override
    public void handle(ProviderAssignmentResponseEvent event) {
        log.info("Handling ProviderAssignmentResponseEvent for assignmentId={}, providerId={}, response={}",
            event.assignmentId(),
            event.providerId(),
            event.response());
        
        switch (event.response()) {
            case ACCEPTED -> handleAccepted(event);
            case REJECTED -> handleRejected(event);
            case TIMEOUT -> handleTimeout(event);
        }
        
        log.debug("Successfully processed ProviderAssignmentResponseEvent eventId={}", event.eventId());
    }
    
    private void handleAccepted(ProviderAssignmentResponseEvent event) {
        log.info("Provider {} accepted assignment {} for transport request {}",
            event.providerName(),
            event.assignmentId(),
            event.transportRequestId());
        
        // Business Logic:
        // 1. Update assignment status to CONFIRMED
        // 2. Update transport request status to PLANNED
        // 3. Publish TransportEvents.Request.ProviderConfirmed
        
        // TODO: Implement when services are wired
        // assignmentRepository.updateStatus(event.assignmentId(), AssignmentStatus.CONFIRMED);
        // commandService.confirmAssignment(event.transportRequestId(), event.assignmentId());
    }
    
    private void handleRejected(ProviderAssignmentResponseEvent event) {
        log.warn("Provider {} rejected assignment {} for transport request {}. Reason: {}",
            event.providerName(),
            event.assignmentId(),
            event.transportRequestId(),
            event.responseReason());
        
        // Business Logic:
        // 1. Update assignment status to REJECTED
        // 2. Find alternative providers or revert status
        // 3. Notify user of rejection
        
        // TODO: Implement rejection handling
        // assignmentRepository.updateStatus(event.assignmentId(), AssignmentStatus.REJECTED);
        // 
        // If there are other pending assignments for this request:
        //   - Auto-assign to next best provider
        // Else:
        //   - Revert request to REQUESTED status
        //   - Trigger new provider matching
    }
    
    private void handleTimeout(ProviderAssignmentResponseEvent event) {
        log.warn("Provider {} did not respond to assignment {} for transport request {} within timeout",
            event.providerName(),
            event.assignmentId(),
            event.transportRequestId());
        
        // Business Logic:
        // 1. Update assignment status to TIMED_OUT
        // 2. Auto-assign to next provider or alert user
        // 3. Consider marking provider as less reliable
        
        // TODO: Implement timeout handling
        // Similar to rejection but may have different retry logic
    }
    
    @Override
    public String getSupportedEventType() {
        return EventTypes.Provider.ASSIGNMENT_RESPONSE;
    }
    
    @Override
    public Class<ProviderAssignmentResponseEvent> getEventClass() {
        return ProviderAssignmentResponseEvent.class;
    }
}
