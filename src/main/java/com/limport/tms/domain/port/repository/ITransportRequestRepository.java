package com.limport.tms.domain.port.repository;

import com.limport.tms.domain.model.entity.TransportRequest;
import com.limport.tms.domain.model.enums.TransportRequestStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository port for TransportRequest persistence.
 * Part of the domain layer - defines the contract for infrastructure implementations.
 */
public interface ITransportRequestRepository {

    TransportRequest save(TransportRequest request);

    Optional<TransportRequest> findById(UUID id);

    List<TransportRequest> findAll();

    List<TransportRequest> findByStatus(TransportRequestStatus status);

    /**
     * Find transport requests by status and weight less than or equal to the given weight.
     */
    List<TransportRequest> findByStatusAndWeightLessThanEqual(TransportRequestStatus status, BigDecimal maxWeight);
}
