package com.limport.tms.domain.event;

import java.time.Instant;

/**
 * Marker interface for all domain events
 */
public interface IDomainEvent {
    
    Instant occurredOn();
}