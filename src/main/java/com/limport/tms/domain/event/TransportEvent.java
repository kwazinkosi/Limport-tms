package com.limport.tms.domain.event;

import java.util.UUID;

/**
 * Base class for all transport-related domain events in TMS.
 * Extends BaseEvent and adds transport-specific context.
 */
public abstract class TransportEvent extends BaseEvent {
    
    private final UUID transportRequestId;
    private final String userId;

    protected TransportEvent(UUID transportRequestId, String userId) {
        super();
        this.transportRequestId = transportRequestId;
        this.userId = userId;
    }

    public UUID getTransportRequestId() {
        return transportRequestId;
    }

    public String getUserId() {
        return userId;
    }

    /**
     * Returns the event category (e.g., "Request", "Capacity", "Route")
     */
    public abstract String eventCategory();

    @Override
    public String eventType() {
        return "TransportEvents." + eventCategory() + "." + eventName();
    }

    /**
     * Returns the specific event name (e.g., "Created", "Updated", "Cancelled")
     */
    protected abstract String eventName();
}