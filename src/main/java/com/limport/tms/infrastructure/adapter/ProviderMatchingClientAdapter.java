package com.limport.tms.infrastructure.adapter;

import com.limport.tms.domain.port.service.IProviderMatchingClient;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * REST client adapter for Provider Matching Service (PMS).
 * Implements communication with external PMS microservice with circuit breaker and retry logic.
 */
@Component
public class ProviderMatchingClientAdapter implements IProviderMatchingClient {
    
    private static final Logger log = LoggerFactory.getLogger(ProviderMatchingClientAdapter.class);
    
    private final RestClient restClient;
    private final boolean stubMode;
    private final CircuitBreaker circuitBreaker;
    private final Retry retry;

    public ProviderMatchingClientAdapter(
            RestClient.Builder restClientBuilder,
            CircuitBreakerRegistry circuitBreakerRegistry,
            RetryRegistry retryRegistry,
            @Value("${tms.pms.base-url:http://localhost:8081}") String pmsBaseUrl,
            @Value("${tms.pms.stub-mode:false}") boolean stubMode) {
        this.stubMode = stubMode;
        this.restClient = restClientBuilder.baseUrl(pmsBaseUrl).build();
        this.circuitBreaker = circuitBreakerRegistry.circuitBreaker("pms-client");
        this.retry = retryRegistry.retry("pms-client");
        
        if (stubMode) {
            log.warn("PMS client running in STUB MODE - using mock data instead of calling PMS");
        } else {
            log.info("PMS client initialized with base URL: {}", pmsBaseUrl);
        }
    }

    @Override
    public CapacityVerificationResponse verifyCapacity(UUID providerId, UUID requestId, double requiredWeightKg) {
        if (stubMode) {
            return verifyCapacityStub(providerId, requiredWeightKg);
        }
        
        Supplier<CapacityVerificationResponse> supplier = () -> {
            log.debug("Calling PMS to verify capacity for provider: {}", providerId);
            
            var response = restClient.get()
                .uri("/api/providers/{providerId}/capacity?requestId={requestId}&weight={weight}", 
                     providerId, requestId, requiredWeightKg)
                .retrieve()
                .body(Map.class);
            
            return new CapacityVerificationResponse(
                (Boolean) response.get("isSufficient"),
                ((Number) response.get("availableCapacity")).doubleValue(),
                ((Number) response.get("requiredCapacity")).doubleValue(),
                (String) response.get("message")
            );
        };
        
        try {
            return Retry.decorateSupplier(retry, CircuitBreaker.decorateSupplier(circuitBreaker, supplier)).get();
        } catch (Exception e) {
            log.error("Failed to verify capacity with PMS for provider {} after retries", providerId, e);
            // Fail safely: return insufficient capacity when PMS is unavailable
            return CapacityVerificationResponse.unavailable(
                "PMS unavailable - cannot verify capacity: " + e.getMessage()
            );
        }
    }

    @Override
    public List<ProviderMatchResponse> matchProviders(
            UUID requestId, 
            String origin, 
            String destination, 
            double weightKg,
            int packages) {
        
        if (stubMode) {
            return matchProvidersStub(weightKg);
        }
        
        Supplier<List<ProviderMatchResponse>> supplier = () -> {
            log.debug("Calling PMS to match providers for request: {}", requestId);
            
            Map<String, Object> request = Map.of(
                "requestId", requestId.toString(),
                "origin", origin,
                "destination", destination,
                "weight", weightKg,
                "packages", packages
            );
            
            List<Map<String, Object>> response = restClient.post()
                .uri("/api/providers/match")
                .body(request)
                .retrieve()
                .body(new ParameterizedTypeReference<List<Map<String, Object>>>() {});
            
            return response.stream()
                .map(this::mapToProviderMatchResponse)
                .toList();
        };
        
        try {
            return Retry.decorateSupplier(retry, CircuitBreaker.decorateSupplier(circuitBreaker, supplier)).get();
        } catch (Exception e) {
            log.error("Failed to match providers with PMS for request {} after retries", requestId, e);
            throw new RuntimeException("Provider matching service unavailable: " + e.getMessage(), e);
        }
    }
    
    // ========== STUB IMPLEMENTATIONS (for development/testing) ==========
    
    private CapacityVerificationResponse verifyCapacityStub(UUID providerId, double requiredWeightKg) {
        log.debug("STUB: Verifying capacity for provider {} - required: {}kg", providerId, requiredWeightKg);
        
        // Stub: Hardcoded provider capacities
        Map<String, Double> stubCapacities = Map.of(
            "00000000-0000-0000-0000-000000000001", 5000.0,
            "00000000-0000-0000-0000-000000000002", 10000.0,
            "00000000-0000-0000-0000-000000000003", 2000.0
        );
        
        double available = stubCapacities.getOrDefault(providerId.toString(), 0.0);
        boolean sufficient = available >= requiredWeightKg;
        
        return sufficient 
            ? CapacityVerificationResponse.sufficient(available, requiredWeightKg)
            : CapacityVerificationResponse.insufficient(available, requiredWeightKg);
    }
    
    private List<ProviderMatchResponse> matchProvidersStub(double weightKg) {
        log.debug("STUB: Matching providers for weight: {}kg", weightKg);
        
        return List.of(
            new ProviderMatchResponse(
                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                "FastFreight SA",
                UUID.fromString("10000000-0000-0000-0000-000000000001"),
                "10-Ton Truck",
                0.95,
                2500.0,
                5000.0,
                "JNB"
            ),
            new ProviderMatchResponse(
                UUID.fromString("00000000-0000-0000-0000-000000000002"),
                "QuickTransport Ltd",
                UUID.fromString("10000000-0000-0000-0000-000000000002"),
                "20-Ton Truck",
                0.88,
                3200.0,
                10000.0,
                "CPT"
            )
        );
    }
    
    private ProviderMatchResponse mapToProviderMatchResponse(Map<String, Object> map) {
        return new ProviderMatchResponse(
            UUID.fromString((String) map.get("providerId")),
            (String) map.get("providerName"),
            UUID.fromString((String) map.get("vehicleId")),
            (String) map.get("vehicleType"),
            ((Number) map.get("matchScore")).doubleValue(),
            ((Number) map.get("estimatedCostZAR")).doubleValue(),
            ((Number) map.get("availableCapacityKg")).doubleValue(),
            (String) map.get("location")
        );
    }
}
