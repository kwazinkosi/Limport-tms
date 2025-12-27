package com.limport.tms.application.service.impl;

import com.limport.tms.application.service.interfaces.IWorkflowService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Implementation of workflow service for triggering background processes.
 * Currently logs workflow triggers - in a real implementation, this would
 * submit jobs to a job queue or trigger async processing.
 */
@Service
public class WorkflowServiceImpl implements IWorkflowService {

    private static final Logger log = LoggerFactory.getLogger(WorkflowServiceImpl.class);

    @Override
    public void scheduleRouteOptimization(UUID transportRequestId) {
        log.info("Scheduling route optimization workflow for transport request: {}", transportRequestId);

        // TODO: Submit route optimization job to background processing queue
        // Example: Send message to route-optimization-queue or trigger async service

        log.debug("Route optimization workflow scheduled for: {}", transportRequestId);
    }
}