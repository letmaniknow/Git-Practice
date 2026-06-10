package com.mmva.newsapp.infrastructure.requestanalytics.dto;

import java.util.List;

/**
 * SLA (Service Level Agreement) metrics.
 */
public record SlaMetrics(
                double availabilityPercentage,
                double successRate,
                long successfulRequests,
                long failedRequests,
                long totalRequests,
                long uptimeHours,
                long downtimeHours,
                long totalHoursInPeriod,
                List<DowntimePeriod> downtimePeriods) {

        public record DowntimePeriod(
                        String startTime,
                        String endTime,
                        long durationHours) {
        }
}