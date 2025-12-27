package com.limport.tms.infrastructure.event.consumer;

import com.limport.tms.application.event.ExternalEvent;
import com.limport.tms.application.event.ExternalEventHandlerRegistry;
import com.limport.tms.application.ports.IProcessedEventTracker;
import com.limport.tms.application.service.interfaces.IUnifiedEventSerializer;
import com.limport.tms.domain.event.CorrelationIdContext;
import com.limport.tms.infrastructure.event.DeadLetterQueueService;
import com.limport.tms.infrastructure.event.EventProcessingMetrics;
import com.limport.tms.infrastructure.persistance.entity.ExternalEventInboxEntity;
import com.limport.tms.infrastructure.repository.jpa.ExternalEventInboxJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExternalEventConsumerTest {

    @Mock
    private IUnifiedEventSerializer eventSerializer;

    @Mock
    private ExternalEventInboxJpaRepository inboxRepository;

    @Mock
    private EventProcessingMetrics metrics;

    @Mock
    private ExternalEventHandlerRegistry handlerRegistry;

    @Mock
    private IProcessedEventTracker processedEventTracker;

    @Mock
    private DeadLetterQueueService deadLetterService;

    @Mock
    private Acknowledgment acknowledgment;

    private ExternalEventConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new ExternalEventConsumer(
            eventSerializer, inboxRepository, metrics,
            handlerRegistry, processedEventTracker, deadLetterService);
    }

    @Test
    void consumeExternalEvent_SuccessfulProcessing_AcknowledgesMessage() {
        // Given
        String payload = "{\"eventType\":\"Test.Event\",\"eventId\":\"123e4567-e89b-12d3-a456-426614174000\"}";
        String key = "test-key";
        String topic = "pms.events";
        long offset = 100L;
        UUID eventId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

        ExternalEvent event = mock(ExternalEvent.class);
        when(event.eventId()).thenReturn(eventId);
        when(event.eventType()).thenReturn("Test.Event");
        when(eventSerializer.deserializeExternalEvent(payload)).thenReturn(Optional.of(event));

        ArgumentCaptor<ExternalEventInboxEntity> inboxCaptor = ArgumentCaptor.forClass(ExternalEventInboxEntity.class);
        when(inboxRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(processedEventTracker.isProcessed(eventId)).thenReturn(false);
        when(handlerRegistry.dispatch(event)).thenReturn(true);

        // When
        consumer.consumeExternalEvent(payload, key, topic, offset, acknowledgment);

        // Then
        verify(acknowledgment).acknowledge();
        verify(metrics).recordExternalEventReceived();
        verify(metrics).recordExternalEventProcessed();
        verify(processedEventTracker).markAsProcessed(eventId, "Test.Event");
        verify(inboxRepository, times(2)).save(inboxCaptor.capture()); // Once for storage, once for marking processed

        ExternalEventInboxEntity savedEntity = inboxCaptor.getAllValues().get(1); // The second save call
        assertEquals("PROCESSED", savedEntity.getStatus().toString());
    }

    @Test
    void consumeExternalEvent_AlreadyProcessed_AcknowledgesMessage() {
        // Given
        String payload = "{\"eventType\":\"Test.Event\",\"eventId\":\"123e4567-e89b-12d3-a456-426614174000\"}";
        UUID eventId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        ExternalEvent event = mock(ExternalEvent.class);
        when(event.eventId()).thenReturn(eventId);
        when(eventSerializer.deserializeExternalEvent(payload)).thenReturn(Optional.of(event));

        when(inboxRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(processedEventTracker.isProcessed(eventId)).thenReturn(true); // Already processed

        // When
        consumer.consumeExternalEvent(payload, "key", "topic", 1L, acknowledgment);

        // Then
        verify(acknowledgment).acknowledge();
        verify(handlerRegistry, never()).dispatch(any()); // Should not dispatch
        verify(metrics).recordExternalEventReceived();
        verify(metrics, never()).recordExternalEventProcessed(); // Not processed again
    }

    @Test
    void consumeExternalEvent_HandlerNotFound_DoesNotAcknowledge() {
        // Given
        String payload = "{\"eventType\":\"Test.Event\",\"eventId\":\"123e4567-e89b-12d3-a456-426614174000\"}";
        UUID eventId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        ExternalEvent event = mock(ExternalEvent.class);
        when(event.eventId()).thenReturn(eventId);
        when(event.eventType()).thenReturn("Test.Event");
        when(eventSerializer.deserializeExternalEvent(payload)).thenReturn(Optional.of(event));

        ArgumentCaptor<ExternalEventInboxEntity> inboxCaptor = ArgumentCaptor.forClass(ExternalEventInboxEntity.class);
        when(inboxRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(processedEventTracker.isProcessed(eventId)).thenReturn(false);
        when(handlerRegistry.dispatch(event)).thenReturn(false); // No handler found

        // When
        consumer.consumeExternalEvent(payload, "key", "topic", 1L, acknowledgment);

        // Then
        verify(acknowledgment, never()).acknowledge(); // Should NOT acknowledge
        verify(inboxRepository, times(2)).save(inboxCaptor.capture());
        verify(deadLetterService).storeFailedEvent(
            anyString(),
            eq("Test.Event"),
            eq(payload),
            eq("INBOX"),
            contains("No handler found")
        );
        verify(metrics).recordExternalEventFailed();

        ExternalEventInboxEntity savedEntity = inboxCaptor.getAllValues().get(1);
        assertEquals("PENDING", savedEntity.getStatus().toString());
    }

    @Test
    void consumeExternalEvent_ProcessingException_DoesNotAcknowledge() {
        // Given
        String payload = "{\"eventType\":\"Test.Event\",\"eventId\":\"123e4567-e89b-12d3-a456-426614174000\"}";
        UUID eventId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        ExternalEvent event = mock(ExternalEvent.class);
        when(event.eventId()).thenReturn(eventId);
        when(event.eventType()).thenReturn("Test.Event");
        when(eventSerializer.deserializeExternalEvent(payload)).thenReturn(Optional.of(event));

        ArgumentCaptor<ExternalEventInboxEntity> inboxCaptor = ArgumentCaptor.forClass(ExternalEventInboxEntity.class);
        when(inboxRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(processedEventTracker.isProcessed(eventId)).thenReturn(false);
        when(handlerRegistry.dispatch(event)).thenThrow(new RuntimeException("Processing failed"));

        // When
        consumer.consumeExternalEvent(payload, "key", "topic", 1L, acknowledgment);

        // Then
        verify(acknowledgment, never()).acknowledge(); // Should NOT acknowledge
        verify(inboxRepository, times(2)).save(inboxCaptor.capture());
        verify(deadLetterService).storeFailedEvent(
            anyString(),
            eq("Test.Event"),
            eq(payload),
            eq("INBOX"),
            eq("Processing failed")
        );
        verify(metrics).recordExternalEventFailed();

        ExternalEventInboxEntity savedEntity = inboxCaptor.getAllValues().get(1);
        assertEquals("PENDING", savedEntity.getStatus().toString());
    }

    @Test
    void consumeExternalEvent_InvalidPayload_DoesNotAcknowledge() {
        // Given
        String invalidPayload = "invalid json";
        when(eventSerializer.deserializeExternalEvent(invalidPayload)).thenReturn(Optional.empty());

        // When
        consumer.consumeExternalEvent(invalidPayload, "key", "topic", 1L, acknowledgment);

        // Then
        verify(acknowledgment, never()).acknowledge();
        verify(inboxRepository, never()).save(any()); // Should not save invalid events
    }

    @Test
    void consumeExternalEvent_SetsCorrelationContext() {
        // Given
        String payload = "{\"eventType\":\"Test.Event\",\"eventId\":\"123e4567-e89b-12d3-a456-426614174000\"}";
        UUID eventId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        ExternalEvent event = mock(ExternalEvent.class);
        when(event.eventId()).thenReturn(eventId);
        when(event.eventType()).thenReturn("Test.Event");
        when(eventSerializer.deserializeExternalEvent(payload)).thenReturn(Optional.of(event));

        when(inboxRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(processedEventTracker.isProcessed(eventId)).thenReturn(false);
        when(handlerRegistry.dispatch(event)).thenReturn(true);

        try (MockedStatic<CorrelationIdContext> correlationMock = mockStatic(CorrelationIdContext.class)) {
            // When
            consumer.consumeExternalEvent(payload, "key", "topic", 1L, acknowledgment);

            // Then
            correlationMock.verify(() -> CorrelationIdContext.setIds(null, eventId.toString()));
            correlationMock.verify(CorrelationIdContext::clear);
        }
    }

    @Test
    void storeEventInInbox_BackpressureWarning() {
        // Given
        String payload = "{\"eventType\":\"Test.Event\",\"eventId\":\"123e4567-e89b-12d3-a456-426614174000\"}";
        ExternalEvent event = mock(ExternalEvent.class);
        when(event.eventType()).thenReturn("Test.Event");
        lenient().when(eventSerializer.deserializeExternalEvent(payload)).thenReturn(Optional.of(event));

        when(inboxRepository.countPendingEvents()).thenReturn(3000L); // Above threshold

        // When
        ExternalEventInboxEntity result = consumer.storeEventInInbox(payload, "topic", 1L);

        // Then
        assertNotNull(result);
        assertEquals("Test.Event", result.getEventType());
        verify(metrics).recordExternalEventReceived();
    }
}