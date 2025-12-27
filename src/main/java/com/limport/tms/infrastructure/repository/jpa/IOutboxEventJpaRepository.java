package com.limport.tms.infrastructure.repository.jpa;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.limport.tms.infrastructure.persistence.entity.OutboxEventJpaEntity;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for OutboxEvent persistence.
 */
@Repository
public interface IOutboxEventJpaRepository extends JpaRepository<OutboxEventJpaEntity, UUID> {
    
    /**
     * Find pending outbox events ordered by occurrence time.
     * Uses Pageable to properly limit results.
     */
    @Query("SELECT o FROM OutboxEventJpaEntity o WHERE o.status = 'PENDING' ORDER BY o.occurredOn ASC")
    List<OutboxEventJpaEntity> findPendingEvents(Pageable pageable);
    
    @Modifying
    @Query("DELETE FROM OutboxEventJpaEntity o WHERE o.status = 'PROCESSED' AND o.processedAt < :before")
    int deleteProcessedBefore(@Param("before") Instant before);
    
    /**
     * Count pending events for monitoring.
     */
    @Query("SELECT COUNT(o) FROM OutboxEventJpaEntity o WHERE o.status = 'PENDING'")
    long countPendingEvents();
}
