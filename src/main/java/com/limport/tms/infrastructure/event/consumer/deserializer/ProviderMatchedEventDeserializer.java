package com.limport.tms.infrastructure.event.consumer.deserializer;

import com.fasterxml.jackson.databind.JsonNode;
import com.limport.tms.application.event.pms.ProviderMatchedEvent;
import com.limport.tms.domain.event.EventTypes;
import com.limport.tms.infrastructure.event.consumer.BaseEventDeserializer;
import com.limport.tms.infrastructure.event.consumer.IEventDeserializer;
import org.springframework.stereotype.Component;

/**
 * Deserializer for ProviderMatchedEvent from PMS.
 * 
 * Self-registering component that handles deserialization of
 * provider match events from Kafka messages.
 */
@Component
public class ProviderMatchedEventDeserializer 
        extends BaseEventDeserializer 
        implements IEventDeserializer<ProviderMatchedEvent> {
    
    private static final String EVENT_TYPE = EventTypes.Provider.MATCHED;
    
    @Override
    public String supportedEventType() {
        return EVENT_TYPE;
    }
    
    @Override
    public ProviderMatchedEvent deserialize(JsonNode node) {
        return new ProviderMatchedEvent(
            getRequiredUUID(node, "eventId"),
            getRequiredInstant(node, "occurredOn"),
            "PMS", // sourceService
            getRequiredUUID(node, "transportRequestId"),
            getRequiredUUID(node, "providerId"),
            getText(node, "providerName"),
            getUUID(node, "vehicleId"),
            getText(node, "vehicleType"),
            getDouble(node, "matchScore"),
            getDouble(node, "estimatedCostZAR"),
            getDouble(node, "availableCapacityKg")
        );
    }
    
    @Override
    public Class<ProviderMatchedEvent> getEventClass() {
        return ProviderMatchedEvent.class;
    }
}
