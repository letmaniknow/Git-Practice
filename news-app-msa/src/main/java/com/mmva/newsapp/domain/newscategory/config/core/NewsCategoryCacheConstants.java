package com.mmva.newsapp.domain.newscategory.config.core;

/**
 * Cache name constants for the News Category module.
 * 
 * <p>
 * Following Feature Ownership principle (PROJECT_PRINCIPLES.md),
 * each domain module owns its cache name constants.
 * </p>
 * 
 * <h3>Cache Strategy:</h3>
 * <ul>
 * <li>{@link #CATEGORY_CACHE} - Individual/paginated categories (15 min
 * TTL)</li>
 * <li>{@link #ALL_CATEGORIES_CACHE} - Complete category list (15 min TTL)</li>
 * </ul>
 * 
 * <p>
 * Categories are reference data that changes infrequently,
 * so medium TTL is appropriate.
 * </p>
 * 
 * @author MMVA News Team
 * @since 1.0.0
 * @see com.mmva.newsapp.infrastructure.common.config.CacheConfig
 */
public final class NewsCategoryCacheConstants {

    private NewsCategoryCacheConstants() {
        // Utility class - prevent instantiation
    }

    // ========================================
    // Cache Names - News Category
    // ========================================

    /**
     * Cache for individual categories and paginated listings.
     * TTL: 15 minutes (medium - reference data)
     */
    public static final String CATEGORY_CACHE = "categoryCache";

    /**
     * Cache for complete category list (findAll without pagination).
     * TTL: 15 minutes (medium - reference data)
     */
    public static final String ALL_CATEGORIES_CACHE = "allCategoriesCache";
}
