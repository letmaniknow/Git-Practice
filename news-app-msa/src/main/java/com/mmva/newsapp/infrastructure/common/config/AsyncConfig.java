package com.mmva.newsapp.infrastructure.common.config;

import lombok.extern.slf4j.Slf4j;
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
 * Async Thread Pool Configuration.
 * 
 * <p>
 * Configures a custom thread pool for @Async methods with proper sizing,
 * queue capacity, and exception handling.
 * </p>
 * 
 * <h3>Thread Pool Sizing Guidelines:</h3>
 * <ul>
 * <li><b>Core Pool Size:</b> Number of threads to keep alive even when
 * idle</li>
 * <li><b>Max Pool Size:</b> Maximum threads when queue is full</li>
 * <li><b>Queue Capacity:</b> Tasks waiting when core threads are busy</li>
 * </ul>
 * 
 * <h3>Flow:</h3>
 * 
 * <pre>
 * Task arrives → Core threads available? → Execute immediately
 *             → Core threads busy? → Add to queue
 *             → Queue full? → Create new thread (up to max)
 *             → Max threads reached + queue full? → Rejection policy
 * </pre>
 * 
 * @author MMVA News Team
 * @since 1.0.0
 */
@Slf4j
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    // ================================
    // Configuration Properties
    // ================================

    @Value("${async.executor.core-pool-size:5}")
    private int corePoolSize;

    @Value("${async.executor.max-pool-size:20}")
    private int maxPoolSize;

    @Value("${async.executor.queue-capacity:100}")
    private int queueCapacity;

    @Value("${async.executor.thread-name-prefix:Async-}")
    private String threadNamePrefix;

    @Value("${async.executor.keep-alive-seconds:60}")
    private int keepAliveSeconds;

    @Value("${async.executor.await-termination-seconds:60}")
    private int awaitTerminationSeconds;

    // ================================
    // Main Async Executor
    // ================================

    /**
     * Creates the main async executor for @Async methods.
     * 
     * @return Configured ThreadPoolTaskExecutor
     */
    @Override
    @Bean(name = "taskExecutor")
    public Executor getAsyncExecutor() {
        log.info("Initializing Async Thread Pool: core={}, max={}, queue={}",
                corePoolSize, maxPoolSize, queueCapacity);

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // Thread pool sizing
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);

        // Thread naming for debugging
        executor.setThreadNamePrefix(threadNamePrefix);

        // Keep-alive time for idle threads above core size
        executor.setKeepAliveSeconds(keepAliveSeconds);

        // Graceful shutdown
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(awaitTerminationSeconds);

        // Rejection policy: CallerRunsPolicy ensures task is not lost
        // Task will run in the calling thread if pool is exhausted
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        executor.initialize();

        log.info("Async Thread Pool initialized successfully");
        return executor;
    }

    // ================================
    // Exception Handler
    // ================================

    /**
     * Handles uncaught exceptions from @Async methods.
     * Since async methods run in a separate thread, exceptions don't propagate
     * to the caller. This handler logs them properly.
     * 
     * @return AsyncUncaughtExceptionHandler
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new CustomAsyncExceptionHandler();
    }

    /**
     * Custom exception handler for async operations.
     */
    @Slf4j
    private static class CustomAsyncExceptionHandler implements AsyncUncaughtExceptionHandler {

        @Override
        public void handleUncaughtException(Throwable ex, Method method, Object... params) {
            log.error("Async method '{}' threw exception: {}",
                    method.getName(),
                    ex.getMessage(),
                    ex);

            // Log parameters for debugging (be careful with sensitive data)
            if (params != null && params.length > 0) {
                log.error("Async method parameters: {}", java.util.Arrays.toString(params));
            }

            // TODO: In production, you might want to:
            // - Send alert to monitoring system
            // - Retry the operation
            // - Store failed task for later processing
        }
    }
}
