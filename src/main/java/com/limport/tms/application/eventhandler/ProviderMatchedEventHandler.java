package com.limport.tms.application.eventhandler;

import com.limport.tms.application.event.IExternalEventHandler;
import com.limport.tms.application.event.pms.ProviderMatchedEvent;
import com.limport.tms.domain.event.EventTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Handles ProviderMatchedEvent from PMS.
 * 
 * When PMS finds matching providers for a transport request,
 * TMS stores these suggestions for user selection.
 */
@Component
public class ProviderMatchedEventHandler implements IExternalEventHandler<ProviderMatchedEvent> {
    
    private static final Logger log = LoggerFactory.getLogger(ProviderMatchedEventHandler.class);
    
    // TODO: Inject repository to store provider suggestions
    // private final IProviderSuggestionRepository suggestionRepository;
    
    @Override
    public void handle(ProviderMatchedEvent event) {
        log.info("Handling ProviderMatchedEvent for transportRequestId={}, providerId={}, matchScore={}",
            event.transportRequestId(),
            event.providerId(),
            event.matchScore());
        
        // Business Logic:
        // 1. Validate transport request exists and is in valid state
        // 2. Store provider suggestion with match score
        // 3. Optionally notify user of available provider
        
        // TODO: Implement when ProviderSuggestion entity is created
        // ProviderSuggestion suggestion = ProviderSuggestion.builder()
        //     .transportRequestId(event.transportRequestId())
        //     .providerId(event.providerId())
        //     .providerName(event.providerName())
        //     .vehicleId(event.vehicleId())
        //     .vehicleType(event.vehicleType())
        //     .matchScore(event.matchScore())
        //     .estimatedCostZAR(event.estimatedCostZAR())
        //     .availableCapacityKg(event.availableCapacityKg())
        //     .suggestedAt(event.occurredOn())
        //     .build();
        // 
        // suggestionRepository.save(suggestion);
        
        log.debug("Successfully processed ProviderMatchedEvent eventId={}", event.eventId());
    }
    
    @Override
    public String getSupportedEventType() {
        return EventTypes.Provider.MATCHED;
    }
    
    @Override
    public Class<ProviderMatchedEvent> getEventClass() {
        return ProviderMatchedEvent.class;
    }
}
