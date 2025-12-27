package com.limport.tms.application.eventhandler;

import com.limport.tms.application.event.IExternalEventHandler;
import com.limport.tms.application.event.pms.ProviderMatchedEvent;
import com.limport.tms.domain.event.EventTypes.Provider;
import com.limport.tms.domain.model.entity.ProviderSuggestion;
import com.limport.tms.domain.model.entity.TransportRequest;
import com.limport.tms.domain.model.enums.TransportRequestStatus;
import com.limport.tms.domain.port.repository.IProviderSuggestionRepository;
import com.limport.tms.domain.port.repository.ITransportRequestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Handles ProviderMatchedEvent from PMS.
 * 
 * When PMS finds matching providers for a transport request,
 * TMS stores these suggestions for user selection.
 * 
 * Design: Event-driven, idempotent, eventual consistency.
 */
@Component
public class ProviderMatchedEventHandler implements IExternalEventHandler<ProviderMatchedEvent> {
    
    private static final Logger log = LoggerFactory.getLogger(ProviderMatchedEventHandler.class);
    
    private final IProviderSuggestionRepository suggestionRepository;
    private final ITransportRequestRepository transportRequestRepository;
    
    public ProviderMatchedEventHandler(
            IProviderSuggestionRepository suggestionRepository,
            ITransportRequestRepository transportRequestRepository) {
        this.suggestionRepository = suggestionRepository;
        this.transportRequestRepository = transportRequestRepository;
    }
    
    @Override
    @Transactional
    public void handle(ProviderMatchedEvent event) {
        log.info("Handling ProviderMatchedEvent: transportRequestId={}, providerId={}, matchScore={}",
            event.transportRequestId(), event.providerId(), event.matchScore());
        
        // 1. Validate transport request exists and is in valid state
        Optional<TransportRequest> transportRequest = transportRequestRepository.findById(event.transportRequestId());
        if (transportRequest.isEmpty()) {
            log.warn("Transport request {} not found, skipping provider suggestion", event.transportRequestId());
            return;
        }
        
        if (!isValidStateForSuggestions(transportRequest.get().getStatus())) {
            log.info("Transport request {} in status {}, not accepting suggestions", 
                event.transportRequestId(), transportRequest.get().getStatus());
            return;
        }
        
        // 2. Check for duplicates (idempotency)
        if (suggestionRepository.existsByTransportRequestIdAndProviderId(
                event.transportRequestId(), event.providerId())) {
            log.debug("Provider suggestion already exists for transportRequest={} and provider={}, skipping", 
                event.transportRequestId(), event.providerId());
            return;
        }
        
        // 3. Create and store provider suggestion
        ProviderSuggestion suggestion = ProviderSuggestion.builder()
            .transportRequestId(event.transportRequestId())
            .providerId(event.providerId())
            .providerName(event.providerName())
            .vehicleId(event.vehicleId())
            .vehicleType(event.vehicleType())
            .matchScore(event.matchScore())
            .estimatedCostZAR(event.estimatedCostZAR())
            .availableCapacityKg(event.availableCapacityKg())
            .suggestedAt(event.occurredOn())
            .status(ProviderSuggestion.SuggestionStatus.ACTIVE)
            .build();
        
        suggestionRepository.save(suggestion);
        
        log.info("Stored provider suggestion: transportRequest={}, provider={}, score={}", 
            event.transportRequestId(), event.providerId(), event.matchScore());
    }
    
    /**
     * Determines if the transport request status allows new provider suggestions.
     * Business rule: Only accept suggestions for requests that haven't been assigned yet.
     */
    private boolean isValidStateForSuggestions(TransportRequestStatus status) {
        return status == TransportRequestStatus.REQUESTED || status == TransportRequestStatus.PLANNED;
    }
    
    @Override
    public String getSupportedEventType() {
        return Provider.MATCHED;
    }
    
    @Override
    public Class<ProviderMatchedEvent> getEventClass() {
        return ProviderMatchedEvent.class;
    }
}
