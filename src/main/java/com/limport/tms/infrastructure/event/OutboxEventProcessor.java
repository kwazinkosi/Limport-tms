package com.limport.tms.infrastructure.event;

import com.limport.tms.domain.port.service.IOutboxEventProcessor;
import com.limport.tms.domain.port.repository.IOutboxEventRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled processor that polls the outbox table and publishes events.
 * Implements the polling publisher pattern for reliable event delivery.
 */
@Component
public class OutboxEventProcessor {
    
    private static final Logger log = LoggerFactory.getLogger(OutboxEventProcessor.class);
    
    private final IOutboxEventProcessor outboxEventProcessor;
    private final IOutboxEventRepository outboxRepository;
    private final EventProcessingMetrics metrics;
    
    private final EventProcessingProperties eventProcessingProperties;
    
    @Value("${tms.outbox.enabled:true}")
    private boolean enabled;
    
    public OutboxEventProcessor(
            IOutboxEventProcessor outboxEventProcessor,
            IOutboxEventRepository outboxRepository,
            EventProcessingMetrics metrics,
            EventProcessingProperties eventProcessingProperties) {
        this.outboxEventProcessor = outboxEventProcessor;
        this.outboxRepository = outboxRepository;
        this.metrics = metrics;
        this.eventProcessingProperties = eventProcessingProperties;
    }
    
    /**
     * Polls the outbox and publishes pending events.
     * Runs every second by default (configurable).
     */
    @Scheduled(fixedDelayString = "#{@eventProcessingProperties.getOutboxPollIntervalMs()}")
    public void processOutbox() {
        if (!enabled) {
            return;
        }
        
        try {
            int batchSize = eventProcessingProperties.getOutboxBatchSize();
            int processed = outboxEventProcessor.processPendingEvents(batchSize);
            if (processed > 0) {
                log.debug("Outbox processor published {} events", processed);
            }
            
            // Update queue size metrics
            long pendingCount = outboxRepository.countPendingEvents();
            metrics.updateOutboxQueueSize(pendingCount);
            
        } catch (Exception e) {
            log.error("Error processing outbox: {}", e.getMessage(), e);
        }
    }
}
