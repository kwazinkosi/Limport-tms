-- Outbox events table for reliable event publishing (transactional outbox pattern)
CREATE TABLE IF NOT EXISTS outbox_events (
    id UUID PRIMARY KEY,
    event_type VARCHAR(255) NOT NULL,
    aggregate_type VARCHAR(100) NOT NULL,
    aggregate_id VARCHAR(50) NOT NULL,
    payload TEXT NOT NULL,
    occurred_on TIMESTAMP WITH TIME ZONE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    retry_count INTEGER NOT NULL DEFAULT 0,
    processed_at TIMESTAMP WITH TIME ZONE,
    error_message VARCHAR(1000),
    
    CONSTRAINT chk_status CHECK (status IN ('PENDING', 'PROCESSED', 'FAILED'))
);

-- Index for polling pending events
CREATE INDEX IF NOT EXISTS idx_outbox_status_occurred ON outbox_events(status, occurred_on) 
    WHERE status = 'PENDING';

-- Index for cleanup job
CREATE INDEX IF NOT EXISTS idx_outbox_processed_at ON outbox_events(processed_at) 
    WHERE status = 'PROCESSED';

-- Index for aggregate lookups
CREATE INDEX IF NOT EXISTS idx_outbox_aggregate ON outbox_events(aggregate_type, aggregate_id);
