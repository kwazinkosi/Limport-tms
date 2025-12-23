package com.limport.tms.application.dto.response;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for assignment information in responses.
 */
public class AssignmentResponse {

    private UUID id;
    private UUID transportRequestId;
    private UUID providerId;
    private UUID vehicleId;
    private LocalDateTime scheduledPickupTime;
    private LocalDateTime estimatedDeliveryTime;
    private String assignmentNotes;
    private String status;
    private String assignedBy;
    private Instant assignedAt;
    private Instant lastUpdatedAt;

    public AssignmentResponse() {
    }

    public AssignmentResponse(UUID id, UUID transportRequestId, UUID providerId, UUID vehicleId,
                            LocalDateTime scheduledPickupTime, LocalDateTime estimatedDeliveryTime,
                            String assignmentNotes, String status, String assignedBy,
                            Instant assignedAt, Instant lastUpdatedAt) {
        this.id = id;
        this.transportRequestId = transportRequestId;
        this.providerId = providerId;
        this.vehicleId = vehicleId;
        this.scheduledPickupTime = scheduledPickupTime;
        this.estimatedDeliveryTime = estimatedDeliveryTime;
        this.assignmentNotes = assignmentNotes;
        this.status = status;
        this.assignedBy = assignedBy;
        this.assignedAt = assignedAt;
        this.lastUpdatedAt = lastUpdatedAt;
    }

    // Getters and setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public LocalDateTime getScheduledPickupTime() {
        return scheduledPickupTime;
    }

    public void setScheduledPickupTime(LocalDateTime scheduledPickupTime) {
        this.scheduledPickupTime = scheduledPickupTime;
    }

    public LocalDateTime getEstimatedDeliveryTime() {
        return estimatedDeliveryTime;
    }

    public void setEstimatedDeliveryTime(LocalDateTime estimatedDeliveryTime) {
        this.estimatedDeliveryTime = estimatedDeliveryTime;
    }

    public String getAssignmentNotes() {
        return assignmentNotes;
    }

    public void setAssignmentNotes(String assignmentNotes) {
        this.assignmentNotes = assignmentNotes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAssignedBy() {
        return assignedBy;
    }

    public void setAssignedBy(String assignedBy) {
        this.assignedBy = assignedBy;
    }

    public Instant getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(Instant assignedAt) {
        this.assignedAt = assignedAt;
    }

    public Instant getLastUpdatedAt() {
        return lastUpdatedAt;
    }

    public void setLastUpdatedAt(Instant lastUpdatedAt) {
        this.lastUpdatedAt = lastUpdatedAt;
    }
}
