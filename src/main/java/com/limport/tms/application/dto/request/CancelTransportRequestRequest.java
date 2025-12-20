package com.limport.tms.application.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Request payload for cancelling a transport request.
 */
public class CancelTransportRequestRequest {

    @NotBlank
    private String reason;

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
