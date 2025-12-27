package com.limport.tms.infrastructure.persistance.entity;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Entity representing events that failed processing and are stored in a dead letter queue.
 * Used for manual inspection and reprocessing of failed events.
 */
@Entity
@Table(name = "dead_letter_events")
public class DeadLetterEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false)
    private String eventId;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "event_payload", columnDefinition = "TEXT", nullable = false)
    private String eventPayload;

    @Column(name = "source", nullable = false)
    private String source; // "OUTBOX" or "INBOX"

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Column(name = "failure_count", nullable = false)
    private int failureCount = 1;

    @Column(name = "first_failed_at", nullable = false)
    private Instant firstFailedAt;

    @Column(name = "last_failed_at", nullable = false)
    private Instant lastFailedAt;

    @Column(name = "next_retry_at")
    private Instant nextRetryAt;

    @Column(name = "quarantined_at")
    private Instant quarantinedAt;

    @Column(name = "quarantine_reason", columnDefinition = "TEXT")
    private String quarantineReason;

    @Column(name = "processed_at")
    private Instant processedAt;

    protected DeadLetterEventEntity() {}

    public DeadLetterEventEntity(
            String eventId,
            String eventType,
            String eventPayload,
            String source,
            String failureReason) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.eventPayload = eventPayload;
        this.source = source;
        this.failureReason = failureReason;
        this.firstFailedAt = Instant.now();
        this.lastFailedAt = Instant.now();
    }

    // Getters
    public Long getId() { return id; }
    public String getEventId() { return eventId; }
    public String getEventType() { return eventType; }
    public String getEventPayload() { return eventPayload; }
    public String getSource() { return source; }
    public String getFailureReason() { return failureReason; }
    public int getFailureCount() { return failureCount; }
    public Instant getFirstFailedAt() { return firstFailedAt; }
    public Instant getLastFailedAt() { return lastFailedAt; }
    public Instant getNextRetryAt() { return nextRetryAt; }
    public Instant getQuarantinedAt() { return quarantinedAt; }
    public String getQuarantineReason() { return quarantineReason; }

    // Business methods
    public void incrementFailureCount() {
        this.failureCount++;
        this.lastFailedAt = Instant.now();
    }

    public void scheduleNextRetry(Instant nextRetryAt) {
        this.nextRetryAt = nextRetryAt;
    }

    public void markAsProcessed() {
        this.processedAt = Instant.now();
    }

    public void quarantine(String reason) {
        this.quarantinedAt = Instant.now();
        this.quarantineReason = reason;
        this.nextRetryAt = null; // Stop retries
    }

    public boolean isQuarantined() {
        return quarantinedAt != null;
    }

    public boolean isExpired(int maxRetries) {
        return failureCount >= maxRetries;
    }

    public boolean isReadyForRetry() {
        return nextRetryAt != null && Instant.now().isAfter(nextRetryAt) && !isQuarantined();
    }
}