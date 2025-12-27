package com.limport.tms.domain.event.states;

import com.limport.tms.domain.model.enums.TransportRequestStatus;

import java.util.Map;
import java.util.UUID;

/**
 * Published when a new transport request is created.
 * This event includes all relevant details of the request and is consumed by
 * services involved in provider matching and route optimization.
 * 
 * Event Type: TMS.Transport.Request.Created
 */
public final class TransportRequestCreatedEvent extends TransportStatusChangedEvent {

    private final String origin;
    private final String destination;
    private final Map<String, Object> requestDetails;

    public TransportRequestCreatedEvent(
            UUID transportRequestId,
            String userId,
            String origin,
            String destination,
            Map<String, Object> requestDetails) {
        super(transportRequestId, userId, null, TransportRequestStatus.REQUESTED, "Transport request created");
        this.origin = origin;
        this.destination = destination;
        this.requestDetails = requestDetails != null ? Map.copyOf(requestDetails) : Map.of();
    }

    public String getOrigin() {
        return origin;
    }

    public String getDestination() {
        return destination;
    }

    public Map<String, Object> getRequestDetails() {
        return requestDetails;
    }

    @Override
    protected String eventName() {
        return "Created";
    }
}
