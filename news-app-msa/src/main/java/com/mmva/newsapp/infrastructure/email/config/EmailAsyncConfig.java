package com.mmva.newsapp.infrastructure.email.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Email Module Async Configuration.
 * 
 * <p>
 * Provides a dedicated thread pool for email sending operations.
 * Separate pool prevents email operations from blocking other async tasks.
 * </p>
 * 
 * <h3>Why Dedicated Pool?</h3>
 * <ul>
 * <li>Email sending can be slow (SMTP latency)</li>
 * <li>Prevents email queue from starving other async operations</li>
 * <li>Allows independent tuning of email concurrency</li>
 * <li>Easier debugging with "Email-" thread prefix</li>
 * </ul>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Slf4j
@Configuration
public class EmailAsyncConfig {

    /**
     * Creates a dedicated executor for email sending operations.
     * 
     * <p>
     * Usage in service:
     * </p>
     * 
     * <pre>{@code
     * &#64;Async("emailExecutor")
     * public void sendEmail(EmailDto email) { ... }
     * }</pre>
     * 
     * @return Configured ThreadPoolTaskExecutor for emails
     */
    @Bean(name = "emailExecutor")
    public Executor emailExecutor() {
        log.info("Initializing Email Thread Pool");

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // Smaller pool for email operations
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("Email-");
        executor.setKeepAliveSeconds(30);

        // Graceful shutdown
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);

        // CallerRunsPolicy: If pool is full, caller thread sends the email
        // This ensures no email is lost, just slightly slower
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        executor.initialize();

        log.info("Email Thread Pool initialized: core=2, max=5, queue=50");
        return executor;
    }
}
