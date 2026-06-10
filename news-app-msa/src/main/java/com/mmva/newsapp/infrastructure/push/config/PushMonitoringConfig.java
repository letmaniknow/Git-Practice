package com.mmva.newsapp.infrastructure.push.config;

import com.mmva.newsapp.infrastructure.push.service.PushNotificationService;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Push Notification Monitoring and Dashboard Configuration.
 *
 * <h3>Features:</h3>
 * <ul>
 * <li>Custom health indicators for push notifications</li>
 * <li>Comprehensive metrics collection</li>
 * <li>Actuator endpoint security configuration</li>
 * <li>JVM and system metrics integration</li>
 * </ul>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Slf4j
public class PushMonitoringConfig {

    private final PushNotificationService pushNotificationService;
    private final MeterRegistry meterRegistry;

    /**
     * Configure security for actuator endpoints.
     *
     * @param http HTTP security configuration
     * @return configured security filter chain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain actuatorSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/actuator/**")
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/actuator/info").permitAll()
                        .requestMatchers("/actuator/metrics").hasRole("ADMIN")
                        .requestMatchers("/actuator/push/**").hasRole("ADMIN")
                        .requestMatchers("/actuator/**").hasRole("ADMIN"))
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(org.springframework.security.config.Customizer.withDefaults());

        return http.build();
    }

    /**
     * Custom health indicator for push notification service.
     *
     * @return push notification health indicator
     */
    @Bean
    public HealthIndicator pushNotificationHealthIndicator() {
        return () -> {
            try {
                // Check if FCM service is accessible
                boolean fcmHealthy = pushNotificationService.isFcmServiceHealthy();

                // Check database connectivity for push-related data
                boolean dbHealthy = pushNotificationService.isDatabaseHealthy();

                // Check queue health
                boolean queueHealthy = pushNotificationService.isQueueHealthy();

                if (fcmHealthy && dbHealthy && queueHealthy) {
                    return Health.up()
                            .withDetail("fcm", "healthy")
                            .withDetail("database", "healthy")
                            .withDetail("queue", "healthy")
                            .build();
                } else {
                    Health.Builder health = Health.down();
                    if (!fcmHealthy)
                        health.withDetail("fcm", "unhealthy");
                    if (!dbHealthy)
                        health.withDetail("database", "unhealthy");
                    if (!queueHealthy)
                        health.withDetail("queue", "unhealthy");
                    return health.build();
                }
            } catch (Exception e) {
                log.error("Error checking push notification health", e);
                return Health.down(e).build();
            }
        };
    }

    /**
     * Custom health indicator for push notification performance.
     *
     * @return performance health indicator
     */
    @Bean
    public HealthIndicator pushNotificationPerformanceHealthIndicator() {
        return () -> {
            try {
                // Check delivery rate (should be > 90%)
                double deliveryRate = calculateDeliveryRate();
                boolean deliveryHealthy = deliveryRate >= 90.0;

                // Check error rate (should be < 5%)
                double errorRate = calculateErrorRate();
                boolean errorHealthy = errorRate <= 5.0;

                // Check queue size (should be < 1000)
                int queueSize = getQueueSize();
                boolean queueHealthy = queueSize < 1000;

                if (deliveryHealthy && errorHealthy && queueHealthy) {
                    return Health.up()
                            .withDetail("deliveryRate", String.format("%.2f%%", deliveryRate))
                            .withDetail("errorRate", String.format("%.2f%%", errorRate))
                            .withDetail("queueSize", queueSize)
                            .build();
                } else {
                    Health.Builder health = Health.down();
                    if (!deliveryHealthy)
                        health.withDetail("deliveryRate", String.format("%.2f%%", deliveryRate));
                    if (!errorHealthy)
                        health.withDetail("errorRate", String.format("%.2f%%", errorRate));
                    if (!queueHealthy)
                        health.withDetail("queueSize", queueSize);
                    return health.build();
                }
            } catch (Exception e) {
                log.error("Error checking push notification performance health", e);
                return Health.down(e).build();
            }
        };
    }

    /**
     * Configure JVM memory metrics.
     *
     * @return JVM memory metrics binder
     */
    @Bean
    public JvmMemoryMetrics jvmMemoryMetrics() {
        JvmMemoryMetrics metrics = new JvmMemoryMetrics();
        metrics.bindTo(meterRegistry);
        return metrics;
    }

    /**
     * Configure JVM thread metrics.
     *
     * @return JVM thread metrics binder
     */
    @Bean
    public JvmThreadMetrics jvmThreadMetrics() {
        JvmThreadMetrics metrics = new JvmThreadMetrics();
        metrics.bindTo(meterRegistry);
        return metrics;
    }

    /**
     * Configure system processor metrics.
     *
     * @return processor metrics binder
     */
    @Bean
    public ProcessorMetrics processorMetrics() {
        ProcessorMetrics metrics = new ProcessorMetrics();
        metrics.bindTo(meterRegistry);
        return metrics;
    }

    /**
     * Calculate delivery rate from metrics.
     *
     * @return delivery rate percentage
     */
    private double calculateDeliveryRate() {
        double sent = meterRegistry.counter("push.notifications.sent").count();
        double delivered = meterRegistry.counter("push.notifications.delivered").count();
        return sent > 0 ? (delivered / sent) * 100 : 0;
    }

    /**
     * Calculate error rate from metrics.
     *
     * @return error rate percentage
     */
    private double calculateErrorRate() {
        double sent = meterRegistry.counter("push.notifications.sent").count();
        double errors = meterRegistry.counter("push.errors.total").count();
        return sent > 0 ? (errors / sent) * 100 : 0;
    }

    /**
     * Get current queue size.
     *
     * @return queue size
     */
    private int getQueueSize() {
        return (int) meterRegistry.gauge("push.queue.size", 0);
    }
}