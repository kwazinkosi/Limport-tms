package com.limport.tms.application.ports;

import java.util.UUID;

/**
 * Port for tracking processed events to ensure idempotency.
 * 
 * In event-driven systems, the same event may be delivered multiple times
 * (network retries, consumer rebalances, etc.). This port allows handlers
 * to check if an event was already processed, preventing duplicate processing.
 * 
 * Implementation options:
 * - Database table (processed_events)
 * - Redis with TTL
 * - In-memory cache (for non-critical events)
 */
public interface IProcessedEventTracker {
    
    /**
     * Check if an event has already been processed.
     * 
     * @param eventId The unique event identifier
     * @return true if already processed, false otherwise
     */
    boolean isProcessed(UUID eventId);
    
    /**
     * Mark an event as processed.
     * Should be called AFTER successful processing, typically in the same transaction.
     * 
     * @param eventId The unique event identifier
     * @param eventType The event type for logging/debugging
     */
    void markAsProcessed(UUID eventId, String eventType);
    
}
