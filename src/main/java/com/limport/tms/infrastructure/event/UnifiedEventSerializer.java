package com.limport.tms.infrastructure.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.limport.tms.application.event.ExternalEvent;
import com.limport.tms.application.service.interfaces.IUnifiedEventSerializer;
import com.limport.tms.domain.event.EventTypes;
import com.limport.tms.domain.event.IDomainEvent;
import com.limport.tms.domain.event.states.*;
import com.limport.tms.infrastructure.event.consumer.IEventDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
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
@Primary
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
        registry.put(EventTypes.Transport.Request.CREATED, TransportRequestCreatedEvent.class);
        registry.put(EventTypes.Transport.Request.UPDATED, TransportRequestUpdatedEvent.class);
        registry.put(EventTypes.Transport.Request.CANCELLED, TransportRequestCancelledEvent.class);
        registry.put(EventTypes.Transport.Request.ASSIGNED, TransportRequestAssignedEvent.class);
        registry.put(EventTypes.Transport.Request.COMPLETED, TransportRequestCompletedEvent.class);

        // Register route optimization events (TMS responsibility)
        registry.put(EventTypes.Transport.Route.OPTIMIZED, TransportRouteOptimizedEvent.class);

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
            // Create a wrapper with version information
            ObjectNode wrapper = objectMapper.createObjectNode();
            wrapper.put("version", event.getVersion());
            wrapper.put("eventType", event.eventType());
            
            // Serialize the event data
            String eventJson = objectMapper.writeValueAsString(event);
            JsonNode eventNode = objectMapper.readTree(eventJson);
            
            // Add all event fields to the wrapper
            eventNode.fields().forEachRemaining(entry -> 
                wrapper.set(entry.getKey(), entry.getValue()));
            
            return objectMapper.writeValueAsString(wrapper);
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
            JsonNode jsonNode = objectMapper.readTree(payload);
            
            // Extract version for schema evolution handling
            int version = extractVersion(jsonNode);
            int currentVersion = getCurrentVersion(eventType);
            
            if (version > currentVersion) {
                throw new EventSerializationException(
                    "Event version " + version + " is newer than supported version " + currentVersion + 
                    " for event type: " + eventType + ". Please upgrade the application.");
            }
            
            if (version < currentVersion) {
                log.debug("Deserializing event {} with older version {} (current: {})", 
                    eventType, version, currentVersion);
                
                // Apply version migration
                jsonNode = (ObjectNode) migrateEvent((ObjectNode) jsonNode, eventType, version, currentVersion);
            }
            
            // Remove wrapper fields before deserializing
            ObjectNode cleanNode = jsonNode.deepCopy();
            cleanNode.remove("version");
            cleanNode.remove("eventType");
            
            return objectMapper.readValue(cleanNode.toString(), eventClass);
        } catch (JsonProcessingException e) {
            throw new EventSerializationException("Failed to deserialize domain event: " + eventType, e);
        }
    }
    
    /**
     * Extracts the version from the event JSON.
     * Defaults to 1 if not present for backward compatibility.
     */
    private int extractVersion(JsonNode jsonNode) {
        JsonNode versionNode = jsonNode.get("version");
        return versionNode != null ? versionNode.asInt(1) : 1;
    }
    
    /**
     * Gets the current supported version for an event type.
     * This should be maintained as events evolve.
     */
    private int getCurrentVersion(String eventType) {
        return eventVersionRegistry.getOrDefault(eventType, 1);
    }

    /**
     * Registry of current versions for event types.
     * Update this as event schemas evolve.
     */
    private final Map<String, Integer> eventVersionRegistry = Map.of(
        EventTypes.Transport.Request.CREATED, 1,
        EventTypes.Transport.Request.UPDATED, 1,
        EventTypes.Transport.Request.CANCELLED, 1,
        EventTypes.Transport.Request.ASSIGNED, 1,
        EventTypes.Transport.Request.COMPLETED, 1,
        EventTypes.Transport.Route.OPTIMIZED, 1
    );

    /**
     * Migrates an event from an older version to the current version.
     * This method should be updated as new versions are introduced.
     */
    private ObjectNode migrateEvent(ObjectNode eventNode, String eventType, int fromVersion, int toVersion) {
        ObjectNode migratedNode = eventNode.deepCopy();
        
        // Apply migrations step by step
        for (int version = fromVersion + 1; version <= toVersion; version++) {
            migratedNode = applyMigration(migratedNode, eventType, version);
        }
        
        return migratedNode;
    }

    /**
     * Applies a specific version migration.
     * Add new migration logic here as versions evolve.
     */
    private ObjectNode applyMigration(ObjectNode eventNode, String eventType, int targetVersion) {
        // Example migration patterns:
        // - Add new required fields with defaults
        // - Rename fields
        // - Transform field values
        // - Remove deprecated fields
        
        switch (targetVersion) {
            case 2:
                // Example: Add new field with default value
                if (!eventNode.has("newRequiredField")) {
                    eventNode.put("newRequiredField", "defaultValue");
                }
                break;
                
            // Add more version cases as needed
            default:
                log.warn("No migration logic defined for version {} of event type {}", targetVersion, eventType);
        }
        
        return eventNode;
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
    public boolean canDeserializeExternalEvent(String eventType) {
        return externalEventDeserializerRegistry.containsKey(eventType);
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
}