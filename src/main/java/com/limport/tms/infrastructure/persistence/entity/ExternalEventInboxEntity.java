package com.limport.tms.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * Entity for storing external events in an inbox before processing.
 * Implements the inbox pattern for reliable external event processing.
 */
@Entity
@Table(name = "external_event_inbox")
public class ExternalEventInboxEntity {

    @Id
    private UUID id;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "payload", columnDefinition = "TEXT", nullable = false)
    private String payload;

    @Column(name = "source_service")
    private String sourceService;

    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private InboxStatus status;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    @Column(name = "processed_at")
    private Instant processedAt;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    // Constructors
    protected ExternalEventInboxEntity() {}

    public ExternalEventInboxEntity(String eventType, String payload, String sourceService) {
        this.id = UUID.randomUUID();
        this.eventType = eventType;
        this.payload = payload;
        this.sourceService = sourceService;
        this.receivedAt = Instant.now();
        this.status = InboxStatus.PENDING;
        this.retryCount = 0;
    }

    // Methods
    public void markAsProcessed() {
        this.status = InboxStatus.PROCESSED;
        this.processedAt = Instant.now();
        this.errorMessage = null;
    }

    public void markAsFailed(String error) {
        this.retryCount++;
        this.errorMessage = error;
        this.status = retryCount >= 3 ? InboxStatus.FAILED : InboxStatus.PENDING;
    }

    public void resetForRetry() {
        this.status = InboxStatus.PENDING;
        this.errorMessage = null;
        // Keep retryCount as is for tracking
    }

    // Getters
    public UUID getEventId() { return id; }
    public String getEventType() { return eventType; }
    public String getPayload() { return payload; }
    public String getSourceService() { return sourceService; }
    public Instant getReceivedAt() { return receivedAt; }
    public InboxStatus getStatus() { return status; }
    public int getRetryCount() { return retryCount; }
    public Instant getProcessedAt() { return processedAt; }
    public String getErrorMessage() { return errorMessage; }

    public enum InboxStatus {
        PENDING,
        PROCESSED,
        FAILED
    }
}