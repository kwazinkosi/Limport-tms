package com.limport.tms.application.eventhandler;

import com.limport.tms.application.event.IExternalEventHandler;
import com.limport.tms.application.event.pms.ProviderAssignmentResponseEvent;
import com.limport.tms.domain.event.EventTypes.Provider;
import com.limport.tms.domain.model.entity.Assignment;
import com.limport.tms.domain.model.entity.Assignment.AssignmentStatus;
import com.limport.tms.domain.model.entity.TransportRequest;
import com.limport.tms.domain.model.enums.TransportRequestStatus;
import com.limport.tms.domain.ports.IAssignmentRepository;
import com.limport.tms.domain.ports.ITransportRequestRepository;
import com.limport.tms.application.service.interfaces.IDomainEventService;
import com.limport.tms.domain.event.states.TransportRequestReMatchingTriggeredEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

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
    private final IDomainEventService domainEventService;
    
    @Value("${tms.assignment.max-attempts:3}")
    int maxAssignmentAttempts;
    
    // For testing purposes
    void setMaxAssignmentAttempts(int maxAssignmentAttempts) {
        this.maxAssignmentAttempts = maxAssignmentAttempts;
    }
    
    public ProviderAssignmentResponseEventHandler(
            IAssignmentRepository assignmentRepository,
            ITransportRequestRepository transportRequestRepository,
            IDomainEventService domainEventService) {
        this.assignmentRepository = assignmentRepository;
        this.transportRequestRepository = transportRequestRepository;
        this.domainEventService = domainEventService;
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
        
        // Handle re-matching with retry limit
        handleReMatchingWithRetryLimit(event.transportRequestId(), "rejection", event.assignmentId(), event.providerId(), event.providerName(), event.responseReason());
        
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
        
        // Handle re-matching with retry limit
        handleReMatchingWithRetryLimit(event.transportRequestId(), "timeout", event.assignmentId(), event.providerId(), event.providerName(), null);
        
        log.info("Assignment {} cancelled due to provider timeout", event.assignmentId());
    }
    
    /**
     * Handles re-matching logic with a retry limit.
     * If max attempts exceeded, marks the transport request as UNASSIGNABLE.
     * 
     * @param transportRequestId the transport request to re-match
     * @param reason the reason for re-matching (rejection/timeout)
     * @param previousAssignmentId the ID of the failed assignment
     * @param previousProviderId the ID of the provider that failed
     * @param providerName the name of the provider that failed
     * @param rejectionReason the reason for rejection (null for timeout)
     */
    private void handleReMatchingWithRetryLimit(UUID transportRequestId, String reason, UUID previousAssignmentId, UUID previousProviderId, String providerName, String rejectionReason) {
        Optional<TransportRequest> transportRequestOpt = transportRequestRepository.findById(transportRequestId);
        if (transportRequestOpt.isEmpty()) {
            log.warn("Transport request {} not found when handling {}", transportRequestId, reason);
            return;
        }
        
        TransportRequest transportRequest = transportRequestOpt.get();
        int attempts = transportRequest.incrementAssignmentAttempts();
        
        if (attempts >= maxAssignmentAttempts) {
            // Max retries exceeded - mark as unassignable for manual intervention
            transportRequest.setStatus(TransportRequestStatus.UNASSIGNABLE);
            transportRequestRepository.save(transportRequest);
            log.warn("Transport request {} marked as UNASSIGNABLE after {} failed assignment attempts. Manual intervention required.",
                transportRequestId, attempts);
        } else {
            // Set back to REQUESTED for another matching attempt
            transportRequest.setStatus(TransportRequestStatus.REQUESTED);
            transportRequestRepository.save(transportRequest);
            
            // Publish event to trigger re-matching
            TransportRequestReMatchingTriggeredEvent reMatchingEvent = new TransportRequestReMatchingTriggeredEvent(
                transportRequestId,
                null, // userId - could be extracted from transport request if needed
                previousAssignmentId,
                previousProviderId,
                providerName,
                reason.equals("rejection") ? rejectionReason : null,
                attempts,
                maxAssignmentAttempts
            );
            domainEventService.publishToOutbox(reMatchingEvent, "TransportRequest", transportRequestId.toString());
            
            log.info("Transport request {} set back to REQUESTED for re-matching after provider {} (attempt {}/{})",
                transportRequestId, reason, attempts, maxAssignmentAttempts);
        }
    }
    
    @Override
    public String getSupportedEventType() {
        return Provider.ASSIGNMENT_RESPONSE;
    }
    
    @Override
    public Class<ProviderAssignmentResponseEvent> getEventClass() {
        return ProviderAssignmentResponseEvent.class;
    }
}
