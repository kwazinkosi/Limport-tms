package com.limport.tms.domain.model.aggregate;

import com.limport.tms.domain.event.states.*;
import com.limport.tms.domain.model.enums.TransportRequestStatus;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Aggregate root for Transport Request domain.
 * Encapsulates all transport request business logic and raises domain events.
 */
public class TransportRequestAggregate extends AggregateRoot {
    
    private final UUID id;
    private String userId;
    private String origin;
    private String destination;
    private TransportRequestStatus status;
    private UUID assignedProviderId;
    private UUID assignedVehicleId;
    private Map<String, Object> details;
    private Instant createdAt;
    private Instant updatedAt;
    
    /**
     * Private constructor - use factory methods to create instances.
     */
    private TransportRequestAggregate(UUID id) {
        this.id = id;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }
    
    /**
     * Factory method to create a new transport request.
     * Raises TransportRequestCreatedEvent.
     */
    public static TransportRequestAggregate create(
            String userId,
            String origin,
            String destination,
            Map<String, Object> details) {
        
        TransportRequestAggregate aggregate = new TransportRequestAggregate(UUID.randomUUID());
        aggregate.userId = userId;
        aggregate.origin = origin;
        aggregate.destination = destination;
        aggregate.details = details;
        aggregate.status = TransportRequestStatus.REQUESTED;
        
        // Raise domain event
        aggregate.registerEvent(new TransportRequestCreatedEvent(
            aggregate.id,
            userId,
            origin,
            destination,
            details
        ));
        
        return aggregate;
    }
    
    /**
     * Reconstitutes an aggregate from persistence (no events raised).
     */
    public static TransportRequestAggregate reconstitute(
            UUID id,
            String userId,
            String origin,
            String destination,
            TransportRequestStatus status,
            UUID assignedProviderId,
            UUID assignedVehicleId,
            Map<String, Object> details,
            Instant createdAt,
            Instant updatedAt) {
        
        TransportRequestAggregate aggregate = new TransportRequestAggregate(id);
        aggregate.userId = userId;
        aggregate.origin = origin;
        aggregate.destination = destination;
        aggregate.status = status;
        aggregate.assignedProviderId = assignedProviderId;
        aggregate.assignedVehicleId = assignedVehicleId;
        aggregate.details = details;
        aggregate.createdAt = createdAt;
        aggregate.updatedAt = updatedAt;
        return aggregate;
    }
    
    /**
     * Updates the transport request details.
     * Raises TransportRequestUpdatedEvent.
     */
    public void update(Map<String, Object> updatedFields, String updateDescription) {
        validateNotCancelledOrCompleted();
        
        TransportRequestStatus previousStatus = this.status;
        if (updatedFields != null) {
            this.details = updatedFields;
        }
        this.updatedAt = Instant.now();
        
        registerEvent(new TransportRequestUpdatedEvent(
            this.id,
            this.userId,
            previousStatus,
            this.status,
            updatedFields,
            updateDescription
        ));
    }
    
    /**
     * Assigns a provider and vehicle to this transport request.
     * Raises TransportRequestAssignedEvent.
     */
    public void assignProvider(UUID providerId, UUID vehicleId, String assignmentDetails) {
        validateNotCancelledOrCompleted();
        
        if (providerId == null) {
            throw new IllegalArgumentException("Provider ID cannot be null");
        }
        
        TransportRequestStatus previousStatus = this.status;
        this.assignedProviderId = providerId;
        this.assignedVehicleId = vehicleId;
        this.status = TransportRequestStatus.PLANNED;
        this.updatedAt = Instant.now();
        
        registerEvent(new TransportRequestAssignedEvent(
            this.id,
            this.userId,
            previousStatus,
            providerId,
            vehicleId,
            assignmentDetails
        ));
    }
    
    /**
     * Marks the transport request as in transit.
     * Raises TransportRequestUpdatedEvent with status change.
     */
    public void startTransit() {
        if (this.status != TransportRequestStatus.PLANNED) {
            throw new IllegalStateException("Cannot start transit: request must be in PLANNED status");
        }
        
        TransportRequestStatus previousStatus = this.status;
        this.status = TransportRequestStatus.IN_TRANSIT;
        this.updatedAt = Instant.now();
        
        registerEvent(new TransportRequestUpdatedEvent(
            this.id,
            this.userId,
            previousStatus,
            this.status,
            Map.of("status", this.status.name()),
            "Transport started"
        ));
    }
    
    /**
     * Completes the transport request.
     * Raises TransportRequestCompletedEvent.
     */
    public void complete(String completionNotes) {
        if (this.status != TransportRequestStatus.IN_TRANSIT) {
            throw new IllegalStateException("Cannot complete: request must be IN_TRANSIT");
        }
        
        TransportRequestStatus previousStatus = this.status;
        this.status = TransportRequestStatus.COMPLETED;
        this.updatedAt = Instant.now();
        
        registerEvent(new TransportRequestCompletedEvent(
            this.id,
            this.userId,
            previousStatus,
            completionNotes
        ));
    }
    
    /**
     * Cancels the transport request.
     * Raises TransportRequestCancelledEvent.
     */
    public void cancel(String reason, String cancelledBy) {
        if (this.status == TransportRequestStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel: request is already completed");
        }
        if (this.status == TransportRequestStatus.CANCELLED) {
            throw new IllegalStateException("Cannot cancel: request is already cancelled");
        }
        
        TransportRequestStatus previousStatus = this.status;
        this.status = TransportRequestStatus.CANCELLED;
        this.updatedAt = Instant.now();
        
        registerEvent(new TransportRequestCancelledEvent(
            this.id,
            this.userId,
            previousStatus,
            reason,
            cancelledBy
        ));
    }
    
    private void validateNotCancelledOrCompleted() {
        if (this.status == TransportRequestStatus.CANCELLED) {
            throw new IllegalStateException("Cannot modify cancelled request");
        }
        if (this.status == TransportRequestStatus.COMPLETED) {
            throw new IllegalStateException("Cannot modify completed request");
        }
    }
    
    // Getters
    @Override
    public UUID getId() { return id; }
    public String getUserId() { return userId; }
    public String getOrigin() { return origin; }
    public String getDestination() { return destination; }
    public TransportRequestStatus getStatus() { return status; }
    public UUID getAssignedProviderId() { return assignedProviderId; }
    public UUID getAssignedVehicleId() { return assignedVehicleId; }
    public Map<String, Object> getDetails() { return details; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
