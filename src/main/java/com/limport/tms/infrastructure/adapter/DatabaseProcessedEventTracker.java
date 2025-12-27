package com.limport.tms.infrastructure.adapter;

import com.limport.tms.domain.port.service.IProcessedEventTracker;
import com.limport.tms.infrastructure.persistence.entity.ProcessedEventEntity;
import com.limport.tms.infrastructure.repository.jpa.ProcessedEventJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Database-based implementation of processed event tracker.
 * 
 * Uses PostgreSQL for persistent, transactional idempotency checking.
 * 
 * Benefits:
 * - ACID guarantees with database transactions
 * - Audit trail: keeps record of all processed events
 * - Works without Redis infrastructure
 * - Queryable for debugging and monitoring
 * 
 * Trade-offs:
 * - Slightly slower than Redis for high-throughput scenarios
 * - Requires cleanup job to prevent table growth
 * 
 * Cleanup:
 * - Scheduled job removes events older than retention period
 * - Default retention: 7 days
 */
@Component
@ConditionalOnProperty(name = "tms.event-tracker.type", havingValue = "database")
public class DatabaseProcessedEventTracker implements IProcessedEventTracker {
    
    private static final Logger log = LoggerFactory.getLogger(DatabaseProcessedEventTracker.class);
    
    private static final int DEFAULT_RETENTION_DAYS = 7;
    
    private final ProcessedEventJpaRepository repository;
    private final int retentionDays;
    
    public DatabaseProcessedEventTracker(ProcessedEventJpaRepository repository) {
        this.repository = repository;
        this.retentionDays = DEFAULT_RETENTION_DAYS;
        log.info("Initialized Database processed event tracker with retention={}days", retentionDays);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean isProcessed(UUID eventId) {
        return repository.existsByEventId(eventId);
    }
    
    @Override
    @Transactional
    public void markAsProcessed(UUID eventId, String eventType) {
        // Use INSERT with ON CONFLICT DO NOTHING for idempotent insert
        try {
            ProcessedEventEntity entity = new ProcessedEventEntity(eventId, eventType);
            repository.saveAndFlush(entity);
            log.debug("Marked event as processed in database: eventId={}, eventType={}", eventId, eventType);
        } catch (DataIntegrityViolationException e) {
            // Already exists - this is fine, idempotent operation
            log.debug("Event already marked as processed: eventId={}, eventType={}", eventId, eventType);
        }
    }

    /**
     * Scheduled cleanup of old processed events.
     * Runs daily at 2 AM to clean up events older than retention period.
     */
    @Scheduled(cron = "${tms.event-tracker.cleanup-cron:0 0 2 * * *}")
    @Transactional
    public void cleanupOldEvents() {
        Instant cutoff = Instant.now().minus(retentionDays, ChronoUnit.DAYS);
        int deleted = repository.deleteByProcessedAtBefore(cutoff);
        
        if (deleted > 0) {
            log.info("Cleaned up {} processed events older than {} days", deleted, retentionDays);
        }
    }
}
