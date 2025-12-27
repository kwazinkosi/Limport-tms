package com.limport.tms.infrastructure.event;

import com.limport.tms.domain.port.service.IDeadLetterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UnifiedEventProcessorTest {

    @Mock
    private Logger log;

    @Mock
    private EventProcessingMetrics metrics;

    @Mock
    private IDeadLetterService deadLetterService;

    private TestUnifiedEventProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new TestUnifiedEventProcessor(log, metrics, deadLetterService, 3);
    }

    @Test
    void processPendingEvents_AllSuccessful_ReturnsSuccessCount() {
        // Given
        processor.setPendingEvents(Arrays.asList("event1", "event2", "event3"));
        processor.setProcessingResults(true, true, true); // All succeed

        // When
        int result = processor.processPendingEvents(10);

        // Then
        assertEquals(3, result);
        verify(metrics, times(3)).recordDomainEventPublished();
        verify(metrics, never()).recordDomainEventFailed();
        verify(deadLetterService, never()).storeFailedEvent(anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void processPendingEvents_IsolatedFailures_ContinuesProcessing() {
        // Given
        processor.setPendingEvents(Arrays.asList("event1", "event2", "event3"));
        processor.setProcessingResults(true, false, true); // event2 fails, others succeed

        // When
        int result = processor.processPendingEvents(10);

        // Then
        assertEquals(2, result); // 2 successful
        verify(metrics, times(2)).recordDomainEventPublished();
        verify(metrics, times(1)).recordDomainEventFailed();
        verify(deadLetterService, times(1)).storeFailedEvent(anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void processPendingEvents_ConsecutiveFailures_StopProcessing() {
        // Given
        processor.setPendingEvents(Arrays.asList("event1", "event2", "event3", "event4"));
        processor.setProcessingResults(false, false, false, true); // First 3 fail, 4th would succeed

        // When
        int result = processor.processPendingEvents(10);

        // Then
        assertEquals(0, result); // No successes
        verify(metrics, times(3)).recordDomainEventFailed(); // Only first 3 processed
        verify(metrics, never()).recordDomainEventPublished();
        verify(deadLetterService, times(3)).storeFailedEvent(anyString(), anyString(), anyString(), anyString(), anyString());
        // event4 should not be processed due to consecutive failures
    }

    @Test
    void processPendingEvents_SuccessResetsConsecutiveFailureCounter() {
        // Given
        processor.setPendingEvents(Arrays.asList("event1", "event2", "event3", "event4", "event5"));
        processor.setProcessingResults(false, false, true, false, false); // Fail, Fail, Success, Fail, Fail

        // When
        int result = processor.processPendingEvents(10);

        // Then
        assertEquals(1, result); // Only the success
        verify(metrics, times(1)).recordDomainEventPublished();
        verify(metrics, times(4)).recordDomainEventFailed(); // All except the success
        // Should process all 5 because success reset the counter
    }

    @Test
    void processPendingEvents_EmptyBatch_ReturnsZero() {
        // Given
        processor.setPendingEvents(Collections.emptyList());

        // When
        int result = processor.processPendingEvents(10);

        // Then
        assertEquals(0, result);
        verifyNoInteractions(metrics);
        verifyNoInteractions(deadLetterService);
    }

    @Test
    void processPendingEvents_ExceptionInProcessing_HandlesFailure() {
        // Given
        processor.setPendingEvents(Arrays.asList("event1"));
        processor.setProcessingException(new RuntimeException("Processing failed"));

        // When
        int result = processor.processPendingEvents(10);

        // Then
        assertEquals(0, result);
        verify(metrics, times(1)).recordDomainEventFailed();
        verify(deadLetterService, times(1)).storeFailedEvent(anyString(), anyString(), anyString(), anyString(), anyString());
    }

    // Test implementation of UnifiedEventProcessor
    private static class TestUnifiedEventProcessor extends UnifiedEventProcessor<String> {
        private List<String> pendingEvents = Collections.emptyList();
        private List<Boolean> processingResults = Collections.emptyList();
        private RuntimeException processingException;

        public TestUnifiedEventProcessor(Logger log, EventProcessingMetrics metrics,
                                       IDeadLetterService deadLetterService, int maxConsecutiveFailures) {
            super(log, metrics, deadLetterService, maxConsecutiveFailures);
        }

        public void setPendingEvents(List<String> events) {
            this.pendingEvents = events;
        }

        public void setProcessingResults(Boolean... results) {
            this.processingResults = Arrays.asList(results);
        }

        public void setProcessingException(RuntimeException exception) {
            this.processingException = exception;
        }

        @Override
        protected List<String> findPendingEvents(int batchSize) {
            return pendingEvents;
        }

        @Override
        protected boolean processEvent(String event) {
            if (processingException != null) {
                throw processingException;
            }
            // Return results in order, cycling if needed
            int index = pendingEvents.indexOf(event);
            if (index < processingResults.size()) {
                return processingResults.get(index);
            }
            return true; // Default to success
        }

        @Override
        protected String getEventId(String event) {
            return event;
        }

        @Override
        protected void handleProcessingFailure(String event, Exception e) {
            deadLetterService.storeFailedEvent(event, "TEST_EVENT", "payload", "TEST", e.getMessage());
        }

        @Override
        protected void recordSuccess() {
            metrics.recordDomainEventPublished();
        }

        @Override
        protected void recordFailure() {
            metrics.recordDomainEventFailed();
        }
    }
}