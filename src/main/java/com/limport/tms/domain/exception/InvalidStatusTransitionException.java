package com.limport.tms.domain.exception;

import com.limport.tms.domain.model.enums.TransportRequestStatus;

/**
 * Exception thrown when an invalid status transition is attempted.
 */
public class InvalidStatusTransitionException extends DomainException {
    
    public InvalidStatusTransitionException(
            TransportRequestStatus from, 
            TransportRequestStatus to) {
        super(
            "INVALID_STATUS_TRANSITION",
            String.format("Cannot transition from %s to %s", from, to)
        );
    }
    
    public InvalidStatusTransitionException(
            TransportRequestStatus from, 
            TransportRequestStatus to, 
            String reason) {
        super(
            "INVALID_STATUS_TRANSITION",
            String.format("Cannot transition from %s to %s: %s", from, to, reason)
        );
    }
}
