package com.mmva.newsapp.infrastructure.requestanalytics.dto;

import java.time.Instant;
import java.util.List;

/**
 * User behavior analytics.
 */
public record UserAnalytics(
                long uniqueUsers,
                long authenticatedRequests,
                long anonymousRequests,
                Double authenticationRate,
                List<TopUser> topUsers) {

        public record TopUser(
                        String userId,
                        String username,
                        long requestCount,
                        Instant lastActivity) {
        }
}