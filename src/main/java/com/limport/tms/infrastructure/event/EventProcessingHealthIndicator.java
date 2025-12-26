package com.limport.tms.infrastructure.event;

import com.limport.tms.infrastructure.persistance.repository.DeadLetterEventJpaRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Health indicator for event processing system.
 * Monitors queue sizes, dead letter queue, and circuit breaker status.
 */
@Component
public class EventProcessingHealthIndicator implements HealthIndicator {

    private final DeadLetterEventJpaRepository deadLetterRepository;
    private final DeadLetterQueueService deadLetterQueueService;
    private final CircuitBreaker circuitBreaker;
    private final EventProcessingMetrics metrics;

    @Value("${tms.health.dead-letter-threshold:100}")
    private int deadLetterThreshold;

    @Value("${tms.health.outbox-threshold:1000}")
    private int outboxThreshold;

    @Value("${tms.health.inbox-threshold:1000}")
    private int inboxThreshold;

    public EventProcessingHealthIndicator(
            DeadLetterEventJpaRepository deadLetterRepository,
            DeadLetterQueueService deadLetterQueueService,
            CircuitBreaker circuitBreaker,
            EventProcessingMetrics metrics) {
        this.deadLetterRepository = deadLetterRepository;
        this.deadLetterQueueService = deadLetterQueueService;
        this.circuitBreaker = circuitBreaker;
        this.metrics = metrics;
    }

    @Override
    public Health health() {
        DeadLetterQueueService.DeadLetterStats deadLetterStats = deadLetterQueueService.getStats();

        // Check dead letter queue size
        long deadLetterCount = deadLetterStats.totalUnprocessed;
        if (deadLetterCount > deadLetterThreshold) {
            return Health.down()
                .withDetail("deadLetterQueue", deadLetterCount)
                .withDetail("threshold", deadLetterThreshold)
                .withDetail("status", "DEAD_LETTER_QUEUE_TOO_LARGE")
                .build();
        }

        // Check circuit breaker status
        CircuitBreaker.State circuitState = circuitBreaker.getState("kafka-publisher");
        if (circuitState == CircuitBreaker.State.OPEN) {
            return Health.down()
                .withDetail("circuitBreaker", "OPEN")
                .withDetail("status", "CIRCUIT_BREAKER_OPEN")
                .build();
        }

        // All checks passed
        return Health.up()
            .withDetail("deadLetterQueue", deadLetterCount)
            .withDetail("circuitBreaker", circuitState)
            .withDetail("outboxSize", 0) // TODO: Get from repository
            .withDetail("inboxSize", 0)  // TODO: Get from repository
            .build();
    }
}