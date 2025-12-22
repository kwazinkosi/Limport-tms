package com.limport.tms.infrastructure.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.limport.tms.application.service.interfaces.IEventSerializer;
import com.limport.tms.domain.event.IDomainEvent;
import com.limport.tms.domain.event.states.*;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JSON-based event serializer using Jackson.
 * Handles serialization/deserialization of domain events.
 */
@Component
public class JsonEventSerializer implements IEventSerializer {
    
    private final ObjectMapper objectMapper;
    private final Map<String, Class<? extends IDomainEvent>> eventTypeRegistry;
    
    public JsonEventSerializer() {
        this.objectMapper = createObjectMapper();
        this.eventTypeRegistry = createEventTypeRegistry();
    }
    
    private ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        return mapper;
    }
    
    private Map<String, Class<? extends IDomainEvent>> createEventTypeRegistry() {
        Map<String, Class<? extends IDomainEvent>> registry = new ConcurrentHashMap<>();
        
        // Register all transport request events
        registry.put("TransportEvents.Request.Created", TransportRequestCreatedEvent.class);
        registry.put("TransportEvents.Request.Updated", TransportRequestUpdatedEvent.class);
        registry.put("TransportEvents.Request.Cancelled", TransportRequestCancelledEvent.class);
        registry.put("TransportEvents.Request.Assigned", TransportRequestAssignedEvent.class);
        registry.put("TransportEvents.Request.Completed", TransportRequestCompletedEvent.class);
        
        // Register capacity and route events
        registry.put("TransportEvents.Capacity.Verified", TransportCapacityVerifiedEvent.class);
        registry.put("TransportEvents.Route.Optimized", TransportRouteOptimizedEvent.class);
        
        return registry;
    }
    
    /**
     * Registers a new event type for deserialization.
     * Allows dynamic event registration if needed.
     */
    public void registerEventType(String eventType, Class<? extends IDomainEvent> eventClass) {
        eventTypeRegistry.put(eventType, eventClass);
    }
    
    @Override
    public String serialize(IDomainEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new EventSerializationException("Failed to serialize event: " + event.eventType(), e);
        }
    }
    
    @Override
    public IDomainEvent deserialize(String payload, String eventType) {
        Class<? extends IDomainEvent> eventClass = eventTypeRegistry.get(eventType);
        
        if (eventClass == null) {
            throw new EventSerializationException("Unknown event type: " + eventType);
        }
        
        try {
            return objectMapper.readValue(payload, eventClass);
        } catch (JsonProcessingException e) {
            throw new EventSerializationException("Failed to deserialize event: " + eventType, e);
        }
    }
    
    /**
     * Exception thrown when event serialization/deserialization fails.
     */
    public static class EventSerializationException extends RuntimeException {
        public EventSerializationException(String message) {
            super(message);
        }
        
        public EventSerializationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
