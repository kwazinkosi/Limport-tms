package com.limport.tms.application.query;

import com.limport.tms.application.cqrs.IQuery;
import com.limport.tms.application.dto.response.AssignmentResponse;

import java.util.UUID;

/**
 * Query to get the active assignment for a transport request.
 */
public class GetActiveAssignmentQuery implements IQuery<AssignmentResponse> {

    private UUID transportRequestId;

    public GetActiveAssignmentQuery(UUID transportRequestId) {
        this.transportRequestId = transportRequestId;
    }

    public UUID getTransportRequestId() {
        return transportRequestId;
    }

    public void setTransportRequestId(UUID transportRequestId) {
        this.transportRequestId = transportRequestId;
    }
}
