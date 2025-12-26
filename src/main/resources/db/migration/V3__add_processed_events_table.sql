-- V3: Add processed_events table for idempotency tracking
-- This table tracks which external events have been processed to prevent duplicates.

CREATE TABLE IF NOT EXISTS processed_events (
    event_id UUID PRIMARY KEY,
    event_type VARCHAR(100) NOT NULL,
    source_service VARCHAR(50),
    processed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    handler_name VARCHAR(100),
    
    -- Index for cleanup queries
    CONSTRAINT processed_events_event_id_unique UNIQUE (event_id)
);

-- Index for finding events by type (useful for monitoring/debugging)
CREATE INDEX idx_processed_events_type ON processed_events(event_type);

-- Index for cleanup of old records
CREATE INDEX idx_processed_events_processed_at ON processed_events(processed_at);

-- Comment on table
COMMENT ON TABLE processed_events IS 'Tracks processed external events for idempotency. Events are kept for audit/debugging and cleaned up after retention period.';
COMMENT ON COLUMN processed_events.event_id IS 'Unique identifier of the external event';
COMMENT ON COLUMN processed_events.event_type IS 'Type of event (e.g., ProviderEvents.Matched)';
COMMENT ON COLUMN processed_events.source_service IS 'Service that published the event (e.g., PMS)';
COMMENT ON COLUMN processed_events.processed_at IS 'Timestamp when event was processed';
COMMENT ON COLUMN processed_events.handler_name IS 'Handler class that processed the event';
