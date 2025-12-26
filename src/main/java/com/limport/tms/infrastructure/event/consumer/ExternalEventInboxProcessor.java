package com.limport.tms.infrastructure.event.consumer;

import com.limport.tms.application.event.ExternalEvent;
import com.limport.tms.application.event.ExternalEventHandlerRegistry;
import com.limport.tms.application.ports.IProcessedEventTracker;
import com.limport.tms.application.service.interfaces.IUnifiedEventSerializer;
import com.limport.tms.infrastructure.event.DeadLetterQueueService;
import com.limport.tms.infrastructure.event.EventProcessingMetrics;
import com.limport.tms.infrastructure.persistance.entity.ExternalEventInboxEntity;
import com.limport.tms.infrastructure.repository.jpa.ExternalEventInboxJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Processes external events from the inbox using the outbox pattern.
 *
 * This provides consistency with domain event processing:
 * 1. External events are stored in inbox immediately upon receipt
 * 2. Processed asynchronously via scheduled job
 * 3. Same reliability guarantees as domain events
 */
@Component
public class ExternalEventInboxProcessor {

    private static final Logger log = LoggerFactory.getLogger(ExternalEventInboxProcessor.class);

    private final ExternalEventInboxJpaRepository inboxRepository;
    private final IUnifiedEventSerializer eventSerializer;
    private final ExternalEventHandlerRegistry handlerRegistry;
    private final IProcessedEventTracker processedEventTracker;
    private final DeadLetterQueueService deadLetterQueueService;
    private final EventProcessingMetrics metrics;

    @Value("${tms.inbox.batch-size:50}")
    private int batchSize;

    @Value("${tms.inbox.enabled:true}")
    private boolean enabled;

    public ExternalEventInboxProcessor(
            ExternalEventInboxJpaRepository inboxRepository,
            IUnifiedEventSerializer eventSerializer,
            ExternalEventHandlerRegistry handlerRegistry,
            IProcessedEventTracker processedEventTracker,
            DeadLetterQueueService deadLetterQueueService,
            EventProcessingMetrics metrics) {
        this.inboxRepository = inboxRepository;
        this.eventSerializer = eventSerializer;
        this.handlerRegistry = handlerRegistry;
        this.processedEventTracker = processedEventTracker;
        this.deadLetterQueueService = deadLetterQueueService;
        this.metrics = metrics;
    }

    /**
     * Processes pending external events from the inbox.
     * Runs every 2 seconds by default.
     */
    @Scheduled(fixedDelayString = "${tms.inbox.poll-interval-ms:2000}")
    public void processInbox() {
        if (!enabled) {
            return;
        }

        List<ExternalEventInboxEntity> pendingEvents = inboxRepository.findPendingEvents(batchSize);
        int successCount = 0;

        for (ExternalEventInboxEntity inboxEvent : pendingEvents) {
            try {
                boolean processed = processEvent(inboxEvent);
                if (processed) {
                    successCount++;
                    metrics.recordExternalEventProcessed();
                } else {
                    metrics.recordExternalEventFailed();
                }
            } catch (Exception e) {
                log.error("Failed to process inbox event {}: {}", inboxEvent.getEventId(), e.getMessage());
                metrics.recordExternalEventFailed();
                inboxEvent.markAsFailed(e.getMessage());
                inboxRepository.save(inboxEvent);
            }
        }

        if (successCount > 0) {
            log.info("Processed {}/{} inbox events", successCount, pendingEvents.size());
        }
    }

    @Transactional
    public boolean processEvent(ExternalEventInboxEntity inboxEvent) {
        // Deserialize the event
        var eventOptional = eventSerializer.deserializeExternalEvent(inboxEvent.getPayload());
        if (eventOptional.isEmpty()) {
            log.warn("Failed to deserialize inbox event {}", inboxEvent.getEventId());
            deadLetterQueueService.storeFailedEvent(
                inboxEvent.getEventId().toString(),
                inboxEvent.getEventType(),
                inboxEvent.getPayload(),
                "INBOX",
                "Deserialization failed"
            );
            inboxEvent.markAsFailed("Deserialization failed");
            inboxRepository.save(inboxEvent);
            return false;
        }

        ExternalEvent event = eventOptional.get();

        // Check for duplicate processing
        if (processedEventTracker.isProcessed(event.eventId())) {
            log.debug("Event {} already processed, marking inbox as processed", event.eventId());
            inboxEvent.markAsProcessed();
            inboxRepository.save(inboxEvent);
            return true;
        }

        // Dispatch to handler
        boolean handled = handlerRegistry.dispatch(event);
        if (handled) {
            processedEventTracker.markAsProcessed(event.eventId(), event.eventType());
            inboxEvent.markAsProcessed();
            inboxRepository.save(inboxEvent);
            log.debug("Successfully processed inbox event: {} (id={})",
                event.eventType(), event.eventId());
            return true;
        } else {
            log.warn("No handler found for event type: {}", event.eventType());
            deadLetterQueueService.storeFailedEvent(
                event.eventId().toString(),
                event.eventType(),
                inboxEvent.getPayload(),
                "INBOX",
                "No handler found for event type: " + event.eventType()
            );
            inboxEvent.markAsFailed("No handler found");
            inboxRepository.save(inboxEvent);
            return false;
        }
    }
}