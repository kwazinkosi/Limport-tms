package com.limport.tms.domain.model.entity;

import com.limport.tms.domain.model.enums.TransportRequestStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Core domain entity representing a transport request in the TMS.
 *
 * This models the information needed to drive the main UI flows
 * (dashboard, detail view, creation wizard, and tracking).
 */
public class TransportRequest {

    private UUID id;
    private String reference;
    private String customerId;

    private String originLocationCode;
    private String destinationLocationCode;

    private LocalDateTime pickupFrom;
    private LocalDateTime pickupUntil;
    private LocalDateTime deliveryFrom;
    private LocalDateTime deliveryUntil;

    private BigDecimal totalWeight;
    private Integer totalPackages;

    private TransportRequestStatus status;

    private Instant createdAt;
    private Instant lastUpdatedAt;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

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

    public LocalDateTime getPickupUntil() {
        return pickupUntil;
    }

    public void setPickupUntil(LocalDateTime pickupUntil) {
        this.pickupUntil = pickupUntil;
    }

    public LocalDateTime getDeliveryFrom() {
        return deliveryFrom;
    }

    public void setDeliveryFrom(LocalDateTime deliveryFrom) {
        this.deliveryFrom = deliveryFrom;
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

    public TransportRequestStatus getStatus() {
        return status;
    }

    public void setStatus(TransportRequestStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getLastUpdatedAt() {
        return lastUpdatedAt;
    }

    public void setLastUpdatedAt(Instant lastUpdatedAt) {
        this.lastUpdatedAt = lastUpdatedAt;
    }
}
