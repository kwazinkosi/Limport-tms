package com.limport.tms.infrastructure.event.publisher;

import com.limport.tms.application.service.interfaces.IUnifiedEventSerializer;
import com.limport.tms.domain.event.CorrelationIdContext;
import com.limport.tms.domain.event.IDomainEvent;
import com.limport.tms.infrastructure.event.EventProcessingMetrics;

import com.limport.tms.domain.event.TransportEvent;
import com.limport.tms.domain.port.messaging.IEventPublisher;
import io.micrometer.core.instrument.Timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Kafka-based implementation of the event publisher.
 * Publishes domain events to Kafka topics for consumption by other services.
 * 
 * Uses synchronous send to integrate with outbox pattern - we need to know
 * if the send succeeded before marking the outbox event as processed.
 */
@Component
public class KafkaEventPublisher implements IEventPublisher {
    
    private static final Logger log = LoggerFactory.getLogger(KafkaEventPublisher.class);
    
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final IUnifiedEventSerializer eventSerializer;
    private final EventProcessingMetrics metrics;
    
    @Value("${tms.kafka.publish.timeout-seconds:5}")
    private int publishTimeoutSeconds;
    
    @Value("${tms.kafka.topic.prefix:tms.events}")
    private String topicPrefix;
    
    // Configurable topic transformation rules
    // Maps event type prefixes to their topic transformation rules
    private final Map<String, TopicTransformationRule> topicRules = Map.of(
        "TMS.Transport", new TopicTransformationRule("TMS.Transport.", ""),
        "PMS.Provider", new TopicTransformationRule("PMS.Provider.", "provider")
    );
    
    /**
     * Represents a rule for transforming event types to topic names.
     */
    private static class TopicTransformationRule {
        private final String prefixToRemove;
        private final String topicSuffix;
        
        public TopicTransformationRule(String prefixToRemove, String topicSuffix) {
            this.prefixToRemove = prefixToRemove;
            this.topicSuffix = topicSuffix;
        }
        
        public String transform(String eventType) {
            if (eventType.startsWith(prefixToRemove)) {
                String suffix = eventType.substring(prefixToRemove.length())
                    .toLowerCase();
                return topicSuffix.isEmpty() ? suffix : topicSuffix + "." + suffix;
            }
            return eventType.toLowerCase();
        }
    }
    
    public KafkaEventPublisher(
            KafkaTemplate<String, String> kafkaTemplate,
            IUnifiedEventSerializer eventSerializer,
            EventProcessingMetrics metrics) {
        this.kafkaTemplate = kafkaTemplate;
        this.eventSerializer = eventSerializer;
        this.metrics = metrics;
    }
    
    @Override
    public void publish(IDomainEvent event) {
        Timer.Sample sample = metrics.startDomainEventPublishTimer();

        try {
            String topic = buildTopic(event);
            String key = buildKey(event);
            String payload = eventSerializer.serialize(event);

            // Synchronous send with configurable timeout
            CompletableFuture<SendResult<String, String>> future =
                kafkaTemplate.send(topic, key, payload);

            SendResult<String, String> result = future.get(publishTimeoutSeconds, TimeUnit.SECONDS);

            log.debug("Published event {} to topic {} partition {} offset {}",
                event.eventType(),
                topic,
                result.getRecordMetadata().partition(),
                result.getRecordMetadata().offset());

            metrics.recordDomainEventPublished();
            sample.stop(metrics.getDomainEventPublishTimer());

        } catch (ExecutionException e) {
            sample.stop(metrics.getDomainEventPublishTimer());
            metrics.recordDomainEventFailed();
            log.error("Failed to publish event {} to topic {}: {}",
                event.eventType(), buildTopic(event), e.getCause().getMessage());
            throw new RuntimeException("Kafka publish failed: " + e.getCause().getMessage(), e.getCause());
        } catch (TimeoutException e) {
            sample.stop(metrics.getDomainEventPublishTimer());
            metrics.recordDomainEventFailed();
            log.error("Timeout publishing event {} to topic {} after {} seconds",
                event.eventType(), buildTopic(event), publishTimeoutSeconds);
            throw new RuntimeException("Kafka publish timeout after " + publishTimeoutSeconds + " seconds", e);
        } catch (InterruptedException e) {
            sample.stop(metrics.getDomainEventPublishTimer());
            metrics.recordDomainEventFailed();
            Thread.currentThread().interrupt();
            throw new RuntimeException("Kafka publish interrupted", e);
        }
    }

    /**
     * Publishes an event asynchronously with callback for outbox processing.
     * Returns a CompletableFuture that completes when publishing is done.
     * 
     * ThreadLocal cleanup is performed in async handlers to prevent memory leaks.
     */
    public CompletableFuture<Void> publishAsync(IDomainEvent event) {
        String topic = buildTopic(event);
        String key = buildKey(event);
        String payload = eventSerializer.serialize(event);

        return kafkaTemplate.send(topic, key, payload)
            .thenAccept(result -> {
                // Clear ThreadLocal to prevent memory leaks in async processing
                CorrelationIdContext.clear();
                log.debug("Published event {} to topic {} partition {} offset {}",
                    event.eventType(),
                    topic,
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset());
            })
            .exceptionally(throwable -> {
                // Clear ThreadLocal to prevent memory leaks in async processing
                CorrelationIdContext.clear();
                log.error("Failed to publish event {} to topic {}: {}",
                    event.eventType(), topic, throwable.getMessage());
                throw new RuntimeException("Kafka publish failed: " + throwable.getMessage(), throwable);
            })
            .toCompletableFuture();
    }
    
    @Override
    public void publishAll(List<? extends IDomainEvent> events) {
        if (events.isEmpty()) {
            return;
        }

        // For small batches, publish individually to maintain ordering guarantees
        if (events.size() <= 10) {
            events.forEach(this::publish);
            return;
        }

        // For larger batches, use parallel publishing but maintain per-aggregate ordering
        publishBatchParallel(events);
    }

    /**
     * Publishes events in parallel while maintaining ordering per aggregate.
     * Groups events by aggregate ID and publishes each group sequentially.
     */
    private void publishBatchParallel(List<? extends IDomainEvent> events) {
        // Group events by aggregate for ordering
        Map<String, List<IDomainEvent>> eventsByAggregate = events.stream()
            .collect(Collectors.groupingBy(this::getAggregateKey));

        // Publish each aggregate's events sequentially, but aggregates in parallel
        List<CompletableFuture<Void>> futures = eventsByAggregate.entrySet().stream()
            .map(entry -> CompletableFuture.runAsync(() ->
                entry.getValue().forEach(this::publish)))
            .toList();

        // Wait for all to complete
        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .get(publishTimeoutSeconds * events.size(), TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Batch publish failed: {}", e.getMessage());
            throw new RuntimeException("Batch publish failed", e);
        }
    }

    /**
     * Gets a key for grouping events by aggregate to maintain ordering.
     */
    private String getAggregateKey(IDomainEvent event) {
        if (event instanceof TransportEvent transportEvent) {
            return transportEvent.getTransportRequestId().toString();
        }
        return event.getEventId().toString();
    }
    
    /**
     * Builds the Kafka topic name from the event type using configurable transformation rules.
     * 
     * The method applies transformation rules based on event type prefixes:
     * - TMS.Transport.* events -> tms.events.*
     * - PMS.Provider.* events -> tms.events.provider.*
     * - Unknown prefixes -> fallback to generic transformation
     * 
     * Example: TMS.Transport.Request.Created -> tms.events.request.created
     */
    private String buildTopic(IDomainEvent event) {
        String eventType = event.eventType();
        
        // Find the appropriate transformation rule
        for (Map.Entry<String, TopicTransformationRule> entry : topicRules.entrySet()) {
            if (eventType.startsWith(entry.getKey())) {
                String transformedSuffix = entry.getValue().transform(eventType);
                return topicPrefix + "." + transformedSuffix;
            }
        }
        
        // Fallback for unknown event types - use generic transformation
        log.warn("No topic transformation rule found for event type: {}. Using fallback transformation.", eventType);
        String fallbackTopic = eventType.replace(".", "-").toLowerCase();
        return topicPrefix + "." + fallbackTopic;
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
