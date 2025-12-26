package com.limport.tms.domain.event;

import java.time.Instant;
import java.util.UUID;


public abstract class BaseEvent implements IDomainEvent {
    private final UUID eventId;
    private final Instant occurredOn;
    private final int version;
    private final String correlationId;
    private final String causationId;

    protected BaseEvent() {
        this.eventId = UUID.randomUUID();
        this.occurredOn = Instant.now();
        this.version = 1; // Default version
        this.correlationId = generateOrInheritCorrelationId();
        this.causationId = eventId.toString(); // Self-causation for root events
    }

    protected BaseEvent(int version) {
        this.eventId = UUID.randomUUID();
        this.occurredOn = Instant.now();
        this.version = version;
        this.correlationId = generateOrInheritCorrelationId();
        this.causationId = eventId.toString(); // Self-causation for root events
    }

    protected BaseEvent(String correlationId, String causationId) {
        this.eventId = UUID.randomUUID();
        this.occurredOn = Instant.now();
        this.version = 1;
        this.correlationId = correlationId != null ? correlationId : generateOrInheritCorrelationId();
        this.causationId = causationId != null ? causationId : eventId.toString();
    }

    protected BaseEvent(int version, String correlationId, String causationId) {
        this.eventId = UUID.randomUUID();
        this.occurredOn = Instant.now();
        this.version = version;
        this.correlationId = correlationId != null ? correlationId : generateOrInheritCorrelationId();
        this.causationId = causationId != null ? causationId : eventId.toString();
    }

    public UUID getEventId() {
        return eventId;
    }

    @Override
    public Instant occurredOn() {
        return occurredOn;
    }

    public int getVersion() {
        return version;
    }

    @Override
    public String getCorrelationId() {
        return correlationId;
    }

    @Override
    public String getCausationId() {
        return causationId;
    }

    // This will be included in JSON serialization
    public abstract String eventType();

    /**
     * Generates or inherits correlation ID from thread-local context.
     * This enables distributed tracing across service boundaries.
     */
    private String generateOrInheritCorrelationId() {
        // Check if there's an existing correlation ID in thread-local or MDC
        // For now, generate a new one. In a real implementation, this would
        // check for existing correlation IDs from incoming requests/events
        return UUID.randomUUID().toString();
    }
}