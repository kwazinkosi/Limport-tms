package com.limport.tms.application.service.impl;

import com.limport.tms.application.service.interfaces.IDomainEventService;
import com.limport.tms.application.service.interfaces.IUnifiedEventSerializer;
import com.limport.tms.domain.event.IDomainEvent;
import com.limport.tms.domain.model.aggregate.AggregateRoot;
import com.limport.tms.domain.model.entity.OutboxEvent;
import com.limport.tms.domain.ports.IEventPublisher;
import com.limport.tms.domain.ports.IOutboxEventRepository;
import com.limport.tms.application.ports.IDeadLetterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of domain event service using the transactional outbox pattern.
 * 
 * Flow:
 * 1. collectAndStore: Saves events to outbox table (same transaction as aggregate)
 * 2. processPendingEvents: Polls outbox, publishes to broker, marks as processed
 */
@Service
public class DomainEventServiceImpl implements IDomainEventService {
    
    private static final Logger log = LoggerFactory.getLogger(DomainEventServiceImpl.class);
    
    private final IOutboxEventRepository outboxRepository;
    private final IEventPublisher eventPublisher;
    private final IUnifiedEventSerializer eventSerializer;
    private final IDeadLetterService deadLetterService;
    
    public DomainEventServiceImpl(
            IOutboxEventRepository outboxRepository,
            IEventPublisher eventPublisher,
            IUnifiedEventSerializer eventSerializer,
            IDeadLetterService deadLetterService) {
        this.outboxRepository = outboxRepository;
        this.eventPublisher = eventPublisher;
        this.eventSerializer = eventSerializer;
        this.deadLetterService = deadLetterService;
    }
    
    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void collectAndStore(AggregateRoot aggregate, String aggregateType) {
        if (aggregate == null || !aggregate.hasPendingEvents()) {
            return;
        }
        
        List<OutboxEvent> outboxEvents = new ArrayList<>();
        String aggregateId = aggregate.getId().toString();
        
        for (IDomainEvent event : aggregate.getDomainEvents()) {
            String payload = eventSerializer.serialize(event);
            OutboxEvent outboxEvent = new OutboxEvent(
                event.eventType(),
                aggregateType,
                aggregateId,
                payload,
                event.occurredOn()
            );
            outboxEvents.add(outboxEvent);
        }
        
        outboxRepository.saveAll(outboxEvents);
        aggregate.clearDomainEvents();
        
        log.debug("Stored {} events for aggregate {} ({})", 
            outboxEvents.size(), aggregateType, aggregateId);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void publishToOutbox(IDomainEvent event, String aggregateType, String aggregateId) {
        String payload = eventSerializer.serialize(event);
        OutboxEvent outboxEvent = new OutboxEvent(
            event.eventType(),
            aggregateType,
            aggregateId,
            payload,
            event.occurredOn()
        );
        outboxRepository.save(outboxEvent);
        
        log.debug("Stored event {} for aggregate {} ({})", 
            event.eventType(), aggregateType, aggregateId);
    }
    
    @Override
    @Transactional
    public int processPendingEvents(int batchSize) {
        List<OutboxEvent> pendingEvents = outboxRepository.findPendingEvents(batchSize);
        int successCount = 0;

        for (OutboxEvent outboxEvent : pendingEvents) {
            try {
                IDomainEvent domainEvent = eventSerializer.deserialize(
                    outboxEvent.getPayload(),
                    outboxEvent.getEventType()
                );

eventPublisher.publish(domainEvent);
                outboxEvent.markAsProcessed();
                outboxRepository.update(outboxEvent);
                successCount++;

                log.debug("Published event: {} for aggregate {}",
                    outboxEvent.getEventType(), outboxEvent.getAggregateId());

            } catch (Exception e) {
                log.error("Failed to process event {}: {}", outboxEvent.getId(), e.getMessage());
                deadLetterService.storeFailedEvent(
                    outboxEvent.getId().toString(),
                    outboxEvent.getEventType(),
                    outboxEvent.getPayload(),
                    "OUTBOX",
                    e.getMessage()
                );
                outboxEvent.markAsFailed(e.getMessage());
                outboxRepository.update(outboxEvent);

                // Break the loop on failure to prevent long delays if broker is down
                // The transaction will commit the processed/failed events so far
                break;
            }
        }

        if (successCount > 0) {
            log.info("Processed {}/{} outbox events", successCount, pendingEvents.size());
        }

        return successCount;
    }
}
