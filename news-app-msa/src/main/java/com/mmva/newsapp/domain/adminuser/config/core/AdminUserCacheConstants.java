package com.mmva.newsapp.domain.adminuser.config.core;

/**
 * Cache name constants for the Admin User module.
 * 
 * <p>
 * Following Feature Ownership principle (PROJECT_PRINCIPLES.md),
 * each domain module owns its cache name constants.
 * </p>
 * 
 * <h3>Cache Strategy:</h3>
 * <ul>
 * <li>{@link #ADMIN_CACHE} - Individual admin users by ID (15 min TTL)</li>
 * <li>{@link #ADMIN_LIST_CACHE} - Paginated admin user listings (15 min
 * TTL)</li>
 * <li>{@link #ADMIN_BY_ROLE_CACHE} - Admin users filtered by role (15 min
 * TTL)</li>
 * </ul>
 * 
 * <p>
 * Admin user data is relatively stable (internal staff),
 * so medium TTL is appropriate.
 * </p>
 * 
 * @author MMVA News Team
 * @since 1.0.0
 * @see com.mmva.newsapp.infrastructure.common.config.CacheConfig
 */
public final class AdminUserCacheConstants {

    private AdminUserCacheConstants() {
        // Utility class - prevent instantiation
    }

    // ========================================
    // Cache Names - Admin User
    // ========================================

    /**
     * Cache for individual admin users by ID.
     * TTL: 15 minutes (medium - staff data)
     */
    public static final String ADMIN_CACHE = "adminCache";

    /**
     * Cache for paginated admin user listings.
     * TTL: 15 minutes (medium - staff data)
     */
    public static final String ADMIN_LIST_CACHE = "adminListCache";

    /**
     * Cache for admin users filtered by role.
     * TTL: 15 minutes (medium - staff data)
     */
    public static final String ADMIN_BY_ROLE_CACHE = "adminByRoleCache";
}
