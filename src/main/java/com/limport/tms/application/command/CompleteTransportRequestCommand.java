package com.limport.tms.application.command;

import com.limport.tms.application.cqrs.ICommand;
import com.limport.tms.application.dto.response.TransportRequestResponse;

import java.time.Instant;
import java.util.UUID;

/**
 * Command representing the completion of a transport request.
 */
public class CompleteTransportRequestCommand implements ICommand<TransportRequestResponse> {

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
