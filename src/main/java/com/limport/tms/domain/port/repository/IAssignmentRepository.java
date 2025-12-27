package com.limport.tms.domain.port.repository;

import com.limport.tms.domain.model.entity.Assignment;
import com.limport.tms.domain.model.entity.Assignment.AssignmentStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository port for Assignment persistence.
 * Part of the domain layer - defines the contract for infrastructure implementations.
 */
public interface IAssignmentRepository {

    /**
     * Save or update an assignment.
     */
    Assignment save(Assignment assignment);

    /**
     * Find assignment by ID.
     */
    Optional<Assignment> findById(UUID id);

    /**
     * Find all assignments for a transport request.
     */
    List<Assignment> findByTransportRequestId(UUID transportRequestId);

    /**
     * Find assignments by provider.
     */
    List<Assignment> findByProviderId(UUID providerId);

    /**
     * Find assignments by status.
     */
    List<Assignment> findByStatus(AssignmentStatus status);

    /**
     * Find active assignments for a transport request.
     */
    List<Assignment> findActiveByTransportRequestId(UUID transportRequestId);

    /**
     * Find active assignments for a provider.
     */
    List<Assignment> findActiveByProviderId(UUID providerId);

    /**
     * Delete an assignment.
     */
    void delete(UUID id);
}
