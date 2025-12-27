package com.limport.tms.infrastructure.persistence.mapper;

import com.limport.tms.domain.model.entity.OutboxEvent;
import com.limport.tms.infrastructure.persistence.entity.OutboxEventJpaEntity;

import org.springframework.stereotype.Component;

/**
 * Maps between OutboxEvent domain entity and JPA persistence entity.
 */
@Component
public class OutboxEventEntityMapper {

    /**
     * Converts JPA entity to domain entity.
     */
    public OutboxEvent toDomain(OutboxEventJpaEntity jpaEntity) {
        if (jpaEntity == null) {
            return null;
        }

        return new OutboxEvent(
            jpaEntity.getId(),
            jpaEntity.getEventType(),
            jpaEntity.getAggregateType(),
            jpaEntity.getAggregateId(),
            jpaEntity.getPayload(),
            jpaEntity.getOccurredOn(),
            jpaEntity.getStatus(),
            jpaEntity.getRetryCount(),
            jpaEntity.getProcessedAt(),
            jpaEntity.getErrorMessage()
        );
    }

    /**
     * Converts domain entity to JPA entity.
     */
    public OutboxEventJpaEntity toJpaEntity(OutboxEvent domain) {
        if (domain == null) {
            return null;
        }

        return OutboxEventJpaEntity.fromDomain(domain);
    }

    /**
     * Updates an existing JPA entity from domain entity.
     */
    public void updateJpaEntity(OutboxEvent domain, OutboxEventJpaEntity jpaEntity) {
        if (domain == null || jpaEntity == null) {
            return;
        }

        jpaEntity.updateFromDomain(domain);
    }
}
