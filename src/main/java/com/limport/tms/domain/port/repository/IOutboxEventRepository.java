package com.limport.tms.domain.port.repository;

import com.limport.tms.domain.model.entity.OutboxEvent;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository port for outbox event persistence.
 * Part of the domain layer - defines the contract for infrastructure implementations.
 */
public interface IOutboxEventRepository {

    /**
     * Saves a new outbox event.
     * @param event the event to save
     */
    void save(OutboxEvent event);

    /**
     * Saves multiple outbox events in a batch.
     * @param events the events to save
     */
    void saveAll(List<OutboxEvent> events);

    /**
     * Finds an outbox event by its ID.
     * @param id the event ID
     * @return the event if found
     */
    Optional<OutboxEvent> findById(UUID id);

    /**
     * Finds all pending events ready for processing.
     * @param limit maximum number of events to return
     * @return list of pending events
     */
    List<OutboxEvent> findPendingEvents(int limit);

    /**
     * Updates an existing outbox event.
     * @param event the event to update
     */
    void update(OutboxEvent event);

    /**
     * Counts pending events for monitoring.
     * @return number of pending events
     */
    long countPendingEvents();

    /**
     * Deletes processed events older than the given timestamp.
     * @param before timestamp before which to delete
     * @return number of deleted events
     */
    int deleteProcessedBefore(Instant before);
}
