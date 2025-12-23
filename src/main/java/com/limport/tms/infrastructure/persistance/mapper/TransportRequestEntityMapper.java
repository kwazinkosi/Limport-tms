package com.limport.tms.infrastructure.persistance.mapper;

import com.limport.tms.domain.model.entity.TransportRequest;
import com.limport.tms.infrastructure.persistance.entity.TransportRequestJpaEntity;
import org.springframework.stereotype.Component;

/**
 * Maps between TransportRequest domain entity and JPA persistence entity.
 * Separates mapping responsibility from entity classes.
 */
@Component
public class TransportRequestEntityMapper {

    /**
     * Converts JPA entity to domain entity.
     */
    public TransportRequest toDomain(TransportRequestJpaEntity jpaEntity) {
        if (jpaEntity == null) {
            return null;
        }

        TransportRequest domain = new TransportRequest();
        domain.setId(jpaEntity.getId());
        domain.setReference(jpaEntity.getReference());
        domain.setCustomerId(jpaEntity.getCustomerId());
        domain.setOriginLocationCode(jpaEntity.getOriginLocationCode());
        domain.setDestinationLocationCode(jpaEntity.getDestinationLocationCode());
        domain.setPickupFrom(jpaEntity.getPickupFrom());
        domain.setPickupUntil(jpaEntity.getPickupUntil());
        domain.setDeliveryFrom(jpaEntity.getDeliveryFrom());
        domain.setDeliveryUntil(jpaEntity.getDeliveryUntil());
        domain.setTotalWeight(jpaEntity.getTotalWeight());
        domain.setTotalPackages(jpaEntity.getTotalPackages());
        domain.setStatus(jpaEntity.getStatus());
        domain.setCreatedAt(jpaEntity.getCreatedAt());
        domain.setLastUpdatedAt(jpaEntity.getLastUpdatedAt());
        return domain;
    }

    /**
     * Converts domain entity to JPA entity.
     */
    public TransportRequestJpaEntity toJpaEntity(TransportRequest domain) {
        if (domain == null) {
            return null;
        }

        return new TransportRequestJpaEntity(
            domain.getId(),
            domain.getReference(),
            domain.getCustomerId(),
            domain.getOriginLocationCode(),
            domain.getDestinationLocationCode(),
            domain.getPickupFrom(),
            domain.getPickupUntil(),
            domain.getDeliveryFrom(),
            domain.getDeliveryUntil(),
            domain.getTotalWeight(),
            domain.getTotalPackages(),
            domain.getStatus(),
            domain.getCreatedAt(),
            domain.getLastUpdatedAt()
        );
    }

    /**
     * Updates an existing JPA entity from domain entity.
     * Useful for merge operations.
     */
    public void updateJpaEntity(TransportRequest domain, TransportRequestJpaEntity jpaEntity) {
        if (domain == null || jpaEntity == null) {
            return;
        }

        jpaEntity.setReference(domain.getReference());
        jpaEntity.setCustomerId(domain.getCustomerId());
        jpaEntity.setOriginLocationCode(domain.getOriginLocationCode());
        jpaEntity.setDestinationLocationCode(domain.getDestinationLocationCode());
        jpaEntity.setPickupFrom(domain.getPickupFrom());
        jpaEntity.setPickupUntil(domain.getPickupUntil());
        jpaEntity.setDeliveryFrom(domain.getDeliveryFrom());
        jpaEntity.setDeliveryUntil(domain.getDeliveryUntil());
        jpaEntity.setTotalWeight(domain.getTotalWeight());
        jpaEntity.setTotalPackages(domain.getTotalPackages());
        jpaEntity.setStatus(domain.getStatus());
        jpaEntity.setLastUpdatedAt(domain.getLastUpdatedAt());
    }
}
