package com.limport.tms.infrastructure.adapter;

import com.limport.tms.domain.port.service.IProviderMatchingClient;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProviderMatchingClientAdapterTest {

    @Mock
    private RestClient.Builder restClientBuilder;

    @Mock
    private RestClient restClient;

    @Mock
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @Mock
    private RetryRegistry retryRegistry;

    private ProviderMatchingClientAdapter adapter;

    @BeforeEach
    void setUp() {
        when(restClientBuilder.baseUrl("http://localhost:8081")).thenReturn(restClientBuilder);
        when(restClientBuilder.build()).thenReturn(restClient);

        adapter = new ProviderMatchingClientAdapter(
            restClientBuilder,
            circuitBreakerRegistry,
            retryRegistry,
            "http://localhost:8081",
            false // stubMode = false
        );
    }

    @Test
    void verifyCapacity_StubMode_ReturnsStubResponse() {
        // Given
        ProviderMatchingClientAdapter stubAdapter = new ProviderMatchingClientAdapter(
            restClientBuilder,
            circuitBreakerRegistry,
            retryRegistry,
            "http://localhost:8081",
            true // stubMode = true
        );

        UUID providerId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID requestId = UUID.randomUUID();
        double requiredWeight = 1000.0;

        // When
        IProviderMatchingClient.CapacityVerificationResponse response =
            stubAdapter.verifyCapacity(providerId, requestId, requiredWeight);

        // Then
        assertThat(response.isSufficient()).isTrue();
        assertThat(response.availableCapacity()).isEqualTo(5000.0);
        assertThat(response.requiredCapacity()).isEqualTo(1000.0);
    }

    @Test
    void matchProviders_StubMode_ReturnsStubResponses() {
        // Given
        ProviderMatchingClientAdapter stubAdapter = new ProviderMatchingClientAdapter(
            restClientBuilder,
            circuitBreakerRegistry,
            retryRegistry,
            "http://localhost:8081",
            true // stubMode = true
        );

        UUID requestId = UUID.randomUUID();
        double weight = 2000.0;

        // When
        var responses = stubAdapter.matchProviders(requestId, "JNB", "CPT", weight, 5);

        // Then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).providerName()).isEqualTo("FastFreight SA");
        assertThat(responses.get(1).providerName()).isEqualTo("QuickTransport Ltd");
    }

    // TODO: Add integration tests with WireMock for real API calls when PMS is available
}