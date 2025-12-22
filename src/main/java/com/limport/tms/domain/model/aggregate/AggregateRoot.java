package com.limport.tms.domain.model.aggregate;

import com.limport.tms.domain.event.IDomainEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Base class for all aggregate roots in TMS.
 * Provides domain event collection and lifecycle management.
 * 
 * Events are collected during aggregate operations and should be
 * published after the aggregate is persisted (transactional outbox pattern).
 */
public abstract class AggregateRoot {
    
    private final List<IDomainEvent> domainEvents = new ArrayList<>();
    
    /**
     * Returns the unique identifier for this aggregate.
     */
    public abstract UUID getId();
    
    /**
     * Registers a domain event to be published after persistence.
     * @param event the domain event to register
     */
    protected void registerEvent(IDomainEvent event) {
        if (event != null) {
            domainEvents.add(event);
        }
    }
    
    /**
     * Returns an unmodifiable view of the pending domain events.
     * @return list of domain events
     */
    public List<IDomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }
    
    /**
     * Clears all pending domain events.
     * Should be called after events have been successfully published.
     */
    public void clearDomainEvents() {
        domainEvents.clear();
    }
    
    /**
     * Checks if there are any pending domain events.
     * @return true if there are pending events
     */
    public boolean hasPendingEvents() {
        return !domainEvents.isEmpty();
    }
}
