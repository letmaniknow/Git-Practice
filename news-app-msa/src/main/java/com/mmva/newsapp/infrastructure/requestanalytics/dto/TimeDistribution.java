package com.mmva.newsapp.infrastructure.requestanalytics.dto;

import java.util.List;

/**
 * Time-based distribution analytics.
 */
public record TimeDistribution(
                List<HourlyStats> hourlyStats,
                List<PeakHour> peakHours) {

        public record HourlyStats(
                        int hour,
                        long requestCount) {
        }

        public record PeakHour(
                        int dayOfWeek,
                        int hour,
                        long requestCount,
                        String dayName) {
        }
}