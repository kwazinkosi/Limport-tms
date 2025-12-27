package com.limport.tms.infrastructure.repository.jpa;

import com.limport.tms.domain.model.entity.Assignment.AssignmentStatus;
import com.limport.tms.infrastructure.persistence.entity.AssignmentJpaEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for Assignment persistence.
 */
@Repository
public interface IAssignmentJpaRepository extends JpaRepository<AssignmentJpaEntity, UUID> {

    /**
     * Find all assignments for a specific transport request.
     */
    List<AssignmentJpaEntity> findByTransportRequestId(UUID transportRequestId);

    /**
     * Find assignments by provider ID.
     */
    List<AssignmentJpaEntity> findByProviderId(UUID providerId);

    /**
     * Find assignments by status.
     */
    List<AssignmentJpaEntity> findByStatus(AssignmentStatus status);

    /**
     * Find active (non-cancelled, non-completed) assignments for a transport request.
     */
    List<AssignmentJpaEntity> findByTransportRequestIdAndStatusIn(
        UUID transportRequestId, 
        List<AssignmentStatus> statuses
    );

    /**
     * Find active assignments for a provider.
     */
    List<AssignmentJpaEntity> findByProviderIdAndStatusIn(
        UUID providerId,
        List<AssignmentStatus> statuses
    );
}
