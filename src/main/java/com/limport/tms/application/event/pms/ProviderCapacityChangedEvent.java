package com.limport.tms.application.event.pms;

import com.limport.tms.application.event.ExternalEvent;
import com.limport.tms.domain.event.EventTypes;
import java.time.Instant;
import java.util.UUID;

/**
 * Event published by PMS when provider capacity changes.
 * 
 * Received ASYNCHRONOUSLY via Kafka when:
 * - Provider completes a delivery (capacity increases)
 * - Provider accepts a new assignment (capacity decreases)
 * - Provider updates their fleet/availability
 * 
 * TMS can use this to:
 * - Re-evaluate pending transport requests
 * - Alert if assigned provider no longer has capacity
 */
public record ProviderCapacityChangedEvent(
    UUID eventId,
    Instant occurredOn,
    String sourceService,
    UUID providerId,
    String providerName,
    double previousCapacityKg,
    double newCapacityKg,
    String changeReason
) implements ExternalEvent {
    public String eventType() {
        return EventTypes.Provider.CAPACITY_CHANGED;
    }
    
    /**
     * Check if capacity increased.
     */
    public boolean isCapacityIncreased() {
        return newCapacityKg > previousCapacityKg;
    }
    
    /**
     * Get the capacity change amount.
     */
    public double getCapacityDelta() {
        return newCapacityKg - previousCapacityKg;
    }
}
