package com.limport.tms.application.command.handler;

import com.limport.tms.application.command.AssignProviderToTransportRequestCommand;
import com.limport.tms.application.cqrs.ICommandHandler;
import com.limport.tms.application.dto.request.AssignProviderRequest;
import com.limport.tms.application.dto.response.TransportRequestResponse;
import com.limport.tms.application.service.interfaces.ITransportRequestCommandService;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Handler for AssignProviderToTransportRequestCommand.
 * Orchestrates the assignment of a provider with scheduling details.
 */
@Component
public class AssignProviderToTransportRequestCommandHandler 
        implements ICommandHandler<AssignProviderToTransportRequestCommand, TransportRequestResponse> {

    private final ITransportRequestCommandService commandService;

    public AssignProviderToTransportRequestCommandHandler(ITransportRequestCommandService commandService) {
        this.commandService = commandService;
    }

    @Override
    public TransportRequestResponse handle(AssignProviderToTransportRequestCommand command) {
        AssignProviderRequest request = new AssignProviderRequest();
        request.setProviderId(UUID.fromString(command.getProviderId()));
        request.setVehicleId(UUID.fromString(command.getVehicleId()));
        request.setScheduledPickup(command.getScheduledPickup());
        request.setScheduledDelivery(command.getScheduledDelivery());
        request.setAssignmentNotes(command.getAssignmentNotes());
        
        return commandService.assignProvider(command.getTransportRequestId(), request);
    }

    @Override
    public Class<AssignProviderToTransportRequestCommand> getCommandType() {
        return AssignProviderToTransportRequestCommand.class;
    }
}
