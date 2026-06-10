package com.mmva.newsapp.controller.admin.ads;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.mmva.newsapp.infrastructure.monetization.ads.external.common.dto.*;
import com.mmva.newsapp.infrastructure.monetization.ads.external.common.enums.ProviderType;
import com.mmva.newsapp.infrastructure.monetization.ads.external.common.service.AdProviderDashboardService;
import com.mmva.newsapp.infrastructure.monetization.ads.external.service.AdProviderMultiTenantDashboardService;
import com.mmva.newsapp.infrastructure.monetization.ads.external.service.AdProviderReportingService;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * REST API Controller for Ad Providers Dashboard
 *
 * Provides comprehensive dashboard data for ad provider metrics visualization
 * and analytics. All endpoints require ADMIN role authorization.
 *
 * Features:
 * - Dashboard overview with aggregated statistics
 * - Provider-specific metrics and trends
 * - Recent activity monitoring
 * - Health status indicators
 * - Date range filtering and pagination
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/admin/ad-providers/dashboard")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "Ad Providers Dashboard", description = "Admin dashboard for ad provider metrics and analytics")
public class AdminAdProvidersDashboardController {

        private final AdProviderDashboardService adProviderDashboardService;
        private final AdProviderMultiTenantDashboardService adProviderMultiTenantDashboardService;
        private final AdProviderReportingService adProviderReportingService;

        /**
         * Get complete dashboard overview
         *
         * Returns comprehensive dashboard data including statistics, metrics by
         * provider,
         * recent activity, and health indicators.
         *
         * @param startDate Optional start date for filtering (default: 30 days ago)
         * @param endDate   Optional end date for filtering (default: today)
         * @param tenantId  Optional tenant ID for multi-tenant isolation (default:
         *                  default-tenant)
         * @return Complete dashboard overview response
         */
        @GetMapping("/overview")
        @Operation(summary = "Get dashboard overview", description = "Returns complete dashboard data with statistics, metrics, and health indicators")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Dashboard overview retrieved successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = AdProviderDashboardOverviewResponse.class))),
                        @ApiResponse(responseCode = "403", description = "Access denied - Admin role required"),
                        @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        public ResponseEntity<AdProviderDashboardOverviewResponse> getDashboardOverview(
                        @Parameter(description = "Start date for metrics filtering (YYYY-MM-DD)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,

                        @Parameter(description = "End date for metrics filtering (YYYY-MM-DD)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,

                        @Parameter(description = "Tenant ID for multi-tenant isolation") @RequestParam(required = false, defaultValue = "default-tenant") String tenantId) {

                log.info("📊 Dashboard overview request - tenant: {}, date range: {} to {}",
                                tenantId, startDate, endDate);

                try {
                        AdProviderDashboardOverviewResponse response = adProviderDashboardService.getDashboardOverview(
                                        tenantId, startDate, endDate);

                        log.info("✅ Dashboard overview retrieved successfully for tenant: {}", tenantId);
                        return ResponseEntity.ok(response);

                } catch (Exception e) {
                        log.error("❌ Error retrieving dashboard overview for tenant: {}", tenantId, e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(createErrorResponse("Failed to retrieve dashboard overview",
                                                        e.getMessage()));
                }
        }

        /**
         * Get dashboard statistics only
         *
         * Returns aggregated statistics without detailed metrics data.
         * Useful for quick status checks and summary displays.
         *
         * @param startDate Optional start date for filtering
         * @param endDate   Optional end date for filtering
         * @param tenantId  Optional tenant ID
         * @return Dashboard statistics
         */
        @GetMapping("/stats")
        @Operation(summary = "Get dashboard statistics", description = "Returns aggregated dashboard statistics without detailed metrics")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = AdProviderDashboardStats.class))),
                        @ApiResponse(responseCode = "403", description = "Access denied - Admin role required")
        })
        public ResponseEntity<AdProviderDashboardStats> getDashboardStats(
                        @Parameter(description = "Start date for metrics filtering") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,

                        @Parameter(description = "End date for metrics filtering") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,

                        @Parameter(description = "Tenant ID for multi-tenant isolation") @RequestParam(required = false, defaultValue = "default-tenant") String tenantId) {

                log.debug("📈 Dashboard stats request - tenant: {}, date range: {} to {}",
                                tenantId, startDate, endDate);

                try {
                        AdProviderDashboardStats stats = adProviderDashboardService.getDashboardStats(
                                        tenantId, startDate, endDate);

                        log.debug("✅ Dashboard stats retrieved successfully for tenant: {}", tenantId);
                        return ResponseEntity.ok(stats);

                } catch (Exception e) {
                        log.error("❌ Error retrieving dashboard stats for tenant: {}", tenantId, e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(createEmptyStats());
                }
        }

        /**
         * Get metrics grouped by provider
         *
         * Returns metrics data organized by provider type for comparative analysis.
         *
         * @param startDate Optional start date for filtering
         * @param endDate   Optional end date for filtering
         * @param tenantId  Optional tenant ID
         * @return Metrics grouped by provider
         */
        @GetMapping("/providers")
        @Operation(summary = "Get metrics by provider", description = "Returns metrics data grouped by ad provider for comparative analysis")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Provider metrics retrieved successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Map.class))),
                        @ApiResponse(responseCode = "403", description = "Access denied - Admin role required")
        })
        public ResponseEntity<Map<ProviderType, List<AdProviderMetricsSummary>>> getMetricsByProvider(
                        @Parameter(description = "Start date for metrics filtering") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,

                        @Parameter(description = "End date for metrics filtering") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,

                        @Parameter(description = "Tenant ID for multi-tenant isolation") @RequestParam(required = false, defaultValue = "default-tenant") String tenantId) {

                log.debug("📋 Provider metrics request - tenant: {}, date range: {} to {}",
                                tenantId, startDate, endDate);

                try {
                        Map<ProviderType, List<AdProviderMetricsSummary>> metricsByProvider = adProviderDashboardService
                                        .getMetricsByProvider(tenantId, startDate, endDate);

                        log.debug("✅ Provider metrics retrieved successfully for tenant: {}", tenantId);
                        return ResponseEntity.ok(metricsByProvider);

                } catch (Exception e) {
                        log.error("❌ Error retrieving provider metrics for tenant: {}", tenantId, e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(Map.of());
                }
        }

        /**
         * Get metrics for specific provider
         *
         * Returns detailed metrics for a single ad provider.
         *
         * @param providerCode Provider code (google-adsense, google-admob,
         *                     facebook-audience-network)
         * @param startDate    Optional start date for filtering
         * @param endDate      Optional end date for filtering
         * @param tenantId     Optional tenant ID
         * @return Provider-specific metrics
         */
        @GetMapping("/providers/{providerCode}")
        @Operation(summary = "Get provider-specific metrics", description = "Returns detailed metrics for a specific ad provider")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Provider metrics retrieved successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = List.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid provider code"),
                        @ApiResponse(responseCode = "403", description = "Access denied - Admin role required")
        })
        public ResponseEntity<List<AdProviderMetricsSummary>> getProviderMetrics(
                        @Parameter(description = "Provider code (google-adsense, google-admob, facebook-audience-network)") @PathVariable String providerCode,

                        @Parameter(description = "Start date for metrics filtering") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,

                        @Parameter(description = "End date for metrics filtering") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,

                        @Parameter(description = "Tenant ID for multi-tenant isolation") @RequestParam(required = false, defaultValue = "default-tenant") String tenantId) {

                log.debug("📊 Provider metrics request - provider: {}, tenant: {}, date range: {} to {}",
                                providerCode, tenantId, startDate, endDate);

                try {
                        ProviderType providerType = ProviderType.fromCode(providerCode);

                        List<AdProviderMetricsSummary> metrics = adProviderDashboardService.getProviderMetrics(
                                        tenantId, providerType, startDate, endDate);

                        log.debug("✅ Provider metrics retrieved successfully for {} in tenant: {}",
                                        providerCode, tenantId);
                        return ResponseEntity.ok(metrics);

                } catch (IllegalArgumentException e) {
                        log.warn("⚠️  Invalid provider code: {}", providerCode);
                        return ResponseEntity.badRequest().body(List.of());
                } catch (Exception e) {
                        log.error("❌ Error retrieving metrics for provider: {}", providerCode, e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(List.of());
                }
        }

        /**
         * Get recent activity
         *
         * Returns the most recent sync operations and activities.
         *
         * @param limit    Maximum number of activities to return (default: 10, max: 50)
         * @param tenantId Optional tenant ID
         * @return List of recent activities
         */
        @GetMapping("/activity")
        @Operation(summary = "Get recent activity", description = "Returns the most recent ad provider sync operations and activities")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Recent activity retrieved successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = List.class))),
                        @ApiResponse(responseCode = "403", description = "Access denied - Admin role required")
        })
        public ResponseEntity<List<AdProviderRecentActivity>> getRecentActivity(
                        @Parameter(description = "Maximum number of activities to return") @RequestParam(required = false, defaultValue = "10") @Min(1) @Max(50) Integer limit,

                        @Parameter(description = "Tenant ID for multi-tenant isolation") @RequestParam(required = false, defaultValue = "default-tenant") String tenantId) {

                log.debug("🔄 Recent activity request - tenant: {}, limit: {}", tenantId, limit);

                try {
                        List<AdProviderRecentActivity> activities = adProviderDashboardService.getRecentActivity(
                                        tenantId, limit);

                        log.debug("✅ Recent activity retrieved successfully for tenant: {}", tenantId);
                        return ResponseEntity.ok(activities);

                } catch (Exception e) {
                        log.error("❌ Error retrieving recent activity for tenant: {}", tenantId, e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(List.of());
                }
        }

        /**
         * Get dashboard health indicators
         *
         * Returns system health status, alerts, and recommendations.
         *
         * @param tenantId Optional tenant ID
         * @return Dashboard health information
         */
        @GetMapping("/health")
        @Operation(summary = "Get dashboard health", description = "Returns system health indicators, alerts, and recommendations")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Health indicators retrieved successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = AdProviderDashboardHealth.class))),
                        @ApiResponse(responseCode = "403", description = "Access denied - Admin role required")
        })
        public ResponseEntity<AdProviderDashboardHealth> getDashboardHealth(
                        @Parameter(description = "Tenant ID for multi-tenant isolation") @RequestParam(required = false, defaultValue = "default-tenant") String tenantId) {

                log.debug("🏥 Dashboard health request - tenant: {}, limit: {}", tenantId);

                try {
                        AdProviderDashboardHealth health = adProviderDashboardService.getDashboardHealth(tenantId);

                        log.debug("✅ Dashboard health retrieved successfully for tenant: {}", tenantId);
                        return ResponseEntity.ok(health);

                } catch (Exception e) {
                        log.error("❌ Error retrieving dashboard health for tenant: {}", tenantId, e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(createDefaultHealth());
                }
        }

        /**
         * Get dashboard overview for specific tenant
         *
         * Returns tenant-specific dashboard data with isolated metrics and analytics.
         *
         * @param tenantId  Tenant identifier (required)
         * @param startDate Optional start date for filtering
         * @param endDate   Optional end date for filtering
         * @return Tenant-specific dashboard overview
         */
        @GetMapping("/tenants/{tenantId}/overview")
        @Operation(summary = "Get tenant dashboard overview", description = "Returns dashboard data isolated to a specific tenant")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Tenant dashboard retrieved successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = AdProviderDashboardOverviewResponse.class))),
                        @ApiResponse(responseCode = "403", description = "Access denied - Admin role required"),
                        @ApiResponse(responseCode = "404", description = "Tenant not found"),
                        @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        public ResponseEntity<AdProviderDashboardOverviewResponse> getTenantDashboardOverview(
                        @Parameter(description = "Tenant identifier", required = true) @PathVariable String tenantId,

                        @Parameter(description = "Start date for metrics filtering (YYYY-MM-DD)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,

                        @Parameter(description = "End date for metrics filtering (YYYY-MM-DD)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

                log.info("🏢 Tenant dashboard overview request - tenant: {}, date range: {} to {}",
                                tenantId, startDate, endDate);

                try {
                        AdProviderDashboardOverviewResponse response = adProviderMultiTenantDashboardService
                                        .getTenantDashboardOverview(tenantId, startDate, endDate);

                        log.info("✅ Tenant dashboard overview retrieved successfully for tenant: {}", tenantId);
                        return ResponseEntity.ok(response);

                } catch (Exception e) {
                        log.error("❌ Error retrieving tenant dashboard for tenant: {}", tenantId, e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(createErrorResponse("Failed to retrieve tenant dashboard",
                                                        "Error: " + e.getMessage()));
                }
        }

        /**
         * Get dashboard overview for all tenants
         *
         * Returns aggregated dashboard data across all tenants for enterprise
         * monitoring.
         *
         * @return Cross-tenant dashboard overview map
         */
        @GetMapping("/tenants/overview")
        @Operation(summary = "Get all tenants dashboard overview", description = "Returns dashboard data aggregated across all tenants")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "All tenants dashboard retrieved successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Map.class))),
                        @ApiResponse(responseCode = "403", description = "Access denied - Admin role required"),
                        @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        public ResponseEntity<Map<String, AdProviderDashboardOverviewResponse>> getAllTenantsDashboardOverview(
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

                log.info("🏢 All tenants dashboard overview request");

                try {
                        Map<String, AdProviderDashboardOverviewResponse> response = adProviderMultiTenantDashboardService
                                        .getAllTenantsDashboardOverview(startDate, endDate);

                        log.info("✅ All tenants dashboard overview retrieved successfully - {} tenants",
                                        response.size());
                        return ResponseEntity.ok(response);

                } catch (Exception e) {
                        log.error("❌ Error retrieving all tenants dashboard", e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(Map.of("error",
                                                        createErrorResponse("Failed to retrieve all tenants dashboard",
                                                                        "Error: " + e.getMessage())));
                }
        }

        /**
         * Get list of active tenants
         *
         * Returns list of tenants with recent activity or data.
         *
         * @return List of active tenant IDs
         */
        @GetMapping("/tenants/active")
        @Operation(summary = "Get active tenants", description = "Returns list of tenants with recent activity")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Active tenants retrieved successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = List.class))),
                        @ApiResponse(responseCode = "403", description = "Access denied - Admin role required"),
                        @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        public ResponseEntity<List<String>> getActiveTenants() {

                log.info("🏢 Active tenants list request");

                try {
                        List<String> activeTenants = adProviderMultiTenantDashboardService.getActiveTenants();

                        log.info("✅ Active tenants retrieved successfully - {} tenants", activeTenants.size());
                        return ResponseEntity.ok(activeTenants);

                } catch (Exception e) {
                        log.error("❌ Error retrieving active tenants", e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(List.of("Error: " + e.getMessage()));
                }
        }

        /**
         * Get usage statistics for specific tenant
         *
         * Returns detailed usage metrics and analytics for a tenant.
         *
         * @param tenantId Tenant identifier (required)
         * @return Tenant usage statistics
         */
        @GetMapping("/tenants/{tenantId}/usage")
        @Operation(summary = "Get tenant usage statistics", description = "Returns detailed usage metrics for a specific tenant")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Tenant usage stats retrieved successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = AdProviderTenantUsageStats.class))),
                        @ApiResponse(responseCode = "403", description = "Access denied - Admin role required"),
                        @ApiResponse(responseCode = "404", description = "Tenant not found"),
                        @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        public ResponseEntity<AdProviderTenantUsageStats> getTenantUsageStats(
                        @Parameter(description = "Tenant identifier", required = true) @PathVariable String tenantId) {

                log.info("📈 Tenant usage statistics request - tenant: {}", tenantId);

                try {
                        AdProviderTenantUsageStats stats = adProviderMultiTenantDashboardService
                                        .getTenantUsageStats(tenantId);

                        log.info("✅ Tenant usage stats retrieved successfully for tenant: {}", tenantId);
                        return ResponseEntity.ok(stats);

                } catch (Exception e) {
                        log.error("❌ Error retrieving tenant usage stats for tenant: {}", tenantId, e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(AdProviderTenantUsageStats.builder()
                                                        .adProviderTenantId(tenantId)
                                                        .build());
                }
        }

        /**
         * Get usage statistics for all tenants
         *
         * Returns usage metrics aggregated across all tenants.
         *
         * @return List of tenant usage statistics
         */
        @GetMapping("/tenants/usage")
        @Operation(summary = "Get all tenants usage statistics", description = "Returns usage metrics for all tenants")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "All tenants usage stats retrieved successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = List.class))),
                        @ApiResponse(responseCode = "403", description = "Access denied - Admin role required"),
                        @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        public ResponseEntity<List<AdProviderTenantUsageStats>> getAllTenantsUsageStats() {

                log.info("📈 All tenants usage statistics request");

                try {
                        List<AdProviderTenantUsageStats> stats = adProviderMultiTenantDashboardService
                                        .getAllTenantsUsageStats();

                        log.info("✅ All tenants usage stats retrieved successfully - {} tenants", stats.size());
                        return ResponseEntity.ok(stats);

                } catch (Exception e) {
                        log.error("❌ Error retrieving all tenants usage stats", e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(List.of());
                }
        }

        /**
         * Get system-wide usage statistics
         *
         * Returns aggregated usage metrics across the entire system.
         *
         * @return System-wide usage statistics
         */
        @GetMapping("/system/usage")
        @Operation(summary = "Get system-wide usage statistics", description = "Returns aggregated usage metrics across all tenants")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "System usage stats retrieved successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = AdProviderTenantUsageStats.class))),
                        @ApiResponse(responseCode = "403", description = "Access denied - Admin role required"),
                        @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        public ResponseEntity<AdProviderTenantUsageStats> getSystemWideUsageStats() {

                log.info("🌐 System-wide usage statistics request");

                try {
                        AdProviderTenantUsageStats stats = adProviderMultiTenantDashboardService
                                        .getSystemWideUsageStats();

                        log.info("✅ System-wide usage stats retrieved successfully");
                        return ResponseEntity.ok(stats);

                } catch (Exception e) {
                        log.error("❌ Error retrieving system-wide usage stats", e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(AdProviderTenantUsageStats.builder()
                                                        .adProviderTenantId("SYSTEM")
                                                        .build());
                }
        }

        /**
         * Export dashboard overview as PDF
         *
         * Generates and downloads a comprehensive PDF report of the dashboard data.
         *
         * @param tenantId  Tenant identifier
         * @param startDate Optional start date for filtering
         * @param endDate   Optional end date for filtering
         * @return PDF file download
         */
        @GetMapping("/export/pdf")
        @Operation(summary = "Export dashboard as PDF", description = "Generates and downloads a PDF report of dashboard data")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "PDF report generated successfully", content = @Content(mediaType = "application/pdf")),
                        @ApiResponse(responseCode = "403", description = "Access denied - Admin role required"),
                        @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        public ResponseEntity<byte[]> exportDashboardAsPdf(
                        @Parameter(description = "Tenant identifier") @RequestParam(required = false, defaultValue = "default-tenant") String tenantId,

                        @Parameter(description = "Start date for metrics filtering (YYYY-MM-DD)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,

                        @Parameter(description = "End date for metrics filtering (YYYY-MM-DD)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

                log.info("📄 PDF export request - tenant: {}, date range: {} to {}",
                                tenantId, startDate, endDate);

                try {
                        AdProviderDashboardOverviewResponse overview = adProviderDashboardService.getDashboardOverview(
                                        tenantId, startDate, endDate);

                        java.io.ByteArrayOutputStream pdfReport = adProviderReportingService
                                        .generateDashboardPdfReport(overview, tenantId);

                        HttpHeaders headers = new HttpHeaders();
                        headers.setContentType(MediaType.APPLICATION_PDF);
                        headers.setContentDispositionFormData("attachment",
                                        "ad-providers-dashboard-" + tenantId + "-" +
                                                        java.time.LocalDate.now() + ".pdf");

                        log.info("✅ PDF report generated successfully for tenant: {}", tenantId);
                        return ResponseEntity.ok()
                                        .headers(headers)
                                        .body(pdfReport.toByteArray());

                } catch (Exception e) {
                        log.error("❌ Error generating PDF report for tenant: {}", tenantId, e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body("Error generating PDF report".getBytes());
                }
        }

        /**
         * Export dashboard overview as Excel
         *
         * Generates and downloads an Excel spreadsheet with dashboard analytics.
         *
         * @param tenantId  Tenant identifier
         * @param startDate Optional start date for filtering
         * @param endDate   Optional end date for filtering
         * @return Excel file download
         */
        @GetMapping("/export/excel")
        @Operation(summary = "Export dashboard as Excel", description = "Generates and downloads an Excel report of dashboard data")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Excel report generated successfully", content = @Content(mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")),
                        @ApiResponse(responseCode = "403", description = "Access denied - Admin role required"),
                        @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        public ResponseEntity<byte[]> exportDashboardAsExcel(
                        @Parameter(description = "Tenant identifier") @RequestParam(required = false, defaultValue = "default-tenant") String tenantId,

                        @Parameter(description = "Start date for metrics filtering (YYYY-MM-DD)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,

                        @Parameter(description = "End date for metrics filtering (YYYY-MM-DD)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

                log.info("📊 Excel export request - tenant: {}, date range: {} to {}",
                                tenantId, startDate, endDate);

                try {
                        AdProviderDashboardOverviewResponse overview = adProviderDashboardService.getDashboardOverview(
                                        tenantId, startDate, endDate);

                        java.io.ByteArrayOutputStream excelReport = adProviderReportingService
                                        .generateDashboardExcelReport(overview, tenantId);

                        HttpHeaders headers = new HttpHeaders();
                        headers.setContentType(
                                        MediaType.parseMediaType(
                                                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
                        headers.setContentDispositionFormData("attachment",
                                        "ad-providers-dashboard-" + tenantId + "-" +
                                                        java.time.LocalDate.now() + ".xlsx");

                        log.info("✅ Excel report generated successfully for tenant: {}", tenantId);
                        return ResponseEntity.ok()
                                        .headers(headers)
                                        .body(excelReport.toByteArray());

                } catch (Exception e) {
                        log.error("❌ Error generating Excel report for tenant: {}", tenantId, e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body("Error generating Excel report".getBytes());
                }
        }

        /**
         * Export tenant usage statistics as PDF
         *
         * Generates a PDF report of usage statistics for a specific tenant.
         *
         * @param tenantId Tenant identifier (required)
         * @return PDF file download
         */
        @GetMapping("/tenants/{tenantId}/usage/export/pdf")
        @Operation(summary = "Export tenant usage stats as PDF", description = "Generates a PDF report of tenant usage statistics")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "PDF usage report generated successfully", content = @Content(mediaType = "application/pdf")),
                        @ApiResponse(responseCode = "403", description = "Access denied - Admin role required"),
                        @ApiResponse(responseCode = "404", description = "Tenant not found"),
                        @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        public ResponseEntity<byte[]> exportTenantUsageAsPdf(
                        @Parameter(description = "Tenant identifier", required = true) @PathVariable String tenantId) {

                log.info("📄 Tenant usage PDF export request - tenant: {}", tenantId);

                try {
                        AdProviderTenantUsageStats usageStats = adProviderMultiTenantDashboardService
                                        .getTenantUsageStats(tenantId);

                        java.io.ByteArrayOutputStream pdfReport = adProviderReportingService
                                        .generateUsageStatsPdfReport(usageStats, tenantId);

                        HttpHeaders headers = new HttpHeaders();
                        headers.setContentType(MediaType.APPLICATION_PDF);
                        headers.setContentDispositionFormData("attachment",
                                        "tenant-usage-stats-" + tenantId + "-" +
                                                        java.time.LocalDate.now() + ".pdf");

                        log.info("✅ Tenant usage PDF report generated successfully for tenant: {}", tenantId);
                        return ResponseEntity.ok()
                                        .headers(headers)
                                        .body(pdfReport.toByteArray());

                } catch (Exception e) {
                        log.error("❌ Error generating tenant usage PDF report for tenant: {}", tenantId, e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body("Error generating PDF report".getBytes());
                }
        }

        /**
         * Export system-wide analytics as PDF
         *
         * Generates a comprehensive PDF report of system-wide analytics across all
         * tenants.
         *
         * @return PDF file download
         */
        @GetMapping("/system/analytics/export/pdf")
        @Operation(summary = "Export system analytics as PDF", description = "Generates a PDF report of system-wide analytics")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "System analytics PDF generated successfully", content = @Content(mediaType = "application/pdf")),
                        @ApiResponse(responseCode = "403", description = "Access denied - Admin role required"),
                        @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        public ResponseEntity<byte[]> exportSystemAnalyticsAsPdf() {

                log.info("📄 System analytics PDF export request");

                try {
                        List<AdProviderTenantUsageStats> allTenantStats = adProviderMultiTenantDashboardService
                                        .getAllTenantsUsageStats();
                        AdProviderTenantUsageStats systemStats = adProviderMultiTenantDashboardService
                                        .getSystemWideUsageStats();

                        java.io.ByteArrayOutputStream pdfReport = adProviderReportingService
                                        .generateSystemAnalyticsPdfReport(allTenantStats, systemStats);

                        HttpHeaders headers = new HttpHeaders();
                        headers.setContentType(MediaType.APPLICATION_PDF);
                        headers.setContentDispositionFormData("attachment",
                                        "system-analytics-" + java.time.LocalDate.now() + ".pdf");

                        log.info("✅ System analytics PDF report generated successfully");
                        return ResponseEntity.ok()
                                        .headers(headers)
                                        .body(pdfReport.toByteArray());

                } catch (Exception e) {
                        log.error("❌ Error generating system analytics PDF report", e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body("Error generating PDF report".getBytes());
                }
        }

        /**
         * Export date range analytics report
         *
         * Generates a report for a specific date range with trend analysis.
         *
         * @param tenantId  Tenant identifier
         * @param startDate Start date for the report (required)
         * @param endDate   End date for the report (required)
         * @param format    Report format (PDF or EXCEL, default: PDF)
         * @return Report file download
         */
        @GetMapping("/analytics/date-range/export")
        @Operation(summary = "Export date range analytics", description = "Generates a report for a specific date range")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Date range report generated successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid date range or format"),
                        @ApiResponse(responseCode = "403", description = "Access denied - Admin role required"),
                        @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        public ResponseEntity<byte[]> exportDateRangeAnalytics(
                        @Parameter(description = "Tenant identifier") @RequestParam(required = false, defaultValue = "default-tenant") String tenantId,

                        @Parameter(description = "Start date for the report (YYYY-MM-DD)", required = true) @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,

                        @Parameter(description = "End date for the report (YYYY-MM-DD)", required = true) @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,

                        @Parameter(description = "Report format (PDF or EXCEL)") @RequestParam(required = false, defaultValue = "PDF") String format) {

                log.info("📅 Date range analytics export request - tenant: {}, range: {} to {}, format: {}",
                                tenantId, startDate, endDate, format);

                if (startDate.isAfter(endDate)) {
                        log.warn("❌ Invalid date range: start date {} is after end date {}", startDate, endDate);
                        return ResponseEntity.badRequest()
                                        .body("Start date must be before or equal to end date".getBytes());
                }

                try {
                        java.io.ByteArrayOutputStream report = adProviderReportingService
                                        .generateDateRangeAnalyticsReport(tenantId, startDate, endDate,
                                                        format.toUpperCase());

                        HttpHeaders headers = new HttpHeaders();
                        String fileName = "date-range-analytics-" + tenantId + "-" +
                                        startDate + "-to-" + endDate + "." +
                                        (format.equalsIgnoreCase("EXCEL") ? "xlsx" : "pdf");

                        if (format.equalsIgnoreCase("EXCEL")) {
                                headers.setContentType(
                                                MediaType.parseMediaType(
                                                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
                        } else {
                                headers.setContentType(MediaType.APPLICATION_PDF);
                        }

                        headers.setContentDispositionFormData("attachment", fileName);

                        log.info("✅ Date range analytics report generated successfully - format: {}", format);
                        return ResponseEntity.ok()
                                        .headers(headers)
                                        .body(report.toByteArray());

                } catch (Exception e) {
                        log.error("❌ Error generating date range analytics report", e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body("Error generating report".getBytes());
                }
        }

        // Private helper methods

        private AdProviderDashboardOverviewResponse createErrorResponse(String message, String details) {
                return AdProviderDashboardOverviewResponse.builder()
                                .adProviderDashboardMetadata(AdProviderDashboardMetadata.builder()
                                                .adProviderRequestTimestamp(LocalDateTime.now())
                                                .adProviderResponseTimestamp(LocalDateTime.now())
                                                .adProviderApiVersion("v1")
                                                .adProviderDataSource("ERROR")
                                                .build())
                                .build();
        }

        private AdProviderDashboardStats createEmptyStats() {
                return AdProviderDashboardStats.builder()
                                .adProviderTotalProviders(0)
                                .adProviderActiveProviders(0)
                                .adProviderTotalMetricsRecords(0L)
                                .build();
        }

        private AdProviderDashboardHealth createDefaultHealth() {
                return AdProviderDashboardHealth.builder()
                                .adProviderSystemHealthStatus("UNKNOWN")
                                .adProviderDataFreshnessScore(0)
                                .adProviderActiveAlerts(List.of())
                                .adProviderSystemRecommendations(List.of("Unable to determine system health"))
                                .build();
        }
}