package com.limport.tms.application.eventhandler;

import com.limport.tms.application.event.IInternalEventHandler;
import com.limport.tms.application.service.interfaces.IAssignmentTrackingService;
import com.limport.tms.application.service.interfaces.ICapacityPlanningService;
import com.limport.tms.application.service.interfaces.IInternalNotificationService;
import com.limport.tms.application.service.interfaces.IReadModelUpdater;
import com.limport.tms.domain.event.EventTypes;
import com.limport.tms.domain.event.states.TransportRequestAssignedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Internal handler for TransportRequestAssignedEvent.
 * Performs synchronous internal processing when transport requests are assigned.
 */
@Component
public class TransportRequestAssignedInternalHandler implements IInternalEventHandler<TransportRequestAssignedEvent> {

    private static final Logger log = LoggerFactory.getLogger(TransportRequestAssignedInternalHandler.class);

    private final IAssignmentTrackingService trackingService;
    private final ICapacityPlanningService capacityService;
    private final IInternalNotificationService notificationService;
    private final IReadModelUpdater readModelUpdater;

    public TransportRequestAssignedInternalHandler(
            IAssignmentTrackingService trackingService,
            ICapacityPlanningService capacityService,
            IInternalNotificationService notificationService,
            IReadModelUpdater readModelUpdater) {
        this.trackingService = trackingService;
        this.capacityService = capacityService;
        this.notificationService = notificationService;
        this.readModelUpdater = readModelUpdater;
    }

    @Override
    public void handle(TransportRequestAssignedEvent event) {
        log.info("Processing TransportRequestAssignedEvent synchronously: requestId={}, providerId={}, vehicleId={}",
            event.getTransportRequestId(), event.getProviderId(), event.getVehicleId());

        // Internal business logic that should happen immediately:

        // 1. Update assignment tracking
        trackingService.recordAssignment(event.getTransportRequestId(), event.getProviderId(), event.getVehicleId());

        // 2. Update capacity planning
        capacityService.reserveCapacity(event.getProviderId(), event.getVehicleId(), event.getTransportRequestId());

        // 3. Trigger internal notifications
        notificationService.notifyProviderTeam("Transport request assigned", event);

        // 4. Update operational dashboards
        readModelUpdater.updateActiveAssignments(event.getTransportRequestId());

        log.debug("Internal processing completed for transport request assignment: {}", event.getTransportRequestId());
    }

    @Override
    public String getSupportedEventType() {
        return EventTypes.Transport.Request.ASSIGNED;
    }

    @Override
    public Class<TransportRequestAssignedEvent> getEventClass() {
        return TransportRequestAssignedEvent.class;
    }
}
