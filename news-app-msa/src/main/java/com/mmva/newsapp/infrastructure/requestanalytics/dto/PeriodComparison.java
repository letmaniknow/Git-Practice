package com.mmva.newsapp.infrastructure.requestanalytics.dto;

/**
 * Period comparison metrics.
 */
public record PeriodComparison(
        PeriodStats currentPeriod,
        PeriodStats previousPeriod,
        PeriodDelta delta,
        String comparisonType) {
}