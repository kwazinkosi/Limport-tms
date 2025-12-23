package com.limport.tms.application.service.impl;

import com.limport.tms.application.dto.response.TransportRequestResponse;
import com.limport.tms.application.mapper.TransportRequestMapper;
import com.limport.tms.application.service.interfaces.ITransportRequestQueryService;
import com.limport.tms.domain.model.entity.TransportRequest;
import com.limport.tms.domain.model.enums.TransportRequestStatus;
import com.limport.tms.domain.ports.ITransportRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Application service handling transport request queries.
 */
@Service
@Transactional(readOnly = true)
public class TransportRequestQueryServiceImpl implements ITransportRequestQueryService {

    private final ITransportRequestRepository repository;
    private final TransportRequestMapper mapper;

    public TransportRequestQueryServiceImpl(
            ITransportRequestRepository repository,
            TransportRequestMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public TransportRequestResponse getById(UUID id) {
        TransportRequest transportRequest = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Transport request not found: " + id));
        return mapper.toResponse(transportRequest);
    }

    @Override
    public List<TransportRequestResponse> listAll() {
        return repository.findAll().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<TransportRequestResponse> listByStatus(TransportRequestStatus status) {
        return repository.findByStatus(status).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }
}
