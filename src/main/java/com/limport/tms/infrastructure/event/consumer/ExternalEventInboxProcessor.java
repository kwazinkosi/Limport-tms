package com.limport.tms.infrastructure.event.consumer;

import com.limport.tms.application.event.ExternalEvent;
import com.limport.tms.application.event.ExternalEventHandlerRegistry;
import com.limport.tms.domain.port.service.IDeadLetterService;
import com.limport.tms.domain.port.service.IProcessedEventTracker;
import com.limport.tms.application.service.interfaces.IUnifiedEventSerializer;
import com.limport.tms.domain.event.CorrelationIdContext;
import com.limport.tms.infrastructure.event.EventProcessingMetrics;
import com.limport.tms.infrastructure.event.EventProcessingProperties;
import com.limport.tms.infrastructure.event.UnifiedEventProcessor;
import com.limport.tms.infrastructure.persistence.entity.ExternalEventInboxEntity;
import com.limport.tms.infrastructure.repository.jpa.ExternalEventInboxJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

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
public class ExternalEventInboxProcessor extends UnifiedEventProcessor<ExternalEventInboxEntity> {

    private static final Logger log = LoggerFactory.getLogger(ExternalEventInboxProcessor.class);

    private final ExternalEventInboxJpaRepository inboxRepository;
    private final IUnifiedEventSerializer eventSerializer;
    private final ExternalEventHandlerRegistry handlerRegistry;
    private final IProcessedEventTracker processedEventTracker;
    private final EventProcessingProperties eventProcessingProperties;

    @Value("${tms.inbox.enabled:true}")
    private boolean enabled;

    public ExternalEventInboxProcessor(
            ExternalEventInboxJpaRepository inboxRepository,
            IUnifiedEventSerializer eventSerializer,
            ExternalEventHandlerRegistry handlerRegistry,
            IProcessedEventTracker processedEventTracker,
            IDeadLetterService deadLetterService,
            EventProcessingMetrics metrics,
            EventProcessingProperties eventProcessingProperties,
            @Value("${tms.eventprocessor.max-consecutive-failures:3}") int maxConsecutiveFailures) {
        super(log, metrics, deadLetterService, maxConsecutiveFailures);
        this.inboxRepository = inboxRepository;
        this.eventSerializer = eventSerializer;
        this.handlerRegistry = handlerRegistry;
        this.processedEventTracker = processedEventTracker;
        this.eventProcessingProperties = eventProcessingProperties;
    }

    /**
     * Processes pending external events from the inbox.
     * Runs every 2 seconds by default.
     */
    @Scheduled(fixedDelayString = "#{@eventProcessingProperties.getInboxPollIntervalMs()}")
    public void processInbox() {
        if (!enabled) {
            return;
        }

        //int batchSize = eventProcessingProperties.getInboxBatchSize();
        //int processed = processPendingEvents(batchSize);
        
        // Update queue size metrics
        long pendingCount = inboxRepository.countPendingEvents();
        metrics.updateInboxQueueSize(pendingCount);
    }

    @Override
    protected List<ExternalEventInboxEntity> findPendingEvents(int batchSize) {
        return inboxRepository.findPendingEvents(batchSize);
    }

    @Override
    protected boolean processEvent(ExternalEventInboxEntity inboxEvent) {
        // Deserialize the event
        var eventOptional = eventSerializer.deserializeExternalEvent(inboxEvent.getPayload());
        if (eventOptional.isEmpty()) {
            log.warn("Failed to deserialize inbox event {}", inboxEvent.getEventId());
            deadLetterService.storeFailedEvent(
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

        // Set correlation context for event processing
        // For external events, generate new correlation ID and set causation ID to the event ID
        CorrelationIdContext.setIds(null, event.eventId().toString());

        try {
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
                deadLetterService.storeFailedEvent(
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
        } finally {
            // Clear correlation context after processing
            CorrelationIdContext.clear();
        }
    }

    @Override
    protected String getEventId(ExternalEventInboxEntity event) {
        return event.getEventId().toString();
    }

    @Override
    protected void handleProcessingFailure(ExternalEventInboxEntity event, Exception e) {
        event.markAsFailed(e.getMessage());
        inboxRepository.save(event);
    }

    @Override
    protected void recordSuccess() {
        metrics.recordExternalEventProcessed();
    }

    @Override
    protected void recordFailure() {
        metrics.recordExternalEventFailed();
    }
}