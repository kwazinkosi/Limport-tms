package com.limport.tms.application.service.interfaces;

import com.limport.tms.domain.event.IDomainEvent;

/**
 * Port for event serialization/deserialization.
 * Allows domain events to be converted to/from a transportable format.
 */
public interface IEventSerializer {
    
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
}
