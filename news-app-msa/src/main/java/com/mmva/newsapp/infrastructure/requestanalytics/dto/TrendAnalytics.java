package com.mmva.newsapp.infrastructure.requestanalytics.dto;

import java.time.Instant;
import java.util.List;

/**
 * Trend analytics over time.
 */
public record TrendAnalytics(
                List<LatencyTrend> latencyTrends,
                List<ErrorRateTrend> errorRateTrends,
                TrendSummary latencySummary,
                TrendSummary errorRateSummary) {

        public record LatencyTrend(
                        Instant timestamp,
                        long requestCount,
                        long errorCount,
                        Double errorRate) {
        }

        public record ErrorRateTrend(
                        Instant timestamp,
                        long requestCount,
                        long errorCount,
                        Double errorRate) {
        }

        public record TrendSummary(
                        String direction,
                        double changePercent,
                        double currentValue,
                        double previousValue,
                        String interpretation) {
        }
}