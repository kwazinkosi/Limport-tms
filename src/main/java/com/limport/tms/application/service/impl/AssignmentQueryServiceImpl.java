package com.limport.tms.application.service.impl;

import com.limport.tms.application.dto.response.AssignmentResponse;
import com.limport.tms.application.mapper.AssignmentMapper;
import com.limport.tms.application.service.interfaces.IAssignmentQueryService;
import com.limport.tms.domain.model.entity.Assignment;
import com.limport.tms.domain.port.repository.IAssignmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Query service implementation for assignments.
 */
@Service
@Transactional(readOnly = true)
public class AssignmentQueryServiceImpl implements IAssignmentQueryService {

    private final IAssignmentRepository assignmentRepository;
    private final AssignmentMapper assignmentMapper;

    public AssignmentQueryServiceImpl(IAssignmentRepository assignmentRepository,
                                     AssignmentMapper assignmentMapper) {
        this.assignmentRepository = assignmentRepository;
        this.assignmentMapper = assignmentMapper;
    }

    @Override
    public AssignmentResponse getById(UUID id) {
        Assignment assignment = assignmentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Assignment not found: " + id));
        return assignmentMapper.toResponse(assignment);
    }

    @Override
    public List<AssignmentResponse> listByTransportRequest(UUID transportRequestId) {
        return assignmentRepository.findByTransportRequestId(transportRequestId)
            .stream()
            .map(assignmentMapper::toResponse)
            .collect(Collectors.toList());
    }

    @Override
    public List<AssignmentResponse> listByProvider(UUID providerId) {
        return assignmentRepository.findByProviderId(providerId)
            .stream()
            .map(assignmentMapper::toResponse)
            .collect(Collectors.toList());
    }

    @Override
    public AssignmentResponse getActiveAssignment(UUID transportRequestId) {
        List<Assignment> activeAssignments = 
            assignmentRepository.findActiveByTransportRequestId(transportRequestId);
        
        if (activeAssignments.isEmpty()) {
            return null;
        }
        
        // Return the most recent active assignment
        Assignment latest = activeAssignments.stream()
            .max((a1, a2) -> a1.getAssignedAt().compareTo(a2.getAssignedAt()))
            .orElse(null);
            
        return assignmentMapper.toResponse(latest);
    }
}
