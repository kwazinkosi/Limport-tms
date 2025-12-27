package com.limport.tms.application.event;

import com.limport.tms.domain.event.IDomainEvent;

/**
 * Interface for synchronous internal event handlers.
 * These handlers process domain events immediately within the same transaction
 * for internal business logic, read model updates, etc.
 *
 * @param <T> The specific domain event type this handler supports
 */
public interface IInternalEventHandler<T extends IDomainEvent> {

    /**
     * Handle the domain event synchronously.
     * Called immediately after event collection in the same transaction.
     *
     * @param event The domain event to handle
     */
    void handle(T event);

    /**
     * The event type this handler supports.
     * @return Event type string (e.g., "TMS.Transport.Request.Created")
     */
    String getSupportedEventType();

    /**
     * Get the class of event this handler supports.
     * Used for type-safe handling.
     *
     * @return The event class
     */
    Class<T> getEventClass();

    /**
     * Whether this handler supports the given event type.
     */
    default boolean supports(String eventType) {
        return getSupportedEventType().equals(eventType);
    }
}