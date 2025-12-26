package com.limport.tms.infrastructure.persistance.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity for tracking processed external events.
 * Used for idempotency checking to prevent duplicate event processing.
 */
@Entity
@Table(name = "processed_events")
public class ProcessedEventEntity {
    
    @Id
    @Column(name = "event_id", nullable = false, updatable = false)
    private UUID eventId;
    
    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;
    
    @Column(name = "source_service", length = 50)
    private String sourceService;
    
    @Column(name = "processed_at", nullable = false)
    private Instant processedAt;
    
    @Column(name = "handler_name", length = 100)
    private String handlerName;
    
    // Default constructor for JPA
    protected ProcessedEventEntity() {}
    
    public ProcessedEventEntity(UUID eventId, String eventType) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.processedAt = Instant.now();
    }
    
    public ProcessedEventEntity(UUID eventId, String eventType, String sourceService, String handlerName) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.sourceService = sourceService;
        this.handlerName = handlerName;
        this.processedAt = Instant.now();
    }
    
    // Getters
    public UUID getEventId() { return eventId; }
    public String getEventType() { return eventType; }
    public String getSourceService() { return sourceService; }
    public Instant getProcessedAt() { return processedAt; }
    public String getHandlerName() { return handlerName; }
    
    // Setters for optional fields
    public void setSourceService(String sourceService) { this.sourceService = sourceService; }
    public void setHandlerName(String handlerName) { this.handlerName = handlerName; }
}
