package com.limport.tms.infrastructure.event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CircuitBreakerTest {

    private CircuitBreaker circuitBreaker;

    @BeforeEach
    void setUp() {
        // Use custom configuration for testing
        circuitBreaker = new CircuitBreaker(3, 2, 1000); // 3 failures, 2 successes, 1 second timeout
    }

    @Test
    void execute_SuccessfulOperation_StaysClosed() throws Exception {
        // When
        String result = circuitBreaker.execute("test-service", () -> "success");

        // Then
        assertEquals("success", result);
        assertEquals(CircuitBreaker.State.CLOSED, circuitBreaker.getState("test-service"));
    }

    @Test
    void execute_FailuresOpenCircuit() throws Exception {
        // When - 3 failures
        for (int i = 0; i < 3; i++) {
            assertThrows(RuntimeException.class, () ->
                circuitBreaker.execute("test-service", () -> { throw new RuntimeException("fail"); }));
        }

        // Then - circuit should be open
        assertEquals(CircuitBreaker.State.OPEN, circuitBreaker.getState("test-service"));

        // And further calls should fail fast
        assertThrows(CircuitBreaker.CircuitBreakerOpenException.class, () ->
            circuitBreaker.execute("test-service", () -> "should not execute"));
    }

    @Test
    void execute_SuccessesAfterFailuresCloseCircuit() throws Exception {
        // Given - Open the circuit
        for (int i = 0; i < 3; i++) {
            assertThrows(RuntimeException.class, () ->
                circuitBreaker.execute("test-service", () -> { throw new RuntimeException("fail"); }));
        }
        assertEquals(CircuitBreaker.State.OPEN, circuitBreaker.getState("test-service"));

        // When - Wait for timeout and try successful operations
        Thread.sleep(1100); // Wait longer than timeout

        // First success should put in half-open
        String result1 = circuitBreaker.execute("test-service", () -> "success1");
        assertEquals("success1", result1);
        // Should still be half-open after first success
        assertEquals(CircuitBreaker.State.HALF_OPEN, circuitBreaker.getState("test-service"));

        // Second success should close the circuit
        String result2 = circuitBreaker.execute("test-service", () -> "success2");
        assertEquals("success2", result2);
        assertEquals(CircuitBreaker.State.CLOSED, circuitBreaker.getState("test-service"));
    }

    @Test
    void execute_FailureInHalfOpenReopensCircuit() throws Exception {
        // Given - Open the circuit
        for (int i = 0; i < 3; i++) {
            assertThrows(RuntimeException.class, () ->
                circuitBreaker.execute("test-service", () -> { throw new RuntimeException("fail"); }));
        }
        assertEquals(CircuitBreaker.State.OPEN, circuitBreaker.getState("test-service"));

        // When - Wait for timeout and try, but fail
        Thread.sleep(1100);
        assertThrows(RuntimeException.class, () ->
            circuitBreaker.execute("test-service", () -> { throw new RuntimeException("fail in half-open"); }));

        // Then - Should be back to open
        assertEquals(CircuitBreaker.State.OPEN, circuitBreaker.getState("test-service"));
    }

    @Test
    void execute_DifferentServices_Isolated() throws Exception {
        // Given - Open circuit for service1
        for (int i = 0; i < 3; i++) {
            assertThrows(RuntimeException.class, () ->
                circuitBreaker.execute("service1", () -> { throw new RuntimeException("fail"); }));
        }
        assertEquals(CircuitBreaker.State.OPEN, circuitBreaker.getState("service1"));

        // When - Use service2
        String result = circuitBreaker.execute("service2", () -> "success");

        // Then - service2 should work fine
        assertEquals("success", result);
        assertEquals(CircuitBreaker.State.OPEN, circuitBreaker.getState("service1")); // Still open
        assertEquals(CircuitBreaker.State.CLOSED, circuitBreaker.getState("service2")); // Still closed
    }

    @Test
    void getState_UnknownService_ReturnsClosed() {
        // When
        CircuitBreaker.State state = circuitBreaker.getState("unknown-service");

        // Then
        assertEquals(CircuitBreaker.State.CLOSED, state);
    }
}