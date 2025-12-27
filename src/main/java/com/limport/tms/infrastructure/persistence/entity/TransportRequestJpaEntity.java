package com.limport.tms.infrastructure.persistence.entity;

import com.limport.tms.domain.model.enums.TransportRequestStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA entity for persisting TransportRequest aggregate.
 * Maps to the transport_requests table.
 */
@Entity
@Table(name = "transport_requests")
public class TransportRequestJpaEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "reference", nullable = false, unique = true, length = 50)
    private String reference;

    @Column(name = "customer_id", nullable = false, length = 100)
    private String customerId;

    @Column(name = "origin_location_code", nullable = false, length = 50)
    private String originLocationCode;

    @Column(name = "destination_location_code", nullable = false, length = 50)
    private String destinationLocationCode;

    @Column(name = "pickup_from", nullable = false)
    private LocalDateTime pickupFrom;

    @Column(name = "pickup_until", nullable = false)
    private LocalDateTime pickupUntil;

    @Column(name = "delivery_from", nullable = false)
    private LocalDateTime deliveryFrom;

    @Column(name = "delivery_until", nullable = false)
    private LocalDateTime deliveryUntil;

    @Column(name = "total_weight", precision = 10, scale = 2)
    private BigDecimal totalWeight;

    @Column(name = "total_packages")
    private Integer totalPackages;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TransportRequestStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "last_updated_at", nullable = false)
    private Instant lastUpdatedAt;

    // JPA requires no-arg constructor
    protected TransportRequestJpaEntity() {
    }

    public TransportRequestJpaEntity(UUID id, String reference, String customerId,
                                     String originLocationCode, String destinationLocationCode,
                                     LocalDateTime pickupFrom, LocalDateTime pickupUntil,
                                     LocalDateTime deliveryFrom, LocalDateTime deliveryUntil,
                                     BigDecimal totalWeight, Integer totalPackages,
                                     TransportRequestStatus status, Instant createdAt, Instant lastUpdatedAt) {
        this.id = id;
        this.reference = reference;
        this.customerId = customerId;
        this.originLocationCode = originLocationCode;
        this.destinationLocationCode = destinationLocationCode;
        this.pickupFrom = pickupFrom;
        this.pickupUntil = pickupUntil;
        this.deliveryFrom = deliveryFrom;
        this.deliveryUntil = deliveryUntil;
        this.totalWeight = totalWeight;
        this.totalPackages = totalPackages;
        this.status = status;
        this.createdAt = createdAt;
        this.lastUpdatedAt = lastUpdatedAt;
    }

    // Getters and setters for JPA

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
