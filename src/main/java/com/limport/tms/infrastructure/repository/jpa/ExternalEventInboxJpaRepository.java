package com.limport.tms.infrastructure.repository.jpa;

import com.limport.tms.infrastructure.persistance.entity.ExternalEventInboxEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Repository for external event inbox operations.
 */
@Repository
public interface ExternalEventInboxJpaRepository extends JpaRepository<ExternalEventInboxEntity, UUID> {

    /**
     * Find pending events ordered by received time (FIFO).
     */
    @Query("SELECT e FROM ExternalEventInboxEntity e WHERE e.status = 'PENDING' ORDER BY e.receivedAt ASC")
    List<ExternalEventInboxEntity> findPendingEvents(@Param("limit") int limit);

    /**
     * Find failed events for retry or monitoring.
     */
    @Query("SELECT e FROM ExternalEventInboxEntity e WHERE e.status = 'FAILED'")
    List<ExternalEventInboxEntity> findFailedEvents();

    /**
     * Clean up old processed events.
     */
    @Query("DELETE FROM ExternalEventInboxEntity e WHERE e.status = 'PROCESSED' AND e.processedAt < :cutoffTime")
    int deleteProcessedEventsOlderThan(@Param("cutoffTime") Instant cutoffTime);
}