package com.limport.tms.application.command;

import com.limport.tms.application.cqrs.ICommand;
import com.limport.tms.application.dto.response.TransportRequestResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Command representing the intent to create a new transport request.
 */
public class CreateTransportRequestCommand implements ICommand<TransportRequestResponse> {

    private String customerId;
    private String originLocationCode;
    private String destinationLocationCode;
    private LocalDateTime pickupFrom;
    private LocalDateTime deliveryUntil;
    private BigDecimal totalWeight;
    private Integer totalPackages;
    private String notes;

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getOriginLocationCode() {
        return originLocationCode;
    }

    public void setOriginLocationCode(String originLocationCode) {
        this.originLocationCode = originLocationCode;
    }

    public String getDestinationLocationCode() {
        return destinationLocationCode;
    }

    public void setDestinationLocationCode(String destinationLocationCode) {
        this.destinationLocationCode = destinationLocationCode;
    }

    public LocalDateTime getPickupFrom() {
        return pickupFrom;
    }

    public void setPickupFrom(LocalDateTime pickupFrom) {
        this.pickupFrom = pickupFrom;
    }

    public LocalDateTime getDeliveryUntil() {
        return deliveryUntil;
    }

    public void setDeliveryUntil(LocalDateTime deliveryUntil) {
        this.deliveryUntil = deliveryUntil;
    }

    public BigDecimal getTotalWeight() {
        return totalWeight;
    }

    public void setTotalWeight(BigDecimal totalWeight) {
        this.totalWeight = totalWeight;
    }

    public Integer getTotalPackages() {
        return totalPackages;
    }

    public void setTotalPackages(Integer totalPackages) {
        this.totalPackages = totalPackages;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
