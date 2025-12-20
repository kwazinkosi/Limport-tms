package com.limport.tms.domain.ports;

import com.limport.tms.domain.model.entity.TransportRequest;
import com.limport.tms.domain.model.enums.TransportRequestStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port for persisting and querying transport requests.
 */
public interface ITransportRequestRepository {

    TransportRequest save(TransportRequest request);

    Optional<TransportRequest> findById(UUID id);

    List<TransportRequest> findAll();

    List<TransportRequest> findByStatus(TransportRequestStatus status);
}
