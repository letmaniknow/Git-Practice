package com.mmva.newsapp.infrastructure.requestanalytics.dto;

import java.time.Instant;

/**
 * Period statistics for comparison.
 */
public record PeriodStats(
                String periodLabel,
                Instant startTime,
                Instant endTime,
                long totalRequests,
                long uniqueUsers,
                long uniqueSessions,
                Double avgResponseTime,
                long errorCount,
                long successCount,
                Double errorRate,
                Double successRate) {
}