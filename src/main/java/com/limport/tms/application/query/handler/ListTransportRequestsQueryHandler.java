package com.limport.tms.application.query.handler;

import com.limport.tms.application.cqrs.IQueryHandler;
import com.limport.tms.application.dto.response.TransportRequestResponse;
import com.limport.tms.application.query.ListTransportRequestsQuery;
import com.limport.tms.application.service.interfaces.ITransportRequestQueryService;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Handler for ListTransportRequestsQuery.
 * Lists transport requests, optionally filtered by status.
 */
@Component
public class ListTransportRequestsQueryHandler 
        implements IQueryHandler<ListTransportRequestsQuery, List<TransportRequestResponse>> {

    private final ITransportRequestQueryService queryService;

    public ListTransportRequestsQueryHandler(ITransportRequestQueryService queryService) {
        this.queryService = queryService;
    }

    @Override
    public List<TransportRequestResponse> handle(ListTransportRequestsQuery query) {
        if (query.getStatus() != null) {
            return queryService.listByStatus(query.getStatus());
        }
        return queryService.listAll();
    }

    @Override
    public Class<ListTransportRequestsQuery> getQueryType() {
        return ListTransportRequestsQuery.class;
    }
}
