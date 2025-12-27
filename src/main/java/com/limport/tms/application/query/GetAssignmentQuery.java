package com.limport.tms.application.query;

import com.limport.tms.application.cqrs.IQuery;
import com.limport.tms.application.dto.response.AssignmentResponse;

import java.util.UUID;

/**
 * Query to get a single assignment by ID.
 */
public class GetAssignmentQuery implements IQuery<AssignmentResponse> {

    private UUID id;

    public GetAssignmentQuery(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}
