package com.limport.tms.application.query;

import com.limport.tms.domain.model.enums.TransportRequestStatus;

/**
 * Query to list transport requests, optionally filtered by status.
 */
public class ListTransportRequestsQuery {

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
