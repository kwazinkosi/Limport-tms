package com.limport.tms.infrastructure.adapter;

import com.limport.tms.domain.port.service.IProcessedEventTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of processed event tracker.
 * 
 * Suitable for:
 * - Development and testing
 * - Single-instance deployments where restart is acceptable
 * - Non-critical events where occasional duplicates are tolerable
 * 
 * NOT suitable for:
 * - Production multi-instance deployments
 * - Events requiring strict exactly-once processing
 * 
 * For production, use Redis (tms.event-tracker.type=redis) or 
 * Database (tms.event-tracker.type=database) implementation.
 */
@Component
@ConditionalOnProperty(name = "tms.event-tracker.type", havingValue = "memory", matchIfMissing = true)
public class InMemoryProcessedEventTracker implements IProcessedEventTracker {
    
    private static final Logger log = LoggerFactory.getLogger(InMemoryProcessedEventTracker.class);
    
    // Simple in-memory store - NOT suitable for production
    private final Map<UUID, ProcessedEventRecord> processedEvents = new ConcurrentHashMap<>();
    
    public InMemoryProcessedEventTracker() {
        log.warn("Using in-memory processed event tracker. " +
            "This is NOT suitable for production - events will be reprocessed after restart!");
    }
    
    @Override
    public boolean isProcessed(UUID eventId) {
        return processedEvents.containsKey(eventId);
    }
    
    @Override
    public void markAsProcessed(UUID eventId, String eventType) {
        processedEvents.put(eventId, new ProcessedEventRecord(eventId, eventType, Instant.now()));
        log.debug("Marked event as processed: eventId={}, eventType={}", eventId, eventType);
    }
    
    
    
    /**
     * Record of a processed event.
     */
    private record ProcessedEventRecord(UUID eventId, String eventType, Instant processedAt) {}
}
