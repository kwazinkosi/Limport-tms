package com.limport.tms.application.command.handler;

import com.limport.tms.application.command.CompleteTransportRequestCommand;
import com.limport.tms.application.cqrs.ICommandHandler;
import com.limport.tms.application.dto.response.TransportRequestResponse;
import com.limport.tms.application.service.interfaces.ITransportRequestCommandService;
import org.springframework.stereotype.Component;

/**
 * Handler for CompleteTransportRequestCommand.
 * Orchestrates the completion of a transport request.
 */
@Component
public class CompleteTransportRequestCommandHandler 
        implements ICommandHandler<CompleteTransportRequestCommand, TransportRequestResponse> {

    private final ITransportRequestCommandService commandService;

    public CompleteTransportRequestCommandHandler(ITransportRequestCommandService commandService) {
        this.commandService = commandService;
    }

    @Override
    public TransportRequestResponse handle(CompleteTransportRequestCommand command) {
        return commandService.completeTransportRequest(command.getTransportRequestId());
    }

    @Override
    public Class<CompleteTransportRequestCommand> getCommandType() {
        return CompleteTransportRequestCommand.class;
    }
}
