package com.limport.tms.application.query;

import java.util.UUID;

/**
 * Query to load a single transport request by id.
 */
public class GetTransportRequestQuery {

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
