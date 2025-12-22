package com.limport.tms.domain.ports;

import com.limport.tms.domain.model.entity.OutboxEvent;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository port for outbox event persistence.
 * Infrastructure layer provides the concrete implementation.
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
     * Deletes processed events older than the specified timestamp.
     * Used for cleanup/archival.
     * @param before delete events processed before this time
     * @return number of deleted events
     */
    int deleteProcessedBefore(Instant before);
}
