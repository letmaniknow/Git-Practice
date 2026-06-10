package com.mmva.newsapp.infrastructure.requestanalytics.dto;

import java.util.List;

/**
 * Performance metrics with percentiles.
 */
public record PerformanceMetrics(
                Double p50ResponseTime,
                Double p95ResponseTime,
                Double p99ResponseTime,
                Double averageResponseTime,
                long minResponseTime,
                long maxResponseTime,
                List<SlowEndpoint> slowestEndpoints) {

        public record SlowEndpoint(
                        String endpoint,
                        String method,
                        long responseTime,
                        double requestsPerSecond,
                        double errorRate) {
        }

        record ResponseTimeDistribution(
                        String range,
                        long count,
                        Double percentage) {
        }
}