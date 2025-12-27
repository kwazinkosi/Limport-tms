package com.limport.tms.infrastructure.event;

import com.limport.tms.application.ports.IDeadLetterService;
import com.limport.tms.domain.model.entity.OutboxEvent;
import com.limport.tms.domain.ports.IOutboxEventRepository;
import com.limport.tms.infrastructure.persistance.entity.DeadLetterEventEntity;
import com.limport.tms.infrastructure.persistance.entity.ExternalEventInboxEntity;
import com.limport.tms.infrastructure.persistance.repository.DeadLetterEventJpaRepository;
import com.limport.tms.infrastructure.repository.jpa.ExternalEventInboxJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for managing dead letter queue operations.
 * Handles storing failed events and retrying them with exponential backoff.
 */
@Service
public class DeadLetterQueueService implements IDeadLetterService {

    private static final Logger log = LoggerFactory.getLogger(DeadLetterQueueService.class);

    private final DeadLetterEventJpaRepository deadLetterRepository;
    private final IOutboxEventRepository outboxRepository;
    private final ExternalEventInboxJpaRepository inboxRepository;
    private final CircuitBreaker circuitBreaker;

    @Value("${tms.deadletter.max-retries:5}")
    private int maxRetries;

    @Value("${tms.deadletter.quarantine-threshold:3}")
    private int quarantineThreshold;

    @Value("${tms.deadletter.initial-retry-delay-ms:1000}")
    private long initialRetryDelayMs;

    @Value("${tms.deadletter.max-retry-delay-ms:300000}")
    private long maxRetryDelayMs;

    public DeadLetterQueueService(
            DeadLetterEventJpaRepository deadLetterRepository,
            IOutboxEventRepository outboxRepository,
            ExternalEventInboxJpaRepository inboxRepository,
            CircuitBreaker circuitBreaker) {
        this.deadLetterRepository = deadLetterRepository;
        this.outboxRepository = outboxRepository;
        this.inboxRepository = inboxRepository;
        this.circuitBreaker = circuitBreaker;
    }

    /**
     * Stores a failed event in the dead letter queue.
     */
    @Transactional
    public void storeFailedEvent(String eventId, String eventType, String payload,
                                String source, String failureReason) {
        DeadLetterEventEntity deadLetterEvent = new DeadLetterEventEntity(
            eventId, eventType, payload, source, failureReason);

        // Schedule next retry with exponential backoff
        Instant nextRetryAt = calculateNextRetryTime(deadLetterEvent.getFailureCount());
        deadLetterEvent.scheduleNextRetry(nextRetryAt);

        deadLetterRepository.save(deadLetterEvent);

        log.warn("Stored failed event {} in dead letter queue. Next retry at: {}",
            eventId, nextRetryAt);
    }

    /**
     * Updates an existing dead letter event with another failure.
     */
    @Transactional
    public void recordFailure(Long deadLetterId, String failureReason) {
        DeadLetterEventEntity event = deadLetterRepository.findById(deadLetterId)
            .orElseThrow(() -> new IllegalArgumentException("Dead letter event not found: " + deadLetterId));

        event.incrementFailureCount();

        // Check if event should be quarantined (consistently failing)
        if (shouldQuarantine(event)) {
            quarantineEvent(event, failureReason);
        } else if (!event.isExpired(maxRetries)) {
            Instant nextRetryAt = calculateNextRetryTime(event.getFailureCount());
            event.scheduleNextRetry(nextRetryAt);
            log.warn("Updated dead letter event {} failure count to {}. Next retry at: {}",
                event.getEventId(), event.getFailureCount(), nextRetryAt);
        } else {
            log.error("Dead letter event {} has exceeded max retries ({})",
                event.getEventId(), maxRetries);
        }

        deadLetterRepository.save(event);
    }

    /**
     * Marks a dead letter event as successfully processed.
     */
    @Transactional
    public void markAsProcessed(Long deadLetterId) {
        DeadLetterEventEntity event = deadLetterRepository.findById(deadLetterId)
            .orElseThrow(() -> new IllegalArgumentException("Dead letter event not found: " + deadLetterId));

        event.markAsProcessed();
        deadLetterRepository.save(event);

        log.info("Marked dead letter event {} as processed", event.getEventId());
    }

    /**
     * Scheduled task to retry events that are ready for retry.
     */
    @Scheduled(fixedDelay = 30000) // Every 30 seconds
    @Transactional
    public void retryReadyEvents() {
        List<DeadLetterEventEntity> readyEvents = deadLetterRepository
            .findEventsReadyForRetry(Instant.now());

        for (DeadLetterEventEntity event : readyEvents) {
            // Skip quarantined events
            if (event.isQuarantined()) {
                log.debug("Skipping quarantined event {}", event.getEventId());
                continue;
            }

            try {
                // Use circuit breaker to attempt retry
                circuitBreaker.execute("dead-letter-retry-" + event.getSource(), () -> {
                    retryEvent(event);
                    return null;
                });

                markAsProcessed(event.getId());

            } catch (Exception e) {
                log.error("Retry failed for dead letter event {}: {}", event.getEventId(), e.getMessage());
                recordFailure(event.getId(), e.getMessage());
            }
        }
    }

    /**
     * Scheduled task to clean up expired events.
     */
    @Scheduled(fixedDelay = 3600000) // Every hour
    @Transactional
    public void cleanupExpiredEvents() {
        List<DeadLetterEventEntity> expiredEvents = deadLetterRepository
            .findExpiredEvents(maxRetries);

        if (!expiredEvents.isEmpty()) {
            log.info("Cleaning up {} expired dead letter events", expiredEvents.size());
            deadLetterRepository.deleteAll(expiredEvents);
        }
    }

    /**
     * Attempts to retry processing a dead letter event.
     * Resets the original event status to PENDING so it can be reprocessed.
     */
    private void retryEvent(DeadLetterEventEntity deadLetterEvent) {
        String source = deadLetterEvent.getSource();
        String eventId = deadLetterEvent.getEventId();

        log.info("Retrying dead letter event: {} from {} (attempt {})",
            eventId, source, deadLetterEvent.getFailureCount() + 1);

        try {
            if ("OUTBOX".equals(source)) {
                // Reset outbox event to pending
                UUID outboxId = UUID.fromString(eventId);
                Optional<OutboxEvent> outboxEventOpt = outboxRepository.findById(outboxId);
                if (outboxEventOpt.isPresent()) {
                    OutboxEvent outboxEvent = outboxEventOpt.get();
                    outboxEvent.resetForRetry();
                    outboxRepository.update(outboxEvent);
                    log.debug("Reset outbox event {} to PENDING for retry", eventId);
                } else {
                    log.warn("Outbox event {} not found for retry", eventId);
                }
            } else if ("INBOX".equals(source)) {
                // Reset inbox event to pending
                UUID inboxId = UUID.fromString(eventId);
                ExternalEventInboxEntity inboxEvent = inboxRepository.findById(inboxId).orElse(null);
                if (inboxEvent != null) {
                    inboxEvent.resetForRetry();
                    inboxRepository.save(inboxEvent);
                    log.debug("Reset inbox event {} to PENDING for retry", eventId);
                } else {
                    log.warn("Inbox event {} not found for retry", eventId);
                }
            } else {
                log.error("Unknown dead letter source: {}", source);
            }
        } catch (Exception e) {
            log.error("Failed to reset event {} for retry: {}", eventId, e.getMessage(), e);
            throw e; // Re-throw to let circuit breaker handle
        }
    }

    /**
     * Calculates the next retry time using exponential backoff.
     */
    private Instant calculateNextRetryTime(int failureCount) {
        // Exponential backoff: initialDelay * 2^(failureCount-1)
        long delayMs = initialRetryDelayMs * (1L << (failureCount - 1));
        // Cap at max retry delay
        delayMs = Math.min(delayMs, maxRetryDelayMs);
        return Instant.now().plusMillis(delayMs);
    }

    /**
     * Determines if an event should be quarantined based on failure patterns.
     * Currently uses consecutive failure count, but could be enhanced with
     * time-based analysis or failure rate patterns.
     */
    private boolean shouldQuarantine(DeadLetterEventEntity event) {
        return event.getFailureCount() >= quarantineThreshold;
    }

    /**
     * Quarantines an event by marking it as permanently failed and preventing further retries.
     */
    private void quarantineEvent(DeadLetterEventEntity event, String failureReason) {
        event.quarantine(failureReason);
        log.error("Quarantined poison pill event {} after {} consecutive failures. Reason: {}",
            event.getEventId(), event.getFailureCount(), failureReason);
    }

    /**
     * Gets statistics about dead letter queue.
     */
    public IDeadLetterService.DeadLetterStats getStats() {
        long outboxCount = deadLetterRepository.countBySourceAndUnprocessed("OUTBOX");
        long inboxCount = deadLetterRepository.countBySourceAndUnprocessed("INBOX");
        long totalUnprocessed = outboxCount + inboxCount;

        return new IDeadLetterService.DeadLetterStats(outboxCount, inboxCount, totalUnprocessed);
    }
}