package com.limport.tms.infrastructure.repository.jpa;

import com.limport.tms.infrastructure.persistance.entity.ProcessedEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA repository for processed events.
 * Used for idempotency tracking when database mode is enabled.
 */
@Repository
public interface ProcessedEventJpaRepository extends JpaRepository<ProcessedEventEntity, UUID> {
    
    /**
     * Check if an event has been processed.
     */
    boolean existsByEventId(UUID eventId);
    
    /**
     * Delete processed events older than the given timestamp.
     * Used for cleanup to prevent table from growing indefinitely.
     * 
     * @param cutoffTime Events processed before this time will be deleted
     * @return Number of deleted records
     */
    @Modifying
    @Query("DELETE FROM ProcessedEventEntity e WHERE e.processedAt < :cutoffTime")
    int deleteByProcessedAtBefore(@Param("cutoffTime") Instant cutoffTime);
    
    /**
     * Count events by type for monitoring.
     */
    long countByEventType(String eventType);
}
