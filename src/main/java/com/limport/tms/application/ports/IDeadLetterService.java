package com.limport.tms.application.ports;

/**
 * Port interface for dead letter queue operations.
 * Defines the contract for handling failed event processing.
 */
public interface IDeadLetterService {

    /**
     * Stores a failed event in the dead letter queue.
     */
    void storeFailedEvent(String eventId, String eventType, String payload,
                         String source, String failureReason);

    /**
     * Records another failure for an existing dead letter event.
     */
    void recordFailure(Long deadLetterId, String failureReason);

    /**
     * Marks a dead letter event as successfully processed.
     */
    void markAsProcessed(Long deadLetterId);

    /**
     * Gets statistics about the dead letter queue.
     */
    DeadLetterStats getStats();

    /**
     * Statistics about dead letter queue.
     */
    class DeadLetterStats {
        public final long outboxFailures;
        public final long inboxFailures;
        public final long totalUnprocessed;

        public DeadLetterStats(long outboxFailures, long inboxFailures, long totalUnprocessed) {
            this.outboxFailures = outboxFailures;
            this.inboxFailures = inboxFailures;
            this.totalUnprocessed = totalUnprocessed;
        }
    }
}