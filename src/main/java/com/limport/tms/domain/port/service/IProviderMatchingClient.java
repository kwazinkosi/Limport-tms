package com.limport.tms.domain.port.service;

import java.util.List;
import java.util.UUID;

/**
 * Port for communicating with Provider Matching Service (PMS).
 * Infrastructure layer provides REST/gRPC implementation.
 * 
 * PMS owns:
 * - Provider/vehicle master data
 * - Capacity verification
 * - Provider matching algorithms
 * - Availability management
 */
public interface IProviderMatchingClient {
    
    /**
     * Verify if provider has sufficient capacity for transport request.
     * Delegates to PMS which is the authoritative source for capacity data.
     * 
     * @param providerId Provider to verify
     * @param requestId Transport request ID
     * @param requiredWeightKg Required capacity in kilograms
     * @return Capacity verification result from PMS
     */
    CapacityVerificationResponse verifyCapacity(UUID providerId, UUID requestId, double requiredWeightKg);
    
    /**
     * Get provider matching suggestions for a transport request.
     * PMS analyzes provider capabilities, availability, location, and pricing.
     * 
     * @param requestId Transport request ID
     * @param origin Origin location code
     * @param destination Destination location code
     * @param weightKg Total weight in kilograms
     * @param packages Number of packages
     * @return List of matching provider suggestions ordered by match score
     */
    List<ProviderMatchResponse> matchProviders(
        UUID requestId, 
        String origin, 
        String destination, 
        double weightKg,
        int packages
    );
    
    /**
     * Response from PMS capacity verification.
     */
    record CapacityVerificationResponse(
        boolean isSufficient,
        double availableCapacity,
        double requiredCapacity,
        String message
    ) {
        public static CapacityVerificationResponse unavailable(String reason) {
            return new CapacityVerificationResponse(false, 0, 0, reason);
        }
        
        public static CapacityVerificationResponse sufficient(double available, double required) {
            return new CapacityVerificationResponse(true, available, required, "Capacity available");
        }
        
        public static CapacityVerificationResponse insufficient(double available, double required) {
            return new CapacityVerificationResponse(false, available, required, "Insufficient capacity");
        }
    }
    
    /**
     * Provider match suggestion from PMS.
     */
    record ProviderMatchResponse(
        UUID providerId,
        String providerName,
        UUID suggestedVehicleId,
        String vehicleType,
        double matchScore,
        double estimatedCostZAR,
        double availableCapacityKg,
        String location
    ) {}
}
