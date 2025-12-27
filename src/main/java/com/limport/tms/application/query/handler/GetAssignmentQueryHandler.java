package com.limport.tms.application.query.handler;

import com.limport.tms.application.cqrs.IQueryHandler;
import com.limport.tms.application.dto.response.AssignmentResponse;
import com.limport.tms.application.query.GetAssignmentQuery;
import com.limport.tms.application.service.interfaces.IAssignmentQueryService;
import org.springframework.stereotype.Component;

/**
 * Handler for GetAssignmentQuery.
 * Retrieves a single assignment by ID.
 */
@Component
public class GetAssignmentQueryHandler
        implements IQueryHandler<GetAssignmentQuery, AssignmentResponse> {

    private final IAssignmentQueryService assignmentQueryService;

    public GetAssignmentQueryHandler(IAssignmentQueryService assignmentQueryService) {
        this.assignmentQueryService = assignmentQueryService;
    }

    @Override
    public AssignmentResponse handle(GetAssignmentQuery query) {
        return assignmentQueryService.getById(query.getId());
    }

    @Override
    public Class<GetAssignmentQuery> getQueryType() {
        return GetAssignmentQuery.class;
    }
}
