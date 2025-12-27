package com.limport.tms.infrastructure.cqrs;

import com.limport.tms.application.cqrs.IQuery;
import com.limport.tms.application.cqrs.IQueryBus;
import com.limport.tms.application.cqrs.IQueryHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Spring-based implementation of the Query Bus.
 * Automatically discovers and registers all IQueryHandler beans.
 */
@Component
public class QueryBus implements IQueryBus {
    
    private static final Logger log = LoggerFactory.getLogger(QueryBus.class);
    
    private final Map<Class<?>, IQueryHandler<?, ?>> handlers = new HashMap<>();
    
    @SuppressWarnings("rawtypes")
    public QueryBus(List<IQueryHandler> queryHandlers) {
        for (IQueryHandler handler : queryHandlers) {
            Class<?> queryType = handler.getQueryType();
            if (handlers.containsKey(queryType)) {
                throw new IllegalStateException(
                    "Duplicate query handler for " + queryType.getSimpleName());
            }
            handlers.put(queryType, handler);
            log.info("Registered query handler: {} -> {}", 
                queryType.getSimpleName(), handler.getClass().getSimpleName());
        }
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <R> R dispatch(IQuery<R> query) {
        IQueryHandler<IQuery<R>, R> handler = 
            (IQueryHandler<IQuery<R>, R>) handlers.get(query.getClass());
        
        if (handler == null) {
            throw new IllegalArgumentException(
                "No handler found for query: " + query.getClass().getSimpleName());
        }
        
        log.debug("Dispatching query {} to handler {}", 
            query.getClass().getSimpleName(), handler.getClass().getSimpleName());
        
        return handler.handle(query);
    }
}
