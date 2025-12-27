package com.limport.tms.application.service.interfaces;

import com.limport.tms.domain.event.IDomainEvent;
import com.limport.tms.domain.model.aggregate.AggregateRoot;

/**
 * Application service for handling domain events from aggregates.
 * Collects events and stores them in outbox for reliable publishing.
 */
public interface IDomainEventService {
    
    /**
     * Collects domain events from an aggregate and stores them in the outbox.
     * Should be called in the same transaction as aggregate persistence.
     * 
     * @param aggregate the aggregate containing pending events
     * @param aggregateType the type name of the aggregate (e.g., "TransportRequest")
     */
    void collectAndStore(AggregateRoot aggregate, String aggregateType);
    
    /**
     * Publishes a single domain event directly to the outbox.
     * Useful for events not tied to an aggregate root.
     * 
     * @param event the domain event to publish
     * @param aggregateType the type of aggregate (e.g., "TransportRequest")
     * @param aggregateId the ID of the aggregate
     */
    void publishToOutbox(IDomainEvent event, String aggregateType, String aggregateId);
}
