package com.limport.tms.application.query;

import com.limport.tms.application.cqrs.IQuery;
import com.limport.tms.application.dto.response.TransportRequestResponse;

import java.util.UUID;

/**
 * Query to load a single transport request by id.
 */
public class GetTransportRequestQuery implements IQuery<TransportRequestResponse> {

    private UUID id;

    public GetTransportRequestQuery(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}
