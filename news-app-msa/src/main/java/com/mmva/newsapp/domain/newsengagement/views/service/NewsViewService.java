package com.mmva.newsapp.domain.newsengagement.views.service;

// ========================================
// DTO Imports
// ========================================
import com.mmva.newsapp.domain.newsengagement.views.dto.NewsViewRequestDto;
import com.mmva.newsapp.domain.newsengagement.views.dto.NewsViewResponseDto;

// ========================================
// Exception Imports
// ========================================
import com.mmva.newsapp.domain.newsengagement.views.exception.NewsViewNotFoundException;

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
 * Service interface for managing newsapp view tracking and analytics.
 * 
 * <p>
 * Provides operations for:
 * </p>
 * <ul>
 * <li>Recording user views on newsapp articles</li>
 * <li>Retrieving view records by newsapp, user, or ID</li>
 * <li>Counting views for analytics</li>
 * <li>Managing view records (delete)</li>
 * </ul>
 * 
 * <p>
 * <strong>Design Note:</strong> Views are EVENTS, not STATES. Unlike likes
 * (binary: liked/not liked), a user can view the same article multiple times.
 * Each view is a discrete event for analytics and tracking. The
 * {@link #addView(NewsViewRequestDto)} method always creates a new record.
 * </p>
 * 
 * @see NewsViewServiceImpl
 */
public interface NewsViewService {

    /**
     * Adds a new view for a newsapp article.
     * 
     * <p>
     * <strong>Note:</strong> This method always creates a new view record.
     * Views are events (not states like likes), so multiple views per user
     * per article are allowed and expected for analytics purposes.
     * </p>
     *
     * @param dto the view request containing newsapp ID and optional user ID
     * @return the created view response with updatedViewCount
     */
    NewsViewResponseDto addView(NewsViewRequestDto dto);

    /**
     * Retrieves a view record by its ID.
     *
     * @param id the view record ID
     * @return the view response
     * @throws NewsViewNotFoundException if the view record is not found
     */
    NewsViewResponseDto getViewById(Long id);

    /**
     * Retrieves all views for a specific newsapp article.
     *
     * @param newsId the newsapp article ID
     * @return list of view responses
     */
    List<NewsViewResponseDto> getViewsByNewsId(UUID newsId);

    /**
     * Retrieves all views by a specific user.
     *
     * @param userId the user ID
     * @return list of view responses
     */
    List<NewsViewResponseDto> getViewsByUserId(UUID userId);

    /**
     * Retrieves all views with pagination.
     *
     * @param pageable the pagination information
     * @return paginated view responses
     */
    Page<NewsViewResponseDto> getAllViews(Pageable pageable);

    /**
     * Gets the total view count for a newsapp article.
     *
     * @param newsId the newsapp article ID
     * @return the total view count
     */
    Long getViewCountByNewsId(UUID newsId);

    /**
     * Gets the view count for a newsapp article by a specific user.
     *
     * @param newsId the newsapp article ID
     * @param userId the user ID
     * @return the view count
     */
    Long getViewCountByNewsIdAndUserId(UUID newsId, UUID userId);

    /**
     * Removes a view record by admin.
     * 
     * <p>
     * <strong>Note:</strong> This is an admin-only operation for cleanup.
     * Decrements the view counter to maintain consistency.
     * Throws an exception if the view record does not exist.
     * </p>
     *
     * @param viewId the view record ID to remove
     * @return the updated view count for the news article
     * @throws NewsViewNotFoundException if the view record is not found
     */
    Long removeViewByAdmin(Long viewId);

    /**
     * Gets the total view count across all news articles.
     *
     * @return the total view count
     */
    Long getTotalViewCount();

    /**
     * Gets the total view count within a date range.
     *
     * @param start the start date
     * @param end   the end date
     * @return the total view count
     */
    Long getTotalViewCountBetween(Instant start, Instant end);
}
