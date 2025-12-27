package com.limport.tms.domain.event.states;

import com.limport.tms.domain.model.enums.TransportRequestStatus;

import java.util.Map;
import java.util.UUID;

/**
 * Published when there are updates to an existing transport request,
 * such as changes in status or item details.
 * Other services can use this information to react appropriately to the request's current state.
 * 
 * Event Type: TMS.Transport.Request.Updated
 */
public final class TransportRequestUpdatedEvent extends TransportStatusChangedEvent {

    private final Map<String, Object> updatedFields;
    private final String updateDescription;

    public TransportRequestUpdatedEvent(
            UUID transportRequestId,
            String userId,
            TransportRequestStatus previousStatus,
            TransportRequestStatus newStatus,
            Map<String, Object> updatedFields,
            String updateDescription) {
        super(transportRequestId, userId, previousStatus, newStatus, "Transport request updated");
        this.updatedFields = updatedFields != null ? Map.copyOf(updatedFields) : Map.of();
        this.updateDescription = updateDescription;
    }

    public Map<String, Object> getUpdatedFields() {
        return updatedFields;
    }

    public String getUpdateDescription() {
        return updateDescription;
    }

    @Override
    protected String eventName() {
        return "Updated";
    }
}
