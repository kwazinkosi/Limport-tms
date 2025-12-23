package com.limport.tms.infrastructure.repository;

import com.limport.tms.domain.model.entity.TransportRequest;
import com.limport.tms.domain.model.enums.TransportRequestStatus;
import com.limport.tms.domain.ports.ITransportRequestRepository;
import com.limport.tms.infrastructure.persistance.mapper.TransportRequestEntityMapper;
import com.limport.tms.infrastructure.repository.jpa.ITransportRequestJpaRepository;
import com.limport.tms.infrastructure.persistance.entity.TransportRequestJpaEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adapter that implements the domain repository port using JPA.
 * Bridges the gap between domain and infrastructure layers.
 */
@Component
public class TransportRequestRepositoryAdapter implements ITransportRequestRepository {

    private final ITransportRequestJpaRepository jpaRepository;
    private final TransportRequestEntityMapper mapper;

    public TransportRequestRepositoryAdapter(
            ITransportRequestJpaRepository jpaRepository,
            TransportRequestEntityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public TransportRequest save(TransportRequest request) {
        TransportRequestJpaEntity entity = mapper.toJpaEntity(request);
        TransportRequestJpaEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<TransportRequest> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<TransportRequest> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<TransportRequest> findByStatus(TransportRequestStatus status) {
        return jpaRepository.findByStatus(status).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
