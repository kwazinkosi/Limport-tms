package com.limport.tms.application.query.handler;

import com.limport.tms.application.cqrs.IQueryHandler;
import com.limport.tms.application.dto.response.AssignmentResponse;
import com.limport.tms.application.query.GetActiveAssignmentQuery;
import com.limport.tms.application.service.interfaces.IAssignmentQueryService;
import org.springframework.stereotype.Component;

/**
 * Handler for GetActiveAssignmentQuery.
 * Retrieves the active assignment for a transport request.
 */
@Component
public class GetActiveAssignmentQueryHandler
        implements IQueryHandler<GetActiveAssignmentQuery, AssignmentResponse> {

    private final IAssignmentQueryService assignmentQueryService;

    public GetActiveAssignmentQueryHandler(IAssignmentQueryService assignmentQueryService) {
        this.assignmentQueryService = assignmentQueryService;
    }

    @Override
    public AssignmentResponse handle(GetActiveAssignmentQuery query) {
        return assignmentQueryService.getActiveAssignment(query.getTransportRequestId());
    }

    @Override
    public Class<GetActiveAssignmentQuery> getQueryType() {
        return GetActiveAssignmentQuery.class;
    }
}
