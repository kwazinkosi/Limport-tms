package com.limport.tms.domain.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Base interface for all domain events in TMS.
 * Provides common event metadata contract.
 */
public interface IDomainEvent {
    
    /**
     * Unique identifier for this event instance.
     */
    UUID getEventId();
    
    /**
     * Timestamp when the event occurred.
     */
    Instant occurredOn();
    
    /**
     * Fully qualified event type name (e.g., "TransportEvents.Request.Created").
     */
    String eventType();
}