package com.mmva.newsapp.infrastructure.requestanalytics.dto;

import java.time.Instant;
import java.util.List;
import java.util.Map;

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
public record ComprehensiveAnalyticsResponse(
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
                // Enterprise Features
                ThroughputMetrics throughput,
                ApdexMetrics apdex,
                SlaMetrics sla,
                TrendAnalytics trends,
                PeriodComparison periodComparison,
                ActiveUsersMetrics activeUsers,
                BandwidthMetrics bandwidth) {

        /**
         * Overview metrics summary.
         */
        public record OverviewMetrics(
                        long totalRequests,
                        long uniqueUsers,
                        long uniqueSessions,
                        Double averageResponseTime,
                        Double errorRate,
                        String timeRange) {
        }

        /**
         * User behavior analytics.
         */
        public record UserAnalytics(
                        long uniqueUsers,
                        long authenticatedRequests,
                        long anonymousRequests,
                        Double authenticationRate,
                        List<TopUser> topUsers) {

                public record TopUser(
                                String userId,
                                String username,
                                long requestCount,
                                Double averageResponseTime) {
                }
        }

        /**
         * Performance metrics with percentiles.
         */
        public record PerformanceMetrics(
                        Double p50ResponseTime,
                        Double p95ResponseTime,
                        Double p99ResponseTime,
                        Double minResponseTime,
                        Double maxResponseTime,
                        Double averageResponseTime,
                        List<SlowEndpoint> slowestEndpoints) {

                public record SlowEndpoint(
                                String uri,
                                String method,
                                long requestCount,
                                Double averageResponseTime,
                                Double maxResponseTime) {
                }
        }

        /**
         * Security monitoring insights.
         */
        public record SecurityInsights(
                        long totalFailedAuthAttempts,
                        List<FailedAuthIp> topFailedAuthIps,
                        List<SuspiciousIp> suspiciousIps,
                        BotTraffic botTraffic) {

                public record FailedAuthIp(
                                String ipAddress,
                                String country,
                                long attemptCount) {
                }

                public record SuspiciousIp(
                                String ipAddress,
                                String country,
                                long requestCount) {
                }

                public record BotTraffic(
                                long botRequests,
                                long humanRequests,
                                Double botPercentage) {
                }
        }

        /**
         * Device and browser intelligence.
         */
        public record DeviceIntelligence(
                        Map<String, DeviceStats> deviceDistribution,
                        List<BrowserShare> browserMarketShare,
                        List<OsShare> osMarketShare) {

                public record DeviceStats(
                                long count,
                                Double percentage) {
                }

                public record BrowserShare(
                                String browser,
                                String version,
                                long count,
                                Double percentage) {
                }

                public record OsShare(
                                String os,
                                String version,
                                long count,
                                Double percentage) {
                }
        }

        /**
         * Geographic analytics and distribution.
         */
        public record GeographicAnalytics(
                        List<CountryStats> topCountries,
                        List<CityStats> topCities,
                        List<IpStats> topIps,
                        List<TimezoneStats> topTimezones,
                        List<IspStats> topIsps) {

                public record CountryStats(
                                String country,
                                long count) {
                }

                public record CityStats(
                                String city,
                                String country,
                                long count) {
                }

                public record IpStats(
                                String ip,
                                String country,
                                String city,
                                long count) {
                }

                public record TimezoneStats(
                                String timezone,
                                long count) {
                }

                public record IspStats(
                                String isp,
                                String country,
                                long count) {
                }
        }

        /**
         * Time-based traffic distribution.
         */
        public record TimeDistribution(
                        List<HourlyStats> hourlyDistribution,
                        List<PeakHour> peakHoursByDayOfWeek) {

                public record HourlyStats(
                                int hour,
                                long count) {
                }

                public record PeakHour(
                                int dayOfWeek,
                                int hour,
                                long count,
                                String dayName) {
                }
        }

        /**
         * Error analysis and patterns.
         */
        public record ErrorAnalytics(
                        long totalErrors,
                        Double errorRate,
                        List<ErrorByEndpoint> errorsByEndpoint,
                        List<ErrorStatusDistribution> statusDistribution,
                        List<ExceptionPattern> exceptionPatterns) {

                public record ErrorByEndpoint(
                                String uri,
                                String method,
                                int statusCode,
                                long count) {
                }

                public record ErrorStatusDistribution(
                                int statusCode,
                                long count,
                                Double percentage) {
                }

                public record ExceptionPattern(
                                String exceptionClass,
                                long count) {
                }
        }

        /**
         * Session analytics.
         */
        public record SessionAnalytics(
                        long totalSessions,
                        long newSessions,
                        long returningSessions,
                        Double newSessionRate) {
        }

        /**
         * Content and API performance.
         */
        public record ContentPerformance(
                        List<EndpointStats> mostAccessedEndpoints,
                        List<EndpointStats> leastAccessedEndpoints,
                        List<RefererStats> topReferrers,
                        List<MethodDistribution> methodDistribution) {

                public record EndpointStats(
                                String uri,
                                String method,
                                long count,
                                Double averageResponseTime) {
                }

                public record RefererStats(
                                String referer,
                                long count) {
                }

                public record MethodDistribution(
                                String method,
                                long count,
                                Double percentage) {
                }
        }

        // ========================================
        // Enterprise Analytics Features
        // ========================================

        /**
         * Throughput metrics - requests per second/minute.
         */
        public record ThroughputMetrics(
                        Double requestsPerSecond,
                        Double requestsPerMinute,
                        Double requestsPerHour,
                        Double peakRequestsPerMinute,
                        Instant peakTime,
                        List<MinutelyThroughput> recentThroughput) {

                public record MinutelyThroughput(
                                Instant minute,
                                long count,
                                Double requestsPerSecond) {
                }
        }

        /**
         * Apdex (Application Performance Index) score.
         * Industry standard: 0.0 (unacceptable) to 1.0 (excellent)
         * 
         * Apdex = (Satisfied + Tolerating/2) / Total
         * - Satisfied: response <= satisfiedThreshold (default 500ms)
         * - Tolerating: satisfiedThreshold < response <= frustratedThreshold (default
         * 2000ms)
         * - Frustrated: response > frustratedThreshold
         */
        public record ApdexMetrics(
                        Double apdexScore,
                        String apdexRating,
                        long satisfiedCount,
                        long toleratingCount,
                        long frustratedCount,
                        long totalSamples,
                        long satisfiedThresholdMs,
                        long frustratedThresholdMs,
                        String interpretation) {
        }

        /**
         * SLA (Service Level Agreement) metrics.
         */
        public record SlaMetrics(
                        Double availabilityPercentage,
                        Double successRate,
                        long successfulRequests,
                        long failedRequests,
                        long totalRequests,
                        long uptimeHours,
                        long downtimeHours,
                        long totalHoursInPeriod,
                        List<DowntimePeriod> downtimePeriods) {

                public record DowntimePeriod(
                                Instant startTime,
                                Instant endTime,
                                long durationMinutes) {
                }
        }

        /**
         * Trend analytics - latency and error rate over time.
         */
        public record TrendAnalytics(
                        List<LatencyTrend> latencyTrend,
                        List<ErrorRateTrend> errorRateTrend,
                        TrendSummary latencySummary,
                        TrendSummary errorRateSummary) {

                public record LatencyTrend(
                                Instant hour,
                                Double p50,
                                Double p95,
                                Double p99,
                                Double avgResponseTime,
                                long requestCount) {
                }

                public record ErrorRateTrend(
                                Instant hour,
                                long totalRequests,
                                long errorCount,
                                Double errorRate) {
                }

                public record TrendSummary(
                                String direction,
                                Double changePercentage,
                                Double currentValue,
                                Double previousValue,
                                String interpretation) {
                }
        }

        /**
         * Period comparison - compare current period vs previous period.
         * e.g., "This week vs Last week", "Today vs Yesterday"
         */
        public record PeriodComparison(
                        PeriodStats currentPeriod,
                        PeriodStats previousPeriod,
                        PeriodDelta delta,
                        String comparisonType) {

                public record PeriodStats(
                                String periodLabel,
                                Instant startTime,
                                Instant endTime,
                                long totalRequests,
                                long uniqueUsers,
                                long uniqueSessions,
                                Double avgResponseTime,
                                long errorCount,
                                long successCount,
                                Double errorRate,
                                Double successRate) {
                }

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
        }

        /**
         * Active users metrics - real-time user activity.
         */
        public record ActiveUsersMetrics(
                        long activeUsersLast5Min,
                        long activeUsersLast15Min,
                        long activeUsersLast30Min,
                        long activeUsersLast60Min,
                        long activeSessionsLast5Min,
                        long activeSessionsLast15Min,
                        long activeSessionsLast30Min,
                        long activeSessionsLast60Min,
                        List<ActiveUser> recentActiveUsers) {

                public record ActiveUser(
                                String userId,
                                long requestCount,
                                Instant lastActivityTime) {
                }
        }

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
}
