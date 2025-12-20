package com.limport.tms.domain.event;

import java.time.Instant;
import java.util.UUID;


public abstract class BaseEvent implements IDomainEvent {
    private final UUID eventId;
    private final Instant occurredOn;

    protected BaseEvent() {
        this.eventId = UUID.randomUUID();
        this.occurredOn = Instant.now();
    }

    public UUID getEventId() {
        return eventId;
    }

    @Override
    public Instant occurredOn() {
        return occurredOn;
    }

    // This will be included in JSON serialization
    public abstract String eventType();
}