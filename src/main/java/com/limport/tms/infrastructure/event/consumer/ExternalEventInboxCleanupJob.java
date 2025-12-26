package com.limport.tms.infrastructure.event.consumer;

import com.limport.tms.infrastructure.repository.jpa.ExternalEventInboxJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Scheduled job to clean up processed external event inbox entries.
 * Prevents the inbox table from growing indefinitely.
 */
@Component
public class ExternalEventInboxCleanupJob {

    private static final Logger log = LoggerFactory.getLogger(ExternalEventInboxCleanupJob.class);

    private final ExternalEventInboxJpaRepository inboxRepository;

    @Value("${tms.inbox.retention-days:7}")
    private int retentionDays;

    @Value("${tms.inbox.cleanup-enabled:true}")
    private boolean cleanupEnabled;

    public ExternalEventInboxCleanupJob(ExternalEventInboxJpaRepository inboxRepository) {
        this.inboxRepository = inboxRepository;
    }

    /**
     * Cleans up old processed inbox events.
     * Runs once per hour at 30 minutes past by default.
     */
    @Scheduled(cron = "${tms.inbox.cleanup-cron:0 30 * * * *}")
    public void cleanupProcessedEvents() {
        if (!cleanupEnabled) {
            return;
        }

        try {
            Instant cutoff = Instant.now().minus(retentionDays, ChronoUnit.DAYS);
            int deleted = inboxRepository.deleteProcessedEventsOlderThan(cutoff);

            if (deleted > 0) {
                log.info("Cleaned up {} processed inbox events older than {} days",
                    deleted, retentionDays);
            }
        } catch (Exception e) {
            log.error("Error during inbox cleanup: {}", e.getMessage(), e);
        }
    }
}