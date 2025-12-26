package com.limport.tms.application.event.pms;

import com.limport.tms.application.event.ExternalEvent;

import com.limport.tms.domain.event.EventTypes;
import java.time.Instant;
import java.util.UUID;

/**
 * Event published by PMS when a provider accepts or rejects an assignment.
 * 
 * Received ASYNCHRONOUSLY via Kafka after TMS assigns a provider.
 * 
 * Flow:
 * 1. TMS assigns provider via REST (sync) → validates capacity
 * 2. TMS publishes TransportEvents.Request.Assigned → Kafka
 * 3. PMS notifies provider (email/SMS/app)
 * 4. Provider responds (accept/reject)
 * 5. PMS publishes ProviderEvents.AssignmentResponse → Kafka
 * 6. TMS consumes this event, updates status accordingly
 */
public record ProviderAssignmentResponseEvent(
    UUID eventId,
    Instant occurredOn,
    String sourceService,
    UUID transportRequestId,
    UUID assignmentId,
    UUID providerId,
    String providerName,
    AssignmentResponse response,
    String responseReason,
    Instant respondedAt) implements ExternalEvent {
    
    @Override
    public String eventType() {
        return EventTypes.Provider.ASSIGNMENT_RESPONSE;
    }
    
    public boolean isAccepted() {
        return response == AssignmentResponse.ACCEPTED;
    }
    
    public boolean isRejected() {
        return response == AssignmentResponse.REJECTED;
    }
    
    /**
     * Provider's response to assignment.
     */
    public enum AssignmentResponse {
        ACCEPTED,
        REJECTED,
        TIMEOUT  // Provider didn't respond in time
    }
}
