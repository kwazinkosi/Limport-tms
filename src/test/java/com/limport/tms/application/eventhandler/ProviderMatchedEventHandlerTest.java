package com.limport.tms.application.eventhandler;

import com.limport.tms.application.event.pms.ProviderMatchedEvent;
import com.limport.tms.domain.model.entity.ProviderSuggestion;
import com.limport.tms.domain.model.entity.TransportRequest;
import com.limport.tms.domain.model.enums.TransportRequestStatus;
import com.limport.tms.domain.ports.IProviderSuggestionRepository;
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
class ProviderMatchedEventHandlerTest {

    @Mock
    private IProviderSuggestionRepository suggestionRepository;

    @Mock
    private ITransportRequestRepository transportRequestRepository;

    @InjectMocks
    private ProviderMatchedEventHandler handler;

    private UUID transportRequestId;
    private UUID providerId;
    private UUID vehicleId;
    private ProviderMatchedEvent event;
    private TransportRequest transportRequest;

    @BeforeEach
    void setUp() {
        transportRequestId = UUID.randomUUID();
        providerId = UUID.randomUUID();
        vehicleId = UUID.randomUUID();

        event = new ProviderMatchedEvent(
            UUID.randomUUID(),
            Instant.now(),
            "PMS",
            transportRequestId,
            providerId,
            "Test Provider",
            vehicleId,
            "Truck",
            0.95,
            1500.0,
            500.0
        );

        transportRequest = new TransportRequest();
        transportRequest.setId(transportRequestId);
        transportRequest.setStatus(TransportRequestStatus.REQUESTED);
    }

    @Test
    void handle_ValidEvent_SavesSuggestion() {
        // Given
        when(transportRequestRepository.findById(transportRequestId)).thenReturn(Optional.of(transportRequest));
        when(suggestionRepository.existsByTransportRequestIdAndProviderId(transportRequestId, providerId)).thenReturn(false);
        when(suggestionRepository.save(any(ProviderSuggestion.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        handler.handle(event);

        // Then
        ArgumentCaptor<ProviderSuggestion> captor = ArgumentCaptor.forClass(ProviderSuggestion.class);
        verify(suggestionRepository).save(captor.capture());

        ProviderSuggestion saved = captor.getValue();
        assertEquals(transportRequestId, saved.getTransportRequestId());
        assertEquals(providerId, saved.getProviderId());
        assertEquals("Test Provider", saved.getProviderName());
        assertEquals(vehicleId, saved.getVehicleId());
        assertEquals("Truck", saved.getVehicleType());
        assertEquals(0.95, saved.getMatchScore());
        assertEquals(1500.0, saved.getEstimatedCostZAR());
        assertEquals(500.0, saved.getAvailableCapacityKg());
        assertEquals(ProviderSuggestion.SuggestionStatus.ACTIVE, saved.getStatus());
    }

    @Test
    void handle_TransportRequestNotFound_SkipsProcessing() {
        // Given
        when(transportRequestRepository.findById(transportRequestId)).thenReturn(Optional.empty());

        // When
        handler.handle(event);

        // Then
        verify(suggestionRepository, never()).save(any());
        verify(suggestionRepository, never()).existsByTransportRequestIdAndProviderId(any(), any());
    }

    @Test
    void handle_InvalidStatus_SkipsProcessing() {
        // Given
        transportRequest.setStatus(TransportRequestStatus.COMPLETED);
        when(transportRequestRepository.findById(transportRequestId)).thenReturn(Optional.of(transportRequest));

        // When
        handler.handle(event);

        // Then
        verify(suggestionRepository, never()).save(any());
        verify(suggestionRepository, never()).existsByTransportRequestIdAndProviderId(any(), any());
    }

    @Test
    void handle_DuplicateSuggestion_SkipsProcessing() {
        // Given
        when(transportRequestRepository.findById(transportRequestId)).thenReturn(Optional.of(transportRequest));
        when(suggestionRepository.existsByTransportRequestIdAndProviderId(transportRequestId, providerId)).thenReturn(true);

        // When
        handler.handle(event);

        // Then
        verify(suggestionRepository, never()).save(any());
    }

    @Test
    void handle_PlannedStatus_AllowsSuggestion() {
        // Given
        transportRequest.setStatus(TransportRequestStatus.PLANNED);
        when(transportRequestRepository.findById(transportRequestId)).thenReturn(Optional.of(transportRequest));
        when(suggestionRepository.existsByTransportRequestIdAndProviderId(transportRequestId, providerId)).thenReturn(false);
        when(suggestionRepository.save(any(ProviderSuggestion.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        handler.handle(event);

        // Then
        verify(suggestionRepository).save(any(ProviderSuggestion.class));
    }

    @Test
    void getSupportedEventType_ReturnsCorrectType() {
        // When
        String eventType = handler.getSupportedEventType();

        // Then
        assertEquals("PMS.Provider.Matched", eventType);
    }
}