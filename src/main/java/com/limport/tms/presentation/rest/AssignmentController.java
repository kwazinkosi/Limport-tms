package com.limport.tms.presentation.rest;

import com.limport.tms.application.dto.request.AssignProviderRequest;
import com.limport.tms.application.dto.response.AssignmentResponse;
import com.limport.tms.application.dto.response.TransportRequestResponse;
import com.limport.tms.application.service.interfaces.IAssignmentQueryService;
import com.limport.tms.application.service.interfaces.ITransportRequestCommandService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST endpoints for provider/vehicle assignments.
 * Handles assignment queries and accepts assignment notifications from PMS.
 */
@RestController
@RequestMapping("/api/assignments")
public class AssignmentController {

    private final ITransportRequestCommandService commandService;
    private final IAssignmentQueryService assignmentQueryService;

    public AssignmentController(ITransportRequestCommandService commandService,
                               IAssignmentQueryService assignmentQueryService) {
        this.commandService = commandService;
        this.assignmentQueryService = assignmentQueryService;
    }

    /**
     * Assign a provider to a transport request.
     * Typically called after PMS matches a provider or manually by operators.
     */
    @PostMapping("/transport-requests/{id}/assign")
    @ResponseStatus(HttpStatus.OK)
    public TransportRequestResponse assignProvider(
            @PathVariable("id") UUID transportRequestId,
            @Valid @RequestBody AssignProviderRequest request) {
        return commandService.assignProvider(transportRequestId, request);
    }

    /**
     * Get all assignments for a specific transport request.
     */
    @GetMapping("/transport-requests/{id}")
    public List<AssignmentResponse> getAssignmentsByTransportRequest(
            @PathVariable("id") UUID transportRequestId) {
        return assignmentQueryService.listByTransportRequest(transportRequestId);
    }

    /**
     * Get a specific assignment by ID.
     */
    @GetMapping("/{id}")
    public AssignmentResponse getAssignment(@PathVariable("id") UUID assignmentId) {
        return assignmentQueryService.getById(assignmentId);
    }

    /**
     * Get active assignment for a transport request.
     */
    @GetMapping("/transport-requests/{id}/active")
    public AssignmentResponse getActiveAssignment(@PathVariable("id") UUID transportRequestId) {
        return assignmentQueryService.getActiveAssignment(transportRequestId);
    }

    /**
     * List assignments by provider (for provider dashboard).
     */
    @GetMapping("/providers/{providerId}")
    public List<AssignmentResponse> getAssignmentsByProvider(
            @PathVariable("providerId") UUID providerId) {
        return assignmentQueryService.listByProvider(providerId);
    }
}