package com.limport.tms.application.event;

import com.limport.tms.domain.event.IDomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Registry that routes domain events to synchronous internal handlers.
 * Enables immediate processing of domain events for internal business logic.
 *
 * Spring automatically injects all IInternalEventHandler beans.
 */
@Component
public class InternalEventHandlerRegistry {

    private static final Logger log = LoggerFactory.getLogger(InternalEventHandlerRegistry.class);

    private final List<IInternalEventHandler<? extends IDomainEvent>> handlers;

    public InternalEventHandlerRegistry(List<IInternalEventHandler<? extends IDomainEvent>> handlers) {
        this.handlers = handlers;
        log.info("Registered {} internal event handlers: {}",
            handlers.size(),
            handlers.stream().map(h -> h.getSupportedEventType()).toList()
        );
    }

    /**
     * Find handler for given event type.
     */
    @SuppressWarnings("unchecked")
    public <T extends IDomainEvent> Optional<IInternalEventHandler<T>> findHandler(String eventType) {
        return handlers.stream()
                .filter(handler -> handler.supports(eventType))
                .map(handler -> (IInternalEventHandler<T>) handler)
                .findFirst();
    }

    /**
     * Dispatch event to synchronous internal handler.
     * @return true if handler found and executed, false otherwise
     */
    public <T extends IDomainEvent> boolean dispatch(T event) {
        String eventType = event.eventType();

        Optional<IInternalEventHandler<T>> handler = findHandler(eventType);

        if (handler.isPresent()) {
            log.debug("Dispatching event {} to internal handler {}", eventType, handler.get().getClass().getSimpleName());
            try {
                handler.get().handle(event);
                return true;
            } catch (Exception e) {
                log.error("Failed to handle internal event {}: {}", eventType, e.getMessage(), e);
                // For internal handlers, we might want to fail the transaction
                throw new RuntimeException("Internal event handling failed: " + e.getMessage(), e);
            }
        } else {
            log.debug("No internal handler found for event type: {}", eventType);
            return false;
        }
    }

    /**
     * Check if a handler exists for the given event type.
     */
    public boolean hasHandler(String eventType) {
        return handlers.stream().anyMatch(h -> h.supports(eventType));
    }
}