package com.limport.tms.application.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Base interface for all external events consumed from other services.
 * Application layer defines contracts; infrastructure provides implementations.
 * 
 * External events are received ASYNCHRONOUSLY via Kafka, in contrast to
 * synchronous REST calls for commands/queries.
 */
public interface ExternalEvent {
    
    /**
     * Unique event identifier.
     */
    UUID eventId();
    
    /**
     * Event type string (e.g., "ProviderEvents.Matched").
     */
    String eventType();
    
    /**
     * When the event occurred at source.
     */
    Instant occurredOn();
    
    /**
     * Source service that published the event.
     */
    String sourceService();
}
