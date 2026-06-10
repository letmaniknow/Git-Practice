package com.mmva.newsapp.domain.appuser.config.core;

/**
 * Cache name constants for the App User module.
 * 
 * <p>
 * Following Feature Ownership principle (PROJECT_PRINCIPLES.md),
 * each domain module owns its cache name constants.
 * </p>
 * 
 * <h3>Cache Strategy:</h3>
 * <ul>
 * <li>{@link #USER_CACHE} - Individual users by ID (15 min TTL)</li>
 * <li>{@link #USER_LIST_CACHE} - Paginated user listings (15 min TTL)</li>
 * <li>{@link #USER_BY_EMAIL_CACHE} - Users by email lookup (15 min TTL)</li>
 * <li>{@link #USER_BY_PHONE_CACHE} - Users by phone lookup (15 min TTL)</li>
 * </ul>
 * 
 * <p>
 * User profile data changes occasionally (profile updates),
 * so medium TTL provides good balance between performance and freshness.
 * </p>
 * 
 * @author MMVA News Team
 * @since 1.0.0
 * @see com.mmva.newsapp.infrastructure.common.config.CacheConfig
 */
public final class AppUserCacheConstants {

    private AppUserCacheConstants() {
        // Utility class - prevent instantiation
    }

    // ========================================
    // Cache Names - App User
    // ========================================

    /**
     * Cache for individual users by ID.
     * TTL: 15 minutes (medium - user profiles)
     */
    public static final String USER_CACHE = "userCache";

    /**
     * Cache for paginated user listings.
     * TTL: 15 minutes (medium - user listings)
     */
    public static final String USER_LIST_CACHE = "userListCache";

    /**
     * Cache for users by email lookup (authentication flow).
     * TTL: 15 minutes (medium - auth lookups)
     */
    public static final String USER_BY_EMAIL_CACHE = "userByEmailCache";

    /**
     * Cache for users by phone number lookup (authentication flow).
     * TTL: 15 minutes (medium - auth lookups)
     */
    public static final String USER_BY_PHONE_CACHE = "userByPhoneCache";
}
