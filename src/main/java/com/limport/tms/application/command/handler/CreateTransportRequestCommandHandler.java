package com.limport.tms.application.command.handler;

import com.limport.tms.application.command.CreateTransportRequestCommand;
import com.limport.tms.application.cqrs.ICommandHandler;
import com.limport.tms.application.dto.request.CreateTransportRequest;
import com.limport.tms.application.dto.response.TransportRequestResponse;
import com.limport.tms.application.service.interfaces.ITransportRequestCommandService;
import org.springframework.stereotype.Component;

/**
 * Handler for CreateTransportRequestCommand.
 * Orchestrates the creation of a new transport request.
 */
@Component
public class CreateTransportRequestCommandHandler 
        implements ICommandHandler<CreateTransportRequestCommand, TransportRequestResponse> {

    private final ITransportRequestCommandService commandService;

    public CreateTransportRequestCommandHandler(ITransportRequestCommandService commandService) {
        this.commandService = commandService;
    }

    @Override
    public TransportRequestResponse handle(CreateTransportRequestCommand command) {
        CreateTransportRequest request = new CreateTransportRequest();
        request.setCustomerId(command.getCustomerId());
        request.setOriginLocationCode(command.getOriginLocationCode());
        request.setDestinationLocationCode(command.getDestinationLocationCode());
        request.setPickupFrom(command.getPickupFrom());
        request.setDeliveryUntil(command.getDeliveryUntil());
        request.setTotalWeight(command.getTotalWeight());
        request.setTotalPackages(command.getTotalPackages());
        request.setNotes(command.getNotes());
        
        return commandService.createTransportRequest(request);
    }

    @Override
    public Class<CreateTransportRequestCommand> getCommandType() {
        return CreateTransportRequestCommand.class;
    }
}
