package com.mmva.newsapp.infrastructure.requestanalytics.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Configuration properties for Request Analytics feature.
 * 
 * <p>
 * Externalized configuration following Spring Boot best practices.
 * All properties are prefixed with {@code app.analytics}.
 * </p>
 * 
 * <h3>Example Configuration:</h3>
 * 
 * <pre>
 * app:
 *   analytics:
 *     enabled: true
 *     async: true
 *     exclude-patterns:
 *       - /actuator/**
 *       - /swagger-ui/**
 *       - /v3/api-docs/**
 *       - /health
 *       - /favicon.ico
 *     sampling:
 *       enabled: false
 *       rate: 0.1
 *     retention:
 *       enabled: true
 *       period: 90d
 *     sensitive-headers:
 *       - Authorization
 *       - Cookie
 *       - X-Api-Key
 *     max-header-length: 1024
 *     max-uri-length: 2048
 * </pre>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Component
@ConfigurationProperties(prefix = "app.analytics")
@Validated
@Getter
@Setter
public class RequestAnalyticsProperties {

    /**
     * Enable or disable request analytics logging.
     * Default: false
     */
    private boolean enabled = false;

    /**
     * Use async logging for better performance.
     * Default: true
     */
    private boolean async = true;

    /**
     * URL patterns to exclude from analytics logging.
     * Supports Ant-style patterns.
     */
    private List<String> excludePatterns = new ArrayList<>(List.of(
            "/actuator/**",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/health",
            "/favicon.ico",
            "/**/*.css",
            "/**/*.js",
            "/**/*.png",
            "/**/*.jpg",
            "/**/*.ico"));

    /**
     * Headers that should be masked/excluded for security.
     */
    private List<String> sensitiveHeaders = new ArrayList<>(List.of(
            "Authorization",
            "Cookie",
            "Set-Cookie",
            "X-Api-Key",
            "X-Auth-Token"));

    /**
     * Maximum length for stored header values.
     * Longer values will be truncated.
     */
    private int maxHeaderLength = 1024;

    /**
     * Maximum length for stored URI.
     */
    private int maxUriLength = 2048;

    /**
     * Maximum length for stored User-Agent.
     */
    private int maxUserAgentLength = 512;

    /**
     * Sampling configuration for high-traffic applications.
     */
    private Sampling sampling = new Sampling();

    /**
     * Data retention configuration.
     */
    private Retention retention = new Retention();

    /**
     * Performance thresholds for alerting.
     */
    private Thresholds thresholds = new Thresholds();

    /**
     * Sampling configuration to reduce storage for high-traffic apps.
     */
    @Getter
    @Setter
    public static class Sampling {
        /**
         * Enable sampling (log only a percentage of requests).
         */
        private boolean enabled = false;

        /**
         * Sampling rate (0.0 to 1.0). E.g., 0.1 = 10% of requests.
         */
        private double rate = 1.0;

        /**
         * Endpoints to always log (bypass sampling).
         */
        private List<String> alwaysLogPatterns = new ArrayList<>(List.of(
                "/api/**/admindashboard/**",
                "/api/**/auth/**"));
    }

    /**
     * Data retention configuration.
     */
    @Getter
    @Setter
    public static class Retention {
        /**
         * Enable automatic cleanup of old analytics data.
         */
        private boolean enabled = true;

        /**
         * How long to keep analytics data.
         * Default: 90 days
         */
        private Duration period = Duration.ofDays(90);

        /**
         * Cron expression for cleanup job.
         * Default: 2 AM daily
         */
        private String cleanupCron = "0 0 2 * * ?";
    }

    /**
     * Performance thresholds for monitoring and alerting.
     */
    @Getter
    @Setter
    public static class Thresholds {
        /**
         * Response time threshold (ms) to flag as slow.
         */
        private long slowResponseMs = 3000;

        /**
         * Number of requests from single IP to flag as suspicious.
         */
        private long suspiciousIpRequestCount = 1000;

        /**
         * Time window for suspicious IP detection.
         */
        private Duration suspiciousIpWindow = Duration.ofHours(1);

        /**
         * Error rate threshold (percentage) to flag endpoint.
         */
        private double highErrorRatePercent = 10.0;
    }
}
