package com.limport.tms.infrastructure.event.listener;

import com.limport.tms.application.service.interfaces.IEventSerializer;
import com.limport.tms.domain.event.IDomainEvent;
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
    
    private final IEventSerializer eventSerializer;
    
    public TransportEventListener(IEventSerializer eventSerializer) {
        this.eventSerializer = eventSerializer;
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
                eventSerializer.deserialize(payload, "TransportEvents.Request.Created");
            
            log.info("Received TransportRequestCreated: id={}, origin={}, destination={}",
                event.getTransportRequestId(),
                event.getOrigin(),
                event.getDestination());
            
            // Trigger downstream processes:
            // - Capacity verification
            // - Route optimization
            // - Provider matching
            
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
                eventSerializer.deserialize(payload, "TransportEvents.Request.Assigned");
            
            log.info("Received TransportRequestAssigned: requestId={}, providerId={}, vehicleId={}",
                event.getTransportRequestId(),
                event.getProviderId(),
                event.getVehicleId());
            
            // Trigger downstream processes:
            // - Notify provider
            // - Update scheduling system
            // - Send customer notification
            
        } catch (Exception e) {
            log.error("Failed to process request assigned event: {}", e.getMessage(), e);
        }
    }
}
