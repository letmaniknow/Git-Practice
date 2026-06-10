package com.mmva.newsapp.controller.admin.news;

import lombok.extern.slf4j.Slf4j;

// ===============================
// OpenAPI/Swagger Imports
// ===============================
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

// ===============================
// Validation Imports
// ===============================
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

// ===============================
// Request Analytics Imports
// ===============================
import com.mmva.newsapp.infrastructure.requestanalytics.dto.RequestClientInfoDto;
import com.mmva.newsapp.infrastructure.requestanalytics.service.RequestInfoService;

// ===============================
// Lombok Imports
// ===============================
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// ===============================
// Spring Framework Imports
// ===============================
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.mmva.newsapp.domain.adminuser.service.validation.AdminValidationService;
import com.mmva.newsapp.domain.news.dto.core.NewsCreateRequestDto;
import com.mmva.newsapp.domain.news.dto.core.NewsCreateResponseDto;
import com.mmva.newsapp.domain.news.dto.core.NewsMediaFileRequestDto;
import com.mmva.newsapp.domain.news.dto.core.NewsMediaUploadResponseDto;
import com.mmva.newsapp.domain.news.dto.core.ThumbnailResponseDto;
import com.mmva.newsapp.domain.news.dto.social.SocialMediaShareDashboardResponseDto;
import com.mmva.newsapp.domain.news.dto.social.SocialMediaShareMarkPlatformSharedRequestDto;
import com.mmva.newsapp.domain.news.audit.model.NewsAuditLog;
import com.mmva.newsapp.domain.news.service.core.NewsService;
import com.mmva.newsapp.domain.news.service.search.AdminNewsSearchService;
import com.mmva.newsapp.domain.news.service.elasticsearch.NewsElasticSearchService;
import com.mmva.newsapp.domain.news.service.social.SocialMediaShareService;
import com.mmva.newsapp.infrastructure.common.api.dto.ApiResponseDto;
import com.mmva.newsapp.infrastructure.common.util.SortUtils;
import com.mmva.newsapp.infrastructure.rbac.permission.core.checker.NewsPermissionChecker;
import com.mmva.newsapp.infrastructure.security.userdetails.AdminUserDetails;
import com.mmva.newsapp.infrastructure.common.exception.InvalidRequestException;

// ===============================
// Java Core Imports
// ===============================
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Admin News Controller - Provides full CRUD access to all newsapp articles
 * regardless of status.
 * <p>
 * This controller is intended for administrative users with elevated
 * privileges.
 * All endpoints require admindashboard validation via adminId parameter.
 * </p>
 * 
 * <p>
 * Endpoints:
 * </p>
 * <ul>
 * <li>1. POST / - Create newsapp newsapp</li>
 * <li>2. GET /{newsId} - Get newsapp by ID</li>
 * <li>3. GET / - Get all newsapp</li>
 * <li>4. PUT /{newsId} - Update newsapp newsapp</li>
 * <li>5. DELETE /{newsId} - Delete newsapp newsapp</li>
 * <li>6. GET /workflow-statuses - Get available workflow status options</li>
 * <li>7. GET /status/{workflowStatus} - Get newsapp by workflow status</li>
 * <li>8. PATCH /{newsId}/workflow - Update workflow status</li>
 * <li>9. GET /search - Search newsapp</li>
 * <li>10. GET /newscategory/{categoryId} - Get newsapp by newscategory</li>
 * <li>11. GET /author/{authorId} - Get newsapp by author</li>
 * <li>12. GET /slug/{slug} - Get newsapp by slug</li>
 * <li>13. POST /draft - Save draft newsapp</li>
 * <li>14. PATCH /{newsId}/archive - Archive newsapp</li>
 * <li>15. PATCH /{newsId}/unarchive - Unarchive newsapp</li>
 * <li>16. PATCH /{newsId}/restore - Restore deleted newsapp</li>
 * <li>17. PATCH /{newsId}/pin - Pin/Unpin newsapp</li>
 * <li>18. PATCH /{newsId}/schedule - Schedule publication</li>
 * <li>19. PATCH /bulk/publish - Bulk publish</li>
 * <li>20. DELETE /bulk - Bulk delete</li>
 * <li>21. GET /{newsId}/audit-logs - Get audit logs</li>
 * <li>22. GET /{newsId}/versions - Get version history</li>
 * <li>23. GET /{newsId}/statistics - Get newsapp statistics</li>
 * <li>24. GET /export - Export newsapp</li>
 * <li>25. GET /media/{filename} - Get media file</li>
 * <li>26. GET /{newsId}/related - Get related newsapp</li>
 * <li>27. GET /trending - Get trending newsapp</li>
 * <li>28. GET /social-sharing/dashboard - Get social sharing dashboard</li>
 * <li>29. POST /social-sharing/mark-completed - Mark social shares
 * completed</li>
 * </ul>
 * 
 * @author MMVA Team
 * @version 1.0
 * @since 2024-06-01
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/admin/news")
@Tag(name = "Admin - News Management", description = "Admin APIs for full news management including draft, published, and archived articles")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = { "Range", "Accept", "Content-Type" }, exposedHeaders = { "Content-Length",
                "Content-Range", "Accept-Ranges" })
public class AdminNewsController {

        // ===============================
        // Constants
        // ===============================
        // Use Java property name (createdAt), not DB column name (created_at)
        private static final String DEFAULT_SORT = "createdAt,desc";
        private static final String DEFAULT_PAGE = "0";
        private static final String DEFAULT_SIZE = "10";
        private static final int MAX_PAGE_SIZE = 100;

        // ===============================
        // Dependencies
        // ===============================
        private final AdminValidationService adminValidationService;
        private final NewsService newsService;
        private final AdminNewsSearchService adminNewsSearchService;
        private final SocialMediaShareService socialSharingService;
        private final NewsPermissionChecker newsPermissionChecker;
        private final RequestInfoService requestInfoService;
        private final Optional<NewsElasticSearchService> newsElasticSearchService; // Elasticsearch reindexing
                                                                                   // (optional)

        // ===============================
        // Endpoint 1: Create News Article
        // ===============================

        /**
         * Creates a new newsapp newsapp with associated media files.
         *
         * @param newsRequest the newsapp creation request containing title, content,
         *                    and
         *                    media
         * @param adminId     the UUID of the admindashboard performing the action
         * @return the created newsapp newsapp response
         */
        @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        @PreAuthorize("hasAuthority('news.create')")
        @Operation(summary = "1. Create newsapp newsapp", description = "Creates a new newsapp newsapp with media files")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "News newsapp created successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid request data"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid admindashboard credentials"),
                        @ApiResponse(responseCode = "409", description = "Duplicate newsapp newsapp (slug conflict)")
        })
        public ResponseEntity<ApiResponseDto<NewsCreateResponseDto>> createNews(
                        @Valid @ModelAttribute("news") NewsCreateRequestDto newsRequest) {

                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                newsRequest.setCreatedBy(authenticatedAdminId);
                log.info("Admin: Creating newsapp newsapp with title: {} by admindashboard: {}",
                                newsRequest.getNewsTitleEn(),
                                authenticatedAdminId);

                NewsCreateResponseDto response = newsService.createNews(newsRequest);

                log.info("Admin: News newsapp created successfully with ID: {}", response.getNewsNewsId());
                return ResponseEntity.ok(ApiResponseDto.success("News created successfully", response));
        }

        // ===============================
        // Endpoint 2: Get News by ID
        // ===============================

        /**
         * Retrieves a newsapp newsapp by its unique identifier.
         *
         * @param newsId  the newsapp newsapp ID
         * @param adminId the UUID of the admindashboard performing the action
         * @return the newsapp newsapp response
         */
        @GetMapping("/{newsId}")
        @Operation(summary = "2. Get newsapp by ID", description = "Retrieves any newsapp newsapp by ID regardless of status")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "News newsapp retrieved successfully"),
                        @ApiResponse(responseCode = "404", description = "News newsapp not found")
        })
        public ResponseEntity<ApiResponseDto<NewsCreateResponseDto>> getNewsById(
                        @Parameter(description = "News newsapp ID", required = true) @PathVariable String newsId) {

                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.debug("Admin: Fetching newsapp by ID: {} for admindashboard: {}", newsId, authenticatedAdminId);

                NewsCreateResponseDto response = newsService.getNewsById(newsId);

                log.debug("Admin: Retrieved newsapp newsapp: {}", newsId);
                return ResponseEntity.ok(ApiResponseDto.success("News fetched successfully", response));
        }

        // ===============================
        // Endpoint 3: Get All News
        // ===============================

        /**
         * Retrieves all newsapp articles with pagination support.
         *
         * @param page    the page number (0-indexed)
         * @param size    the page size
         * @param sort    the sort order (e.g., "createdAt,desc")
         * @param adminId the UUID of the admindashboard performing the action
         * @return paginated list of newsapp articles
         */
        @GetMapping
        @Operation(summary = "3. Get all newsapp", description = "Retrieves all newsapp articles regardless of status with pagination")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "News articles retrieved successfully")
        })
        public ResponseEntity<ApiResponseDto<Page<NewsCreateResponseDto>>> getAllNews(
                        @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = DEFAULT_PAGE) @Min(0) int page,
                        @Parameter(description = "Page size") @RequestParam(defaultValue = DEFAULT_SIZE) @Min(1) @Max(MAX_PAGE_SIZE) int size,
                        @Parameter(description = "Sort order (e.g., createdAt,desc)") @RequestParam(defaultValue = DEFAULT_SORT) String sort) {

                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.debug("Admin: Fetching all newsapp - page: {}, size: {}, sort: {} for admindashboard: {}", page,
                                size, sort,
                                authenticatedAdminId);

                Pageable pageable = createPageable(page, size, sort);
                Page<NewsCreateResponseDto> newsPage = newsService.getAllNews(pageable);

                log.debug("Admin: Retrieved {} newsapp articles", newsPage.getTotalElements());
                return ResponseEntity.ok(ApiResponseDto.success("All newsapp fetched successfully", newsPage));
        }

        // ===============================
        // Endpoint 4: Update News Article
        // ===============================

        /**
         * Updates an existing newsapp newsapp.
         *
         * @param newsId     the newsapp newsapp ID to update
         * @param requestDto the updated newsapp data
         * @param adminId    the UUID of the admindashboard performing the action
         * @return the updated newsapp newsapp response
         */
        @PutMapping(value = "/{newsId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        @PreAuthorize("hasAuthority('news.update')")
        @Operation(summary = "4. Update newsapp newsapp", description = "Updates an existing newsapp newsapp")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "News newsapp updated successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid request data"),
                        @ApiResponse(responseCode = "404", description = "News newsapp not found")
        })
        public ResponseEntity<ApiResponseDto<NewsCreateResponseDto>> updateNews(
                        @Parameter(description = "News newsapp ID", required = true) @PathVariable String newsId,
                        @Valid @ModelAttribute("news") NewsCreateRequestDto requestDto) {

                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                // AUDIT SEMANTICS: For UPDATE operations, we reuse the 'createdBy' DTO field to
                // pass the updater ID
                // to the service. The service interprets this as updatedBy and preserves the
                // original createdBy unchanged.
                // This maintains immutable creator tracking while capturing the last updater.
                requestDto.setCreatedBy(authenticatedAdminId);
                log.info("Admin: Updating newsapp newsapp: {} by admindashboard: {}", newsId, authenticatedAdminId);

                NewsCreateResponseDto response = newsService.updateNews(newsId, requestDto);

                log.info("Admin: News newsapp updated successfully: {}", newsId);
                return ResponseEntity.ok(ApiResponseDto.success("News updated successfully", response));
        }

        // ===============================
        // Endpoint 5: Delete News Article (Soft Delete)
        // ===============================

        /**
         * Soft deletes a newsapp newsapp by its ID.
         * Files are preserved for potential restore.
         *
         * @param newsId the newsapp newsapp ID to delete
         * @return success response with no data
         */
        @DeleteMapping("/{newsId}/softdelete")
        @PreAuthorize("hasAuthority('news.delete')")
        @Operation(summary = "5. Soft delete newsapp newsapp", description = "Soft deletes a newsapp newsapp (recoverable - files preserved). Use permanent delete for irreversible deletion.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "News newsapp soft deleted successfully"),
                        @ApiResponse(responseCode = "404", description = "News newsapp not found")
        })
        public ResponseEntity<ApiResponseDto<Void>> softDeleteNews(
                        @Parameter(description = "News newsapp ID", required = true) @PathVariable String newsId) {

                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.info("Admin: Soft deleting newsapp newsapp: {} by admindashboard: {}", newsId,
                                authenticatedAdminId);

                newsService.softDeleteNews(newsId, authenticatedAdminId);

                log.info("Admin: News newsapp soft deleted successfully: {}", newsId);
                return ResponseEntity
                                .ok(ApiResponseDto.success("News soft deleted successfully (can be restored)", null));
        }

        // ===============================
        // Endpoint 5b: Permanent Delete News Article
        // ===============================

        /**
         * Permanently deletes a newsapp newsapp by its ID.
         * Media and thumbnail files are moved to backup folder.
         * Database record is hard deleted and cannot be restored.
         *
         * @param newsId the newsapp newsapp ID to permanently delete
         * @return success response with no data
         */
        @DeleteMapping("/{newsId}/permanentdelete")
        @PreAuthorize("hasAuthority('news.delete')")
        @Operation(summary = "5b. Permanently delete newsapp newsapp (IRREVERSIBLE)", description = "Permanently deletes a newsapp newsapp from the database. Files are moved to backup. This action cannot be undone.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "News newsapp permanently deleted (IRREVERSIBLE)"),
                        @ApiResponse(responseCode = "404", description = "News newsapp not found")
        })
        public ResponseEntity<ApiResponseDto<Void>> permanentDeleteNews(
                        @Parameter(description = "News newsapp ID", required = true) @PathVariable String newsId) {

                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.info("Admin: Permanently deleting newsapp newsapp: {} by admindashboard: {}", newsId,
                                authenticatedAdminId);

                newsService.permanentDelete(newsId);

                log.info("Admin: News newsapp permanently deleted: {}. Files moved to backup.", newsId);
                return ResponseEntity
                                .ok(ApiResponseDto.success("News permanently deleted. Files moved to backup.", null));
        }

        // =========================
        // Workflow Status Management
        // =========================

        // ===============================
        // Endpoint 6: Get Available Workflow Status Options
        // ===============================

        /**
         * Retrieves all available workflow status options.
         * <p>
         * This endpoint returns all possible values for the WorkflowStatus enum,
         * allowing clients (like Angular SPA) to dynamically populate dropdowns
         * and filtering options in the UI.
         * </p>
         * <p>
         * <strong>Why This Endpoint?</strong>
         * <ul>
         * <li>Single Source of Truth - Backend enum is the authority</li>
         * <li>No Frontend Duplication - No hardcoded arrays in Angular</li>
         * <li>Future-Proof - Add new statuses without frontend changes</li>
         * <li>Industry Standard - Follows REST best practices</li>
         * </ul>
         * </p>
         *
         * @return list of all available workflow status values
         */
        @GetMapping("/workflow-statuses")
        @Operation(summary = "6. Get available workflow status options", description = "Returns all available workflow status values from the backend enum. Use this to populate dropdowns in your UI.", tags = {
                        "Workflow Status", "Lookup Data" })
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Workflow status options retrieved successfully"),
                        @ApiResponse(responseCode = "403", description = "Unauthorized - Admin access required")
        })
        public ResponseEntity<ApiResponseDto<List<String>>> getWorkflowStatusOptions() {

                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.debug("Admin: Fetching workflow status options for admin: {}", authenticatedAdminId);

                List<String> statuses = newsService.getAvailableWorkflowStatuses();

                log.debug("Admin: Retrieved {} workflow status options", statuses.size());
                return ResponseEntity.ok(ApiResponseDto.success(
                                "Workflow status options retrieved successfully",
                                statuses));
        }

        // ===============================
        // Endpoint 7: Get News by Workflow Status
        // ===============================

        /**
         * Retrieves newsapp articles filtered by workflow status.
         *
         * @param workflowStatus the workflow status to filter by (e.g., DRAFT,
         *                       PUBLISHED, ARCHIVED)
         * @param page           the page number
         * @param size           the page size
         * @param sort           the sort order
         * @param adminId        the UUID of the admindashboard performing the action
         * @return paginated list of newsapp articles with the specified status
         */
        @GetMapping("/status/{workflowStatus}")
        @Operation(summary = "6. Get newsapp by workflow status", description = "Retrieves newsapp articles by specific workflow status")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "News articles retrieved successfully")
        })
        public ResponseEntity<ApiResponseDto<Page<NewsCreateResponseDto>>> getNewsByWorkflowStatus(
                        @Parameter(description = "Workflow status (DRAFT, SUBMITTED, REVIEWED, APPROVED, PUBLISHED, ARCHIVED)") @PathVariable String workflowStatus,
                        @Parameter(description = "Page number") @RequestParam(defaultValue = DEFAULT_PAGE) @Min(0) int page,
                        @Parameter(description = "Page size") @RequestParam(defaultValue = DEFAULT_SIZE) @Min(1) @Max(MAX_PAGE_SIZE) int size,
                        @Parameter(description = "Sort order") @RequestParam(defaultValue = DEFAULT_SORT) String sort) {

                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.debug("Admin: Fetching newsapp by status: {} - page: {}, size: {} for admindashboard: {}",
                                workflowStatus, page,
                                size, authenticatedAdminId);

                Pageable pageable = createPageable(page, size, sort);
                Page<NewsCreateResponseDto> newsPage = newsService.getNewsByWorkflowStatus(workflowStatus, pageable);

                log.debug("Admin: Retrieved {} newsapp articles with status: {}", newsPage.getTotalElements(),
                                workflowStatus);
                return ResponseEntity.ok(ApiResponseDto.success("News by status fetched successfully", newsPage));
        }

        // ===============================
        // Endpoint 8: Update Workflow Status
        // ===============================

        /**
         * Updates the workflow status of a newsapp newsapp.
         *
         * @param newsId    the newsapp newsapp ID
         * @param newStatus the new workflow status
         * @param adminId   the UUID of the admindashboard performing the action
         * @return success response
         */
        @PatchMapping("/{newsId}/workflow")
        @PreAuthorize("hasAuthority('news.publish')")
        @Operation(summary = "7. Update workflow status", description = "Transitions newsapp newsapp to a new workflow status")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Workflow status updated successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid status transition"),
                        @ApiResponse(responseCode = "403", description = "Insufficient permissions for status change"),
                        @ApiResponse(responseCode = "404", description = "News newsapp not found")
        })
        public ResponseEntity<ApiResponseDto<Void>> updateWorkflowStatus(
                        @Parameter(description = "News newsapp ID", required = true) @PathVariable String newsId,
                        @Parameter(description = "New workflow status", required = true) @RequestParam String newStatus) {

                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.info("Admin: Transitioning newsapp: {} to status: {} by admindashboard: {}", newsId, newStatus,
                                authenticatedAdminId);

                newsService.updateWorkflowStatus(newsId, newStatus, authenticatedAdminId.toString());

                log.info("Admin: Workflow status updated successfully for newsapp: {} to: {}", newsId, newStatus);
                return ResponseEntity.ok(ApiResponseDto.success("Workflow status updated successfully", null));
        }

        // ===============================
        // Endpoint 9: Search News
        // ===============================

        /**
         * Searches newsapp articles with various filter criteria.
         *
         * @param query          text search query
         * @param newsCategoryId filter by newscategory ID
         * @param fromDate       filter from date (ISO 8601)
         * @param toDate         filter to date (ISO 8601)
         * @param sort           sort order
         * @param page           page number
         * @param size           page size
         * @param adminId        admindashboard UUID
         * @return paginated search results
         */
        @GetMapping("/search")
        @Operation(summary = "8. Search newsapp", description = "Search newsapp articles with filters across all statuses")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Search completed successfully")
        })
        public ResponseEntity<ApiResponseDto<Page<NewsCreateResponseDto>>> searchNews(
                        @Parameter(description = "Text search query") @RequestParam(required = false) String query,
                        @Parameter(description = "Category ID filter") @RequestParam(required = false) String newsCategoryId,
                        @Parameter(description = "From date (ISO 8601)") @RequestParam(required = false) String fromDate,
                        @Parameter(description = "To date (ISO 8601)") @RequestParam(required = false) String toDate,
                        @Parameter(description = "Sort order") @RequestParam(defaultValue = DEFAULT_SORT) String sort,
                        @Parameter(description = "Page number") @RequestParam(defaultValue = DEFAULT_PAGE) @Min(0) int page,
                        @Parameter(description = "Page size") @RequestParam(defaultValue = DEFAULT_SIZE) @Min(1) @Max(MAX_PAGE_SIZE) int size) {

                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.debug("Admin: Searching newsapp - query: {}, categoryId: {} by admindashboard: {}", query,
                                newsCategoryId,
                                authenticatedAdminId);

                Pageable pageable = createPageable(page, size, sort);
                Page<NewsCreateResponseDto> searchResults = newsService.searchNews(query, newsCategoryId, fromDate,
                                toDate,
                                sort, pageable);

                log.debug("Admin: Search returned {} results", searchResults.getTotalElements());
                return ResponseEntity.ok(ApiResponseDto.success("Search completed successfully", searchResults));
        }

        // ===============================
        // Endpoint 9: Advanced Search with Status Filtering
        // ===============================

        /**
         * Advanced search with workflow status filtering.
         *
         * <p>
         * Primary search endpoint for admin dashboards supporting:
         * <ul>
         * <li>Full-text search across title and content</li>
         * <li>Multiple workflow status filtering (e.g., PUBLISHED, SCHEDULED)</li>
         * <li>Category filtering</li>
         * <li>Date range filtering</li>
         * <li>Pagination and sorting</li>
         * </ul>
         * </p>
         *
         * @param request the search parameters (query, statuses, categoryId, date
         *                range)
         * @param page    page number (0-based)
         * @param size    page size (max 100)
         * @param sort    sort order (e.g., "createdAt,desc")
         * @return paginated search results
         */
        @GetMapping("/search-advanced")
        @Operation(summary = "9. Advanced search with status filtering", description = "Search with multi-status workflow filtering, full-text query, category, and date range")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Advanced search completed successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid search parameters")
        })
        public ResponseEntity<ApiResponseDto<Page<NewsCreateResponseDto>>> advancedSearch(
                        @Parameter(description = "Text search query") @RequestParam(required = false) String query,
                        @Parameter(description = "Workflow statuses to filter by (comma-separated)") @RequestParam(required = false) List<String> workflowStatuses,
                        @Parameter(description = "Category ID filter") @RequestParam(required = false) String categoryId,
                        @Parameter(description = "From date (ISO 8601)") @RequestParam(required = false) String fromDate,
                        @Parameter(description = "To date (ISO 8601)") @RequestParam(required = false) String toDate,
                        @Parameter(description = "Page number") @RequestParam(defaultValue = DEFAULT_PAGE) @Min(0) int page,
                        @Parameter(description = "Page size") @RequestParam(defaultValue = DEFAULT_SIZE) @Min(1) @Max(MAX_PAGE_SIZE) int size,
                        @Parameter(description = "Sort order") @RequestParam(defaultValue = DEFAULT_SORT) String sort) {

                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.debug("Admin: Advanced search - query: {}, statuses: {}, categoryId: {}, from: {}, to: {} by admin: {}",
                                query, workflowStatuses, categoryId, fromDate, toDate, authenticatedAdminId);

                // Parse date strings to LocalDate
                java.time.LocalDate parsedFromDate = null;
                java.time.LocalDate parsedToDate = null;

                if (fromDate != null && !fromDate.isBlank()) {
                        try {
                                parsedFromDate = java.time.LocalDate.parse(fromDate);
                        } catch (java.time.format.DateTimeParseException e) {
                                log.warn("Invalid fromDate format: {}", fromDate);
                                return ResponseEntity.badRequest()
                                                .body(ApiResponseDto.error(
                                                                "Invalid fromDate format. Use ISO 8601 (YYYY-MM-DD)"));
                        }
                }

                if (toDate != null && !toDate.isBlank()) {
                        try {
                                parsedToDate = java.time.LocalDate.parse(toDate);
                        } catch (java.time.format.DateTimeParseException e) {
                                log.warn("Invalid toDate format: {}", toDate);
                                return ResponseEntity.badRequest()
                                                .body(ApiResponseDto.error(
                                                                "Invalid toDate format. Use ISO 8601 (YYYY-MM-DD)"));
                        }
                }

                // Validate date range
                if (parsedFromDate != null && parsedToDate != null && parsedFromDate.isAfter(parsedToDate)) {
                        log.warn("Invalid date range: fromDate ({}) is after toDate ({})", parsedFromDate,
                                        parsedToDate);
                        return ResponseEntity.badRequest()
                                        .body(ApiResponseDto.error("fromDate must not be after toDate"));
                }

                Pageable pageable = createPageable(page, size, sort);

                // Execute advanced search using workflow status filtering
                Page<NewsCreateResponseDto> searchResults = adminNewsSearchService.searchWithStatusFilter(
                                query,
                                workflowStatuses,
                                categoryId,
                                parsedFromDate,
                                parsedToDate,
                                pageable);

                log.debug("Admin: Advanced search returned {} results", searchResults.getTotalElements());
                return ResponseEntity
                                .ok(ApiResponseDto.success("Advanced search completed successfully", searchResults));
        }

        // ===============================
        // Endpoint 10: Multi-Field Search
        // ===============================

        /**
         * Multi-field search across multiple content fields.
         *
         * <p>
         * Searches across multiple fields simultaneously:
         * <ul>
         * <li>newsSlug - article URL slug</li>
         * <li>newsTitleEn/newsTitleEs - article titles</li>
         * <li>newsContentEn/newsContentEs - article content</li>
         * <li>newsKeywords - SEO keywords</li>
         * <li>newsMetaDescription - SEO meta description</li>
         * </ul>
         * </p>
         *
         * <p>
         * <strong>Search Strategy:</strong> Uses ES-first/DB-fallback architecture:
         * <ol>
         * <li>Attempts Elasticsearch for fast full-text search (if configured)</li>
         * <li>Falls back to database LIKE search if ES unavailable</li>
         * <li>Combines results with workflow status, category, and date filters</li>
         * </ol>
         * </p>
         *
         * @param query            optional text search query (searches multiple fields)
         * @param workflowStatuses optional workflow statuses to filter by
         *                         (comma-separated)
         * @param categoryId       optional category ID to filter by
         * @param fromDate         optional start date (ISO 8601)
         * @param toDate           optional end date (ISO 8601)
         * @param page             page number (0-based)
         * @param size             page size (max 100)
         * @param sort             sort order
         * @return paginated search results across all searchable fields
         */
        @GetMapping("/search-multi-field")
        @Operation(summary = "10. Multi-field search", description = "Search across multiple content fields (title, content, keywords, slug, metadata) with status/category/date filtering")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Multi-field search completed successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid search parameters")
        })
        public ResponseEntity<ApiResponseDto<Page<NewsCreateResponseDto>>> multiFieldSearch(
                        @Parameter(description = "Text search query") @RequestParam(required = false) String query,
                        @Parameter(description = "Workflow statuses to filter by (comma-separated)") @RequestParam(required = false) List<String> workflowStatuses,
                        @Parameter(description = "Category ID filter") @RequestParam(required = false) String categoryId,
                        @Parameter(description = "Creator admin user UUID filter") @RequestParam(required = false) String createdBy,
                        @Parameter(description = "From date (ISO 8601)") @RequestParam(required = false) String fromDate,
                        @Parameter(description = "To date (ISO 8601)") @RequestParam(required = false) String toDate,
                        @Parameter(description = "Page number") @RequestParam(defaultValue = DEFAULT_PAGE) @Min(0) int page,
                        @Parameter(description = "Page size") @RequestParam(defaultValue = DEFAULT_SIZE) @Min(1) @Max(MAX_PAGE_SIZE) int size,
                        @Parameter(description = "Sort order") @RequestParam(defaultValue = DEFAULT_SORT) String sort) {

                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.debug("Admin: Multi-field search - query: {}, statuses: {}, categoryId: {}, createdBy: {}, from: {}, to: {} by admin: {}",
                                query, workflowStatuses, categoryId, createdBy, fromDate, toDate, authenticatedAdminId);

                // Parse date strings to LocalDate
                java.time.LocalDate parsedFromDate = null;
                java.time.LocalDate parsedToDate = null;

                if (fromDate != null && !fromDate.isBlank()) {
                        try {
                                parsedFromDate = java.time.LocalDate.parse(fromDate);
                        } catch (java.time.format.DateTimeParseException e) {
                                log.warn("Invalid fromDate format: {}", fromDate);
                                return ResponseEntity.badRequest()
                                                .body(ApiResponseDto.error(
                                                                "Invalid fromDate format. Use ISO 8601 (YYYY-MM-DD)"));
                        }
                }

                if (toDate != null && !toDate.isBlank()) {
                        try {
                                parsedToDate = java.time.LocalDate.parse(toDate);
                        } catch (java.time.format.DateTimeParseException e) {
                                log.warn("Invalid toDate format: {}", toDate);
                                return ResponseEntity.badRequest()
                                                .body(ApiResponseDto.error(
                                                                "Invalid toDate format. Use ISO 8601 (YYYY-MM-DD)"));
                        }
                }

                // Validate date range
                if (parsedFromDate != null && parsedToDate != null && parsedFromDate.isAfter(parsedToDate)) {
                        log.warn("Invalid date range: fromDate ({}) is after toDate ({})", parsedFromDate,
                                        parsedToDate);
                        return ResponseEntity.badRequest()
                                        .body(ApiResponseDto.error("fromDate must not be after toDate"));
                }

                Pageable pageable = createPageable(page, size, sort);

                // Execute multi-field search
                Page<NewsCreateResponseDto> searchResults = adminNewsSearchService.searchWithMultipleFields(
                                query,
                                workflowStatuses,
                                categoryId,
                                createdBy,
                                parsedFromDate,
                                parsedToDate,
                                pageable);

                log.debug("Admin: Multi-field search returned {} results", searchResults.getTotalElements());
                return ResponseEntity
                                .ok(ApiResponseDto.success("Multi-field search completed successfully", searchResults));
        }

        // ===============================
        // Endpoint 11: Search by Title Only
        // ===============================

        /**
         * Search articles by title only (exact article lookup).
         *
         * <p>
         * Searches ONLY title fields for precise article matching.
         * Admin can find ONE specific article to edit/delete by title.
         * Combines with optional workflow status, category, and date filters.
         * </p>
         *
         * @param query            text search query (title match)
         * @param workflowStatuses optional workflow statuses (comma-separated)
         * @param categoryId       optional category ID filter
         * @param fromDate         optional start date (ISO 8601)
         * @param toDate           optional end date (ISO 8601)
         * @param page             page number (0-based)
         * @param size             page size (max 100)
         * @param sort             sort order
         * @return paginated results matching title query
         */
        @GetMapping("/search-by-title")
        @Operation(summary = "11. Search by title only", description = "Search articles by exact/partial title match for precise article lookup")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Title search completed successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid search parameters")
        })
        public ResponseEntity<ApiResponseDto<Page<NewsCreateResponseDto>>> searchByTitle(
                        @Parameter(description = "Text search query (title match)") @RequestParam(required = false) String query,
                        @Parameter(description = "Workflow statuses (comma-separated)") @RequestParam(required = false) List<String> workflowStatuses,
                        @Parameter(description = "Category ID filter") @RequestParam(required = false) String categoryId,
                        @Parameter(description = "Creator admin user UUID filter") @RequestParam(required = false) String createdBy,
                        @Parameter(description = "From date (ISO 8601)") @RequestParam(required = false) String fromDate,
                        @Parameter(description = "To date (ISO 8601)") @RequestParam(required = false) String toDate,
                        @Parameter(description = "Page number") @RequestParam(defaultValue = DEFAULT_PAGE) @Min(0) int page,
                        @Parameter(description = "Page size") @RequestParam(defaultValue = DEFAULT_SIZE) @Min(1) @Max(MAX_PAGE_SIZE) int size,
                        @Parameter(description = "Sort order") @RequestParam(defaultValue = DEFAULT_SORT) String sort) {

                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.debug("Admin: Title-only search - query: {}, statuses: {}, categoryId: {}, createdBy: {}, from: {}, to: {} by admin: {}",
                                query, workflowStatuses, categoryId, createdBy, fromDate, toDate, authenticatedAdminId);

                // Parse date strings to LocalDate
                java.time.LocalDate parsedFromDate = null;
                java.time.LocalDate parsedToDate = null;

                if (fromDate != null && !fromDate.isBlank()) {
                        try {
                                parsedFromDate = java.time.LocalDate.parse(fromDate);
                        } catch (java.time.format.DateTimeParseException e) {
                                log.warn("Invalid fromDate format: {}", fromDate);
                                return ResponseEntity.badRequest()
                                                .body(ApiResponseDto.error(
                                                                "Invalid fromDate format. Use ISO 8601 (YYYY-MM-DD)"));
                        }
                }

                if (toDate != null && !toDate.isBlank()) {
                        try {
                                parsedToDate = java.time.LocalDate.parse(toDate);
                        } catch (java.time.format.DateTimeParseException e) {
                                log.warn("Invalid toDate format: {}", toDate);
                                return ResponseEntity.badRequest()
                                                .body(ApiResponseDto.error(
                                                                "Invalid toDate format. Use ISO 8601 (YYYY-MM-DD)"));
                        }
                }

                // Validate date range
                if (parsedFromDate != null && parsedToDate != null && parsedFromDate.isAfter(parsedToDate)) {
                        log.warn("Invalid date range: fromDate ({}) is after toDate ({})", parsedFromDate,
                                        parsedToDate);
                        return ResponseEntity.badRequest()
                                        .body(ApiResponseDto.error("fromDate must not be after toDate"));
                }

                Pageable pageable = createPageable(page, size, sort);

                // Execute title-only search
                Page<NewsCreateResponseDto> searchResults = adminNewsSearchService.searchByTitleOnly(
                                query,
                                workflowStatuses,
                                categoryId,
                                createdBy,
                                parsedFromDate,
                                parsedToDate,
                                pageable);

                log.debug("Admin: Title-only search returned {} results", searchResults.getTotalElements());
                return ResponseEntity
                                .ok(ApiResponseDto.success("Title search completed successfully", searchResults));
        }

        // ===============================
        // Endpoint 12: Search by Content Fields Only
        // ===============================

        /**
         * Search articles by content/metadata only (topic discovery).
         *
         * <p>
         * Searches content, metadata, keywords, tags (everything EXCEPT title).
         * Admin can find related articles by topic without exact title matches.
         * </p>
         *
         * @param query            text search query (content/topic match)
         * @param workflowStatuses optional workflow statuses (comma-separated)
         * @param categoryId       optional category ID filter
         * @param fromDate         optional start date (ISO 8601)
         * @param toDate           optional end date (ISO 8601)
         * @param page             page number (0-based)
         * @param size             page size (max 100)
         * @param sort             sort order
         * @return paginated results matching content query
         */
        @GetMapping("/search-by-content")
        @Operation(summary = "12. Search by content only", description = "Search articles by content/metadata for topic discovery and related article finding")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Content search completed successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid search parameters")
        })
        public ResponseEntity<ApiResponseDto<Page<NewsCreateResponseDto>>> searchByContent(
                        @Parameter(description = "Text search query (content/topic match)") @RequestParam(required = false) String query,
                        @Parameter(description = "Workflow statuses (comma-separated)") @RequestParam(required = false) List<String> workflowStatuses,
                        @Parameter(description = "Category ID filter") @RequestParam(required = false) String categoryId,
                        @Parameter(description = "Creator admin user UUID filter") @RequestParam(required = false) String createdBy,
                        @Parameter(description = "From date (ISO 8601)") @RequestParam(required = false) String fromDate,
                        @Parameter(description = "To date (ISO 8601)") @RequestParam(required = false) String toDate,
                        @Parameter(description = "Page number") @RequestParam(defaultValue = DEFAULT_PAGE) @Min(0) int page,
                        @Parameter(description = "Page size") @RequestParam(defaultValue = DEFAULT_SIZE) @Min(1) @Max(MAX_PAGE_SIZE) int size,
                        @Parameter(description = "Sort order") @RequestParam(defaultValue = DEFAULT_SORT) String sort) {

                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.debug("Admin: Content-only search - query: {}, statuses: {}, categoryId: {}, createdBy: {}, from: {}, to: {} by admin: {}",
                                query, workflowStatuses, categoryId, createdBy, fromDate, toDate, authenticatedAdminId);

                // Parse date strings to LocalDate
                java.time.LocalDate parsedFromDate = null;
                java.time.LocalDate parsedToDate = null;

                if (fromDate != null && !fromDate.isBlank()) {
                        try {
                                parsedFromDate = java.time.LocalDate.parse(fromDate);
                        } catch (java.time.format.DateTimeParseException e) {
                                log.warn("Invalid fromDate format: {}", fromDate);
                                return ResponseEntity.badRequest()
                                                .body(ApiResponseDto.error(
                                                                "Invalid fromDate format. Use ISO 8601 (YYYY-MM-DD)"));
                        }
                }

                if (toDate != null && !toDate.isBlank()) {
                        try {
                                parsedToDate = java.time.LocalDate.parse(toDate);
                        } catch (java.time.format.DateTimeParseException e) {
                                log.warn("Invalid toDate format: {}", toDate);
                                return ResponseEntity.badRequest()
                                                .body(ApiResponseDto.error(
                                                                "Invalid toDate format. Use ISO 8601 (YYYY-MM-DD)"));
                        }
                }

                // Validate date range
                if (parsedFromDate != null && parsedToDate != null && parsedFromDate.isAfter(parsedToDate)) {
                        log.warn("Invalid date range: fromDate ({}) is after toDate ({})", parsedFromDate,
                                        parsedToDate);
                        return ResponseEntity.badRequest()
                                        .body(ApiResponseDto.error("fromDate must not be after toDate"));
                }

                Pageable pageable = createPageable(page, size, sort);

                // Execute content-only search
                Page<NewsCreateResponseDto> searchResults = adminNewsSearchService.searchByContentFieldsOnly(
                                query,
                                workflowStatuses,
                                categoryId,
                                createdBy,
                                parsedFromDate,
                                parsedToDate,
                                pageable);

                log.debug("Admin: Content-only search returned {} results", searchResults.getTotalElements());
                return ResponseEntity
                                .ok(ApiResponseDto.success("Content search completed successfully", searchResults));
        }

        // ===============================
        // Endpoint 13: Get News by Category
        // ===============================

        /**
         * Retrieves newsapp articles by newscategory.
         *
         * @param newsCategoryId the newscategory ID
         * @param page           the page number
         * @param size           the page size
         * @param sort           the sort order
         * @param adminId        the admindashboard UUID
         * @return paginated list of newsapp articles
         */
        @GetMapping("/newscategory/{newsCategoryId}")
        @Operation(summary = "9. Get newsapp by newscategory", description = "Retrieves all newsapp articles by newscategory ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "News articles retrieved successfully")
        })
        public ResponseEntity<ApiResponseDto<Page<NewsCreateResponseDto>>> getNewsByCategory(
                        @Parameter(description = "Category ID", required = true) @PathVariable String newsCategoryId,
                        @Parameter(description = "Page number") @RequestParam(defaultValue = DEFAULT_PAGE) @Min(0) int page,
                        @Parameter(description = "Page size") @RequestParam(defaultValue = DEFAULT_SIZE) @Min(1) @Max(MAX_PAGE_SIZE) int size,
                        @Parameter(description = "Sort order") @RequestParam(defaultValue = DEFAULT_SORT) String sort) {

                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.debug("Admin: Fetching newsapp by newscategory: {} by admindashboard: {}", newsCategoryId,
                                authenticatedAdminId);

                Pageable pageable = createPageable(page, size, sort);
                Page<NewsCreateResponseDto> newsPage = newsService.getNewsByCategory(newsCategoryId, pageable);

                log.debug("Admin: Retrieved {} newsapp articles for newscategory: {}", newsPage.getTotalElements(),
                                newsCategoryId);
                return ResponseEntity.ok(ApiResponseDto.success("News by newscategory fetched successfully", newsPage));
        }

        // ===============================
        // Endpoint 11: Get News by Author
        // ===============================

        /**
         * Retrieves newsapp articles by author.
         *
         * @param authorId the author ID
         * @param page     the page number
         * @param size     the page size
         * @param sort     the sort order
         * @param adminId  the admindashboard UUID
         * @return paginated list of newsapp articles
         */
        @GetMapping("/author/{authorId}")
        @Operation(summary = "10. Get newsapp by author", description = "Retrieves all newsapp articles by author ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "News articles retrieved successfully")
        })
        public ResponseEntity<ApiResponseDto<Page<NewsCreateResponseDto>>> getNewsByAuthor(
                        @Parameter(description = "Author ID", required = true) @PathVariable String authorId,
                        @Parameter(description = "Page number") @RequestParam(defaultValue = DEFAULT_PAGE) @Min(0) int page,
                        @Parameter(description = "Page size") @RequestParam(defaultValue = DEFAULT_SIZE) @Min(1) @Max(MAX_PAGE_SIZE) int size,
                        @Parameter(description = "Sort order") @RequestParam(defaultValue = DEFAULT_SORT) String sort) {

                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.debug("Admin: Fetching newsapp by author: {} by admindashboard: {}", authorId,
                                authenticatedAdminId);

                Pageable pageable = createPageable(page, size, sort);
                Page<NewsCreateResponseDto> newsPage = newsService.getNewsByAuthor(authorId, pageable);

                log.debug("Admin: Retrieved {} newsapp articles for author: {}", newsPage.getTotalElements(), authorId);
                return ResponseEntity.ok(ApiResponseDto.success("News by author fetched successfully", newsPage));
        }

        // ===============================
        // Endpoint 12: Get News by Slug
        // ===============================

        /**
         * Retrieves a newsapp newsapp by its URL slug.
         *
         * @param slug    the URL-friendly slug
         * @param adminId the admindashboard UUID
         * @return the newsapp newsapp response
         */
        @GetMapping("/slug/{slug}")
        @Operation(summary = "11. Get newsapp by slug", description = "Retrieves a newsapp newsapp by its URL-friendly slug")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "News newsapp retrieved successfully"),
                        @ApiResponse(responseCode = "404", description = "News newsapp not found")
        })
        public ResponseEntity<ApiResponseDto<NewsCreateResponseDto>> getNewsBySlug(
                        @Parameter(description = "News slug", required = true) @PathVariable String slug) {

                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.debug("Admin: Fetching newsapp by slug: {} by admindashboard: {}", slug, authenticatedAdminId);

                NewsCreateResponseDto response = newsService.getNewsBySlug(slug);

                log.debug("Admin: Retrieved newsapp newsapp with slug: {}", slug);
                return ResponseEntity.ok(ApiResponseDto.success("News fetched successfully", response));
        }

        // ===============================
        // Endpoint 13: Save Draft News
        // ===============================

        /**
         * Saves a newsapp newsapp as a draft.
         *
         * @param requestDto the newsapp data
         * @param adminId    the admindashboard UUID
         * @return the created draft response
         */
        @PostMapping(value = "/draft", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        @Operation(summary = "12. Save draft newsapp", description = "Saves a newsapp newsapp as a draft for later editing")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Draft saved successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid request data")
        })
        public ResponseEntity<ApiResponseDto<NewsCreateResponseDto>> saveDraftNews(
                        @Valid @ModelAttribute("news") NewsCreateRequestDto requestDto) {

                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                requestDto.setCreatedBy(authenticatedAdminId);
                log.info("Admin: Saving draft newsapp by admindashboard: {}", authenticatedAdminId);

                NewsCreateResponseDto response = newsService.saveDraftNews(requestDto);

                log.info("Admin: Draft newsapp saved successfully with ID: {}", response.getNewsNewsId());
                return ResponseEntity.ok(ApiResponseDto.success("Draft saved successfully", response));
        }

        // ===============================
        // Endpoint 14: Archive News
        // ===============================

        /**
         * Archives a newsapp newsapp (soft archive).
         *
         * @param newsId  the newsapp newsapp ID
         * @param adminId the admindashboard UUID
         * @return success response
         */
        @PatchMapping("/{newsId}/archive")
        @Operation(summary = "13. Archive newsapp", description = "Archives a newsapp newsapp, removing it from public view")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "News archived successfully"),
                        @ApiResponse(responseCode = "404", description = "News newsapp not found")
        })
        public ResponseEntity<ApiResponseDto<Void>> archiveNews(
                        @Parameter(description = "News newsapp ID", required = true) @PathVariable String newsId) {

                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.info("Admin: Archiving newsapp: {} by admindashboard: {}", newsId, authenticatedAdminId);

                newsService.archiveNews(newsId, authenticatedAdminId);

                log.info("Admin: News archived successfully: {}", newsId);
                return ResponseEntity.ok(ApiResponseDto.success("News archived successfully", null));
        }

        // ===============================
        // Endpoint 15: Unarchive News
        // ===============================

        /**
         * Unarchives a newsapp newsapp.
         *
         * @param newsId  the newsapp newsapp ID
         * @param adminId the admindashboard UUID
         * @return success response
         */
        @PatchMapping("/{newsId}/unarchive")
        @Operation(summary = "14. Unarchive newsapp", description = "Restores an archived newsapp newsapp to published status")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "News unarchived successfully"),
                        @ApiResponse(responseCode = "404", description = "News newsapp not found")
        })
        public ResponseEntity<ApiResponseDto<Void>> unarchiveNews(
                        @Parameter(description = "News newsapp ID", required = true) @PathVariable String newsId) {

                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.info("Admin: Unarchiving newsapp: {} by admindashboard: {}", newsId, authenticatedAdminId);

                newsService.unarchiveNews(newsId, authenticatedAdminId);

                log.info("Admin: News unarchived successfully: {}", newsId);
                return ResponseEntity.ok(ApiResponseDto.success("News unarchived successfully", null));
        }

        // ===============================
        // Endpoint 16: Restore Deleted News
        // ===============================

        /**
         * Restores a soft-deleted newsapp newsapp.
         *
         * @param newsId  the newsapp newsapp ID
         * @param adminId the admindashboard UUID
         * @return success response
         */
        @PatchMapping("/{newsId}/restore")
        @Operation(summary = "15. Restore deleted newsapp", description = "Restores a soft-deleted newsapp newsapp")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "News restored successfully"),
                        @ApiResponse(responseCode = "404", description = "News newsapp not found")
        })
        public ResponseEntity<ApiResponseDto<Void>> restoreNews(
                        @Parameter(description = "News newsapp ID", required = true) @PathVariable String newsId) {

                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.info("Admin: Restoring deleted newsapp: {} by admindashboard: {}", newsId, authenticatedAdminId);

                newsService.restoreNews(newsId);

                log.info("Admin: News restored successfully: {}", newsId);
                return ResponseEntity.ok(ApiResponseDto.success("News restored successfully", null));
        }

        // ===============================
        // Endpoint 17: Pin/Unpin News
        // ===============================

        /**
         * Pins or unpins a newsapp newsapp as featured.
         *
         * @param newsId   the newsapp newsapp ID
         * @param featured the featured status
         * @param adminId  the admindashboard UUID
         * @return success response
         */
        @PatchMapping("/{newsId}/pin")
        @Operation(summary = "16. Pin/Unpin newsapp", description = "Marks or unmarks a newsapp newsapp as featured")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "News pin status updated successfully"),
                        @ApiResponse(responseCode = "404", description = "News newsapp not found")
        })
        public ResponseEntity<ApiResponseDto<Void>> pinNews(
                        @Parameter(description = "News newsapp ID", required = true) @PathVariable String newsId,
                        @Parameter(description = "Featured status") @RequestParam(defaultValue = "true") boolean featured) {

                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.info("Admin: {} newsapp: {} by admindashboard: {}", featured ? "Pinning" : "Unpinning", newsId,
                                authenticatedAdminId);

                newsService.pinNews(newsId, featured);
                String message = featured ? "News pinned successfully" : "News unpinned successfully";

                log.info("Admin: News {} successfully: {}", featured ? "pinned" : "unpinned", newsId);
                return ResponseEntity.ok(ApiResponseDto.success(message, null));
        }

        // ===============================
        // Endpoint 18: Schedule Publication
        // ===============================

        /**
         * Schedules a newsapp newsapp for future publication.
         *
         * @param newsId          the newsapp newsapp ID
         * @param publishDateTime the scheduled publish date/time (ISO 8601)
         * @param adminId         the admindashboard UUID
         * @return success response
         */
        @PatchMapping("/{newsId}/schedule")
        @PreAuthorize("hasAuthority('news.publish')")
        @Operation(summary = "17. Schedule publication", description = "Schedules a newsapp newsapp for future publication")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "News scheduled successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid date/time format"),
                        @ApiResponse(responseCode = "404", description = "News newsapp not found")
        })
        public ResponseEntity<ApiResponseDto<NewsCreateResponseDto>> schedulePublish(
                        @Parameter(description = "News newsapp ID", required = true) @PathVariable String newsId,
                        @Parameter(description = "Scheduled publish date/time (ISO 8601)", required = true) @RequestParam String publishDateTime) {

                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.info("Admin: Scheduling newsapp: {} for publication at: {} by admindashboard: {}", newsId,
                                publishDateTime,
                                authenticatedAdminId);

                NewsCreateResponseDto response = newsService.schedulePublish(newsId, publishDateTime,
                                authenticatedAdminId);

                log.info("Admin: News scheduled for publication successfully: {} at: {}", newsId, publishDateTime);
                return ResponseEntity
                                .ok(ApiResponseDto.success("News scheduled for publication successfully", response));
        }

        // ===============================
        // Endpoint 19: Bulk Publish
        // ===============================

        /**
         * Publishes multiple newsapp articles in a single operation.
         *
         * @param ids     the list of newsapp newsapp IDs
         * @param adminId the admindashboard UUID
         * @return success response
         */
        @PatchMapping("/bulk/publish")
        @PreAuthorize("hasAuthority('news.publish')")
        @Operation(summary = "18. Bulk publish", description = "Publishes multiple newsapp articles at once")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "News articles published successfully")
        })
        public ResponseEntity<ApiResponseDto<Void>> bulkPublish(
                        @Parameter(description = "List of newsapp newsapp IDs", required = true) @RequestBody List<String> ids) {

                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.info("Admin: Bulk publishing {} newsapp articles by admindashboard: {}", ids.size(),
                                authenticatedAdminId);

                newsService.bulkPublish(ids);

                log.info("Admin: Bulk publish completed for {} newsapp articles", ids.size());
                return ResponseEntity.ok(ApiResponseDto.success("News articles published successfully", null));
        }

        // ===============================
        // Endpoint 20: Bulk Delete
        // ===============================

        /**
         * Soft-deletes multiple newsapp articles in a single operation.
         *
         * @param ids     the list of newsapp newsapp IDs
         * @param adminId the admindashboard UUID
         * @return success response
         */
        @DeleteMapping("/bulksoftdelete")
        @PreAuthorize("hasAuthority('news.delete')")
        @Operation(summary = "19. Bulk soft delete (recoverable)", description = "Soft-deletes multiple newsapp articles at once. This operation is reversible; use permanentDeleteNews() for irreversible deletion.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "News articles soft deleted successfully (can be restored)")
        })
        public ResponseEntity<ApiResponseDto<Void>> bulkSoftDeleteNews(
                        @Parameter(description = "List of newsapp newsapp IDs", required = true) @RequestBody List<String> ids) {

                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.info("Admin: Bulk soft deleting {} newsapp articles by admindashboard: {}", ids.size(),
                                authenticatedAdminId);

                newsService.bulkSoftDeleteNews(ids, authenticatedAdminId);

                log.info("Admin: Bulk soft delete completed for {} newsapp articles", ids.size());
                return ResponseEntity.ok(ApiResponseDto
                                .success("News articles soft deleted successfully (can be restored)", null));
        }

        // ===============================
        // Endpoint 21: Get Audit Logs
        // ===============================

        /**
         * Retrieves audit logs for a specific newsapp newsapp.
         *
         * @param newsId  the newsapp newsapp ID
         * @param adminId the admindashboard UUID
         * @return list of audit logs
         */
        @GetMapping("/{newsId}/audit-logs")
        @Operation(summary = "20. Get audit logs", description = "Retrieves the audit trail for a newsapp newsapp")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Audit logs retrieved successfully"),
                        @ApiResponse(responseCode = "404", description = "News newsapp not found")
        })
        public ResponseEntity<ApiResponseDto<List<NewsAuditLog>>> getNewsAuditLogs(
                        @Parameter(description = "News newsapp ID", required = true) @PathVariable UUID newsId) {

                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.debug("Admin: Fetching audit logs for newsapp: {} by admindashboard: {}", newsId,
                                authenticatedAdminId);

                List<NewsAuditLog> logs = newsService.findAuditLogsByNewsId(newsId);

                log.debug("Admin: Retrieved {} audit logs for newsapp: {}", logs.size(), newsId);
                return ResponseEntity.ok(ApiResponseDto.success("Audit logs fetched successfully", logs));
        }

        // ===============================
        // Endpoint 22: Get Version History
        // ===============================

        /**
         * Retrieves version history for a newsapp newsapp.
         *
         * @param newsId  the newsapp newsapp ID
         * @param adminId the admindashboard UUID
         * @return list of newsapp versions
         */
        @GetMapping("/{newsId}/versions")
        @Operation(summary = "21. Get version history", description = "Retrieves version history for a newsapp newsapp")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Version history retrieved successfully"),
                        @ApiResponse(responseCode = "404", description = "News newsapp not found")
        })
        public ResponseEntity<ApiResponseDto<List<NewsCreateResponseDto>>> getVersionHistory(
                        @Parameter(description = "News newsapp ID", required = true) @PathVariable String newsId) {

                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.debug("Admin: Fetching version history for newsapp: {} by admindashboard: {}", newsId,
                                authenticatedAdminId);

                List<NewsCreateResponseDto> versions = newsService.getVersionHistory(newsId);

                log.debug("Admin: Retrieved {} versions for newsapp: {}", versions.size(), newsId);
                return ResponseEntity.ok(ApiResponseDto.success("Version history fetched successfully", versions));
        }

        // ===============================
        // Endpoint 23: Get News Statistics
        // ===============================

        /**
         * Retrieves statistics for a specific newsapp newsapp.
         *
         * @param newsId  the newsapp newsapp ID
         * @param adminId the admindashboard UUID
         * @return the newsapp statistics
         */
        @GetMapping("/{newsId}/statistics")
        @Operation(summary = "22. Get newsapp statistics", description = "Retrieves newsengagement statistics for a newsapp newsapp")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
                        @ApiResponse(responseCode = "404", description = "News newsapp not found")
        })
        public ResponseEntity<ApiResponseDto<Object>> getNewsStatistics(
                        @Parameter(description = "News newsapp ID", required = true) @PathVariable String newsId) {

                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.debug("Admin: Fetching statistics for newsapp: {} by admindashboard: {}", newsId,
                                authenticatedAdminId);

                Object stats = newsService.getNewsStatistics(newsId);

                log.debug("Admin: Retrieved statistics for newsapp: {}", newsId);
                return ResponseEntity.ok(ApiResponseDto.success("Statistics fetched successfully", stats));
        }

        // ===============================
        // Endpoint 24: Export News
        // ===============================

        /**
         * Exports newsapp articles in the specified format.
         *
         * @param format  the export format (csv)
         * @param page    the page number
         * @param size    the page size
         * @param adminId the admindashboard UUID
         * @return the exported resource
         */
        @GetMapping("/export")
        @Operation(summary = "23. Export newsapp", description = "Exports newsapp articles in CSV format")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "News exported successfully")
        })
        public ResponseEntity<Resource> exportNews(
                        @Parameter(description = "Export format (currently only 'csv' supported)") @RequestParam(defaultValue = "csv") String format,
                        @Parameter(description = "Page number") @RequestParam(defaultValue = "0") @Min(0) int page,
                        @Parameter(description = "Page size") @RequestParam(defaultValue = "100") @Min(1) @Max(1000) int size) {

                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.info("Admin: Exporting newsapp - format: {}, page: {}, size: {} by admindashboard: {}", format,
                                page, size,
                                authenticatedAdminId);

                Pageable pageable = PageRequest.of(page, size);
                Resource resource = newsService.exportNews(format, pageable);

                log.info("Admin: News export completed - format: {}", format);
                return ResponseEntity.ok()
                                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                                .header(HttpHeaders.CONTENT_DISPOSITION,
                                                "attachment; filename=\"news_export." + format + "\"")
                                .body(resource);
        }

        // ===============================
        // Endpoint 25: Get Media File
        // ===============================

        /**
         * Retrieves a media file by filename.
         *
         * @param filename the media filename
         * @param adminId  the admindashboard UUID
         * @return the media resource
         */
        @GetMapping("/media/{filename}")
        @Operation(summary = "24. Get media file", description = "Retrieves a media file associated with newsapp")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Media file retrieved successfully"),
                        @ApiResponse(responseCode = "404", description = "Media file not found")
        })
        public ResponseEntity<Resource> getMediaFile(
                        @Parameter(description = "Media filename", required = true) @PathVariable String filename) {

                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.debug("Admin: Fetching media file: {} by admindashboard: {}", filename, authenticatedAdminId);

                NewsMediaFileRequestDto mediaFile = newsService.getMediaFile(filename);

                log.debug("Admin: Retrieved media file: {}", filename);
                return ResponseEntity.ok()
                                .contentType(MediaType.parseMediaType(mediaFile.getContentType()))
                                .body(mediaFile.getResource());
        }

        // ===============================
        // Endpoint 25a: Upload Media File
        // ===============================

        /**
         * Uploads a media file and returns its details including accessible URL.
         *
         * @param file the media file to upload
         * @return the upload response with filename, URL, and metadata
         */
        @PostMapping(value = "/media/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        @Operation(summary = "24a. Upload media file", description = "Uploads a media file and returns its accessible URL")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Media file uploaded successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid file or upload failed")
        })
        public ResponseEntity<ApiResponseDto<NewsMediaUploadResponseDto>> uploadMediaFile(
                        @Parameter(description = "Media file to upload", required = true) @RequestParam("file") MultipartFile file) {

                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.info("Admin: Uploading media file: {} by admindashboard: {}", file.getOriginalFilename(),
                                authenticatedAdminId);

                NewsMediaUploadResponseDto response = newsService.uploadMediaFile(file);

                log.info("Admin: Media file uploaded successfully: {}", response.getFilename());
                return ResponseEntity.ok(ApiResponseDto.success("Media file uploaded successfully", response));
        }

        // ===============================
        // Endpoint 25b: Delete Media File
        // ===============================

        /**
         * Deletes a media file by filename.
         *
         * @param filename the media filename to delete
         * @return success response if deleted
         */
        @DeleteMapping("/media/{filename}")
        @Operation(summary = "24b. Delete media file", description = "Deletes a media file by filename")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Media file deleted successfully"),
                        @ApiResponse(responseCode = "404", description = "Media file not found")
        })
        public ResponseEntity<ApiResponseDto<Boolean>> deleteMediaFile(
                        @Parameter(description = "Media filename to delete", required = true) @PathVariable String filename) {

                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.info("Admin: Deleting media file: {} by admindashboard: {}", filename, authenticatedAdminId);

                boolean deleted = newsService.deleteMediaFileByName(filename);

                if (deleted) {
                        log.info("Admin: Media file deleted successfully: {}", filename);
                        return ResponseEntity.ok(ApiResponseDto.success("Media file deleted successfully", true));
                } else {
                        log.warn("Admin: Media file not found: {}", filename);
                        return ResponseEntity.ok(ApiResponseDto.success("Media file not found", false));
                }
        }

        // ===============================
        // Endpoint 25c: Get Thumbnail File
        // ===============================

        /**
         * Retrieves a thumbnail file by filename.
         *
         * @param filename the thumbnail filename
         * @return the thumbnail resource
         */
        @GetMapping("/thumbnails/{filename}")
        @Operation(summary = "24c. Get thumbnail file", description = "Retrieves a thumbnail file by filename")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Thumbnail retrieved successfully"),
                        @ApiResponse(responseCode = "404", description = "Thumbnail not found")
        })
        public ResponseEntity<Resource> getThumbnailFile(
                        @Parameter(description = "Thumbnail filename", required = true) @PathVariable String filename) {

                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.debug("Admin: Fetching thumbnail: {} by admindashboard: {}", filename, authenticatedAdminId);

                NewsMediaFileRequestDto thumbnailFile = newsService.getThumbnailFile(filename);

                log.debug("Admin: Retrieved thumbnail: {}", filename);
                return ResponseEntity.ok()
                                .contentType(MediaType.parseMediaType(thumbnailFile.getContentType()))
                                .body(thumbnailFile.getResource());
        }

        // ===============================
        // Endpoint 25d: Upload Custom Thumbnail
        // ===============================

        /**
         * Uploads a custom thumbnail for a media file.
         *
         * @param file      the thumbnail image file
         * @param mediaName the associated media filename for naming consistency
         * @return the thumbnail response with URL and metadata
         */
        @PostMapping(value = "/thumbnails/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        @Operation(summary = "24d. Upload custom thumbnail", description = "Uploads a custom thumbnail image")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Thumbnail uploaded successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid file or upload failed")
        })
        public ResponseEntity<ApiResponseDto<ThumbnailResponseDto>> uploadThumbnail(
                        @Parameter(description = "Thumbnail image file", required = true) @RequestParam("file") MultipartFile file,
                        @Parameter(description = "Associated media filename for naming", required = true) @RequestParam("mediaName") String mediaName)
                        throws IOException {

                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.info("Admin: Uploading custom thumbnail for: {} by admindashboard: {}", mediaName,
                                authenticatedAdminId);

                ThumbnailResponseDto response = newsService.uploadThumbnail(file, mediaName);

                log.info("Admin: Custom thumbnail uploaded: {}", response.getFilename());
                return ResponseEntity.ok(ApiResponseDto.success("Thumbnail uploaded successfully", response));
        }

        // ===============================
        // Endpoint 25e: Generate Thumbnail from Media
        // ===============================

        /**
         * Generates a thumbnail from an uploaded media file.
         * Auto-detects if image (resize) or video (extract frame).
         *
         * @param file the source image or video file
         * @return the generated thumbnail response
         */
        @PostMapping(value = "/thumbnails/generate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        @Operation(summary = "24e. Generate thumbnail from media", description = "Generates a thumbnail by resizing image or extracting video frame")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Thumbnail generated successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid file or generation failed")
        })
        public ResponseEntity<ApiResponseDto<ThumbnailResponseDto>> generateThumbnail(
                        @Parameter(description = "Source media file (image or video)", required = true) @RequestParam("file") MultipartFile file)
                        throws IOException {

                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.info("Admin: Generating thumbnail from: {} by admindashboard: {}",
                                file.getOriginalFilename(), authenticatedAdminId);

                ThumbnailResponseDto response = newsService.generateThumbnail(file);

                log.info("Admin: Thumbnail generated: {} (source: {})",
                                response.getFilename(), response.getSource());
                return ResponseEntity.ok(ApiResponseDto.success("Thumbnail generated successfully", response));
        }

        // ===============================
        // Endpoint 25f: Delete Thumbnail
        // ===============================

        /**
         * Deletes a thumbnail file by filename.
         *
         * @param filename the thumbnail filename to delete
         * @return success response if deleted
         */
        @DeleteMapping("/thumbnails/{filename}")
        @Operation(summary = "24f. Delete thumbnail", description = "Deletes a thumbnail file by filename")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Thumbnail deleted successfully"),
                        @ApiResponse(responseCode = "404", description = "Thumbnail not found")
        })
        public ResponseEntity<ApiResponseDto<Boolean>> deleteThumbnail(
                        @Parameter(description = "Thumbnail filename to delete", required = true) @PathVariable String filename) {

                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.info("Admin: Deleting thumbnail: {} by admindashboard: {}", filename, authenticatedAdminId);

                boolean deleted = newsService.deleteThumbnail(filename);

                if (deleted) {
                        log.info("Admin: Thumbnail deleted successfully: {}", filename);
                        return ResponseEntity.ok(ApiResponseDto.success("Thumbnail deleted successfully", true));
                } else {
                        log.warn("Admin: Thumbnail not found: {}", filename);
                        return ResponseEntity.ok(ApiResponseDto.success("Thumbnail not found", false));
                }
        }

        // ===============================
        // Endpoint 26: Get Related News
        // ===============================

        /**
         * Retrieves related newsapp articles based on newscategory.
         *
         * @param newsId  the newsapp newsapp ID
         * @param limit   the maximum number of related articles
         * @param sort    the sort order
         * @param adminId the admindashboard UUID
         * @return list of related newsapp articles
         */
        @GetMapping("/{newsId}/related")
        @Operation(summary = "25. Get related newsapp", description = "Retrieves related newsapp articles based on newscategory")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Related newsapp retrieved successfully"),
                        @ApiResponse(responseCode = "404", description = "News newsapp not found")
        })
        public ResponseEntity<ApiResponseDto<List<NewsCreateResponseDto>>> getRelatedNews(
                        @Parameter(description = "News newsapp ID", required = true) @PathVariable String newsId,
                        @Parameter(description = "Maximum number of related articles") @RequestParam(defaultValue = "5") @Min(1) @Max(20) int limit,
                        @Parameter(description = "Sort order") @RequestParam(defaultValue = DEFAULT_SORT) String sort) {

                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.debug("Admin: Fetching related newsapp for: {} limit: {} by admindashboard: {}", newsId, limit,
                                authenticatedAdminId);

                List<NewsCreateResponseDto> related = newsService.getRelatedNews(newsId, limit, sort);

                log.debug("Admin: Retrieved {} related newsapp articles for: {}", related.size(), newsId);
                return ResponseEntity.ok(ApiResponseDto.success("Related newsapp fetched successfully", related));
        }

        // ===============================
        // Endpoint 27: Get Trending News
        // ===============================

        /**
         * Retrieves trending newsapp articles based on newsengagement metrics.
         *
         * @param page    the page number
         * @param size    the page size
         * @param adminId the admindashboard UUID
         * @return paginated list of trending newsapp
         */
        @GetMapping("/trending")
        @Operation(summary = "26. Get trending newsapp", description = "Retrieves trending newsapp based on views, likes, and shares")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Trending newsapp retrieved successfully")
        })
        public ResponseEntity<ApiResponseDto<Page<NewsCreateResponseDto>>> getTrendingNews(
                        @Parameter(description = "Page number") @RequestParam(defaultValue = DEFAULT_PAGE) @Min(0) int page,
                        @Parameter(description = "Page size") @RequestParam(defaultValue = DEFAULT_SIZE) @Min(1) @Max(MAX_PAGE_SIZE) int size) {

                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.debug("Admin: Fetching trending newsapp - page: {}, size: {} by admindashboard: {}", page, size,
                                authenticatedAdminId);

                Pageable pageable = PageRequest.of(page, size);
                Page<NewsCreateResponseDto> trending = newsService.getTrendingNews(pageable);

                log.debug("Admin: Retrieved {} trending newsapp articles", trending.getTotalElements());
                return ResponseEntity.ok(ApiResponseDto.success("Trending newsapp fetched successfully", trending));
        }

        // ===============================
        // Endpoint 28: Get Social Sharing Dashboard
        // ===============================

        /**
         * Retrieves the social sharing dashboard with per-platform tracking for
         * published and scheduled news.
         *
         * @return the social sharing dashboard response with platform statuses
         */
        @GetMapping("/social-sharing/dashboard")
        @Operation(summary = "27. Get social sharing dashboard", description = "Retrieves published and scheduled news with per-platform social media sharing status")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Social sharing dashboard retrieved successfully")
        })
        public ResponseEntity<ApiResponseDto<SocialMediaShareDashboardResponseDto>> getSocialSharingDashboard() {

                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.debug("Admin: Fetching social sharing dashboard for admin: {}", authenticatedAdminId);

                SocialMediaShareDashboardResponseDto response = socialSharingService.getSocialMediaSharingDashboard();

                log.debug("Admin: Retrieved social sharing dashboard with {} high priority, {} medium priority, {} scheduled items",
                                response.getHighPriorityCount(), response.getMediumPriorityCount(),
                                response.getScheduledReadyCount());
                return ResponseEntity
                                .ok(ApiResponseDto.success("Social sharing dashboard fetched successfully", response));
        }

        // ===============================
        // Endpoint 29: Mark Platform as Shared
        // ===============================

        /**
         * Marks a specific platform as shared for a news article.
         *
         * @param requestDto the request containing platform sharing details
         * @return success response
         */
        @PostMapping("/social-sharing/platforms/complete")
        @Operation(summary = "28. Mark platform as shared", description = "Marks a specific social media platform as shared for a news article")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Platform marked as shared successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid request data")
        })
        public ResponseEntity<ApiResponseDto<Void>> markPlatformShared(
                        @Valid @RequestBody SocialMediaShareMarkPlatformSharedRequestDto requestDto) {

                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.info("Admin: Marking platform {} as shared for news {} by admin: {}",
                                requestDto.getPlatform(), requestDto.getNewsId(), authenticatedAdminId);

                // Set the admin who performed the sharing
                requestDto.setSharedBy(authenticatedAdminId);

                socialSharingService.markPlatformShared(requestDto);

                log.info("Admin: Platform {} marked as shared successfully for news {}", requestDto.getPlatform(),
                                requestDto.getNewsId());
                return ResponseEntity
                                .ok(ApiResponseDto.success("Platform marked as shared successfully", null));
        }

        // ===============================
        // Endpoint 30: Get Sharing Statistics
        // ===============================

        /**
         * Retrieves social sharing statistics for the dashboard.
         *
         * @return sharing statistics
         */
        @GetMapping("/social-sharing/statistics")
        @Operation(summary = "29. Get sharing statistics", description = "Retrieves social media sharing statistics and completion rates")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Sharing statistics retrieved successfully")
        })
        public ResponseEntity<ApiResponseDto<Map<String, Object>>> getSharingStatistics() {

                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.debug("Admin: Fetching sharing statistics for admin: {}", authenticatedAdminId);

                Map<String, Object> statistics = socialSharingService.getSharingStatistics();

                log.debug("Admin: Retrieved sharing statistics");
                return ResponseEntity
                                .ok(ApiResponseDto.success("Sharing statistics fetched successfully", statistics));
        }

        // ===============================
        // Endpoint 31: Elasticsearch Reindex
        // ===============================

        /**
         * Manually triggers Elasticsearch reindexing of all news articles.
         *
         * <p>
         * This endpoint is useful when:
         * </p>
         * <ul>
         * <li>Elasticsearch was down and missed indexing of new/updated articles</li>
         * <li>Elasticsearch index needs to be rebuilt from scratch</li>
         * <li>Manual synchronization is needed between database and search index</li>
         * </ul>
         *
         * <p>
         * <b>Note:</b> This is a long-running operation and may take several seconds
         * depending on the number of articles.
         * </p>
         *
         * @return result of reindexing operation
         */
        @PostMapping("/elasticsearch/reindex")
        @Operation(summary = "30. Reindex all news to Elasticsearch", description = "Manually triggers complete reindexing of all news articles to Elasticsearch. Use when ES was down or index needs rebuild.")
        @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Reindexing started successfully") })
        public ResponseEntity<ApiResponseDto<Map<String, Object>>> reindexNewsToElasticsearch() {

                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                long startTime = System.currentTimeMillis();
                log.info("Admin {} initiated Elasticsearch reindex operation", authenticatedAdminId);

                try {
                        // Get all news articles and reindex them
                        List<UUID> allNewsIds = newsService.getAllNewsIds();
                        log.info("Reindexing {} news articles to Elasticsearch", allNewsIds.size());

                        if (newsElasticSearchService.isPresent()) {
                                newsElasticSearchService.get().indexNewsArticles(allNewsIds);

                                long duration = System.currentTimeMillis() - startTime;
                                log.info("Elasticsearch reindex completed in {} ms - indexed {} articles", duration,
                                                allNewsIds.size());

                                Map<String, Object> result = Map.of(
                                                "indexedCount", allNewsIds.size(),
                                                "durationMs", duration,
                                                "status", "SUCCESS");

                                return ResponseEntity.ok(
                                                ApiResponseDto.success(
                                                                "Elasticsearch reindex completed successfully - "
                                                                                + allNewsIds.size()
                                                                                + " articles indexed",
                                                                result));
                        } else {
                                log.warn("Elasticsearch is not available - reindex skipped");
                                return ResponseEntity.ok(ApiResponseDto.success(
                                                "Elasticsearch is not available - reindex skipped",
                                                Map.of("status", "SKIPPED", "indexedCount", 0)));
                        }
                } catch (Exception e) {
                        long duration = System.currentTimeMillis() - startTime;
                        log.error("Elasticsearch reindex failed after {} ms - error: {}", duration, e.getMessage(), e);

                        return ResponseEntity.internalServerError().body(
                                        ApiResponseDto.error("Elasticsearch reindex failed: " + e.getMessage()));
                }
        }

        // ===============================
        // Endpoint 29: Clone News Article
        // ===============================

        /**
         * Clones an existing news article creating a new DRAFT copy for editing.
         * 
         * <p>
         * <strong>What Gets Copied:</strong>
         * <ul>
         * <li>Content: titles, content, excerpts (all languages)</li>
         * <li>Media: images, thumbnails, URLs</li>
         * <li>Metadata: category, tags, keywords</li>
         * <li>Configuration: urgency level, content format</li>
         * </ul>
         * </p>
         *
         * <p>
         * <strong>What Gets Reset (Fresh Start):</strong>
         * <ul>
         * <li>Status: DRAFT (requires editing and publishing)</li>
         * <li>Engagement metrics: 0 (views, likes, shares, comments)</li>
         * <li>Publishing dates: cleared (must reschedule if needed)</li>
         * <li>ID: new UUID (unique article)</li>
         * <li>Workflow: fresh clone, independent lifecycle</li>
         * </ul>
         * </p>
         *
         * @param newsId the ID of the news article to clone
         * @return the cloned news article (DRAFT status, ready for editing)
         */
        @PostMapping("/{newsId}/clone")
        @PreAuthorize("hasAuthority('news.create')")
        @Operation(summary = "29. Clone news article", description = "Creates a new DRAFT copy of an existing news article. Perfect for creating variations or templates. Content is copied but engagement metrics are reset.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "News cloned successfully"),
                        @ApiResponse(responseCode = "404", description = "Source news article not found"),
                        @ApiResponse(responseCode = "403", description = "Unauthorized - news.create permission required")
        })
        public ResponseEntity<ApiResponseDto<NewsCreateResponseDto>> cloneNews(
                        @Parameter(description = "Source news article ID", required = true) @PathVariable String newsId,
                        HttpServletRequest request) {

                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.info("Admin: Cloning news article: {} by admindashboard: {}", newsId, authenticatedAdminId);

                try {
                        NewsCreateResponseDto clonedNews = newsService.cloneNews(newsId, authenticatedAdminId);

                        // Extract client info for audit logging
                        RequestClientInfoDto clientInfo = requestInfoService.getClientInfo(request);
                        log.info("Admin: News cloned successfully - source={}, clone={}",
                                        newsId, clonedNews.getNewsNewsId());

                        return ResponseEntity.ok(ApiResponseDto.success(
                                        "News article cloned successfully. New article is in DRAFT status and ready for editing.",
                                        clonedNews));
                } catch (InvalidRequestException e) {
                        log.warn("Admin: Clone failed - source news not found: {}", newsId);
                        return ResponseEntity.status(404)
                                        .body(ApiResponseDto.error("Source news article not found"));
                } catch (Exception e) {
                        log.error("Admin: Clone failed - error: {}", e.getMessage(), e);
                        return ResponseEntity.internalServerError()
                                        .body(ApiResponseDto.error("Failed to clone news article: " + e.getMessage()));
                }
        }

        // ===============================
        // Private Helper Methods
        // ===============================

        /**
         * Validates admindashboard access by extracting the admindashboard ID from JWT
         * and checking
         * /**
         * Creates a Pageable instance with the specified parameters.
         *
         * @param page the page number
         * @param size the page size
         * @param sort the sort specification
         * @return configured Pageable instance
         */
        private Pageable createPageable(int page, int size, String sort) {
                return PageRequest.of(page, size, Sort.by(SortUtils.parseSort(sort)));
        }
}
