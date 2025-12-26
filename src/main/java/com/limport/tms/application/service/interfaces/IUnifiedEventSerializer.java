package com.limport.tms.application.service.interfaces;

import com.limport.tms.domain.event.IDomainEvent;
import com.limport.tms.application.event.ExternalEvent;

import java.util.Optional;

/**
 * Unified interface for event serialization/deserialization.
 *
 * Supports both domain events (internal) and external events.
 * Provides consistent ObjectMapper usage across the application.
 */
public interface IUnifiedEventSerializer extends IEventSerializer {

    // Domain event serialization (existing functionality)
    String serialize(IDomainEvent event);
    IDomainEvent deserializeDomainEvent(String payload, String eventType);

    // External event deserialization (existing functionality)
    Optional<ExternalEvent> deserializeExternalEvent(String jsonPayload);

    // Utility methods
    boolean canDeserializeExternalEvent(String eventType);
}