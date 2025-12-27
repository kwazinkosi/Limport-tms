package com.limport.tms.infrastructure.event;

import com.limport.tms.application.ports.IOutboxEventProcessor;
import com.limport.tms.domain.event.IDomainEvent;
import com.limport.tms.domain.model.entity.OutboxEvent;
import com.limport.tms.domain.ports.IEventPublisher;
import com.limport.tms.domain.ports.IOutboxEventRepository;
import com.limport.tms.application.service.interfaces.IUnifiedEventSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Infrastructure implementation of outbox event processing.
 * Handles the actual processing and publishing of events from the outbox.
 */
@Service
public class OutboxEventProcessorImpl extends UnifiedEventProcessor<OutboxEvent> implements IOutboxEventProcessor {

    private static final Logger log = LoggerFactory.getLogger(OutboxEventProcessorImpl.class);

    private final IOutboxEventRepository outboxRepository;
    private final IEventPublisher eventPublisher;
    private final IUnifiedEventSerializer eventSerializer;

    public OutboxEventProcessorImpl(
            IOutboxEventRepository outboxRepository,
            IEventPublisher eventPublisher,
            IUnifiedEventSerializer eventSerializer,
            DeadLetterQueueService deadLetterService,
            EventProcessingMetrics metrics,
            @Value("${tms.eventprocessor.max-consecutive-failures:3}") int maxConsecutiveFailures) {
        super(log, metrics, deadLetterService, maxConsecutiveFailures);
        this.outboxRepository = outboxRepository;
        this.eventPublisher = eventPublisher;
        this.eventSerializer = eventSerializer;
    }

    @Override
    protected List<OutboxEvent> findPendingEvents(int batchSize) {
        return outboxRepository.findPendingEvents(batchSize);
    }

    @Override
    protected boolean processEvent(OutboxEvent outboxEvent) {
        try {
            // Deserialize the event
            IDomainEvent domainEvent = eventSerializer.deserialize(
                outboxEvent.getPayload(),
                outboxEvent.getEventType()
            );

            // Publish to message broker
            eventPublisher.publish(domainEvent);

            // Mark as processed
            outboxEvent.markAsProcessed();
            outboxRepository.update(outboxEvent);

            log.debug("Published event: {} for aggregate {}",
                outboxEvent.getEventType(), outboxEvent.getAggregateId());

            return true;

        } catch (Exception e) {
            log.error("Failed to process outbox event {}: {}", outboxEvent.getId(), e.getMessage());
            throw e; // Let UnifiedEventProcessor handle the failure
        }
    }

    @Override
    protected String getEventId(OutboxEvent event) {
        return event.getId().toString();
    }

    @Override
    protected void handleProcessingFailure(OutboxEvent event, Exception e) {
        deadLetterService.storeFailedEvent(
            event.getId().toString(),
            event.getEventType(),
            event.getPayload(),
            "OUTBOX",
            e.getMessage()
        );
        event.markAsFailed(e.getMessage());
        outboxRepository.update(event);
    }

    @Override
    protected void recordSuccess() {
        metrics.recordDomainEventPublished();
    }

    @Override
    protected void recordFailure() {
        metrics.recordDomainEventFailed();
    }
}