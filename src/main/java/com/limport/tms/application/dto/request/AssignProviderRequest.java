package com.limport.tms.application.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Request payload for assigning a transport request to a provider/vehicle.
 */
public class AssignProviderRequest {

    @NotNull
    private UUID providerId;

    @NotNull
    private UUID vehicleId;

    @NotNull
    private LocalDateTime scheduledPickup;

    @NotNull
    private LocalDateTime scheduledDelivery;

    private String assignmentNotes;

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

