package com.limport.tms.domain.model.entity;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Assignment entity representing the relationship between a transport request
 * and a provider/vehicle. Created when PMS successfully matches a provider
 * to a transport request.
 */
public class Assignment {

    private UUID id;
    private UUID transportRequestId;
    
    // Provider and vehicle IDs from PMS
    private UUID providerId;
    private UUID vehicleId;
    
    // Assignment scheduling
    private LocalDateTime scheduledPickupTime;
    private LocalDateTime estimatedDeliveryTime;
    
    // Assignment metadata
    private String assignmentNotes;
    private AssignmentStatus status;
    
    // Audit fields
    private String assignedBy;
    private Instant assignedAt;
    private Instant lastUpdatedAt;

    public Assignment() {
    }

    public Assignment(UUID id, UUID transportRequestId, UUID providerId, UUID vehicleId,
                     LocalDateTime scheduledPickupTime, LocalDateTime estimatedDeliveryTime,
                     String assignmentNotes, AssignmentStatus status, String assignedBy,
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

    /**
     * Factory method to create a new assignment from PMS matching result.
     */
    public static Assignment createNew(UUID transportRequestId, UUID providerId, UUID vehicleId,
                                      LocalDateTime scheduledPickupTime, LocalDateTime estimatedDeliveryTime,
                                      String assignmentNotes, String assignedBy) {
        return new Assignment(
            UUID.randomUUID(),
            transportRequestId,
            providerId,
            vehicleId,
            scheduledPickupTime,
            estimatedDeliveryTime,
            assignmentNotes,
            AssignmentStatus.ASSIGNED,
            assignedBy,
            Instant.now(),
            Instant.now()
        );
    }

    /**
     * Confirm the assignment (e.g., provider accepted).
     */
    public void confirm() {
        if (this.status != AssignmentStatus.ASSIGNED) {
            throw new IllegalStateException("Can only confirm assignments in ASSIGNED status");
        }
        this.status = AssignmentStatus.CONFIRMED;
        this.lastUpdatedAt = Instant.now();
    }

    /**
     * Mark assignment as in progress (vehicle en route).
     */
    public void startExecution() {
        if (this.status != AssignmentStatus.CONFIRMED) {
            throw new IllegalStateException("Can only start execution after confirmation");
        }
        this.status = AssignmentStatus.IN_PROGRESS;
        this.lastUpdatedAt = Instant.now();
    }

    /**
     * Complete the assignment.
     */
    public void complete() {
        if (this.status != AssignmentStatus.IN_PROGRESS) {
            throw new IllegalStateException("Can only complete assignments in progress");
        }
        this.status = AssignmentStatus.COMPLETED;
        this.lastUpdatedAt = Instant.now();
    }

    /**
     * Cancel the assignment.
     */
    public void cancel(String reason) {
        if (this.status == AssignmentStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel completed assignments");
        }
        this.assignmentNotes = (this.assignmentNotes != null ? this.assignmentNotes + "\n" : "") 
                             + "Cancelled: " + reason;
        this.status = AssignmentStatus.CANCELLED;
        this.lastUpdatedAt = Instant.now();
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

    public AssignmentStatus getStatus() {
        return status;
    }

    public void setStatus(AssignmentStatus status) {
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

    public enum AssignmentStatus {
        ASSIGNED,    // Initial state - provider matched
        CONFIRMED,   // Provider accepted the assignment
        IN_PROGRESS, // Vehicle is executing the transport
        COMPLETED,   // Successfully completed
        CANCELLED    // Assignment was cancelled
    }
}
