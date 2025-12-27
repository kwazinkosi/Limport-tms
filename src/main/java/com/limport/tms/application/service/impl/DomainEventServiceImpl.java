package com.limport.tms.application.service.impl;

import com.limport.tms.application.event.InternalEventHandlerRegistry;
import com.limport.tms.domain.event.IDomainEvent;
import com.limport.tms.domain.model.aggregate.AggregateRoot;
import com.limport.tms.domain.model.entity.OutboxEvent;
import com.limport.tms.domain.port.repository.IOutboxEventRepository;
import com.limport.tms.application.service.interfaces.IDomainEventService;
import com.limport.tms.application.service.interfaces.IUnifiedEventSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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
 */
@Service
public class DomainEventServiceImpl implements IDomainEventService {
    
    private static final Logger log = LoggerFactory.getLogger(DomainEventServiceImpl.class);
    
    private final IOutboxEventRepository outboxRepository;
    private final IUnifiedEventSerializer eventSerializer;
    private final InternalEventHandlerRegistry internalEventHandlerRegistry;
    
    @Value("${tms.outbox.backpressure-threshold:5000}")
    private long backpressureThreshold;
    
    public DomainEventServiceImpl(
            IOutboxEventRepository outboxRepository,
            IUnifiedEventSerializer eventSerializer,
            InternalEventHandlerRegistry internalEventHandlerRegistry) {
        this.outboxRepository = outboxRepository;
        this.eventSerializer = eventSerializer;
        this.internalEventHandlerRegistry = internalEventHandlerRegistry;
    }
    
    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void collectAndStore(AggregateRoot aggregate, String aggregateType) {
        if (aggregate == null || !aggregate.hasPendingEvents()) {
            return;
        }

        // Check for backpressure - if outbox is too full, log warning but continue
        // This prevents unbounded growth while still allowing the transaction to complete
        long pendingCount = outboxRepository.countPendingEvents();
        if (pendingCount > backpressureThreshold) {
            log.warn("Outbox queue size {} exceeds backpressure threshold {}. " +
                "Event processing may be delayed. Aggregate: {} ({})",
                pendingCount, backpressureThreshold, aggregateType, aggregate.getId());
        }

        // Get events from aggregate
        List<IDomainEvent> events = new ArrayList<>(aggregate.getDomainEvents());

        // Dispatch events to synchronous internal handlers
        for (IDomainEvent event : events) {
            internalEventHandlerRegistry.dispatch(event);
        }

        // Store events in outbox for asynchronous external publishing
        List<OutboxEvent> outboxEvents = new ArrayList<>();
        String aggregateId = aggregate.getId().toString();

        for (IDomainEvent event : events) {
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

        log.debug("Processed {} events for aggregate {} ({}): {} handled internally, {} stored for external publishing",
            events.size(), aggregateType, aggregateId, events.size(), outboxEvents.size());
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
}
