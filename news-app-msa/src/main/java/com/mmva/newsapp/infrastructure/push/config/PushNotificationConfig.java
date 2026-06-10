package com.mmva.newsapp.infrastructure.push.config;

import javax.annotation.PostConstruct;

import com.mmva.newsapp.infrastructure.push.service.PushFcmService;
import com.mmva.newsapp.infrastructure.push.service.PushNotificationService;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Push Notification Module Configuration.
 *
 * <h3>Features:</h3>
 * <ul>
 * <li>FCM service configuration</li>
 * <li>Async processing for notifications</li>
 * <li>Metrics registration for push operations</li>
 * <li>Push notification health indicators</li>
 * </ul>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Configuration
@EnableAsync
@RequiredArgsConstructor
@Slf4j
public class PushNotificationConfig {

    private final MeterRegistry meterRegistry;
    private final PushFcmService fcmService;
    private final PushNotificationService notificationService;

    /**
     * Configure push notification metrics.
     *
     * Registers custom metrics for monitoring push notification performance.
     */
    @PostConstruct
    public void configurePushMetrics() {
        log.info("Configuring push notification metrics");

        // Register FCM service metrics
        meterRegistry.gauge("push.fcm.service.status",
                fcmService,
                service -> service.isHealthy() ? 1 : 0);

        // Register notification queue metrics
        meterRegistry.gauge("push.notifications.pending",
                notificationService,
                service -> 0); // Placeholder - implement actual queue size

        log.info("Push notification metrics configured successfully");
    }

    /**
     * FCM configuration for production environment.
     */
    @Configuration
    @ConditionalOnProperty(name = "app.push.fcm.enabled", havingValue = "true", matchIfMissing = true)
    public static class FcmProductionConfig {

        /**
         * Validate FCM configuration on startup.
         */
        @PostConstruct
        public void validateFcmConfiguration() {
            log.info("Validating FCM configuration for production use");
            // Add FCM validation logic here
        }
    }

    /**
     * FCM mock configuration for testing/development.
     */
    @Configuration
    @ConditionalOnProperty(name = "app.push.fcm.enabled", havingValue = "false")
    public static class FcmMockConfig {

        /**
         * Configure mock FCM service for testing.
         */
        @PostConstruct
        public void configureFcmMock() {
            log.info("Configuring FCM mock service for testing");
        }
    }
}