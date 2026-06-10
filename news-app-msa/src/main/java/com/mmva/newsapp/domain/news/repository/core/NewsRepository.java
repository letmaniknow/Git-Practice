package com.mmva.newsapp.domain.news.repository.core;

import com.mmva.newsapp.domain.news.config.core.NewsCacheConstants;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mmva.newsapp.domain.news.model.core.NewsMasterEntity;
import com.mmva.newsapp.domain.news.model.core.NewsMasterEntity.WorkflowStatus;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NewsRepository
                extends JpaRepository<NewsMasterEntity, UUID>, JpaSpecificationExecutor<NewsMasterEntity> {

        // Override findById to add caching for frequently accessed news articles
        @Override
        @Cacheable(value = NewsCacheConstants.NEWS_CACHE, key = "#id")
        Optional<NewsMasterEntity> findById(UUID id);

        // Scheduled publishing: find non-deleted drafts with scheduledPublishAt before
        // now
        @Query("SELECT n FROM NewsMasterEntity n WHERE n.deletedAt IS NULL AND n.newsWorkflowStatus = :status AND n.newsScheduledPublishAt < :scheduledTime")
        List<NewsMasterEntity> findScheduledForPublishing(@Param("status") WorkflowStatus newsWorkflowStatus,
                        @Param("scheduledTime") Instant newsScheduledPublishAt);

        // Legacy method (may include soft-deleted - kept for backwards compatibility)
        List<NewsMasterEntity> findAllByNewsWorkflowStatusAndNewsScheduledPublishAtBefore(
                        WorkflowStatus newsWorkflowStatus,
                        Instant newsScheduledPublishAt);

        // Search by newscategory
        Page<NewsMasterEntity> findByNewsNewsCategoryId(UUID newsNewsCategoryId, Pageable pageable);

        // For version history
        List<NewsMasterEntity> findAllByNewsPreviousVersionId(UUID newsPreviousVersionId);

        // For author search
        Page<NewsMasterEntity> findByCreatedBy(UUID createdBy, Pageable pageable);

        /**
         * Search by category (simple query - ranking in service layer)
         */
        Page<NewsMasterEntity> findByNewsNewsCategoryIdAndDeletedAtIsNull(UUID newsNewsCategoryId, Pageable pageable);

        // Find related newsapp by newscategory (excluding current newsapp)
        @Query("SELECT n FROM NewsMasterEntity n WHERE n.deletedAt IS NULL AND n.newsNewsCategoryId = :newsNewsCategoryId AND n.newsNewsId <> :excludeNewsId ORDER BY n.createdAt DESC")
        List<NewsMasterEntity> findRelatedNewsByCategory(@Param("newsNewsCategoryId") UUID newsNewsCategoryId,
                        @Param("excludeNewsId") UUID excludeNewsId,
                        Pageable pageable);

        // Search by date range
        @Query("SELECT n FROM NewsMasterEntity n WHERE n.deletedAt IS NULL AND n.createdAt >= :fromDate AND n.createdAt <= :toDate")
        Page<NewsMasterEntity> findByDateRange(@Param("fromDate") Instant fromDate,
                        @Param("toDate") Instant toDate,
                        Pageable pageable);

        // Find by workflow status (admindashboard - includes soft-deleted)
        Page<NewsMasterEntity> findByNewsWorkflowStatus(WorkflowStatus newsWorkflowStatus, Pageable pageable);

        // Find by slug (admindashboard - includes soft-deleted)
        NewsMasterEntity findByNewsSlug(String newsSlug);

        // Find by English title (admin dashboard - includes soft-deleted) - used for
        // title uniqueness check
        NewsMasterEntity findByNewsTitleEn(String newsTitleEn);

        // Find by Spanish title (admin dashboard - includes soft-deleted) - used for
        // title uniqueness check
        NewsMasterEntity findByNewsTitleEs(String newsTitleEs);

        // Find active by slug (excludes soft-deleted)
        @Cacheable(value = NewsCacheConstants.NEWS_BY_SLUG_CACHE, key = "#newsSlug")
        @Query("SELECT n FROM NewsMasterEntity n WHERE n.deletedAt IS NULL AND n.newsSlug = :newsSlug")
        Optional<NewsMasterEntity> findActiveBySlug(@Param("newsSlug") String newsSlug);

        // =========================
        // Public Access Queries (PUBLISHED + isActive + NOT DELETED)
        // =========================

        // Find all published and active newsapp (excludes soft-deleted)
        @Query("SELECT n FROM NewsMasterEntity n WHERE n.deletedAt IS NULL AND n.newsWorkflowStatus = :status AND n.newsIsActive = true")
        Page<NewsMasterEntity> findPublishedAndActive(@Param("status") WorkflowStatus newsWorkflowStatus,
                        Pageable pageable);

        // Find published newsapp by ID (excludes soft-deleted)
        @Cacheable(value = NewsCacheConstants.NEWS_CACHE, key = "'published:' + #newsNewsId")
        @Query("SELECT n FROM NewsMasterEntity n WHERE n.deletedAt IS NULL AND n.newsNewsId = :newsNewsId AND n.newsWorkflowStatus = :status AND n.newsIsActive = true")
        Optional<NewsMasterEntity> findPublishedById(@Param("newsNewsId") UUID newsNewsId,
                        @Param("status") WorkflowStatus newsWorkflowStatus);

        // Find published newsapp by slug (excludes soft-deleted) - PRIMARY PUBLIC API
        @Query("SELECT n FROM NewsMasterEntity n WHERE n.deletedAt IS NULL AND n.newsSlug = :newsSlug AND n.newsWorkflowStatus = :status AND n.newsIsActive = true")
        Optional<NewsMasterEntity> findPublishedBySlug(@Param("newsSlug") String newsSlug,
                        @Param("status") WorkflowStatus newsWorkflowStatus);

        // Find published newsapp by newscategory (excludes soft-deleted)
        @Query("SELECT n FROM NewsMasterEntity n WHERE n.deletedAt IS NULL AND n.newsNewsCategoryId = :categoryId AND n.newsWorkflowStatus = :status AND n.newsIsActive = true")
        Page<NewsMasterEntity> findPublishedByCategory(@Param("categoryId") UUID newsNewsCategoryId,
                        @Param("status") WorkflowStatus newsWorkflowStatus, Pageable pageable);

        // Find published newsapp by author (excludes soft-deleted)
        @Query("SELECT n FROM NewsMasterEntity n WHERE n.deletedAt IS NULL AND n.createdBy = :authorId AND n.newsWorkflowStatus = :status AND n.newsIsActive = true")
        Page<NewsMasterEntity> findPublishedByAuthor(@Param("authorId") UUID createdBy,
                        @Param("status") WorkflowStatus newsWorkflowStatus, Pageable pageable);

        // Find featured published newsapp (excludes soft-deleted)
        @Query("SELECT n FROM NewsMasterEntity n WHERE n.deletedAt IS NULL AND n.newsIsFeatured = true AND n.newsWorkflowStatus = :status AND n.newsIsActive = true")
        Page<NewsMasterEntity> findFeaturedPublished(@Param("status") WorkflowStatus newsWorkflowStatus,
                        Pageable pageable);

        // Legacy derived methods (keep for backwards compatibility - admindashboard
        // use)
        Page<NewsMasterEntity> findByNewsWorkflowStatusAndNewsIsActiveTrue(WorkflowStatus newsWorkflowStatus,
                        Pageable pageable);

        // Find published newsapp by ID
        NewsMasterEntity findByNewsNewsIdAndNewsWorkflowStatusAndNewsIsActiveTrue(UUID newsNewsId,
                        WorkflowStatus newsWorkflowStatus);

        // Find published newsapp by slug
        NewsMasterEntity findByNewsSlugAndNewsWorkflowStatusAndNewsIsActiveTrue(String newsSlug,
                        WorkflowStatus newsWorkflowStatus);

        // Find published newsapp by newscategory
        Page<NewsMasterEntity> findByNewsNewsCategoryIdAndNewsWorkflowStatusAndNewsIsActiveTrue(UUID newsNewsCategoryId,
                        WorkflowStatus newsWorkflowStatus,
                        Pageable pageable);

        // Find published newsapp by author
        Page<NewsMasterEntity> findByCreatedByAndNewsWorkflowStatusAndNewsIsActiveTrue(UUID createdBy,
                        WorkflowStatus newsWorkflowStatus,
                        Pageable pageable);

        // Find featured published newsapp
        Page<NewsMasterEntity> findByNewsIsFeaturedTrueAndNewsWorkflowStatusAndNewsIsActiveTrue(
                        WorkflowStatus newsWorkflowStatus,
                        Pageable pageable);

        /**
         * Find published articles by status and active flag.
         * Uses LEFT JOIN FETCH for eager loading to prevent N+1 queries.
         * Ranking handled in service layer.
         */
        @Query("SELECT DISTINCT n FROM NewsMasterEntity n " +
                        "WHERE n.deletedAt IS NULL AND n.newsWorkflowStatus = :status AND n.newsIsActive = :isActive")
        Page<NewsMasterEntity> findByDeletedAtIsNullAndNewsWorkflowStatusAndNewsIsActive(
                        @Param("status") WorkflowStatus status,
                        @Param("isActive") Boolean isActive,
                        Pageable pageable);

        /**
         * Find published articles by category.
         * Uses LEFT JOIN FETCH for eager loading to prevent N+1 queries.
         * Ranking handled in service layer.
         */
        @Query("SELECT DISTINCT n FROM NewsMasterEntity n " +
                        "WHERE n.deletedAt IS NULL AND n.newsNewsCategoryId = :newsNewsCategoryId " +
                        "AND n.newsWorkflowStatus = :status AND n.newsIsActive = :isActive")
        Page<NewsMasterEntity> findByDeletedAtIsNullAndNewsNewsCategoryIdAndNewsWorkflowStatusAndNewsIsActive(
                        @Param("newsNewsCategoryId") UUID newsNewsCategoryId,
                        @Param("status") WorkflowStatus status,
                        @Param("isActive") Boolean isActive,
                        Pageable pageable);

        // Find related published newsapp by newscategory
        @Query("SELECT n FROM NewsMasterEntity n WHERE n.deletedAt IS NULL AND n.newsNewsCategoryId = :newsNewsCategoryId AND n.newsNewsId <> :excludeNewsId "
                        +
                        "AND n.newsWorkflowStatus = :status AND n.newsIsActive = true ORDER BY n.newsPublishedAt DESC")
        List<NewsMasterEntity> findRelatedPublishedNewsByCategory(@Param("newsNewsCategoryId") UUID newsNewsCategoryId,
                        @Param("excludeNewsId") UUID excludeNewsId, @Param("status") WorkflowStatus status,
                        Pageable pageable);

        // Find trending published newsapp (ordered by views, likes, shares)
        @Query("SELECT n FROM NewsMasterEntity n WHERE n.deletedAt IS NULL AND n.newsWorkflowStatus = :status AND n.newsIsActive = true "
                        +
                        "ORDER BY (n.newsViewCount + n.newsLikeCount + n.newsShareCount) DESC")
        Page<NewsMasterEntity> findTrendingPublishedNews(@Param("status") WorkflowStatus status, Pageable pageable);

        // Fetch trending newsapp (custom query for trending logic)
        @Query("SELECT n FROM NewsMasterEntity n WHERE n.deletedAt IS NULL AND n.newsWorkflowStatus = 'PUBLISHED' AND n.newsIsActive = true ORDER BY (n.newsViewCount + n.newsLikeCount + n.newsShareCount) DESC")
        List<NewsMasterEntity> findTrendingNews(Pageable pageable);

        /**
         * Find all soft-deleted newsapp.
         * Works when soft-delete filter is disabled (admindashboard endpoints).
         */
        List<NewsMasterEntity> findByDeletedAtIsNotNull();

        /**
         * Find by date range (simple query - ranking in service layer).
         * Uses @Query for explicit control over query optimization.
         */
        @Query("SELECT DISTINCT n FROM NewsMasterEntity n " +
                        "WHERE n.deletedAt IS NULL AND n.createdAt BETWEEN :fromDate AND :toDate")
        Page<NewsMasterEntity> findByDeletedAtIsNullAndCreatedAtBetween(
                        @Param("fromDate") Instant fromDate,
                        @Param("toDate") Instant toDate,
                        Pageable pageable);

        /**
         * Find all non-deleted news articles.
         * Used for in-memory ranking and filtering in service layer.
         */
        List<NewsMasterEntity> findByDeletedAtIsNull();

        /**
         * Permanently delete a news article by ID (hard delete).
         * This bypasses soft-delete and removes the record from the database.
         *
         * @param newsNewsId the UUID of the news article to delete
         */
        @Modifying
        @Query("DELETE FROM NewsMasterEntity n WHERE n.newsNewsId = :newsNewsId")
        void hardDeleteById(@Param("newsNewsId") UUID newsNewsId);

        // ========================================
        // Engagement Counter Methods
        // ========================================
        // These atomic updates maintain denormalized counters for performance.
        // See PROJECT_PRINCIPLES.md "Engagement Counter Management" section.

        // ====== EVENT COUNTERS (Views, Shares) ======
        // Note: Admin cleanup operations may need to decrement these counters
        // to maintain consistency between records and counter values.

        /**
         * Atomically increments the view count for a news article.
         * Views are EVENT type (typically append-only).
         *
         * @param newsId the UUID of the news article
         */
        @Modifying
        @Query("UPDATE NewsMasterEntity n SET n.newsViewCount = n.newsViewCount + 1 WHERE n.newsNewsId = :newsId")
        void incrementViewCount(@Param("newsId") UUID newsId);

        /**
         * Atomically decrements the view count for a news article.
         * Used for admin cleanup operations to maintain counter consistency.
         * Uses CASE WHEN to prevent negative counts from race conditions.
         *
         * @param newsId the UUID of the news article
         */
        @Modifying
        @Query("UPDATE NewsMasterEntity n SET n.newsViewCount = CASE WHEN n.newsViewCount > 0 THEN n.newsViewCount - 1 ELSE 0 END WHERE n.newsNewsId = :newsId")
        void decrementViewCount(@Param("newsId") UUID newsId);

        /**
         * Atomically increments the share count for a news article.
         * Shares are EVENT type (typically append-only).
         *
         * @param newsId the UUID of the news article
         */
        @Modifying
        @Query("UPDATE NewsMasterEntity n SET n.newsShareCount = n.newsShareCount + 1 WHERE n.newsNewsId = :newsId")
        void incrementShareCount(@Param("newsId") UUID newsId);

        /**
         * Atomically decrements the share count for a news article.
         * Used for admin cleanup operations to maintain counter consistency.
         * Uses CASE WHEN to prevent negative counts from race conditions.
         *
         * @param newsId the UUID of the news article
         */
        @Modifying
        @Query("UPDATE NewsMasterEntity n SET n.newsShareCount = CASE WHEN n.newsShareCount > 0 THEN n.newsShareCount - 1 ELSE 0 END WHERE n.newsNewsId = :newsId")
        void decrementShareCount(@Param("newsId") UUID newsId);

        // ====== STATE COUNTERS (Toggle - Likes, Bookmarks) ======

        /**
         * Atomically increments the like count for a news article.
         * Likes are STATE type (user can toggle like/unlike).
         *
         * @param newsId the UUID of the news article
         */
        @Modifying
        @Query("UPDATE NewsMasterEntity n SET n.newsLikeCount = n.newsLikeCount + 1 WHERE n.newsNewsId = :newsId")
        void incrementLikeCount(@Param("newsId") UUID newsId);

        /**
         * Atomically decrements the like count for a news article.
         * Uses CASE WHEN to prevent negative counts from race conditions.
         *
         * @param newsId the UUID of the news article
         */
        @Modifying
        @Query("UPDATE NewsMasterEntity n SET n.newsLikeCount = CASE WHEN n.newsLikeCount > 0 THEN n.newsLikeCount - 1 ELSE 0 END WHERE n.newsNewsId = :newsId")
        void decrementLikeCount(@Param("newsId") UUID newsId);

        /**
         * Atomically increments the bookmark count for a news article.
         * Bookmarks are STATE type (user can add/remove).
         *
         * @param newsId the UUID of the news article
         */
        @Modifying
        @Query("UPDATE NewsMasterEntity n SET n.newsBookmarkCount = n.newsBookmarkCount + 1 WHERE n.newsNewsId = :newsId")
        void incrementBookmarkCount(@Param("newsId") UUID newsId);

        /**
         * Atomically decrements the bookmark count for a news article.
         * Uses CASE WHEN to prevent negative counts from race conditions.
         *
         * @param newsId the UUID of the news article
         */
        @Modifying
        @Query("UPDATE NewsMasterEntity n SET n.newsBookmarkCount = CASE WHEN n.newsBookmarkCount > 0 THEN n.newsBookmarkCount - 1 ELSE 0 END WHERE n.newsNewsId = :newsId")
        void decrementBookmarkCount(@Param("newsId") UUID newsId);

        // ====== OBJECT COUNTERS (CRUD - Comments, Replies) ======

        /**
         * Atomically increments the comment count for a news article.
         * Comments are OBJECT type (can be created/deleted).
         *
         * @param newsId the UUID of the news article
         */
        @Modifying
        @Query("UPDATE NewsMasterEntity n SET n.newsCommentCount = n.newsCommentCount + 1 WHERE n.newsNewsId = :newsId")
        void incrementCommentCount(@Param("newsId") UUID newsId);

        /**
         * Atomically decrements the comment count for a news article.
         * Uses CASE WHEN to prevent negative counts from race conditions.
         *
         * @param newsId the UUID of the news article
         */
        @Modifying
        @Query("UPDATE NewsMasterEntity n SET n.newsCommentCount = CASE WHEN n.newsCommentCount > 0 THEN n.newsCommentCount - 1 ELSE 0 END WHERE n.newsNewsId = :newsId")
        void decrementCommentCount(@Param("newsId") UUID newsId);

        /**
         * Atomically increments the reply count for a news article.
         * Replies are OBJECT type (can be created/deleted).
         *
         * @param newsId the UUID of the news article
         */
        @Modifying
        @Query("UPDATE NewsMasterEntity n SET n.newsReplyCount = n.newsReplyCount + 1 WHERE n.newsNewsId = :newsId")
        void incrementReplyCount(@Param("newsId") UUID newsId);

        /**
         * Atomically decrements the reply count for a news article.
         * Uses CASE WHEN to prevent negative counts from race conditions.
         *
         * @param newsId the UUID of the news article
         */
        @Modifying
        @Query("UPDATE NewsMasterEntity n SET n.newsReplyCount = CASE WHEN n.newsReplyCount > 0 THEN n.newsReplyCount - 1 ELSE 0 END WHERE n.newsNewsId = :newsId")
        void decrementReplyCount(@Param("newsId") UUID newsId);

        // ====== COUNTER GETTERS (for response DTOs) ======

        /**
         * Gets the current view count for a news article.
         *
         * @param newsId the UUID of the news article
         * @return the current view count, or 0 if article not found
         */
        @Query("SELECT COALESCE(n.newsViewCount, 0) FROM NewsMasterEntity n WHERE n.newsNewsId = :newsId")
        Long getViewCount(@Param("newsId") UUID newsId);

        /**
         * Gets the current share count for a news article.
         *
         * @param newsId the UUID of the news article
         * @return the current share count, or 0 if article not found
         */
        @Query("SELECT COALESCE(n.newsShareCount, 0) FROM NewsMasterEntity n WHERE n.newsNewsId = :newsId")
        Long getShareCount(@Param("newsId") UUID newsId);

        /**
         * Gets the current like count for a news article.
         *
         * @param newsId the UUID of the news article
         * @return the current like count, or 0 if article not found
         */
        @Query("SELECT COALESCE(n.newsLikeCount, 0) FROM NewsMasterEntity n WHERE n.newsNewsId = :newsId")
        Long getLikeCount(@Param("newsId") UUID newsId);

        /**
         * Gets the current bookmark count for a news article.
         *
         * @param newsId the UUID of the news article
         * @return the current bookmark count, or 0 if article not found
         */
        @Query("SELECT COALESCE(n.newsBookmarkCount, 0) FROM NewsMasterEntity n WHERE n.newsNewsId = :newsId")
        Long getBookmarkCount(@Param("newsId") UUID newsId);

        /**
         * Gets the current comment count for a news article.
         *
         * @param newsId the UUID of the news article
         * @return the current comment count, or 0 if article not found
         */
        @Query("SELECT COALESCE(n.newsCommentCount, 0) FROM NewsMasterEntity n WHERE n.newsNewsId = :newsId")
        Long getCommentCount(@Param("newsId") UUID newsId);

        /**
         * Gets the current reply count for a news article.
         *
         * @param newsId the UUID of the news article
         * @return the current reply count, or 0 if article not found
         */
        @Query("SELECT COALESCE(n.newsReplyCount, 0) FROM NewsMasterEntity n WHERE n.newsNewsId = :newsId")
        Long getReplyCount(@Param("newsId") UUID newsId);

        // ====== SOCIAL SHARING METHODS ======

        /**
         * Find news by IDs for bulk social sharing operations.
         */
        @Query("SELECT n FROM NewsMasterEntity n WHERE n.newsNewsId IN :newsIds")
        List<NewsMasterEntity> findByNewsIds(@Param("newsIds") List<UUID> newsIds);

        /**
         * Returns the count of news articles referencing the given media file name
         * (excluding a specific news ID).
         */
        @Query("SELECT COUNT(n) FROM NewsMasterEntity n WHERE n.deletedAt IS NULL AND n.newsMediaFileName = :mediaFileName AND n.newsNewsId <> :excludeNewsId")
        long countByMediaFileNameExcludingNewsId(@Param("mediaFileName") String mediaFileName,
                        @Param("excludeNewsId") UUID excludeNewsId);

        /**
         * Returns the count of news articles referencing the given thumbnail URL
         * (excluding a specific news ID).
         */
        @Query("SELECT COUNT(n) FROM NewsMasterEntity n WHERE n.deletedAt IS NULL AND n.newsThumbnailUrl = :thumbnailUrl AND n.newsNewsId <> :excludeNewsId")
        long countByThumbnailUrlExcludingNewsId(@Param("thumbnailUrl") String thumbnailUrl,
                        @Param("excludeNewsId") UUID excludeNewsId);

        /**
         * Returns the count of news articles referencing the given card image URL
         * (excluding a specific news ID).
         */
        @Query("SELECT COUNT(n) FROM NewsMasterEntity n WHERE n.deletedAt IS NULL AND n.newsImageCardUrl = :cardUrl AND n.newsNewsId <> :excludeNewsId")
        long countByCardUrlExcludingNewsId(@Param("cardUrl") String cardUrl,
                        @Param("excludeNewsId") UUID excludeNewsId);

        /**
         * Returns the count of news articles referencing the given hero image URL
         * (excluding a specific news ID).
         */
        @Query("SELECT COUNT(n) FROM NewsMasterEntity n WHERE n.deletedAt IS NULL AND n.newsImageHeroUrl = :heroUrl AND n.newsNewsId <> :excludeNewsId")
        long countByHeroUrlExcludingNewsId(@Param("heroUrl") String heroUrl,
                        @Param("excludeNewsId") UUID excludeNewsId);
}
