package com.mmva.newsapp.controller.admin.newsengagement;

import com.mmva.newsapp.infrastructure.common.api.dto.ApiResponseDto;
import com.mmva.newsapp.domain.newsengagement.comments.dto.NewsCommentResponseDto;

// ==================== Service ====================
import com.mmva.newsapp.domain.adminuser.service.validation.AdminValidationService;
import com.mmva.newsapp.domain.newsengagement.comments.service.NewsCommentService;

// ==================== OpenAPI ====================
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

// ==================== Validation ====================
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

// ==================== Lombok ====================
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// ==================== Spring ====================
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// ==================== Java ====================
import java.util.UUID;

/**
 * Admin API Controller for News Comments.
 * <p>
 * Provides administrative operations for managing newsapp comments including
 * filtering, retrieval, and moderation (soft delete).
 * All endpoints require admindashboard validation via adminId parameter.
 * </p>
 *
 * <h2>Endpoints:</h2>
 * <ol>
 * <li>GET /api/v1/admindashboard/comments - Get all comments with filters</li>
 * <li>GET /api/v1/admindashboard/comments/{id} - Get comment by ID</li>
 * <li>DELETE /api/v1/admindashboard/comments/{id} - Delete comment (soft
 * delete)</li>
 * </ol>
 *
 * @author MMVA Team
 * @version 1.0
 * @since 2024-06-01
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/admin/comments")
@Tag(name = "Admin - Comment Moderation", description = "Admin APIs for comment moderation and management")
@RequiredArgsConstructor
public class AdminCommentModerationController {

        private static final String DEFAULT_PAGE = "0";
        private static final String DEFAULT_SIZE = "10";
        private static final int MAX_PAGE_SIZE = 100;

        private final NewsCommentService commentService;
        private final AdminValidationService adminValidationService;

        // ==================== 1. Get All Comments with Filters ====================

        /**
         * Retrieves all comments with optional filters.
         *
         * @param status  filter by comment status (PENDING, APPROVED, REJECTED,
         *                DELETED)
         * @param newsId  filter by newsapp newsapp ID
         * @param userId  filter by user ID
         * @param page    page number (0-indexed)
         * @param size    page size
         * @param adminId admindashboard UUID for validation
         * @return paginated list of comments matching the filters
         */
        @GetMapping
        @Operation(summary = "1. Get all comments with filters", description = "Retrieves all comments with optional filters by status, newsapp ID, and user ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Comments retrieved successfully"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid admindashboard credentials")
        })
        public ResponseEntity<ApiResponseDto<Page<NewsCommentResponseDto>>> getAllComments(
                        @Parameter(description = "Filter by status: PENDING, APPROVED, REJECTED, DELETED") @RequestParam(required = false) String status,
                        @Parameter(description = "Filter by newsapp newsapp ID") @RequestParam(required = false) UUID newsId,
                        @Parameter(description = "Filter by user ID") @RequestParam(required = false) UUID userId,
                        @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = DEFAULT_PAGE) @Min(0) int page,
                        @Parameter(description = "Page size (1-100)") @RequestParam(defaultValue = DEFAULT_SIZE) @Min(1) @Max(MAX_PAGE_SIZE) int size) {

                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.debug("Admin: Fetching comments - adminId: {}, status: {}, newsId: {}, userId: {}, page: {}, size: {}",
                                authenticatedAdminId, status, newsId, userId, page, size);

                Pageable pageable = PageRequest.of(page, size);
                Page<NewsCommentResponseDto> comments = commentService.getCommentsWithFilters(status, newsId, userId,
                                pageable);

                log.debug("Admin: Retrieved {} comments for admindashboard: {}", comments.getTotalElements(),
                                authenticatedAdminId);
                return ResponseEntity.ok(ApiResponseDto.success("Comments retrieved successfully", comments));
        }

        // ==================== 2. Get Comment by ID ====================

        /**
         * Retrieves a specific comment by its ID.
         *
         * @param commentId the comment ID
         * @param adminId   admindashboard UUID for validation
         * @return the comment response
         */
        @GetMapping("/{commentId}")
        @Operation(summary = "2. Get comment by ID", description = "Retrieves a specific comment by its UUID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Comment retrieved successfully"),
                        @ApiResponse(responseCode = "404", description = "Comment not found"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid admindashboard credentials")
        })
        public ResponseEntity<ApiResponseDto<NewsCommentResponseDto>> getCommentById(
                        @Parameter(description = "Comment UUID", required = true) @PathVariable UUID commentId) {

                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.debug("Admin: Fetching comment by ID - adminId: {}, commentId: {}", authenticatedAdminId,
                                commentId);

                NewsCommentResponseDto response = commentService.getCommentById(commentId);

                log.debug("Admin: Retrieved comment {} for admindashboard: {}", commentId, authenticatedAdminId);
                return ResponseEntity.ok(ApiResponseDto.success("Comment retrieved successfully", response));
        }

        // ==================== 3. Delete Comment (Soft Delete) ====================

        /**
         * Soft deletes a comment (admindashboard moderation).
         *
         * @param commentId the comment ID to delete
         * @param adminId   admindashboard UUID for validation and audit tracking
         * @return success response
         */
        @DeleteMapping("/{commentId}")
        @Operation(summary = "3. Delete comment (soft delete)", description = "Soft deletes a comment for admindashboard moderation purposes")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Comment deleted successfully"),
                        @ApiResponse(responseCode = "404", description = "Comment not found"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid admindashboard credentials")
        })
        public ResponseEntity<ApiResponseDto<Void>> deleteComment(
                        @Parameter(description = "Comment UUID to delete", required = true) @PathVariable UUID commentId) {

                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.info("Admin: Removing comment - adminId: {}, commentId: {}", authenticatedAdminId, commentId);

                commentService.removeCommentByAdmin(commentId, authenticatedAdminId);

                log.info("Admin: Comment {} removed successfully by admindashboard: {}", commentId,
                                authenticatedAdminId);
                return ResponseEntity.ok(ApiResponseDto.success("Comment deleted successfully", null));
        }

        // ============================================================
        // 4. Approve Comment
        // ============================================================
        @PutMapping("/{commentId}/approve")
        @Operation(summary = "4. Approve comment", description = "Approves a pending comment for public visibility")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Comment approved successfully"),
                        @ApiResponse(responseCode = "404", description = "Comment not found"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid admindashboard credentials")
        })
        public ResponseEntity<ApiResponseDto<NewsCommentResponseDto>> approveComment(
                        @Parameter(description = "Comment UUID to approve", required = true) @PathVariable UUID commentId) {

                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.info("Admin: Approving comment - adminId: {}, commentId: {}", authenticatedAdminId, commentId);

                NewsCommentResponseDto approvedComment = commentService.approveComment(commentId, authenticatedAdminId);

                log.info("Admin: Comment {} approved successfully by admindashboard: {}", commentId,
                                authenticatedAdminId);
                return ResponseEntity.ok(ApiResponseDto.success("Comment approved successfully", approvedComment));
        }

        // ============================================================
        // 5. Reject Comment
        // ============================================================
        @PutMapping("/{commentId}/reject")
        @Operation(summary = "5. Reject comment", description = "Rejects a comment (removes from public visibility)")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Comment rejected successfully"),
                        @ApiResponse(responseCode = "404", description = "Comment not found"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid admindashboard credentials")
        })
        public ResponseEntity<ApiResponseDto<NewsCommentResponseDto>> rejectComment(
                        @Parameter(description = "Comment UUID to reject", required = true) @PathVariable UUID commentId) {

                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.info("Admin: Rejecting comment - adminId: {}, commentId: {}", authenticatedAdminId, commentId);

                NewsCommentResponseDto rejectedComment = commentService.rejectComment(commentId, authenticatedAdminId);

                log.info("Admin: Comment {} rejected successfully by admindashboard: {}", commentId,
                                authenticatedAdminId);
                return ResponseEntity.ok(ApiResponseDto.success("Comment rejected successfully", rejectedComment));
        }
}
