package com.mmva.newsapp.domain.news.config.core;

/**
 * Cache name constants for the News module.
 * 
 * <p>
 * Following Feature Ownership principle (PROJECT_PRINCIPLES.md),
 * each domain module owns its cache name constants.
 * </p>
 * 
 * <h3>Cache Strategy:</h3>
 * <ul>
 * <li>{@link #NEWS_CACHE} - Individual news articles by ID (15 min TTL)</li>
 * <li>{@link #NEWS_BY_SLUG_CACHE} - News articles by slug (15 min TTL)</li>
 * <li>{@link #PUBLISHED_NEWS_CACHE} - Published news listings (15 min TTL)</li>
 * <li>{@link #TRENDING_NEWS_CACHE} - Trending/dynamic content (5 min TTL)</li>
 * </ul>
 * 
 * @author MMVA News Team
 * @since 1.0.0
 * @see com.mmva.newsapp.infrastructure.common.config.CacheConfig
 */
public final class NewsCacheConstants {

    private NewsCacheConstants() {
        // Utility class - prevent instantiation
    }

    // ========================================
    // Cache Names - News
    // ========================================

    /**
     * Cache for individual news articles by ID.
     * TTL: 15 minutes (medium - regular content)
     */
    public static final String NEWS_CACHE = "newsCache";

    /**
     * Cache for news articles by slug (URL-friendly identifier).
     * TTL: 15 minutes (medium - regular content)
     */
    public static final String NEWS_BY_SLUG_CACHE = "newsBySlugCache";

    /**
     * Cache for published news listings (paginated).
     * TTL: 15 minutes (medium - regular content)
     */
    public static final String PUBLISHED_NEWS_CACHE = "publishedNewsCache";

    /**
     * Cache for trending news (frequently changing).
     * TTL: 5 minutes (short - dynamic content)
     */
    public static final String TRENDING_NEWS_CACHE = "trendingNewsCache";
}
