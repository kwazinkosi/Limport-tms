package com.limport.tms.domain.model.entity;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain entity representing a provider suggestion from PMS.
 * Stores matching results for user selection in TMS.
 *
 * Business Rules:
 * - Suggestions are created when PMS matches providers to transport requests
 * - Users can select from multiple suggestions
 * - Suggestions expire or become invalid when request status changes
 */
public class ProviderSuggestion {

    private UUID id;
    private UUID transportRequestId;
    private UUID providerId;
    private String providerName;
    private UUID vehicleId;
    private String vehicleType;
    private double matchScore;
    private double estimatedCostZAR;
    private double availableCapacityKg;
    private Instant suggestedAt;
    private SuggestionStatus status;

    // Default constructor for frameworks
    public ProviderSuggestion() {}

    // Builder pattern for clean creation
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private UUID transportRequestId;
        private UUID providerId;
        private String providerName;
        private UUID vehicleId;
        private String vehicleType;
        private double matchScore;
        private double estimatedCostZAR;
        private double availableCapacityKg;
        private Instant suggestedAt;
        private SuggestionStatus status;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder transportRequestId(UUID transportRequestId) {
            this.transportRequestId = transportRequestId;
            return this;
        }

        public Builder providerId(UUID providerId) {
            this.providerId = providerId;
            return this;
        }

        public Builder providerName(String providerName) {
            this.providerName = providerName;
            return this;
        }

        public Builder vehicleId(UUID vehicleId) {
            this.vehicleId = vehicleId;
            return this;
        }

        public Builder vehicleType(String vehicleType) {
            this.vehicleType = vehicleType;
            return this;
        }

        public Builder matchScore(double matchScore) {
            this.matchScore = matchScore;
            return this;
        }

        public Builder estimatedCostZAR(double estimatedCostZAR) {
            this.estimatedCostZAR = estimatedCostZAR;
            return this;
        }

        public Builder availableCapacityKg(double availableCapacityKg) {
            this.availableCapacityKg = availableCapacityKg;
            return this;
        }

        public Builder suggestedAt(Instant suggestedAt) {
            this.suggestedAt = suggestedAt;
            return this;
        }

        public Builder status(SuggestionStatus status) {
            this.status = status;
            return this;
        }

        public ProviderSuggestion build() {
            ProviderSuggestion suggestion = new ProviderSuggestion();
            suggestion.id = this.id != null ? this.id : UUID.randomUUID();
            suggestion.transportRequestId = this.transportRequestId;
            suggestion.providerId = this.providerId;
            suggestion.providerName = this.providerName;
            suggestion.vehicleId = this.vehicleId;
            suggestion.vehicleType = this.vehicleType;
            suggestion.matchScore = this.matchScore;
            suggestion.estimatedCostZAR = this.estimatedCostZAR;
            suggestion.availableCapacityKg = this.availableCapacityKg;
            suggestion.suggestedAt = this.suggestedAt;
            suggestion.status = this.status != null ? this.status : SuggestionStatus.ACTIVE;
            return suggestion;
        }
    }

    // Getters and setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getTransportRequestId() {
        return transportRequestId;
    }

    public void setTransportRequestId(UUID transportRequestId) {
        this.transportRequestId = transportRequestId;
    }

    public UUID getProviderId() {
        return providerId;
    }

    public void setProviderId(UUID providerId) {
        this.providerId = providerId;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public UUID getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(UUID vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public double getMatchScore() {
        return matchScore;
    }

    public void setMatchScore(double matchScore) {
        this.matchScore = matchScore;
    }

    public double getEstimatedCostZAR() {
        return estimatedCostZAR;
    }

    public void setEstimatedCostZAR(double estimatedCostZAR) {
        this.estimatedCostZAR = estimatedCostZAR;
    }

    public double getAvailableCapacityKg() {
        return availableCapacityKg;
    }

    public void setAvailableCapacityKg(double availableCapacityKg) {
        this.availableCapacityKg = availableCapacityKg;
    }

    public Instant getSuggestedAt() {
        return suggestedAt;
    }

    public void setSuggestedAt(Instant suggestedAt) {
        this.suggestedAt = suggestedAt;
    }

    public SuggestionStatus getStatus() {
        return status;
    }

    public void setStatus(SuggestionStatus status) {
        this.status = status;
    }

    /**
     * Status enum for provider suggestion lifecycle.
     */
    public enum SuggestionStatus {
        ACTIVE,     // Available for selection
        EXPIRED,    // No longer valid (e.g., capacity changed)
        SELECTED,   // Chosen by user
        REJECTED    // Explicitly rejected
    }

    @Override
    public String toString() {
        return "ProviderSuggestion{" +
                "id=" + id +
                ", transportRequestId=" + transportRequestId +
                ", providerId=" + providerId +
                ", providerName='" + providerName + '\'' +
                ", matchScore=" + matchScore +
                ", status=" + status +
                '}';
    }
}
