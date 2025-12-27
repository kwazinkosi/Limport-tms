package com.limport.tms.domain.event.states;

import com.limport.tms.domain.event.TransportEvent;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Published when an optimal route for a transport request has been determined.
 * Provides crucial information for delivery planning and coordination.
 * 
 * Event Type: TMS.Transport.Route.Optimized
 */
public final class TransportRouteOptimizedEvent extends TransportEvent {

    private final List<String> waypoints;
    private final double totalDistanceKm;
    private final long estimatedDurationMinutes;
    private final Instant optimizedAt;
    private final String optimizationAlgorithm;

    public TransportRouteOptimizedEvent(
            UUID transportRequestId,
            String userId,
            List<String> waypoints,
            double totalDistanceKm,
            long estimatedDurationMinutes,
            String optimizationAlgorithm) {
        super(transportRequestId, userId);
        this.waypoints = waypoints != null ? List.copyOf(waypoints) : List.of();
        this.totalDistanceKm = totalDistanceKm;
        this.estimatedDurationMinutes = estimatedDurationMinutes;
        this.optimizedAt = Instant.now();
        this.optimizationAlgorithm = optimizationAlgorithm;
    }

    public List<String> getWaypoints() {
        return waypoints;
    }

    public double getTotalDistanceKm() {
        return totalDistanceKm;
    }

    public long getEstimatedDurationMinutes() {
        return estimatedDurationMinutes;
    }

    public Instant getOptimizedAt() {
        return optimizedAt;
    }

    public String getOptimizationAlgorithm() {
        return optimizationAlgorithm;
    }

    @Override
    public String eventCategory() {
        return "Route";
    }

    @Override
    protected String eventName() {
        return "Optimized";
    }
}
