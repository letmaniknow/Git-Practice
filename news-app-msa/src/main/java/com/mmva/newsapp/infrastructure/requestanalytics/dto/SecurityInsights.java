package com.mmva.newsapp.infrastructure.requestanalytics.dto;

import java.util.List;

/**
 * Security monitoring insights.
 */
public record SecurityInsights(
                long totalFailedAuthAttempts,
                List<FailedAuthIp> topFailedAuthIps,
                List<SuspiciousIp> suspiciousIps,
                BotTraffic botTraffic) {

        public record FailedAuthIp(
                        String ipAddress,
                        String country,
                        long attemptCount) {
        }

        public record SuspiciousIp(
                        String ipAddress,
                        String country,
                        long requestCount) {
        }

        public record BotTraffic(
                        long botRequests,
                        long humanRequests,
                        Double botPercentage) {
        }
}