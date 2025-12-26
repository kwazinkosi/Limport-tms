package com.limport.tms.infrastructure.event.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.limport.tms.application.event.ExternalEvent;

/**
 * Interface for self-registering event deserializers.
 * 
 * Each event type has its own deserializer that registers itself,
 * following the Open/Closed Principle - new event types can be added
 * without modifying the main deserializer.
 * 
 * @param <T> The specific external event type this deserializer handles
 */
public interface IEventDeserializer<T extends ExternalEvent> {
    
    /**
     * The event type string this deserializer handles.
     * Must match the "eventType" field in incoming JSON.
     * 
     * @return Event type identifier (e.g., "ProviderEvents.Matched")
     */
    String supportedEventType();
    
    /**
     * Deserialize JSON node to the specific event type.
     * 
     * @param jsonNode The parsed JSON node containing event data
     * @return The deserialized event
     * @throws IllegalArgumentException if required fields are missing or invalid
     */
    T deserialize(JsonNode jsonNode);
    
    /**
     * Get the event class this deserializer produces.
     * Used for type information and logging.
     * 
     * @return The event class
     */
    Class<T> getEventClass();
    
    /**
     * Check if this deserializer supports the given event type.
     * 
     * @param eventType The event type to check
     * @return true if this deserializer handles the event type
     */
    default boolean supports(String eventType) {
        return supportedEventType().equals(eventType);
    }
}
