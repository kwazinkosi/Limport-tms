package com.limport.tms.infrastructure.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis configuration for TMS.
 * 
 * Spring Boot auto-configures:
 * - RedisConnectionFactory (from application.yml settings)
 * - StringRedisTemplate (for string key-value operations)
 * 
 * This config adds:
 * - RedisTemplate<String, Object> for JSON-serialized values (future caching needs)
 * 
 * Enabled when spring.data.redis.host is configured.
 * Individual features (event tracking, caching) have their own conditionals.
 */
@Configuration
@ConditionalOnProperty(name = "spring.data.redis.host")
public class RedisConfig {
    
    /**
     * RedisTemplate for storing JSON-serialized objects.
     * Useful for caching domain objects, session data, etc.
     * 
     * Note: StringRedisTemplate is auto-configured by Spring Boot
     * and used by RedisProcessedEventTracker for simple string operations.
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // Keys are always strings
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        
        // Values are JSON-serialized for readability and debugging
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer();
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);
        
        template.afterPropertiesSet();
        return template;
    }
}
