package com.limport.tms.application.eventhandler;

import com.limport.tms.application.event.pms.ProviderAssignmentResponseEvent;
import com.limport.tms.application.event.pms.ProviderAssignmentResponseEvent.AssignmentResponse;
import com.limport.tms.application.service.interfaces.IDomainEventService;
import com.limport.tms.domain.event.states.TransportRequestReMatchingTriggeredEvent;
import com.limport.tms.domain.model.entity.Assignment;
import com.limport.tms.domain.model.entity.Assignment.AssignmentStatus;
import com.limport.tms.domain.model.entity.TransportRequest;
import com.limport.tms.domain.model.enums.TransportRequestStatus;
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

    @Mock
    private IDomainEventService domainEventService;

    @InjectMocks
    private ProviderAssignmentResponseEventHandler handler;

    private UUID assignmentId;
    private UUID providerId;
    private UUID transportRequestId;
    private Assignment assignment;
    private TransportRequest transportRequest;
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

        transportRequest = new TransportRequest();
        transportRequest.setId(transportRequestId);
        transportRequest.setStatus(TransportRequestStatus.PLANNED);
        transportRequest.setAssignmentAttempts(0);

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

        // Set default max attempts for tests
        handler.setMaxAssignmentAttempts(3);
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
        when(transportRequestRepository.findById(transportRequestId)).thenReturn(Optional.of(transportRequest));
        when(transportRequestRepository.save(any(TransportRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        handler.handle(event);

        // Then
        ArgumentCaptor<Assignment> assignmentCaptor = ArgumentCaptor.forClass(Assignment.class);
        verify(assignmentRepository).save(assignmentCaptor.capture());
        Assignment savedAssignment = assignmentCaptor.getValue();
        assertEquals(AssignmentStatus.CANCELLED, savedAssignment.getStatus());

        ArgumentCaptor<TransportRequest> transportRequestCaptor = ArgumentCaptor.forClass(TransportRequest.class);
        verify(transportRequestRepository).save(transportRequestCaptor.capture());
        TransportRequest savedTransportRequest = transportRequestCaptor.getValue();
        assertEquals(TransportRequestStatus.REQUESTED, savedTransportRequest.getStatus());
        assertEquals(1, savedTransportRequest.getAssignmentAttempts());

        // Verify re-matching event is published
        ArgumentCaptor<TransportRequestReMatchingTriggeredEvent> eventCaptor = ArgumentCaptor.forClass(TransportRequestReMatchingTriggeredEvent.class);
        verify(domainEventService).publishToOutbox(eventCaptor.capture(), eq("TransportRequest"), eq(transportRequestId.toString()));
        TransportRequestReMatchingTriggeredEvent publishedEvent = eventCaptor.getValue();
        assertEquals(transportRequestId, publishedEvent.getTransportRequestId());
        assertEquals(assignmentId, publishedEvent.getPreviousAssignmentId());
        assertEquals(providerId, publishedEvent.getPreviousProviderId());
        assertEquals("Test Provider", publishedEvent.getProviderName());
        assertEquals("Not available", publishedEvent.getRejectionReason());
        assertEquals(1, publishedEvent.getAttemptNumber());
        assertEquals(3, publishedEvent.getMaxAttempts());
    }

    @Test
    void handle_RejectedResponse_ExceedsMaxAttempts_SetsUnassignable() {
        // Given - already at 2 attempts, this will be the 3rd (max)
        transportRequest.setAssignmentAttempts(2);
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
        when(transportRequestRepository.findById(transportRequestId)).thenReturn(Optional.of(transportRequest));
        when(transportRequestRepository.save(any(TransportRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        handler.handle(event);

        // Then
        ArgumentCaptor<Assignment> assignmentCaptor = ArgumentCaptor.forClass(Assignment.class);
        verify(assignmentRepository).save(assignmentCaptor.capture());
        Assignment savedAssignment = assignmentCaptor.getValue();
        assertEquals(AssignmentStatus.CANCELLED, savedAssignment.getStatus());

        ArgumentCaptor<TransportRequest> transportRequestCaptor = ArgumentCaptor.forClass(TransportRequest.class);
        verify(transportRequestRepository).save(transportRequestCaptor.capture());
        TransportRequest savedTransportRequest = transportRequestCaptor.getValue();
        assertEquals(TransportRequestStatus.UNASSIGNABLE, savedTransportRequest.getStatus());
        assertEquals(3, savedTransportRequest.getAssignmentAttempts());

        // Verify NO re-matching event is published when max attempts exceeded
        verify(domainEventService, never()).publishToOutbox(any(), any(), any());
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
        when(transportRequestRepository.findById(transportRequestId)).thenReturn(Optional.of(transportRequest));
        when(transportRequestRepository.save(any(TransportRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        handler.handle(event);

        // Then
        ArgumentCaptor<Assignment> assignmentCaptor = ArgumentCaptor.forClass(Assignment.class);
        verify(assignmentRepository).save(assignmentCaptor.capture());
        Assignment savedAssignment = assignmentCaptor.getValue();
        assertEquals(AssignmentStatus.CANCELLED, savedAssignment.getStatus());

        ArgumentCaptor<TransportRequest> transportRequestCaptor = ArgumentCaptor.forClass(TransportRequest.class);
        verify(transportRequestRepository).save(transportRequestCaptor.capture());
        TransportRequest savedTransportRequest = transportRequestCaptor.getValue();
        assertEquals(TransportRequestStatus.REQUESTED, savedTransportRequest.getStatus());
        assertEquals(1, savedTransportRequest.getAssignmentAttempts());

        // Verify re-matching event is published
        ArgumentCaptor<TransportRequestReMatchingTriggeredEvent> eventCaptor = ArgumentCaptor.forClass(TransportRequestReMatchingTriggeredEvent.class);
        verify(domainEventService).publishToOutbox(eventCaptor.capture(), eq("TransportRequest"), eq(transportRequestId.toString()));
        TransportRequestReMatchingTriggeredEvent publishedEvent = eventCaptor.getValue();
        assertEquals(transportRequestId, publishedEvent.getTransportRequestId());
        assertEquals(assignmentId, publishedEvent.getPreviousAssignmentId());
        assertEquals(providerId, publishedEvent.getPreviousProviderId());
        assertEquals("Test Provider", publishedEvent.getProviderName());
        assertNull(publishedEvent.getRejectionReason()); // timeout has no reason
        assertEquals(1, publishedEvent.getAttemptNumber());
        assertEquals(3, publishedEvent.getMaxAttempts());
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