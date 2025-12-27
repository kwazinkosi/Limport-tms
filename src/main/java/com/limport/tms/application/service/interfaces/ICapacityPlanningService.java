package com.limport.tms.application.service.interfaces;

import java.util.UUID;

/**
 * Service for capacity planning and resource allocation.
 */
public interface ICapacityPlanningService {

    /**
     * Reserves capacity for a provider's vehicle when a transport request is assigned.
     *
     * @param providerId the ID of the provider
     * @param vehicleId the ID of the vehicle
     * @param transportRequestId the ID of the transport request being assigned
     */
    void reserveCapacity(UUID providerId, UUID vehicleId, UUID transportRequestId);
}