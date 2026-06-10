package com.mmva.newsapp.controller.user.engagement;

import com.mmva.newsapp.domain.newsengagement.comments.dto.NewsCommentRequestDto;
import com.mmva.newsapp.domain.newsengagement.comments.dto.NewsCommentResponseDto;
import com.mmva.newsapp.domain.newsengagement.comments.dto.NewsCommentUpdateDto;
import com.mmva.newsapp.domain.newsengagement.comments.service.NewsCommentService;
import com.mmva.newsapp.infrastructure.common.api.dto.ApiResponseDto;
import com.mmva.newsapp.infrastructure.security.userdetails.AppUserDetails;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * User Comment Controller.
 * 
 * <p>
 * Handles all comment operations for the authenticated user:
 * </p>
 * <ul>
 * <li>Comment management (add, update, delete)</li>
 * <li>Comment engagement (like, dislike, report)</li>
 * <li>Comment queries (list, replies, threads)</li>
 * </ul>
 * 
 * <p>
 * Path prefix: /api/v1/me/comments
 * </p>
 */
@CrossOrigin(origins = "*", allowedHeaders = { "Range", "Accept", "Content-Type", "Authorization" }, exposedHeaders = {
                "Content-Length", "Content-Range", "Accept-Ranges" })
@RestController
@RequestMapping("/api/v1/me/comments")
@Tag(name = "User Comments", description = "Comment management and engagement for authenticated users")
@Validated
@Slf4j
@RequiredArgsConstructor
public class UserCommentController {

        private final NewsCommentService commentService;

        // ==========================================
        // COMMENT CRUD OPERATIONS (1-4)
        // ==========================================

        @GetMapping
        @Operation(summary = "1. Get my comments", description = "Retrieves all comments made by the current user (all statuses)")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Comments retrieved"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized")
        })
        public ResponseEntity<ApiResponseDto<List<NewsCommentResponseDto>>> getMyComments(
                        @AuthenticationPrincipal AppUserDetails userDetails) {
                UUID userId = userDetails.getUserId();
                log.debug("Comment [{}]: Fetching all comments", userId);
                List<NewsCommentResponseDto> comments = commentService.getMyComments(userId);
                log.debug("Comment [{}]: Retrieved {} comments", userId, comments.size());
                return ResponseEntity.ok(ApiResponseDto.success("Comments retrieved successfully", comments));
        }

        @PostMapping
        @Operation(summary = "2. Add a comment", description = "Adds a comment to a news article")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Comment added"),
                        @ApiResponse(responseCode = "400", description = "Invalid request"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized")
        })
        public ResponseEntity<ApiResponseDto<NewsCommentResponseDto>> addComment(
                        @AuthenticationPrincipal AppUserDetails userDetails,
                        @Valid @RequestBody NewsCommentRequestDto dto) {
                UUID userId = userDetails.getUserId();
                log.info("Comment [{}]: Adding comment to news: {}", userId, dto.getNewsCommentsNewsId());
                dto.setNewsCommentsUserId(userId);
                NewsCommentResponseDto response = commentService.addComment(dto);
                log.info("Comment [{}]: Comment added to news: {}", userId, dto.getNewsCommentsNewsId());
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponseDto.success("Comment added successfully", response));
        }

        @PutMapping("/{commentId}")
        @Operation(summary = "3. Update my comment", description = "Updates a comment (validates ownership)")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Comment updated"),
                        @ApiResponse(responseCode = "400", description = "Invalid request"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized"),
                        @ApiResponse(responseCode = "403", description = "Not comment owner"),
                        @ApiResponse(responseCode = "404", description = "Comment not found")
        })
        public ResponseEntity<ApiResponseDto<NewsCommentResponseDto>> updateMyComment(
                        @AuthenticationPrincipal AppUserDetails userDetails,
                        @Parameter(description = "Comment ID to update", required = true) @PathVariable UUID commentId,
                        @Valid @RequestBody NewsCommentUpdateDto dto) {
                UUID userId = userDetails.getUserId();
                log.info("Comment [{}]: Updating comment: {}", userId, commentId);
                NewsCommentResponseDto response = commentService.updateComment(commentId, userId, dto);
                log.info("Comment [{}]: Comment updated: {}", userId, commentId);
                return ResponseEntity.ok(ApiResponseDto.success("Comment updated successfully", response));
        }

        @DeleteMapping("/{commentId}")
        @Operation(summary = "4. Delete my comment", description = "Soft deletes a comment (validates ownership)")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Comment deleted"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized"),
                        @ApiResponse(responseCode = "403", description = "Not comment owner"),
                        @ApiResponse(responseCode = "404", description = "Comment not found")
        })
        public ResponseEntity<ApiResponseDto<Void>> deleteMyComment(
                        @AuthenticationPrincipal AppUserDetails userDetails,
                        @Parameter(description = "Comment ID to delete", required = true) @PathVariable UUID commentId) {
                UUID userId = userDetails.getUserId();
                log.info("Comment [{}]: Removing comment: {}", userId, commentId);
                commentService.removeCommentByUser(commentId, userId);
                log.info("Comment [{}]: Comment removed: {}", userId, commentId);
                return ResponseEntity.ok(ApiResponseDto.success("Comment deleted successfully", null));
        }

        // ==========================================
        // COMMENT THREAD OPERATIONS (5-6)
        // ==========================================

        @GetMapping("/{commentId}/replies")
        @Operation(summary = "5. Get replies for a comment", description = "Fetches all replies for a given comment ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Replies retrieved"),
                        @ApiResponse(responseCode = "404", description = "Comment not found")
        })
        public ResponseEntity<ApiResponseDto<List<NewsCommentResponseDto>>> getRepliesForComment(
                        @Parameter(description = "Comment ID to get replies for", required = true) @PathVariable UUID commentId) {
                log.debug("Comment: Fetching replies for comment: {}", commentId);
                List<NewsCommentResponseDto> replies = commentService.getApprovedReplies(commentId);
                log.debug("Comment: Retrieved {} replies for comment: {}", replies.size(), commentId);
                return ResponseEntity.ok(ApiResponseDto.success("Replies fetched", replies));
        }

        @GetMapping("/news/{newsId}/thread")
        @Operation(summary = "6. Get comment thread for news", description = "Fetches the full nested comment thread for a news article")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Comment thread retrieved"),
                        @ApiResponse(responseCode = "404", description = "News not found")
        })
        public ResponseEntity<ApiResponseDto<List<NewsCommentResponseDto>>> getCommentThreadForNews(
                        @Parameter(description = "News ID to get comment thread for", required = true) @PathVariable UUID newsId) {
                log.debug("Comment: Fetching comment thread for news: {}", newsId);
                List<NewsCommentResponseDto> thread = commentService.getApprovedCommentThreadForNews(newsId);
                log.debug("Comment: Retrieved comment thread with {} top-level comments for news: {}", thread.size(),
                                newsId);
                return ResponseEntity.ok(ApiResponseDto.success("Comment thread fetched", thread));
        }

        // ==========================================
        // COMMENT ENGAGEMENT OPERATIONS (7-10)
        // ==========================================

        @PostMapping("/{commentId}/like")
        @Operation(summary = "7. Like a comment", description = "Likes a comment as the current user")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Comment liked"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized"),
                        @ApiResponse(responseCode = "404", description = "Comment not found")
        })
        public ResponseEntity<ApiResponseDto<Void>> likeComment(
                        @AuthenticationPrincipal AppUserDetails userDetails,
                        @Parameter(description = "Comment ID to like", required = true) @PathVariable UUID commentId) {
                UUID userId = userDetails.getUserId();
                log.info("Comment [{}]: Liking comment: {}", userId, commentId);
                commentService.likeComment(commentId, userId);
                log.info("Comment [{}]: Comment liked: {}", userId, commentId);
                return ResponseEntity.ok(ApiResponseDto.success("Comment liked", null));
        }

        @PostMapping("/{commentId}/dislike")
        @Operation(summary = "8. Dislike a comment", description = "Dislikes a comment as the current user")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Comment disliked"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized"),
                        @ApiResponse(responseCode = "404", description = "Comment not found")
        })
        public ResponseEntity<ApiResponseDto<Void>> dislikeComment(
                        @AuthenticationPrincipal AppUserDetails userDetails,
                        @Parameter(description = "Comment ID to dislike", required = true) @PathVariable UUID commentId) {
                UUID userId = userDetails.getUserId();
                log.info("Comment [{}]: Disliking comment: {}", userId, commentId);
                commentService.dislikeComment(commentId, userId);
                log.info("Comment [{}]: Comment disliked: {}", userId, commentId);
                return ResponseEntity.ok(ApiResponseDto.success("Comment disliked", null));
        }

        @PostMapping("/{commentId}/report")
        @Operation(summary = "9. Report a comment", description = "Reports a comment for abuse or spam")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Comment reported"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized"),
                        @ApiResponse(responseCode = "404", description = "Comment not found")
        })
        public ResponseEntity<ApiResponseDto<Void>> reportComment(
                        @AuthenticationPrincipal AppUserDetails userDetails,
                        @Parameter(description = "Comment ID to report", required = true) @PathVariable UUID commentId,
                        @Parameter(description = "Reason for reporting", required = true) @RequestParam String reason) {
                UUID userId = userDetails.getUserId();
                log.info("Comment [{}]: Reporting comment: {} - reason: {}", userId, commentId, reason);
                commentService.reportComment(commentId, userId, reason);
                log.info("Comment [{}]: Comment reported: {}", userId, commentId);
                return ResponseEntity.ok(ApiResponseDto.success("Comment reported", null));
        }

        @GetMapping("/{commentId}/likes")
        @Operation(summary = "10. Get users who liked a comment", description = "Returns a list of user IDs who liked the comment")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Users retrieved"),
                        @ApiResponse(responseCode = "404", description = "Comment not found")
        })
        public ResponseEntity<ApiResponseDto<List<UUID>>> getUsersWhoLikedComment(
                        @Parameter(description = "Comment ID", required = true) @PathVariable UUID commentId) {
                log.debug("Comment: Fetching users who liked comment: {}", commentId);
                List<UUID> users = commentService.getUsersWhoLikedComment(commentId);
                log.debug("Comment: Retrieved {} users who liked comment: {}", users.size(), commentId);
                return ResponseEntity.ok(ApiResponseDto.success("Users who liked comment retrieved", users));
        }
}
