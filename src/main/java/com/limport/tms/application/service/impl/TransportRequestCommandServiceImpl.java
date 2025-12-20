package com.limport.tms.application.service.impl;

import com.limport.tms.application.dto.request.AssignProviderRequest;
import com.limport.tms.application.dto.request.CancelTransportRequestRequest;
import com.limport.tms.application.dto.request.CreateTransportRequest;
import com.limport.tms.application.dto.response.TransportRequestResponse;
import com.limport.tms.application.service.interfaces.ITransportRequestCommandService;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * Skeleton implementation of the command application service for transport requests.
 *
 * Methods currently throw UnsupportedOperationException and should be wired to
 * domain services and repositories as the implementation matures.
 */
@Service
public class TransportRequestCommandServiceImpl implements ITransportRequestCommandService {

    @Override
    public TransportRequestResponse createTransportRequest(CreateTransportRequest request) {
        throw new UnsupportedOperationException("createTransportRequest not implemented yet");
    }

    @Override
    public TransportRequestResponse assignProvider(UUID id, AssignProviderRequest request) {
        throw new UnsupportedOperationException("assignProvider not implemented yet");
    }

    @Override
    public TransportRequestResponse cancelTransportRequest(UUID id, CancelTransportRequestRequest request) {
        throw new UnsupportedOperationException("cancelTransportRequest not implemented yet");
    }

    @Override
    public TransportRequestResponse completeTransportRequest(UUID id) {
        throw new UnsupportedOperationException("completeTransportRequest not implemented yet");
    }
}
