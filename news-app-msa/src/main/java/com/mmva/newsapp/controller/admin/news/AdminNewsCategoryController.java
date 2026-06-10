package com.mmva.newsapp.controller.admin.news;

import com.mmva.newsapp.infrastructure.common.api.dto.ApiResponseDto;
import com.mmva.newsapp.domain.newscategory.dto.audit.NewsCategoryAuditLogDto;
import com.mmva.newsapp.domain.newscategory.dto.core.NewsCategoryRequestDto;
import com.mmva.newsapp.domain.newscategory.dto.core.NewsCategoryResponseDto;
// ==================== Service ====================
import com.mmva.newsapp.domain.adminuser.service.validation.AdminValidationService;
import com.mmva.newsapp.domain.newscategory.service.audit.NewsCategoryAuditLogService;
import com.mmva.newsapp.domain.newscategory.service.core.NewsCategoryService;
import com.mmva.newsapp.infrastructure.requestanalytics.dto.RequestClientInfoDto;
import com.mmva.newsapp.infrastructure.requestanalytics.service.RequestInfoService;

// ==================== OpenAPI ====================
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

// ==================== Validation ====================
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.servlet.http.HttpServletRequest;

// ==================== Lombok ====================
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// ==================== Spring ====================
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// ==================== Java ====================
import java.util.List;
import java.util.UUID;

/**
 * Admin API Controller for News Categories.
 * <p>
 * Provides full CRUD operations for newsapp categories - admindashboard only.
 * All endpoints require admindashboard validation via adminId parameter.
 * </p>
 *
 * <h2>Endpoints:</h2>
 * <ol>
 * <li>POST /api/v1/admindashboard/categories - Create newscategory</li>
 * <li>GET /api/v1/admindashboard/categories - Get all categories
 * (paginated)</li>
 * <li>GET /api/v1/admindashboard/categories/all - Get all categories (no
 * pagination)</li>
 * <li>GET /api/v1/admindashboard/categories/{id} - Get newscategory by ID</li>
 * <li>PUT /api/v1/admindashboard/categories/{id} - Update newscategory</li>
 * <li>DELETE /api/v1/admindashboard/categories/{id} - Delete newscategory</li>
 * <li>GET /api/v1/admindashboard/categories/{id}/audit-logs - Get newscategory
 * audit logs</li>
 * <li>PATCH /api/v1/admindashboard/categories/{id}/activate - Activate
 * category</li>
 * <li>PATCH /api/v1/admindashboard/categories/{id}/deactivate - Deactivate
 * category</li>
 * <li>POST /api/v1/admindashboard/categories/{id}/restore - Restore
 * category</li>
 * </ol>
 *
 * @author MMVA Team
 * @version 1.0
 * @since 2024-06-01
 * @see com.mmva.newsapp.controller.publicapi.PublicNewsCategoryController for
 *      public read-only access
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/admin/news-categories")
@Tag(name = "Admin - Category Management", description = "Admin operations for news category management")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = { "Range", "Accept", "Content-Type" }, exposedHeaders = { "Content-Length",
                "Content-Range", "Accept-Ranges" })
public class AdminNewsCategoryController {

        private static final String DEFAULT_SORT = "createdAt,desc";
        private static final String DEFAULT_PAGE = "0";
        private static final String DEFAULT_SIZE = "10";
        private static final int MAX_PAGE_SIZE = 100;

        private final NewsCategoryService categoryService;
        private final NewsCategoryAuditLogService auditLogService;
        private final AdminValidationService adminValidationService;
        private final RequestInfoService requestInfoService;

        // ==================== 1. Create Category ====================

        /**
         * Creates a new newsapp newscategory.
         *
         * @param dto     the newscategory creation request
         * @param adminId the UUID of the admindashboard performing the action
         * @return the created newscategory response
         */
        @PostMapping
        @Operation(summary = "1. Create a new newscategory", description = "Creates a new newsapp newscategory with the provided details")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Category created successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid request data"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid admindashboard credentials"),
                        @ApiResponse(responseCode = "409", description = "Duplicate newscategory (slug conflict)")
        })
        public ResponseEntity<ApiResponseDto<NewsCategoryResponseDto>> createCategory(
                        @Parameter(description = "Category creation data", required = true) @Valid @RequestBody NewsCategoryRequestDto dto,
                        HttpServletRequest request) {

                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                dto.setAdminId(authenticatedAdminId); // Set admin ID from JWT token
                log.info("Admin: Creating newscategory - adminId: {}, name: {}", authenticatedAdminId,
                                dto.getCategoryNameEn());

                RequestClientInfoDto clientInfo = requestInfoService.getClientInfo(request);
                NewsCategoryResponseDto response = categoryService.create(dto, clientInfo);

                log.info("Admin: Category created successfully - adminId: {}, categoryId: {}", authenticatedAdminId,
                                response.getNewsCategoriesId());
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponseDto.success("Category created successfully", response));
        }

        // ==================== 2. Get All Categories (Paginated) ====================

        /**
         * Retrieves all categories with pagination.
         * 
         * Query Parameters:
         * - includeDeleted (default: false): If true, includes soft-deleted categories;
         * if false, only active/inactive
         *
         * @param page           the page number (0-indexed)
         * @param size           the page size
         * @param sort           the sort order
         * @param includeDeleted whether to include soft-deleted categories (default:
         *                       false)
         * @return paginated list of categories
         */
        @GetMapping
        @Operation(summary = "2. Get categories (paginated)", description = "Retrieves categories with pagination and sorting. Use statusFilter=ACTIVE for news creation, statusFilter=ACTIVE_INACTIVE for browsing, or includeDeleted=true for admin view with all including deleted.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Categories retrieved successfully"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid administrator credentials")
        })
        public ResponseEntity<ApiResponseDto<Page<NewsCategoryResponseDto>>> getAllCategories(
                        @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = DEFAULT_PAGE) @Min(0) int page,
                        @Parameter(description = "Page size (1-100)") @RequestParam(defaultValue = DEFAULT_SIZE) @Min(1) @Max(MAX_PAGE_SIZE) int size,
                        @Parameter(description = "Sort order (e.g., createdAt,desc)") @RequestParam(defaultValue = DEFAULT_SORT) String sort,
                        @Parameter(description = "Status filter: ACTIVE (ACTIVE only), ACTIVE_INACTIVE (ACTIVE+INACTIVE), or omit for default (ACTIVE+INACTIVE)") @RequestParam(required = false) String statusFilter,
                        @Parameter(description = "Include soft-deleted categories (default: false)") @RequestParam(defaultValue = "false") boolean includeDeleted) {

                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.debug("Admin: Fetching categories - adminId: {}, page: {}, size: {}, sort: {}, statusFilter: {}, includeDeleted: {}",
                                authenticatedAdminId, page, size, sort, statusFilter, includeDeleted);

                Pageable pageable = createPageable(page, size, sort);
                log.debug("Admin: Pageable created - sort={}, hashCode={}", pageable.getSort(),
                                pageable.getSort().hashCode());

                Page<NewsCategoryResponseDto> categories;

                if (includeDeleted) {
                        // Admin view: ALL including DELETED
                        categories = categoryService.getAllIncludingDeleted(pageable);
                } else if ("ACTIVE".equalsIgnoreCase(statusFilter)) {
                        // News creation: ACTIVE ONLY
                        categories = categoryService.getActiveCategories(pageable);
                } else {
                        // Public/default: ACTIVE + INACTIVE (statusFilter=ACTIVE_INACTIVE or null)
                        categories = categoryService.getAllActiveInactiveCategories(pageable);
                }

                log.debug("Admin: Retrieved {} categories (statusFilter: {}, includeDeleted: {}) for administrator: {}",
                                categories.getTotalElements(), statusFilter, includeDeleted, authenticatedAdminId);
                return ResponseEntity.ok(ApiResponseDto.success("Categories retrieved successfully", categories));
        }

        // ==================== 4. Get Category by ID ====================

        /**
         * Retrieves a newscategory by its ID.
         *
         * @param newsCategoryId the newscategory ID
         * @param adminId        the UUID of the admindashboard performing the action
         * @return the newscategory response
         */
        @GetMapping("/{newsCategoryId}")
        @Operation(summary = "4. Get newscategory by ID", description = "Retrieves a specific newsapp newscategory by its UUID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Category retrieved successfully"),
                        @ApiResponse(responseCode = "404", description = "Category not found"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid admindashboard credentials")
        })
        public ResponseEntity<ApiResponseDto<NewsCategoryResponseDto>> getCategoryById(
                        @Parameter(description = "Category UUID", required = true) @PathVariable UUID newsCategoryId) {

                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.debug("Admin: Fetching newscategory by ID - adminId: {}, categoryId: {}", authenticatedAdminId,
                                newsCategoryId);

                NewsCategoryResponseDto category = categoryService.getById(newsCategoryId);

                log.debug("Admin: Retrieved newscategory {} for admindashboard: {}", newsCategoryId,
                                authenticatedAdminId);
                return ResponseEntity.ok(ApiResponseDto.success("Category retrieved successfully", category));
        }

        // ==================== 5. Update Category ====================

        /**
         * Updates an existing newscategory.
         *
         * @param newsCategoryId the newscategory ID to update
         * @param dto            the updated newscategory data
         * @param adminId        the UUID of the admindashboard performing the action
         * @return the updated newscategory response
         */
        @PutMapping("/{newsCategoryId}")
        @Operation(summary = "5. Update newscategory", description = "Updates an existing newsapp newscategory with new data")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Category updated successfully"),
                        @ApiResponse(responseCode = "404", description = "Category not found"),
                        @ApiResponse(responseCode = "400", description = "Invalid request data"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid admindashboard credentials")
        })
        public ResponseEntity<ApiResponseDto<NewsCategoryResponseDto>> updateCategory(
                        @Parameter(description = "Category UUID to update", required = true) @PathVariable UUID newsCategoryId,
                        @Parameter(description = "Updated newscategory data", required = true) @Valid @RequestBody NewsCategoryRequestDto dto,
                        HttpServletRequest request) {

                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                dto.setAdminId(authenticatedAdminId); // Set admin ID from JWT token
                log.info("Admin: Updating newscategory - adminId: {}, categoryId: {}", authenticatedAdminId,
                                newsCategoryId);

                RequestClientInfoDto clientInfo = requestInfoService.getClientInfo(request);
                NewsCategoryResponseDto response = categoryService.update(newsCategoryId, dto, clientInfo);

                log.info("Admin: Category updated successfully - adminId: {}, categoryId: {}", authenticatedAdminId,
                                newsCategoryId);
                return ResponseEntity.ok(ApiResponseDto.success("Category updated successfully", response));
        }

        // ==================== 6. Delete Category ====================

        /**
         * Deletes a newscategory by its ID.
         *
         * @param newsCategoryId the newscategory ID to delete
         * @param adminId        the UUID of the admindashboard performing the action
         * @return success response
         */
        @DeleteMapping("/{newsCategoryId}")
        @Operation(summary = "6. Delete newscategory", description = "Deletes a newsapp newscategory by its UUID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Category deleted successfully"),
                        @ApiResponse(responseCode = "404", description = "Category not found"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid admindashboard credentials")
        })
        public ResponseEntity<ApiResponseDto<Void>> deleteCategory(
                        @Parameter(description = "Category UUID to delete", required = true) @PathVariable UUID newsCategoryId,
                        HttpServletRequest request) {

                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.info("Admin: Deleting newscategory - adminId: {}, categoryId: {}", authenticatedAdminId,
                                newsCategoryId);

                RequestClientInfoDto clientInfo = requestInfoService.getClientInfo(request);
                categoryService.delete(newsCategoryId, authenticatedAdminId, clientInfo);

                log.info("Admin: Category deleted successfully - adminId: {}, categoryId: {}", authenticatedAdminId,
                                newsCategoryId);
                return ResponseEntity.ok(ApiResponseDto.success("Category deleted successfully", null));
        }

        // ==================== 7. Get Category Audit Logs ====================

        /**
         * Retrieves audit logs for a specific newscategory.
         *
         * @param newsCategoryId the newscategory ID
         * @param adminId        the UUID of the admindashboard performing the action
         * @return list of audit log entries
         */
        @GetMapping("/{newsCategoryId}/audit-logs")
        @Operation(summary = "7. Get newscategory audit logs", description = "Retrieves audit logs for a specific newscategory")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Audit logs retrieved successfully"),
                        @ApiResponse(responseCode = "404", description = "Category not found"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid admindashboard credentials")
        })
        public ResponseEntity<ApiResponseDto<List<NewsCategoryAuditLogDto>>> getCategoryAuditLogs(
                        @Parameter(description = "Category UUID", required = true) @PathVariable UUID newsCategoryId) {

                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.debug("Admin: Fetching audit logs - adminId: {}, categoryId: {}", authenticatedAdminId,
                                newsCategoryId);

                List<NewsCategoryAuditLogDto> logs = auditLogService.findByCategoryId(newsCategoryId);

                log.debug("Admin: Retrieved {} audit logs for newscategory: {}", logs.size(), newsCategoryId);
                return ResponseEntity.ok(ApiResponseDto.success("Audit logs retrieved successfully", logs));
        }

        // ==================== 8. Activate Category ====================

        /**
         * Activates a news category (sets status to ACTIVE).
         *
         * @param newsCategoryId the category ID to activate
         * @return the activated category response
         */
        @PatchMapping("/{newsCategoryId}/activate")
        @Operation(summary = "8. Activate newscategory", description = "Activates a newscategory (sets status to ACTIVE)")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Category activated successfully"),
                        @ApiResponse(responseCode = "404", description = "Category not found"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid admindashboard credentials")
        })
        public ResponseEntity<ApiResponseDto<NewsCategoryResponseDto>> activateCategory(
                        @Parameter(description = "Category UUID", required = true) @PathVariable UUID newsCategoryId) {

                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.info("Admin: Activating category - adminId: {}, categoryId: {}", authenticatedAdminId,
                                newsCategoryId);

                NewsCategoryResponseDto response = categoryService.activateCategory(newsCategoryId);

                log.info("Admin: Category activated successfully - adminId: {}, categoryId: {}", authenticatedAdminId,
                                newsCategoryId);
                return ResponseEntity.ok(ApiResponseDto.success("Category activated successfully", response));
        }

        // ==================== 9. Deactivate Category ====================

        /**
         * Deactivates a news category (sets status to INACTIVE).
         *
         * @param newsCategoryId the category ID to deactivate
         * @return the deactivated category response
         */
        @PatchMapping("/{newsCategoryId}/deactivate")
        @Operation(summary = "9. Deactivate newscategory", description = "Deactivates a newscategory (sets status to INACTIVE)")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Category deactivated successfully"),
                        @ApiResponse(responseCode = "404", description = "Category not found"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid admindashboard credentials")
        })
        public ResponseEntity<ApiResponseDto<NewsCategoryResponseDto>> deactivateCategory(
                        @Parameter(description = "Category UUID", required = true) @PathVariable UUID newsCategoryId) {

                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.info("Admin: Deactivating category - adminId: {}, categoryId: {}", authenticatedAdminId,
                                newsCategoryId);

                NewsCategoryResponseDto response = categoryService.deactivateCategory(newsCategoryId);

                log.info("Admin: Category deactivated successfully - adminId: {}, categoryId: {}", authenticatedAdminId,
                                newsCategoryId);
                return ResponseEntity.ok(ApiResponseDto.success("Category deactivated successfully", response));
        }

        // ==================== 10. Restore Category ====================

        /**
         * Restores a soft-deleted news category (sets status to ACTIVE).
         *
         * @param newsCategoryId the category ID to restore
         * @return the restored category response
         */
        @PostMapping("/{newsCategoryId}/restore")
        @Operation(summary = "10. Restore newscategory", description = "Restores a soft-deleted newscategory to ACTIVE status")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Category restored successfully"),
                        @ApiResponse(responseCode = "404", description = "Category not found"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid admindashboard credentials")
        })
        public ResponseEntity<ApiResponseDto<NewsCategoryResponseDto>> restoreCategory(
                        @Parameter(description = "Category UUID", required = true) @PathVariable UUID newsCategoryId) {

                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.info("Admin: Restoring category - adminId: {}, categoryId: {}", authenticatedAdminId,
                                newsCategoryId);

                NewsCategoryResponseDto response = categoryService.restoreCategory(newsCategoryId);

                log.info("Admin: Category restored successfully - adminId: {}, categoryId: {}", authenticatedAdminId,
                                newsCategoryId);
                return ResponseEntity.ok(ApiResponseDto.success("Category restored successfully", response));
        }

        // ==================== Private Helper Methods ====================

        /**
         * Validates admindashboard access by extracting the admindashboard ID from JWT
         * and checking
         * /**
         * Creates a Pageable object with sorting.
         *
         * @param page the page number
         * @param size the page size
         * @param sort the sort specification (format: "fieldName,direction")
         * @return configured Pageable
         */
        private Pageable createPageable(int page, int size, String sort) {
                try {
                        String[] sortParams = sort.split(",");
                        if (sortParams.length < 1 || sortParams[0].trim().isEmpty()) {
                                log.warn("Admin: Invalid sort parameter - empty field name. Using default sort");
                                return PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
                        }

                        Sort.Direction direction = sortParams.length > 1 && "asc".equalsIgnoreCase(sortParams[1])
                                        ? Sort.Direction.ASC
                                        : Sort.Direction.DESC;

                        String fieldName = sortParams[0].trim();
                        log.debug("Admin: Creating Pageable with page={}, size={}, sort={} {}",
                                        page, size, fieldName, direction);

                        return PageRequest.of(page, size, Sort.by(direction, fieldName));
                } catch (Exception e) {
                        log.error("Admin: Error parsing sort parameter '{}': {}", sort, e.getMessage());
                        return PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
                }
        }
}
