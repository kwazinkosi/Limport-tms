package com.limport.tms.application.command;

import com.limport.tms.application.cqrs.ICommand;
import com.limport.tms.application.dto.response.TransportRequestResponse;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Command to assign an existing transport request to a provider and vehicle.
 */
public class AssignProviderToTransportRequestCommand implements ICommand<TransportRequestResponse> {

    private UUID transportRequestId;
    private String providerId;
    private String vehicleId;
    private LocalDateTime scheduledPickup;
    private LocalDateTime scheduledDelivery;
    private String assignmentNotes;

    public UUID getTransportRequestId() {
        return transportRequestId;
    }

    public void setTransportRequestId(UUID transportRequestId) {
        this.transportRequestId = transportRequestId;
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public LocalDateTime getScheduledPickup() {
        return scheduledPickup;
    }

    public void setScheduledPickup(LocalDateTime scheduledPickup) {
        this.scheduledPickup = scheduledPickup;
    }

    public LocalDateTime getScheduledDelivery() {
        return scheduledDelivery;
    }

    public void setScheduledDelivery(LocalDateTime scheduledDelivery) {
        this.scheduledDelivery = scheduledDelivery;
    }

    public String getAssignmentNotes() {
        return assignmentNotes;
    }

    public void setAssignmentNotes(String assignmentNotes) {
        this.assignmentNotes = assignmentNotes;
    }
}
