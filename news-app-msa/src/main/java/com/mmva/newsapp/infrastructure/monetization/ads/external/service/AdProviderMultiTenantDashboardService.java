package com.mmva.newsapp.infrastructure.monetization.ads.external.service;

import com.mmva.newsapp.infrastructure.monetization.ads.external.common.dto.AdProviderDashboardOverviewResponse;
import com.mmva.newsapp.infrastructure.monetization.ads.external.common.dto.AdProviderDashboardStats;
import com.mmva.newsapp.infrastructure.monetization.ads.external.common.dto.AdProviderTenantUsageStats;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Multi-tenant dashboard service interface
 *
 * Provides dashboard functionality across multiple tenants
 *
 * @author MMVA Team
 * @since 1.0.0
 */
public interface AdProviderMultiTenantDashboardService {

        /**
         * Get dashboard overview for specific tenant
         *
         * @param tenantId  Tenant identifier
         * @param startDate Optional start date filter
         * @param endDate   Optional end date filter
         * @return Dashboard overview for tenant
         */
        AdProviderDashboardOverviewResponse getTenantDashboardOverview(
                        String tenantId,
                        LocalDate startDate,
                        LocalDate endDate);

        /**
         * Get dashboard statistics for specific tenant
         *
         * @param tenantId  Tenant identifier
         * @param startDate Optional start date filter
         * @param endDate   Optional end date filter
         * @return Dashboard statistics for tenant
         */
        AdProviderDashboardStats getTenantDashboardStats(
                        String tenantId,
                        LocalDate startDate,
                        LocalDate endDate);

        /**
         * Get dashboard overview for all tenants (super admin only)
         *
         * @param startDate Optional start date filter
         * @param endDate   Optional end date filter
         * @return Map of tenant ID to dashboard overview
         */
        Map<String, AdProviderDashboardOverviewResponse> getAllTenantsDashboardOverview(
                        LocalDate startDate,
                        LocalDate endDate);

        /**
         * Get dashboard statistics for all tenants (super admin only)
         *
         * @param startDate Optional start date filter
         * @param endDate   Optional end date filter
         * @return Map of tenant ID to dashboard statistics
         */
        Map<String, AdProviderDashboardStats> getAllTenantsDashboardStats(
                        LocalDate startDate,
                        LocalDate endDate);

        /**
         * Get list of active tenants
         *
         * @return List of tenant IDs with active ad provider data
         */
        List<String> getActiveTenants();

        /**
         * Get tenant usage statistics
         *
         * @param tenantId Tenant identifier
         * @return Usage statistics for the tenant
         */
        AdProviderTenantUsageStats getTenantUsageStats(String tenantId);

        /**
         * Get usage statistics for all tenants
         *
         * @return List of usage statistics for all tenants
         */
        List<AdProviderTenantUsageStats> getAllTenantsUsageStats();

        /**
         * Get system-wide usage statistics (aggregated across all tenants)
         *
         * @return Aggregated usage statistics for the entire system
         */
        AdProviderTenantUsageStats getSystemWideUsageStats();
}