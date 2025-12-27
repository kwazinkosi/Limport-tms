package com.limport.tms.infrastructure.cqrs;

import com.limport.tms.application.cqrs.ICommand;
import com.limport.tms.application.cqrs.ICommandBus;
import com.limport.tms.application.cqrs.ICommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Spring-based implementation of the Command Bus.
 * Automatically discovers and registers all ICommandHandler beans.
 */
@Component
public class CommandBus implements ICommandBus {
    
    private static final Logger log = LoggerFactory.getLogger(CommandBus.class);
    
    private final Map<Class<?>, ICommandHandler<?, ?>> handlers = new HashMap<>();
    
    @SuppressWarnings("rawtypes")
    public CommandBus(List<ICommandHandler> commandHandlers) {
        for (ICommandHandler handler : commandHandlers) {
            Class<?> commandType = handler.getCommandType();
            if (handlers.containsKey(commandType)) {
                throw new IllegalStateException(
                    "Duplicate command handler for " + commandType.getSimpleName());
            }
            handlers.put(commandType, handler);
            log.info("Registered command handler: {} -> {}", 
                commandType.getSimpleName(), handler.getClass().getSimpleName());
        }
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <R> R dispatch(ICommand<R> command) {
        ICommandHandler<ICommand<R>, R> handler = 
            (ICommandHandler<ICommand<R>, R>) handlers.get(command.getClass());
        
        if (handler == null) {
            throw new IllegalArgumentException(
                "No handler found for command: " + command.getClass().getSimpleName());
        }
        
        log.debug("Dispatching command {} to handler {}", 
            command.getClass().getSimpleName(), handler.getClass().getSimpleName());
        
        return handler.handle(command);
    }
}
