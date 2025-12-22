package com.limport.tms.infrastructure.repository.jpa;

import com.limport.tms.domain.model.entity.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for OutboxEvent persistence.
 */
@Repository
public interface JpaOutboxEventRepository extends JpaRepository<OutboxEventJpaEntity, UUID> {
    
    @Query("SELECT o FROM OutboxEventJpaEntity o WHERE o.status = 'PENDING' ORDER BY o.occurredOn ASC")
    List<OutboxEventJpaEntity> findPendingEvents(@Param("limit") int limit);
    
    @Modifying
    @Query("DELETE FROM OutboxEventJpaEntity o WHERE o.status = 'PROCESSED' AND o.processedAt < :before")
    int deleteProcessedBefore(@Param("before") Instant before);
}
