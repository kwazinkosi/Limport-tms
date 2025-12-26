-- V4: Add external_event_inbox table for reliable event processing
-- This table implements the inbox pattern for external events, providing
-- the same reliability guarantees as the outbox pattern for domain events.

CREATE TABLE IF NOT EXISTS external_event_inbox (
    id UUID PRIMARY KEY,
    event_type VARCHAR(100) NOT NULL,
    payload TEXT NOT NULL,
    source_service VARCHAR(50),
    received_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'PROCESSED', 'FAILED')),
    retry_count INTEGER NOT NULL DEFAULT 0,
    processed_at TIMESTAMP WITH TIME ZONE,
    error_message TEXT,

    -- Index for finding pending events (FIFO order)
    CONSTRAINT external_event_inbox_id_unique UNIQUE (id)
);

-- Index for finding pending events efficiently
CREATE INDEX idx_external_event_inbox_status_received ON external_event_inbox(status, received_at) WHERE status = 'PENDING';

-- Index for finding failed events for monitoring
CREATE INDEX idx_external_event_inbox_status ON external_event_inbox(status);

-- Index for cleanup of processed events
CREATE INDEX idx_external_event_inbox_processed_at ON external_event_inbox(processed_at) WHERE status = 'PROCESSED';

-- Comment on table
COMMENT ON TABLE external_event_inbox IS 'Inbox for external events before processing. Implements reliable event processing with retry logic.';
COMMENT ON COLUMN external_event_inbox.id IS 'Unique identifier for inbox entry';
COMMENT ON COLUMN external_event_inbox.event_type IS 'Type of external event';
COMMENT ON COLUMN external_event_inbox.payload IS 'JSON payload of the event';
COMMENT ON COLUMN external_event_inbox.source_service IS 'Service that sent the event';
COMMENT ON COLUMN external_event_inbox.received_at IS 'Timestamp when event was received';
COMMENT ON COLUMN external_event_inbox.status IS 'Processing status: PENDING, PROCESSED, or FAILED';
COMMENT ON COLUMN external_event_inbox.retry_count IS 'Number of processing attempts';
COMMENT ON COLUMN external_event_inbox.processed_at IS 'Timestamp when event was successfully processed';
COMMENT ON COLUMN external_event_inbox.error_message IS 'Error message if processing failed';