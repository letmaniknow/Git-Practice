package com.mmva.newsapp.infrastructure.requestanalytics.dto;

import java.time.Instant;
import java.util.List;

/**
 * Throughput metrics.
 */
public record ThroughputMetrics(
                Double requestsPerSecond,
                Double requestsPerMinute,
                Double requestsPerHour,
                long peakRequestsPerMinute,
                Instant peakTime,
                List<MinutelyThroughput> recentThroughput) {

        public record MinutelyThroughput(
                        Instant minute,
                        long requestCount,
                        Double requestsPerSecond) {
        }
}