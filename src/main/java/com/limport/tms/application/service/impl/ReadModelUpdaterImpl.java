package com.limport.tms.application.service.impl;

import com.limport.tms.application.service.interfaces.IReadModelUpdater;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Implementation of read model updater for internal dashboards.
 * Currently logs updates - in a real implementation, this would update
 * database tables or cache used by dashboard queries.
 */
@Service
public class ReadModelUpdaterImpl implements IReadModelUpdater {

    private static final Logger log = LoggerFactory.getLogger(ReadModelUpdaterImpl.class);

    @Override
    public void updateTransportRequestSummary(UUID transportRequestId, String origin, String destination) {
        log.info("Updating transport request summary read model: requestId={}, origin={}, destination={}",
            transportRequestId, origin, destination);

        // TODO: Update database read model tables for dashboard queries
        // Example: Insert/update record in transport_request_summary table
        // with aggregated data for origin/destination statistics

        log.debug("Transport request summary read model updated for: {}", transportRequestId);
    }

    @Override
    public void updateActiveAssignments(UUID transportRequestId) {
        log.info("Updating active assignments read model: requestId={}", transportRequestId);

        // TODO: Update operational dashboard read models
        // Example: Update active_assignments table with new assignment data

        log.debug("Active assignments read model updated for: {}", transportRequestId);
    }
}