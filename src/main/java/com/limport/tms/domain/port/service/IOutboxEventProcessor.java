package com.limport.tms.domain.port.service;

/**
 * Port interface for outbox event processing.
 * Defines the contract for processing and publishing events from the outbox.
 */
public interface IOutboxEventProcessor {

    /**
     * Processes pending outbox events and publishes them to the message broker.
     * Called by a scheduled job or triggered manually.
     *
     * @param batchSize maximum number of events to process
     * @return number of events successfully processed
     */
    int processPendingEvents(int batchSize);
}
