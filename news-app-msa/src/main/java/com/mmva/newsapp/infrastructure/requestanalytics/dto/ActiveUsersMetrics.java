package com.mmva.newsapp.infrastructure.requestanalytics.dto;

import java.time.Instant;
import java.util.List;

/**
 * Active users metrics.
 */
public record ActiveUsersMetrics(
                long users5Min,
                long users15Min,
                long users30Min,
                long users60Min,
                long sessions5Min,
                long sessions15Min,
                long sessions30Min,
                long sessions60Min,
                List<ActiveUser> recentActiveUsers) {

        public record ActiveUser(
                        String userId,
                        long requestCount,
                        Instant lastActivity) {
        }
}