package com.limport.tms.application.service.interfaces;

import com.limport.tms.application.dto.response.AssignmentResponse;

import java.util.List;
import java.util.UUID;

/**
 * Query service interface for assignment-related operations.
 */
public interface IAssignmentQueryService {

    /**
     * Get assignment by ID.
     */
    AssignmentResponse getById(UUID id);

    /**
     * List all assignments for a transport request.
     */
    List<AssignmentResponse> listByTransportRequest(UUID transportRequestId);

    /**
     * List assignments by provider.
     */
    List<AssignmentResponse> listByProvider(UUID providerId);

    /**
     * Get active assignment for a transport request (if any).
     */
    AssignmentResponse getActiveAssignment(UUID transportRequestId);
}
