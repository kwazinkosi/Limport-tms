package com.limport.tms.presentation.rest;

import com.limport.tms.application.command.AssignProviderCommand;
import com.limport.tms.application.cqrs.ICommandBus;
import com.limport.tms.application.cqrs.IQueryBus;
import com.limport.tms.application.dto.request.AssignProviderRequest;
import com.limport.tms.application.dto.response.AssignmentResponse;
import com.limport.tms.application.dto.response.TransportRequestResponse;
import com.limport.tms.application.query.GetActiveAssignmentQuery;
import com.limport.tms.application.query.GetAssignmentQuery;
import com.limport.tms.application.query.ListAssignmentsByProviderQuery;
import com.limport.tms.application.query.ListAssignmentsByTransportRequestQuery;
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

    private final ICommandBus commandBus;
    private final IQueryBus queryBus;

    public AssignmentController(ICommandBus commandBus, IQueryBus queryBus) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
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
        AssignProviderCommand command = new AssignProviderCommand();
        command.setTransportRequestId(transportRequestId);
        command.setProviderId(request.getProviderId());
        command.setVehicleId(request.getVehicleId());
        
        return commandBus.dispatch(command);
    }

    /**
     * Get all assignments for a specific transport request.
     */
    @GetMapping("/transport-requests/{id}")
    public List<AssignmentResponse> getAssignmentsByTransportRequest(
            @PathVariable("id") UUID transportRequestId) {
        ListAssignmentsByTransportRequestQuery query = new ListAssignmentsByTransportRequestQuery(transportRequestId);
        return queryBus.dispatch(query);
    }

    /**
     * Get a specific assignment by ID.
     */
    @GetMapping("/{id}")
    public AssignmentResponse getAssignment(@PathVariable("id") UUID assignmentId) {
        GetAssignmentQuery query = new GetAssignmentQuery(assignmentId);
        return queryBus.dispatch(query);
    }

    /**
     * Get active assignment for a transport request.
     */
    @GetMapping("/transport-requests/{id}/active")
    public AssignmentResponse getActiveAssignment(@PathVariable("id") UUID transportRequestId) {
        GetActiveAssignmentQuery query = new GetActiveAssignmentQuery(transportRequestId);
        return queryBus.dispatch(query);
    }

    /**
     * List assignments by provider (for provider dashboard).
     */
    @GetMapping("/providers/{providerId}")
    public List<AssignmentResponse> getAssignmentsByProvider(
            @PathVariable("providerId") UUID providerId) {
        ListAssignmentsByProviderQuery query = new ListAssignmentsByProviderQuery(providerId);
        return queryBus.dispatch(query);
    }
}