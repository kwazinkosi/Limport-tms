package com.limport.tms.domain.event.states;

import com.limport.tms.domain.event.TransportEvent;
import com.limport.tms.domain.model.enums.TransportRequestStatus;

import java.util.UUID;

/**
 * Base class for all transport request state change events.
 * All request lifecycle events (Created, Updated, Cancelled, etc.) extend this.
 */
public abstract class TransportStatusChangedEvent extends TransportEvent {
    
    private final TransportRequestStatus previousStatus;
    private final TransportRequestStatus newStatus;
    private final String reason;

    protected TransportStatusChangedEvent(
            UUID transportRequestId,
            String userId,
            TransportRequestStatus previousStatus,
            TransportRequestStatus newStatus,
            String reason) {
        super(transportRequestId, userId);
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.reason = reason;
    }

    public TransportRequestStatus getPreviousStatus() {
        return previousStatus;
    }

    public TransportRequestStatus getNewStatus() {
        return newStatus;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public String eventCategory() {
        return "Request";
    }
}
