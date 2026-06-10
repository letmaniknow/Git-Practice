package com.mmva.newsapp.infrastructure.requestanalytics.service;

import com.mmva.newsapp.infrastructure.requestanalytics.dto.*;
import com.mmva.newsapp.infrastructure.requestanalytics.model.RequestAnalytics;
import com.mmva.newsapp.infrastructure.requestanalytics.repository.RequestAnalyticsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of RequestAnalyticsService for logging and analyzing HTTP
 * request analytics.
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
@Service
@RequiredArgsConstructor
public class RequestAnalyticsServiceImpl implements RequestAnalyticsService {

        private final RequestAnalyticsRepository requestAnalyticsRepository;

        private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
                        .getLogger(RequestAnalyticsServiceImpl.class);

        // ========================================
        // Type Conversion Helpers (DB Compatibility)
        // ========================================

        /**
         * Safely converts a database timestamp result to Instant.
         * Handles SQL Server datetime, PostgreSQL timestamp, java.sql.Timestamp, etc.
         */
        private Instant toInstant(Object value) {
                if (value == null)
                        return null;
                if (value instanceof Instant)
                        return (Instant) value;
                if (value instanceof Timestamp)
                        return ((Timestamp) value).toInstant();
                if (value instanceof java.sql.Date)
                        return ((java.sql.Date) value).toLocalDate().atStartOfDay(ZoneOffset.UTC).toInstant();
                if (value instanceof LocalDateTime)
                        return ((LocalDateTime) value).toInstant(ZoneOffset.UTC);
                if (value instanceof LocalDate)
                        return ((LocalDate) value).atStartOfDay(ZoneOffset.UTC).toInstant();
                if (value instanceof java.util.Date)
                        return ((java.util.Date) value).toInstant();
                log.warn("Unknown date type for conversion: {}", value.getClass().getName());
                return null;
        }

        /**
         * Safely converts a database numeric result to Number.
         * Handles various numeric types including BigInteger, BigDecimal, Long,
         * Integer.
         */
        private Number toNumber(Object value) {
                if (value == null)
                        return 0;
                if (value instanceof Number)
                        return (Number) value;
                log.warn("Unknown number type for conversion: {}", value.getClass().getName());
                return 0;
        }

        /**
         * Unwraps aggregate query results that may be nested.
         * Some JPA implementations wrap Object[] results in an extra layer.
         */
        private Object[] unwrapStats(Object[] stats) {
                if (stats == null || stats.length == 0) {
                        return stats;
                }
                // Check if first element is itself an Object[] (nested result)
                if (stats.length == 1 && stats[0] instanceof Object[]) {
                        return (Object[]) stats[0];
                }
                return stats;
        }

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
        @Override
        @Async
        @Transactional
        public void logRequestAsync(RequestAnalyticsInfoDto info, UUID userId, String username,
                        Integer responseStatus, Long processingTimeMs) {
                try {
                        logRequest(info, userId, username, responseStatus, processingTimeMs, null, null);
                } catch (Exception e) {
                        log.error("Failed to log request analytics: {}", e.getMessage(), e);
                }
        }

        /**
         * Asynchronously logs request analytics with error information.
         */
        @Override
        @Async
        @Transactional
        public void logRequestWithErrorAsync(RequestAnalyticsInfoDto info, UUID userId, String username,
                        Integer responseStatus, Long processingTimeMs,
                        String errorMessage, String exceptionClass) {
                try {
                        logRequest(info, userId, username, responseStatus, processingTimeMs, errorMessage,
                                        exceptionClass);
                } catch (Exception e) {
                        log.error("Failed to log request analytics with error: {}", e.getMessage(), e);
                }
        }

        /**
         * Synchronously logs request analytics (blocking).
         */
        @Override
        @Transactional
        public RequestAnalytics logRequest(RequestAnalyticsInfoDto info, UUID userId, String username,
                        Integer responseStatus, Long processingTimeMs,
                        String errorMessage, String exceptionClass) {

                RequestAnalytics analytics = RequestAnalytics.builder()
                                // Identity
                                .requestId(info.xRequestId())
                                .correlationId(info.xCorrelationId())
                                .userId(userId)
                                .username(username)
                                // Connection
                                .clientIpAddress(info.clientIpAddress())
                                .remoteAddress(info.remoteAddress())
                                .remoteHost(info.remoteHost())
                                .remotePort(info.remotePort())
                                .serverPort(info.serverPort())
                                .protocol(info.protocol())
                                .isSecure(info.isSecure())
                                // Request
                                .method(info.method())
                                .requestUri(info.requestUri())
                                .requestUrl(info.requestUrl())
                                .queryString(info.queryString())
                                .contentType(info.contentType())
                                .contentLength(info.contentLength())
                                // Client
                                .userAgent(truncateIfNeeded(info.userAgent(), 1024))
                                .acceptHeader(truncateIfNeeded(info.accept(), 512))
                                .acceptLanguage(truncateIfNeeded(info.acceptLanguage(), 255))
                                .acceptEncoding(truncateIfNeeded(info.acceptEncoding(), 255))
                                .host(info.host())
                                .referer(truncateIfNeeded(info.referer(), 2048))
                                .origin(info.origin())
                                // Proxy
                                .xForwardedFor(truncateIfNeeded(info.xForwardedFor(), 512))
                                .xForwardedProto(info.xForwardedProto())
                                .xForwardedHost(info.xForwardedHost())
                                // Security
                                .authType(info.authType())
                                .secFetchSite(info.secFetchSite())
                                .secFetchMode(info.secFetchMode())
                                .cookieCount(info.cookieCount())
                                // Client Hints
                                .secChUa(truncateIfNeeded(info.secChUa(), 512))
                                .secChUaMobile(info.secChUaMobile())
                                .secChUaPlatform(info.secChUaPlatform())
                                .secChUaPlatformVersion(info.secChUaPlatformVersion())
                                .secChUaArch(info.secChUaArch())
                                .deviceMemory(info.deviceMemory())
                                .ect(info.ect())
                                .saveData(info.saveData())
                                // Derived
                                .browserName(info.getBrowserName())
                                .osName(info.getOsName())
                                .deviceType(info.isMobile() ? "Mobile" : "Desktop")
                                .isBot(info.isBot())
                                // Response
                                .responseStatus(responseStatus)
                                .processingTimeMs(processingTimeMs)
                                .hasError(errorMessage != null || (responseStatus != null && responseStatus >= 400))
                                .errorMessage(truncateIfNeeded(errorMessage, 1024))
                                .exceptionClass(exceptionClass)
                                // Session
                                .sessionId(info.sessionId())
                                .isNewSession(info.isNewSession())
                                .dispatcherType(info.dispatcherType())
                                // Headers
                                .allHeaders(info.allHeaders())
                                .headerCount(info.headerCount())
                                .parameterCount(info.parameterCount())
                                .build();

                return requestAnalyticsRepository.save(analytics);
        }

        // ========================================
        // Statistics Methods
        // ========================================

        /**
         * Gets traffic statistics for a time period.
         */
        @Override
        @Transactional(readOnly = true)
        public RequestAnalyticsService.TrafficStatistics getTrafficStatistics(Instant start, Instant end) {
                return new RequestAnalyticsService.TrafficStatistics(
                                requestAnalyticsRepository.countByCreatedAtBetween(start, end),
                                requestAnalyticsRepository.countByEndpointBetween(start, end),
                                requestAnalyticsRepository.countByDeviceTypeBetween(start, end),
                                requestAnalyticsRepository.countByBrowserBetween(start, end),
                                requestAnalyticsRepository.countByOsBetween(start, end),
                                requestAnalyticsRepository.countByResponseStatusBetween(start, end),
                                requestAnalyticsRepository.countByCountryBetween(start, end),
                                requestAnalyticsRepository.countByCityBetween(start, end),
                                requestAnalyticsRepository.countByIpAddressBetween(start, end));
        }

        /**
         * Gets endpoint performance statistics.
         */
        @Override
        @Transactional(readOnly = true)
        public List<RequestAnalyticsService.EndpointPerformance> getEndpointPerformance(Instant start, Instant end) {
                return requestAnalyticsRepository.getAverageResponseTimeByEndpoint(start, end).stream()
                                .map(row -> new RequestAnalyticsService.EndpointPerformance(
                                                (String) row[0],
                                                (String) row[1],
                                                (Double) row[2],
                                                (Long) row[3],
                                                (Long) row[4],
                                                (Long) row[5]))
                                .toList();
        }

        /**
         * Gets hourly traffic pattern.
         */
        @Override
        @Transactional(readOnly = true)
        public List<RequestAnalyticsService.HourlyTraffic> getHourlyTraffic(Instant start, Instant end) {
                return requestAnalyticsRepository.countByHourBetween(start, end).stream()
                                .map(row -> new RequestAnalyticsService.HourlyTraffic(
                                                toInstant(row[0]),
                                                toNumber(row[1]).longValue()))
                                .toList();
        }

        /**
         * Gets daily traffic pattern.
         */
        @Override
        @Transactional(readOnly = true)
        public List<RequestAnalyticsService.DailyTraffic> getDailyTraffic(Instant start, Instant end) {
                return requestAnalyticsRepository.countByDayBetween(start, end).stream()
                                .map(row -> new RequestAnalyticsService.DailyTraffic(
                                                toInstant(row[0]),
                                                toNumber(row[1]).longValue()))
                                .toList();
        }

        /**
         * Finds potentially suspicious IPs.
         */
        @Override
        @Transactional(readOnly = true)
        public List<RequestAnalyticsService.SuspiciousIp> findSuspiciousIps(Instant start, Instant end,
                        long volumeThreshold,
                        long errorThreshold) {
                List<Object[]> highVolume = requestAnalyticsRepository.findHighVolumeIps(start, end, volumeThreshold);
                List<Object[]> highError = requestAnalyticsRepository.findHighErrorRateIps(start, end, errorThreshold);

                // Combine high volume and high error rate IPs
                var volumeIps = highVolume.stream()
                                .map(row -> new RequestAnalyticsService.SuspiciousIp(
                                                (String) row[0],
                                                ((Number) row[1]).longValue(),
                                                0L,
                                                "HIGH_VOLUME"))
                                .toList();

                var errorIps = highError.stream()
                                .map(row -> new RequestAnalyticsService.SuspiciousIp(
                                                (String) row[0],
                                                0L,
                                                ((Number) row[1]).longValue(),
                                                "HIGH_ERROR_RATE"))
                                .toList();

                // Return combined list (could add deduplication logic if needed)
                var combined = new java.util.ArrayList<>(volumeIps);
                combined.addAll(errorIps);
                return combined;
        }

        // ========================================
        // Cleanup Methods
        // ========================================

        /**
         * Deletes analytics older than the specified retention period.
         */
        @Override
        @Transactional
        public int cleanupOldAnalytics(Duration retentionPeriod) {
                Instant cutoff = Instant.now().minus(retentionPeriod);
                int deleted = requestAnalyticsRepository.deleteOlderThan(cutoff);
                log.info("Deleted {} analytics records older than {}", deleted, cutoff);
                return deleted;
        }

        // ========================================
        // Helper Methods
        // ========================================

        private String truncateIfNeeded(String value, int maxLength) {
                if (value == null)
                        return null;
                return value.length() > maxLength ? value.substring(0, maxLength) : value;
        }

        // ========================================
        // User Analytics
        // ========================================

        /**
         * Gets user analytics including registration, activity, and engagement metrics.
         * 
         * @param start Start time
         * @param end   End time
         * @return User analytics data
         */
        @Override
        public UserAnalytics getUserAnalytics(Instant start, Instant end) {
                log.debug("Generating user analytics for period: {} to {}", start, end);

                // Calculate user metrics
                Long uniqueUsers = requestAnalyticsRepository.countUniqueUsersBetween(start, end);

                // Get authenticated vs anonymous counts
                List<Object[]> authStats = requestAnalyticsRepository.countAuthenticatedVsAnonymousBetween(start, end);
                long authenticatedRequests = 0;
                long anonymousRequests = 0;
                for (Object[] row : authStats) {
                        String type = (String) row[0];
                        long count = ((Number) row[1]).longValue();
                        if ("authenticated".equals(type)) {
                                authenticatedRequests = count;
                        } else if ("anonymous".equals(type)) {
                                anonymousRequests = count;
                        }
                }

                double authRate = authenticatedRequests + anonymousRequests > 0
                                ? (double) authenticatedRequests / (authenticatedRequests + anonymousRequests) * 100.0
                                : 0.0;

                // Get top users
                PageRequest top10 = PageRequest.of(0, 10);
                List<Object[]> topUsersData = requestAnalyticsRepository.getTopUsersBetween(start, end, top10)
                                .getContent();
                List<UserAnalytics.TopUser> topUsers = topUsersData.stream()
                                .map(row -> new UserAnalytics.TopUser(
                                                row[0] != null ? row[0].toString() : "unknown",
                                                (String) row[1],
                                                ((Number) row[2]).longValue(),
                                                toInstant(row[3])))
                                .collect(Collectors.toList());

                return new UserAnalytics(
                                uniqueUsers != null ? uniqueUsers : 0L,
                                authenticatedRequests,
                                anonymousRequests,
                                authRate,
                                topUsers);
        }

        // ========================================
        // Comprehensive Analytics (Industry Best Practices)
        // ========================================

        /**
         * Get comprehensive analytics covering all dimensions.
         * 
         * @param start Start time
         * @param end   End time
         * @return Complete analytics response with all metrics
         */
        @Override
        public ComprehensiveAnalyticsResponseDto getComprehensiveAnalytics(Instant start, Instant end) {
                log.debug("Generating comprehensive analytics for period: {} to {}", start, end);

                // Calculate limits for top N queries
                PageRequest top10 = PageRequest.of(0, 10);
                PageRequest top20 = PageRequest.of(0, 20);

                // 1. Overview Metrics
                long totalRequests = requestAnalyticsRepository.countByCreatedAtBetween(start, end);
                Long uniqueUsers = requestAnalyticsRepository.countUniqueUsersBetween(start, end);
                Long uniqueSessions = requestAnalyticsRepository.countUniqueSessionsBetween(start, end);

                List<RequestAnalyticsService.EndpointPerformance> allEndpoints = getEndpointPerformance(start, end);
                Double avgResponseTime = allEndpoints.isEmpty() ? 0.0
                                : allEndpoints.stream().mapToDouble(
                                                RequestAnalyticsService.EndpointPerformance::avgResponseTimeMs)
                                                .average()
                                                .orElse(0.0);

                Double errorRate = requestAnalyticsRepository.getErrorRateBetween(start, end);
                if (errorRate == null)
                        errorRate = 0.0;

                var overview = new OverviewMetrics(
                                totalRequests,
                                uniqueUsers != null ? uniqueUsers : 0L,
                                uniqueSessions != null ? uniqueSessions : 0L,
                                avgResponseTime,
                                errorRate,
                                start + " to " + end);

                // 2. User Analytics
                List<Object[]> authVsAnon = requestAnalyticsRepository.countAuthenticatedVsAnonymousBetween(start, end);
                long authenticatedRequests = 0L;
                long anonymousRequests = 0L;
                for (Object[] row : authVsAnon) {
                        String type = (String) row[0];
                        long count = ((Number) row[1]).longValue();
                        if ("authenticated".equals(type)) {
                                authenticatedRequests = count;
                        } else {
                                anonymousRequests = count;
                        }
                }
                Double authRate = totalRequests > 0 ? (authenticatedRequests * 100.0 / totalRequests) : 0.0;

                List<Object[]> topUsersData = requestAnalyticsRepository.getTopUsersBetween(start, end, top10)
                                .getContent();
                List<UserAnalytics.TopUser> topUsers = topUsersData.stream()
                                .map(row -> new UserAnalytics.TopUser(
                                                row[0] != null ? row[0].toString() : "unknown",
                                                (String) row[1],
                                                ((Number) row[2]).longValue(),
                                                toInstant(row[3])))
                                .collect(Collectors.toList());

                var users = new UserAnalytics(
                                uniqueUsers != null ? uniqueUsers : 0L,
                                authenticatedRequests,
                                anonymousRequests,
                                authRate,
                                topUsers);

                // 3. Performance Metrics
                List<Object[]> percentilesData = requestAnalyticsRepository.getResponseTimePercentilesBetween(start,
                                end);
                Object[] percentiles = percentilesData != null && !percentilesData.isEmpty() ? percentilesData.get(0)
                                : null;
                Double p50 = percentiles != null && percentiles.length > 0 && percentiles[0] != null
                                ? toNumber(percentiles[0]).doubleValue()
                                : 0.0;
                Double p95 = percentiles != null && percentiles.length > 1 && percentiles[1] != null
                                ? toNumber(percentiles[1]).doubleValue()
                                : 0.0;
                Double p99 = percentiles != null && percentiles.length > 2 && percentiles[2] != null
                                ? toNumber(percentiles[2]).doubleValue()
                                : 0.0;
                Double minTime = percentiles != null && percentiles.length > 3 && percentiles[3] != null
                                ? toNumber(percentiles[3]).doubleValue()
                                : 0.0;
                Double maxTime = percentiles != null && percentiles.length > 4 && percentiles[4] != null
                                ? toNumber(percentiles[4]).doubleValue()
                                : 0.0;

                List<Object[]> slowestData = requestAnalyticsRepository.getSlowestEndpointsBetween(start, end, top10)
                                .getContent();
                List<PerformanceMetrics.SlowEndpoint> slowest = slowestData.stream()
                                .map(row -> new PerformanceMetrics.SlowEndpoint(
                                                (String) row[0],
                                                (String) row[1],
                                                ((Number) row[2]).longValue(),
                                                ((Number) row[3]).doubleValue(),
                                                ((Number) row[4]).doubleValue()))
                                .collect(Collectors.toList());

                var performance = new PerformanceMetrics(
                                p50, p95, p99, avgResponseTime, minTime.longValue(), maxTime.longValue(), slowest);

                // 4. Security Insights
                List<Object[]> failedAuthData = requestAnalyticsRepository.getFailedAuthAttemptsBetween(start, end,
                                top10).getContent();
                long totalFailedAuth = failedAuthData.stream().mapToLong(row -> ((Number) row[2]).longValue()).sum();
                List<SecurityInsights.FailedAuthIp> failedAuthIps = failedAuthData
                                .stream()
                                .map(row -> new SecurityInsights.FailedAuthIp(
                                                (String) row[0],
                                                (String) row[1],
                                                ((Number) row[2]).longValue()))
                                .collect(Collectors.toList());

                List<Object[]> suspiciousData = requestAnalyticsRepository.getSuspiciousIpsBetween(start, end, 100,
                                top10).getContent();
                List<SecurityInsights.SuspiciousIp> suspiciousIps = suspiciousData
                                .stream()
                                .map(row -> new SecurityInsights.SuspiciousIp(
                                                (String) row[0],
                                                (String) row[1],
                                                ((Number) row[2]).longValue()))
                                .collect(Collectors.toList());

                List<Object[]> botData = requestAnalyticsRepository.getBotVsHumanTrafficBetween(start, end);
                long botRequests = 0L;
                long humanRequests = 0L;
                for (Object[] row : botData) {
                        String type = (String) row[0];
                        long count = ((Number) row[1]).longValue();
                        if ("bot".equals(type)) {
                                botRequests = count;
                        } else {
                                humanRequests = count;
                        }
                }
                Double botPercentage = totalRequests > 0 ? (botRequests * 100.0 / totalRequests) : 0.0;
                var botTraffic = new SecurityInsights.BotTraffic(
                                botRequests, humanRequests, botPercentage);

                var security = new SecurityInsights(
                                totalFailedAuth, failedAuthIps, suspiciousIps, botTraffic);

                // 5. Device Intelligence
                List<Object[]> deviceData = requestAnalyticsRepository.getDeviceDistributionBetween(start, end);
                Map<String, DeviceIntelligence.DeviceStats> deviceDist = deviceData
                                .stream()
                                .collect(Collectors.toMap(
                                                row -> (String) row[0],
                                                row -> new DeviceIntelligence.DeviceStats(
                                                                (String) row[0],
                                                                ((Number) row[1]).longValue(),
                                                                ((Number) row[2]).doubleValue())));

                List<Object[]> browserData = requestAnalyticsRepository.getBrowserMarketShareBetween(start, end, top10)
                                .getContent();
                List<DeviceIntelligence.BrowserShare> browserShare = browserData
                                .stream()
                                .map(row -> new DeviceIntelligence.BrowserShare(
                                                (String) row[0],
                                                (String) row[1],
                                                ((Number) row[2]).longValue(),
                                                ((Number) row[3]).doubleValue()))
                                .collect(Collectors.toList());

                List<Object[]> osData = requestAnalyticsRepository.getOsMarketShareBetween(start, end, top10)
                                .getContent();
                List<DeviceIntelligence.OsShare> osShare = osData.stream()
                                .map(row -> new DeviceIntelligence.OsShare(
                                                (String) row[0],
                                                (String) row[1],
                                                ((Number) row[2]).longValue(),
                                                ((Number) row[3]).doubleValue()))
                                .collect(Collectors.toList());

                var devices = new DeviceIntelligence(
                                new ArrayList<>(deviceDist.values()), browserShare, osShare);

                // 6. Geographic Analytics
                List<Object[]> countryData = requestAnalyticsRepository.countByCountryBetween(start, end);
                List<GeographicAnalytics.CountryStats> countries = countryData
                                .stream()
                                .limit(20)
                                .map(row -> new GeographicAnalytics.CountryStats(
                                                (String) row[0],
                                                ((Number) row[1]).longValue()))
                                .collect(Collectors.toList());

                List<Object[]> cityData = requestAnalyticsRepository.countByCityBetween(start, end);
                List<GeographicAnalytics.CityStats> cities = cityData.stream()
                                .limit(20)
                                .map(row -> new GeographicAnalytics.CityStats(
                                                (String) row[0],
                                                (String) row[1],
                                                ((Number) row[2]).longValue()))
                                .collect(Collectors.toList());

                List<Object[]> ipData = requestAnalyticsRepository.countByIpAddressBetween(start, end);
                List<GeographicAnalytics.IpStats> ips = ipData.stream()
                                .limit(20)
                                .map(row -> new GeographicAnalytics.IpStats(
                                                (String) row[0],
                                                (String) row[1],
                                                (String) row[2],
                                                ((Number) row[3]).longValue()))
                                .collect(Collectors.toList());

                List<Object[]> timezoneData = requestAnalyticsRepository.getRequestsByTimezoneBetween(start, end,
                                top10).getContent();
                List<GeographicAnalytics.TimezoneStats> timezones = timezoneData
                                .stream()
                                .map(row -> new GeographicAnalytics.TimezoneStats(
                                                (String) row[0],
                                                ((Number) row[1]).longValue()))
                                .collect(Collectors.toList());

                List<Object[]> ispData = requestAnalyticsRepository.getTopIspsBetween(start, end, top10).getContent();
                List<GeographicAnalytics.IspStats> isps = ispData.stream()
                                .map(row -> new GeographicAnalytics.IspStats(
                                                (String) row[0],
                                                (String) row[1],
                                                ((Number) row[2]).longValue()))
                                .collect(Collectors.toList());

                var geographic = new GeographicAnalytics(
                                countries, cities, ips, timezones, isps);

                // 7. Time Distribution
                List<Object[]> hourlyData = requestAnalyticsRepository.getHourlyDistributionBetween(start, end);
                List<TimeDistribution.HourlyStats> hourly = hourlyData.stream()
                                .map(row -> new TimeDistribution.HourlyStats(
                                                ((Number) row[0]).intValue(),
                                                ((Number) row[1]).longValue()))
                                .collect(Collectors.toList());

                List<Object[]> peakData = requestAnalyticsRepository.getPeakHoursByDayOfWeekBetween(start, end);
                String[] dayNames = { "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday" };
                List<TimeDistribution.PeakHour> peaks = peakData.stream()
                                .map(row -> {
                                        int dow = ((Number) row[0]).intValue();
                                        return new TimeDistribution.PeakHour(
                                                        dow,
                                                        ((Number) row[1]).intValue(),
                                                        ((Number) row[2]).longValue(),
                                                        dayNames[dow]);
                                })
                                .collect(Collectors.toList());

                var timeDistribution = new TimeDistribution(hourly, peaks);

                // 8. Error Analytics
                List<Object[]> errorEndpointData = requestAnalyticsRepository.getErrorBreakdownByEndpointBetween(start,
                                end, top20).getContent();
                List<ErrorAnalytics.ErrorByEndpoint> errorsByEndpoint = errorEndpointData
                                .stream()
                                .map(row -> new ErrorAnalytics.ErrorByEndpoint(
                                                (String) row[0],
                                                (String) row[1],
                                                ((Number) row[2]).intValue(),
                                                ((Number) row[3]).longValue()))
                                .collect(Collectors.toList());

                long totalErrors = errorsByEndpoint.stream().mapToLong(e -> e.count()).sum();

                List<Object[]> statusData = requestAnalyticsRepository.getErrorStatusDistributionBetween(start, end);
                List<ErrorAnalytics.ErrorStatusDistribution> statusDist = statusData
                                .stream()
                                .map(row -> new ErrorAnalytics.ErrorStatusDistribution(
                                                ((Number) row[0]).intValue(),
                                                ((Number) row[1]).longValue(),
                                                ((Number) row[2]).doubleValue()))
                                .collect(Collectors.toList());

                List<Object[]> exceptionData = requestAnalyticsRepository.getExceptionPatternsBetween(start, end,
                                top10).getContent();
                List<ErrorAnalytics.ExceptionPattern> exceptions = exceptionData
                                .stream()
                                .map(row -> new ErrorAnalytics.ExceptionPattern(
                                                (String) row[0],
                                                ((Number) row[1]).longValue()))
                                .collect(Collectors.toList());

                var errors = new ErrorAnalytics(
                                totalErrors, errorRate, errorsByEndpoint, statusDist, exceptions);

                // 9. Session Analytics
                List<Object[]> sessionData = requestAnalyticsRepository.getNewVsReturningSessionsBetween(start, end);
                long newSessions = 0L;
                long returningSessions = 0L;
                for (Object[] row : sessionData) {
                        String type = (String) row[0];
                        long count = ((Number) row[1]).longValue();
                        if ("new".equals(type)) {
                                newSessions = count;
                        } else {
                                returningSessions = count;
                        }
                }
                long totalSessions = uniqueSessions != null ? uniqueSessions : 0L;
                Double newSessionRate = totalSessions > 0 ? (newSessions * 100.0 / totalSessions) : 0.0;

                var sessions = new SessionAnalytics(
                                totalSessions, newSessions, returningSessions, newSessionRate);

                // 10. Content Performance
                List<Object[]> mostAccessedData = requestAnalyticsRepository.countByEndpointBetween(start, end);
                List<ContentPerformance.EndpointStats> mostAccessedEndpoints = mostAccessedData
                                .stream()
                                .limit(20)
                                .map(row -> new ContentPerformance.EndpointStats(
                                                (String) row[0],
                                                (String) row[1],
                                                ((Number) row[2]).longValue(),
                                                0.0))
                                .collect(Collectors.toList());

                List<Object[]> leastAccessedData = requestAnalyticsRepository.getLeastAccessedEndpointsBetween(start,
                                end, top10).getContent();
                List<ContentPerformance.EndpointStats> leastAccessedEndpoints = leastAccessedData
                                .stream()
                                .map(row -> new ContentPerformance.EndpointStats(
                                                (String) row[0],
                                                (String) row[1],
                                                ((Number) row[2]).longValue(),
                                                row[3] != null ? ((Number) row[3]).doubleValue() : 0.0))
                                .collect(Collectors.toList());

                List<Object[]> refererData = requestAnalyticsRepository.getTopReferersBetween(start, end, top10)
                                .getContent();
                List<ContentPerformance.RefererStats> topReferrers = refererData.stream()
                                .map(row -> new ContentPerformance.RefererStats(
                                                (String) row[0],
                                                ((Number) row[1]).longValue()))
                                .collect(Collectors.toList());

                List<Object[]> methodData = requestAnalyticsRepository.getRequestMethodDistributionBetween(start, end);
                List<ContentPerformance.MethodDistribution> methodDistribution = methodData
                                .stream()
                                .map(row -> new ContentPerformance.MethodDistribution(
                                                (String) row[0],
                                                ((Number) row[1]).longValue(),
                                                ((Number) row[2]).doubleValue()))
                                .collect(Collectors.toList());

                var content = new ContentPerformance(
                                mostAccessedEndpoints, leastAccessedEndpoints, topReferrers, methodDistribution);

                // ========================================
                // Enterprise Analytics Features
                // ========================================

                // 11. Throughput Metrics
                var throughput = calculateThroughputMetrics(start, end, totalRequests);

                // 12. Apdex Score
                var apdex = calculateApdexMetrics(start, end);

                // 13. SLA Metrics
                var sla = calculateSlaMetrics(start, end, totalRequests);

                // 14. Trend Analytics
                var trends = calculateTrendAnalytics(start, end);

                // 15. Period Comparison
                var periodComparison = calculatePeriodComparison(start, end);

                // 16. Active Users
                var activeUsersMetrics = calculateActiveUsersMetrics();

                // 17. Bandwidth Metrics
                var bandwidth = calculateBandwidthMetrics(start, end);

                return new ComprehensiveAnalyticsResponseDto(
                                overview, users, performance, security, devices,
                                geographic, timeDistribution, errors, sessions, content,
                                throughput, apdex, sla, trends, periodComparison, activeUsersMetrics, bandwidth);
        }

        // ========================================
        // Enterprise Analytics Helper Methods
        // ========================================

        /**
         * Calculate throughput metrics (requests per second/minute/hour).
         */
        private ThroughputMetrics calculateThroughputMetrics(
                        Instant start, Instant end, long totalRequests) {

                long durationSeconds = java.time.Duration.between(start, end).getSeconds();
                if (durationSeconds <= 0)
                        durationSeconds = 1;

                double requestsPerSecond = (double) totalRequests / durationSeconds;
                double requestsPerMinute = requestsPerSecond * 60;
                double requestsPerHour = requestsPerSecond * 3600;

                // Get per-minute data for peak calculation
                List<Object[]> minutelyData = requestAnalyticsRepository.getRequestsPerMinuteBetween(start, end);

                long peakRequestsPerMinute = 0L;
                Instant peakTime = null;
                List<ThroughputMetrics.MinutelyThroughput> recentThroughput = new ArrayList<>();

                for (Object[] row : minutelyData) {
                        if (row == null || row.length < 2) {
                                continue; // Skip invalid rows
                        }
                        Instant minute = toInstant(row[0]);
                        long count = toNumber(row[1]).longValue();
                        double rps = count / 60.0;

                        if (count > peakRequestsPerMinute) {
                                peakRequestsPerMinute = count;
                                peakTime = minute;
                        }

                        recentThroughput.add(new ThroughputMetrics.MinutelyThroughput(
                                        minute, count, rps));
                }

                // Limit to last 60 entries
                if (recentThroughput.size() > 60) {
                        recentThroughput = recentThroughput.subList(0, 60);
                }

                return new ThroughputMetrics(
                                requestsPerSecond,
                                requestsPerMinute,
                                requestsPerHour,
                                peakRequestsPerMinute,
                                peakTime,
                                recentThroughput);
        }

        /**
         * Calculate Apdex (Application Performance Index) score.
         * Apdex = (Satisfied + Tolerating/2) / Total
         */
        private ApdexMetrics calculateApdexMetrics(Instant start, Instant end) {
                // Industry standard thresholds (configurable)
                long satisfiedThreshold = 500L; // 500ms
                long frustratedThreshold = 2000L; // 2000ms (4x satisfied)

                Long satisfied = requestAnalyticsRepository.countSatisfiedRequests(start, end, satisfiedThreshold);
                Long tolerating = requestAnalyticsRepository.countToleratingRequests(start, end, satisfiedThreshold,
                                frustratedThreshold);
                Long frustrated = requestAnalyticsRepository.countFrustratedRequests(start, end, frustratedThreshold);
                Long total = requestAnalyticsRepository.countRequestsWithResponseTime(start, end);

                satisfied = satisfied != null ? satisfied : 0L;
                tolerating = tolerating != null ? tolerating : 0L;
                frustrated = frustrated != null ? frustrated : 0L;
                total = total != null && total > 0 ? total : 1L;

                // Apdex formula: (Satisfied + Tolerating/2) / Total
                double apdexScore = (satisfied + (tolerating / 2.0)) / total;
                apdexScore = Math.round(apdexScore * 100.0) / 100.0; // Round to 2 decimals

                // Rating interpretation
                String rating;
                String interpretation;
                if (apdexScore >= 0.94) {
                        rating = "Excellent";
                        interpretation = "Users are very satisfied with performance";
                } else if (apdexScore >= 0.85) {
                        rating = "Good";
                        interpretation = "Users are generally satisfied";
                } else if (apdexScore >= 0.70) {
                        rating = "Fair";
                        interpretation = "Some users are experiencing delays";
                } else if (apdexScore >= 0.50) {
                        rating = "Poor";
                        interpretation = "Many users are frustrated with performance";
                } else {
                        rating = "Unacceptable";
                        interpretation = "Performance is causing significant user dissatisfaction";
                }

                return new ApdexMetrics(
                                apdexScore,
                                rating,
                                satisfied,
                                tolerating,
                                frustrated,
                                total,
                                satisfiedThreshold,
                                frustratedThreshold,
                                interpretation);
        }

        /**
         * Calculate SLA metrics (availability, success rate).
         */
        private SlaMetrics calculateSlaMetrics(
                        Instant start, Instant end, long totalRequests) {

                Long successfulRequests = requestAnalyticsRepository.countSuccessfulRequests(start, end);
                Long failedRequests = requestAnalyticsRepository.countFailedRequests(start, end);

                successfulRequests = successfulRequests != null ? successfulRequests : 0L;
                failedRequests = failedRequests != null ? failedRequests : 0L;

                double successRate = totalRequests > 0 ? (successfulRequests * 100.0 / totalRequests) : 100.0;

                // Calculate uptime based on hourly request counts
                List<Object[]> hourlyData = requestAnalyticsRepository.getHourlyRequestCountsForUptime(start, end);

                long totalHoursInPeriod = java.time.Duration.between(start, end).toHours();
                if (totalHoursInPeriod <= 0)
                        totalHoursInPeriod = 1;

                long uptimeHours = hourlyData.size(); // Hours with requests
                long downtimeHours = totalHoursInPeriod - uptimeHours;

                double availabilityPercentage = (uptimeHours * 100.0 / totalHoursInPeriod);

                // Identify downtime periods (simplified - hours with 0 requests)
                List<SlaMetrics.DowntimePeriod> downtimePeriods = new ArrayList<>();
                // Note: Would need more complex logic to identify contiguous downtime periods

                return new SlaMetrics(
                                availabilityPercentage,
                                successRate,
                                successfulRequests,
                                failedRequests,
                                totalRequests,
                                uptimeHours,
                                downtimeHours,
                                totalHoursInPeriod,
                                downtimePeriods);
        }

        /**
         * Calculate trend analytics (latency and error rate over time).
         */
        private TrendAnalytics calculateTrendAnalytics(Instant start, Instant end) {
                // Latency trend
                List<Object[]> errorRateData = requestAnalyticsRepository.getHourlyErrorRateTrendBetween(start, end);

                List<TrendAnalytics.LatencyTrend> latencyTrend = new ArrayList<>();
                // Note: Using error rate query for now, would need separate latency trend query
                // for P50/P95/P99

                List<TrendAnalytics.ErrorRateTrend> errorRateTrend = errorRateData
                                .stream()
                                .filter(row -> row != null && row.length >= 4)
                                .map(row -> new TrendAnalytics.ErrorRateTrend(
                                                toInstant(row[0]),
                                                toNumber(row[1]).longValue(),
                                                toNumber(row[2]).longValue(),
                                                row[3] != null ? toNumber(row[3]).doubleValue() : 0.0))
                                .collect(Collectors.toList());

                // Calculate trend summary
                TrendAnalytics.TrendSummary latencySummary = null;
                TrendAnalytics.TrendSummary errorRateSummary = null;

                if (errorRateTrend.size() >= 2) {
                        var first = errorRateTrend.get(0);
                        var last = errorRateTrend.get(errorRateTrend.size() - 1);

                        double currentErrorRate = last.errorRate() != null ? last.errorRate() : 0;
                        double previousErrorRate = first.errorRate() != null ? first.errorRate() : 0;
                        double change = currentErrorRate - previousErrorRate;
                        double changePercent = previousErrorRate > 0 ? (change / previousErrorRate * 100) : 0;

                        String direction = change > 0 ? "increasing" : change < 0 ? "decreasing" : "stable";
                        String interpretation = direction.equals("increasing")
                                        ? "Error rate is trending up - investigate issues"
                                        : direction.equals("decreasing")
                                                        ? "Error rate is improving"
                                                        : "Error rate is stable";

                        errorRateSummary = new TrendAnalytics.TrendSummary(
                                        direction, changePercent, currentErrorRate, previousErrorRate, interpretation);
                }

                return new TrendAnalytics(
                                latencyTrend, errorRateTrend, latencySummary, errorRateSummary);
        }

        /**
         * Calculate period comparison (current vs previous period).
         */
        private PeriodComparison calculatePeriodComparison(Instant start,
                        Instant end) {
                // Calculate previous period (same duration before start)
                java.time.Duration periodDuration = java.time.Duration.between(start, end);
                Instant previousStart = start.minus(periodDuration);
                Instant previousEnd = start;

                // Get stats for both periods
                Object[] currentStats = unwrapStats(requestAnalyticsRepository.getPeriodSummaryStats(start, end));
                Object[] previousStats = unwrapStats(
                                requestAnalyticsRepository.getPeriodSummaryStats(previousStart, previousEnd));

                // Parse current period stats (with length checks)
                long currentRequests = currentStats != null && currentStats.length > 0 && currentStats[0] != null
                                ? toNumber(currentStats[0]).longValue()
                                : 0;
                long currentUsers = currentStats != null && currentStats.length > 1 && currentStats[1] != null
                                ? toNumber(currentStats[1]).longValue()
                                : 0;
                long currentSessions = currentStats != null && currentStats.length > 2 && currentStats[2] != null
                                ? toNumber(currentStats[2]).longValue()
                                : 0;
                double currentAvgResponse = currentStats != null && currentStats.length > 3 && currentStats[3] != null
                                ? toNumber(currentStats[3]).doubleValue()
                                : 0;
                long currentErrors = currentStats != null && currentStats.length > 4 && currentStats[4] != null
                                ? toNumber(currentStats[4]).longValue()
                                : 0;
                long currentSuccess = currentStats != null && currentStats.length > 5 && currentStats[5] != null
                                ? toNumber(currentStats[5]).longValue()
                                : 0;
                double currentErrorRate = currentRequests > 0 ? (currentErrors * 100.0 / currentRequests) : 0;
                double currentSuccessRate = currentRequests > 0 ? (currentSuccess * 100.0 / currentRequests) : 100;

                // Parse previous period stats (with length checks)
                long prevRequests = previousStats != null && previousStats.length > 0 && previousStats[0] != null
                                ? toNumber(previousStats[0]).longValue()
                                : 0;
                long prevUsers = previousStats != null && previousStats.length > 1 && previousStats[1] != null
                                ? toNumber(previousStats[1]).longValue()
                                : 0;
                long prevSessions = previousStats != null && previousStats.length > 2 && previousStats[2] != null
                                ? toNumber(previousStats[2]).longValue()
                                : 0;
                double prevAvgResponse = previousStats != null && previousStats.length > 3 && previousStats[3] != null
                                ? toNumber(previousStats[3]).doubleValue()
                                : 0;
                long prevErrors = previousStats != null && previousStats.length > 4 && previousStats[4] != null
                                ? toNumber(previousStats[4]).longValue()
                                : 0;
                long prevSuccess = previousStats != null && previousStats.length > 5 && previousStats[5] != null
                                ? toNumber(previousStats[5]).longValue()
                                : 0;
                double prevErrorRate = prevRequests > 0 ? (prevErrors * 100.0 / prevRequests) : 0;
                double prevSuccessRate = prevRequests > 0 ? (prevSuccess * 100.0 / prevRequests) : 100;

                // Determine period label
                long days = periodDuration.toDays();
                String comparisonType = days >= 7 ? "Week over Week" : days >= 1 ? "Day over Day" : "Hour over Hour";
                String currentLabel = "Current Period";
                String previousLabel = "Previous Period";

                var currentPeriod = new PeriodStats(
                                currentLabel, start, end, currentRequests, currentUsers, currentSessions,
                                currentAvgResponse, currentErrors, currentSuccess, currentErrorRate,
                                currentSuccessRate);

                var previousPeriod = new PeriodStats(
                                previousLabel, previousStart, previousEnd, prevRequests, prevUsers, prevSessions,
                                prevAvgResponse, prevErrors, prevSuccess, prevErrorRate, prevSuccessRate);

                // Calculate deltas
                long requestsDelta = currentRequests - prevRequests;
                double requestsChangePercent = prevRequests > 0 ? (requestsDelta * 100.0 / prevRequests) : 0;
                long usersDelta = currentUsers - prevUsers;
                double usersChangePercent = prevUsers > 0 ? (usersDelta * 100.0 / prevUsers) : 0;
                double avgResponseTimeDelta = currentAvgResponse - prevAvgResponse;
                double responseTimeChangePercent = prevAvgResponse > 0
                                ? (avgResponseTimeDelta * 100.0 / prevAvgResponse)
                                : 0;
                double errorRateDelta = currentErrorRate - prevErrorRate;

                // Determine overall trend
                String overallTrend;
                if (requestsChangePercent > 10 && errorRateDelta < 0) {
                        overallTrend = "Improving";
                } else if (requestsChangePercent < -10 || errorRateDelta > 5) {
                        overallTrend = "Declining";
                } else {
                        overallTrend = "Stable";
                }

                // Generate insights
                List<String> insights = new ArrayList<>();
                if (requestsChangePercent > 20) {
                        insights.add("Traffic increased significantly by "
                                        + String.format("%.1f%%", requestsChangePercent));
                } else if (requestsChangePercent < -20) {
                        insights.add("Traffic decreased significantly by "
                                        + String.format("%.1f%%", Math.abs(requestsChangePercent)));
                }
                if (errorRateDelta > 5) {
                        insights.add("Error rate increased by " + String.format("%.1f%%", errorRateDelta)
                                        + " - investigate issues");
                } else if (errorRateDelta < -5) {
                        insights.add("Error rate improved by " + String.format("%.1f%%", Math.abs(errorRateDelta)));
                }
                if (responseTimeChangePercent > 25) {
                        insights.add("Response time degraded by " + String.format("%.1f%%", responseTimeChangePercent));
                } else if (responseTimeChangePercent < -25) {
                        insights.add("Response time improved by "
                                        + String.format("%.1f%%", Math.abs(responseTimeChangePercent)));
                }
                if (insights.isEmpty()) {
                        insights.add("Performance is stable compared to previous period");
                }

                var delta = new PeriodDelta(
                                requestsDelta, requestsChangePercent, usersDelta, usersChangePercent,
                                avgResponseTimeDelta, responseTimeChangePercent, errorRateDelta,
                                overallTrend, insights);

                return new PeriodComparison(
                                currentPeriod, previousPeriod, delta, comparisonType);
        }

        /**
         * Calculate active users metrics.
         */
        private ActiveUsersMetrics calculateActiveUsersMetrics() {
                Instant now = Instant.now();
                Instant fiveMinAgo = now.minus(java.time.Duration.ofMinutes(5));
                Instant fifteenMinAgo = now.minus(java.time.Duration.ofMinutes(15));
                Instant thirtyMinAgo = now.minus(java.time.Duration.ofMinutes(30));
                Instant sixtyMinAgo = now.minus(java.time.Duration.ofMinutes(60));

                Long users5 = requestAnalyticsRepository.countActiveUsersSince(fiveMinAgo);
                Long users15 = requestAnalyticsRepository.countActiveUsersSince(fifteenMinAgo);
                Long users30 = requestAnalyticsRepository.countActiveUsersSince(thirtyMinAgo);
                Long users60 = requestAnalyticsRepository.countActiveUsersSince(sixtyMinAgo);

                Long sessions5 = requestAnalyticsRepository.countActiveSessionsSince(fiveMinAgo);
                Long sessions15 = requestAnalyticsRepository.countActiveSessionsSince(fifteenMinAgo);
                Long sessions30 = requestAnalyticsRepository.countActiveSessionsSince(thirtyMinAgo);
                Long sessions60 = requestAnalyticsRepository.countActiveSessionsSince(sixtyMinAgo);

                // Get recent active users details
                List<Object[]> activeUsersData = requestAnalyticsRepository.getActiveUsersDetailsSince(thirtyMinAgo);
                List<ActiveUsersMetrics.ActiveUser> recentActiveUsers = activeUsersData
                                .stream()
                                .limit(20)
                                .map(row -> new ActiveUsersMetrics.ActiveUser(
                                                row[0] != null ? row[0].toString() : "anonymous",
                                                toNumber(row[1]).longValue(),
                                                toInstant(row[2])))
                                .collect(Collectors.toList());

                return new ActiveUsersMetrics(
                                users5 != null ? users5 : 0,
                                users15 != null ? users15 : 0,
                                users30 != null ? users30 : 0,
                                users60 != null ? users60 : 0,
                                sessions5 != null ? sessions5 : 0,
                                sessions15 != null ? sessions15 : 0,
                                sessions30 != null ? sessions30 : 0,
                                sessions60 != null ? sessions60 : 0,
                                recentActiveUsers);
        }

        /**
         * Calculate bandwidth metrics.
         */
        private BandwidthMetrics calculateBandwidthMetrics(Instant start,
                        Instant end) {
                Object[] stats = unwrapStats(requestAnalyticsRepository.getResponseSizeStatsBetween(start, end));

                double avgBytes = stats != null && stats.length > 0 && stats[0] != null
                                ? toNumber(stats[0]).doubleValue()
                                : 0;
                long totalBytes = stats != null && stats.length > 1 && stats[1] != null ? toNumber(stats[1]).longValue()
                                : 0;
                long minBytes = stats != null && stats.length > 2 && stats[2] != null ? toNumber(stats[2]).longValue()
                                : 0;
                long maxBytes = stats != null && stats.length > 3 && stats[3] != null ? toNumber(stats[3]).longValue()
                                : 0;

                // Get hourly bandwidth
                List<Object[]> hourlyData = requestAnalyticsRepository.getHourlyBandwidthBetween(start, end);
                List<BandwidthMetrics.HourlyBandwidth> hourlyBandwidth = hourlyData
                                .stream()
                                .filter(row -> row != null && row.length >= 4)
                                .map(row -> new BandwidthMetrics.HourlyBandwidth(
                                                toInstant(row[0]),
                                                toNumber(row[1]).longValue(),
                                                toNumber(row[2]).doubleValue(),
                                                toNumber(row[3]).longValue(),
                                                formatBytes(toNumber(row[1]).longValue())))
                                .collect(Collectors.toList());

                return new BandwidthMetrics(
                                avgBytes,
                                totalBytes,
                                minBytes,
                                maxBytes,
                                formatBytes(totalBytes),
                                formatBytes((long) avgBytes),
                                hourlyBandwidth);
        }

        /**
         * Format bytes to human-readable string.
         */
        private String formatBytes(long bytes) {
                if (bytes < 1024)
                        return bytes + " B";
                if (bytes < 1024 * 1024)
                        return String.format("%.2f KB", bytes / 1024.0);
                if (bytes < 1024 * 1024 * 1024)
                        return String.format("%.2f MB", bytes / (1024.0 * 1024));
                return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
        }
}
