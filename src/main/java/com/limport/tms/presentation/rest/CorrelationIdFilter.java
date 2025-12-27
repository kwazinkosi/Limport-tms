package com.limport.tms.presentation.rest;

import com.limport.tms.domain.event.CorrelationIdContext;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;

/**
 * Servlet filter that extracts correlation ID from HTTP request headers
 * and sets it in the correlation context for distributed tracing.
 */
@Component
@Order(1) // Execute early in the filter chain
public class CorrelationIdFilter implements Filter {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String CORRELATION_ID_HEADER_ALT = "correlation-id";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (request instanceof HttpServletRequest httpRequest) {
            String correlationId = extractCorrelationId(httpRequest);
            if (StringUtils.hasText(correlationId)) {
                CorrelationIdContext.setCorrelationId(correlationId);
            }
        }

        try {
            chain.doFilter(request, response);
        } finally {
            // Clear the correlation ID after request processing
            CorrelationIdContext.clear();
        }
    }

    /**
     * Extracts correlation ID from request headers.
     * Checks multiple common header names.
     */
    private String extractCorrelationId(HttpServletRequest request) {
        // Check primary header
        String correlationId = request.getHeader(CORRELATION_ID_HEADER);
        if (StringUtils.hasText(correlationId)) {
            return correlationId;
        }

        // Check alternative header
        correlationId = request.getHeader(CORRELATION_ID_HEADER_ALT);
        if (StringUtils.hasText(correlationId)) {
            return correlationId;
        }

        // Could also check query parameters or other sources
        return null;
    }
}