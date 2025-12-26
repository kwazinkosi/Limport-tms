package com.limport.tms.application.event.pms;

import com.limport.tms.application.event.ExternalEvent;
import com.limport.tms.domain.event.EventTypes;

import java.time.Instant;
import java.util.UUID;

/**
 * Event published by PMS when a provider is matched to a transport request.
 * 
 * Received ASYNCHRONOUSLY via Kafka after TMS publishes TransportEvents.Request.Created.
 * 
 * Flow:
 * 1. TMS publishes TransportEvents.Request.Created → Kafka
 * 2. PMS consumes event, runs matching algorithm
 * 3. PMS publishes ProviderEvents.Matched → Kafka
 * 4. TMS consumes this event, stores suggestions
 */
public record ProviderMatchedEvent(
    UUID eventId,
    Instant occurredOn,
    String sourceService,
    UUID transportRequestId,
    UUID providerId,
    String providerName,
    UUID vehicleId,
    String vehicleType,
    double matchScore,
    double estimatedCostZAR,
    double availableCapacityKg
) implements ExternalEvent {
    public String eventType() {
        return EventTypes.Provider.MATCHED;
    }
}
