package com.limport.tms.application.query.handler;

import com.limport.tms.application.cqrs.IQueryHandler;
import com.limport.tms.application.dto.response.AssignmentResponse;
import com.limport.tms.application.query.ListAssignmentsByProviderQuery;
import com.limport.tms.application.service.interfaces.IAssignmentQueryService;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Handler for ListAssignmentsByProviderQuery.
 * Lists assignments by provider.
 */
@Component
public class ListAssignmentsByProviderQueryHandler
        implements IQueryHandler<ListAssignmentsByProviderQuery, List<AssignmentResponse>> {

    private final IAssignmentQueryService assignmentQueryService;

    public ListAssignmentsByProviderQueryHandler(IAssignmentQueryService assignmentQueryService) {
        this.assignmentQueryService = assignmentQueryService;
    }

    @Override
    public List<AssignmentResponse> handle(ListAssignmentsByProviderQuery query) {
        return assignmentQueryService.listByProvider(query.getProviderId());
    }

    @Override
    public Class<ListAssignmentsByProviderQuery> getQueryType() {
        return ListAssignmentsByProviderQuery.class;
    }
}
