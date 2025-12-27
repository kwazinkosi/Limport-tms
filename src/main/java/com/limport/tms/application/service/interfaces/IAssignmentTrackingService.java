package com.limport.tms.application.service.interfaces;

import java.util.UUID;

/**
 * Service for tracking transport request assignments and their lifecycle.
 */
public interface IAssignmentTrackingService {

    /**
     * Records a new assignment of a transport request to a provider and vehicle.
     *
     * @param transportRequestId the ID of the transport request
     * @param providerId the ID of the assigned provider
     * @param vehicleId the ID of the assigned vehicle
     */
    void recordAssignment(UUID transportRequestId, UUID providerId, UUID vehicleId);
}