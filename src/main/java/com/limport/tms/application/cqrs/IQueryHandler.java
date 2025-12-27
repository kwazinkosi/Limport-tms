package com.limport.tms.application.cqrs;

/**
 * Handler for queries in CQRS pattern.
 * Each query type has exactly one handler responsible for its execution.
 * Query handlers must be side-effect free.
 * 
 * @param <Q> The query type
 * @param <R> The result type
 */
public interface IQueryHandler<Q extends IQuery<R>, R> {
    
    /**
     * Handle the query and return the result.
     * This method must be side-effect free.
     * 
     * @param query The query to handle
     * @return The result of the query
     */
    R handle(Q query);
    
    /**
     * Get the query class this handler supports.
     */
    Class<Q> getQueryType();
}
