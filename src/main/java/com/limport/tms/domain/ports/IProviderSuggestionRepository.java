package com.limport.tms.domain.ports;

import com.limport.tms.domain.model.entity.ProviderSuggestion;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port for persisting and querying provider suggestions.
 *
 * Domain layer interface - infrastructure provides implementation.
 */
public interface IProviderSuggestionRepository {

    /**
     * Saves a provider suggestion.
     * @param suggestion The suggestion to save
     * @return The saved suggestion with generated ID if applicable
     */
    ProviderSuggestion save(ProviderSuggestion suggestion);

    /**
     * Finds a suggestion by its ID.
     * @param id The suggestion ID
     * @return Optional containing the suggestion if found
     */
    Optional<ProviderSuggestion> findById(UUID id);

    /**
     * Finds all active suggestions for a transport request.
     * @param transportRequestId The transport request ID
     * @return List of active suggestions ordered by match score (descending)
     */
    List<ProviderSuggestion> findActiveByTransportRequestId(UUID transportRequestId);

    /**
     * Finds all suggestions for a transport request (including inactive).
     * @param transportRequestId The transport request ID
     * @return List of all suggestions
     */
    List<ProviderSuggestion> findByTransportRequestId(UUID transportRequestId);

    /**
     * Checks if a suggestion already exists for the given transport request and provider.
     * Used for idempotency in event processing.
     *
     * @param transportRequestId The transport request ID
     * @param providerId The provider ID
     * @return true if suggestion exists, false otherwise
     */
    boolean existsByTransportRequestIdAndProviderId(UUID transportRequestId, UUID providerId);

    /**
     * Updates the status of a suggestion.
     * @param id The suggestion ID
     * @param status The new status
     * @return true if update was successful, false if suggestion not found
     */
    boolean updateStatus(UUID id, ProviderSuggestion.SuggestionStatus status);
}
