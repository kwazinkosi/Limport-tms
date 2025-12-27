package com.limport.tms.domain.event.states;

import com.limport.tms.domain.model.enums.TransportRequestStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * Published when a transport request is successfully completed.
 * This marks the end of the transport lifecycle.
 * 
 * Event Type: TMS.Transport.Request.Completed
 */
public final class TransportRequestCompletedEvent extends TransportStatusChangedEvent {

    private final Instant completedAt;
    private final String completionNotes;

    public TransportRequestCompletedEvent(
            UUID transportRequestId,
            String userId,
            TransportRequestStatus previousStatus,
            String completionNotes) {
        super(transportRequestId, userId, previousStatus, TransportRequestStatus.COMPLETED, "Transport completed successfully");
        this.completedAt = Instant.now();
        this.completionNotes = completionNotes;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public String getCompletionNotes() {
        return completionNotes;
    }

    @Override
    protected String eventName() {
        return "Completed";
    }
}
