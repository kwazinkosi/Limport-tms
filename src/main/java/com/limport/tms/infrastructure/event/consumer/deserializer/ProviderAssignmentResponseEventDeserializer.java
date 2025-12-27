package com.limport.tms.infrastructure.event.consumer.deserializer;

import com.fasterxml.jackson.databind.JsonNode;
import com.limport.tms.application.event.pms.ProviderAssignmentResponseEvent;
import com.limport.tms.domain.event.EventTypes;
import com.limport.tms.infrastructure.event.consumer.BaseEventDeserializer;
import com.limport.tms.infrastructure.event.consumer.IEventDeserializer;
import org.springframework.stereotype.Component;

/**
 * Deserializer for ProviderAssignmentResponseEvent from PMS.
 * 
 * Self-registering component that handles deserialization of
 * provider assignment response events from Kafka messages.
 */
@Component
public class ProviderAssignmentResponseEventDeserializer 
        extends BaseEventDeserializer 
        implements IEventDeserializer<ProviderAssignmentResponseEvent> {
    
    private static final String EVENT_TYPE = EventTypes.Provider.ASSIGNMENT_RESPONSE;
    
    @Override
    public String supportedEventType() {
        return EVENT_TYPE;
    }
    
    @Override
    public ProviderAssignmentResponseEvent deserialize(JsonNode node) {
        return new ProviderAssignmentResponseEvent(
            getRequiredUUID(node, "eventId"),
            getRequiredInstant(node, "occurredOn"),
            "PMS", // sourceService
            getRequiredUUID(node, "transportRequestId"),
            getUUID(node, "assignmentId"),
            getRequiredUUID(node, "providerId"),
            getText(node, "providerName"),
            getRequiredEnum(node, "response", ProviderAssignmentResponseEvent.AssignmentResponse.class),
            getText(node, "responseReason"),
            getInstant(node, "respondedAt")
        );
    }
    
    @Override
    public Class<ProviderAssignmentResponseEvent> getEventClass() {
        return ProviderAssignmentResponseEvent.class;
    }
}
