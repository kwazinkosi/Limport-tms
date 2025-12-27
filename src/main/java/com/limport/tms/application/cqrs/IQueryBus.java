package com.limport.tms.application.cqrs;

/**
 * Dispatches queries to their appropriate handlers.
 * Acts as a mediator in the CQRS pattern.
 */
public interface IQueryBus {
    
    /**
     * Dispatch a query to its handler and return the result.
     * 
     * @param query The query to dispatch
     * @param <R> The result type
     * @return The result of the query
     */
    <R> R dispatch(IQuery<R> query);
}
