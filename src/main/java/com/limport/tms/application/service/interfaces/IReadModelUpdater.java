package com.limport.tms.application.service.interfaces;

import java.util.UUID;

/**
 * Service for updating read models used by dashboards and UI components.
 */
public interface IReadModelUpdater {

    /**
     * Updates the transport request summary read model.
     * Used for internal dashboards showing transport request statistics.
     *
     * @param transportRequestId the ID of the transport request
     * @param origin the origin location
     * @param destination the destination location
     */
    void updateTransportRequestSummary(UUID transportRequestId, String origin, String destination);

    /**
     * Updates the active assignments dashboard read model.
     * Used for operational dashboards tracking current assignments.
     *
     * @param transportRequestId the ID of the transport request that was assigned
     */
    void updateActiveAssignments(UUID transportRequestId);
}