package com.limport.tms.domain.port.service;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Port for async execution in the application layer.
 * 
 * This abstraction allows the application layer to execute tasks
 * asynchronously without depending on infrastructure (Executor, ThreadPool).
 * 
 * Following Clean Architecture: Domain layer defines the PORT,
 * Infrastructure layer provides the ADAPTER (implementation).
 */
public interface IAsyncExecutor {
    
    /**
     * Execute a task asynchronously.
     * 
     * @param task The task to execute
     * @param <T> The result type
     * @return CompletableFuture that completes when task finishes
     */
    <T> CompletableFuture<T> executeAsync(Supplier<T> task);
    
    /**
     * Execute a runnable asynchronously.
     * 
     * @param task The task to execute
     * @return CompletableFuture that completes when task finishes
     */
    CompletableFuture<Void> executeAsync(Runnable task);
}
