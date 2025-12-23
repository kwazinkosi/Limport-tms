package com.limport.tms.domain.exception;

/**
 * Exception thrown when insufficient capacity is available.
 */
public class InsufficientCapacityException extends DomainException {
    
    public InsufficientCapacityException(double required, double available) {
        super(
            "INSUFFICIENT_CAPACITY",
            String.format("Insufficient capacity: required %.2fkg, available %.2fkg", 
                required, available)
        );
    }
}
