package com.mmva.newsapp.infrastructure.monetization.ads.external.common.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiting configuration for ad providers dashboard API
 *
 * Implements token bucket algorithm to prevent API abuse
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Configuration
@Slf4j
public class AdProviderDashboardRateLimitConfig {

    @Value("${adprovider.dashboard.rate-limit.requests-per-minute:60}")
    private int requestsPerMinute;

    @Value("${adprovider.dashboard.rate-limit.requests-per-hour:1000}")
    private int requestsPerHour;

    /**
     * Rate limit buckets storage
     * In production, consider using Redis for distributed rate limiting
     */
    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    /**
     * Create rate limit bucket for dashboard API
     *
     * Allows 60 requests per minute and 1000 requests per hour per client
     *
     * @return Configured bucket
     */
    @Bean
    public Bucket adProviderDashboardRateLimitBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.classic(requestsPerMinute,
                        Refill.intervally(requestsPerMinute, Duration.ofMinutes(1))))
                .addLimit(Bandwidth.classic(requestsPerHour, Refill.intervally(requestsPerHour, Duration.ofHours(1))))
                .build();
    }

    /**
     * Get or create bucket for specific client
     *
     * @param clientId Client identifier (IP address, user ID, etc.)
     * @return Bucket for the client
     */
    public Bucket getBucketForClient(String clientId) {
        return buckets.computeIfAbsent(clientId, k -> {
            log.debug("Creating new rate limit bucket for client: {}", clientId);
            return Bucket.builder()
                    .addLimit(Bandwidth.classic(requestsPerMinute,
                            Refill.intervally(requestsPerMinute, Duration.ofMinutes(1))))
                    .addLimit(
                            Bandwidth.classic(requestsPerHour, Refill.intervally(requestsPerHour, Duration.ofHours(1))))
                    .build();
        });
    }

    /**
     * Check if request is allowed for client
     *
     * @param clientId Client identifier
     * @return true if request is allowed, false if rate limited
     */
    public boolean isRequestAllowed(String clientId) {
        Bucket bucket = getBucketForClient(clientId);
        boolean allowed = bucket.tryConsume(1);

        if (!allowed) {
            log.warn("Rate limit exceeded for client: {}", clientId);
        }

        return allowed;
    }

    /**
     * Get remaining tokens for client
     *
     * @param clientId Client identifier
     * @return Number of remaining requests
     */
    public long getRemainingRequests(String clientId) {
        Bucket bucket = getBucketForClient(clientId);
        return bucket.getAvailableTokens();
    }
}