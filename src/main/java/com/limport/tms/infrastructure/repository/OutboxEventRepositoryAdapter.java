package com.limport.tms.infrastructure.repository;

import com.limport.tms.domain.model.entity.OutboxEvent;
import com.limport.tms.domain.ports.IOutboxEventRepository;
import com.limport.tms.infrastructure.repository.jpa.JpaOutboxEventRepository;
import com.limport.tms.infrastructure.repository.jpa.OutboxEventJpaEntity;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adapter implementing the outbox repository port.
 * Bridges domain repository interface with Spring Data JPA.
 */
@Repository
public class OutboxEventRepositoryAdapter implements IOutboxEventRepository {
    
    private final JpaOutboxEventRepository jpaRepository;
    
    public OutboxEventRepositoryAdapter(JpaOutboxEventRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }
    
    @Override
    @Transactional
    public void save(OutboxEvent event) {
        jpaRepository.save(OutboxEventJpaEntity.fromDomain(event));
    }
    
    @Override
    @Transactional
    public void saveAll(List<OutboxEvent> events) {
        List<OutboxEventJpaEntity> entities = events.stream()
            .map(OutboxEventJpaEntity::fromDomain)
            .collect(Collectors.toList());
        jpaRepository.saveAll(entities);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<OutboxEvent> findById(UUID id) {
        return jpaRepository.findById(id)
            .map(OutboxEventJpaEntity::toDomain);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<OutboxEvent> findPendingEvents(int limit) {
        return jpaRepository.findPendingEvents(limit).stream()
            .map(OutboxEventJpaEntity::toDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public void update(OutboxEvent event) {
        jpaRepository.findById(event.getId())
            .ifPresent(entity -> {
                entity.updateFromDomain(event);
                jpaRepository.save(entity);
            });
    }
    
    @Override
    @Transactional
    public int deleteProcessedBefore(Instant before) {
        return jpaRepository.deleteProcessedBefore(before);
    }
}
