package com.mmva.newsapp.domain.newsengagement.shares.service;

// ========================================
// DTO Imports
// ========================================
import com.mmva.newsapp.domain.newsengagement.shares.dto.NewsShareRequestDto;
import com.mmva.newsapp.domain.newsengagement.shares.dto.NewsShareResponseDto;

// ========================================
// Exception Imports
// ========================================
import com.mmva.newsapp.domain.newsengagement.shares.exception.NewsShareNotFoundException;

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
 * Service interface for managing newsapp shares and social newsengagement
 * tracking.
 * 
 * <p>
 * Provides operations for:
 * </p>
 * <ul>
 * <li>Recording shares on newsapp articles</li>
 * <li>Retrieving share records by newsapp, user, or ID</li>
 * <li>Counting shares for analytics</li>
 * <li>Checking if a user has shared specific newsapp</li>
 * </ul>
 * 
 * <p>
 * <strong>Design Note:</strong> Shares are EVENTS, not STATES. Unlike likes
 * (binary: liked/not liked), a user can share the same article multiple times
 * to different platforms or at different times. Each share is a discrete event.
 * </p>
 * 
 * @see NewsShareServiceImpl
 */
public interface NewsShareService {

    /**
     * Adds a share for a newsapp article.
     * 
     * <p>
     * <strong>Note:</strong> This method always creates a new share record.
     * Shares are events (not states like likes), so multiple shares per user
     * per article are allowed and expected.
     * </p>
     *
     * @param dto the share request containing newsapp ID and user ID
     * @return the newly created share response with updatedShareCount
     */
    NewsShareResponseDto addShare(NewsShareRequestDto dto);

    /**
     * Retrieves a share record by its ID.
     *
     * @param id the share record ID
     * @return the share response
     * @throws NewsShareNotFoundException if the share record is not found
     */
    NewsShareResponseDto getShareById(Long id);

    /**
     * Retrieves all shares for a specific newsapp article.
     *
     * @param newsId the newsapp article ID
     * @return list of share responses
     */
    List<NewsShareResponseDto> getSharesByNewsId(UUID newsId);

    /**
     * Retrieves all shares by a specific user.
     *
     * @param userId the user ID
     * @return list of share responses
     */
    List<NewsShareResponseDto> getSharesByUserId(UUID userId);

    /**
     * Retrieves all shares with pagination.
     *
     * @param pageable the pagination information
     * @return paginated share responses
     */
    Page<NewsShareResponseDto> getAllShares(Pageable pageable);

    /**
     * Gets the total share count for a newsapp article.
     *
     * @param newsId the newsapp article ID
     * @return the total share count
     */
    Long getShareCountByNewsId(UUID newsId);

    /**
     * Gets the total share count by a specific user.
     *
     * @param userId the user ID
     * @return the total share count
     */
    Long getShareCountByUserId(UUID userId);

    /**
     * Checks if a user has shared a specific newsapp article.
     *
     * @param newsId the newsapp article ID
     * @param userId the user ID
     * @return true if the user has shared the newsapp, false otherwise
     */
    boolean hasUserShared(UUID newsId, UUID userId);

    /**
     * Gets share statistics grouped by platform for a news article.
     * 
     * <p>
     * Returns analytics data showing which social media platforms users are
     * sharing the article to, ordered by popularity (most shares first).
     * </p>
     * 
     * @param newsId the news article ID
     * @return List of [platform, count] pairs ordered by count descending
     */
    List<Object[]> getSharesByPlatform(UUID newsId);

    /**
     * Removes a share record by admin.
     * 
     * <p>
     * <strong>Note:</strong> This is an admin-only operation for cleanup.
     * Decrements the share counter to maintain consistency.
     * Throws an exception if the share record does not exist.
     * </p>
     *
     * @param shareId the share record ID to remove
     * @return the updated share count for the news article
     * @throws NewsShareNotFoundException if the share record is not found
     */
    Long removeShareByAdmin(Long shareId);

    /**
     * Gets the total share count across all news articles.
     *
     * @return the total share count
     */
    Long getTotalShareCount();

    /**
     * Gets the total share count within a date range.
     *
     * @param start the start date
     * @param end   the end date
     * @return the total share count
     */
    Long getTotalShareCountBetween(Instant start, Instant end);
}
