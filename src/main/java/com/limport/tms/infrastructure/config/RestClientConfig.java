package com.limport.tms.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Configuration for REST clients used by TMS to communicate with external services.
 */
@Configuration
public class RestClientConfig {
    
    /**
     * RestClient builder with common configuration for all outbound HTTP calls.
     */
    @Bean
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder()
            // Add common headers, interceptors, error handlers here
            .defaultHeader("Content-Type", "application/json")
            .defaultHeader("User-Agent", "TMS/1.0");
    }
}
