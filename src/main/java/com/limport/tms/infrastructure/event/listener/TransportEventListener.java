package com.limport.tms.infrastructure.event.listener;

import com.limport.tms.application.service.interfaces.ITransportEventService;
import com.limport.tms.application.service.interfaces.IUnifiedEventSerializer;
import com.limport.tms.domain.event.EventTypes;
import com.limport.tms.domain.event.states.TransportRequestAssignedEvent;
import com.limport.tms.domain.event.states.TransportRequestCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Kafka listener for transport request events.
 * Demonstrates how other services can consume TMS events.
 */
@Component
public class TransportEventListener {
    
    private static final Logger log = LoggerFactory.getLogger(TransportEventListener.class);
    
    private final IUnifiedEventSerializer eventSerializer;
    private final ITransportEventService transportEventService;
    
    public TransportEventListener(
            IUnifiedEventSerializer eventSerializer,
            ITransportEventService transportEventService) {
        this.eventSerializer = eventSerializer;
        this.transportEventService = transportEventService;
    }
    
    /**
     * Handles new transport request creation.
     * Could trigger provider matching, capacity verification, etc.
     */
    @KafkaListener(
        topics = "${tms.kafka.topic-prefix:tms.events}.request-created",
        groupId = "tms-internal"
    )
    public void handleRequestCreated(
            @Payload String payload,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        
        try {
            TransportRequestCreatedEvent event = (TransportRequestCreatedEvent) 
                eventSerializer.deserialize(payload, EventTypes.Transport.Request.CREATED);
            
            // Delegate business logic to application service
            transportEventService.handleTransportRequestCreated(event);
            
        } catch (Exception e) {
            log.error("Failed to process request created event: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Handles provider assignment events.
     * Could trigger notifications, scheduling, etc.
     */
    @KafkaListener(
        topics = "${tms.kafka.topic-prefix:tms.events}.request-assigned",
        groupId = "tms-internal"
    )
    public void handleRequestAssigned(
            @Payload String payload,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        
        try {
            TransportRequestAssignedEvent event = (TransportRequestAssignedEvent) 
                eventSerializer.deserialize(payload, EventTypes.Transport.Request.ASSIGNED);
            
            // Delegate business logic to application service
            transportEventService.handleTransportRequestAssigned(event);
            
        } catch (Exception e) {
            log.error("Failed to process request assigned event: {}", e.getMessage(), e);
        }
    }
}
