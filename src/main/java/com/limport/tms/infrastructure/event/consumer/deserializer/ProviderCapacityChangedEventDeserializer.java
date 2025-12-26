package com.limport.tms.infrastructure.event.consumer.deserializer;

import com.fasterxml.jackson.databind.JsonNode;
import com.limport.tms.application.event.pms.ProviderCapacityChangedEvent;
import com.limport.tms.infrastructure.event.consumer.BaseEventDeserializer;
import com.limport.tms.infrastructure.event.consumer.IEventDeserializer;
import org.springframework.stereotype.Component;

/**
 * Deserializer for ProviderCapacityChangedEvent from PMS.
 * 
 * Self-registering component that handles deserialization of
 * provider capacity change events from Kafka messages.
 */
@Component
public class ProviderCapacityChangedEventDeserializer 
        extends BaseEventDeserializer 
        implements IEventDeserializer<ProviderCapacityChangedEvent> {
    
    private static final String EVENT_TYPE = "ProviderEvents.CapacityChanged";
    
    @Override
    public String supportedEventType() {
        return EVENT_TYPE;
    }
    
    @Override
    public ProviderCapacityChangedEvent deserialize(JsonNode node) {
        return new ProviderCapacityChangedEvent(
            getRequiredUUID(node, "eventId"),
            getRequiredInstant(node, "occurredOn"),
            "PMS", // sourceService
            getRequiredUUID(node, "providerId"),
            getText(node, "providerName"),
            getDouble(node, "previousCapacityKg"),
            getDouble(node, "newCapacityKg"),
            getText(node, "changeReason")
        );
    }
    
    @Override
    public Class<ProviderCapacityChangedEvent> getEventClass() {
        return ProviderCapacityChangedEvent.class;
    }
}
