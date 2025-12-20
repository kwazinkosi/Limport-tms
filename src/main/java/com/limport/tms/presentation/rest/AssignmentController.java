package com.limport.tms.presentation.rest;

import com.limport.tms.application.dto.request.AssignProviderRequest;
import com.limport.tms.application.dto.response.TransportRequestResponse;
import com.limport.tms.application.service.interfaces.ITransportRequestCommandService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST endpoints focused on provider/vehicle assignment for transport requests.
 *
 * This is separated from the main TransportRequestController to keep the
 * assignment-related use cases cohesive.
 */
@RestController
@RequestMapping("/api/transport-requests/{id}/assignment")
public class AssignmentController {

    private final ITransportRequestCommandService commandService;

    public AssignmentController(ITransportRequestCommandService commandService) {
        this.commandService = commandService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public TransportRequestResponse assignProvider(
            @PathVariable("id") UUID transportRequestId,
            @Valid @RequestBody AssignProviderRequest request) {
        return commandService.assignProvider(transportRequestId, request);
    }
}

