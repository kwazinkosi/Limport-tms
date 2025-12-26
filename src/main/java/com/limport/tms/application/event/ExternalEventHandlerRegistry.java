package com.limport.tms.application.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Registry that routes external events to appropriate handlers.
 * Follows Strategy Pattern + Registry Pattern.
 * 
 * Spring automatically injects all IExternalEventHandler beans.
 */
@Component
public class ExternalEventHandlerRegistry {
    
    private static final Logger log = LoggerFactory.getLogger(ExternalEventHandlerRegistry.class);
    
    private final List<IExternalEventHandler<? extends ExternalEvent>> handlers;

    public ExternalEventHandlerRegistry(List<IExternalEventHandler<? extends ExternalEvent>> handlers) {
        this.handlers = handlers;
        log.info("Registered {} external event handlers: {}", 
            handlers.size(),
            handlers.stream().map(h -> h.getSupportedEventType()).toList()
        );
    }

    /**
     * Find handler for given event type.
     */
    @SuppressWarnings("unchecked")
    public <T extends ExternalEvent> Optional<IExternalEventHandler<T>> findHandler(String eventType) {
        return handlers.stream()
                .filter(handler -> handler.supports(eventType))
                .map(handler -> (IExternalEventHandler<T>) handler)
                .findFirst();
    }

    /**
     * Dispatch event to appropriate handler.
     * @return true if handler found and executed, false otherwise
     */
    public <T extends ExternalEvent> boolean dispatch(T event) {
        String eventType = event.eventType();
        
        Optional<IExternalEventHandler<T>> handler = findHandler(eventType);
        
        if (handler.isPresent()) {
            log.debug("Dispatching event {} to handler {}", 
                eventType, handler.get().getClass().getSimpleName());
            handler.get().handle(event);
            return true;
        } else {
            log.warn("No handler found for event type: {} from {}", 
                eventType, event.sourceService());
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
