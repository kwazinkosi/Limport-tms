package com.limport.tms.infrastructure.adapter;

import com.limport.tms.domain.port.service.IAsyncExecutor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

/**
 * Infrastructure adapter for async execution.
 * 
 * Implements the application port using Spring's thread pool executor.
 * This keeps infrastructure concerns (thread pools, executors) in the
 * infrastructure layer where they belong.
 */
@Component
public class AsyncExecutorAdapter implements IAsyncExecutor {
    
    private final Executor executor;
    
    public AsyncExecutorAdapter(@Qualifier("eventHandlerExecutor") Executor executor) {
        this.executor = executor;
    }
    
    @Override
    public <T> CompletableFuture<T> executeAsync(Supplier<T> task) {
        return CompletableFuture.supplyAsync(task, executor);
    }
    
    @Override
    public CompletableFuture<Void> executeAsync(Runnable task) {
        return CompletableFuture.runAsync(task, executor);
    }
}
