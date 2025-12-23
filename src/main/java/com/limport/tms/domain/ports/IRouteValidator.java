package com.limport.tms.domain.ports;

import java.util.List;
import java.util.UUID;

/**
 * Domain port for route validation and optimization.
 * Validates routes and suggests optimal paths.
 */
public interface IRouteValidator {
    
    /**
     * Validates and optimizes a route for a transport request.
     * 
     * @param transportRequestId The transport request
     * @param origin Origin location code
     * @param destination Destination location code
     * @return RouteOptimizationResult with optimal route details
     */
    RouteOptimizationResult optimizeRoute(
        UUID transportRequestId,
        String origin,
        String destination
    );
    
    /**
     * Validates if a route is feasible.
     * 
     * @param origin Origin location code
     * @param destination Destination location code
     * @return true if route is valid and feasible
     */
    boolean isRouteFeasible(String origin, String destination);
    
    /**
     * Result of route optimization.
     */
    class RouteOptimizationResult {
        private final boolean feasible;
        private final List<String> waypoints;
        private final double totalDistanceKm;
        private final long estimatedDurationMinutes;
        private final String algorithm;
        private final String message;
        
        public RouteOptimizationResult(
                boolean feasible,
                List<String> waypoints,
                double totalDistanceKm,
                long estimatedDurationMinutes,
                String algorithm,
                String message) {
            this.feasible = feasible;
            this.waypoints = waypoints;
            this.totalDistanceKm = totalDistanceKm;
            this.estimatedDurationMinutes = estimatedDurationMinutes;
            this.algorithm = algorithm;
            this.message = message;
        }
        
        public static RouteOptimizationResult success(
                List<String> waypoints, 
                double distance, 
                long duration,
                String algorithm) {
            return new RouteOptimizationResult(
                true, waypoints, distance, duration, algorithm, "Route optimized successfully"
            );
        }
        
        public static RouteOptimizationResult infeasible(String message) {
            return new RouteOptimizationResult(
                false, List.of(), 0, 0, "NONE", message
            );
        }
        
        public boolean isFeasible() { return feasible; }
        public List<String> getWaypoints() { return waypoints; }
        public double getTotalDistanceKm() { return totalDistanceKm; }
        public long getEstimatedDurationMinutes() { return estimatedDurationMinutes; }
        public String getAlgorithm() { return algorithm; }
        public String getMessage() { return message; }
    }
}
