package com.mmva.newsapp.infrastructure.requestanalytics.service;

import com.mmva.newsapp.infrastructure.requestanalytics.dto.ComprehensiveAnalyticsResponseDto;
import com.mmva.newsapp.infrastructure.requestanalytics.dto.RequestAnalyticsInfoDto;
import com.mmva.newsapp.infrastructure.requestanalytics.dto.UserAnalytics;
import com.mmva.newsapp.infrastructure.requestanalytics.model.RequestAnalytics;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Service interface for logging and analyzing HTTP request analytics.
 * 
 * <p>
 * Provides:
 * </p>
 * <ul>
 * <li>Async request logging for non-blocking performance</li>
 * <li>Traffic statistics and reporting</li>
 * <li>Performance analytics</li>
 * <li>Security monitoring helpers</li>
 * <li>Data cleanup operations</li>
 * </ul>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
public interface RequestAnalyticsService {

        // ========================================
        // Logging Methods
        // ========================================

        /**
         * Asynchronously logs request analytics (non-blocking).
         * 
         * @param info             The request analytics info to log
         * @param userId           Optional user ID
         * @param username         Optional username
         * @param responseStatus   HTTP response status code
         * @param processingTimeMs Time taken to process the request
         */
        void logRequestAsync(RequestAnalyticsInfoDto info, UUID userId, String username,
                        Integer responseStatus, Long processingTimeMs);

        /**
         * Asynchronously logs request analytics with error information.
         * 
         * @param info             The request analytics info to log
         * @param userId           Optional user ID
         * @param username         Optional username
         * @param responseStatus   HTTP response status code
         * @param processingTimeMs Time taken to process the request
         * @param errorMessage     Error message if any
         * @param exceptionClass   Exception class name if any
         */
        void logRequestWithErrorAsync(RequestAnalyticsInfoDto info, UUID userId, String username,
                        Integer responseStatus, Long processingTimeMs,
                        String errorMessage, String exceptionClass);

        /**
         * Synchronously logs request analytics (blocking).
         * 
         * @param info             The request analytics info to log
         * @param userId           Optional user ID
         * @param username         Optional username
         * @param responseStatus   HTTP response status code
         * @param processingTimeMs Time taken to process the request
         * @param errorMessage     Error message if any
         * @param exceptionClass   Exception class name if any
         * @return The saved RequestAnalytics entity
         */
        RequestAnalytics logRequest(RequestAnalyticsInfoDto info, UUID userId, String username,
                        Integer responseStatus, Long processingTimeMs,
                        String errorMessage, String exceptionClass);

        // ========================================
        // Statistics Methods
        // ========================================

        /**
         * Gets traffic statistics for a time period.
         * 
         * @param start Start time
         * @param end   End time
         * @return Traffic statistics including requests by endpoint, device, browser,
         *         OS, status, country, city, IP
         */
        TrafficStatistics getTrafficStatistics(Instant start, Instant end);

        /**
         * Gets endpoint performance statistics.
         * 
         * @param start Start time
         * @param end   End time
         * @return List of endpoint performance metrics
         */
        List<EndpointPerformance> getEndpointPerformance(Instant start, Instant end);

        /**
         * Gets hourly traffic pattern.
         * 
         * @param start Start time
         * @param end   End time
         * @return List of hourly traffic data points
         */
        List<HourlyTraffic> getHourlyTraffic(Instant start, Instant end);

        /**
         * Gets daily traffic pattern.
         * 
         * @param start Start time
         * @param end   End time
         * @return List of daily traffic data points
         */
        List<DailyTraffic> getDailyTraffic(Instant start, Instant end);

        /**
         * Finds potentially suspicious IPs.
         * 
         * @param start           Start time
         * @param end             End time
         * @param volumeThreshold Request volume threshold for suspicious detection
         * @param errorThreshold  Error count threshold for suspicious detection
         * @return List of suspicious IP information
         */
        List<SuspiciousIp> findSuspiciousIps(Instant start, Instant end, long volumeThreshold,
                        long errorThreshold);

        /**
         * Gets user analytics including registration, activity, and engagement metrics.
         * 
         * @param start Start time
         * @param end   End time
         * @return User analytics data
         */
        UserAnalytics getUserAnalytics(Instant start, Instant end);

        /**
         * Get comprehensive analytics covering all dimensions.
         * 
         * @param start Start time
         * @param end   End time
         * @return Complete analytics response with all metrics
         */
        ComprehensiveAnalyticsResponseDto getComprehensiveAnalytics(Instant start, Instant end);

        // ========================================
        // Cleanup Methods
        // ========================================

        /**
         * Deletes analytics older than the specified retention period.
         * 
         * @param retentionPeriod Duration to retain analytics data
         * @return Number of deleted records
         */
        int cleanupOldAnalytics(Duration retentionPeriod);

        // ========================================
        // Inner Record Classes for Results
        // ========================================

        /**
         * Traffic statistics summary.
         */
        record TrafficStatistics(
                        long totalRequests,
                        List<Object[]> requestsByEndpoint,
                        List<Object[]> requestsByDeviceType,
                        List<Object[]> requestsByBrowser,
                        List<Object[]> requestsByOs,
                        List<Object[]> requestsByStatus,
                        List<Object[]> requestsByCountry,
                        List<Object[]> requestsByCity,
                        List<Object[]> requestsByIp) {
        }

        /**
         * Endpoint performance metrics.
         */
        record EndpointPerformance(
                        String endpoint,
                        String method,
                        Double avgResponseTimeMs,
                        Long maxResponseTimeMs,
                        Long minResponseTimeMs,
                        Long requestCount) {
        }

        /**
         * Hourly traffic data point.
         */
        record HourlyTraffic(
                        Instant hour,
                        long requestCount) {
        }

        /**
         * Daily traffic data point.
         */
        record DailyTraffic(
                        Instant day,
                        long requestCount) {
        }

        /**
         * Suspicious IP information.
         */
        record SuspiciousIp(
                        String ipAddress,
                        long requestCount,
                        long errorCount,
                        String reason) {
        }
}
