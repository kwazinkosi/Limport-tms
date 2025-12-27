package com.limport.tms.infrastructure.event;

import com.limport.tms.domain.port.repository.IOutboxEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Scheduled job to clean up processed outbox events.
 * Prevents the outbox table from growing indefinitely.
 */
@Component
public class OutboxCleanupJob {
    
    private static final Logger log = LoggerFactory.getLogger(OutboxCleanupJob.class);
    
    private final IOutboxEventRepository outboxRepository;
    
    @Value("${tms.outbox.retention-days:7}")
    private int retentionDays;
    
    @Value("${tms.outbox.cleanup-enabled:true}")
    private boolean cleanupEnabled;
    
    public OutboxCleanupJob(IOutboxEventRepository outboxRepository) {
        this.outboxRepository = outboxRepository;
    }
    
    /**
     * Cleans up old processed events.
     * Runs once per hour by default.
     */
    @Scheduled(cron = "${tms.outbox.cleanup-cron:0 0 * * * *}")
    public void cleanupProcessedEvents() {
        if (!cleanupEnabled) {
            return;
        }
        
        try {
            Instant cutoff = Instant.now().minus(retentionDays, ChronoUnit.DAYS);
            int deleted = outboxRepository.deleteProcessedBefore(cutoff);
            
            if (deleted > 0) {
                log.info("Cleaned up {} processed outbox events older than {} days", 
                    deleted, retentionDays);
            }
        } catch (Exception e) {
            log.error("Error during outbox cleanup: {}", e.getMessage(), e);
        }
    }
}
