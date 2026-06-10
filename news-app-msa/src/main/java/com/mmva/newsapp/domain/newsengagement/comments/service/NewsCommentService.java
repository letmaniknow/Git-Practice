package com.mmva.newsapp.domain.newsengagement.comments.service;

// ========================================
// DTO Imports
// ========================================
import com.mmva.newsapp.domain.newsengagement.comments.dto.NewsCommentCountDto;
import com.mmva.newsapp.domain.newsengagement.comments.dto.NewsCommentRequestDto;
import com.mmva.newsapp.domain.newsengagement.comments.dto.NewsCommentResponseDto;
import com.mmva.newsapp.domain.newsengagement.comments.dto.NewsCommentUpdateDto;

// ========================================
// Exception Imports
// ========================================
import com.mmva.newsapp.domain.newsengagement.comments.exception.NewsCommentNotFoundException;
import com.mmva.newsapp.infrastructure.requestanalytics.exception.RateLimitExceededException;
import com.mmva.newsapp.infrastructure.common.exception.UnauthorizedAccessException;
import com.mmva.newsapp.domain.appuser.exception.core.AppUserNotFoundException;

// ========================================
// Spring Framework Imports
// ========================================
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

// ========================================
// Java Core Imports
// ========================================
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Service interface for News Comment management operations.
 * <p>
 * Provides business logic for creating, retrieving, updating, and deleting
 * newsapp comments. Supports public, user, and admindashboard access levels
 * with
 * appropriate filtering and validation.
 * </p>
 * 
 * <h3>Access Levels:</h3>
 * <ul>
 * <li><b>Public API</b> - Only approved comments visible</li>
 * <li><b>User API</b> - User's own comments (all statuses)</li>
 * <li><b>Admin API</b> - Full access with filters and moderation</li>
 * </ul>
 * 
 * @author MMVA Team
 * @version 1.0
 * @since 2024-06-01
 * @see NewsCommentServiceImpl
 */
public interface NewsCommentService {

    // =========================
    // Public API - Approved Comments Only
    // =========================

    /**
     * Retrieves approved replies for a parent comment.
     *
     * @param parentCommentId the parent comment UUID
     * @return list of approved reply comments
     */
    List<NewsCommentResponseDto> getApprovedReplies(UUID parentCommentId);

    /**
     * Retrieves approved comments for a newsapp newsapp with pagination.
     *
     * @param newsId   the newsapp newsapp UUID
     * @param pageable pagination parameters
     * @return paginated approved comments
     */
    Page<NewsCommentResponseDto> getApprovedCommentsByNewsId(UUID newsId, Pageable pageable);

    /**
     * Retrieves all approved comments for a newsapp newsapp.
     *
     * @param newsId the newsapp newsapp UUID
     * @return list of approved comments
     */
    List<NewsCommentResponseDto> getApprovedCommentsByNewsId(UUID newsId);

    /**
     * Gets the count of approved comments for a newsapp newsapp.
     *
     * @param newsId the newsapp newsapp UUID
     * @return count of approved comments
     */
    Long getApprovedCommentCountByNewsId(UUID newsId);

    /**
     * Retrieves a threaded view of approved comments for a newsapp newsapp.
     *
     * @param newsId the newsapp newsapp UUID
     * @return hierarchical list of approved comments with replies
     */
    List<NewsCommentResponseDto> getApprovedCommentThreadForNews(UUID newsId);

    // =========================
    // User API - User's Own Comments
    // =========================

    /**
     * Retrieves all comments by a specific user.
     *
     * @param userId the user UUID
     * @return list of user's comments (all statuses)
     */
    List<NewsCommentResponseDto> getMyComments(UUID userId);

    /**
     * Retrieves user's comments with pagination.
     *
     * @param userId   the user UUID
     * @param pageable pagination parameters
     * @return paginated user comments
     */
    Page<NewsCommentResponseDto> getMyComments(UUID userId, Pageable pageable);

    // =========================
    // Admin API - Full Access
    // =========================

    /**
     * Retrieves comments with optional filters (admindashboard only).
     *
     * @param status   filter by status (PENDING, APPROVED, REJECTED, DELETED)
     * @param newsId   filter by newsapp newsapp ID
     * @param userId   filter by user ID
     * @param pageable pagination parameters
     * @return paginated filtered comments
     */
    Page<NewsCommentResponseDto> getCommentsWithFilters(String status, UUID newsId, UUID userId, Pageable pageable);

    /**
     * Retrieves all comments with pagination (admindashboard only).
     *
     * @param pageable pagination parameters
     * @return paginated comments
     */
    Page<NewsCommentResponseDto> getAllComments(Pageable pageable);

    // =========================
    // Legacy/Shared Read Methods
    // =========================

    /**
     * Retrieves replies for a parent comment (includes all statuses).
     *
     * @param parentCommentId the parent comment UUID
     * @return list of reply comments
     * @deprecated Use {@link #getApprovedReplies(UUID)} for public access
     */
    @Deprecated
    List<NewsCommentResponseDto> getReplies(UUID parentCommentId);

    /**
     * Retrieves full comment thread for a newsapp newsapp (includes all statuses).
     *
     * @param newsId the newsapp newsapp UUID
     * @return hierarchical list of comments with replies
     * @deprecated Use {@link #getApprovedCommentThreadForNews(UUID)} for public
     *             access
     */
    @Deprecated
    List<NewsCommentResponseDto> getCommentThreadForNews(UUID newsId);

    /**
     * Gets list of user IDs who liked a comment.
     *
     * @param commentId the comment UUID
     * @return list of user UUIDs
     */
    List<UUID> getUsersWhoLikedComment(UUID commentId);

    // =========================
    // Engagement Operations
    // =========================

    /**
     * Records a like on a comment.
     *
     * @param commentId the comment UUID
     * @param userId    the user UUID liking the comment
     */
    void likeComment(UUID commentId, UUID userId);

    /**
     * Records a dislike on a comment.
     *
     * @param commentId the comment UUID
     * @param userId    the user UUID disliking the comment
     */
    void dislikeComment(UUID commentId, UUID userId);

    /**
     * Gets the like count for a comment.
     *
     * @param commentId the comment UUID
     * @return number of likes
     */
    long getLikeCount(UUID commentId);

    /**
     * Reports a comment for moderation.
     *
     * @param commentId the comment UUID
     * @param userId    the user UUID reporting
     * @param reason    the reason for reporting
     */
    void reportComment(UUID commentId, UUID userId, String reason);

    /**
     * Gets the report count for a comment.
     *
     * @param commentId the comment UUID
     * @return number of reports
     */
    long getReportCount(UUID commentId);

    // =========================
    // Create Operations
    // =========================

    /**
     * Adds a new comment to a newsapp newsapp.
     *
     * @param dto the comment creation request
     * @return the created comment response
     * @throws AppUserNotFoundException   if the user does not exist
     * @throws RateLimitExceededException if rate limit is exceeded
     */
    NewsCommentResponseDto addComment(NewsCommentRequestDto dto);

    // =========================
    // Read Operations
    // =========================

    /**
     * Retrieves a comment by its unique identifier.
     *
     * @param id the comment UUID
     * @return the comment response
     * @throws NewsCommentNotFoundException if comment not found
     */
    NewsCommentResponseDto getCommentById(UUID id);

    /**
     * Retrieves comments for a newsapp newsapp (all statuses).
     *
     * @param newsId the newsapp newsapp UUID
     * @return list of comments
     */
    List<NewsCommentResponseDto> getCommentsByNewsId(UUID newsId);

    /**
     * Retrieves comments for a newsapp newsapp with pagination.
     *
     * @param newsId   the newsapp newsapp UUID
     * @param pageable pagination parameters
     * @return paginated comments
     */
    Page<NewsCommentResponseDto> getCommentsByNewsId(UUID newsId, Pageable pageable);

    /**
     * Retrieves comments by user ID.
     *
     * @param userId the user UUID
     * @return list of user's comments
     */
    List<NewsCommentResponseDto> getCommentsByUserId(UUID userId);

    /**
     * Gets the total comment count for a newsapp newsapp.
     *
     * @param newsId the newsapp newsapp UUID
     * @return count of comments
     */
    Long getCommentCountByNewsId(UUID newsId);

    // =========================
    // Update Operations
    // =========================

    /**
     * Updates a comment with ownership validation.
     *
     * @param id     the comment UUID
     * @param userId the user UUID (for ownership validation)
     * @param dto    the update data
     * @return the updated comment response
     * @throws NewsCommentNotFoundException if comment not found
     * @throws UnauthorizedAccessException  if user doesn't own the comment
     */
    NewsCommentResponseDto updateComment(UUID id, UUID userId, NewsCommentUpdateDto dto);

    /**
     * Updates a comment without ownership validation.
     *
     * @param id  the comment UUID
     * @param dto the update data
     * @return the updated comment response
     * @throws NewsCommentNotFoundException if comment not found
     * @deprecated Use {@link #updateComment(UUID, UUID, NewsCommentUpdateDto)} with
     *             user validation
     */
    @Deprecated
    NewsCommentResponseDto updateComment(UUID id, NewsCommentUpdateDto dto);

    // =========================
    // Delete Operations
    // =========================

    /**
     * Removes a comment by user (soft delete with ownership validation).
     *
     * @param commentId the comment UUID
     * @param userId    the user UUID (must own the comment)
     * @return the updated comment and reply counts for the news article
     * @throws NewsCommentNotFoundException if comment not found
     * @throws UnauthorizedAccessException  if user doesn't own the comment
     */
    NewsCommentCountDto removeCommentByUser(UUID commentId, UUID userId);

    /**
     * Removes a comment by admin (soft delete without ownership validation).
     *
     * @param commentId the comment UUID
     * @param adminId   the admin UUID performing deletion
     * @return the updated comment and reply counts for the news article
     * @throws NewsCommentNotFoundException if comment not found
     */
    NewsCommentCountDto removeCommentByAdmin(UUID commentId, UUID adminId);

    /**
     * Internal soft delete implementation.
     *
     * @param commentId the comment UUID
     * @param deletedBy the UUID of the actor deleting
     * @return the updated comment and reply counts for the news article
     * @throws NewsCommentNotFoundException if comment not found
     */
    NewsCommentCountDto softDeleteComment(UUID commentId, UUID deletedBy);

    // =========================
    // Admin Moderation
    // =========================

    /**
     * Approves a comment for public visibility.
     *
     * @param id      the comment UUID
     * @param adminId the admindashboard UUID performing approval
     * @return the approved comment response
     * @throws NewsCommentNotFoundException if comment not found
     */
    NewsCommentResponseDto approveComment(UUID id, UUID adminId);

    /**
     * Rejects a comment (removes from public visibility).
     *
     * @param id      the comment UUID
     * @param adminId the admindashboard UUID performing rejection
     * @return the rejected comment response
     * @throws NewsCommentNotFoundException if comment not found
     */
    NewsCommentResponseDto rejectComment(UUID id, UUID adminId);

    /**
     * Gets the total comment count across all news articles.
     *
     * @return the total comment count
     */
    Long getTotalCommentCount();

    /**
     * Gets the total comment count within a date range.
     *
     * @param start the start date
     * @param end   the end date
     * @return the total comment count
     */
    Long getTotalCommentCountBetween(Instant start, Instant end);
}
