package com.limport.tms.application.service.impl;

import com.limport.tms.application.service.interfaces.ITransportEventService;
import com.limport.tms.domain.event.states.TransportRequestAssignedEvent;
import com.limport.tms.domain.event.states.TransportRequestCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Implementation of transport event service.
 * Contains business logic for processing transport events.
 */
@Service
public class TransportEventServiceImpl implements ITransportEventService {

    private static final Logger log = LoggerFactory.getLogger(TransportEventServiceImpl.class);

    @Override
    public void handleTransportRequestCreated(TransportRequestCreatedEvent event) {
        log.info("Processing TransportRequestCreated: id={}, origin={}, destination={}",
            event.getTransportRequestId(),
            event.getOrigin(),
            event.getDestination());

        // Business logic for handling transport request creation
        // - Capacity verification
        // - Route optimization
        // - Provider matching
        // - Initial scheduling

        log.debug("Transport request {} processed successfully", event.getTransportRequestId());
    }

    @Override
    public void handleTransportRequestAssigned(TransportRequestAssignedEvent event) {
        log.info("Processing TransportRequestAssigned: requestId={}, providerId={}, vehicleId={}",
            event.getTransportRequestId(),
            event.getProviderId(),
            event.getVehicleId());

        // Business logic for handling transport request assignment
        // - Notify provider
        // - Update scheduling system
        // - Send customer notification
        // - Update capacity reservations

        log.debug("Transport request assignment {} processed successfully", event.getTransportRequestId());
    }
}