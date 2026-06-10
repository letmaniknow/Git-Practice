package com.mmva.newsapp.controller.admin.news;

import com.mmva.newsapp.domain.news.dto.dashboard.AdminDashboardStatsDto;
import com.mmva.newsapp.domain.news.dto.audit.NewsAuditLogDto;
import com.mmva.newsapp.domain.news.service.dashboard.AdminDashboardService;
import com.mmva.newsapp.infrastructure.common.api.dto.ApiResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import com.mmva.newsapp.infrastructure.common.util.SortUtils;

/**
 * Admin Dashboard Controller - Provides dashboard statistics and recent
 * activity
 * 
 * Endpoints:
 * - GET /api/v1/admin/dashboard/stats - Get aggregated dashboard statistics
 * - GET /api/v1/admin/dashboard/recent-activity - Get recent article activity
 * (paginated)
 * 
 * @author MMVA Team
 * @version 1.0
 * @since 2026-05-10
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/admin/dashboard")
@Tag(name = "Admin - Dashboard", description = "Admin Dashboard APIs for statistics and activity tracking")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*", exposedHeaders = "*")
public class AdminDashboardController {

    private static final String DEFAULT_PAGE = "0";
    private static final String DEFAULT_SIZE = "10";
    private static final int MAX_PAGE_SIZE = 100;
    private static final String DEFAULT_SORT = "createdAt,desc";

    private final AdminDashboardService adminDashboardService;

    /**
     * Get dashboard statistics
     * 
     * Returns aggregated metrics including:
     * - Total articles count
     * - Articles published this month
     * - Draft count
     * - Scheduled count
     * - Archived count
     * - System health status
     * - Total page views
     * - Total engagement
     * 
     * @return dashboard statistics
     */
    @GetMapping("/stats")
    @PreAuthorize("hasAuthority('news.read')")
    @Operation(summary = "Get Dashboard Statistics", description = "Retrieves aggregated news statistics for the admin dashboard")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dashboard statistics retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AdminDashboardStatsDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ApiResponseDto<AdminDashboardStatsDto>> getDashboardStats() {
        log.info("📊 GET /api/v1/admin/dashboard/stats - Fetching dashboard statistics");

        try {
            AdminDashboardStatsDto stats = adminDashboardService.getDashboardStats();

            ApiResponseDto<AdminDashboardStatsDto> response = ApiResponseDto.success(
                    "Dashboard statistics retrieved successfully",
                    stats);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Error fetching dashboard statistics", e);

            ApiResponseDto<AdminDashboardStatsDto> errorResponse = ApiResponseDto.error(
                    "Failed to fetch dashboard statistics: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get recent activity
     * 
     * Returns recent article operations including:
     * - Article creation
     * - Publication
     * - Updates
     * - Archival
     * - Deletion
     * 
     * @param limit maximum number of activities to return (default: 10, max: 100)
     * @return list of recent activities
     */
    @GetMapping("/recent-activity")
    @PreAuthorize("hasAuthority('news.read')")
    @Operation(summary = "Get Recent Activity", description = "Retrieves recent article activities and operations (paginated)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Recent activities retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = NewsAuditLogDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ApiResponseDto<Page<NewsAuditLogDto>>> getRecentActivity(
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = DEFAULT_PAGE) @Min(0) int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = DEFAULT_SIZE) @Min(1) @Max(MAX_PAGE_SIZE) int size,
            @Parameter(description = "Sort order (e.g., createdAt,desc)") @RequestParam(defaultValue = DEFAULT_SORT) String sort) {

        log.info("📝 GET /api/v1/admin/dashboard/recent-activity - page: {}, size: {}, sort: {}", page, size, sort);

        try {
            Pageable pageable = createPageable(page, size, sort);
            Page<NewsAuditLogDto> activities = adminDashboardService.getRecentActivity(pageable);

            ApiResponseDto<Page<NewsAuditLogDto>> response = ApiResponseDto.success(
                    "Recent activities retrieved successfully (" + activities.getTotalElements() + " total, "
                            + activities.getNumberOfElements() + " in page)",
                    activities);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Error fetching recent activity", e);

            ApiResponseDto<Page<NewsAuditLogDto>> errorResponse = ApiResponseDto.error(
                    "Failed to fetch recent activity: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Helper method to create Pageable from page, size, and sort parameters
     */
    private Pageable createPageable(int page, int size, String sort) {
        return PageRequest.of(page, size, Sort.by(SortUtils.parseSort(sort)));
    }
}
