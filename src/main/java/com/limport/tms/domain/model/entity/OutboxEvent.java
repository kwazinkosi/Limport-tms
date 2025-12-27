package com.limport.tms.domain.model.entity;

import java.time.Instant;
import java.util.UUID;

/**
 * Outbox event entity for reliable event publishing.
 * Implements the transactional outbox pattern to ensure events are never lost.
 * 
 * Events are stored in the same transaction as the aggregate, then
 * asynchronously published to the message broker.
 */
public class OutboxEvent {
    
    private final UUID id;
    private final String eventType;
    private final String aggregateType;
    private final String aggregateId;
    private final String payload;
    private final Instant occurredOn;
    private OutboxStatus status;
    private int retryCount;
    private Instant processedAt;
    private String errorMessage;
    
    /**
     * Creates a new outbox event entry.
     */
    public OutboxEvent(
            String eventType,
            String aggregateType,
            String aggregateId,
            String payload,
            Instant occurredOn) {
        this.id = UUID.randomUUID();
        this.eventType = eventType;
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.payload = payload;
        this.occurredOn = occurredOn;
        this.status = OutboxStatus.PENDING;
        this.retryCount = 0;
    }
    
    /**
     * Reconstitutes from persistence.
     */
    public OutboxEvent(
            UUID id,
            String eventType,
            String aggregateType,
            String aggregateId,
            String payload,
            Instant occurredOn,
            OutboxStatus status,
            int retryCount,
            Instant processedAt,
            String errorMessage) {
        this.id = id;
        this.eventType = eventType;
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.payload = payload;
        this.occurredOn = occurredOn;
        this.status = status;
        this.retryCount = retryCount;
        this.processedAt = processedAt;
        this.errorMessage = errorMessage;
    }
    
    /**
     * Marks the event as successfully processed.
     */
    public void markAsProcessed() {
        this.status = OutboxStatus.PROCESSED;
        this.processedAt = Instant.now();
        this.errorMessage = null;
    }
    
    /**
     * Marks the event as failed and increments retry count.
     * After max retries, status becomes FAILED.
     */
    public void markAsFailed(String error) {
        this.retryCount++;
        this.errorMessage = error;
        this.status = retryCount >= 3 ? OutboxStatus.FAILED : OutboxStatus.PENDING;
    }
    
    /**
     * Resets the event status to PENDING for retry from dead letter queue.
     */
    public void resetForRetry() {
        this.status = OutboxStatus.PENDING;
        this.errorMessage = null;
        // Keep retryCount as is for tracking
    }
    
    // Getters
    public UUID getId() { return id; }
    public String getEventType() { return eventType; }
    public String getAggregateType() { return aggregateType; }
    public String getAggregateId() { return aggregateId; }
    public String getPayload() { return payload; }
    public Instant getOccurredOn() { return occurredOn; }
    public OutboxStatus getStatus() { return status; }
    public int getRetryCount() { return retryCount; }
    public Instant getProcessedAt() { return processedAt; }
    public String getErrorMessage() { return errorMessage; }
    
    /**
     * Outbox event processing status.
     */
    public enum OutboxStatus {
        /** Event is waiting to be published */
        PENDING,
        /** Event has been successfully published */
        PROCESSED,
        /** Event failed after max retries */
        FAILED
    }
}
