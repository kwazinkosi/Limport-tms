package com.limport.tms.infrastructure.event;

import com.limport.tms.domain.model.entity.OutboxEvent;
import com.limport.tms.domain.port.repository.IOutboxEventRepository;
import com.limport.tms.infrastructure.persistence.entity.DeadLetterEventEntity;
import com.limport.tms.infrastructure.persistence.entity.ExternalEventInboxEntity;
import com.limport.tms.infrastructure.repository.jpa.DeadLetterEventJpaRepository;
import com.limport.tms.infrastructure.repository.jpa.ExternalEventInboxJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeadLetterQueueServiceTest {

    @Mock
    private DeadLetterEventJpaRepository deadLetterRepository;

    @Mock
    private IOutboxEventRepository outboxRepository;

    @Mock
    private ExternalEventInboxJpaRepository inboxRepository;

    @Mock
    private CircuitBreaker circuitBreaker;

    private DeadLetterQueueService deadLetterService;

    @BeforeEach
    void setUp() {
        deadLetterService = new DeadLetterQueueService(
            deadLetterRepository, outboxRepository, inboxRepository, circuitBreaker);
    }

    @Test
    void retryReadyEvents_OutboxEvent_SuccessfullyResetsStatus() throws Exception {
        // Given
        String eventId = UUID.randomUUID().toString();
        DeadLetterEventEntity deadLetterEvent = createDeadLetterEventWithId(1L, eventId, "OUTBOX");

        OutboxEvent outboxEvent = mock(OutboxEvent.class);
        when(outboxRepository.findById(UUID.fromString(eventId))).thenReturn(Optional.of(outboxEvent));
        when(deadLetterRepository.findEventsReadyForRetry(any(Instant.class)))
            .thenReturn(List.of(deadLetterEvent));
        when(deadLetterRepository.findById(1L)).thenReturn(Optional.of(deadLetterEvent));
        
        // Configure circuit breaker to actually execute the lambda
        when(circuitBreaker.execute(eq("dead-letter-retry-OUTBOX"), any()))
            .thenAnswer(invocation -> {
                CircuitBreaker.CircuitBreakerOperation<?> operation = invocation.getArgument(1);
                return operation.execute();
            });

        // When
        deadLetterService.retryReadyEvents();

        // Then
        verify(outboxEvent).resetForRetry();
        verify(outboxRepository).update(outboxEvent);
        verify(deadLetterRepository).save(deadLetterEvent); // markAsProcessed saves the entity
    }

    @Test
    void retryReadyEvents_InboxEvent_SuccessfullyResetsStatus() throws Exception {
        // Given
        String eventId = UUID.randomUUID().toString();
        DeadLetterEventEntity deadLetterEvent = createDeadLetterEventWithId(1L, eventId, "INBOX");

        ExternalEventInboxEntity inboxEntity = mock(ExternalEventInboxEntity.class);
        when(inboxRepository.findById(UUID.fromString(eventId))).thenReturn(Optional.of(inboxEntity));
        when(deadLetterRepository.findEventsReadyForRetry(any(Instant.class)))
            .thenReturn(List.of(deadLetterEvent));
        when(deadLetterRepository.findById(1L)).thenReturn(Optional.of(deadLetterEvent));
        
        // Configure circuit breaker to actually execute the lambda
        when(circuitBreaker.execute(eq("dead-letter-retry-INBOX"), any()))
            .thenAnswer(invocation -> {
                CircuitBreaker.CircuitBreakerOperation<?> operation = invocation.getArgument(1);
                return operation.execute();
            });

        // When
        deadLetterService.retryReadyEvents();

        // Then
        verify(inboxEntity).resetForRetry();
        verify(inboxRepository).save(inboxEntity);
        verify(deadLetterRepository).save(deadLetterEvent); // markAsProcessed saves the entity
    }

    @Test
    void retryReadyEvents_OutboxEventNotFound_LogsWarning() throws Exception {
        // Given
        String eventId = UUID.randomUUID().toString();
        DeadLetterEventEntity deadLetterEvent = createDeadLetterEventWithId(1L, eventId, "OUTBOX");

        when(outboxRepository.findById(UUID.fromString(eventId))).thenReturn(Optional.empty());
        when(deadLetterRepository.findEventsReadyForRetry(any(Instant.class)))
            .thenReturn(List.of(deadLetterEvent));
        when(deadLetterRepository.findById(1L)).thenReturn(Optional.of(deadLetterEvent));
        
        // Configure circuit breaker to actually execute the lambda
        when(circuitBreaker.execute(eq("dead-letter-retry-OUTBOX"), any()))
            .thenAnswer(invocation -> {
                CircuitBreaker.CircuitBreakerOperation<?> operation = invocation.getArgument(1);
                return operation.execute();
            });

        // When
        deadLetterService.retryReadyEvents();

        // Then - event not found but still marks as processed (logged warning)
        verify(outboxRepository, never()).update(any());
        verify(deadLetterRepository).save(deadLetterEvent);
    }

    @Test
    void retryReadyEvents_InboxEventNotFound_LogsWarning() throws Exception {
        // Given
        String eventId = UUID.randomUUID().toString();
        DeadLetterEventEntity deadLetterEvent = createDeadLetterEventWithId(1L, eventId, "INBOX");

        when(inboxRepository.findById(UUID.fromString(eventId))).thenReturn(Optional.empty());
        when(deadLetterRepository.findEventsReadyForRetry(any(Instant.class)))
            .thenReturn(List.of(deadLetterEvent));
        when(deadLetterRepository.findById(1L)).thenReturn(Optional.of(deadLetterEvent));
        
        // Configure circuit breaker to actually execute the lambda
        when(circuitBreaker.execute(eq("dead-letter-retry-INBOX"), any()))
            .thenAnswer(invocation -> {
                CircuitBreaker.CircuitBreakerOperation<?> operation = invocation.getArgument(1);
                return operation.execute();
            });

        // When
        deadLetterService.retryReadyEvents();

        // Then - event not found but still marks as processed (logged warning)
        verify(inboxRepository, never()).save(any(ExternalEventInboxEntity.class));
        verify(deadLetterRepository).save(deadLetterEvent);
    }

    @Test
    void retryReadyEvents_UnknownSource_LogsError() throws Exception {
        // Given
        DeadLetterEventEntity deadLetterEvent = createDeadLetterEventWithId(1L, UUID.randomUUID().toString(), "UNKNOWN");

        when(deadLetterRepository.findEventsReadyForRetry(any(Instant.class)))
            .thenReturn(List.of(deadLetterEvent));
        when(deadLetterRepository.findById(1L)).thenReturn(Optional.of(deadLetterEvent));
        
        // Configure circuit breaker to actually execute the lambda
        when(circuitBreaker.execute(eq("dead-letter-retry-UNKNOWN"), any()))
            .thenAnswer(invocation -> {
                CircuitBreaker.CircuitBreakerOperation<?> operation = invocation.getArgument(1);
                return operation.execute();
            });

        // When
        deadLetterService.retryReadyEvents();

        // Then - unknown source still marks as processed (logged error)
        verify(deadLetterRepository).save(deadLetterEvent);
    }

    @Test
    void retryReadyEvents_CircuitBreakerFails_RecordsFailure() throws Exception {
        // Given
        String eventId = UUID.randomUUID().toString();
        DeadLetterEventEntity deadLetterEvent = createDeadLetterEventWithId(1L, eventId, "OUTBOX");

        when(circuitBreaker.execute(eq("dead-letter-retry-OUTBOX"), any()))
            .thenThrow(new RuntimeException("Circuit breaker open"));
        when(deadLetterRepository.findEventsReadyForRetry(any(Instant.class)))
            .thenReturn(List.of(deadLetterEvent));
        when(deadLetterRepository.findById(1L)).thenReturn(Optional.of(deadLetterEvent));

        // When
        deadLetterService.retryReadyEvents();

        // Then - failure is recorded
        verify(deadLetterRepository).save(any(DeadLetterEventEntity.class));
    }

    @Test
    void retryReadyEvents_NoEventsReady_DoesNothing() {
        // Given
        when(deadLetterRepository.findEventsReadyForRetry(any(Instant.class)))
            .thenReturn(List.of());

        // When
        deadLetterService.retryReadyEvents();

        // Then
        verifyNoInteractions(outboxRepository, inboxRepository, circuitBreaker);
    }

    private DeadLetterEventEntity createDeadLetterEvent(String eventId, String source) {
        return new DeadLetterEventEntity(eventId, "TestEvent", "{}", source, "Test failure");
    }

    private DeadLetterEventEntity createDeadLetterEventWithId(Long id, String eventId, String source) {
        DeadLetterEventEntity entity = mock(DeadLetterEventEntity.class);
        when(entity.getId()).thenReturn(id);
        when(entity.getEventId()).thenReturn(eventId);
        when(entity.getSource()).thenReturn(source);
        when(entity.getFailureCount()).thenReturn(1);
        return entity;
    }
}