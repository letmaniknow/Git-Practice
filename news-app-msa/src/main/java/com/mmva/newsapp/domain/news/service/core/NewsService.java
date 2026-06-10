package com.mmva.newsapp.domain.news.service.core;

import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import com.mmva.newsapp.domain.news.dto.core.NewsCreateRequestDto;
import com.mmva.newsapp.domain.news.dto.core.NewsCreateResponseDto;
import com.mmva.newsapp.domain.news.dto.core.NewsMediaFileRequestDto;
import com.mmva.newsapp.domain.news.dto.core.NewsMediaUploadResponseDto;
import com.mmva.newsapp.domain.news.dto.core.ThumbnailResponseDto;
import com.mmva.newsapp.domain.news.exception.core.NewsNotFoundException;
import com.mmva.newsapp.domain.news.audit.model.NewsAuditLog;
import com.mmva.newsapp.domain.news.model.core.NewsMasterEntity;
import com.mmva.newsapp.infrastructure.common.exception.InvalidRequestException;
import com.mmva.newsapp.infrastructure.common.exception.ResourceNotFoundException;
import com.mmva.newsapp.infrastructure.common.exception.UnauthorizedAccessException;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Service interface for News newsapp management.
 * <p>
 * This interface defines operations for creating, reading, updating, and
 * deleting newsapp articles,
 * as well as workflow management, search, and public access methods.
 * </p>
 * 
 * @author MMVA Team
 * @version 1.0
 * @since 2024-06-01
 */
public interface NewsService {

    // =========================
    // CRUD Operations
    // =========================

    /**
     * Creates a new newsapp newsapp.
     *
     * @param request the newsapp creation request containing newsapp details and
     *                media
     * @return the created newsapp newsapp response
     * @throws InvalidRequestException if creation fails
     */
    NewsCreateResponseDto createNews(NewsCreateRequestDto request);

    /**
     * Retrieves a newsapp newsapp by its unique identifier.
     *
     * @param id the newsapp newsapp ID
     * @return the newsapp newsapp response
     * @throws NewsNotFoundException if newsapp not found
     */
    NewsCreateResponseDto getNewsById(String id);

    /**
     * Retrieves all newsapp articles with pagination.
     *
     * @param pageable pagination information
     * @return paginated list of newsapp articles
     */
    Page<NewsCreateResponseDto> getAllNews(Pageable pageable);

    /**
     * Retrieves only active (non-deleted) newsapp articles with pagination.
     * Use for public-facing APIs.
     *
     * @param pageable pagination information
     * @return paginated list of active newsapp articles
     */
    Page<NewsCreateResponseDto> getActiveNews(Pageable pageable);

    /**
     * Retrieves only soft-deleted newsapp articles with pagination.
     * Use for admindashboard restore listing.
     *
     * @param pageable pagination information
     * @return paginated list of deleted newsapp articles
     */
    Page<NewsCreateResponseDto> getDeletedNews(Pageable pageable);

    /**
     * Updates an existing newsapp newsapp.
     *
     * @param id      the newsapp newsapp ID to update
     * @param request the updated newsapp data
     * @return the updated newsapp newsapp response
     * @throws NewsNotFoundException if newsapp not found
     */
    NewsCreateResponseDto updateNews(String id, NewsCreateRequestDto request);

    /**
     * Soft deletes a newsapp newsapp by its ID (recoverable).
     * Files are preserved for potential restore.
     * The deletion is tracked with an audit log entry.
     * This is NOT permanent - use {@link #permanentDelete(String)} for irreversible
     * deletion.
     *
     * @param id        the newsapp newsapp ID to soft delete
     * @param deletedBy the UUID of the admin performing the deletion
     * @throws NewsNotFoundException if newsapp not found
     */
    void softDeleteNews(String id, UUID deletedBy);

    /**
     * Permanently deletes a newsapp newsapp and moves associated files to backup.
     * This operation is irreversible for the database record.
     * Media and thumbnail files are moved to backup folder for recovery if needed.
     *
     * @param id the newsapp newsapp ID to permanently delete
     * @throws NewsNotFoundException if newsapp not found
     */
    void permanentDelete(String id);

    /**
     * Saves a newsapp newsapp as a draft.
     *
     * @param request the draft newsapp data
     * @return the saved draft newsapp response
     */
    NewsCreateResponseDto saveDraftNews(NewsCreateRequestDto request);

    /**
     * Clones an existing news article creating a new DRAFT copy for editing.
     * 
     * <p>
     * <strong>Fields Copied:</strong> content, media, metadata, configuration
     * </p>
     *
     * <p>
     * <strong>Fields Reset:</strong> status to DRAFT, engagement metrics to 0,
     * publishing dates cleared, ID generated new for independent lifecycle
     * </p>
     *
     * @param sourceNewsId the ID of the news article to clone
     * @return the cloned news article (DRAFT status, ready for editing)
     * @throws InvalidRequestException if source news not found
     */
    NewsCreateResponseDto cloneNews(String sourceNewsId, java.util.UUID adminUserId);

    // =========================
    // Workflow Management
    // =========================

    /**
     * Updates the workflow status of a newsapp newsapp.
     *
     * @param newsId    the newsapp newsapp ID
     * @param newStatus the new workflow status
     * @param userId    the ID of the user making the change
     * @throws UnauthorizedAccessException if user lacks
     *                                     permission
     */
    void updateWorkflowStatus(String newsId, String newStatus, String userId);

    /**
     * Retrieves newsapp articles by workflow status.
     *
     * @param workflowStatus the workflow status to filter by
     * @param pageable       pagination information
     * @return paginated list of newsapp articles with the specified status
     */
    Page<NewsCreateResponseDto> getNewsByWorkflowStatus(String workflowStatus, Pageable pageable);

    /**
     * Retrieves all available workflow status options.
     * <p>
     * Returns the list of all valid WorkflowStatus enum values.
     * This is used by the frontend to populate dropdowns and filter options.
     * </p>
     *
     * @return list of all workflow status values
     */
    List<String> getAvailableWorkflowStatuses();

    /**
     * Schedules a newsapp newsapp for future publication.
     *
     * @param id              the newsapp newsapp ID
     * @param publishDateTime the scheduled publication date/time (ISO 8601)
     * @param scheduledBy     the UUID of the admin performing the scheduling
     * @return the updated newsapp response with scheduling information
     */
    NewsCreateResponseDto schedulePublish(String id, String publishDateTime, UUID scheduledBy);

    /**
     * Archives a newsapp newsapp (soft-deletes by marking deletedAt).
     *
     * @param id         the newsapp newsapp ID
     * @param archivedBy the UUID of the admin performing the archive
     */
    void archiveNews(String id, UUID archivedBy);

    /**
     * Unarchives a newsapp newsapp (restores by clearing deletedAt).
     *
     * @param id           the newsapp newsapp ID
     * @param unarchivedBy the UUID of the admin performing the unarchive
     */
    void unarchiveNews(String id, UUID unarchivedBy);

    /**
     * Restores a soft-deleted newsapp newsapp.
     *
     * @param id the newsapp newsapp ID
     */
    void restoreNews(String id);

    /**
     * Pins or unpins a newsapp newsapp as featured.
     *
     * @param id       the newsapp newsapp ID
     * @param featured true to pin, false to unpin
     */
    void pinNews(String id, boolean featured);

    // =========================
    // Search & Filter Operations
    // =========================

    /**
     * Searches newsapp articles with various filter criteria.
     *
     * @param query      text search query
     * @param categoryId newscategory ID filter
     * @param fromDate   date range start (ISO 8601)
     * @param toDate     date range end (ISO 8601)
     * @param sort       sort specification
     * @param pageable   pagination information
     * @return paginated search results
     */
    Page<NewsCreateResponseDto> searchNews(String query, String categoryId, String fromDate,
            String toDate, String sort, Pageable pageable);

    /**
     * Retrieves newsapp articles by newscategory.
     *
     * @param categoryId the newscategory ID
     * @param pageable   pagination information
     * @return paginated list of newsapp articles in the newscategory
     */
    Page<NewsCreateResponseDto> getNewsByCategory(String categoryId, Pageable pageable);

    /**
     * Retrieves newsapp articles by author.
     *
     * @param authorId the author ID
     * @param pageable pagination information
     * @return paginated list of newsapp articles by the author
     */
    Page<NewsCreateResponseDto> getNewsByAuthor(String authorId, Pageable pageable);

    /**
     * Retrieves a newsapp newsapp by its URL slug.
     *
     * @param slug the newsapp newsapp slug
     * @return the newsapp newsapp response
     * @throws NewsNotFoundException if newsapp not found
     */
    NewsCreateResponseDto getNewsBySlug(String slug);

    /**
     * Retrieves related newsapp articles.
     *
     * @param newsId the reference newsapp newsapp ID
     * @param limit  maximum number of related articles
     * @param sort   sort specification
     * @return list of related newsapp articles
     */
    List<NewsCreateResponseDto> getRelatedNews(String newsId, int limit, String sort);

    /**
     * Retrieves related newsapp with default sort order.
     *
     * @param newsId the reference newsapp newsapp ID
     * @param limit  maximum number of related articles
     * @return list of related newsapp articles
     */
    List<NewsCreateResponseDto> getRelatedNews(String newsId, int limit);

    /**
     * Retrieves trending newsapp articles.
     *
     * @param pageable pagination information
     * @return paginated list of trending newsapp
     */
    Page<NewsCreateResponseDto> getTrendingNews(Pageable pageable);

    /**
     * Retrieves top trending newsapp with a limit.
     *
     * @param limit maximum number of trending articles
     * @return list of trending newsapp articles
     */
    List<NewsCreateResponseDto> getTrendingNews(int limit);

    // =========================
    // Bulk Operations
    // =========================

    /**
     * Publishes multiple newsapp articles at once.
     *
     * @param ids list of newsapp newsapp IDs to publish
     */
    void bulkPublish(List<String> ids);

    /**
     * Soft-deletes multiple newsapp articles at once (recoverable).
     * Files are preserved for potential restore per article.
     * This is NOT permanent - use individual {@link #permanentDelete(String)} for
     * irreversible deletion.
     *
     * @param ids       list of newsapp newsapp IDs to soft delete
     * @param deletedBy the ID of the admindashboard performing the deletion
     */
    void bulkSoftDeleteNews(List<String> ids, UUID deletedBy);

    // =========================
    // Media Operations
    // =========================

    /**
     * Retrieves a media file by filename.
     *
     * @param filename the media filename
     * @return the media file request containing resource and content type
     * @throws ResourceNotFoundException if file not found
     */
    NewsMediaFileRequestDto getMediaFile(String filename);

    /**
     * Uploads a media file and returns its details including accessible URL.
     * Generates a unique filename and stores the file in the configured media
     * folder.
     *
     * @param file the multipart file to upload
     * @return the upload response containing filename, URL, and metadata
     * @throws InvalidRequestException if upload fails
     */
    NewsMediaUploadResponseDto uploadMediaFile(MultipartFile file);

    /**
     * Deletes a media file by filename.
     *
     * @param filename the media filename to delete
     * @return true if file was deleted, false if file did not exist
     * @throws InvalidRequestException if deletion fails
     */
    boolean deleteMediaFileByName(String filename);

    // =========================
    // Thumbnail Operations
    // =========================

    /**
     * Retrieves a thumbnail file by filename.
     *
     * @param filename the thumbnail filename
     * @return the media file request containing resource and content type
     * @throws ResourceNotFoundException if file not found
     */
    NewsMediaFileRequestDto getThumbnailFile(String filename);

    /**
     * Retrieves a card size image file by filename.
     * Card images are 400x225 pixels (16:9) for news list views - Inshorts style.
     *
     * @param filename the card image filename
     * @return the media file request containing resource and content type
     * @throws ResourceNotFoundException if file not found
     */
    NewsMediaFileRequestDto getCardImageFile(String filename);

    /**
     * Retrieves a hero size image file by filename.
     * Hero images are 800x450 pixels (16:9) for featured news - Inshorts style.
     *
     * @param filename the hero image filename
     * @return the media file request containing resource and content type
     * @throws ResourceNotFoundException if file not found
     */
    NewsMediaFileRequestDto getHeroImageFile(String filename);

    /**
     * Uploads a custom thumbnail file.
     * Used when user provides a specific thumbnail instead of auto-generating.
     *
     * @param file              the thumbnail image file
     * @param originalMediaName the associated media filename for naming consistency
     * @return the thumbnail response with URL and metadata
     * @throws IOException if upload fails
     */
    ThumbnailResponseDto uploadThumbnail(MultipartFile file, String originalMediaName) throws IOException;

    /**
     * Generates a thumbnail from a media file.
     * Auto-detects if image (resize) or video (extract frame).
     *
     * @param mediaFile the source image or video file
     * @return the thumbnail response with URL and metadata
     * @throws IOException if generation fails
     */
    ThumbnailResponseDto generateThumbnail(MultipartFile mediaFile) throws IOException;

    /**
     * Generates a thumbnail from an already-saved media file path.
     *
     * @param mediaFilePath    path to the saved media file
     * @param originalFilename original filename for naming
     * @return the thumbnail response with URL and metadata
     * @throws IOException if generation fails
     */
    ThumbnailResponseDto generateThumbnailFromPath(String mediaFilePath, String originalFilename) throws IOException;

    /**
     * Deletes a thumbnail file by filename.
     *
     * @param filename the thumbnail filename to delete
     * @return true if deleted, false if not found
     */
    boolean deleteThumbnail(String filename);

    /**
     * Exports newsapp articles in the specified format.
     *
     * @param format   the export format (e.g., "csv")
     * @param pageable pagination information
     * @return the exported file as a Resource
     */
    Resource exportNews(String format, Pageable pageable);

    // =========================
    // Analytics & Statistics
    // =========================

    /**
     * Retrieves version history for a newsapp newsapp.
     *
     * @param id the newsapp newsapp ID
     * @return list of historical versions
     */
    List<NewsCreateResponseDto> getVersionHistory(String id);

    /**
     * Retrieves audit log for a newsapp newsapp.
     *
     * @param id the newsapp newsapp ID
     * @return list of audit log entries
     */
    List<String> getAuditLog(String id);

    /**
     * Retrieves analytics for a newsapp newsapp.
     *
     * @param id the newsapp newsapp ID
     * @return analytics data
     */
    Object getAnalytics(String id);

    /**
     * Retrieves statistics for a newsapp newsapp.
     *
     * @param id the newsapp newsapp ID
     * @return statistics data including view/like/share counts
     */
    Object getNewsStatistics(String id);

    // =========================
    // Audit Logging
    // =========================

    /**
     * Logs an audit action for a newsapp newsapp.
     *
     * @param newsId  the newsapp newsapp ID
     * @param action  the action performed
     * @param details additional details about the action
     * @param actorId the ID of the user who performed the action
     */
    void logAction(UUID newsId, String action, String details, UUID actorId);

    /**
     * Retrieves audit logs for a newsapp newsapp.
     *
     * @param newsId the newsapp newsapp ID
     * @return list of audit log entries
     */
    List<NewsAuditLog> findAuditLogsByNewsId(UUID newsId);

    // =========================
    // Public Access Methods (Published & Active Only)
    // =========================

    /**
     * Retrieves all published and active newsapp for public users.
     *
     * @param pageable pagination information
     * @return paginated list of published newsapp
     */
    Page<NewsCreateResponseDto> getPublishedActiveNews(Pageable pageable);

    /**
     * Retrieves only published newsapp articles.
     *
     * @param pageable pagination information
     * @return paginated list of published newsapp
     */
    Page<NewsCreateResponseDto> getPublishedNews(Pageable pageable);

    /**
     * Retrieves a specific published newsapp by ID for public users.
     *
     * @param id the newsapp newsapp ID
     * @return the published newsapp newsapp
     * @throws NewsNotFoundException if not found or not
     *                               published
     */
    NewsCreateResponseDto getPublishedNewsById(String id);

    /**
     * Retrieves a specific published newsapp by slug for public users.
     *
     * @param slug the newsapp newsapp slug
     * @return the published newsapp newsapp
     * @throws NewsNotFoundException if not found or not
     *                               published
     */
    NewsCreateResponseDto getPublishedNewsBySlug(String slug);

    /**
     * Retrieves published newsapp by newscategory for public users.
     *
     * @param categoryId the newscategory ID
     * @param pageable   pagination information
     * @return paginated list of published newsapp in the newscategory
     */
    Page<NewsCreateResponseDto> getPublishedNewsByCategory(String categoryId, Pageable pageable);

    /**
     * Searches published newsapp for public users.
     *
     * @param query      text search query
     * @param categoryId newscategory ID filter
     * @param pageable   pagination information
     * @return paginated search results
     */
    Page<NewsCreateResponseDto> searchPublishedNews(String query, String categoryId, Pageable pageable);

    /**
     * Retrieves trending published newsapp for public users.
     *
     * @param pageable pagination information
     * @return paginated list of trending published newsapp
     */
    Page<NewsCreateResponseDto> getTrendingPublishedNews(Pageable pageable);

    /**
     * Retrieves featured published newsapp for public users.
     *
     * @param pageable pagination information
     * @return paginated list of featured published newsapp
     */
    Page<NewsCreateResponseDto> getFeaturedPublishedNews(Pageable pageable);

    /**
     * Retrieves related published newsapp for public users.
     *
     * @param newsId the reference newsapp newsapp ID
     * @param limit  maximum number of related articles
     * @return list of related published newsapp
     */
    List<NewsCreateResponseDto> getRelatedPublishedNews(String newsId, int limit);

    /**
     * Retrieves published newsapp by author for public users.
     *
     * @param authorId the author ID
     * @param pageable pagination information
     * @return paginated list of published newsapp by the author
     */
    Page<NewsCreateResponseDto> getPublishedNewsByAuthor(String authorId, Pageable pageable);

    // =========================
    // Recommendations
    // =========================

    /**
     * Generates personalized newsapp recommendations for a user.
     *
     * @param userId   the user ID
     * @param pageable pagination information
     * @return list of recommended newsapp articles
     */
    List<NewsCreateResponseDto> getUserRecommendations(UUID userId, Pageable pageable);

    // =========================
    // Presentation Methods (Platform-Specific Content Processing)
    // =========================

    /**
     * Retrieves a news article optimized for mobile display.
     * Processes content using mobile-specific formatting and includes all
     * engagement metrics.
     *
     * @param newsId the news article ID
     * @return the mobile-optimized news response with processed content
     * @throws NewsNotFoundException if news not found or not published
     */
    NewsCreateResponseDto getNewsForMobile(Long newsId);

    /**
     * Retrieves a news article optimized for web display.
     * Processes content using HTML formatting and includes all engagement metrics.
     *
     * @param newsId the news article ID
     * @return the web-optimized news response with processed content
     * @throws NewsNotFoundException if news not found or not published
     */
    NewsCreateResponseDto getNewsForWeb(Long newsId);

    /**
     * Retrieves news cards for general display (dashboard/cards).
     * Includes processed excerpts and all engagement metrics for card display.
     *
     * @param pageable pagination information
     * @return paginated list of news cards with processed content
     */
    Page<NewsCreateResponseDto> getNewsCards(Pageable pageable);

    /**
     * Retrieves trending news cards with a specified limit.
     * Optimized for trending sections with processed excerpts and engagement
     * metrics.
     *
     * @param limit maximum number of trending news cards
     * @return list of trending news cards with processed content
     */
    List<NewsCreateResponseDto> getTrendingNewsCards(int limit);

    /**
     * Retrieves news cards filtered by category.
     * Includes processed excerpts and engagement metrics for category-specific
     * displays.
     *
     * @param categoryId the category ID to filter by
     * @param pageable   pagination information
     * @return paginated list of news cards in the specified category
     */
    Page<NewsCreateResponseDto> getNewsCardsByCategory(String categoryId, Pageable pageable);

    /**
     * Retrieves news cards optimized for mobile display with filtering options.
     * Supports category and priority filtering for mobile-specific layouts.
     *
     * @param pageable pagination information
     * @param category optional category filter
     * @param priority optional priority filter
     * @return paginated list of mobile-optimized news cards
     */
    Page<NewsCreateResponseDto> getNewsCardsForMobile(Pageable pageable, String category, String priority);

    /**
     * Retrieves breaking news cards optimized for mobile display.
     * Prioritizes breaking news with processed content for urgent notifications.
     *
     * @param pageable pagination information
     * @return paginated list of breaking news cards for mobile
     */
    Page<NewsCreateResponseDto> getBreakingNewsForMobile(Pageable pageable);

    /**
     * Retrieves news articles by category optimized for mobile display.
     * Processes content for mobile consumption within specific categories.
     *
     * @param category the category to filter by
     * @param pageable pagination information
     * @return paginated list of mobile-optimized news by category
     */
    Page<NewsCreateResponseDto> getNewsByCategoryForMobile(String category, Pageable pageable);

    /**
     * Retrieves a specific published newsapp entity by ID for public users.
     * Returns the full entity for internal processing (e.g., SEO generation).
     *
     * @param id the newsapp newsapp ID
     * @return the published newsapp entity
     * @throws NewsNotFoundException if not found or not published
     */
    NewsMasterEntity getPublishedNewsEntityById(String id);

    /**
     * Retrieves a specific published newsapp entity by slug for public users.
     * Returns the full entity for internal processing (e.g., SEO generation).
     *
     * @param slug the newsapp newsapp slug
     * @return the published newsapp entity
     * @throws NewsNotFoundException if not found or not published
     */
    NewsMasterEntity getPublishedNewsEntityBySlug(String slug);

    /**
     * Retrieves default recommendations (trending/popular) for anonymous users.
     *
     * @param pageable pagination information
     * @return list of default recommended newsapp articles
     */
    List<NewsCreateResponseDto> getDefaultRecommendations(Pageable pageable);

    /**
     * Retrieves all news article IDs (for bulk operations like Elasticsearch
     * reindexing).
     * <p>
     * This method is useful for administrative operations that need to process all
     * news articles in bulk, such as search index rebuilding.
     * </p>
     *
     * @return list of all news article UUIDs
     */
    List<UUID> getAllNewsIds();
}
