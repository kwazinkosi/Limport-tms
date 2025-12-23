package com.limport.tms.infrastructure.persistance.entity;

import com.limport.tms.domain.model.entity.OutboxEvent;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity for outbox event persistence.
 */
@Entity
@Table(name = "outbox_events", indexes = {
    @Index(name = "idx_outbox_status", columnList = "status"),
    @Index(name = "idx_outbox_occurred_on", columnList = "occurred_on")
})
public class OutboxEventJpaEntity {
    
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;
    
    @Column(name = "event_type", nullable = false, length = 255)
    private String eventType;
    
    @Column(name = "aggregate_type", nullable = false, length = 100)
    private String aggregateType;
    
    @Column(name = "aggregate_id", nullable = false, length = 50)
    private String aggregateId;
    
    @Column(name = "payload", nullable = false, columnDefinition = "TEXT")
    private String payload;
    
    @Column(name = "occurred_on", nullable = false)
    private Instant occurredOn;
    
    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private OutboxEvent.OutboxStatus status;
    
    @Column(name = "retry_count", nullable = false)
    private int retryCount;
    
    @Column(name = "processed_at")
    private Instant processedAt;
    
    @Column(name = "error_message", length = 1000)
    private String errorMessage;
    
    protected OutboxEventJpaEntity() {}
    
    /**
     * Converts domain entity to JPA entity.
     */
    public static OutboxEventJpaEntity fromDomain(OutboxEvent domain) {
        OutboxEventJpaEntity entity = new OutboxEventJpaEntity();
        entity.id = domain.getId();
        entity.eventType = domain.getEventType();
        entity.aggregateType = domain.getAggregateType();
        entity.aggregateId = domain.getAggregateId();
        entity.payload = domain.getPayload();
        entity.occurredOn = domain.getOccurredOn();
        entity.status = domain.getStatus();
        entity.retryCount = domain.getRetryCount();
        entity.processedAt = domain.getProcessedAt();
        entity.errorMessage = domain.getErrorMessage();
        return entity;
    }
    
    /**
     * Converts JPA entity to domain entity.
     */
    public OutboxEvent toDomain() {
        return new OutboxEvent(
            id,
            eventType,
            aggregateType,
            aggregateId,
            payload,
            occurredOn,
            status,
            retryCount,
            processedAt,
            errorMessage
        );
    }
    
    /**
     * Updates JPA entity from domain entity.
     */
    public void updateFromDomain(OutboxEvent domain) {
        this.status = domain.getStatus();
        this.retryCount = domain.getRetryCount();
        this.processedAt = domain.getProcessedAt();
        this.errorMessage = domain.getErrorMessage();
    }
    
    // Getters
    public UUID getId() { return id; }
    public String getEventType() { return eventType; }
    public String getAggregateType() { return aggregateType; }
    public String getAggregateId() { return aggregateId; }
    public String getPayload() { return payload; }
    public Instant getOccurredOn() { return occurredOn; }
    public OutboxEvent.OutboxStatus getStatus() { return status; }
    public int getRetryCount() { return retryCount; }
    public Instant getProcessedAt() { return processedAt; }
    public String getErrorMessage() { return errorMessage; }
}
