-- Transport Request table - core aggregate
CREATE TABLE IF NOT EXISTS transport_requests (
    id UUID PRIMARY KEY,
    reference VARCHAR(50) NOT NULL UNIQUE,
    customer_id VARCHAR(100) NOT NULL,
    
    origin_location_code VARCHAR(50) NOT NULL,
    destination_location_code VARCHAR(50) NOT NULL,
    
    pickup_from TIMESTAMP NOT NULL,
    pickup_until TIMESTAMP NOT NULL,
    delivery_from TIMESTAMP NOT NULL,
    delivery_until TIMESTAMP NOT NULL,
    
    total_weight DECIMAL(10, 2),
    total_packages INTEGER,
    
    status VARCHAR(20) NOT NULL DEFAULT 'REQUESTED',
    
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    last_updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    
    CONSTRAINT chk_transport_status CHECK (status IN ('REQUESTED', 'PLANNED', 'IN_TRANSIT', 'COMPLETED', 'CANCELLED'))
);

-- Indexes for transport_requests
CREATE INDEX idx_transport_requests_customer ON transport_requests(customer_id);
CREATE INDEX idx_transport_requests_status ON transport_requests(status);
CREATE INDEX idx_transport_requests_reference ON transport_requests(reference);
CREATE INDEX idx_transport_requests_created ON transport_requests(created_at DESC);

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

-- Indexes for outbox_events
CREATE INDEX idx_outbox_status_occurred ON outbox_events(status, occurred_on) 
    WHERE status = 'PENDING';
CREATE INDEX idx_outbox_processed_at ON outbox_events(processed_at) 
    WHERE status = 'PROCESSED';
CREATE INDEX idx_outbox_aggregate ON outbox_events(aggregate_type, aggregate_id);
