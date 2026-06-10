package com.mmva.newsapp.infrastructure.requestanalytics.dto;

/**
 * Apdex (Application Performance Index) metrics.
 */
public record ApdexMetrics(
                double apdexScore,
                String rating,
                long satisfied,
                long tolerating,
                long frustrated,
                long total,
                long satisfiedThreshold,
                long frustratedThreshold,
                String interpretation) {
}