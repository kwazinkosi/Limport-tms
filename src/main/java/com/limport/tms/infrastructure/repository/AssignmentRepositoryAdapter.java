package com.limport.tms.infrastructure.repository;

import com.limport.tms.domain.model.entity.Assignment;
import com.limport.tms.domain.model.entity.Assignment.AssignmentStatus;
import com.limport.tms.domain.port.repository.IAssignmentRepository;
import com.limport.tms.infrastructure.persistence.entity.AssignmentJpaEntity;
import com.limport.tms.infrastructure.persistence.mapper.AssignmentEntityMapper;
import com.limport.tms.infrastructure.repository.jpa.IAssignmentJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adapter implementing the assignment repository port using JPA.
 */
@Component
public class AssignmentRepositoryAdapter implements IAssignmentRepository {

    private final IAssignmentJpaRepository jpaRepository;
    private final AssignmentEntityMapper mapper;

    public AssignmentRepositoryAdapter(
            IAssignmentJpaRepository jpaRepository,
            AssignmentEntityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Assignment save(Assignment assignment) {
        AssignmentJpaEntity entity = mapper.toJpaEntity(assignment);
        AssignmentJpaEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Assignment> findById(UUID id) {
        return jpaRepository.findById(id)
            .map(mapper::toDomain);
    }

    @Override
    public List<Assignment> findByTransportRequestId(UUID transportRequestId) {
        return jpaRepository.findByTransportRequestId(transportRequestId)
            .stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<Assignment> findByProviderId(UUID providerId) {
        return jpaRepository.findByProviderId(providerId)
            .stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<Assignment> findByStatus(AssignmentStatus status) {
        return jpaRepository.findByStatus(status)
            .stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<Assignment> findActiveByTransportRequestId(UUID transportRequestId) {
        List<AssignmentStatus> activeStatuses = List.of(
            AssignmentStatus.ASSIGNED,
            AssignmentStatus.CONFIRMED,
            AssignmentStatus.IN_PROGRESS
        );
        return jpaRepository.findByTransportRequestIdAndStatusIn(transportRequestId, activeStatuses)
            .stream()
            .map(AssignmentJpaEntity::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<Assignment> findActiveByProviderId(UUID providerId) {
        List<AssignmentStatus> activeStatuses = List.of(
            AssignmentStatus.ASSIGNED,
            AssignmentStatus.CONFIRMED,
            AssignmentStatus.IN_PROGRESS
        );
        return jpaRepository.findByProviderIdAndStatusIn(providerId, activeStatuses)
            .stream()
            .map(AssignmentJpaEntity::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public void delete(UUID id) {
        jpaRepository.deleteById(id);
    }
}
