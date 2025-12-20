package com.limport.tms.domain.event.states;

import com.limport.tms.domain.model.enums.TransportRequestStatus;

import java.util.UUID;

/**
 * Published when a transport request is assigned to a provider/vehicle.
 * This event indicates that planning is complete and the request is ready for execution.
 * 
 * Event Type: TransportEvents.Request.Assigned
 */
public final class TransportRequestAssignedEvent extends TransportStatusChangedEvent {

    private final UUID providerId;
    private final UUID vehicleId;
    private final String assignmentDetails;

    public TransportRequestAssignedEvent(
            UUID transportRequestId,
            String userId,
            TransportRequestStatus previousStatus,
            UUID providerId,
            UUID vehicleId,
            String assignmentDetails) {
        super(transportRequestId, userId, previousStatus, TransportRequestStatus.PLANNED, "Assigned to provider");
        this.providerId = providerId;
        this.vehicleId = vehicleId;
        this.assignmentDetails = assignmentDetails;
    }

    public UUID getProviderId() {
        return providerId;
    }

    public UUID getVehicleId() {
        return vehicleId;
    }

    public String getAssignmentDetails() {
        return assignmentDetails;
    }

    @Override
    protected String eventName() {
        return "Assigned";
    }
}
