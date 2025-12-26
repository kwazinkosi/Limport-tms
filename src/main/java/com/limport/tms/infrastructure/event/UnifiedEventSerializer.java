package com.limport.tms.infrastructure.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.limport.tms.application.event.ExternalEvent;
import com.limport.tms.application.service.interfaces.IUnifiedEventSerializer;
import com.limport.tms.domain.event.IDomainEvent;
import com.limport.tms.domain.event.states.*;
import com.limport.tms.infrastructure.event.consumer.IEventDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Unified event serializer that handles both domain and external events.
 *
 * Combines the functionality of JsonEventSerializer and ExternalEventDeserializer
 * to eliminate redundancy and provide consistent serialization across the application.
 */
@Component
public class UnifiedEventSerializer implements IUnifiedEventSerializer {

    private static final Logger log = LoggerFactory.getLogger(UnifiedEventSerializer.class);

    private final ObjectMapper objectMapper;
    private final Map<String, Class<? extends IDomainEvent>> domainEventRegistry;
    private final Map<String, IEventDeserializer<? extends ExternalEvent>> externalEventDeserializerRegistry;

    public UnifiedEventSerializer(
            ObjectMapper objectMapper,
            List<IEventDeserializer<? extends ExternalEvent>> externalEventDeserializers) {
        this.objectMapper = objectMapper;
        this.domainEventRegistry = createDomainEventRegistry();
        this.externalEventDeserializerRegistry = createExternalEventDeserializerRegistry(externalEventDeserializers);

        log.info("Initialized UnifiedEventSerializer with {} domain event types and {} external event deserializers",
            domainEventRegistry.size(), externalEventDeserializerRegistry.size());
    }

    private Map<String, Class<? extends IDomainEvent>> createDomainEventRegistry() {
        Map<String, Class<? extends IDomainEvent>> registry = new ConcurrentHashMap<>();

        // Register all transport request events
        registry.put("TransportEvents.Request.Created", TransportRequestCreatedEvent.class);
        registry.put("TransportEvents.Request.Updated", TransportRequestUpdatedEvent.class);
        registry.put("TransportEvents.Request.Cancelled", TransportRequestCancelledEvent.class);
        registry.put("TransportEvents.Request.Assigned", TransportRequestAssignedEvent.class);
        registry.put("TransportEvents.Request.Completed", TransportRequestCompletedEvent.class);

        // Register route optimization events (TMS responsibility)
        registry.put("TransportEvents.Route.Optimized", TransportRouteOptimizedEvent.class);

        return registry;
    }

    private Map<String, IEventDeserializer<? extends ExternalEvent>> createExternalEventDeserializerRegistry(
            List<IEventDeserializer<? extends ExternalEvent>> deserializers) {
        return deserializers.stream()
            .collect(Collectors.toMap(
                IEventDeserializer::supportedEventType,
                Function.identity(),
                (existing, replacement) -> {
                    log.warn("Duplicate external event deserializer for event type {}. Using: {}",
                        existing.supportedEventType(), replacement.getClass().getSimpleName());
                    return replacement;
                }
            ));
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
    public IDomainEvent deserializeDomainEvent(String payload, String eventType) {
        Class<? extends IDomainEvent> eventClass = domainEventRegistry.get(eventType);

        if (eventClass == null) {
            throw new EventSerializationException("Unknown domain event type: " + eventType);
        }

        try {
            return objectMapper.readValue(payload, eventClass);
        } catch (JsonProcessingException e) {
            throw new EventSerializationException("Failed to deserialize domain event: " + eventType, e);
        }
    }

    @Override
    public Optional<ExternalEvent> deserializeExternalEvent(String jsonPayload) {
        try {
            JsonNode rootNode = objectMapper.readTree(jsonPayload);
            String eventType = extractEventType(rootNode);

            if (eventType == null) {
                log.warn("Cannot deserialize external event: missing 'eventType' field");
                return Optional.empty();
            }

            IEventDeserializer<? extends ExternalEvent> deserializer = externalEventDeserializerRegistry.get(eventType);
            if (deserializer == null) {
                log.warn("Unknown external event type: {}. Event will be skipped. Registered types: {}",
                    eventType, externalEventDeserializerRegistry.keySet());
                return Optional.empty();
            }

            ExternalEvent event = deserializer.deserialize(rootNode);
            log.debug("Successfully deserialized external event: {} (id={})",
                eventType, event.eventId());
            return Optional.of(event);

        } catch (JsonProcessingException e) {
            log.error("Failed to parse JSON payload: {}", e.getMessage());
            return Optional.empty();
        } catch (IllegalArgumentException e) {
            log.error("Invalid external event data: {}", e.getMessage());
            return Optional.empty();
        } catch (Exception e) {
            log.error("Unexpected error deserializing external event: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    public IDomainEvent deserialize(String payload, String eventType) {
        return deserializeDomainEvent(payload, eventType);
    }

    private String extractEventType(JsonNode rootNode) {
        JsonNode eventTypeNode = rootNode.get("eventType");
        return eventTypeNode != null && !eventTypeNode.isNull() ? eventTypeNode.asText() : null;
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

    @Override
    public boolean canDeserializeExternalEvent(String eventType) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'canDeserializeExternalEvent'");
    }
}