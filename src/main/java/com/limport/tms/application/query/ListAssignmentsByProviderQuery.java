package com.limport.tms.application.query;

import com.limport.tms.application.cqrs.IQuery;
import com.limport.tms.application.dto.response.AssignmentResponse;

import java.util.List;
import java.util.UUID;

/**
 * Query to list assignments by provider.
 */
public class ListAssignmentsByProviderQuery implements IQuery<List<AssignmentResponse>> {

    private UUID providerId;

    public ListAssignmentsByProviderQuery(UUID providerId) {
        this.providerId = providerId;
    }

    public UUID getProviderId() {
        return providerId;
    }

    public void setProviderId(UUID providerId) {
        this.providerId = providerId;
    }
}
