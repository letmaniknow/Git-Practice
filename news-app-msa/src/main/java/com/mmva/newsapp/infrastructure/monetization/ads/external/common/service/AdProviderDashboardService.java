package com.mmva.newsapp.infrastructure.monetization.ads.external.common.service;

import com.mmva.newsapp.infrastructure.monetization.ads.external.common.dto.*;
import com.mmva.newsapp.infrastructure.monetization.ads.external.common.enums.ProviderType;
import com.mmva.newsapp.infrastructure.monetization.ads.external.persistence.entity.ProviderMetricsSync;
import com.mmva.newsapp.infrastructure.monetization.ads.external.persistence.service.ProviderMetricsSyncService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Service interface for ad providers dashboard operations
 *
 * Provides business logic for dashboard data aggregation and analytics
 *
 * @author MMVA Team
 * @since 1.0.0
 */
public interface AdProviderDashboardService {

        /**
         * Get complete dashboard overview with all metrics and statistics
         *
         * @param tenantId  Tenant identifier for multi-tenant isolation
         * @param startDate Optional start date filter (default: 30 days ago)
         * @param endDate   Optional end date filter (default: today)
         * @return Complete dashboard overview response
         */
        AdProviderDashboardOverviewResponse getDashboardOverview(
                        String tenantId,
                        LocalDate startDate,
                        LocalDate endDate);

        /**
         * Get dashboard statistics only
         *
         * @param tenantId  Tenant identifier
         * @param startDate Optional start date filter
         * @param endDate   Optional end date filter
         * @return Dashboard statistics
         */
        AdProviderDashboardStats getDashboardStats(
                        String tenantId,
                        LocalDate startDate,
                        LocalDate endDate);

        /**
         * Get metrics grouped by provider type
         *
         * @param tenantId  Tenant identifier
         * @param startDate Optional start date filter
         * @param endDate   Optional end date filter
         * @return Metrics grouped by provider
         */
        Map<ProviderType, List<AdProviderMetricsSummary>> getMetricsByProvider(
                        String tenantId,
                        LocalDate startDate,
                        LocalDate endDate);

        /**
         * Get recent activity for dashboard
         *
         * @param tenantId Tenant identifier
         * @param limit    Maximum number of activities to return (default: 10)
         * @return List of recent activities
         */
        List<AdProviderRecentActivity> getRecentActivity(String tenantId, Integer limit);

        /**
         * Get dashboard health indicators
         *
         * @param tenantId Tenant identifier
         * @return Dashboard health information
         */
        AdProviderDashboardHealth getDashboardHealth(String tenantId);

        /**
         * Get metrics for specific provider
         *
         * @param tenantId     Tenant identifier
         * @param providerType Provider type
         * @param startDate    Optional start date filter
         * @param endDate      Optional end date filter
         * @return Provider-specific metrics
         */
        List<AdProviderMetricsSummary> getProviderMetrics(
                        String tenantId,
                        ProviderType providerType,
                        LocalDate startDate,
                        LocalDate endDate);
}