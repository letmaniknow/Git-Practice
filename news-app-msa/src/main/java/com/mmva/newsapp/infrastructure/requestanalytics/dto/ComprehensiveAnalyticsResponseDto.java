package com.mmva.newsapp.infrastructure.requestanalytics.dto;

/**
 * Comprehensive analytics response containing all analytics dimensions.
 *
 * Industry best practices implementation including:
 * - User behavior analytics
 * - Performance metrics (P50, P95, P99)
 * - Security insights
 * - Device intelligence
 * - Geographic distribution
 * - Time patterns
 * - Error analysis
 * - Session analytics
 * - Content performance
 * - Enterprise Features (Apdex, Throughput, SLA, Trends, Period Comparison)
 */
public record ComprehensiveAnalyticsResponseDto(
        OverviewMetrics overview,
        UserAnalytics users,
        PerformanceMetrics performance,
        SecurityInsights security,
        DeviceIntelligence devices,
        GeographicAnalytics geographic,
        TimeDistribution timeDistribution,
        ErrorAnalytics errors,
        SessionAnalytics sessions,
        ContentPerformance content,
        ThroughputMetrics throughput,
        ApdexMetrics apdex,
        SlaMetrics sla,
        TrendAnalytics trends,
        PeriodComparison periodComparison,
        ActiveUsersMetrics activeUsersMetrics,
        BandwidthMetrics bandwidth) {
}
