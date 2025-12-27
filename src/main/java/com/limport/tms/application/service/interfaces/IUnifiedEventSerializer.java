package com.limport.tms.application.service.interfaces;

import com.limport.tms.domain.event.IDomainEvent;
import com.limport.tms.application.event.ExternalEvent;

import java.util.Optional;

/**
 * Unified interface for event serialization/deserialization.
 *
 * Supports both domain events (internal) and external events.
 * Provides consistent ObjectMapper usage across the application.
 */
public interface IUnifiedEventSerializer {

    /**
     * Serializes a domain event to JSON string.
     * @param event the event to serialize
     * @return JSON representation
     */
    String serialize(IDomainEvent event);

    /**
     * Deserializes a JSON string to a domain event.
     * @param payload the JSON payload
     * @param eventType the fully qualified event type
     * @return the deserialized event
     */
    IDomainEvent deserialize(String payload, String eventType);

    /**
     * Deserializes a JSON string to a domain event with enhanced error handling.
     * @param payload the JSON payload
     * @param eventType the fully qualified event type
     * @return the deserialized event
     */
    IDomainEvent deserializeDomainEvent(String payload, String eventType);

    // External event deserialization
    Optional<ExternalEvent> deserializeExternalEvent(String jsonPayload);

    // Utility methods
    boolean canDeserializeExternalEvent(String eventType);
}