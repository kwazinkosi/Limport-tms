package com.limport.tms.application.cqrs;

/**
 * Base interface for all commands in CQRS pattern.
 * Commands represent intent to change state.
 * 
 * @param <R> The result type of executing this command
 */
public interface ICommand<R> {
    // Marker interface - implementations define command-specific fields
}
