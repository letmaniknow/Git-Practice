package com.mmva.newsapp.controller.admin.newsengagement;

import com.mmva.newsapp.infrastructure.common.api.dto.ApiResponseDto;
import com.mmva.newsapp.domain.newsengagement.shares.dto.PlatformShareStatDto;
import com.mmva.newsapp.domain.newsengagement.views.dto.NewsViewRequestDto;
import com.mmva.newsapp.domain.newsengagement.views.dto.NewsViewResponseDto;

// ========================================
// Service Imports
// ========================================
import com.mmva.newsapp.domain.adminuser.service.validation.AdminValidationService;
import com.mmva.newsapp.domain.newsengagement.bookmarks.service.NewsBookmarkService;
import com.mmva.newsapp.domain.newsengagement.comments.service.NewsCommentService;
import com.mmva.newsapp.domain.newsengagement.likes.service.NewsLikeService;
import com.mmva.newsapp.domain.newsengagement.shares.service.NewsShareService;
import com.mmva.newsapp.domain.newsengagement.views.service.NewsViewService;

// ========================================
// OpenAPI/Swagger Imports
// ========================================
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

// ========================================
// Jakarta Validation Imports
// ========================================
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

// ========================================
// Lombok Imports
// ========================================
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// ========================================
// Spring Framework Imports
// ========================================
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// ========================================
// Java Time Imports
// ========================================
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
// Java Core Imports
// ========================================
import java.util.stream.Collectors;

/**
 * Admin API Controller for News Engagement Analytics.
 * 
 * <p>
 * Provides engagement analytics data for admin dashboards including:
 * </p>
 * <ul>
 * <li>View tracking and statistics</li>
 * <li>Like statistics</li>
 * <li>Share statistics</li>
 * <li>Comment statistics</li>
 * <li>Bookmark statistics</li>
 * </ul>
 * 
 * <h3>API Endpoints:</h3>
 * 
 * <pre>
 * BASE PATH: /api/v1/admin/engagement/analytics
 * 
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │ #  │ Method │ Endpoint                        │ Description            │
 * ├─────────────────────────────────────────────────────────────────────────┤
 * │ 1  │ GET    │ /news/{newsId}/summary          │ Aggregated summary     │
 * ├─────────────────────────────────────────────────────────────────────────┤
 * │    │        │ VIEW ANALYTICS                                           │
 * │ 2  │ GET    │ /views                          │ All views (paginated)  │
 * │ 3  │ GET    │ /views/news/{newsId}            │ Views by news          │
 * │ 4  │ GET    │ /views/user/{userId}            │ Views by user          │
 * │ 5  │ GET    │ /views/count/news/{newsId}      │ View count             │
 * │ 6  │ POST   │ /views                          │ Record a view          │
 * │ 7  │ DELETE │ /views/{viewId}                 │ Delete view record     │
 * ├─────────────────────────────────────────────────────────────────────────┤
 * │    │        │ LIKE ANALYTICS                                           │
 * │ 8  │ GET    │ /likes/count/news/{newsId}      │ Like count             │
 * ├─────────────────────────────────────────────────────────────────────────┤
 * │    │        │ SHARE ANALYTICS                                          │
 * │ 9  │ GET    │ /shares/count/news/{newsId}     │ Share count            │
 * │ 10 │ GET    │ /shares/platforms/news/{newsId} │ Platform breakdown     │
 * ├─────────────────────────────────────────────────────────────────────────┤
 * │    │        │ COMMENT ANALYTICS                                        │
 * │ 11 │ GET    │ /comments/count/news/{newsId}   │ Comment count          │
 * ├─────────────────────────────────────────────────────────────────────────┤
 * │    │        │ BOOKMARK ANALYTICS                                       │
 * │ 12 │ GET    │ /bookmarks/count/news/{newsId}  │ Bookmark count         │
 * └─────────────────────────────────────────────────────────────────────────┘
 * </pre>
 * 
 * @author MMVA Team
 * @since 1.0.0
 * @see NewsViewService
 * @see NewsLikeService
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/admin/engagement/analytics")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = { "Range", "Accept", "Content-Type" }, exposedHeaders = { "Content-Length",
                "Content-Range", "Accept-Ranges" })
@Tag(name = "Admin - Engagement Analytics", description = "Admin operations for news engagement analytics (views, likes, shares, comments, bookmarks)")
@SecurityRequirement(name = "bearerAuth")
public class AdminEngagementAnalyticsController {

        // ========================================
        // Constants
        // ========================================

        private static final String DEFAULT_PAGE = "0";
        private static final String DEFAULT_SIZE = "10";
        private static final int MAX_PAGE_SIZE = 100;

        // ========================================
        // Dependencies
        // ========================================

        private final NewsViewService viewService;
        private final NewsLikeService likeService;
        private final NewsShareService shareService;
        private final NewsCommentService commentService;
        private final NewsBookmarkService bookmarkService;
        private final AdminValidationService adminValidationService;

        // ========================================
        // Overall Analytics
        // ========================================

        /**
         * Get overall engagement analytics.
         * 
         * @return Overall engagement metrics
         */
        @GetMapping
        @Operation(summary = "Get overall engagement analytics", description = "Returns comprehensive engagement metrics across all content")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Analytics retrieved successfully")
        })
        public ResponseEntity<ApiResponseDto<Map<String, Object>>> getOverallEngagementAnalytics(
                        @Parameter(description = "Start date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                        @Parameter(description = "End date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.debug("Admin: Fetching overall engagement analytics - adminId: {}", authenticatedAdminId);

                Instant start = startDate.atStartOfDay(ZoneOffset.UTC).toInstant();
                Instant end = endDate.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

                try {
                        // Get total counts from all services within date range
                        long totalViews = viewService.getTotalViewCountBetween(start, end);
                        long totalLikes = likeService.getTotalLikeCountBetween(start, end);
                        long totalShares = shareService.getTotalShareCountBetween(start, end);
                        long totalComments = commentService.getTotalCommentCountBetween(start, end);
                        long totalBookmarks = bookmarkService.getTotalBookmarkCountBetween(start, end);

                        // Calculate engagement rate (simplified)
                        long totalEngagements = totalLikes + totalShares + totalComments + totalBookmarks;
                        double engagementRate = totalViews > 0 ? (double) totalEngagements / totalViews * 100.0 : 0.0;

                        Map<String, Object> analytics = new HashMap<>();
                        analytics.put("totalViews", totalViews);
                        analytics.put("totalLikes", totalLikes);
                        analytics.put("totalShares", totalShares);
                        analytics.put("totalComments", totalComments);
                        analytics.put("totalBookmarks", totalBookmarks);
                        analytics.put("totalEngagements", totalEngagements);
                        analytics.put("engagementRate", Math.round(engagementRate * 100.0) / 100.0);
                        analytics.put("period", Map.of(
                                        "start", start.toString(),
                                        "end", end.toString()));

                        return ResponseEntity.ok(
                                        ApiResponseDto.success("Overall engagement analytics retrieved", analytics));

                } catch (Exception e) {
                        log.error("Error retrieving overall engagement analytics", e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponseDto.error("Failed to retrieve engagement analytics"));
                }
        }

        // ========================================
        // 1. Aggregated Statistics Endpoint
        // ========================================

        /**
         * 1. Get aggregated engagement summary for a news article.
         * 
         * @param newsId News article ID
         * @return Aggregated metrics (views, likes, shares, comments, bookmarks)
         */
        @GetMapping("/news/{newsId}/summary")
        @Operation(summary = "1. Get news engagement summary", description = "Retrieves aggregated engagement metrics for a news article including views, likes, shares, comments, and bookmarks")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Summary retrieved successfully"),
                        @ApiResponse(responseCode = "404", description = "News not found")
        })
        public ResponseEntity<ApiResponseDto<Map<String, Object>>> getNewsSummary(
                        @Parameter(description = "News article ID") @PathVariable UUID newsId) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.debug("Admin: Fetching engagement summary - adminId: {}, newsId: {}", authenticatedAdminId, newsId);

                Map<String, Object> summary = new HashMap<>();
                summary.put("newsId", newsId);
                summary.put("viewCount", viewService.getViewCountByNewsId(newsId));
                summary.put("likeCount", likeService.getLikeCountByNewsId(newsId));
                summary.put("shareCount", shareService.getShareCountByNewsId(newsId));
                summary.put("commentCount", commentService.getCommentCountByNewsId(newsId));
                summary.put("bookmarkCount", bookmarkService.getBookmarkCountByNewsId(newsId));

                log.debug("Admin: Engagement summary retrieved - adminId: {}, newsId: {}", authenticatedAdminId,
                                newsId);
                return ResponseEntity.ok(ApiResponseDto.success("News engagement summary retrieved", summary));
        }

        // ========================================
        // 2-7. View Analytics Endpoints
        // ========================================

        /**
         * 2. Get all views with pagination.
         * 
         * @param page Page number (0-based)
         * @param size Page size (1-100)
         * @return Paginated list of views
         */
        @GetMapping("/views")
        @Operation(summary = "2. Get all views with pagination", description = "Retrieves a paginated list of all news views")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Views retrieved successfully")
        })
        public ResponseEntity<ApiResponseDto<Page<NewsViewResponseDto>>> getAllViews(
                        @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = DEFAULT_PAGE) @Min(0) int page,
                        @Parameter(description = "Page size (1-100)") @RequestParam(defaultValue = DEFAULT_SIZE) @Min(1) @Max(MAX_PAGE_SIZE) int size) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.debug("Admin: Fetching all views - adminId: {}, page: {}, size: {}", authenticatedAdminId, page,
                                size);

                Pageable pageable = PageRequest.of(page, Math.min(size, MAX_PAGE_SIZE));
                Page<NewsViewResponseDto> views = viewService.getAllViews(pageable);

                log.debug("Admin: Retrieved {} views on page {} - adminId: {}", views.getNumberOfElements(), page,
                                authenticatedAdminId);
                return ResponseEntity.ok(ApiResponseDto.success("Views retrieved successfully", views));
        }

        /**
         * 3. Get views by news ID.
         * 
         * @param newsId News article ID
         * @return List of views for the news article
         */
        @GetMapping("/views/news/{newsId}")
        @Operation(summary = "3. Get views by news ID", description = "Retrieves all views for a specific news article")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Views retrieved successfully"),
                        @ApiResponse(responseCode = "404", description = "News not found")
        })
        public ResponseEntity<ApiResponseDto<List<NewsViewResponseDto>>> getViewsByNewsId(
                        @Parameter(description = "News article ID") @PathVariable UUID newsId) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.debug("Admin: Fetching views for news - adminId: {}, newsId: {}", authenticatedAdminId, newsId);

                List<NewsViewResponseDto> views = viewService.getViewsByNewsId(newsId);

                log.debug("Admin: Retrieved {} views for news {} - adminId: {}", views.size(), newsId,
                                authenticatedAdminId);
                return ResponseEntity.ok(ApiResponseDto.success("Views retrieved successfully", views));
        }

        /**
         * 4. Get views by user ID.
         * 
         * @param userId User ID
         * @return List of views by the user
         */
        @GetMapping("/views/user/{userId}")
        @Operation(summary = "4. Get views by user ID", description = "Retrieves all views by a specific user")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Views retrieved successfully"),
                        @ApiResponse(responseCode = "404", description = "User not found")
        })
        public ResponseEntity<ApiResponseDto<List<NewsViewResponseDto>>> getViewsByUserId(
                        @Parameter(description = "User ID") @PathVariable UUID userId) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.debug("Admin: Fetching views for user - adminId: {}, userId: {}", authenticatedAdminId, userId);

                List<NewsViewResponseDto> views = viewService.getViewsByUserId(userId);

                log.debug("Admin: Retrieved {} views for user {} - adminId: {}", views.size(), userId,
                                authenticatedAdminId);
                return ResponseEntity.ok(ApiResponseDto.success("Views retrieved successfully", views));
        }

        /**
         * 5. Get view count by news ID.
         * 
         * @param newsId News article ID
         * @return Total view count
         */
        @GetMapping("/views/count/news/{newsId}")
        @Operation(summary = "5. Get view count by news ID", description = "Returns the total number of views for a news article")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "View count retrieved successfully"),
                        @ApiResponse(responseCode = "404", description = "News not found")
        })
        public ResponseEntity<ApiResponseDto<Long>> getViewCountByNewsId(
                        @Parameter(description = "News article ID") @PathVariable UUID newsId) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.debug("Admin: Fetching view count - adminId: {}, newsId: {}", authenticatedAdminId, newsId);

                Long count = viewService.getViewCountByNewsId(newsId);

                log.debug("Admin: View count for news {}: {} - adminId: {}", newsId, count, authenticatedAdminId);
                return ResponseEntity.ok(ApiResponseDto.success("View count retrieved", count));
        }

        /**
         * 6. Record a view.
         * 
         * @param dto View request data
         * @return Created view record
         */
        @PostMapping("/views")
        @Operation(summary = "6. Record a view", description = "Records a view for a news article")
        @ApiResponses({
                        @ApiResponse(responseCode = "201", description = "View recorded successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid request data")
        })
        public ResponseEntity<ApiResponseDto<NewsViewResponseDto>> recordView(
                        @Valid @RequestBody NewsViewRequestDto dto) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.info("Admin: Adding view - adminId: {}, newsId: {}", authenticatedAdminId,
                                dto.getNewsViewsNewsId());

                NewsViewResponseDto response = viewService.addView(dto);

                log.info("Admin: View added successfully - adminId: {}, newsId: {}", authenticatedAdminId,
                                dto.getNewsViewsNewsId());
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponseDto.success("View recorded successfully", response));
        }

        /**
         * 7. Delete a view record.
         * 
         * @param viewId View record ID
         * @return Success response
         */
        @DeleteMapping("/views/{viewId}")
        @Operation(summary = "7. Delete a view record", description = "Deletes a specific view record")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "View deleted successfully"),
                        @ApiResponse(responseCode = "404", description = "View not found")
        })
        public ResponseEntity<ApiResponseDto<Void>> deleteView(
                        @Parameter(description = "View record ID") @PathVariable Long viewId) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.info("Admin: Removing view record - adminId: {}, viewId: {}", authenticatedAdminId, viewId);

                viewService.removeViewByAdmin(viewId);

                log.info("Admin: View record removed - adminId: {}, viewId: {}", authenticatedAdminId, viewId);
                return ResponseEntity.ok(ApiResponseDto.success("View deleted successfully", null));
        }

        // ========================================
        // 8. Like Analytics Endpoint
        // ========================================

        /**
         * 8. Get like count by news ID.
         * 
         * @param newsId News article ID
         * @return Total like count
         */
        @GetMapping("/likes/count/news/{newsId}")
        @Operation(summary = "8. Get like count by news ID", description = "Returns the total number of likes for a news article")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Like count retrieved successfully"),
                        @ApiResponse(responseCode = "404", description = "News not found")
        })
        public ResponseEntity<ApiResponseDto<Long>> getLikeCountByNewsId(
                        @Parameter(description = "News article ID") @PathVariable UUID newsId) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.debug("Admin: Fetching like count - adminId: {}, newsId: {}", authenticatedAdminId, newsId);

                Long count = likeService.getLikeCountByNewsId(newsId);

                log.debug("Admin: Like count for news {}: {} - adminId: {}", newsId, count, authenticatedAdminId);
                return ResponseEntity.ok(ApiResponseDto.success("Like count retrieved", count));
        }

        // ========================================
        // 9. Share Analytics Endpoint
        // ========================================

        /**
         * 9. Get share count by news ID.
         * 
         * @param newsId News article ID
         * @return Total share count
         */
        @GetMapping("/shares/count/news/{newsId}")
        @Operation(summary = "9. Get share count by news ID", description = "Returns the total number of shares for a news article")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Share count retrieved successfully"),
                        @ApiResponse(responseCode = "404", description = "News not found")
        })
        public ResponseEntity<ApiResponseDto<Long>> getShareCountByNewsId(
                        @Parameter(description = "News article ID") @PathVariable UUID newsId) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.debug("Admin: Fetching share count - adminId: {}, newsId: {}", authenticatedAdminId, newsId);

                Long count = shareService.getShareCountByNewsId(newsId);

                log.debug("Admin: Share count for news {}: {} - adminId: {}", newsId, count, authenticatedAdminId);
                return ResponseEntity.ok(ApiResponseDto.success("Share count retrieved", count));
        }

        /**
         * 10. Get share statistics by platform for news ID.
         * 
         * @param newsId News article ID
         * @return Platform share breakdown with percentages
         */
        @GetMapping("/shares/platforms/news/{newsId}")
        @Operation(summary = "10. Get share platform statistics", description = "Returns share statistics grouped by platform (facebook, twitter, whatsapp, etc.) ordered by popularity")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Platform statistics retrieved successfully"),
                        @ApiResponse(responseCode = "404", description = "News not found")
        })
        public ResponseEntity<ApiResponseDto<List<PlatformShareStatDto>>> getSharesByPlatform(
                        @Parameter(description = "News article ID") @PathVariable UUID newsId) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.debug("Admin: Fetching share platform statistics - adminId: {}, newsId: {}", authenticatedAdminId,
                                newsId);

                List<Object[]> rawStats = shareService.getSharesByPlatform(newsId);

                // Calculate total for percentage
                long totalShares = rawStats.stream()
                                .mapToLong(arr -> (Long) arr[1])
                                .sum();

                // Transform to DTO with percentage calculation
                List<PlatformShareStatDto> stats = rawStats.stream()
                                .map(arr -> {
                                        String platform = (String) arr[0];
                                        Long count = (Long) arr[1];
                                        Double percentage = totalShares > 0
                                                        ? Math.round((count * 10000.0) / totalShares) / 100.0
                                                        : 0.0;

                                        return PlatformShareStatDto.builder()
                                                        .platform(platform)
                                                        .shareCount(count)
                                                        .percentage(percentage)
                                                        .build();
                                })
                                .collect(Collectors.toList());

                log.debug("Admin: Retrieved {} platform statistics for news {} (total shares: {}) - adminId: {}",
                                stats.size(), newsId, totalShares, authenticatedAdminId);
                return ResponseEntity.ok(ApiResponseDto.success("Platform statistics retrieved", stats));
        }

        // ========================================
        // 11. Comment Analytics Endpoint
        // ========================================

        /**
         * 11. Get comment count by news ID.
         * 
         * @param newsId News article ID
         * @return Total comment count
         */
        @GetMapping("/comments/count/news/{newsId}")
        @Operation(summary = "11. Get comment count by news ID", description = "Returns the total number of comments for a news article")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Comment count retrieved successfully"),
                        @ApiResponse(responseCode = "404", description = "News not found")
        })
        public ResponseEntity<ApiResponseDto<Long>> getCommentCountByNewsId(
                        @Parameter(description = "News article ID") @PathVariable UUID newsId) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.debug("Admin: Fetching comment count - adminId: {}, newsId: {}", authenticatedAdminId, newsId);

                Long count = commentService.getCommentCountByNewsId(newsId);

                log.debug("Admin: Comment count for news {}: {} - adminId: {}", newsId, count, authenticatedAdminId);
                return ResponseEntity.ok(ApiResponseDto.success("Comment count retrieved", count));
        }

        // ========================================
        // 12. Bookmark Analytics Endpoint
        // ========================================

        /**
         * 12. Get bookmark count by news ID.
         * 
         * @param newsId News article ID
         * @return Total bookmark count
         */
        @GetMapping("/bookmarks/count/news/{newsId}")
        @Operation(summary = "12. Get bookmark count by news ID", description = "Returns the total number of bookmarks for a news article")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Bookmark count retrieved successfully"),
                        @ApiResponse(responseCode = "404", description = "News not found")
        })
        public ResponseEntity<ApiResponseDto<Long>> getBookmarkCountByNewsId(
                        @Parameter(description = "News article ID") @PathVariable UUID newsId) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.debug("Admin: Fetching bookmark count - adminId: {}, newsId: {}", authenticatedAdminId, newsId);

                Long count = bookmarkService.getBookmarkCountByNewsId(newsId);

                log.debug("Admin: Bookmark count for news {}: {} - adminId: {}", newsId, count, authenticatedAdminId);
                return ResponseEntity.ok(ApiResponseDto.success("Bookmark count retrieved", count));
        }
}
