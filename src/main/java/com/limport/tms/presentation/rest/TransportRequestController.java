package com.limport.tms.presentation.rest;

import com.limport.tms.application.dto.request.CancelTransportRequestRequest;
import com.limport.tms.application.dto.request.CreateTransportRequest;
import com.limport.tms.application.dto.response.TransportRequestResponse;
import com.limport.tms.application.service.interfaces.ITransportRequestCommandService;
import com.limport.tms.application.service.interfaces.ITransportRequestQueryService;
import com.limport.tms.domain.model.enums.TransportRequestStatus;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Primary REST controller for transport request operations in the TMS.
 *
 * Endpoints are designed to align with the provided UI drafts
 * (dashboard, detail view, quick status updates, and creation flow).
 */
@RestController
@RequestMapping("/api/transport-requests")
public class TransportRequestController {

    private final ITransportRequestCommandService commandService;
    private final ITransportRequestQueryService queryService;

    public TransportRequestController(ITransportRequestCommandService commandService,
                                      ITransportRequestQueryService queryService) {
        this.commandService = commandService;
        this.queryService = queryService;
    }

    /**
     * Create a new transport request (wizard result).
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransportRequestResponse create(@Valid @RequestBody CreateTransportRequest request) {
        return commandService.createTransportRequest(request);
    }

    /**
     * Dashboard-style list endpoint with optional status filter.
     */
    @GetMapping
    public List<TransportRequestResponse> list(
            @RequestParam(name = "status", required = false) TransportRequestStatus status) {
        if (status == null) {
            return queryService.listAll();
        }
        return queryService.listByStatus(status);
    }

    /**
     * Detail view for a single transport request.
     */
    @GetMapping("/{id}")
    public TransportRequestResponse getById(@PathVariable("id") UUID id) {
        return queryService.getById(id);
    }

    /**
     * Cancel an existing transport request.
     */
    @PostMapping("/{id}/cancel")
    @ResponseStatus(HttpStatus.OK)
    public TransportRequestResponse cancel(
            @PathVariable("id") UUID id,
            @Valid @RequestBody CancelTransportRequestRequest request) {
        return commandService.cancelTransportRequest(id, request);
    }

    /**
     * Mark a transport request as completed.
     */
    @PostMapping("/{id}/complete")
    @ResponseStatus(HttpStatus.OK)
    public TransportRequestResponse complete(@PathVariable("id") UUID id) {
        return commandService.completeTransportRequest(id);
    }
}
