package com.limport.tms.infrastructure.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Simple circuit breaker implementation for external service calls.
 * Prevents cascading failures by temporarily stopping calls to failing services.
 */
@Component
public class CircuitBreaker {

    private static final Logger log = LoggerFactory.getLogger(CircuitBreaker.class);

    public enum State {
        CLOSED,    // Normal operation
        OPEN,      // Circuit is open, calls fail fast
        HALF_OPEN  // Testing if service recovered
    }

    private final ConcurrentHashMap<String, CircuitBreakerState> circuits = new ConcurrentHashMap<>();

    /**
     * Executes an operation with circuit breaker protection.
     */
    public <T> T execute(String serviceName, CircuitBreakerOperation<T> operation) throws Exception {
        CircuitBreakerState state = circuits.computeIfAbsent(serviceName,
            k -> new CircuitBreakerState());

        if (state.getState() == State.OPEN) {
            if (state.shouldAttemptReset()) {
                state.setState(State.HALF_OPEN);
                log.info("Circuit breaker for {} entering HALF_OPEN state", serviceName);
            } else {
                throw new CircuitBreakerOpenException("Circuit breaker is OPEN for service: " + serviceName);
            }
        }

        try {
            T result = operation.execute();
            state.recordSuccess();
            return result;
        } catch (Exception e) {
            state.recordFailure();
            throw e;
        }
    }

    /**
     * Gets the current state of a circuit breaker.
     */
    public State getState(String serviceName) {
        return circuits.getOrDefault(serviceName, new CircuitBreakerState()).getState();
    }

    /**
     * Functional interface for operations to be executed with circuit breaker protection.
     */
    @FunctionalInterface
    public interface CircuitBreakerOperation<T> {
        T execute() throws Exception;
    }

    /**
     * Exception thrown when circuit breaker is open.
     */
    public static class CircuitBreakerOpenException extends RuntimeException {
        public CircuitBreakerOpenException(String message) {
            super(message);
        }
    }

    /**
     * Internal state management for a single circuit breaker.
     */
    private static class CircuitBreakerState {
        private final AtomicReference<State> state = new AtomicReference<>(State.CLOSED);
        private final AtomicInteger failureCount = new AtomicInteger(0);
        private final AtomicInteger successCount = new AtomicInteger(0);
        private volatile Instant lastFailureTime = Instant.now();

        // Configuration - could be made configurable
        private static final int FAILURE_THRESHOLD = 5;
        private static final int SUCCESS_THRESHOLD = 3;
        private static final long TIMEOUT_MS = 60000; // 1 minute

        public State getState() {
            return state.get();
        }

        public void setState(State newState) {
            state.set(newState);
        }

        public void recordSuccess() {
            failureCount.set(0);
            if (state.get() == State.HALF_OPEN) {
                int successes = successCount.incrementAndGet();
                if (successes >= SUCCESS_THRESHOLD) {
                    state.set(State.CLOSED);
                    successCount.set(0);
                    log.info("Circuit breaker closed after {} successes", successes);
                }
            }
        }

        public void recordFailure() {
            failureCount.incrementAndGet();
            lastFailureTime = Instant.now();
            successCount.set(0);

            if (state.get() == State.HALF_OPEN) {
                state.set(State.OPEN);
                log.warn("Circuit breaker opened due to failure in HALF_OPEN state");
            } else if (failureCount.get() >= FAILURE_THRESHOLD) {
                state.set(State.OPEN);
                log.warn("Circuit breaker opened after {} failures", failureCount.get());
            }
        }

        public boolean shouldAttemptReset() {
            return state.get() == State.OPEN &&
                   Instant.now().isAfter(lastFailureTime.plusMillis(TIMEOUT_MS));
        }
    }
}