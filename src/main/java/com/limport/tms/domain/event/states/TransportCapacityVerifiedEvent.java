package com.limport.tms.domain.event.states;

import com.limport.tms.domain.event.TransportEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * Published when capacity checks for a transport request have been completed successfully.
 * Other services can utilize this event to manage their workflows accordingly.
 * 
 * Event Type: TransportEvents.Capacity.Verified
 */
public final class TransportCapacityVerifiedEvent extends TransportEvent {

    private final UUID providerId;
    private final double availableCapacity;
    private final double requiredCapacity;
    private final Instant verifiedAt;
    private final boolean capacitySufficient;

    public TransportCapacityVerifiedEvent(
            UUID transportRequestId,
            String userId,
            UUID providerId,
            double availableCapacity,
            double requiredCapacity) {
        super(transportRequestId, userId);
        this.providerId = providerId;
        this.availableCapacity = availableCapacity;
        this.requiredCapacity = requiredCapacity;
        this.verifiedAt = Instant.now();
        this.capacitySufficient = availableCapacity >= requiredCapacity;
    }

    public UUID getProviderId() {
        return providerId;
    }

    public double getAvailableCapacity() {
        return availableCapacity;
    }

    public double getRequiredCapacity() {
        return requiredCapacity;
    }

    public Instant getVerifiedAt() {
        return verifiedAt;
    }

    public boolean isCapacitySufficient() {
        return capacitySufficient;
    }

    @Override
    public String eventCategory() {
        return "Capacity";
    }

    @Override
    protected String eventName() {
        return "Verified";
    }
}
