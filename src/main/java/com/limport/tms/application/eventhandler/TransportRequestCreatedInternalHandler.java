package com.limport.tms.application.eventhandler;

import com.limport.tms.application.event.IInternalEventHandler;
import com.limport.tms.application.service.interfaces.IInternalNotificationService;
import com.limport.tms.application.service.interfaces.IReadModelUpdater;
import com.limport.tms.application.service.interfaces.IWorkflowService;
import com.limport.tms.domain.event.EventTypes;
import com.limport.tms.domain.event.states.TransportRequestCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Internal handler for TransportRequestCreatedEvent.
 * Performs synchronous internal processing when transport requests are created.
 *
 * Example internal processing:
 * - Update internal metrics
 * - Trigger internal workflows
 * - Update read models
 * - Send internal notifications
 */
@Component
public class TransportRequestCreatedInternalHandler implements IInternalEventHandler<TransportRequestCreatedEvent> {

    private static final Logger log = LoggerFactory.getLogger(TransportRequestCreatedInternalHandler.class);

    private final IReadModelUpdater readModelUpdater;
    private final IWorkflowService workflowService;
    private final IInternalNotificationService notificationService;

    public TransportRequestCreatedInternalHandler(
            IReadModelUpdater readModelUpdater,
            IWorkflowService workflowService,
            IInternalNotificationService notificationService) {
        this.readModelUpdater = readModelUpdater;
        this.workflowService = workflowService;
        this.notificationService = notificationService;
    }

    @Override
    public void handle(TransportRequestCreatedEvent event) {
        log.info("Processing TransportRequestCreatedEvent synchronously: requestId={}, origin={}, destination={}",
            event.getTransportRequestId(), event.getOrigin(), event.getDestination());

        // Internal business logic that should happen immediately:

        // 1. Update read models for internal dashboards
        readModelUpdater.updateTransportRequestSummary(event.getTransportRequestId(), event.getOrigin(), event.getDestination());

        // 2. Trigger internal workflows (e.g., capacity planning, route optimization scheduling)
        workflowService.scheduleRouteOptimization(event.getTransportRequestId());

        // 3. Send internal notifications
        notificationService.notifyOperationsTeam("New transport request created", event);

        log.debug("Internal processing completed for transport request: {}", event.getTransportRequestId());
    }

    @Override
    public String getSupportedEventType() {
        return EventTypes.Transport.Request.CREATED;
    }

    @Override
    public Class<TransportRequestCreatedEvent> getEventClass() {
        return TransportRequestCreatedEvent.class;
    }
}
