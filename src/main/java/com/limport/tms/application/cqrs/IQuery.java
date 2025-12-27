package com.limport.tms.application.cqrs;

/**
 * Base interface for all queries in CQRS pattern.
 * Queries represent intent to read state without side effects.
 * 
 * @param <R> The result type of this query
 */
public interface IQuery<R> {
    // Marker interface - implementations define query-specific fields
}
