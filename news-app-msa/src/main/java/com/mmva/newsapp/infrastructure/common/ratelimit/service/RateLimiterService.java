package com.mmva.newsapp.infrastructure.common.ratelimit.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple in-memory rate limiter for verification code requests.
 * Prevents abuse by limiting how often users can request new codes.
 */
@Slf4j
@Service
public class RateLimiterService {

    // Map of userId -> last request timestamp
    private final Map<String, RateLimitEntry> rateLimitMap = new ConcurrentHashMap<>();

    // Configurable cooldown and max requests
    private final long cooldownSeconds;
    private final int maxRequestsPerHour;

    public RateLimiterService(
            @Value("${rateLimit.cooldownSeconds:1}") long cooldownSeconds,
            @Value("${rateLimit.maxRequestsPerHour:1000}") int maxRequestsPerHour) {
        this.cooldownSeconds = cooldownSeconds;
        this.maxRequestsPerHour = maxRequestsPerHour;
    }

    /**
     * Check if a verification code request is allowed for the given user and type.
     * 
     * @param userId The user ID
     * @param type   The verification type (e.g., "email", "phone",
     *               "password-reset")
     * @return true if allowed, false if rate limited
     */
    public boolean isAllowed(UUID userId, String type) {
        String key = buildKey(userId, type);
        return isAllowed(key);
    }

    /**
     * Check if request is allowed for a given key (e.g., email address for password
     * reset).
     */
    public boolean isAllowed(String key) {
        RateLimitEntry entry = rateLimitMap.get(key);

        if (entry == null) {
            return true;
        }

        Instant now = Instant.now();

        // Reset hourly counter if an hour has passed
        if (now.isAfter(entry.hourStart.plusSeconds(3600))) {
            rateLimitMap.remove(key);
            return true;
        }

        // Check cooldown period
        if (now.isBefore(entry.lastRequest.plusSeconds(cooldownSeconds))) {
            log.warn("Rate limit: cooldown not elapsed for key: {}", key);
            return false;
        }

        // Check hourly limit
        if (entry.requestCount >= maxRequestsPerHour) {
            log.warn("Rate limit: hourly limit exceeded for key: {}", key);
            return false;
        }

        return true;
    }

    /**
     * Record a verification code request.
     */
    public void recordRequest(UUID userId, String type) {
        String key = buildKey(userId, type);
        recordRequest(key);
    }

    /**
     * Record a request for a given key.
     */
    public void recordRequest(String key) {
        Instant now = Instant.now();

        rateLimitMap.compute(key, (k, existing) -> {
            if (existing == null || now.isAfter(existing.hourStart.plusSeconds(3600))) {
                // New entry or reset after an hour
                return new RateLimitEntry(now, now, 1);
            } else {
                // Increment existing
                return new RateLimitEntry(existing.hourStart, now, existing.requestCount + 1);
            }
        });

        log.info("Rate limit: recorded request for key: {}", key);
    }

    /**
     * Get remaining cooldown seconds for a user/type combination.
     */
    public long getRemainingCooldownSeconds(UUID userId, String type) {
        String key = buildKey(userId, type);
        return getRemainingCooldownSeconds(key);
    }

    /**
     * Get remaining cooldown seconds for a key.
     */
    public long getRemainingCooldownSeconds(String key) {
        RateLimitEntry entry = rateLimitMap.get(key);

        if (entry == null) {
            return 0;
        }

        Instant cooldownEnd = entry.lastRequest.plusSeconds(cooldownSeconds);
        Instant now = Instant.now();

        if (now.isAfter(cooldownEnd)) {
            return 0;
        }

        return cooldownEnd.getEpochSecond() - now.getEpochSecond();
    }

    /**
     * Get remaining requests allowed in the current hour.
     */
    public int getRemainingRequestsInHour(UUID userId, String type) {
        String key = buildKey(userId, type);
        RateLimitEntry entry = rateLimitMap.get(key);

        if (entry == null) {
            return maxRequestsPerHour;
        }

        Instant now = Instant.now();
        if (now.isAfter(entry.hourStart.plusSeconds(3600))) {
            return maxRequestsPerHour;
        }

        return Math.max(0, maxRequestsPerHour - entry.requestCount);
    }

    private String buildKey(UUID userId, String type) {
        return userId.toString() + ":" + type;
    }

    /**
     * Internal class to track rate limit state.
     */
    private record RateLimitEntry(Instant hourStart, Instant lastRequest, int requestCount) {
    }
}
