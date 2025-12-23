package com.limport.tms.infrastructure.adapter;

import com.limport.tms.application.service.interfaces.IDomainEventService;
import com.limport.tms.domain.event.states.TransportRouteOptimizedEvent;
import com.limport.tms.domain.ports.IRouteValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Adapter implementing route validation and optimization.
 * Currently uses simple stub logic; will integrate with external routing service.
 */
@Component
public class RouteOptimizationAdapter implements IRouteValidator {
    
    private static final Logger log = LoggerFactory.getLogger(RouteOptimizationAdapter.class);
    private static final String ALGORITHM = "STUB_DIRECT_ROUTE";
    
    // Stub: Simple distance estimation (in real system, call routing API)
    private static final Map<String, Map<String, Double>> DISTANCE_MATRIX = Map.of(
        "JNB", Map.of("CPT", 1400.0, "DBN", 600.0, "PLZ", 1600.0),
        "CPT", Map.of("JNB", 1400.0, "DBN", 1650.0, "PLZ", 1350.0),
        "DBN", Map.of("JNB", 600.0, "CPT", 1650.0, "PLZ", 1800.0),
        "PLZ", Map.of("JNB", 1600.0, "CPT", 1350.0, "DBN", 1800.0)
    );
    
    private final IDomainEventService eventService;
    
    public RouteOptimizationAdapter(IDomainEventService eventService) {
        this.eventService = eventService;
    }
    
    @Override
    public RouteOptimizationResult optimizeRoute(
            UUID transportRequestId,
            String origin,
            String destination) {
        
        log.info("Optimizing route: {} -> {}", origin, destination);
        
        if (!isRouteFeasible(origin, destination)) {
            return RouteOptimizationResult.infeasible(
                String.format("No route available between %s and %s", origin, destination)
            );
        }
        
        // Stub: Simple direct route
        List<String> waypoints = List.of(origin, destination);
        double distanceKm = calculateDistance(origin, destination);
        long durationMinutes = (long) (distanceKm / 80.0 * 60); // Assume 80 km/h average
        
        // Publish route optimized event
        TransportRouteOptimizedEvent event = new TransportRouteOptimizedEvent(
            transportRequestId,
            "system",  // userId - should come from security context
            waypoints,
            distanceKm,
            durationMinutes,
            ALGORITHM
        );
        
        eventService.publishToOutbox(event, "TransportRequest", transportRequestId.toString());
        
        return RouteOptimizationResult.success(
            waypoints, 
            distanceKm, 
            durationMinutes,
            ALGORITHM
        );
    }
    
    @Override
    public boolean isRouteFeasible(String origin, String destination) {
        return DISTANCE_MATRIX.containsKey(origin) 
            && DISTANCE_MATRIX.get(origin).containsKey(destination);
    }
    
    private double calculateDistance(String origin, String destination) {
        return DISTANCE_MATRIX
            .getOrDefault(origin, Map.of())
            .getOrDefault(destination, 0.0);
    }
}
