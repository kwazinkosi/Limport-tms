package com.limport.tms.infrastructure.event;

import com.limport.tms.application.ports.IDeadLetterService;
import com.limport.tms.domain.ports.IOutboxEventRepository;
import com.limport.tms.infrastructure.persistance.repository.DeadLetterEventJpaRepository;
import com.limport.tms.infrastructure.repository.jpa.ExternalEventInboxJpaRepository;
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
    private final IOutboxEventRepository outboxRepository;
    private final ExternalEventInboxJpaRepository inboxRepository;

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
            EventProcessingMetrics metrics,
            IOutboxEventRepository outboxRepository,
            ExternalEventInboxJpaRepository inboxRepository) {
        this.deadLetterRepository = deadLetterRepository;
        this.deadLetterQueueService = deadLetterQueueService;
        this.circuitBreaker = circuitBreaker;
        this.metrics = metrics;
        this.outboxRepository = outboxRepository;
        this.inboxRepository = inboxRepository;
    }

    @Override
    public Health health() {
        IDeadLetterService.DeadLetterStats deadLetterStats = deadLetterQueueService.getStats();

        // Check dead letter queue size
        long deadLetterCount = deadLetterStats.totalUnprocessed;
        if (deadLetterCount > deadLetterThreshold) {
            return Health.down()
                .withDetail("deadLetterQueue", deadLetterCount)
                .withDetail("threshold", deadLetterThreshold)
                .withDetail("status", "DEAD_LETTER_QUEUE_TOO_LARGE")
                .build();
        }

        // Check outbox queue size
        long outboxSize = outboxRepository.countPendingEvents();
        if (outboxSize > outboxThreshold) {
            return Health.down()
                .withDetail("outboxSize", outboxSize)
                .withDetail("threshold", outboxThreshold)
                .withDetail("status", "OUTBOX_QUEUE_TOO_LARGE")
                .build();
        }

        // Check inbox queue size
        long inboxSize = inboxRepository.countPendingEvents();
        if (inboxSize > inboxThreshold) {
            return Health.down()
                .withDetail("inboxSize", inboxSize)
                .withDetail("threshold", inboxThreshold)
                .withDetail("status", "INBOX_QUEUE_TOO_LARGE")
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
            .withDetail("outboxSize", outboxSize)
            .withDetail("inboxSize", inboxSize)
            .withDetail("circuitBreaker", circuitState)
            .build();
    }
}