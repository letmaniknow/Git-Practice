package com.mmva.newsapp.infrastructure.requestanalytics.dto;

import java.util.List;

/**
 * Period delta metrics for comparison.
 */
public record PeriodDelta(
                long requestsDelta,
                Double requestsChangePercent,
                long usersDelta,
                Double usersChangePercent,
                Double avgResponseTimeDelta,
                Double responseTimeChangePercent,
                Double errorRateDelta,
                String overallTrend,
                List<String> insights) {
}