package com.limport.tms.infrastructure.event.publisher;

import com.limport.tms.application.service.interfaces.IEventSerializer;
import com.limport.tms.domain.event.IDomainEvent;
import com.limport.tms.domain.event.TransportEvent;
import com.limport.tms.domain.ports.IEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Kafka-based implementation of the event publisher.
 * Publishes domain events to Kafka topics for consumption by other services.
 */
@Component
public class KafkaEventPublisher implements IEventPublisher {
    
    private static final Logger log = LoggerFactory.getLogger(KafkaEventPublisher.class);
    
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final IEventSerializer eventSerializer;
    
    @Value("${tms.kafka.topic-prefix:tms.events}")
    private String topicPrefix;
    
    public KafkaEventPublisher(
            KafkaTemplate<String, String> kafkaTemplate,
            IEventSerializer eventSerializer) {
        this.kafkaTemplate = kafkaTemplate;
        this.eventSerializer = eventSerializer;
    }
    
    @Override
    public void publish(IDomainEvent event) {
        String topic = buildTopic(event);
        String key = buildKey(event);
        String payload = eventSerializer.serialize(event);
        
        CompletableFuture<SendResult<String, String>> future = 
            kafkaTemplate.send(topic, key, payload);
        
        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish event {} to topic {}: {}", 
                    event.eventType(), topic, ex.getMessage());
            } else {
                log.debug("Published event {} to topic {} partition {} offset {}", 
                    event.eventType(), 
                    topic,
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset());
            }
        });
    }
    
    @Override
    public void publishAll(List<? extends IDomainEvent> events) {
        events.forEach(this::publish);
    }
    
    /**
     * Builds the Kafka topic name from the event type.
     * Example: TransportEvents.Request.Created -> tms.events.request.created
     */
    private String buildTopic(IDomainEvent event) {
        String eventType = event.eventType()
            .replace("TransportEvents.", "")
            .replace(".", "-")
            .toLowerCase();
        return topicPrefix + "." + eventType;
    }
    
    /**
     * Builds the message key for partitioning.
     * Uses transport request ID for ordering guarantee per aggregate.
     */
    private String buildKey(IDomainEvent event) {
        if (event instanceof TransportEvent transportEvent) {
            return transportEvent.getTransportRequestId().toString();
        }
        return event.getEventId().toString();
    }
}
