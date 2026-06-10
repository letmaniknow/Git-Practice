package com.mmva.newsapp.domain.newsengagement.views.repository;

import com.mmva.newsapp.domain.newsengagement.views.model.NewsView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Repository for NewsView entity.
 * 
 * <p>
 * Table: news_views
 * </p>
 * <p>
 * Naming convention: {tableName}_{fieldName}
 * </p>
 * <p>
 * <strong>Design Note:</strong> Views are EVENTS, not STATES. A user can view
 * the same article multiple times. Each view is a discrete event for analytics.
 * </p>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Repository
public interface NewsViewRepository extends JpaRepository<NewsView, Long> {

    // ========================================
    // Basic Queries
    // ========================================

    List<NewsView> findByNewsViewsNewsId(UUID newsId);

    List<NewsView> findByNewsViewsUserId(UUID userId);

    // ========================================
    // Count Queries
    // ========================================

    @Query("SELECT COUNT(nv) FROM NewsView nv WHERE nv.newsViewsNewsId = :newsId")
    Long countViewsByNewsId(@Param("newsId") UUID newsId);

    @Query("SELECT COUNT(nv) FROM NewsView nv WHERE nv.newsViewsNewsId = :newsId AND nv.newsViewsUserId = :userId")
    Long countViewsByNewsIdAndUserId(@Param("newsId") UUID newsId, @Param("userId") UUID userId);

    @Query("SELECT COUNT(nv) FROM NewsView nv WHERE nv.newsViewsViewedAt BETWEEN :start AND :end")
    Long countViewsBetween(@Param("start") Instant start, @Param("end") Instant end);

    // ========================================
    // Analytics Queries - By Device
    // ========================================

    /**
     * Counts views by device type for a specific article.
     * Useful for understanding mobile vs desktop traffic.
     */
    @Query("SELECT nv.newsViewsDeviceType, COUNT(nv) FROM NewsView nv WHERE nv.newsViewsNewsId = :newsId GROUP BY nv.newsViewsDeviceType")
    List<Object[]> countViewsByDeviceType(@Param("newsId") UUID newsId);

    /**
     * Finds views by device type for analytics.
     */
    List<NewsView> findByNewsViewsDeviceType(String deviceType);

    // ========================================
    // Analytics Queries - By Location
    // ========================================

    /**
     * Counts views by country for a specific article.
     * Useful for geographic analytics.
     */
    @Query("SELECT nv.newsViewsCountryCode, COUNT(nv) FROM NewsView nv WHERE nv.newsViewsNewsId = :newsId GROUP BY nv.newsViewsCountryCode")
    List<Object[]> countViewsByCountry(@Param("newsId") UUID newsId);

    /**
     * Finds views by country code for analytics.
     */
    List<NewsView> findByNewsViewsCountryCode(String countryCode);

    // ========================================
    // Analytics Queries - By Channel
    // ========================================

    /**
     * Counts views by channel (WEB, MOBILE_WEB, IOS_APP, ANDROID_APP, API).
     * Useful for understanding traffic sources.
     */
    @Query("SELECT nv.newsViewsChannel, COUNT(nv) FROM NewsView nv WHERE nv.newsViewsNewsId = :newsId GROUP BY nv.newsViewsChannel")
    List<Object[]> countViewsByChannel(@Param("newsId") UUID newsId);

    // ========================================
    // Analytics Queries - By IP (Fraud Detection)
    // ========================================

    /**
     * Groups views by IP address for fraud detection.
     */
    @Query("SELECT nv.newsViewsIpAddress, COUNT(nv) FROM NewsView nv WHERE nv.newsViewsNewsId = :newsId GROUP BY nv.newsViewsIpAddress")
    List<Object[]> getViewStatsByIpAddress(@Param("newsId") UUID newsId);

    // ========================================
    // Analytics Queries - Bot Detection
    // ========================================

    /**
     * Counts bot vs human views for a specific article.
     */
    @Query("SELECT nv.newsViewsIsBot, COUNT(nv) FROM NewsView nv WHERE nv.newsViewsNewsId = :newsId GROUP BY nv.newsViewsIsBot")
    List<Object[]> countViewsByBotStatus(@Param("newsId") UUID newsId);

    /**
     * Counts views excluding bots (real human traffic).
     */
    @Query("SELECT COUNT(nv) FROM NewsView nv WHERE nv.newsViewsNewsId = :newsId AND (nv.newsViewsIsBot = false OR nv.newsViewsIsBot IS NULL)")
    Long countHumanViewsByNewsId(@Param("newsId") UUID newsId);
}
