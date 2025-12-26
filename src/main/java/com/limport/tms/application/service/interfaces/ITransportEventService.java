package com.limport.tms.application.service.interfaces;

import com.limport.tms.domain.event.states.TransportRequestAssignedEvent;
import com.limport.tms.domain.event.states.TransportRequestCreatedEvent;

/**
 * Application service for handling transport request events.
 * Contains business logic for processing transport events.
 */
public interface ITransportEventService {

    /**
     * Handles transport request creation.
     * Triggers downstream processes like capacity verification, route optimization, etc.
     */
    void handleTransportRequestCreated(TransportRequestCreatedEvent event);

    /**
     * Handles transport request assignment.
     * Triggers notifications, scheduling updates, customer notifications, etc.
     */
    void handleTransportRequestAssigned(TransportRequestAssignedEvent event);
}