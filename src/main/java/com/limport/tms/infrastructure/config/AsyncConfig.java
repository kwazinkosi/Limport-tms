package com.limport.tms.infrastructure.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.lang.reflect.Method;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Async configuration for non-blocking event handler execution.
 * 
 * Event handlers execute in a dedicated thread pool, preventing
 * them from blocking Kafka consumer threads and improving throughput.
 * 
 * Thread pool is sized for I/O-bound work (DB calls, HTTP requests).
 */
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {
    
    private static final Logger log = LoggerFactory.getLogger(AsyncConfig.class);
    
    @Value("${tms.async.event-handler.core-pool-size:5}")
    private int corePoolSize;
    
    @Value("${tms.async.event-handler.max-pool-size:20}")
    private int maxPoolSize;
    
    @Value("${tms.async.event-handler.queue-capacity:100}")
    private int queueCapacity;
    
    @Value("${tms.async.event-handler.thread-prefix:event-handler-}")
    private String threadPrefix;
    
    /**
     * Dedicated thread pool for event handler execution.
     * 
     * Configuration rationale:
     * - Core pool: Handles normal load without creating new threads
     * - Max pool: Handles burst traffic during peak events
     * - Queue capacity: Buffers events when all threads are busy
     * 
     * Uses CallerRunsPolicy to provide backpressure when overwhelmed.
     */
    @Bean("eventHandlerExecutor")
    public Executor eventHandlerExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix(threadPrefix);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        
        // CallerRunsPolicy: If queue is full, caller thread executes the task
        // This provides natural backpressure to Kafka consumer
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        executor.initialize();
        
        log.info("Initialized event handler executor: corePool={}, maxPool={}, queueCapacity={}",
            corePoolSize, maxPoolSize, queueCapacity);
        
        return executor;
    }
    
    @Override
    public Executor getAsyncExecutor() {
        return eventHandlerExecutor();
    }
    
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new EventHandlerExceptionHandler();
    }
    
    /**
     * Handles uncaught exceptions in async event handlers.
     * Logs errors for monitoring and alerting.
     */
    private static class EventHandlerExceptionHandler implements AsyncUncaughtExceptionHandler {
        
        private static final Logger exceptionLog = LoggerFactory.getLogger(EventHandlerExceptionHandler.class);
        
        @Override
        public void handleUncaughtException(Throwable ex, Method method, Object... params) {
            exceptionLog.error("Async event handler exception in method {}: {}",
                method.getName(), ex.getMessage(), ex);
            
            // TODO: In production, consider:
            // - Publishing to error monitoring service (Sentry, DataDog)
            // - Incrementing error metrics for alerting
            // - Sending to dead letter queue for later analysis
        }
    }
}
