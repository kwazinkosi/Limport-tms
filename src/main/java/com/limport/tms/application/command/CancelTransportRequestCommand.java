package com.limport.tms.application.command;

import java.util.UUID;

/**
 * Command representing the intent to cancel a transport request.
 */
public class CancelTransportRequestCommand {

    private UUID transportRequestId;
    private String reason;

    public UUID getTransportRequestId() {
        return transportRequestId;
    }

    public void setTransportRequestId(UUID transportRequestId) {
        this.transportRequestId = transportRequestId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
