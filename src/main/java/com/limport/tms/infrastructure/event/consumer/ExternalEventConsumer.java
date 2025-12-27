package com.limport.tms.infrastructure.event.consumer;

import com.limport.tms.application.event.ExternalEvent;
import com.limport.tms.application.service.interfaces.IUnifiedEventSerializer;
import com.limport.tms.infrastructure.event.EventProcessingMetrics;
import com.limport.tms.infrastructure.persistance.entity.ExternalEventInboxEntity;
import com.limport.tms.infrastructure.repository.jpa.ExternalEventInboxJpaRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Kafka consumer for external events from other services (PMS, etc.).
 *
 * Consumes events from external services, deserializes them, and dispatches
 * to appropriate handlers. Ensures idempotency by tracking processed events.
 */
@Component
@ConditionalOnProperty(value = "tms.kafka.enabled", havingValue = "true")
public class ExternalEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(ExternalEventConsumer.class);

    private final IUnifiedEventSerializer eventSerializer;
    private final ExternalEventInboxJpaRepository inboxRepository;
    private final EventProcessingMetrics metrics;

    @Value("${tms.kafka.topics.pms-events:pms.events}")
    private String pmsEventsTopic;
    
    @Value("${tms.inbox.backpressure-threshold:2000}")
    private long backpressureThreshold;

    public ExternalEventConsumer(
            IUnifiedEventSerializer eventSerializer,
            ExternalEventInboxJpaRepository inboxRepository,
            EventProcessingMetrics metrics) {
        this.eventSerializer = eventSerializer;
        this.inboxRepository = inboxRepository;
        this.metrics = metrics;
    }

    /**
     * Consumes external events from PMS and other services.
     * Uses manual acknowledgment for reliable processing.
     */
    @KafkaListener(
        topics = "${tms.kafka.topics.pms-events:pms.events}",
        groupId = "${tms.kafka.consumer.group-id:tms-consumer-group}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeExternalEvent(
            @Payload String payload,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        log.debug("Received external event from topic={} offset={} key={}", topic, offset, key);

        try {
            // Store the event in inbox for reliable processing
            storeEventInInbox(payload, topic, offset);
            acknowledgment.acknowledge();

        } catch (Exception e) {
            log.error("Failed to store external event from topic={} offset={}: {}",
                topic, offset, e.getMessage(), e);
            // Don't acknowledge - let Kafka redeliver
        }
    }

    /**
     * Stores an external event in the inbox for later processing.
     */
    @Transactional
    public void storeEventInInbox(String payload, String topic, long offset) {
        // Check for backpressure - if inbox is too full, log warning but continue
        // This prevents message loss while still allowing storage
        long pendingCount = inboxRepository.countPendingEvents();
        if (pendingCount > backpressureThreshold) {
            log.warn("Inbox queue size {} exceeds backpressure threshold {}. " +
                "Event processing may be delayed. Topic: {}, offset: {}",
                pendingCount, backpressureThreshold, topic, offset);
        }

        // Quick validation - try to extract event type
        var eventOptional = eventSerializer.deserializeExternalEvent(payload);
        if (eventOptional.isEmpty()) {
            log.warn("Cannot store invalid event from topic={} offset={}", topic, offset);
            throw new IllegalArgumentException("Invalid event payload");
        }

        ExternalEvent event = eventOptional.get();

        // Store in inbox
        ExternalEventInboxEntity inboxEntity = new ExternalEventInboxEntity(
            event.eventType(),
            payload,
            event.sourceService()
        );

        inboxRepository.save(inboxEntity);

        metrics.recordExternalEventReceived();

        log.debug("Stored external event {} in inbox (id={})", event.eventType(), inboxEntity.getEventId());
    }

}