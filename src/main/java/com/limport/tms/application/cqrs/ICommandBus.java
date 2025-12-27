package com.limport.tms.application.cqrs;

/**
 * Dispatches commands to their appropriate handlers.
 * Acts as a mediator in the CQRS pattern.
 */
public interface ICommandBus {
    
    /**
     * Dispatch a command to its handler and return the result.
     * 
     * @param command The command to dispatch
     * @param <R> The result type
     * @return The result of handling the command
     */
    <R> R dispatch(ICommand<R> command);
}
