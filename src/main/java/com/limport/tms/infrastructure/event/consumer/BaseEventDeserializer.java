package com.limport.tms.infrastructure.event.consumer;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.UUID;

/**
 * Base class providing common JSON extraction utilities for event deserializers.
 * 
 * Centralizes null-safe field extraction to avoid duplication across
 * individual deserializer implementations.
 */
public abstract class BaseEventDeserializer {
    
    /**
     * Extract UUID from JSON field.
     * 
     * @param node Parent JSON node
     * @param field Field name
     * @return UUID value or null if field is missing/null
     */
    protected UUID getUUID(JsonNode node, String field) {
        JsonNode fieldNode = node.get(field);
        if (fieldNode == null || fieldNode.isNull()) {
            return null;
        }
        return UUID.fromString(fieldNode.asText());
    }
    
    /**
     * Extract required UUID from JSON field.
     * 
     * @param node Parent JSON node
     * @param field Field name
     * @return UUID value
     * @throws IllegalArgumentException if field is missing or null
     */
    protected UUID getRequiredUUID(JsonNode node, String field) {
        UUID value = getUUID(node, field);
        if (value == null) {
            throw new IllegalArgumentException("Required field '" + field + "' is missing or null");
        }
        return value;
    }
    
    /**
     * Extract Instant from JSON field.
     * 
     * @param node Parent JSON node
     * @param field Field name
     * @return Instant value or null if field is missing/null
     */
    protected Instant getInstant(JsonNode node, String field) {
        JsonNode fieldNode = node.get(field);
        if (fieldNode == null || fieldNode.isNull()) {
            return null;
        }
        return Instant.parse(fieldNode.asText());
    }
    
    /**
     * Extract required Instant from JSON field.
     * 
     * @param node Parent JSON node
     * @param field Field name
     * @return Instant value
     * @throws IllegalArgumentException if field is missing or null
     */
    protected Instant getRequiredInstant(JsonNode node, String field) {
        Instant value = getInstant(node, field);
        if (value == null) {
            throw new IllegalArgumentException("Required field '" + field + "' is missing or null");
        }
        return value;
    }
    
    /**
     * Extract String from JSON field.
     * 
     * @param node Parent JSON node
     * @param field Field name
     * @return String value or null if field is missing/null
     */
    protected String getText(JsonNode node, String field) {
        JsonNode fieldNode = node.get(field);
        if (fieldNode == null || fieldNode.isNull()) {
            return null;
        }
        return fieldNode.asText();
    }
    
    /**
     * Extract required String from JSON field.
     * 
     * @param node Parent JSON node
     * @param field Field name
     * @return String value
     * @throws IllegalArgumentException if field is missing or null
     */
    protected String getRequiredText(JsonNode node, String field) {
        String value = getText(node, field);
        if (value == null) {
            throw new IllegalArgumentException("Required field '" + field + "' is missing or null");
        }
        return value;
    }
    
    /**
     * Extract double from JSON field.
     * 
     * @param node Parent JSON node
     * @param field Field name
     * @return double value or 0.0 if field is missing/null
     */
    protected double getDouble(JsonNode node, String field) {
        JsonNode fieldNode = node.get(field);
        if (fieldNode == null || fieldNode.isNull()) {
            return 0.0;
        }
        return fieldNode.asDouble();
    }
    
    /**
     * Extract int from JSON field.
     * 
     * @param node Parent JSON node
     * @param field Field name
     * @return int value or 0 if field is missing/null
     */
    protected int getInt(JsonNode node, String field) {
        JsonNode fieldNode = node.get(field);
        if (fieldNode == null || fieldNode.isNull()) {
            return 0;
        }
        return fieldNode.asInt();
    }
    
    /**
     * Extract boolean from JSON field.
     * 
     * @param node Parent JSON node
     * @param field Field name
     * @return boolean value or false if field is missing/null
     */
    protected boolean getBoolean(JsonNode node, String field) {
        JsonNode fieldNode = node.get(field);
        if (fieldNode == null || fieldNode.isNull()) {
            return false;
        }
        return fieldNode.asBoolean();
    }
    
    /**
     * Extract enum from JSON field.
     * 
     * @param node Parent JSON node
     * @param field Field name
     * @param enumClass The enum class
     * @return enum value or null if field is missing/null
     */
    protected <E extends Enum<E>> E getEnum(JsonNode node, String field, Class<E> enumClass) {
        String text = getText(node, field);
        if (text == null) {
            return null;
        }
        return Enum.valueOf(enumClass, text);
    }
    
    /**
     * Extract required enum from JSON field.
     * 
     * @param node Parent JSON node
     * @param field Field name
     * @param enumClass The enum class
     * @return enum value
     * @throws IllegalArgumentException if field is missing or null
     */
    protected <E extends Enum<E>> E getRequiredEnum(JsonNode node, String field, Class<E> enumClass) {
        E value = getEnum(node, field, enumClass);
        if (value == null) {
            throw new IllegalArgumentException("Required field '" + field + "' is missing or null");
        }
        return value;
    }
}
