package com.limport.tms.domain.exception;

import java.util.UUID;

/**
 * Exception thrown when a transport request is not found.
 */
public class TransportRequestNotFoundException extends DomainException {
    
    public TransportRequestNotFoundException(UUID id) {
        super(
            "TRANSPORT_REQUEST_NOT_FOUND",
            String.format("Transport request with ID %s not found", id)
        );
    }
    
    public TransportRequestNotFoundException(String reference) {
        super(
            "TRANSPORT_REQUEST_NOT_FOUND",
            String.format("Transport request with reference %s not found", reference)
        );
    }
}
