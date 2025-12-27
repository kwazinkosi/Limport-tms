package com.limport.tms.presentation.rest;

import com.limport.tms.application.command.CancelTransportRequestCommand;
import com.limport.tms.application.command.CompleteTransportRequestCommand;
import com.limport.tms.application.command.CreateTransportRequestCommand;
import com.limport.tms.application.cqrs.ICommandBus;
import com.limport.tms.application.cqrs.IQueryBus;
import com.limport.tms.application.dto.request.CancelTransportRequestRequest;
import com.limport.tms.application.dto.request.CreateTransportRequest;
import com.limport.tms.application.dto.response.TransportRequestResponse;
import com.limport.tms.application.query.GetTransportRequestQuery;
import com.limport.tms.application.query.ListTransportRequestsQuery;
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

    private final ICommandBus commandBus;
    private final IQueryBus queryBus;

    public TransportRequestController(ICommandBus commandBus, IQueryBus queryBus) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
    }

    /**
     * Create a new transport request (wizard result).
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransportRequestResponse create(@Valid @RequestBody CreateTransportRequest request) {
        CreateTransportRequestCommand command = new CreateTransportRequestCommand();
        command.setCustomerId(request.getCustomerId());
        command.setOriginLocationCode(request.getOriginLocationCode());
        command.setDestinationLocationCode(request.getDestinationLocationCode());
        command.setPickupFrom(request.getPickupFrom());
        command.setDeliveryUntil(request.getDeliveryUntil());
        command.setTotalWeight(request.getTotalWeight());
        command.setTotalPackages(request.getTotalPackages());
        command.setNotes(request.getNotes());
        
        return commandBus.dispatch(command);
    }

    /**
     * Dashboard-style list endpoint with optional status filter.
     */
    @GetMapping
    public List<TransportRequestResponse> list(
            @RequestParam(name = "status", required = false) TransportRequestStatus status) {
        ListTransportRequestsQuery query = new ListTransportRequestsQuery(status);
        return queryBus.dispatch(query);
    }

    /**
     * Detail view for a single transport request.
     */
    @GetMapping("/{id}")
    public TransportRequestResponse getById(@PathVariable("id") UUID id) {
        GetTransportRequestQuery query = new GetTransportRequestQuery(id);
        return queryBus.dispatch(query);
    }

    /**
     * Cancel an existing transport request.
     */
    @PostMapping("/{id}/cancel")
    @ResponseStatus(HttpStatus.OK)
    public TransportRequestResponse cancel(
            @PathVariable("id") UUID id,
            @Valid @RequestBody CancelTransportRequestRequest request) {
        CancelTransportRequestCommand command = new CancelTransportRequestCommand();
        command.setTransportRequestId(id);
        command.setReason(request.getReason());
        // Now dispatch the command
        return commandBus.dispatch(command);
    }

    /**
     * Mark a transport request as completed.
     */
    @PostMapping("/{id}/complete")
    @ResponseStatus(HttpStatus.OK)
    public TransportRequestResponse complete(@PathVariable("id") UUID id) {
        CompleteTransportRequestCommand command = new CompleteTransportRequestCommand();
        command.setTransportRequestId(id);
        command.setCompletedAt(java.time.Instant.now());
        
        return commandBus.dispatch(command);
    }
}
