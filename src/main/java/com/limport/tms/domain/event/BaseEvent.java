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
        this.causationId = generateOrInheritCausationId(eventId.toString());
    }

    protected BaseEvent(int version) {
        this.eventId = UUID.randomUUID();
        this.occurredOn = Instant.now();
        this.version = version;
        this.correlationId = generateOrInheritCorrelationId();
        this.causationId = generateOrInheritCausationId(eventId.toString());
    }

    protected BaseEvent(String correlationId, String causationId) {
        this.eventId = UUID.randomUUID();
        this.occurredOn = Instant.now();
        this.version = 1;
        this.correlationId = correlationId != null ? correlationId : generateOrInheritCorrelationId();
        this.causationId = causationId != null ? causationId : generateOrInheritCausationId(eventId.toString());
    }

    protected BaseEvent(int version, String correlationId, String causationId) {
        this.eventId = UUID.randomUUID();
        this.occurredOn = Instant.now();
        this.version = version;
        this.correlationId = correlationId != null ? correlationId : generateOrInheritCorrelationId();
        this.causationId = causationId != null ? causationId : generateOrInheritCausationId(eventId.toString());
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
        return CorrelationIdContext.getOrGenerateCorrelationId();
    }

    /**
     * Generates or inherits causation ID from thread-local context.
     * For root events (no existing causation), uses the provided default (typically eventId).
     * For events caused by other events, uses the causation ID from context.
     */
    private String generateOrInheritCausationId(String defaultCausationId) {
        String existingCausationId = CorrelationIdContext.getCausationId();
        return existingCausationId != null ? existingCausationId : defaultCausationId;
    }
}