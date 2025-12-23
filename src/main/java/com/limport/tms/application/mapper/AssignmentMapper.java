package com.limport.tms.application.mapper;

import com.limport.tms.application.dto.response.AssignmentResponse;
import com.limport.tms.domain.model.entity.Assignment;
import org.springframework.stereotype.Component;

/**
 * Mapper for Assignment entity to DTOs.
 */
@Component
public class AssignmentMapper {

    public AssignmentResponse toResponse(Assignment assignment) {
        if (assignment == null) {
            return null;
        }

        return new AssignmentResponse(
            assignment.getId(),
            assignment.getTransportRequestId(),
            assignment.getProviderId(),
            assignment.getVehicleId(),
            assignment.getScheduledPickupTime(),
            assignment.getEstimatedDeliveryTime(),
            assignment.getAssignmentNotes(),
            assignment.getStatus() != null ? assignment.getStatus().name() : null,
            assignment.getAssignedBy(),
            assignment.getAssignedAt(),
            assignment.getLastUpdatedAt()
        );
    }
}
