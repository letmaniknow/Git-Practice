package com.mmva.newsapp.infrastructure.requestanalytics.dto;

import java.time.Instant;
import java.util.List;

/**
 * Bandwidth and response size metrics.
 */
public record BandwidthMetrics(
                Double avgResponseSizeBytes,
                long totalBandwidthBytes,
                long minResponseSizeBytes,
                long maxResponseSizeBytes,
                String totalBandwidthFormatted,
                String avgResponseSizeFormatted,
                List<HourlyBandwidth> hourlyBandwidth) {

        public record HourlyBandwidth(
                        Instant hour,
                        long totalBytes,
                        Double avgBytes,
                        long requestCount,
                        String formattedTotal) {
        }
}