package com.mmva.newsapp.controller.open.news;

// OpenAPI/Swagger imports
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

// Lombok imports
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// Spring imports
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mmva.newsapp.domain.newscategory.dto.core.NewsCategoryResponseDto;
import com.mmva.newsapp.domain.newscategory.service.core.NewsCategoryService;
import com.mmva.newsapp.infrastructure.common.api.dto.ApiResponseDto;

// Java imports
import java.util.List;
import java.util.UUID;

/**
 * Public API Controller for News Categories.
 * 
 * <p>
 * Provides read-only access to newsapp categories for public (unauthenticated)
 * users.
 * Following industry best practices:
 * </p>
 * <ul>
 * <li>Path prefix: /api/v1/public/news-categories</li>
 * <li>API versioning in path</li>
 * <li>Read-only operations (GET only)</li>
 * <li>No authentication required</li>
 * </ul>
 * 
 * @see com.mmva.newsapp.controller.admindashboard.news.AdminNewsCategoryController
 *      for
 *      admindashboard operations
 */
@CrossOrigin(origins = "*", allowedHeaders = { "Range", "Accept", "Content-Type" }, exposedHeaders = { "Content-Length",
                "Content-Range", "Accept-Ranges" })
@Slf4j
@RestController
@Validated
@RequestMapping("/api/v1/public/news-categories")
@Tag(name = "Public Category API", description = "Public read-only access to newsapp categories")
@RequiredArgsConstructor
public class PublicNewsCategoryController {

        // =============================
        // Constants
        // =============================
        private static final String DEFAULT_PAGE = "0";
        private static final String DEFAULT_SIZE = "10";
        private static final int MAX_PAGE_SIZE = 100;

        // =============================
        // Dependencies
        // =============================
        private final NewsCategoryService categoryService;

        // =============================
        // Category Endpoints
        // =============================

        /**
         * 1. Get all categories with pagination.
         */
        @GetMapping("/paginated")
        @Operation(summary = "1. Get all categories (paginated)", description = "Retrieves all active newsapp categories with pagination support")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Categories retrieved successfully")
        })
        public ResponseEntity<ApiResponseDto<Page<NewsCategoryResponseDto>>> getAllCategoriesPaginated(
                        @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = DEFAULT_PAGE) int page,
                        @Parameter(description = "Page size (max 100)") @RequestParam(defaultValue = DEFAULT_SIZE) int size) {

                log.debug("Public: Fetching ACTIVE/INACTIVE categories with pagination - page={}, size={}", page, size);

                // Limit max page size to prevent abuse
                int limitedSize = Math.min(size, MAX_PAGE_SIZE);
                Pageable pageable = PageRequest.of(page, limitedSize);
                Page<NewsCategoryResponseDto> categories = categoryService.getAllActiveInactiveCategories(pageable);

                log.debug("Public: Retrieved {} categories on page {}",
                                categories.getNumberOfElements(), page);
                return ResponseEntity.ok(ApiResponseDto.success("Categories retrieved successfully", categories));
        }

        /**
         * 3. Get newscategory by ID.
         */
        @GetMapping("/{newsCategoryId}")
        @Operation(summary = "3. Get newscategory by ID", description = "Retrieves a specific newsapp newscategory by its UUID")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Category retrieved successfully"),
                        @ApiResponse(responseCode = "404", description = "Category not found")
        })
        public ResponseEntity<ApiResponseDto<NewsCategoryResponseDto>> getCategoryById(
                        @Parameter(description = "Category UUID", required = true) @PathVariable UUID newsCategoryId) {

                log.debug("Public: Fetching newscategory by ID: {}", newsCategoryId);

                NewsCategoryResponseDto category = categoryService.getById(newsCategoryId);

                log.debug("Public: Retrieved newscategory: {} - {}", newsCategoryId,
                                category.getNewsCategoriesNameEn());
                return ResponseEntity.ok(ApiResponseDto.success("Category retrieved successfully", category));
        }
}
