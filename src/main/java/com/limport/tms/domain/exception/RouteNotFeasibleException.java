package com.limport.tms.domain.exception;

/**
 * Exception thrown when a route is not feasible.
 */
public class RouteNotFeasibleException extends DomainException {
    
    public RouteNotFeasibleException(String origin, String destination) {
        super(
            "ROUTE_NOT_FEASIBLE",
            String.format("No feasible route between %s and %s", origin, destination)
        );
    }
    
    public RouteNotFeasibleException(String message) {
        super("ROUTE_NOT_FEASIBLE", message);
    }
}
