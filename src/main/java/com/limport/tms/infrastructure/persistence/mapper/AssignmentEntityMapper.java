package com.limport.tms.infrastructure.persistence.mapper;

import com.limport.tms.domain.model.entity.Assignment;
import com.limport.tms.infrastructure.persistence.entity.AssignmentJpaEntity;

import org.springframework.stereotype.Component;

/**
 * Maps between Assignment domain entity and JPA persistence entity.
 */
@Component
public class AssignmentEntityMapper {

    /**
     * Converts JPA entity to domain entity.
     */
    public Assignment toDomain(AssignmentJpaEntity jpaEntity) {
        if (jpaEntity == null) {
            return null;
        }

        Assignment domain = new Assignment();
        domain.setId(jpaEntity.getId());
        domain.setTransportRequestId(jpaEntity.getTransportRequestId());
        domain.setProviderId(jpaEntity.getProviderId());
        domain.setVehicleId(jpaEntity.getVehicleId());
        domain.setScheduledPickupTime(jpaEntity.getScheduledPickupTime());
        domain.setEstimatedDeliveryTime(jpaEntity.getEstimatedDeliveryTime());
        domain.setAssignmentNotes(jpaEntity.getAssignmentNotes());
        domain.setStatus(jpaEntity.getStatus());
        domain.setAssignedBy(jpaEntity.getAssignedBy());
        domain.setAssignedAt(jpaEntity.getAssignedAt());
        domain.setLastUpdatedAt(jpaEntity.getLastUpdatedAt());
        return domain;
    }

    /**
     * Converts domain entity to JPA entity.
     */
    public AssignmentJpaEntity toJpaEntity(Assignment domain) {
        if (domain == null) {
            return null;
        }

        return new AssignmentJpaEntity(
            domain.getId(),
            domain.getTransportRequestId(),
            domain.getProviderId(),
            domain.getVehicleId(),
            domain.getScheduledPickupTime(),
            domain.getEstimatedDeliveryTime(),
            domain.getAssignmentNotes(),
            domain.getStatus(),
            domain.getAssignedBy(),
            domain.getAssignedAt(),
            domain.getLastUpdatedAt()
        );
    }

    /**
     * Updates an existing JPA entity from domain entity.
     */
    public void updateJpaEntity(Assignment domain, AssignmentJpaEntity jpaEntity) {
        if (domain == null || jpaEntity == null) {
            return;
        }

        jpaEntity.setProviderId(domain.getProviderId());
        jpaEntity.setVehicleId(domain.getVehicleId());
        jpaEntity.setScheduledPickupTime(domain.getScheduledPickupTime());
        jpaEntity.setEstimatedDeliveryTime(domain.getEstimatedDeliveryTime());
        jpaEntity.setAssignmentNotes(domain.getAssignmentNotes());
        jpaEntity.setStatus(domain.getStatus());
        jpaEntity.setLastUpdatedAt(domain.getLastUpdatedAt());
    }
}
