-- Assignments table - tracks provider/vehicle assignments to transport requests
CREATE TABLE IF NOT EXISTS assignments (
    id UUID PRIMARY KEY,
    transport_request_id UUID NOT NULL,
    provider_id UUID NOT NULL,
    vehicle_id UUID NOT NULL,
    
    scheduled_pickup_time TIMESTAMP NOT NULL,
    estimated_delivery_time TIMESTAMP NOT NULL,
    
    assignment_notes TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'ASSIGNED',
    
    assigned_by VARCHAR(100) NOT NULL,
    assigned_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    last_updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    
    CONSTRAINT fk_assignment_transport_request 
        FOREIGN KEY (transport_request_id) 
        REFERENCES transport_requests(id) 
        ON DELETE CASCADE,
    
    CONSTRAINT chk_assignment_status 
        CHECK (status IN ('ASSIGNED', 'CONFIRMED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'))
);

-- Indexes for assignments
CREATE INDEX idx_assignments_transport_request ON assignments(transport_request_id);
CREATE INDEX idx_assignments_provider ON assignments(provider_id);
CREATE INDEX idx_assignments_vehicle ON assignments(vehicle_id);
CREATE INDEX idx_assignments_status ON assignments(status);
CREATE INDEX idx_assignments_assigned_at ON assignments(assigned_at DESC);

-- Composite index for common query patterns
CREATE INDEX idx_assignments_request_status ON assignments(transport_request_id, status);
