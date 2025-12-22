package com.limport.tms.application.service.interfaces;

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
     * Processes pending outbox events and publishes them to the message broker.
     * Called by a scheduled job or triggered manually.
     * 
     * @param batchSize maximum number of events to process
     * @return number of events successfully processed
     */
    int processPendingEvents(int batchSize);
}
