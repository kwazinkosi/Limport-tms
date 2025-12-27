package com.limport.tms.infrastructure.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.limport.tms.domain.event.EventTypes;
import com.limport.tms.domain.event.states.TransportRequestCreatedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UnifiedEventSerializerTest {

    private ObjectMapper objectMapper;
    private UnifiedEventSerializer serializer;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        // Create serializer with empty external event deserializers list
        serializer = new UnifiedEventSerializer(objectMapper, Collections.emptyList());
    }

    @Test
    void serialize_IncludesVersionInPayload() throws Exception {
        // Given
        TransportRequestCreatedEvent event = new TransportRequestCreatedEvent(
            UUID.randomUUID(), "user-123", "Origin", "Destination", Map.of("weightKg", 100.0));

        // When
        String serialized = serializer.serialize(event);

        // Then
        JsonNode jsonNode = objectMapper.readTree(serialized);
        assertTrue(jsonNode.has("version"));
        assertEquals(EventTypes.Transport.Request.CREATED, jsonNode.get("eventType").asText());
    }

    @Test
    void serialize_IncludesEventTypeInPayload() throws Exception {
        // Given
        TransportRequestCreatedEvent event = new TransportRequestCreatedEvent(
            UUID.randomUUID(), "user-456", "TestOrigin", "TestDestination", Map.of("priority", "high"));

        // When
        String serialized = serializer.serialize(event);

        // Then
        JsonNode jsonNode = objectMapper.readTree(serialized);
        assertEquals(EventTypes.Transport.Request.CREATED, jsonNode.get("eventType").asText());
        assertEquals("TestOrigin", jsonNode.get("origin").asText());
        assertEquals("TestDestination", jsonNode.get("destination").asText());
    }

    @Test
    void deserialize_UnknownEventType_ThrowsException() {
        // Given
        String json = """
            {
                "version": 1,
                "eventType": "UnknownEvent",
                "aggregateId": "test-id"
            }
            """;

        // When & Then
        assertThrows(UnifiedEventSerializer.EventSerializationException.class, 
            () -> serializer.deserialize(json, "UnknownEvent"));
    }

    @Test
    void deserialize_InvalidJson_ThrowsException() {
        // Given
        String invalidJson = "{ invalid json }";

        // When & Then
        assertThrows(UnifiedEventSerializer.EventSerializationException.class, 
            () -> serializer.deserialize(invalidJson, EventTypes.Transport.Request.CREATED));
    }

    @Test
    void serialize_PreservesAllEventData() throws Exception {
        // Given
        UUID requestId = UUID.randomUUID();
        Map<String, Object> details = Map.of("weightKg", 500.0, "notes", "Fragile cargo");
        TransportRequestCreatedEvent originalEvent = new TransportRequestCreatedEvent(
            requestId, "user-789", "Cape Town", "Johannesburg", details);

        // When
        String serialized = serializer.serialize(originalEvent);

        // Then
        JsonNode jsonNode = objectMapper.readTree(serialized);
        assertEquals(requestId.toString(), jsonNode.get("transportRequestId").asText());
        assertEquals("Cape Town", jsonNode.get("origin").asText());
        assertEquals("Johannesburg", jsonNode.get("destination").asText());
        assertEquals("user-789", jsonNode.get("userId").asText());
    }

    @Test
    void canDeserializeExternalEvent_UnknownType_ReturnsFalse() {
        // When & Then
        assertFalse(serializer.canDeserializeExternalEvent("Unknown.Event.Type"));
    }

    @Test
    void deserializeExternalEvent_InvalidPayload_ReturnsEmpty() {
        // Given
        String invalidPayload = "{ not valid json";

        // When
        var result = serializer.deserializeExternalEvent(invalidPayload);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void deserializeExternalEvent_MissingEventType_ReturnsEmpty() {
        // Given
        String payloadWithoutEventType = """
            {
                "data": "some data"
            }
            """;

        // When
        var result = serializer.deserializeExternalEvent(payloadWithoutEventType);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void deserializeExternalEvent_UnknownEventType_ReturnsEmpty() {
        // Given
        String payloadWithUnknownType = """
            {
                "eventType": "Unknown.Event.Type",
                "data": "some data"
            }
            """;

        // When
        var result = serializer.deserializeExternalEvent(payloadWithUnknownType);

        // Then
        assertTrue(result.isEmpty());
    }
}