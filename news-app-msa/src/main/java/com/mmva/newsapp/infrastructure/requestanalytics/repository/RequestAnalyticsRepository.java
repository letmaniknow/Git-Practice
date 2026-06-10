package com.mmva.newsapp.infrastructure.requestanalytics.repository;

import com.mmva.newsapp.infrastructure.requestanalytics.model.RequestAnalytics;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Repository for request analytics data with rich querying capabilities.
 * 
 * <p>
 * Provides comprehensive analytics queries for:
 * </p>
 * <ul>
 * <li>Traffic analysis and statistics</li>
 * <li>User behavior tracking</li>
 * <li>Performance monitoring</li>
 * <li>Security analytics</li>
 * <li>Device and browser statistics</li>
 * </ul>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Repository
public interface RequestAnalyticsRepository extends JpaRepository<RequestAnalytics, UUID> {

        // ========================================
        // Basic Queries
        // ========================================

        /**
         * Find analytics by client IP address within a time range.
         */
        List<RequestAnalytics> findByClientIpAddressAndCreatedAtBetween(
                        String clientIpAddress, Instant start, Instant end);

        /**
         * Find analytics by user ID.
         */
        List<RequestAnalytics> findByUserIdOrderByCreatedAtDesc(UUID userId);

        /**
         * Find analytics by endpoint.
         */
        List<RequestAnalytics> findByRequestUriContainingOrderByCreatedAtDesc(String uriPattern);

        /**
         * Find analytics by request ID (for tracing).
         */
        List<RequestAnalytics> findByRequestIdOrCorrelationId(String requestId, String correlationId);

        /**
         * Find recent analytics with errors.
         */
        List<RequestAnalytics> findByHasErrorTrueAndCreatedAtAfterOrderByCreatedAtDesc(Instant after);

        // ========================================
        // Aggregation Queries
        // ========================================

        /**
         * Count requests by device type.
         */
        @Query("SELECT r.deviceType, COUNT(r) FROM RequestAnalytics r " +
                        "WHERE r.createdAt BETWEEN :start AND :end AND r.deviceType IS NOT NULL " +
                        "GROUP BY r.deviceType " +
                        "ORDER BY COUNT(r) DESC")
        List<Object[]> countByDeviceTypeBetween(@Param("start") Instant start, @Param("end") Instant end);

        /**
         * Count requests by browser.
         */
        @Query("SELECT r.browserName, COUNT(r) FROM RequestAnalytics r " +
                        "WHERE r.createdAt BETWEEN :start AND :end AND r.browserName IS NOT NULL " +
                        "GROUP BY r.browserName " +
                        "ORDER BY COUNT(r) DESC")
        List<Object[]> countByBrowserBetween(@Param("start") Instant start, @Param("end") Instant end);

        /**
         * Count requests by OS.
         */
        @Query("SELECT r.osName, COUNT(r) FROM RequestAnalytics r " +
                        "WHERE r.createdAt BETWEEN :start AND :end AND r.osName IS NOT NULL " +
                        "GROUP BY r.osName " +
                        "ORDER BY COUNT(r) DESC")
        List<Object[]> countByOsBetween(@Param("start") Instant start, @Param("end") Instant end);

        /**
         * Count requests by country (if geo-enriched).
         */
        @Query("SELECT r.geoCountry, COUNT(r) FROM RequestAnalytics r " +
                        "WHERE r.createdAt BETWEEN :start AND :end AND r.geoCountry IS NOT NULL " +
                        "GROUP BY r.geoCountry " +
                        "ORDER BY COUNT(r) DESC")
        List<Object[]> countByCountryBetween(@Param("start") Instant start, @Param("end") Instant end);

        /**
         * Count requests by city (if geo-enriched).
         */
        @Query("SELECT r.geoCity, r.geoCountry, COUNT(r) FROM RequestAnalytics r " +
                        "WHERE r.createdAt BETWEEN :start AND :end AND r.geoCity IS NOT NULL " +
                        "GROUP BY r.geoCity, r.geoCountry " +
                        "ORDER BY COUNT(r) DESC")
        List<Object[]> countByCityBetween(@Param("start") Instant start, @Param("end") Instant end);

        /**
         * Count requests by IP address.
         */
        @Query("SELECT r.clientIpAddress, r.geoCountry, r.geoCity, COUNT(r) FROM RequestAnalytics r " +
                        "WHERE r.createdAt BETWEEN :start AND :end AND r.clientIpAddress IS NOT NULL " +
                        "GROUP BY r.clientIpAddress, r.geoCountry, r.geoCity " +
                        "ORDER BY COUNT(r) DESC")
        List<Object[]> countByIpAddressBetween(@Param("start") Instant start, @Param("end") Instant end);

        /**
         * Get average response time by endpoint.
         */
        @Query("SELECT r.requestUri, r.method, AVG(r.processingTimeMs), MAX(r.processingTimeMs), MIN(r.processingTimeMs), COUNT(r) "
                        +
                        "FROM RequestAnalytics r " +
                        "WHERE r.createdAt BETWEEN :start AND :end AND r.processingTimeMs IS NOT NULL " +
                        "GROUP BY r.requestUri, r.method " +
                        "ORDER BY AVG(r.processingTimeMs) DESC")
        List<Object[]> getAverageResponseTimeByEndpoint(@Param("start") Instant start, @Param("end") Instant end);

        /**
         * Count requests by HTTP status code.
         */
        @Query("SELECT r.responseStatus, COUNT(r) FROM RequestAnalytics r " +
                        "WHERE r.createdAt BETWEEN :start AND :end AND r.responseStatus IS NOT NULL " +
                        "GROUP BY r.responseStatus " +
                        "ORDER BY r.responseStatus")
        List<Object[]> countByResponseStatusBetween(@Param("start") Instant start, @Param("end") Instant end);

        /**
         * Get error rate by endpoint.
         */
        @Query("SELECT r.requestUri, r.method, " +
                        "SUM(CASE WHEN r.responseStatus >= 400 THEN 1 ELSE 0 END), " +
                        "COUNT(r), " +
                        "CAST(SUM(CASE WHEN r.responseStatus >= 400 THEN 1.0 ELSE 0.0 END) / COUNT(r) * 100 AS double) "
                        +
                        "FROM RequestAnalytics r " +
                        "WHERE r.createdAt BETWEEN :start AND :end " +
                        "GROUP BY r.requestUri, r.method " +
                        "HAVING SUM(CASE WHEN r.responseStatus >= 400 THEN 1 ELSE 0 END) > 0 " +
                        "ORDER BY SUM(CASE WHEN r.responseStatus >= 400 THEN 1 ELSE 0 END) DESC")
        List<Object[]> getErrorRateByEndpoint(@Param("start") Instant start, @Param("end") Instant end);

        // ========================================
        // Time-based Analytics
        // ========================================

        /**
         * Count requests per hour for traffic patterns.
         * PostgreSQL native query using date_trunc().
         */
        @Query(value = "SELECT date_trunc('hour', created_at) as hour, COUNT(*) " +
                        "FROM request_analytics " +
                        "WHERE created_at BETWEEN :start AND :end " +
                        "GROUP BY date_trunc('hour', created_at) " +
                        "ORDER BY hour", nativeQuery = true)
        List<Object[]> countByHourBetween(@Param("start") Instant start, @Param("end") Instant end);

        /**
         * Count requests per day.
         * PostgreSQL native query using date_trunc().
         */
        @Query(value = "SELECT date_trunc('day', created_at) as day, COUNT(*) " +
                        "FROM request_analytics " +
                        "WHERE created_at BETWEEN :start AND :end " +
                        "GROUP BY date_trunc('day', created_at) " +
                        "ORDER BY day", nativeQuery = true)
        List<Object[]> countByDayBetween(@Param("start") Instant start, @Param("end") Instant end);

        // ========================================
        // Security Analytics
        // ========================================

        /**
         * Find high-volume IPs (potential DDoS or scraping).
         */
        @Query("SELECT r.clientIpAddress, COUNT(r) FROM RequestAnalytics r " +
                        "WHERE r.createdAt BETWEEN :start AND :end " +
                        "GROUP BY r.clientIpAddress " +
                        "HAVING COUNT(r) > :threshold " +
                        "ORDER BY COUNT(r) DESC")
        List<Object[]> findHighVolumeIps(@Param("start") Instant start, @Param("end") Instant end,
                        @Param("threshold") long threshold);

        /**
         * Find IPs with high error rates (potential attackers).
         */
        @Query("SELECT r.clientIpAddress, " +
                        "SUM(CASE WHEN r.responseStatus >= 400 THEN 1 ELSE 0 END) as errors, " +
                        "COUNT(r) as total " +
                        "FROM RequestAnalytics r " +
                        "WHERE r.createdAt BETWEEN :start AND :end " +
                        "GROUP BY r.clientIpAddress " +
                        "HAVING SUM(CASE WHEN r.responseStatus >= 400 THEN 1 ELSE 0 END) > :threshold " +
                        "ORDER BY errors DESC")
        List<Object[]> findHighErrorRateIps(@Param("start") Instant start, @Param("end") Instant end,
                        @Param("threshold") long threshold);

        /**
         * Find bot traffic.
         */
        @Query("SELECT r.userAgent, COUNT(r) FROM RequestAnalytics r " +
                        "WHERE r.createdAt BETWEEN :start AND :end AND r.isBot = true " +
                        "GROUP BY r.userAgent " +
                        "ORDER BY COUNT(r) DESC")
        List<Object[]> findBotTrafficBetween(@Param("start") Instant start, @Param("end") Instant end);

        // ========================================
        // Cleanup Operations
        // ========================================

        /**
         * Delete analytics older than a certain date.
         */
        @Modifying
        @Query("DELETE FROM RequestAnalytics r WHERE r.createdAt < :before")
        int deleteOlderThan(@Param("before") Instant before);

        /**
         * Count records in a time range.
         */
        long countByCreatedAtBetween(Instant start, Instant end);

        /**
         * Count total records.
         */
        @Query("SELECT COUNT(r) FROM RequestAnalytics r")
        long countTotal();

        // ========================================
        // User Analytics
        // ========================================

        /**
         * Count unique users in time range.
         */
        @Query("SELECT COUNT(DISTINCT r.userId) FROM RequestAnalytics r " +
                        "WHERE r.createdAt BETWEEN :start AND :end AND r.userId IS NOT NULL")
        Long countUniqueUsersBetween(@Param("start") Instant start, @Param("end") Instant end);

        /**
         * Count authenticated vs anonymous requests.
         */
        @Query("SELECT CASE WHEN r.userId IS NOT NULL THEN 'authenticated' ELSE 'anonymous' END, COUNT(r) " +
                        "FROM RequestAnalytics r " +
                        "WHERE r.createdAt BETWEEN :start AND :end " +
                        "GROUP BY CASE WHEN r.userId IS NOT NULL THEN 'authenticated' ELSE 'anonymous' END")
        List<Object[]> countAuthenticatedVsAnonymousBetween(@Param("start") Instant start,
                        @Param("end") Instant end);

        /**
         * Get top users by request count.
         */
        @Query("SELECT r.userId, r.username, COUNT(r), AVG(r.processingTimeMs) " +
                        "FROM RequestAnalytics r " +
                        "WHERE r.createdAt BETWEEN :start AND :end AND r.userId IS NOT NULL " +
                        "GROUP BY r.userId, r.username " +
                        "ORDER BY COUNT(r) DESC")
        Page<Object[]> getTopUsersBetween(@Param("start") Instant start, @Param("end") Instant end,
                        Pageable pageable);

        // ========================================
        // Performance Percentiles & Metrics
        // ========================================

        /**
         * Get response time percentiles (P50, P95, P99).
         * 
         * DATABASE COMPATIBILITY:
         * - SQL Server (DEV): Uses PERCENTILE_CONT with OVER() clause + TOP 1
         * - PostgreSQL (PROD): Uses PERCENTILE_CONT as aggregate function - see
         * commented version below
         * 
         * TODO: Before production deployment, uncomment PostgreSQL version and comment
         * SQL Server version.
         */
        // ===== PostgreSQL Version (ACTIVE) =====
        @Query(value = "SELECT " +
                        "PERCENTILE_CONT(0.50) WITHIN GROUP (ORDER BY processing_time_ms) as p50, " +
                        "PERCENTILE_CONT(0.95) WITHIN GROUP (ORDER BY processing_time_ms) as p95, " +
                        "PERCENTILE_CONT(0.99) WITHIN GROUP (ORDER BY processing_time_ms) as p99, " +
                        "MIN(processing_time_ms) as min_time, " +
                        "MAX(processing_time_ms) as max_time " +
                        "FROM request_analytics " +
                        "WHERE created_at BETWEEN :start AND :end AND processing_time_ms IS NOT NULL", nativeQuery = true)
        List<Object[]> getResponseTimePercentilesBetween(@Param("start") Instant start, @Param("end") Instant end);

        /**
         * Get slowest endpoints by average response time.
         */
        @Query("SELECT r.requestUri, r.method, COUNT(r), AVG(r.processingTimeMs), MAX(r.processingTimeMs) " +
                        "FROM RequestAnalytics r " +
                        "WHERE r.createdAt BETWEEN :start AND :end AND r.processingTimeMs IS NOT NULL " +
                        "GROUP BY r.requestUri, r.method " +
                        "ORDER BY AVG(r.processingTimeMs) DESC")
        Page<Object[]> getSlowestEndpointsBetween(@Param("start") Instant start, @Param("end") Instant end,
                        Pageable pageable);

        /**
         * Get overall error rate percentage.
         */
        @Query("SELECT COUNT(CASE WHEN r.hasError = true THEN 1 END) * 100.0 / COUNT(*) " +
                        "FROM RequestAnalytics r " +
                        "WHERE r.createdAt BETWEEN :start AND :end")
        Double getErrorRateBetween(@Param("start") Instant start, @Param("end") Instant end);

        // ========================================
        // Security Analytics
        // ========================================

        /**
         * Get IPs with most failed authentication attempts.
         */
        @Query("SELECT r.clientIpAddress, r.geoCountry, COUNT(r) " +
                        "FROM RequestAnalytics r " +
                        "WHERE r.createdAt BETWEEN :start AND :end " +
                        "AND (r.responseStatus = 401 OR r.responseStatus = 403) " +
                        "GROUP BY r.clientIpAddress, r.geoCountry " +
                        "ORDER BY COUNT(r) DESC")
        Page<Object[]> getFailedAuthAttemptsBetween(@Param("start") Instant start, @Param("end") Instant end,
                        Pageable pageable);

        /**
         * Get suspicious IPs exceeding request threshold.
         */
        @Query("SELECT r.clientIpAddress, r.geoCountry, COUNT(r) " +
                        "FROM RequestAnalytics r " +
                        "WHERE r.createdAt BETWEEN :start AND :end " +
                        "GROUP BY r.clientIpAddress, r.geoCountry " +
                        "HAVING COUNT(r) > :threshold " +
                        "ORDER BY COUNT(r) DESC")
        Page<Object[]> getSuspiciousIpsBetween(@Param("start") Instant start, @Param("end") Instant end,
                        @Param("threshold") long threshold, Pageable pageable);

        /**
         * Get bot vs human traffic breakdown.
         */
        @Query("SELECT CASE WHEN r.isBot = true THEN 'bot' ELSE 'human' END, COUNT(r) " +
                        "FROM RequestAnalytics r " +
                        "WHERE r.createdAt BETWEEN :start AND :end " +
                        "GROUP BY CASE WHEN r.isBot = true THEN 'bot' ELSE 'human' END")
        List<Object[]> getBotVsHumanTrafficBetween(@Param("start") Instant start, @Param("end") Instant end);

        // ========================================
        // Device Intelligence
        // ========================================

        /**
         * Get device type distribution with percentages.
         */
        @Query("SELECT r.deviceType, COUNT(r), " +
                        "COUNT(r) * 100.0 / (SELECT COUNT(*) FROM RequestAnalytics WHERE createdAt BETWEEN :start AND :end) "
                        +
                        "FROM RequestAnalytics r " +
                        "WHERE r.createdAt BETWEEN :start AND :end AND r.deviceType IS NOT NULL " +
                        "GROUP BY r.deviceType " +
                        "ORDER BY COUNT(r) DESC")
        List<Object[]> getDeviceDistributionBetween(@Param("start") Instant start, @Param("end") Instant end);

        /**
         * Get browser market share with percentages.
         */
        @Query("SELECT r.browserName, r.browserVersion, COUNT(r), " +
                        "COUNT(r) * 100.0 / (SELECT COUNT(*) FROM RequestAnalytics WHERE createdAt BETWEEN :start AND :end) "
                        +
                        "FROM RequestAnalytics r " +
                        "WHERE r.createdAt BETWEEN :start AND :end AND r.browserName IS NOT NULL " +
                        "GROUP BY r.browserName, r.browserVersion " +
                        "ORDER BY COUNT(r) DESC")
        Page<Object[]> getBrowserMarketShareBetween(@Param("start") Instant start, @Param("end") Instant end,
                        Pageable pageable);

        /**
         * Get OS market share with percentages.
         */
        @Query("SELECT r.osName, r.osVersion, COUNT(r), " +
                        "COUNT(r) * 100.0 / (SELECT COUNT(*) FROM RequestAnalytics WHERE createdAt BETWEEN :start AND :end) "
                        +
                        "FROM RequestAnalytics r " +
                        "WHERE r.createdAt BETWEEN :start AND :end AND r.osName IS NOT NULL " +
                        "GROUP BY r.osName, r.osVersion " +
                        "ORDER BY COUNT(r) DESC")
        Page<Object[]> getOsMarketShareBetween(@Param("start") Instant start, @Param("end") Instant end,
                        Pageable pageable);

        // ========================================
        // Time Distribution
        // ========================================

        /**
         * Get request distribution by hour of day.
         * 
         * DATABASE COMPATIBILITY:
         * - SQL Server (DEV): Uses DATEPART(HOUR, ...) for hour extraction
         * - PostgreSQL (PROD): Uses EXTRACT(HOUR FROM ...) - see commented version
         * below
         * 
         * TODO: Before production deployment, uncomment PostgreSQL version and comment
         * SQL Server version.
         */
        @Query(value = "SELECT EXTRACT(HOUR FROM created_at) as hour, COUNT(*) " +
                        "FROM request_analytics " +
                        "WHERE created_at BETWEEN :start AND :end " +
                        "GROUP BY EXTRACT(HOUR FROM created_at) " +
                        "ORDER BY hour", nativeQuery = true)
        List<Object[]> getHourlyDistributionBetween(@Param("start") Instant start, @Param("end") Instant end);

        /**
         * Get peak hours by day of week.
         * 
         * DATABASE COMPATIBILITY:
         * - SQL Server (DEV): Uses DATEPART(WEEKDAY, ...) - 1 (1=Sunday, 7=Saturday,
         * subtract 1 to get 0-6)
         * - PostgreSQL (PROD): Uses EXTRACT(DOW FROM ...) (0=Sunday, 6=Saturday) - see
         * commented version below
         * 
         * TODO: Before production deployment, uncomment PostgreSQL version and comment
         * SQL Server version.
         */
        // ===== PostgreSQL Version (ACTIVE) =====
        @Query(value = "SELECT EXTRACT(DOW FROM created_at) as day_of_week, EXTRACT(HOUR FROM created_at) as hour, COUNT(*) "
                        +
                        "FROM request_analytics " +
                        "WHERE created_at BETWEEN :start AND :end " +
                        "GROUP BY EXTRACT(DOW FROM created_at), EXTRACT(HOUR FROM created_at) " +
                        "ORDER BY day_of_week, hour", nativeQuery = true)
        List<Object[]> getPeakHoursByDayOfWeekBetween(@Param("start") Instant start, @Param("end") Instant end);

        // ========================================
        // Error Analysis
        // ========================================

        /**
         * Get error breakdown by endpoint.
         */
        @Query("SELECT r.requestUri, r.method, r.responseStatus, COUNT(r) " +
                        "FROM RequestAnalytics r " +
                        "WHERE r.createdAt BETWEEN :start AND :end " +
                        "AND (r.responseStatus >= 400 OR r.hasError = true) " +
                        "GROUP BY r.requestUri, r.method, r.responseStatus " +
                        "ORDER BY COUNT(r) DESC")
        Page<Object[]> getErrorBreakdownByEndpointBetween(@Param("start") Instant start, @Param("end") Instant end,
                        Pageable pageable);

        /**
         * Get error status code distribution.
         */
        @Query("SELECT r.responseStatus, COUNT(r), " +
                        "COUNT(r) * 100.0 / (SELECT COUNT(*) FROM RequestAnalytics WHERE createdAt BETWEEN :start AND :end AND responseStatus >= 400) "
                        +
                        "FROM RequestAnalytics r " +
                        "WHERE r.createdAt BETWEEN :start AND :end AND r.responseStatus >= 400 " +
                        "GROUP BY r.responseStatus " +
                        "ORDER BY COUNT(r) DESC")
        List<Object[]> getErrorStatusDistributionBetween(@Param("start") Instant start, @Param("end") Instant end);

        /**
         * Get common exception patterns.
         */
        @Query("SELECT r.exceptionClass, COUNT(r) " +
                        "FROM RequestAnalytics r " +
                        "WHERE r.createdAt BETWEEN :start AND :end AND r.exceptionClass IS NOT NULL " +
                        "GROUP BY r.exceptionClass " +
                        "ORDER BY COUNT(r) DESC")
        Page<Object[]> getExceptionPatternsBetween(@Param("start") Instant start, @Param("end") Instant end,
                        Pageable pageable);

        // ========================================
        // Session Analytics
        // ========================================

        /**
         * Count unique sessions in time range.
         */
        @Query("SELECT COUNT(DISTINCT r.sessionId) FROM RequestAnalytics r " +
                        "WHERE r.createdAt BETWEEN :start AND :end AND r.sessionId IS NOT NULL")
        Long countUniqueSessionsBetween(@Param("start") Instant start, @Param("end") Instant end);

        /**
         * Get new vs returning sessions.
         */
        @Query("SELECT CASE WHEN r.isNewSession = true THEN 'new' ELSE 'returning' END, COUNT(DISTINCT r.sessionId) "
                        +
                        "FROM RequestAnalytics r " +
                        "WHERE r.createdAt BETWEEN :start AND :end AND r.sessionId IS NOT NULL " +
                        "GROUP BY CASE WHEN r.isNewSession = true THEN 'new' ELSE 'returning' END")
        List<Object[]> getNewVsReturningSessionsBetween(@Param("start") Instant start, @Param("end") Instant end);

        // ========================================
        // Geographic Insights
        // ========================================

        /**
         * Get requests by timezone.
         */
        @Query("SELECT r.geoTimezone, COUNT(r) " +
                        "FROM RequestAnalytics r " +
                        "WHERE r.createdAt BETWEEN :start AND :end AND r.geoTimezone IS NOT NULL " +
                        "GROUP BY r.geoTimezone " +
                        "ORDER BY COUNT(r) DESC")
        Page<Object[]> getRequestsByTimezoneBetween(@Param("start") Instant start, @Param("end") Instant end,
                        Pageable pageable);

        /**
         * Get top ISPs by request count.
         */
        @Query("SELECT r.geoIsp, r.geoCountry, COUNT(r) " +
                        "FROM RequestAnalytics r " +
                        "WHERE r.createdAt BETWEEN :start AND :end AND r.geoIsp IS NOT NULL " +
                        "GROUP BY r.geoIsp, r.geoCountry " +
                        "ORDER BY COUNT(r) DESC")
        Page<Object[]> getTopIspsBetween(@Param("start") Instant start, @Param("end") Instant end,
                        Pageable pageable);

        // ========================================
        // Content Performance
        // ========================================

        /**
         * Get top referrers.
         */
        @Query("SELECT r.referer, COUNT(r) " +
                        "FROM RequestAnalytics r " +
                        "WHERE r.createdAt BETWEEN :start AND :end AND r.referer IS NOT NULL " +
                        "GROUP BY r.referer " +
                        "ORDER BY COUNT(r) DESC")
        Page<Object[]> getTopReferersBetween(@Param("start") Instant start, @Param("end") Instant end,
                        Pageable pageable);

        /**
         * Get least accessed endpoints.
         */
        @Query("SELECT r.requestUri, r.method, COUNT(r), AVG(r.processingTimeMs) " +
                        "FROM RequestAnalytics r " +
                        "WHERE r.createdAt BETWEEN :start AND :end " +
                        "GROUP BY r.requestUri, r.method " +
                        "ORDER BY COUNT(r) ASC")
        Page<Object[]> getLeastAccessedEndpointsBetween(@Param("start") Instant start, @Param("end") Instant end,
                        Pageable pageable);

        /**
         * Count requests by endpoint (most accessed).
         */
        @Query("SELECT r.requestUri, r.method, COUNT(r), AVG(r.processingTimeMs) " +
                        "FROM RequestAnalytics r " +
                        "WHERE r.createdAt BETWEEN :start AND :end " +
                        "GROUP BY r.requestUri, r.method " +
                        "ORDER BY COUNT(r) DESC")
        List<Object[]> countByEndpointBetween(@Param("start") Instant start, @Param("end") Instant end);

        /**
         * Get request method distribution (GET, POST, PUT, DELETE).
         */
        @Query("SELECT r.method, COUNT(r), " +
                        "COUNT(r) * 100.0 / (SELECT COUNT(*) FROM RequestAnalytics WHERE createdAt BETWEEN :start AND :end) "
                        +
                        "FROM RequestAnalytics r " +
                        "WHERE r.createdAt BETWEEN :start AND :end " +
                        "GROUP BY r.method " +
                        "ORDER BY COUNT(r) DESC")
        List<Object[]> getRequestMethodDistributionBetween(@Param("start") Instant start, @Param("end") Instant end);

        // ========================================
        // Enterprise Analytics - Throughput
        // ========================================

        /**
         * Get total request count for throughput calculation.
         */
        @Query("SELECT COUNT(r) FROM RequestAnalytics r WHERE r.createdAt BETWEEN :start AND :end")
        Long getTotalRequestCountBetween(@Param("start") Instant start, @Param("end") Instant end);

        /**
         * Get requests per minute for a time range.
         * PostgreSQL compatible.
         */
        @Query(value = "SELECT date_trunc('minute', created_at) as minute, COUNT(*) " +
                        "FROM request_analytics " +
                        "WHERE created_at BETWEEN :start AND :end " +
                        "GROUP BY date_trunc('minute', created_at) " +
                        "ORDER BY minute DESC", nativeQuery = true)
        List<Object[]> getRequestsPerMinuteBetween(@Param("start") Instant start, @Param("end") Instant end);

        // ========================================
        // Enterprise Analytics - Apdex Score
        // ========================================

        /**
         * Count satisfied requests (response time <= threshold).
         * Default satisfied threshold: 500ms
         */
        @Query("SELECT COUNT(r) FROM RequestAnalytics r " +
                        "WHERE r.createdAt BETWEEN :start AND :end " +
                        "AND r.processingTimeMs IS NOT NULL " +
                        "AND r.processingTimeMs <= :satisfiedThreshold")
        Long countSatisfiedRequests(@Param("start") Instant start, @Param("end") Instant end,
                        @Param("satisfiedThreshold") Long satisfiedThreshold);

        /**
         * Count tolerating requests (response time between satisfied and frustrated
         * thresholds).
         * Default tolerating: 500ms < response <= 2000ms
         */
        @Query("SELECT COUNT(r) FROM RequestAnalytics r " +
                        "WHERE r.createdAt BETWEEN :start AND :end " +
                        "AND r.processingTimeMs IS NOT NULL " +
                        "AND r.processingTimeMs > :satisfiedThreshold " +
                        "AND r.processingTimeMs <= :frustratedThreshold")
        Long countToleratingRequests(@Param("start") Instant start, @Param("end") Instant end,
                        @Param("satisfiedThreshold") Long satisfiedThreshold,
                        @Param("frustratedThreshold") Long frustratedThreshold);

        /**
         * Count frustrated requests (response time > frustrated threshold).
         * Default frustrated threshold: 2000ms
         */
        @Query("SELECT COUNT(r) FROM RequestAnalytics r " +
                        "WHERE r.createdAt BETWEEN :start AND :end " +
                        "AND r.processingTimeMs IS NOT NULL " +
                        "AND r.processingTimeMs > :frustratedThreshold")
        Long countFrustratedRequests(@Param("start") Instant start, @Param("end") Instant end,
                        @Param("frustratedThreshold") Long frustratedThreshold);

        /**
         * Count total requests with valid response time for Apdex calculation.
         */
        @Query("SELECT COUNT(r) FROM RequestAnalytics r " +
                        "WHERE r.createdAt BETWEEN :start AND :end " +
                        "AND r.processingTimeMs IS NOT NULL")
        Long countRequestsWithResponseTime(@Param("start") Instant start, @Param("end") Instant end);

        // ========================================
        // Enterprise Analytics - SLA Metrics
        // ========================================

        /**
         * Count successful requests (2xx status codes).
         */
        @Query("SELECT COUNT(r) FROM RequestAnalytics r " +
                        "WHERE r.createdAt BETWEEN :start AND :end " +
                        "AND r.responseStatus >= 200 AND r.responseStatus < 300")
        Long countSuccessfulRequests(@Param("start") Instant start, @Param("end") Instant end);

        /**
         * Count failed requests (4xx and 5xx status codes).
         */
        @Query("SELECT COUNT(r) FROM RequestAnalytics r " +
                        "WHERE r.createdAt BETWEEN :start AND :end " +
                        "AND r.responseStatus >= 400")
        Long countFailedRequests(@Param("start") Instant start, @Param("end") Instant end);

        /**
         * Get uptime periods - find gaps in request data (potential downtime).
         * Returns hourly request counts to identify zero-request periods.
         * PostgreSQL compatible.
         */
        @Query(value = "SELECT date_trunc('hour', created_at) as hour, COUNT(*) " +
                        "FROM request_analytics " +
                        "WHERE created_at BETWEEN :start AND :end " +
                        "GROUP BY date_trunc('hour', created_at) " +
                        "ORDER BY hour", nativeQuery = true)
        List<Object[]> getHourlyRequestCountsForUptime(@Param("start") Instant start, @Param("end") Instant end);

        // ========================================
        // Enterprise Analytics - Latency Trends
        // ========================================

        /**
         * Get P50, P95, P99 per hour for latency trend analysis.
         * PostgreSQL compatible.
         */
        @Query(value = "SELECT date_trunc('hour', created_at) as hour, " +
                        "PERCENTILE_CONT(0.50) WITHIN GROUP (ORDER BY processing_time_ms) as p50, " +
                        "PERCENTILE_CONT(0.95) WITHIN GROUP (ORDER BY processing_time_ms) as p95, " +
                        "PERCENTILE_CONT(0.99) WITHIN GROUP (ORDER BY processing_time_ms) as p99, " +
                        "COUNT(*) as request_count " +
                        "FROM request_analytics " +
                        "WHERE created_at BETWEEN :start AND :end AND processing_time_ms IS NOT NULL " +
                        "GROUP BY date_trunc('hour', created_at)", nativeQuery = true)
        List<Object[]> getHourlyLatencyTrendBetween(@Param("start") Instant start, @Param("end") Instant end);

        /**
         * Get average response time per hour (simpler alternative).
         * PostgreSQL compatible native query.
         */
        @Query(value = "SELECT date_trunc('hour', created_at) as hour, " +
                        "AVG(CAST(processing_time_ms AS NUMERIC)) as avg_latency, " +
                        "MIN(processing_time_ms) as min_latency, " +
                        "MAX(processing_time_ms) as max_latency, " +
                        "COUNT(*) as request_count " +
                        "FROM request_analytics " +
                        "WHERE created_at BETWEEN :start AND :end AND processing_time_ms IS NOT NULL " +
                        "GROUP BY date_trunc('hour', created_at) " +
                        "ORDER BY hour", nativeQuery = true)
        List<Object[]> getHourlyAvgLatencyBetween(@Param("start") Instant start, @Param("end") Instant end);

        // ========================================
        // Enterprise Analytics - Error Rate Trends
        // ========================================

        /**
         * Get error rate per hour for trend analysis.
         * PostgreSQL compatible.
         */
        @Query(value = "SELECT date_trunc('hour', created_at) as hour, " +
                        "COUNT(*) as total_requests, " +
                        "SUM(CASE WHEN response_status >= 400 THEN 1 ELSE 0 END) as error_count, " +
                        "CAST(SUM(CASE WHEN response_status >= 400 THEN 1 ELSE 0 END) AS NUMERIC) * 100.0 / COUNT(*) as error_rate "
                        +
                        "FROM request_analytics " +
                        "WHERE created_at BETWEEN :start AND :end " +
                        "GROUP BY date_trunc('hour', created_at) " +
                        "ORDER BY hour", nativeQuery = true)
        List<Object[]> getHourlyErrorRateTrendBetween(@Param("start") Instant start, @Param("end") Instant end);

        // ========================================
        // Enterprise Analytics - Response Size / Bandwidth
        // ========================================

        /**
         * Get average and total response size.
         * Uses responseContentLength field from entity.
         */
        @Query("SELECT AVG(r.responseContentLength), SUM(r.responseContentLength), MIN(r.responseContentLength), MAX(r.responseContentLength), COUNT(r) "
                        +
                        "FROM RequestAnalytics r " +
                        "WHERE r.createdAt BETWEEN :start AND :end AND r.responseContentLength IS NOT NULL")
        Object[] getResponseSizeStatsBetween(@Param("start") Instant start, @Param("end") Instant end);

        /**
         * Get bandwidth usage per hour.
         * PostgreSQL compatible.
         */
        @Query(value = "SELECT date_trunc('hour', created_at) as hour, " +
                        "SUM(COALESCE(response_content_length, 0)) as total_bytes, " +
                        "AVG(CAST(COALESCE(response_content_length, 0) AS NUMERIC)) as avg_bytes, " +
                        "COUNT(*) as request_count " +
                        "FROM request_analytics " +
                        "WHERE created_at BETWEEN :start AND :end " +
                        "GROUP BY date_trunc('hour', created_at) " +
                        "ORDER BY hour", nativeQuery = true)
        List<Object[]> getHourlyBandwidthBetween(@Param("start") Instant start, @Param("end") Instant end);

        // ========================================
        // Enterprise Analytics - Active Users
        // ========================================

        /**
         * Count unique users in the last N minutes.
         */
        @Query("SELECT COUNT(DISTINCT r.userId) FROM RequestAnalytics r " +
                        "WHERE r.createdAt >= :since AND r.userId IS NOT NULL")
        Long countActiveUsersSince(@Param("since") Instant since);

        /**
         * Count unique sessions in the last N minutes.
         */
        @Query("SELECT COUNT(DISTINCT r.sessionId) FROM RequestAnalytics r " +
                        "WHERE r.createdAt >= :since AND r.sessionId IS NOT NULL")
        Long countActiveSessionsSince(@Param("since") Instant since);

        /**
         * Get active users breakdown by time intervals.
         */
        @Query("SELECT r.userId, COUNT(r), MAX(r.createdAt) " +
                        "FROM RequestAnalytics r " +
                        "WHERE r.createdAt >= :since AND r.userId IS NOT NULL " +
                        "GROUP BY r.userId " +
                        "ORDER BY MAX(r.createdAt) DESC")
        List<Object[]> getActiveUsersDetailsSince(@Param("since") Instant since);

        // ========================================
        // Enterprise Analytics - Period Comparison
        // ========================================

        /**
         * Get summary stats for period comparison.
         * Returns: totalRequests, uniqueUsers, uniqueSessions, avgResponseTime,
         * errorCount, successCount
         */
        @Query("SELECT COUNT(r), " +
                        "COUNT(DISTINCT r.userId), " +
                        "COUNT(DISTINCT r.sessionId), " +
                        "AVG(r.processingTimeMs), " +
                        "SUM(CASE WHEN r.responseStatus >= 400 THEN 1 ELSE 0 END), " +
                        "SUM(CASE WHEN r.responseStatus >= 200 AND r.responseStatus < 300 THEN 1 ELSE 0 END) " +
                        "FROM RequestAnalytics r " +
                        "WHERE r.createdAt BETWEEN :start AND :end")
        Object[] getPeriodSummaryStats(@Param("start") Instant start, @Param("end") Instant end);
}
