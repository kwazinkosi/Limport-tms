package com.limport.tms.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Request payload for creating a new transport request.
 *
 * Designed to align with the three-step UI wizard:
 * 1) Location selection, 2) Item details, 3) Scheduling & review.
 */
public class CreateTransportRequest {

    // General & customer context
    @NotBlank
    private String customerId;

    // Step 1: Locations
    @NotBlank
    private String originLocationCode;

    @NotBlank
    private String destinationLocationCode;

    // Step 3: Scheduling window
    @NotNull
    private LocalDateTime pickupFrom;

    @NotNull
    private LocalDateTime deliveryUntil;

    // Step 2: Item details (aggregated for now)
    @Positive
    private BigDecimal totalWeight;

    @Positive
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

