package com.limport.tms.domain.event;

import org.slf4j.MDC;
import org.springframework.util.StringUtils;

import java.util.UUID;

/**
 * Utility class for managing correlation IDs and causation IDs across service boundaries.
 * Provides thread-local and MDC-based ID propagation for distributed tracing.
 */
public final class CorrelationIdContext {

    // MDC keys
    private static final String CORRELATION_ID_KEY = "correlationId";
    private static final String CAUSATION_ID_KEY = "causationId";

    // Thread-local storage
    private static final ThreadLocal<String> CORRELATION_ID_THREAD_LOCAL = new ThreadLocal<>();
    private static final ThreadLocal<String> CAUSATION_ID_THREAD_LOCAL = new ThreadLocal<>();

    private CorrelationIdContext() {
        // Utility class
    }

    /**
     * Gets the current correlation ID from MDC or thread-local storage.
     * If none exists, generates a new one.
     *
     * @return the correlation ID
     */
    public static String getOrGenerateCorrelationId() {
        // First, try MDC (most common for web requests)
        String correlationId = MDC.get(CORRELATION_ID_KEY);
        if (StringUtils.hasText(correlationId)) {
            return correlationId;
        }

        // Second, try thread-local
        correlationId = CORRELATION_ID_THREAD_LOCAL.get();
        if (StringUtils.hasText(correlationId)) {
            return correlationId;
        }

        // Generate new correlation ID
        correlationId = UUID.randomUUID().toString();
        setCorrelationId(correlationId);
        return correlationId;
    }

    /**
     * Gets the current causation ID from MDC or thread-local storage.
     * Returns null if none exists (for root events).
     *
     * @return the causation ID or null
     */
    public static String getCausationId() {
        // First, try MDC
        String causationId = MDC.get(CAUSATION_ID_KEY);
        if (StringUtils.hasText(causationId)) {
            return causationId;
        }

        // Second, try thread-local
        return CAUSATION_ID_THREAD_LOCAL.get();
    }

    /**
     * Sets the correlation ID in both MDC and thread-local storage.
     *
     * @param correlationId the correlation ID to set
     */
    public static void setCorrelationId(String correlationId) {
        if (StringUtils.hasText(correlationId)) {
            MDC.put(CORRELATION_ID_KEY, correlationId);
            CORRELATION_ID_THREAD_LOCAL.set(correlationId);
        }
    }

    /**
     * Sets the causation ID in both MDC and thread-local storage.
     * Typically set when processing an incoming event that causes other events.
     *
     * @param causationId the causation ID to set
     */
    public static void setCausationId(String causationId) {
        if (StringUtils.hasText(causationId)) {
            MDC.put(CAUSATION_ID_KEY, causationId);
            CAUSATION_ID_THREAD_LOCAL.set(causationId);
        }
    }

    /**
     * Sets both correlation and causation IDs.
     * Useful when processing incoming events.
     *
     * @param correlationId the correlation ID
     * @param causationId the causation ID
     */
    public static void setIds(String correlationId, String causationId) {
        setCorrelationId(correlationId);
        setCausationId(causationId);
    }

    /**
     * Clears all correlation and causation IDs from both MDC and thread-local storage.
     */
    public static void clear() {
        MDC.remove(CORRELATION_ID_KEY);
        MDC.remove(CAUSATION_ID_KEY);
        CORRELATION_ID_THREAD_LOCAL.remove();
        CAUSATION_ID_THREAD_LOCAL.remove();
    }

    /**
     * Gets the current correlation ID without generating a new one if none exists.
     *
     * @return the correlation ID or null if none exists
     */
    public static String getCorrelationId() {
        String correlationId = MDC.get(CORRELATION_ID_KEY);
        if (StringUtils.hasText(correlationId)) {
            return correlationId;
        }
        return CORRELATION_ID_THREAD_LOCAL.get();
    }
}