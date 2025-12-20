package com.limport.tms.application.service.impl;

import com.limport.tms.application.dto.response.TransportRequestResponse;
import com.limport.tms.application.service.interfaces.ITransportRequestQueryService;
import com.limport.tms.domain.model.enums.TransportRequestStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * Skeleton implementation of the query application service for transport requests.
 */
@Service
public class TransportRequestQueryServiceImpl implements ITransportRequestQueryService {

    @Override
    public TransportRequestResponse getById(UUID id) {
        throw new UnsupportedOperationException("getById not implemented yet");
    }

    @Override
    public List<TransportRequestResponse> listAll() {
        throw new UnsupportedOperationException("listAll not implemented yet");
    }

    @Override
    public List<TransportRequestResponse> listByStatus(TransportRequestStatus status) {
        throw new UnsupportedOperationException("listByStatus not implemented yet");
    }
}
