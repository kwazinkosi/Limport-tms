package com.limport.tms.infrastructure.repository.jpa;

import com.limport.tms.domain.model.enums.TransportRequestStatus;
import com.limport.tms.infrastructure.persistence.entity.TransportRequestJpaEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import java.math.BigDecimal;

/**
 * Spring Data JPA repository for TransportRequestJpaEntity.
 */
@Repository
public interface ITransportRequestJpaRepository extends JpaRepository<TransportRequestJpaEntity, UUID> {

    List<TransportRequestJpaEntity> findByStatus(TransportRequestStatus status);
    
    List<TransportRequestJpaEntity> findByCustomerId(String customerId);

    List<TransportRequestJpaEntity> findByStatusAndTotalWeightLessThanEqual(TransportRequestStatus status, BigDecimal maxWeight);
}
