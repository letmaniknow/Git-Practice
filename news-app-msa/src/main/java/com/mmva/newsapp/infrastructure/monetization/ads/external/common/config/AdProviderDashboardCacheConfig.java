package com.mmva.newsapp.infrastructure.monetization.ads.external.common.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cache configuration for ad providers dashboard
 *
 * Provides caching for dashboard data to improve performance and reduce
 * database load
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Configuration
@EnableCaching
public class AdProviderDashboardCacheConfig {

    /**
     * Cache manager for dashboard caching
     *
     * Uses ConcurrentMapCacheManager for development.
     * In production, consider Redis or other distributed cache.
     *
     * @return Cache manager instance
     */
    @Bean
    public CacheManager adProviderDashboardCacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager(
                "dashboardOverview",
                "dashboardStats",
                "metricsByProvider",
                "recentActivity",
                "dashboardHealth",
                "providerMetrics");

        // Configure cache settings if needed
        // cacheManager.setCacheNames(Arrays.asList(cacheNames));

        return cacheManager;
    }
}