package com.limport.tms.infrastructure.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka configuration for event publishing.
 */
@Configuration
@EnableScheduling
public class KafkaEventConfig {
    
    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;
    
    @Value("${tms.kafka.topic-prefix:tms.events}")
    private String topicPrefix;
    
    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        
        // Enable idempotent producer for exactly-once semantics
        config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        config.put(ProducerConfig.ACKS_CONFIG, "all");
        config.put(ProducerConfig.RETRIES_CONFIG, 3);
        
        return new DefaultKafkaProducerFactory<>(config);
    }
    
    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
    
    // Topic definitions for TMS events
    @Bean
    public NewTopic requestCreatedTopic() {
        return TopicBuilder.name(topicPrefix + ".request-created")
            .partitions(3)
            .replicas(1)
            .build();
    }
    
    @Bean
    public NewTopic requestUpdatedTopic() {
        return TopicBuilder.name(topicPrefix + ".request-updated")
            .partitions(3)
            .replicas(1)
            .build();
    }
    
    @Bean
    public NewTopic requestCancelledTopic() {
        return TopicBuilder.name(topicPrefix + ".request-cancelled")
            .partitions(3)
            .replicas(1)
            .build();
    }
    
    @Bean
    public NewTopic requestAssignedTopic() {
        return TopicBuilder.name(topicPrefix + ".request-assigned")
            .partitions(3)
            .replicas(1)
            .build();
    }
    
    @Bean
    public NewTopic requestCompletedTopic() {
        return TopicBuilder.name(topicPrefix + ".request-completed")
            .partitions(3)
            .replicas(1)
            .build();
    }
    
    @Bean
    public NewTopic capacityVerifiedTopic() {
        return TopicBuilder.name(topicPrefix + ".capacity-verified")
            .partitions(3)
            .replicas(1)
            .build();
    }
    
    @Bean
    public NewTopic routeOptimizedTopic() {
        return TopicBuilder.name(topicPrefix + ".route-optimized")
            .partitions(3)
            .replicas(1)
            .build();
    }
}
