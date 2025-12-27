package com.limport.tms.application.query;

import com.limport.tms.application.cqrs.IQuery;
import com.limport.tms.application.dto.response.AssignmentResponse;

import java.util.List;
import java.util.UUID;

/**
 * Query to list all assignments for a transport request.
 */
public class ListAssignmentsByTransportRequestQuery implements IQuery<List<AssignmentResponse>> {

    private UUID transportRequestId;

    public ListAssignmentsByTransportRequestQuery(UUID transportRequestId) {
        this.transportRequestId = transportRequestId;
    }

    public UUID getTransportRequestId() {
        return transportRequestId;
    }

    public void setTransportRequestId(UUID transportRequestId) {
        this.transportRequestId = transportRequestId;
    }
}
