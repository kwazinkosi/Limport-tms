package com.limport.tms.infrastructure.persistance.repository;

import com.limport.tms.infrastructure.persistance.entity.DeadLetterEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/**
 * Repository for dead letter queue operations.
 */
@Repository
public interface DeadLetterEventJpaRepository extends JpaRepository<DeadLetterEventEntity, Long> {

    /**
     * Find events ready for retry based on next_retry_at timestamp.
     */
    @Query("SELECT d FROM DeadLetterEventEntity d WHERE d.nextRetryAt IS NOT NULL AND d.nextRetryAt <= :now AND d.processedAt IS NULL")
    List<DeadLetterEventEntity> findEventsReadyForRetry(@Param("now") Instant now);

    /**
     * Find expired events that have exceeded max retry attempts.
     */
    @Query("SELECT d FROM DeadLetterEventEntity d WHERE d.failureCount >= :maxRetries AND d.processedAt IS NULL")
    List<DeadLetterEventEntity> findExpiredEvents(@Param("maxRetries") int maxRetries);

    /**
     * Count events by source and status.
     */
    @Query("SELECT COUNT(d) FROM DeadLetterEventEntity d WHERE d.source = :source AND d.processedAt IS NULL")
    long countBySourceAndUnprocessed(@Param("source") String source);

    /**
     * Find events by event type for monitoring.
     */
    List<DeadLetterEventEntity> findByEventType(String eventType);
}