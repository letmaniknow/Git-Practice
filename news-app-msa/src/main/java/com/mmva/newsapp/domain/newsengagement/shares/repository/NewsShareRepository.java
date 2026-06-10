package com.mmva.newsapp.domain.newsengagement.shares.repository;

import com.mmva.newsapp.domain.newsengagement.shares.model.NewsShare;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Repository for NewsShare entity.
 * 
 * <p>
 * Table: news_shares
 * </p>
 * <p>
 * Naming convention: {tableName}_{fieldName}
 * </p>
 * <p>
 * <strong>Design Note:</strong> Shares are EVENTS, not STATES. A user can share
 * the same article multiple times (to different platforms or at different
 * times).
 * Therefore, queries by (newsId, userId) return List, not Optional.
 * </p>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Repository
public interface NewsShareRepository extends JpaRepository<NewsShare, Long> {

    List<NewsShare> findByNewsSharesNewsId(UUID newsId);

    List<NewsShare> findByNewsSharesUserId(UUID userId);

    /**
     * Finds all shares by a user for a specific article.
     * Returns List because a user can share the same article multiple times.
     */
    List<NewsShare> findByNewsSharesNewsIdAndNewsSharesUserId(UUID newsId, UUID userId);

    /**
     * Checks if a user has ever shared a specific article.
     */
    boolean existsByNewsSharesNewsIdAndNewsSharesUserId(UUID newsId, UUID userId);

    @Query("SELECT COUNT(ns) FROM NewsShare ns WHERE ns.newsSharesNewsId = :newsId")
    Long countSharesByNewsSharesNewsId(@Param("newsId") UUID newsId);

    @Query("SELECT COUNT(ns) FROM NewsShare ns WHERE ns.newsSharesUserId = :userId")
    Long countSharesByNewsSharesUserId(@Param("userId") UUID userId);

    @Query("SELECT COUNT(ns) FROM NewsShare ns WHERE ns.newsSharesNewsId = :newsId AND ns.newsSharesUserId = :userId")
    Long countSharesByNewsIdAndUserId(@Param("newsId") UUID newsId, @Param("userId") UUID userId);

    @Query("SELECT ns.newsSharesIpAddress, COUNT(ns) FROM NewsShare ns WHERE ns.newsSharesNewsId = :newsId GROUP BY ns.newsSharesIpAddress")
    List<Object[]> getShareStatsByIpAddress(@Param("newsId") UUID newsId);

    /**
     * Finds shares by infrastructure for analytics.
     */
    List<NewsShare> findByNewsSharesPlatform(String platform);

    /**
     * Counts shares by platform for a specific article.
     * 
     * <p>
     * Returns platform share statistics ordered by popularity (most shares first).
     * Excludes shares with NULL platform for clean analytics.
     * </p>
     * 
     * @param newsId the news article ID
     * @return List of [platform, count] pairs ordered by count descending
     */
    @Query("SELECT ns.newsSharesPlatform, COUNT(ns) FROM NewsShare ns " +
            "WHERE ns.newsSharesNewsId = :newsId AND ns.newsSharesPlatform IS NOT NULL " +
            "GROUP BY ns.newsSharesPlatform " +
            "ORDER BY COUNT(ns) DESC")
    List<Object[]> countSharesByPlatform(@Param("newsId") UUID newsId);

    @Query("SELECT COUNT(ns) FROM NewsShare ns WHERE ns.newsSharesSharedAt BETWEEN :start AND :end")
    Long countSharesBetween(@Param("start") Instant start, @Param("end") Instant end);
}
