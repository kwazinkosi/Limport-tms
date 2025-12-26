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
     * Version of the event schema.
     * Used for backward compatibility during schema evolution.
     */
    int getVersion();
    
    /**
     * Correlation ID for distributed tracing.
     * Links related events across service boundaries.
     */
    String getCorrelationId();
    
    /**
     * Causation ID for distributed tracing.
     * Identifies the event that caused this event.
     */
    String getCausationId();
    
    /**
     * Fully qualified event type name (e.g., "TMS.Transport.Request.Created").
     */
    String eventType();
}