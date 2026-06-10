package com.mmva.newsapp.domain.newsengagement.likes.service;

// ========================================
// DTO Imports
// ========================================
import com.mmva.newsapp.domain.newsengagement.likes.dto.NewsLikeRequestDto;
import com.mmva.newsapp.domain.newsengagement.likes.dto.NewsLikeResponseDto;

// ========================================
// Exception Imports
// ========================================
import com.mmva.newsapp.domain.newsengagement.likes.exception.NewsLikeNotFoundException;

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
 * Service interface for managing news likes and engagement tracking.
 * 
 * <p>
 * Provides operations for:
 * </p>
 * <ul>
 * <li>Adding and removing likes on news articles</li>
 * <li>Retrieving like records by news, user, or ID</li>
 * <li>Counting likes for analytics</li>
 * <li>Checking if a user has liked specific news</li>
 * </ul>
 * 
 * @author MMVA Team
 * @since 1.0.0
 * @see NewsLikeServiceImpl
 */
public interface NewsLikeService {

    /**
     * Adds a like to a news article.
     * 
     * <p>
     * <strong>Idempotent:</strong> If user has already liked this news,
     * returns the existing like instead of throwing an exception.
     * </p>
     *
     * @param dto the like request containing news ID and user ID
     * @return the created or existing like response
     */
    NewsLikeResponseDto addLike(NewsLikeRequestDto dto);

    /**
     * Retrieves a like record by its ID.
     *
     * @param id the like record ID
     * @return the like response
     * @throws NewsLikeNotFoundException if like record not found
     */
    NewsLikeResponseDto getLikeById(Long id);

    /**
     * Retrieves all likes for a specific news article.
     *
     * @param newsId the news article ID
     * @return list of like responses
     */
    List<NewsLikeResponseDto> getLikesByNewsId(UUID newsId);

    /**
     * Retrieves all likes by a specific user.
     *
     * @param userId the user ID
     * @return list of like responses
     */
    List<NewsLikeResponseDto> getLikesByUserId(UUID userId);

    /**
     * Retrieves all likes with pagination.
     *
     * @param pageable the pagination information
     * @return paginated like responses
     */
    Page<NewsLikeResponseDto> getAllLikes(Pageable pageable);

    /**
     * Gets the total like count for a news article.
     *
     * @param newsId the news article ID
     * @return the total like count
     */
    Long getLikeCountByNewsId(UUID newsId);

    /**
     * Checks if a user has liked a specific news article.
     *
     * @param newsId the news article ID
     * @param userId the user ID
     * @return true if the user has liked the news, false otherwise
     */
    boolean hasUserLikedNews(UUID newsId, UUID userId);

    /**
     * Removes a like by admin.
     *
     * @param likeId the like record ID to remove
     * @return the updated like count for the news article
     * @throws NewsLikeNotFoundException if like record not found
     */
    Long removeLikeByAdmin(Long likeId);

    /**
     * Removes a like by user.
     * 
     * <p>
     * <strong>Idempotent:</strong> If like doesn't exist, returns current count
     * without throwing an exception.
     * </p>
     *
     * @param newsId the news article ID
     * @param userId the user ID
     * @return the updated like count for the news article
     */
    Long removeLikeByUser(UUID newsId, UUID userId);

    /**
     * Gets the total like count across all news articles.
     *
     * @return the total like count
     */
    Long getTotalLikeCount();

    /**
     * Gets the total like count within a date range.
     *
     * @param start the start date
     * @param end   the end date
     * @return the total like count
     */
    Long getTotalLikeCountBetween(Instant start, Instant end);
}
