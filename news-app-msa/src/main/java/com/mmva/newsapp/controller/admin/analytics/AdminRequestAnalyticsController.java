package com.mmva.newsapp.controller.admin.analytics;

import com.mmva.newsapp.infrastructure.requestanalytics.config.RequestAnalyticsProperties;
import com.mmva.newsapp.infrastructure.requestanalytics.dto.ComprehensiveAnalyticsResponseDto;
import com.mmva.newsapp.infrastructure.requestanalytics.dto.UserAnalytics;
import com.mmva.newsapp.infrastructure.requestanalytics.model.RequestAnalytics;
import com.mmva.newsapp.infrastructure.requestanalytics.repository.RequestAnalyticsRepository;
import com.mmva.newsapp.infrastructure.requestanalytics.scheduler.RequestAnalyticsCleanupScheduler;
import com.mmva.newsapp.infrastructure.requestanalytics.service.RequestAnalyticsService;
import com.mmva.newsapp.infrastructure.common.api.dto.ApiResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Admin controller for viewing and managing system request analytics.
 * 
 * <h3>API Endpoints:</h3>
 * 
 * <pre>
 * BASE PATH: /api/v1/admin/system/analytics/requests
 * 
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │ #  │ Method │ Endpoint                        │ Description            │
 * ├─────────────────────────────────────────────────────────────────────────┤
 * │    │        │ DASHBOARD & STATISTICS                                   │
 * │ 1  │ GET    │ /                                │ Basic request analytics│
 * │ 2  │ GET    │ /comprehensive                   │ Comprehensive analytics│
 * │ 3  │ GET    │ /traffic                         │ Traffic statistics     │
 * │ 4  │ GET    │ /performance                     │ Endpoint performance   │
 * │ 5  │ GET    │ /traffic/hourly                  │ Hourly traffic         │
 * │ 6  │ GET    │ /traffic/daily                   │ Daily traffic          │
 * ├─────────────────────────────────────────────────────────────────────────┤
 * │    │        │ SECURITY MONITORING                                      │
 * │ 7  │ GET    │ /security/suspicious-ips         │ Suspicious IPs         │
 * │ 8  │ GET    │ /requests/by-ip/{ip}             │ Requests by IP         │
 * │ 9  │ GET    │ /requests/errors                 │ Error requests         │
 * ├─────────────────────────────────────────────────────────────────────────┤
 * │    │        │ REQUEST DETAILS                                          │
 * │ 10 │ GET    │ /requests/{id}                   │ Request by ID          │
 * │ 11 │ GET    │ /requests/by-user/{userId}       │ Requests by user       │
 * │ 12 │ GET    │ /requests                        │ Search requests        │
 * ├─────────────────────────────────────────────────────────────────────────┤
 * │    │        │ CONFIGURATION & MAINTENANCE                              │
 * │ 13 │ GET    │ /config                          │ Analytics config       │
 * │ 14 │ GET    │ /storage                         │ Storage statistics     │
 * │ 15 │ DELETE │ /cleanup                         │ Manual cleanup         │
 * └─────────────────────────────────────────────────────────────────────────┘
 * </pre>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/admin/system/analytics/requests")
@RequiredArgsConstructor
@Tag(name = "Admin - System Request Analytics", description = "Admin operations for system request analytics (traffic, performance, security monitoring)")
@SecurityRequirement(name = "bearerAuth")
public class AdminRequestAnalyticsController {

        private static final Logger log = LoggerFactory.getLogger(AdminRequestAnalyticsController.class);

        private final RequestAnalyticsService analyticsService;
        private final RequestAnalyticsRepository analyticsRepository;
        private final RequestAnalyticsProperties properties;
        private final Optional<RequestAnalyticsCleanupScheduler> cleanupScheduler;

        // ========================================
        // Dashboard & Statistics
        // ========================================

        @Operation(summary = "Get comprehensive analytics (ALL METRICS)", description = "Returns complete analytics covering all dimensions: users, performance, security, devices, geography, time patterns, errors, sessions, and content. Industry best practices implementation.")
        @GetMapping("/comprehensive")
        public ResponseEntity<ApiResponseDto<ComprehensiveAnalyticsResponseDto>> getComprehensiveAnalytics(
                        @Parameter(description = "Start date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                        @Parameter(description = "End date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

                Instant start = startDate.atStartOfDay(ZoneOffset.UTC).toInstant();
                Instant end = endDate.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

                return ResponseEntity.ok(ApiResponseDto.success("Comprehensive analytics retrieved",
                                analyticsService.getComprehensiveAnalytics(start, end)));
        }

        @Operation(summary = "Get basic request analytics", description = "Returns basic request analytics including traffic and user statistics")
        @GetMapping
        public ResponseEntity<ApiResponseDto<Map<String, Object>>> getBasicRequestAnalytics(
                        @Parameter(description = "Start date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                        @Parameter(description = "End date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

                Instant start = startDate.atStartOfDay(ZoneOffset.UTC).toInstant();
                Instant end = endDate.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

                // Get basic analytics
                var trafficStats = analyticsService.getTrafficStatistics(start, end);
                var userStats = analyticsService.getUserAnalytics(start, end);

                Map<String, Object> analytics = new HashMap<>();
                analytics.put("period", Map.of(
                                "start", start.toString(),
                                "end", end.toString()));
                analytics.put("traffic", trafficStats);
                analytics.put("users", userStats);

                return ResponseEntity.ok(ApiResponseDto.success("Basic request analytics retrieved", analytics));
        }

        @Operation(summary = "Get traffic statistics", description = "Returns traffic statistics for the specified time range")
        @GetMapping("/traffic")
        public ResponseEntity<ApiResponseDto<RequestAnalyticsService.TrafficStatistics>> getTrafficStatistics(
                        @Parameter(description = "Start date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                        @Parameter(description = "End date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

                Instant start = startDate.atStartOfDay(ZoneOffset.UTC).toInstant();
                Instant end = endDate.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

                return ResponseEntity.ok(ApiResponseDto.success("Traffic statistics retrieved",
                                analyticsService.getTrafficStatistics(start, end)));
        }

        @Operation(summary = "Get user statistics", description = "Returns user analytics including registration, activity, and engagement metrics")
        @GetMapping("/users")
        public ResponseEntity<ApiResponseDto<UserAnalytics>> getUserStatistics(
                        @Parameter(description = "Start date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                        @Parameter(description = "End date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

                Instant start = startDate.atStartOfDay(ZoneOffset.UTC).toInstant();
                Instant end = endDate.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

                return ResponseEntity.ok(ApiResponseDto.success("User statistics retrieved",
                                analyticsService.getUserAnalytics(start, end)));
        }

        @Operation(summary = "Get endpoint performance", description = "Returns performance metrics for all endpoints")
        @GetMapping("/performance")
        public ResponseEntity<ApiResponseDto<List<RequestAnalyticsService.EndpointPerformance>>> getEndpointPerformance(
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

                Instant start = startDate.atStartOfDay(ZoneOffset.UTC).toInstant();
                Instant end = endDate.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

                return ResponseEntity.ok(ApiResponseDto.success("Endpoint performance retrieved",
                                analyticsService.getEndpointPerformance(start, end)));
        }

        @Operation(summary = "Get hourly traffic", description = "Returns hourly request counts for time series charts")
        @GetMapping("/traffic/hourly")
        public ResponseEntity<ApiResponseDto<List<RequestAnalyticsService.HourlyTraffic>>> getHourlyTraffic(
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

                Instant start = startDate.atStartOfDay(ZoneOffset.UTC).toInstant();
                Instant end = endDate.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

                return ResponseEntity
                                .ok(ApiResponseDto.success("Hourly traffic retrieved",
                                                analyticsService.getHourlyTraffic(start, end)));
        }

        @Operation(summary = "Get daily traffic", description = "Returns daily request counts")
        @GetMapping("/traffic/daily")
        public ResponseEntity<ApiResponseDto<List<RequestAnalyticsService.DailyTraffic>>> getDailyTraffic(
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

                Instant start = startDate.atStartOfDay(ZoneOffset.UTC).toInstant();
                Instant end = endDate.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

                return ResponseEntity
                                .ok(ApiResponseDto.success("Daily traffic retrieved",
                                                analyticsService.getDailyTraffic(start, end)));
        }

        // ========================================
        // Security Monitoring
        // ========================================

        @Operation(summary = "Find suspicious IPs", description = "Identifies IPs with unusual activity patterns")
        @GetMapping("/security/suspicious-ips")
        public ResponseEntity<ApiResponseDto<List<RequestAnalyticsService.SuspiciousIp>>> findSuspiciousIps(
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                        @RequestParam(defaultValue = "1000") long volumeThreshold,
                        @RequestParam(defaultValue = "50") long errorThreshold) {

                Instant start = startDate.atStartOfDay(ZoneOffset.UTC).toInstant();
                Instant end = endDate.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

                return ResponseEntity.ok(ApiResponseDto.success("Suspicious IPs retrieved",
                                analyticsService.findSuspiciousIps(start, end, volumeThreshold, errorThreshold)));
        }

        @Operation(summary = "Get requests by IP", description = "Returns all requests from a specific IP address")
        @GetMapping("/requests/by-ip/{ip}")
        public ResponseEntity<ApiResponseDto<List<RequestAnalytics>>> getRequestsByIp(
                        @PathVariable String ip,
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

                Instant start = startDate.atStartOfDay(ZoneOffset.UTC).toInstant();
                Instant end = endDate.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

                return ResponseEntity.ok(ApiResponseDto.success("Requests by IP retrieved",
                                analyticsRepository.findByClientIpAddressAndCreatedAtBetween(ip, start, end)));
        }

        @Operation(summary = "Get error requests", description = "Returns recent requests that resulted in errors")
        @GetMapping("/requests/errors")
        public ResponseEntity<ApiResponseDto<List<RequestAnalytics>>> getErrorRequests(
                        @RequestParam(defaultValue = "24") int hoursBack) {

                Instant after = Instant.now().minusSeconds(hoursBack * 3600L);
                return ResponseEntity.ok(ApiResponseDto.success("Error requests retrieved",
                                analyticsRepository.findByHasErrorTrueAndCreatedAtAfterOrderByCreatedAtDesc(after)));
        }

        // ========================================
        // Request Details
        // ========================================

        @Operation(summary = "Get request by ID", description = "Returns a single request analytics record")
        @GetMapping("/requests/{id}")
        public ResponseEntity<ApiResponseDto<RequestAnalytics>> getRequestById(@PathVariable UUID id) {
                return analyticsRepository.findById(id)
                                .map(r -> ResponseEntity.ok(ApiResponseDto.success("Request retrieved", r)))
                                .orElse(ResponseEntity.notFound().build());
        }

        @Operation(summary = "Get requests by user", description = "Returns all requests from a specific user")
        @GetMapping("/requests/by-user/{userId}")
        public ResponseEntity<ApiResponseDto<List<RequestAnalytics>>> getRequestsByUser(@PathVariable UUID userId) {
                return ResponseEntity.ok(ApiResponseDto.success("Requests by user retrieved",
                                analyticsRepository.findByUserIdOrderByCreatedAtDesc(userId)));
        }

        @Operation(summary = "Search requests", description = "Paginated search for request analytics")
        @GetMapping("/requests")
        public ResponseEntity<ApiResponseDto<Page<RequestAnalytics>>> searchRequests(
                        @PageableDefault(size = 50, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
                return ResponseEntity.ok(
                                ApiResponseDto.success("Requests retrieved", analyticsRepository.findAll(pageable)));
        }

        // ========================================
        // Configuration & Maintenance
        // ========================================

        @Operation(summary = "Get analytics configuration", description = "Returns current analytics configuration")
        @GetMapping("/config")
        public ResponseEntity<ApiResponseDto<Map<String, Object>>> getConfiguration() {
                Map<String, Object> config = new HashMap<>();
                config.put("enabled", properties.isEnabled());
                config.put("async", properties.isAsync());
                config.put("excludePatterns", properties.getExcludePatterns());
                config.put("sampling", Map.of(
                                "enabled", properties.getSampling().isEnabled(),
                                "rate", properties.getSampling().getRate()));
                config.put("retention", Map.of(
                                "enabled", properties.getRetention().isEnabled(),
                                "period", properties.getRetention().getPeriod().toString()));
                config.put("thresholds", Map.of(
                                "slowResponseMs", properties.getThresholds().getSlowResponseMs(),
                                "suspiciousIpRequestCount", properties.getThresholds().getSuspiciousIpRequestCount(),
                                "highErrorRatePercent", properties.getThresholds().getHighErrorRatePercent()));
                return ResponseEntity.ok(ApiResponseDto.success("Configuration retrieved", config));
        }

        @Operation(summary = "Get storage statistics", description = "Returns analytics storage statistics")
        @GetMapping("/storage")
        public ResponseEntity<ApiResponseDto<Map<String, Object>>> getStorageStats() {
                Map<String, Object> stats = new HashMap<>();
                stats.put("totalRecords", analyticsRepository.countTotal());
                stats.put("last24Hours", analyticsRepository.countByCreatedAtBetween(
                                Instant.now().minusSeconds(24 * 3600), Instant.now()));
                stats.put("last7Days", analyticsRepository.countByCreatedAtBetween(
                                Instant.now().minusSeconds(7 * 24 * 3600), Instant.now()));
                return ResponseEntity.ok(ApiResponseDto.success("Storage statistics retrieved", stats));
        }

        @Operation(summary = "Manual cleanup", description = "Manually trigger cleanup of old analytics data")
        @DeleteMapping("/cleanup")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<ApiResponseDto<Map<String, Object>>> manualCleanup(
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate olderThan) {

                Instant cutoff = olderThan.atStartOfDay(ZoneOffset.UTC).toInstant();

                int deleted = cleanupScheduler
                                .map(scheduler -> scheduler.manualCleanup(cutoff))
                                .orElseGet(() -> analyticsRepository.deleteOlderThan(cutoff));

                Map<String, Object> result = new HashMap<>();
                result.put("deletedRecords", deleted);
                result.put("cutoffDate", cutoff.toString());

                log.info("Manual analytics cleanup: deleted {} records older than {}", deleted, cutoff);

                return ResponseEntity.ok(ApiResponseDto.success("Cleanup completed", result));
        }
}
