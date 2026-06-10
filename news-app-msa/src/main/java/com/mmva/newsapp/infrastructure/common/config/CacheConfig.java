package com.mmva.newsapp.infrastructure.common.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.mmva.newsapp.domain.adminuser.config.core.AdminUserCacheConstants;
import com.mmva.newsapp.domain.appuser.config.core.AppUserCacheConstants;
import com.mmva.newsapp.domain.news.config.core.NewsCacheConstants;
import com.mmva.newsapp.domain.newscategory.config.core.NewsCategoryCacheConstants;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Cache infrastructure configuration for the application.
 * 
 * <p>
 * This class provides the CacheManager beans (infrastructure) while cache NAME
 * constants
 * are owned by their respective domain modules following Feature Ownership
 * principle:
 * </p>
 * <ul>
 * <li>{@link NewsCacheConstants} - News module cache names</li>
 * <li>{@link NewsCategoryCacheConstants} - News Category module cache
 * names</li>
 * <li>{@link AdminUserCacheConstants} - Admin User module cache names</li>
 * <li>{@link AppUserCacheConstants} - App User module cache names</li>
 * </ul>
 * 
 * <h3>TTL Strategy:</h3>
 * <ul>
 * <li>Short (5 min): Trending/dynamic data</li>
 * <li>Medium (15 min): Regular content</li>
 * <li>Long (1 hour): Static/reference data</li>
 * </ul>
 * 
 * <h3>Memory Protection:</h3>
 * <ul>
 * <li>Maximum 1000 entries per cache (default)</li>
 * <li>Maximum 500 entries for short-lived caches</li>
 * <li>Automatic eviction of least-recently-used entries</li>
 * </ul>
 * 
 * @author MMVA News Team
 * @since 1.0.0
 */
@Configuration
@EnableCaching
public class CacheConfig {

    // ========================================
    // TTL Configuration (in minutes)
    // ========================================
    private static final int TTL_SHORT = 5; // Trending, frequently changing
    private static final int TTL_MEDIUM = 15; // Regular content
    private static final int TTL_LONG = 60; // Static/reference data

    // ========================================
    // Memory Protection
    // ========================================
    private static final int MAX_CACHE_SIZE = 1000; // Max entries per cache
    private static final int MAX_CACHE_SIZE_SMALL = 500; // Max entries for short-lived caches

    /**
     * Default cache manager with medium TTL (15 minutes).
     * Used for most caches. Marked as @Primary to be the default.
     * 
     * <p>
     * Registered caches (from domain modules):
     * </p>
     * <ul>
     * <li>News: {@code newsCache}, {@code newsBySlugCache},
     * {@code publishedNewsCache}</li>
     * <li>Category: {@code categoryCache}, {@code allCategoriesCache}</li>
     * <li>Admin: {@code adminCache}, {@code adminListCache},
     * {@code adminByRoleCache}</li>
     * <li>User: {@code userCache}, {@code userListCache}, {@code userByEmailCache},
     * {@code userByPhoneCache}</li>
     * </ul>
     */
    @Bean
    @Primary
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(defaultCacheBuilder());
        cacheManager.setCacheNames(List.of(
                // News module caches
                NewsCacheConstants.NEWS_CACHE,
                NewsCacheConstants.NEWS_BY_SLUG_CACHE,
                NewsCacheConstants.PUBLISHED_NEWS_CACHE,
                NewsCacheConstants.TRENDING_NEWS_CACHE,
                // News Category module caches
                NewsCategoryCacheConstants.CATEGORY_CACHE,
                NewsCategoryCacheConstants.ALL_CATEGORIES_CACHE,
                // Admin User module caches
                AdminUserCacheConstants.ADMIN_CACHE,
                AdminUserCacheConstants.ADMIN_LIST_CACHE,
                AdminUserCacheConstants.ADMIN_BY_ROLE_CACHE,
                // App User module caches
                AppUserCacheConstants.USER_CACHE,
                AppUserCacheConstants.USER_LIST_CACHE,
                AppUserCacheConstants.USER_BY_EMAIL_CACHE,
                AppUserCacheConstants.USER_BY_PHONE_CACHE));
        return cacheManager;
    }

    /**
     * Short-lived cache manager for trending/dynamic content (5 minutes TTL).
     * 
     * <p>
     * Registered caches:
     * </p>
     * <ul>
     * <li>{@code trendingNewsCache} - Frequently updated trending news</li>
     * </ul>
     */
    @Bean
    public CacheManager trendingCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(shortTtlCacheBuilder());
        cacheManager.setCacheNames(List.of(
                NewsCacheConstants.TRENDING_NEWS_CACHE));
        return cacheManager;
    }

    /**
     * Default cache configuration: 15 minutes TTL, max 1000 entries.
     */
    private Caffeine<Object, Object> defaultCacheBuilder() {
        return Caffeine.newBuilder()
                .expireAfterWrite(TTL_MEDIUM, TimeUnit.MINUTES)
                .maximumSize(MAX_CACHE_SIZE)
                .recordStats(); // Enable statistics for monitoring
    }

    /**
     * Short TTL cache configuration: 5 minutes TTL, max 500 entries.
     */
    private Caffeine<Object, Object> shortTtlCacheBuilder() {
        return Caffeine.newBuilder()
                .expireAfterWrite(TTL_SHORT, TimeUnit.MINUTES)
                .maximumSize(MAX_CACHE_SIZE_SMALL)
                .recordStats();
    }

    /**
     * Long TTL cache configuration: 1 hour TTL, max 500 entries.
     * Use for static/reference data.
     */
    @SuppressWarnings("unused") // Reserved for future static data caching
    private Caffeine<Object, Object> longTtlCacheBuilder() {
        return Caffeine.newBuilder()
                .expireAfterWrite(TTL_LONG, TimeUnit.MINUTES)
                .maximumSize(MAX_CACHE_SIZE_SMALL)
                .recordStats();
    }
}
