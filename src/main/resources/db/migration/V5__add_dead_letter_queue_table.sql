-- Dead letter queue table for failed event processing
CREATE TABLE IF NOT EXISTS dead_letter_events (
    id BIGSERIAL PRIMARY KEY,
    event_id VARCHAR(255) NOT NULL,
    event_type VARCHAR(255) NOT NULL,
    event_payload TEXT NOT NULL,
    source VARCHAR(50) NOT NULL, -- 'OUTBOX' or 'INBOX'
    failure_reason TEXT,
    failure_count INTEGER NOT NULL DEFAULT 1,
    first_failed_at TIMESTAMP WITH TIME ZONE NOT NULL,
    last_failed_at TIMESTAMP WITH TIME ZONE NOT NULL,
    next_retry_at TIMESTAMP WITH TIME ZONE,
    processed_at TIMESTAMP WITH TIME ZONE,

    CONSTRAINT chk_source CHECK (source IN ('OUTBOX', 'INBOX'))
);

-- Indexes for dead_letter_events
CREATE INDEX idx_dead_letter_event_id ON dead_letter_events(event_id);
CREATE INDEX idx_dead_letter_event_type ON dead_letter_events(event_type);
CREATE INDEX idx_dead_letter_source ON dead_letter_events(source);
CREATE INDEX idx_dead_letter_next_retry ON dead_letter_events(next_retry_at) WHERE next_retry_at IS NOT NULL;
CREATE INDEX idx_dead_letter_unprocessed ON dead_letter_events(processed_at) WHERE processed_at IS NULL;
CREATE INDEX idx_dead_letter_failure_count ON dead_letter_events(failure_count);