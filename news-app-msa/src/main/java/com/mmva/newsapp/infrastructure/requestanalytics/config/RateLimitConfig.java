package com.mmva.newsapp.infrastructure.requestanalytics.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiting configuration using Bucket4j.
 * Provides per-IP rate limiting for API endpoints.
 */
@Configuration
@Component
public class RateLimitConfig {

    // Store buckets per client IP
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Value("${rateLimit.enabled:true}")
    private boolean enabled;

    @Value("${rateLimit.requestsPerMinute:60}")
    private int requestsPerMinute;

    @Value("${rateLimit.burstCapacity:10}")
    private int burstCapacity;

    /**
     * Resolves or creates a rate limit bucket for the given client key (typically
     * IP address).
     */
    public Bucket resolveBucket(String clientKey) {
        return buckets.computeIfAbsent(clientKey, this::createNewBucket);
    }

    /**
     * Creates a new bucket with the configured rate limits.
     */
    private Bucket createNewBucket(String clientKey) {
        Bandwidth limit = Bandwidth.builder()
                .capacity(burstCapacity)
                .refillGreedy(requestsPerMinute, Duration.ofMinutes(1))
                .build();
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * Checks if a request should be allowed for the given client.
     * 
     * @param clientKey The client identifier (typically IP address)
     * @return true if the request is allowed, false if rate limited
     */
    public boolean tryConsume(String clientKey) {
        if (!enabled) {
            return true; // Rate limiting disabled
        }
        Bucket bucket = resolveBucket(clientKey);
        return bucket.tryConsume(1);
    }

    /**
     * Gets the number of available tokens for a client.
     */
    public long getAvailableTokens(String clientKey) {
        Bucket bucket = resolveBucket(clientKey);
        return bucket.getAvailableTokens();
    }
}
