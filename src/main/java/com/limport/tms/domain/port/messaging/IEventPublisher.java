package com.limport.tms.domain.port.messaging;

import com.limport.tms.domain.event.IDomainEvent;

import java.util.List;

/**
 * Port for publishing domain events to external consumers.
 * Infrastructure layer provides the concrete implementation (Kafka, RabbitMQ, etc.).
 */
public interface IEventPublisher {
    
    /**
     * Publishes a single domain event.
     * @param event the event to publish
     */
    void publish(IDomainEvent event);
    
    /**
     * Publishes multiple domain events in order.
     * @param events the events to publish
     */
    void publishAll(List<? extends IDomainEvent> events);
}
