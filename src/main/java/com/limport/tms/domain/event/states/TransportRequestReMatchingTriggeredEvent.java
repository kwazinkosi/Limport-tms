package com.limport.tms.domain.event.states;

import com.limport.tms.domain.event.TransportEvent;

import java.util.UUID;

/**
 * Published when a transport request needs re-matching after provider rejection or timeout.
 * This event signals to external services (like PMS) that the request should be matched again.
 *
 * Event Type: TransportEvents.Request.ReMatchingTriggered
 */
public final class TransportRequestReMatchingTriggeredEvent extends TransportEvent {

    private final UUID previousAssignmentId;
    private final UUID previousProviderId;
    private final String providerName;
    private final String rejectionReason;
    private final int attemptNumber;
    private final int maxAttempts;

    public TransportRequestReMatchingTriggeredEvent(
            UUID transportRequestId,
            String userId,
            UUID previousAssignmentId,
            UUID previousProviderId,
            String providerName,
            String rejectionReason,
            int attemptNumber,
            int maxAttempts) {
        super(transportRequestId, userId);
        this.previousAssignmentId = previousAssignmentId;
        this.previousProviderId = previousProviderId;
        this.providerName = providerName;
        this.rejectionReason = rejectionReason;
        this.attemptNumber = attemptNumber;
        this.maxAttempts = maxAttempts;
    }

    public UUID getPreviousAssignmentId() {
        return previousAssignmentId;
    }

    public UUID getPreviousProviderId() {
        return previousProviderId;
    }

    public String getProviderName() {
        return providerName;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public int getAttemptNumber() {
        return attemptNumber;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    @Override
    public String eventCategory() {
        return "Request";
    }

    @Override
    protected String eventName() {
        return "ReMatchingTriggered";
    }
}