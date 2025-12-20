package com.limport.tms.application.service.interfaces;

import com.limport.tms.application.dto.request.AssignProviderRequest;
import com.limport.tms.application.dto.request.CancelTransportRequestRequest;
import com.limport.tms.application.dto.request.CreateTransportRequest;
import com.limport.tms.application.dto.response.TransportRequestResponse;
import java.util.UUID;

/**
 * Application service for command-style operations on transport requests.
 */
public interface ITransportRequestCommandService {

    TransportRequestResponse createTransportRequest(CreateTransportRequest request);

    TransportRequestResponse assignProvider(UUID id, AssignProviderRequest request);

    TransportRequestResponse cancelTransportRequest(UUID id, CancelTransportRequestRequest request);

    TransportRequestResponse completeTransportRequest(UUID id);
}
