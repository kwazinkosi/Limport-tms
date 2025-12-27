package com.limport.tms.infrastructure.event;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for event processing.
 * Consolidates outbox and inbox settings with sensible defaults and overrides.
 */
@Component
@ConfigurationProperties(prefix = "tms.event-processing")
public class EventProcessingProperties {

    private int pollIntervalMs = 1000;
    private int batchSize = 50;

    private OutboxProperties outbox = new OutboxProperties();
    private InboxProperties inbox = new InboxProperties();

    public int getPollIntervalMs() {
        return pollIntervalMs;
    }

    public void setPollIntervalMs(int pollIntervalMs) {
        this.pollIntervalMs = pollIntervalMs;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public OutboxProperties getOutbox() {
        return outbox;
    }

    public void setOutbox(OutboxProperties outbox) {
        this.outbox = outbox;
    }

    public InboxProperties getInbox() {
        return inbox;
    }

    public void setInbox(InboxProperties inbox) {
        this.inbox = inbox;
    }

    /**
     * Get the effective poll interval for outbox processing.
     * Uses outbox-specific value if set, otherwise falls back to default.
     */
    public int getOutboxPollIntervalMs() {
        return outbox.getPollIntervalMs() != null ? outbox.getPollIntervalMs() : pollIntervalMs;
    }

    /**
     * Get the effective batch size for outbox processing.
     * Uses outbox-specific value if set, otherwise falls back to default.
     */
    public int getOutboxBatchSize() {
        return outbox.getBatchSize() != null ? outbox.getBatchSize() : batchSize;
    }

    /**
     * Get the effective poll interval for inbox processing.
     * Uses inbox-specific value if set, otherwise falls back to default.
     */
    public int getInboxPollIntervalMs() {
        return inbox.getPollIntervalMs() != null ? inbox.getPollIntervalMs() : pollIntervalMs;
    }

    /**
     * Get the effective batch size for inbox processing.
     * Uses inbox-specific value if set, otherwise falls back to default.
     */
    public int getInboxBatchSize() {
        return inbox.getBatchSize() != null ? inbox.getBatchSize() : batchSize;
    }

    public static class OutboxProperties {
        private Integer pollIntervalMs;
        private Integer batchSize;

        public Integer getPollIntervalMs() {
            return pollIntervalMs;
        }

        public void setPollIntervalMs(Integer pollIntervalMs) {
            this.pollIntervalMs = pollIntervalMs;
        }

        public Integer getBatchSize() {
            return batchSize;
        }

        public void setBatchSize(Integer batchSize) {
            this.batchSize = batchSize;
        }
    }

    public static class InboxProperties {
        private Integer pollIntervalMs;
        private Integer batchSize;

        public Integer getPollIntervalMs() {
            return pollIntervalMs;
        }

        public void setPollIntervalMs(Integer pollIntervalMs) {
            this.pollIntervalMs = pollIntervalMs;
        }

        public Integer getBatchSize() {
            return batchSize;
        }

        public void setBatchSize(Integer batchSize) {
            this.batchSize = batchSize;
        }
    }
}