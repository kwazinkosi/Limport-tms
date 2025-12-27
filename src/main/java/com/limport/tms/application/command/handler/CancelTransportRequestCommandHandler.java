package com.limport.tms.application.command.handler;

import com.limport.tms.application.command.CancelTransportRequestCommand;
import com.limport.tms.application.cqrs.ICommandHandler;
import com.limport.tms.application.dto.request.CancelTransportRequestRequest;
import com.limport.tms.application.dto.response.TransportRequestResponse;
import com.limport.tms.application.service.interfaces.ITransportRequestCommandService;
import org.springframework.stereotype.Component;

/**
 * Handler for CancelTransportRequestCommand.
 * Orchestrates the cancellation of a transport request.
 */
@Component
public class CancelTransportRequestCommandHandler 
        implements ICommandHandler<CancelTransportRequestCommand, TransportRequestResponse> {

    private final ITransportRequestCommandService commandService;

    public CancelTransportRequestCommandHandler(ITransportRequestCommandService commandService) {
        this.commandService = commandService;
    }

    @Override
    public TransportRequestResponse handle(CancelTransportRequestCommand command) {
        CancelTransportRequestRequest request = new CancelTransportRequestRequest();
        request.setReason(command.getReason());
        
        return commandService.cancelTransportRequest(command.getTransportRequestId(), request);
    }

    @Override
    public Class<CancelTransportRequestCommand> getCommandType() {
        return CancelTransportRequestCommand.class;
    }
}
