package com.limport.tms.application.event;

/**
 * Port for handling external events from other services.
 * Each event type has its own handler implementation.
 * 
 * Follows Strategy Pattern - handlers are swappable and discoverable.
 * 
 * @param <T> The specific external event type this handler supports
 */
public interface IExternalEventHandler<T extends ExternalEvent> {
    
    /**
     * Handle the incoming event.
     * Called asynchronously when event is received from Kafka.
     * 
     * @param event The event to handle
     */
    void handle(T event);
    
    /**
     * The event type this handler supports.
     * @return Event type string (e.g., "ProviderEvents.Matched")
     */
    String getSupportedEventType();
    
    /**
     * Get the class of event this handler supports.
     * Used for type-safe deserialization.
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
