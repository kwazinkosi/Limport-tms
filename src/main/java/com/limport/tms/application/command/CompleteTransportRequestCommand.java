package com.limport.tms.application.command;

import java.time.Instant;
import java.util.UUID;

/**
 * Command representing the completion of a transport request.
 */
public class CompleteTransportRequestCommand {

    private UUID transportRequestId;
    private Instant completedAt;

    public UUID getTransportRequestId() {
        return transportRequestId;
    }

    public void setTransportRequestId(UUID transportRequestId) {
        this.transportRequestId = transportRequestId;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }
}
