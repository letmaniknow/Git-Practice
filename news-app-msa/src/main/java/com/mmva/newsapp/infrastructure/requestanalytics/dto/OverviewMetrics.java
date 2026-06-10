package com.mmva.newsapp.infrastructure.requestanalytics.dto;

/**
 * Overview metrics summary.
 */
public record OverviewMetrics(
        long totalRequests,
        long uniqueUsers,
        long uniqueSessions,
        Double averageResponseTime,
        Double errorRate,
        String timeRange) {
}