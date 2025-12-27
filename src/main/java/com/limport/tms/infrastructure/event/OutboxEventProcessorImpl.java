package com.limport.tms.infrastructure.event;

import com.limport.tms.domain.port.service.IDeadLetterService;
import com.limport.tms.domain.port.service.IOutboxEventProcessor;
import com.limport.tms.domain.event.IDomainEvent;
import com.limport.tms.domain.model.entity.OutboxEvent;
import com.limport.tms.domain.port.messaging.IEventPublisher;
import com.limport.tms.domain.port.repository.IOutboxEventRepository;
import com.limport.tms.infrastructure.event.publisher.KafkaEventPublisher;
import com.limport.tms.application.service.interfaces.IUnifiedEventSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Infrastructure implementation of outbox event processing.
 * Handles the actual processing and publishing of events from the outbox.
 * 
 * Uses asynchronous Kafka publishing to avoid blocking the processing thread.
 */
@Service
public class OutboxEventProcessorImpl implements IOutboxEventProcessor {

    private static final Logger log = LoggerFactory.getLogger(OutboxEventProcessorImpl.class);

    private final IOutboxEventRepository outboxRepository;
    private final KafkaEventPublisher eventPublisher;
    private final IUnifiedEventSerializer eventSerializer;
    private final IDeadLetterService deadLetterService;
    private final EventProcessingMetrics metrics;

    @Value("${tms.eventprocessor.max-consecutive-failures:3}")
    private int maxConsecutiveFailures;

    @Value("${tms.outbox.publish.timeout-seconds:30}")
    private int publishTimeoutSeconds;

    public OutboxEventProcessorImpl(
            IOutboxEventRepository outboxRepository,
            IEventPublisher eventPublisher,
            IUnifiedEventSerializer eventSerializer,
            IDeadLetterService deadLetterService,
            EventProcessingMetrics metrics) {
        this.outboxRepository = outboxRepository;
        this.eventPublisher = (KafkaEventPublisher) eventPublisher; // Cast to access publishAsync
        this.eventSerializer = eventSerializer;
        this.deadLetterService = deadLetterService;
        this.metrics = metrics;
    }

    @Override
    public int processPendingEvents(int batchSize) {
        List<OutboxEvent> pendingEvents = outboxRepository.findPendingEvents(batchSize);
        if (pendingEvents.isEmpty()) {
            return 0;
        }

        log.debug("Processing {} outbox events", pendingEvents.size());

        // Process events asynchronously
        List<EventPublishResult> results = pendingEvents.stream()
            .map(this::processEventAsync)
            .collect(Collectors.toList());

        // Wait for all async operations to complete
        CompletableFuture.allOf(results.stream()
            .map(result -> result.future)
            .toArray(CompletableFuture[]::new))
            .join();

        // Process results
        int successCount = 0;
        int consecutiveFailures = 0;

        for (EventPublishResult result : results) {
            try {
                // Wait for individual result with timeout
                result.future.get(publishTimeoutSeconds, TimeUnit.SECONDS);
                
                // Success - mark as processed
                result.event.markAsProcessed();
                outboxRepository.update(result.event);
                successCount++;
                consecutiveFailures = 0;
                metrics.recordDomainEventPublished();
                
                log.debug("Successfully published event: {} for aggregate {}",
                    result.event.getEventType(), result.event.getAggregateId());
                
            } catch (Exception e) {
                // Failure - handle dead letter
                consecutiveFailures++;
                metrics.recordDomainEventFailed();
                
                deadLetterService.storeFailedEvent(
                    result.event.getId().toString(),
                    result.event.getEventType(),
                    result.event.getPayload(),
                    "OUTBOX",
                    e.getMessage()
                );
                result.event.markAsFailed(e.getMessage());
                outboxRepository.update(result.event);
                
                log.error("Failed to publish outbox event {}: {}", result.event.getId(), e.getMessage());
                
                // Break on too many consecutive failures
                if (consecutiveFailures >= maxConsecutiveFailures) {
                    log.warn("Breaking batch processing after {} consecutive failures", consecutiveFailures);
                    break;
                }
            }
        }

        if (successCount > 0) {
            log.info("Processed {}/{} outbox events", successCount, pendingEvents.size());
        }

        return successCount;
    }

    /**
     * Processes a single event asynchronously.
     */
    private EventPublishResult processEventAsync(OutboxEvent outboxEvent) {
        try {
            // Deserialize the event
            IDomainEvent domainEvent = eventSerializer.deserialize(
                outboxEvent.getPayload(),
                outboxEvent.getEventType()
            );

            // Start async publish
            CompletableFuture<Void> future = eventPublisher.publishAsync(domainEvent);
            
            return new EventPublishResult(outboxEvent, future);
            
        } catch (Exception e) {
            // Deserialization failed - create failed future
            CompletableFuture<Void> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(e);
            return new EventPublishResult(outboxEvent, failedFuture);
        }
    }

    /**
     * Helper class to hold event and its publish future.
     */
    private static class EventPublishResult {
        final OutboxEvent event;
        final CompletableFuture<Void> future;

        EventPublishResult(OutboxEvent event, CompletableFuture<Void> future) {
            this.event = event;
            this.future = future;
        }
    }
}