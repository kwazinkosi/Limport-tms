package com.limport.tms.domain.service;

import com.limport.tms.domain.exception.InvalidStatusTransitionException;
import com.limport.tms.domain.model.enums.TransportRequestStatus;

import java.util.Map;
import java.util.Set;

/**
 * Domain service that validates transport request status transitions.
 * Implements a state machine to ensure only valid transitions occur.
 * 
 * Valid transitions:
 * - REQUESTED → PLANNED (after assignment)
 * - REQUESTED → CANCELLED (before planning)
 * - PLANNED → IN_TRANSIT (when shipment starts)
 * - PLANNED → CANCELLED (before departure)
 * - IN_TRANSIT → COMPLETED (successful delivery)
 * - IN_TRANSIT → CANCELLED (exceptional cases only)
 */
public class TransportRequestStateMachine {
    
    // Define valid transitions
    private static final Map<TransportRequestStatus, Set<TransportRequestStatus>> VALID_TRANSITIONS = Map.of(
        TransportRequestStatus.REQUESTED, Set.of(
            TransportRequestStatus.PLANNED,
            TransportRequestStatus.CANCELLED
        ),
        TransportRequestStatus.PLANNED, Set.of(
            TransportRequestStatus.IN_TRANSIT,
            TransportRequestStatus.CANCELLED
        ),
        TransportRequestStatus.IN_TRANSIT, Set.of(
            TransportRequestStatus.COMPLETED,
            TransportRequestStatus.CANCELLED  // Exceptional cases
        ),
        TransportRequestStatus.COMPLETED, Set.of(),  // Terminal state
        TransportRequestStatus.CANCELLED, Set.of()   // Terminal state
    );
    
    /**
     * Validates if a status transition is allowed.
     * 
     * @param from Current status
     * @param to Target status
     * @throws InvalidStatusTransitionException if transition is not valid
     */
    public static void validateTransition(TransportRequestStatus from, TransportRequestStatus to) {
        if (from == to) {
            return; // Same status is allowed (idempotent)
        }
        
        Set<TransportRequestStatus> allowedTargets = VALID_TRANSITIONS.get(from);
        
        if (allowedTargets == null || !allowedTargets.contains(to)) {
            throw new InvalidStatusTransitionException(from, to);
        }
    }
    
    /**
     * Checks if a status transition is valid without throwing exception.
     */
    public static boolean isValidTransition(TransportRequestStatus from, TransportRequestStatus to) {
        if (from == to) {
            return true;
        }
        
        Set<TransportRequestStatus> allowedTargets = VALID_TRANSITIONS.get(from);
        return allowedTargets != null && allowedTargets.contains(to);
    }
    
    /**
     * Gets all valid next states for a given status.
     */
    public static Set<TransportRequestStatus> getValidNextStates(TransportRequestStatus current) {
        return VALID_TRANSITIONS.getOrDefault(current, Set.of());
    }
    
    /**
     * Checks if a status is a terminal state (no further transitions allowed).
     */
    public static boolean isTerminalState(TransportRequestStatus status) {
        return status == TransportRequestStatus.COMPLETED 
            || status == TransportRequestStatus.CANCELLED;
    }
}
