package com.limport.tms.infrastructure.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Jackson ObjectMapper configuration for event serialization.
 *
 * Provides a shared ObjectMapper bean with consistent configuration
 * for both domain events and external events.
 */
@Configuration
public class JacksonConfig {

    /**
     * Shared ObjectMapper bean for event serialization/deserialization.
     *
     * Configured with:
     * - JavaTimeModule for proper Instant/LocalDateTime handling
     * - Disabled timestamps (use ISO format)
     * - Ignore unknown properties during deserialization
     */
    @Bean
    public ObjectMapper eventObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // Java 8 time support
        mapper.registerModule(new JavaTimeModule());

        // Date/time serialization
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Deserialization
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        return mapper;
    }
}