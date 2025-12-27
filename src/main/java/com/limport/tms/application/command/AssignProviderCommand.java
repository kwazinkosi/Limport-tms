package com.limport.tms.application.command;

import com.limport.tms.application.cqrs.ICommand;
import com.limport.tms.application.dto.response.TransportRequestResponse;

import java.util.UUID;

/**
 * Command to assign a provider to a transport request.
 */
public class AssignProviderCommand implements ICommand<TransportRequestResponse> {

    private UUID transportRequestId;
    private UUID providerId;
    private UUID vehicleId;

    public AssignProviderCommand() {}

    public AssignProviderCommand(UUID transportRequestId, UUID providerId, UUID vehicleId) {
        this.transportRequestId = transportRequestId;
        this.providerId = providerId;
        this.vehicleId = vehicleId;
    }

    public UUID getTransportRequestId() {
        return transportRequestId;
    }

    public void setTransportRequestId(UUID transportRequestId) {
        this.transportRequestId = transportRequestId;
    }

    public UUID getProviderId() {
        return providerId;
    }

    public void setProviderId(UUID providerId) {
        this.providerId = providerId;
    }

    public UUID getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(UUID vehicleId) {
        this.vehicleId = vehicleId;
    }
}
