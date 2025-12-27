package com.limport.tms.application.query;

import com.limport.tms.application.cqrs.IQuery;
import com.limport.tms.application.dto.response.TransportRequestResponse;
import com.limport.tms.domain.model.enums.TransportRequestStatus;

import java.util.List;

/**
 * Query to list transport requests, optionally filtered by status.
 */
public class ListTransportRequestsQuery implements IQuery<List<TransportRequestResponse>> {

    private TransportRequestStatus status;

    public ListTransportRequestsQuery() {
    }

    public ListTransportRequestsQuery(TransportRequestStatus status) {
        this.status = status;
    }

    public TransportRequestStatus getStatus() {
        return status;
    }

    public void setStatus(TransportRequestStatus status) {
        this.status = status;
    }
}
