package com.limport.tms.infrastructure.persistance.entity;

import com.limport.tms.domain.model.entity.Assignment;
import com.limport.tms.domain.model.entity.Assignment.AssignmentStatus;
import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA entity for persisting Assignment.
 * Maps to the assignments table.
 */
@Entity
@Table(name = "assignments")
public class AssignmentJpaEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "transport_request_id", nullable = false)
    private UUID transportRequestId;

    @Column(name = "provider_id", nullable = false)
    private UUID providerId;

    @Column(name = "vehicle_id", nullable = false)
    private UUID vehicleId;

    @Column(name = "scheduled_pickup_time", nullable = false)
    private LocalDateTime scheduledPickupTime;

    @Column(name = "estimated_delivery_time", nullable = false)
    private LocalDateTime estimatedDeliveryTime;

    @Column(name = "assignment_notes", columnDefinition = "TEXT")
    private String assignmentNotes;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AssignmentStatus status;

    @Column(name = "assigned_by", nullable = false, length = 100)
    private String assignedBy;

    @Column(name = "assigned_at", nullable = false, updatable = false)
    private Instant assignedAt;

    @Column(name = "last_updated_at", nullable = false)
    private Instant lastUpdatedAt;

    // JPA requires no-arg constructor
    protected AssignmentJpaEntity() {
    }

    public AssignmentJpaEntity(UUID id, UUID transportRequestId, UUID providerId, UUID vehicleId,
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
     * Converts JPA entity to domain entity.
     */
    public Assignment toDomain() {
        Assignment domain = new Assignment();
        domain.setId(this.id);
        domain.setTransportRequestId(this.transportRequestId);
        domain.setProviderId(this.providerId);
        domain.setVehicleId(this.vehicleId);
        domain.setScheduledPickupTime(this.scheduledPickupTime);
        domain.setEstimatedDeliveryTime(this.estimatedDeliveryTime);
        domain.setAssignmentNotes(this.assignmentNotes);
        domain.setStatus(this.status);
        domain.setAssignedBy(this.assignedBy);
        domain.setAssignedAt(this.assignedAt);
        domain.setLastUpdatedAt(this.lastUpdatedAt);
        return domain;
    }

    /**
     * Creates JPA entity from domain entity.
     */
    public static AssignmentJpaEntity fromDomain(Assignment domain) {
        return new AssignmentJpaEntity(
            domain.getId(),
            domain.getTransportRequestId(),
            domain.getProviderId(),
            domain.getVehicleId(),
            domain.getScheduledPickupTime(),
            domain.getEstimatedDeliveryTime(),
            domain.getAssignmentNotes(),
            domain.getStatus(),
            domain.getAssignedBy(),
            domain.getAssignedAt(),
            domain.getLastUpdatedAt()
        );
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
}
