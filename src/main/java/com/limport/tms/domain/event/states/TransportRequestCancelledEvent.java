package com.limport.tms.domain.event.states;

import com.limport.tms.domain.model.enums.TransportRequestStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * Published when a transport request is cancelled.
 * This event allows downstream services to cease any processing related to that request.
 * 
 * Event Type: TMS.Transport.Request.Cancelled
 */
public final class TransportRequestCancelledEvent extends TransportStatusChangedEvent {

    private final Instant cancelledAt;
    private final String cancellationReason;
    private final String cancelledBy;

    public TransportRequestCancelledEvent(
            UUID transportRequestId,
            String userId,
            TransportRequestStatus previousStatus,
            String cancellationReason,
            String cancelledBy) {
        super(transportRequestId, userId, previousStatus, TransportRequestStatus.CANCELLED, cancellationReason);
        this.cancelledAt = Instant.now();
        this.cancellationReason = cancellationReason;
        this.cancelledBy = cancelledBy;
    }

    public Instant getCancelledAt() {
        return cancelledAt;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public String getCancelledBy() {
        return cancelledBy;
    }

    @Override
    protected String eventName() {
        return "Cancelled";
    }
}
