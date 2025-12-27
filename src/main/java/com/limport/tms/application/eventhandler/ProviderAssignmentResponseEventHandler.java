package com.limport.tms.application.eventhandler;

import com.limport.tms.application.event.IExternalEventHandler;
import com.limport.tms.application.event.pms.ProviderAssignmentResponseEvent;
import com.limport.tms.application.event.pms.ProviderAssignmentResponseEvent.AssignmentResponse;
import com.limport.tms.domain.model.entity.Assignment;
import com.limport.tms.domain.model.entity.Assignment.AssignmentStatus;
import com.limport.tms.domain.ports.IAssignmentRepository;
import com.limport.tms.domain.ports.ITransportRequestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Handles ProviderAssignmentResponseEvent from PMS.
 * 
 * When a provider responds to an assignment (accept/reject/timeout),
 * TMS updates the assignment and transport request status accordingly.
 * 
 * Design: Event-driven, idempotent, eventual consistency.
 */
@Component
public class ProviderAssignmentResponseEventHandler implements IExternalEventHandler<ProviderAssignmentResponseEvent> {
    
    private static final Logger log = LoggerFactory.getLogger(ProviderAssignmentResponseEventHandler.class);
    
    private final IAssignmentRepository assignmentRepository;
    private final ITransportRequestRepository transportRequestRepository;
    
    public ProviderAssignmentResponseEventHandler(
            IAssignmentRepository assignmentRepository,
            ITransportRequestRepository transportRequestRepository) {
        this.assignmentRepository = assignmentRepository;
        this.transportRequestRepository = transportRequestRepository;
    }
    
    @Override
    @Transactional
    public void handle(ProviderAssignmentResponseEvent event) {
        log.info("Handling ProviderAssignmentResponseEvent for assignmentId={}, providerId={}, response={}",
            event.assignmentId(),
            event.providerId(),
            event.response());
        
        // Validate assignment exists
        Optional<Assignment> assignmentOpt = assignmentRepository.findById(event.assignmentId());
        if (assignmentOpt.isEmpty()) {
            log.warn("Assignment {} not found, skipping provider response", event.assignmentId());
            return;
        }
        
        Assignment assignment = assignmentOpt.get();
        
        // Validate assignment belongs to the provider
        if (!assignment.getProviderId().equals(event.providerId())) {
            log.warn("Assignment {} does not belong to provider {}, event provider={}", 
                event.assignmentId(), assignment.getProviderId(), event.providerId());
            return;
        }
        
        switch (event.response()) {
            case ACCEPTED -> handleAccepted(event, assignment);
            case REJECTED -> handleRejected(event, assignment);
            case TIMEOUT -> handleTimeout(event, assignment);
        }
        
        log.debug("Successfully processed ProviderAssignmentResponseEvent eventId={}", event.eventId());
    }
    
    private void handleAccepted(ProviderAssignmentResponseEvent event, Assignment assignment) {
        log.info("Provider {} accepted assignment {} for transport request {}",
            event.providerName(),
            event.assignmentId(),
            event.transportRequestId());
        
        // Update assignment status to CONFIRMED
        assignment.setStatus(AssignmentStatus.CONFIRMED);
        assignmentRepository.save(assignment);
        
        log.info("Assignment {} confirmed for provider {}", event.assignmentId(), event.providerId());
    }
    
    private void handleRejected(ProviderAssignmentResponseEvent event, Assignment assignment) {
        log.info("Provider {} rejected assignment {} for transport request {} with reason: {}",
            event.providerName(),
            event.assignmentId(),
            event.transportRequestId(),
            event.responseReason());
        
        // Update assignment status to CANCELLED
        assignment.setStatus(AssignmentStatus.CANCELLED);
        assignmentRepository.save(assignment);
        
        // TODO: Trigger re-matching logic or notify operations team
        // This could involve calling PMS again or updating transport request status
        
        log.info("Assignment {} cancelled due to provider rejection", event.assignmentId());
    }
    
    private void handleTimeout(ProviderAssignmentResponseEvent event, Assignment assignment) {
        log.info("Provider {} timed out on assignment {} for transport request {}",
            event.providerName(),
            event.assignmentId(),
            event.transportRequestId());
        
        // Update assignment status to CANCELLED
        assignment.setStatus(AssignmentStatus.CANCELLED);
        assignmentRepository.save(assignment);
        
        // TODO: Trigger re-matching logic or notify operations team
        // Similar to rejection handling
        
        log.info("Assignment {} cancelled due to provider timeout", event.assignmentId());
    }
    
    @Override
    public String getSupportedEventType() {
        return com.limport.tms.domain.event.EventTypes.Provider.ASSIGNMENT_RESPONSE;
    }
    
    @Override
    public Class<ProviderAssignmentResponseEvent> getEventClass() {
        return ProviderAssignmentResponseEvent.class;
    }
}
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
        return com.limport.tms.domain.event.EventTypes.Provider.ASSIGNMENT_RESPONSE;
    }
    
    @Override
    public Class<ProviderAssignmentResponseEvent> getEventClass() {
        return ProviderAssignmentResponseEvent.class;
    }
}
