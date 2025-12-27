package com.limport.tms.application.query.handler;

import com.limport.tms.application.cqrs.IQueryHandler;
import com.limport.tms.application.dto.response.TransportRequestResponse;
import com.limport.tms.application.query.GetTransportRequestQuery;
import com.limport.tms.application.service.interfaces.ITransportRequestQueryService;
import org.springframework.stereotype.Component;

/**
 * Handler for GetTransportRequestQuery.
 * Retrieves a single transport request by ID.
 */
@Component
public class GetTransportRequestQueryHandler 
        implements IQueryHandler<GetTransportRequestQuery, TransportRequestResponse> {

    private final ITransportRequestQueryService queryService;

    public GetTransportRequestQueryHandler(ITransportRequestQueryService queryService) {
        this.queryService = queryService;
    }

    @Override
    public TransportRequestResponse handle(GetTransportRequestQuery query) {
        return queryService.getById(query.getId());
    }

    @Override
    public Class<GetTransportRequestQuery> getQueryType() {
        return GetTransportRequestQuery.class;
    }
}
