package com.limport.tms.application.service.interfaces;

import com.limport.tms.application.dto.response.TransportRequestResponse;
import com.limport.tms.domain.model.enums.TransportRequestStatus;
import java.util.List;
import java.util.UUID;

/**
 * Application service for query-style operations on transport requests.
 */
public interface ITransportRequestQueryService {

    TransportRequestResponse getById(UUID id);

    List<TransportRequestResponse> listAll();

    List<TransportRequestResponse> listByStatus(TransportRequestStatus status);
}
