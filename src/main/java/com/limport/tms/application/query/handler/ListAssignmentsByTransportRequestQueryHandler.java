package com.limport.tms.application.query.handler;

import com.limport.tms.application.cqrs.IQueryHandler;
import com.limport.tms.application.dto.response.AssignmentResponse;
import com.limport.tms.application.query.ListAssignmentsByTransportRequestQuery;
import com.limport.tms.application.service.interfaces.IAssignmentQueryService;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Handler for ListAssignmentsByTransportRequestQuery.
 * Lists all assignments for a transport request.
 */
@Component
public class ListAssignmentsByTransportRequestQueryHandler
        implements IQueryHandler<ListAssignmentsByTransportRequestQuery, List<AssignmentResponse>> {

    private final IAssignmentQueryService assignmentQueryService;

    public ListAssignmentsByTransportRequestQueryHandler(IAssignmentQueryService assignmentQueryService) {
        this.assignmentQueryService = assignmentQueryService;
    }

    @Override
    public List<AssignmentResponse> handle(ListAssignmentsByTransportRequestQuery query) {
        return assignmentQueryService.listByTransportRequest(query.getTransportRequestId());
    }

    @Override
    public Class<ListAssignmentsByTransportRequestQuery> getQueryType() {
        return ListAssignmentsByTransportRequestQuery.class;
    }
}
