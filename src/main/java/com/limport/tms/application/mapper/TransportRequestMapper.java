package com.limport.tms.application.mapper;

import com.limport.tms.application.command.CreateTransportRequestCommand;
import com.limport.tms.application.dto.request.CreateTransportRequest;
import com.limport.tms.application.dto.response.TransportRequestResponse;
import com.limport.tms.domain.model.entity.TransportRequest;

/**
 * Simple mapper between TransportRequest domain objects, commands and DTOs.
 */
public class TransportRequestMapper {

    public CreateTransportRequestCommand toCommand(CreateTransportRequest request) {
        CreateTransportRequestCommand command = new CreateTransportRequestCommand();
        command.setCustomerId(request.getCustomerId());
        command.setOriginLocationCode(request.getOriginLocationCode());
        command.setDestinationLocationCode(request.getDestinationLocationCode());
        command.setPickupFrom(request.getPickupFrom());
        command.setDeliveryUntil(request.getDeliveryUntil());
        command.setTotalWeight(request.getTotalWeight());
        command.setTotalPackages(request.getTotalPackages());
        command.setNotes(request.getNotes());
        return command;
    }

    public TransportRequestResponse toResponse(TransportRequest transportRequest) {
        TransportRequestResponse response = new TransportRequestResponse();
        response.setId(transportRequest.getId());
        response.setReference(transportRequest.getReference());
        response.setCustomerId(transportRequest.getCustomerId());
        response.setOriginLocationCode(transportRequest.getOriginLocationCode());
        response.setDestinationLocationCode(transportRequest.getDestinationLocationCode());
        response.setPickupFrom(transportRequest.getPickupFrom());
        response.setPickupUntil(transportRequest.getPickupUntil());
        response.setDeliveryFrom(transportRequest.getDeliveryFrom());
        response.setDeliveryUntil(transportRequest.getDeliveryUntil());
        response.setTotalWeight(transportRequest.getTotalWeight());
        response.setTotalPackages(transportRequest.getTotalPackages());
        response.setStatus(transportRequest.getStatus());
        response.setCreatedAt(transportRequest.getCreatedAt());
        response.setLastUpdatedAt(transportRequest.getLastUpdatedAt());
        return response;
    }
}
