package com.limport.tms.application.command.handler;

import com.limport.tms.application.command.AssignProviderCommand;
import com.limport.tms.application.cqrs.ICommandHandler;
import com.limport.tms.application.dto.request.AssignProviderRequest;
import com.limport.tms.application.dto.response.TransportRequestResponse;
import com.limport.tms.application.service.interfaces.ITransportRequestCommandService;
import org.springframework.stereotype.Component;

/**
 * Handler for AssignProviderCommand.
 * Orchestrates the assignment of a provider to a transport request.
 */
@Component
public class AssignProviderCommandHandler 
        implements ICommandHandler<AssignProviderCommand, TransportRequestResponse> {

    private final ITransportRequestCommandService commandService;

    public AssignProviderCommandHandler(ITransportRequestCommandService commandService) {
        this.commandService = commandService;
    }

    @Override
    public TransportRequestResponse handle(AssignProviderCommand command) {
        AssignProviderRequest request = new AssignProviderRequest();
        request.setProviderId(command.getProviderId());
        request.setVehicleId(command.getVehicleId());
        
        return commandService.assignProvider(command.getTransportRequestId(), request);
    }

    @Override
    public Class<AssignProviderCommand> getCommandType() {
        return AssignProviderCommand.class;
    }
}
