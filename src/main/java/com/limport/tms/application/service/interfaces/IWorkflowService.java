package com.limport.tms.application.service.interfaces;

import java.util.UUID;

/**
 * Service for triggering internal workflows based on business events.
 */
public interface IWorkflowService {

    /**
     * Schedules route optimization for a transport request.
     * This may trigger background processing for route planning and optimization.
     *
     * @param transportRequestId the ID of the transport request to optimize routes for
     */
    void scheduleRouteOptimization(UUID transportRequestId);
}