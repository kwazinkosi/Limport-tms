package com.limport.tms.domain.port.repository;

import com.limport.tms.domain.model.entity.ProviderSuggestion;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository port for ProviderSuggestion persistence.
 * Part of the domain layer - defines the contract for infrastructure implementations.
 */
public interface IProviderSuggestionRepository {

    /**
     * Save a provider suggestion.
     */
    ProviderSuggestion save(ProviderSuggestion suggestion);

    /**
     * Save multiple provider suggestions.
     */
    void saveAll(List<ProviderSuggestion> suggestions);

    /**
     * Find suggestion by ID.
     */
    Optional<ProviderSuggestion> findById(UUID id);

    /**
     * Find all suggestions for a transport request.
     */
    List<ProviderSuggestion> findByTransportRequestId(UUID transportRequestId);

    /**
     * Find suggestions for a specific provider.
     */
    List<ProviderSuggestion> findByProviderId(UUID providerId);

    /**
     * Delete all suggestions for a transport request.
     */
    void deleteByTransportRequestId(UUID transportRequestId);

    /**
     * Check if a suggestion exists for a transport request and provider.
     */
    boolean existsByTransportRequestIdAndProviderId(UUID transportRequestId, UUID providerId);
}
