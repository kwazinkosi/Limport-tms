package com.limport.tms.infrastructure.event;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Metrics service for event processing monitoring.
 * Provides counters, timers, and gauges for event processing success/failure rates.
 */
@Component
public class EventProcessingMetrics {

    // Counters for event processing
    private final Counter domainEventsPublished;
    private final Counter domainEventsFailed;
    private final Counter externalEventsReceived;
    private final Counter externalEventsProcessed;
    private final Counter externalEventsFailed;
    private final Counter deadLetterEventsStored;
    private final Counter deadLetterEventsRetried;
    private final Counter deadLetterEventsExpired;

    // Timers for performance monitoring
    private final Timer domainEventPublishTimer;
    private final Timer externalEventProcessTimer;
    private final Timer deadLetterRetryTimer;

    // Gauges for queue sizes
    private final AtomicLong outboxQueueSize = new AtomicLong(0);
    private final AtomicLong inboxQueueSize = new AtomicLong(0);
    private final AtomicLong deadLetterQueueSize = new AtomicLong(0);

    public EventProcessingMetrics(MeterRegistry meterRegistry) {
        // Initialize counters
        domainEventsPublished = Counter.builder("tms.events.domain.published")
            .description("Number of domain events successfully published")
            .register(meterRegistry);

        domainEventsFailed = Counter.builder("tms.events.domain.failed")
            .description("Number of domain events that failed to publish")
            .register(meterRegistry);

        externalEventsReceived = Counter.builder("tms.events.external.received")
            .description("Number of external events received")
            .register(meterRegistry);

        externalEventsProcessed = Counter.builder("tms.events.external.processed")
            .description("Number of external events successfully processed")
            .register(meterRegistry);

        externalEventsFailed = Counter.builder("tms.events.external.failed")
            .description("Number of external events that failed processing")
            .register(meterRegistry);

        deadLetterEventsStored = Counter.builder("tms.events.deadletter.stored")
            .description("Number of events stored in dead letter queue")
            .register(meterRegistry);

        deadLetterEventsRetried = Counter.builder("tms.events.deadletter.retried")
            .description("Number of dead letter events retried")
            .register(meterRegistry);

        deadLetterEventsExpired = Counter.builder("tms.events.deadletter.expired")
            .description("Number of dead letter events that expired")
            .register(meterRegistry);

        // Initialize timers
        domainEventPublishTimer = Timer.builder("tms.events.domain.publish.duration")
            .description("Time taken to publish domain events")
            .register(meterRegistry);

        externalEventProcessTimer = Timer.builder("tms.events.external.process.duration")
            .description("Time taken to process external events")
            .register(meterRegistry);

        deadLetterRetryTimer = Timer.builder("tms.events.deadletter.retry.duration")
            .description("Time taken to retry dead letter events")
            .register(meterRegistry);

        // Initialize gauges
        Gauge.builder("tms.events.outbox.size", outboxQueueSize, AtomicLong::get)
            .description("Current size of outbox event queue")
            .register(meterRegistry);

        Gauge.builder("tms.events.inbox.size", inboxQueueSize, AtomicLong::get)
            .description("Current size of inbox event queue")
            .register(meterRegistry);

        Gauge.builder("tms.events.deadletter.size", deadLetterQueueSize, AtomicLong::get)
            .description("Current size of dead letter queue")
            .register(meterRegistry);
    }

    // Domain event metrics
    public void recordDomainEventPublished() {
        domainEventsPublished.increment();
    }

    public void recordDomainEventFailed() {
        domainEventsFailed.increment();
    }

    public Timer.Sample startDomainEventPublishTimer() {
        return Timer.start();
    }

    // External event metrics
    public void recordExternalEventReceived() {
        externalEventsReceived.increment();
    }

    public void recordExternalEventProcessed() {
        externalEventsProcessed.increment();
    }

    public void recordExternalEventFailed() {
        externalEventsFailed.increment();
    }

    public Timer.Sample startExternalEventProcessTimer() {
        return Timer.start();
    }

    // Dead letter queue metrics
    public void recordDeadLetterEventStored() {
        deadLetterEventsStored.increment();
    }

    public void recordDeadLetterEventRetried() {
        deadLetterEventsRetried.increment();
    }

    public void recordDeadLetterEventExpired() {
        deadLetterEventsExpired.increment();
    }

    public Timer.Sample startDeadLetterRetryTimer() {
        return Timer.start();
    }

    // Getters for timers (used by Timer.Sample.stop())
    public Timer getDomainEventPublishTimer() {
        return domainEventPublishTimer;
    }

    public Timer getExternalEventProcessTimer() {
        return externalEventProcessTimer;
    }

    public Timer getDeadLetterRetryTimer() {
        return deadLetterRetryTimer;
    }

    // Queue size updates
    public void updateOutboxQueueSize(long size) {
        outboxQueueSize.set(size);
    }

    public void updateInboxQueueSize(long size) {
        inboxQueueSize.set(size);
    }

    public void updateDeadLetterQueueSize(long size) {
        deadLetterQueueSize.set(size);
    }

    // Circuit breaker metrics
    public void registerCircuitBreakerMetrics(MeterRegistry meterRegistry, CircuitBreaker circuitBreaker) {
        // Register circuit breaker state as a gauge
        Gauge.builder("tms.circuitbreaker.state", () ->
                circuitBreaker.getState("kafka-publisher").ordinal())
            .description("Circuit breaker state (0=CLOSED, 1=OPEN, 2=HALF_OPEN)")
            .register(meterRegistry);
    }
}