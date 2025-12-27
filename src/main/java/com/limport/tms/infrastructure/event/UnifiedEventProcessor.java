package com.limport.tms.infrastructure.event;

import com.limport.tms.domain.port.service.IDeadLetterService;
import org.slf4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Abstract base class for unified event processing (inbox and outbox patterns).
 *
 * Provides common batch processing, error handling, and metrics collection logic
 * to eliminate duplication between inbox and outbox processors.
 *
 * @param <T> The type of event entity being processed
 */
public abstract class UnifiedEventProcessor<T> {

    protected final Logger log;
    protected final EventProcessingMetrics metrics;
    protected final IDeadLetterService deadLetterService;
    protected final int maxConsecutiveFailures;

    protected UnifiedEventProcessor(Logger log, EventProcessingMetrics metrics, IDeadLetterService deadLetterService, int maxConsecutiveFailures) {
        this.log = log;
        this.metrics = metrics;
        this.deadLetterService = deadLetterService;
        this.maxConsecutiveFailures = maxConsecutiveFailures;
    }

    /**
     * Processes a batch of pending events.
     * @param batchSize maximum number of events to process
     * @return number of successfully processed events
     */
    @Transactional
    public int processPendingEvents(int batchSize) {
        List<T> pendingEvents = findPendingEvents(batchSize);
        int successCount = 0;
        int consecutiveFailures = 0;

        for (T event : pendingEvents) {
            try {
                boolean processed = processEvent(event);
                if (processed) {
                    successCount++;
                    consecutiveFailures = 0; // Reset on success
                    recordSuccess();
                } else {
                    consecutiveFailures++;
                    recordFailure();
                    handleProcessingFailure(event, new RuntimeException("Event processing returned false"));
                }
            } catch (Exception e) {
                log.error("Failed to process event {}: {}", getEventId(event), e.getMessage());
                handleProcessingFailure(event, e);
                recordFailure();
                consecutiveFailures++;
            }

            // Break on too many consecutive failures to prevent wasting resources
            // when downstream systems are consistently failing
            if (consecutiveFailures >= maxConsecutiveFailures) {
                log.warn("Breaking batch processing after {} consecutive failures", consecutiveFailures);
                break;
            }
        }

        if (successCount > 0) {
            log.info("Processed {}/{} events", successCount, pendingEvents.size());
        }

        return successCount;
    }

    /**
     * Finds pending events to process.
     */
    protected abstract List<T> findPendingEvents(int batchSize);

    /**
     * Processes a single event.
     * @return true if processing succeeded, false otherwise
     */
    protected abstract boolean processEvent(T event);

    /**
     * Gets the event ID for logging purposes.
     */
    protected abstract String getEventId(T event);

    /**
     * Handles processing failure (dead letter queue, status updates, etc.).
     */
    protected abstract void handleProcessingFailure(T event, Exception e);

    /**
     * Records success metrics.
     */
    protected abstract void recordSuccess();

    /**
     * Records failure metrics.
     */
    protected abstract void recordFailure();
}