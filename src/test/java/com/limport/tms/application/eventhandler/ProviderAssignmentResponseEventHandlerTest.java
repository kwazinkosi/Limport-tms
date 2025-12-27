package com.limport.tms.application.eventhandler;

import com.limport.tms.application.event.pms.ProviderAssignmentResponseEvent;
import com.limport.tms.application.event.pms.ProviderAssignmentResponseEvent.AssignmentResponse;
import com.limport.tms.domain.model.entity.Assignment;
import com.limport.tms.domain.model.entity.Assignment.AssignmentStatus;
import com.limport.tms.domain.ports.IAssignmentRepository;
import com.limport.tms.domain.ports.ITransportRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProviderAssignmentResponseEventHandlerTest {

    @Mock
    private IAssignmentRepository assignmentRepository;

    @Mock
    private ITransportRequestRepository transportRequestRepository;

    @InjectMocks
    private ProviderAssignmentResponseEventHandler handler;

    private UUID assignmentId;
    private UUID providerId;
    private UUID transportRequestId;
    private Assignment assignment;
    private ProviderAssignmentResponseEvent event;

    @BeforeEach
    void setUp() {
        assignmentId = UUID.randomUUID();
        providerId = UUID.randomUUID();
        transportRequestId = UUID.randomUUID();

        assignment = new Assignment();
        assignment.setId(assignmentId);
        assignment.setProviderId(providerId);
        assignment.setTransportRequestId(transportRequestId);
        assignment.setStatus(AssignmentStatus.ASSIGNED);

        event = new ProviderAssignmentResponseEvent(
            UUID.randomUUID(),
            Instant.now(),
            "PMS",
            transportRequestId,
            assignmentId,
            providerId,
            "Test Provider",
            AssignmentResponse.ACCEPTED,
            "Accepted via app",
            Instant.now()
        );
    }

    @Test
    void handle_AcceptedResponse_UpdatesAssignmentToConfirmed() {
        // Given
        when(assignmentRepository.findById(assignmentId)).thenReturn(Optional.of(assignment));
        when(assignmentRepository.save(any(Assignment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        handler.handle(event);

        // Then
        ArgumentCaptor<Assignment> captor = ArgumentCaptor.forClass(Assignment.class);
        verify(assignmentRepository).save(captor.capture());

        Assignment saved = captor.getValue();
        assertEquals(AssignmentStatus.CONFIRMED, saved.getStatus());
    }

    @Test
    void handle_RejectedResponse_UpdatesAssignmentToCancelled() {
        // Given
        event = new ProviderAssignmentResponseEvent(
            event.eventId(),
            event.occurredOn(),
            event.sourceService(),
            event.transportRequestId(),
            event.assignmentId(),
            event.providerId(),
            event.providerName(),
            AssignmentResponse.REJECTED,
            "Not available",
            event.respondedAt()
        );

        when(assignmentRepository.findById(assignmentId)).thenReturn(Optional.of(assignment));
        when(assignmentRepository.save(any(Assignment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        handler.handle(event);

        // Then
        ArgumentCaptor<Assignment> captor = ArgumentCaptor.forClass(Assignment.class);
        verify(assignmentRepository).save(captor.capture());

        Assignment saved = captor.getValue();
        assertEquals(AssignmentStatus.CANCELLED, saved.getStatus());
    }

    @Test
    void handle_TimeoutResponse_UpdatesAssignmentToCancelled() {
        // Given
        event = new ProviderAssignmentResponseEvent(
            event.eventId(),
            event.occurredOn(),
            event.sourceService(),
            event.transportRequestId(),
            event.assignmentId(),
            event.providerId(),
            event.providerName(),
            AssignmentResponse.TIMEOUT,
            null,
            event.respondedAt()
        );

        when(assignmentRepository.findById(assignmentId)).thenReturn(Optional.of(assignment));
        when(assignmentRepository.save(any(Assignment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        handler.handle(event);

        // Then
        ArgumentCaptor<Assignment> captor = ArgumentCaptor.forClass(Assignment.class);
        verify(assignmentRepository).save(captor.capture());

        Assignment saved = captor.getValue();
        assertEquals(AssignmentStatus.CANCELLED, saved.getStatus());
    }

    @Test
    void handle_AssignmentNotFound_SkipsProcessing() {
        // Given
        when(assignmentRepository.findById(assignmentId)).thenReturn(Optional.empty());

        // When
        handler.handle(event);

        // Then
        verify(assignmentRepository, never()).save(any());
    }

    @Test
    void handle_WrongProviderId_SkipsProcessing() {
        // Given
        assignment.setProviderId(UUID.randomUUID()); // Different provider
        when(assignmentRepository.findById(assignmentId)).thenReturn(Optional.of(assignment));

        // When
        handler.handle(event);

        // Then
        verify(assignmentRepository, never()).save(any());
    }

    @Test
    void getSupportedEventType_ReturnsCorrectType() {
        // When
        String eventType = handler.getSupportedEventType();

        // Then
        assertEquals("PMS.Provider.AssignmentResponse", eventType);
    }
}