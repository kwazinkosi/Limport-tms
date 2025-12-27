package com.limport.tms.infrastructure.adapter;

import com.limport.tms.domain.port.service.IProcessedEventTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * Redis-based implementation of processed event tracker.
 * 
 * Uses Redis SET with TTL for efficient, distributed idempotency checking.
 * 
 * Benefits:
 * - Fast O(1) lookups
 * - Distributed: works across multiple app instances
 * - Automatic cleanup via TTL (no manual maintenance)
 * - Survives application restarts
 * 
 * Key format: processed_event:{eventId}
 * Value: {eventType}|{processedAt}
 * TTL: 7 days (configurable)
 */
@Component
@ConditionalOnProperty(name = "tms.event-tracker.type", havingValue = "redis")
public class RedisProcessedEventTracker implements IProcessedEventTracker {
    
    private static final Logger log = LoggerFactory.getLogger(RedisProcessedEventTracker.class);
    
    private static final String KEY_PREFIX = "processed_event:";
    private static final Duration DEFAULT_TTL = Duration.ofDays(7);
    
    private final StringRedisTemplate redisTemplate;
    private final Duration ttl;
    
    public RedisProcessedEventTracker(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.ttl = DEFAULT_TTL;
        log.info("Initialized Redis processed event tracker with TTL={}", ttl);
    }
    
    @Override
    public boolean isProcessed(UUID eventId) {
        String key = buildKey(eventId);
        Boolean exists = redisTemplate.hasKey(key);
        return Boolean.TRUE.equals(exists);
    }
    
    @Override
    public void markAsProcessed(UUID eventId, String eventType) {
        String key = buildKey(eventId);
        String value = buildValue(eventType);
        
        // Idempotent: SET overwrites if exists, which is fine
        redisTemplate.opsForValue().set(key, value, ttl);
        log.debug("Marked event as processed in Redis: eventId={}, eventType={}, ttl={}", 
            eventId, eventType, ttl);
    }
    
    
    
    private String buildKey(UUID eventId) {
        return KEY_PREFIX + eventId.toString();
    }
    
    private String buildValue(String eventType) {
        return eventType + "|" + Instant.now().toString();
    }
}
