package com.limport.tms.application.cqrs;

/**
 * Handler for commands in CQRS pattern.
 * Each command type has exactly one handler responsible for its execution.
 * 
 * @param <C> The command type
 * @param <R> The result type
 */
public interface ICommandHandler<C extends ICommand<R>, R> {
    
    /**
     * Handle the command and return the result.
     * 
     * @param command The command to handle
     * @return The result of handling the command
     */
    R handle(C command);
    
    /**
     * Get the command class this handler supports.
     */
    Class<C> getCommandType();
}
