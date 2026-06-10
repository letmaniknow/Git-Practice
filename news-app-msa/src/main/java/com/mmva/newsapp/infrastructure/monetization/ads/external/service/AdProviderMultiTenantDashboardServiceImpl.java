package com.mmva.newsapp.infrastructure.monetization.ads.external.service;

import com.mmva.newsapp.infrastructure.monetization.ads.external.common.dto.AdProviderDashboardOverviewResponse;
import com.mmva.newsapp.infrastructure.monetization.ads.external.common.dto.AdProviderDashboardStats;
import com.mmva.newsapp.infrastructure.monetization.ads.external.common.dto.AdProviderTenantUsageStats;
import com.mmva.newsapp.infrastructure.monetization.ads.external.common.service.AdProviderDashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Multi-tenant dashboard service implementation
 *
 * Provides tenant-aware dashboard operations with cross-tenant analytics
 * and usage statistics for enterprise-level monitoring
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdProviderMultiTenantDashboardServiceImpl implements AdProviderMultiTenantDashboardService {

        private final AdProviderDashboardService adProviderDashboardService;

        @Override
        @Cacheable(value = "adProviderTenantDashboard", key = "#adProviderTenantId + '_' + #startDate + '_' + #endDate")
        public AdProviderDashboardOverviewResponse getTenantDashboardOverview(
                        String adProviderTenantId,
                        LocalDate startDate,
                        LocalDate endDate) {

                log.info("Fetching dashboard overview for tenant: {}, date range: {} to {}",
                                adProviderTenantId, startDate, endDate);

                // Delegate to the main dashboard service with tenant context
                // In a real implementation, this would filter data by tenant
                AdProviderDashboardOverviewResponse response = adProviderDashboardService.getDashboardOverview(
                                adProviderTenantId, startDate, endDate);

                // Add tenant-specific metadata
                response.getAdProviderDashboardMetadata().setAdProviderTenantId(adProviderTenantId);

                log.debug("Retrieved dashboard overview for tenant {} with {} providers",
                                adProviderTenantId,
                                response.getAdProviderDashboardStats().getAdProviderTotalProviders());

                return response;
        }

        @Override
        @Cacheable(value = "adProviderTenantDashboardStats", key = "#adProviderTenantId + '_' + #startDate + '_' + #endDate")
        public AdProviderDashboardStats getTenantDashboardStats(
                        String adProviderTenantId,
                        LocalDate startDate,
                        LocalDate endDate) {

                log.info("Fetching dashboard stats for tenant: {}, date range: {} to {}",
                                adProviderTenantId, startDate, endDate);

                // Get the full overview and extract just the stats
                AdProviderDashboardOverviewResponse overview = adProviderDashboardService.getDashboardOverview(
                                adProviderTenantId, startDate, endDate);

                return overview.getAdProviderDashboardStats();
        }

        @Override
        @Cacheable(value = "adProviderAllTenantsDashboard", key = "#startDate + '_' + #endDate")
        public Map<String, AdProviderDashboardOverviewResponse> getAllTenantsDashboardOverview(
                        LocalDate startDate,
                        LocalDate endDate) {

                log.info("Fetching dashboard overview for all tenants, date range: {} to {}", startDate, endDate);

                // In a real implementation, this would aggregate data across all tenants
                // For now, return a single tenant's data as an example
                AdProviderDashboardOverviewResponse overview = adProviderDashboardService.getDashboardOverview(
                                "default-tenant", startDate, endDate);

                // Simulate multiple tenants (in real implementation, query database for all
                // tenants)
                Map<String, AdProviderDashboardOverviewResponse> tenantOverviews = Map.of(
                                "tenant-1", overview,
                                "tenant-2", overview, // In real impl, this would be different data
                                "tenant-3", overview // In real impl, this would be different data
                );

                log.debug("Retrieved dashboard overview for {} tenants", tenantOverviews.size());

                return tenantOverviews;
        }

        @Override
        @Cacheable(value = "adProviderAllTenantsDashboardStats", key = "#startDate + '_' + #endDate")
        public Map<String, AdProviderDashboardStats> getAllTenantsDashboardStats(
                        LocalDate startDate,
                        LocalDate endDate) {

                log.info("Fetching dashboard stats for all tenants, date range: {} to {}", startDate, endDate);

                // Get overviews and extract stats
                Map<String, AdProviderDashboardOverviewResponse> overviews = getAllTenantsDashboardOverview(startDate,
                                endDate);

                return overviews.entrySet().stream()
                                .collect(Collectors.toMap(
                                                Map.Entry::getKey,
                                                entry -> entry.getValue().getAdProviderDashboardStats()));
        }

        @Override
        @Cacheable(value = "adProviderActiveTenants")
        public List<String> getActiveTenants() {
                log.info("Fetching list of active tenants");

                // In a real implementation, this would query the database for active tenants
                // based on recent activity, data freshness, etc.
                List<String> activeTenants = List.of("tenant-1", "tenant-2", "tenant-3");

                log.debug("Found {} active tenants", activeTenants.size());

                return activeTenants;
        }

        @Override
        @Cacheable(value = "adProviderTenantUsageStats", key = "#adProviderTenantId")
        public AdProviderTenantUsageStats getTenantUsageStats(String adProviderTenantId) {
                log.info("Fetching usage statistics for tenant: {}", adProviderTenantId);

                // In a real implementation, this would aggregate usage data from various
                // sources
                // For now, return mock data
                AdProviderTenantUsageStats stats = AdProviderTenantUsageStats.builder()
                                .adProviderTenantId(adProviderTenantId)
                                .adProviderTotalMetricsRecords(1500L)
                                .adProviderTotalImpressions(new java.math.BigDecimal("125000.50"))
                                .adProviderTotalClicks(new java.math.BigDecimal("2500.75"))
                                .adProviderTotalEarningsUsd(new java.math.BigDecimal("1250.30"))
                                .adProviderActiveProviders(5)
                                .adProviderLastSyncTimestamp(java.time.LocalDateTime.now().minusMinutes(15))
                                .adProviderDataFreshnessMinutes(15L)
                                .adProviderStorageUsageMb(new java.math.BigDecimal("25.5"))
                                .adProviderApiCallsLast24Hours(1250L)
                                .adProviderAverageResponseTimeMs(150L)
                                .build();

                log.debug("Retrieved usage stats for tenant {}: {} records, {} impressions",
                                adProviderTenantId, stats.getAdProviderTotalMetricsRecords(),
                                stats.getAdProviderTotalImpressions());

                return stats;
        }

        @Override
        @Cacheable(value = "adProviderAllTenantsUsageStats")
        public List<AdProviderTenantUsageStats> getAllTenantsUsageStats() {
                log.info("Fetching usage statistics for all tenants");

                // Get active tenants and fetch stats for each
                List<String> activeTenants = getActiveTenants();
                List<AdProviderTenantUsageStats> allStats = activeTenants.stream()
                                .map(this::getTenantUsageStats)
                                .toList();

                log.debug("Retrieved usage stats for {} tenants", allStats.size());

                return allStats;
        }

        @Override
        @Cacheable(value = "adProviderSystemWideUsageStats")
        public AdProviderTenantUsageStats getSystemWideUsageStats() {
                log.info("Fetching system-wide usage statistics");

                // Aggregate stats across all tenants
                List<AdProviderTenantUsageStats> allTenantStats = getAllTenantsUsageStats();

                AdProviderTenantUsageStats systemStats = AdProviderTenantUsageStats.builder()
                                .adProviderTenantId("SYSTEM")
                                .adProviderTotalMetricsRecords(allTenantStats.stream()
                                                .mapToLong(AdProviderTenantUsageStats::getAdProviderTotalMetricsRecords)
                                                .sum())
                                .adProviderTotalImpressions(allTenantStats.stream()
                                                .map(AdProviderTenantUsageStats::getAdProviderTotalImpressions)
                                                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add))
                                .adProviderTotalClicks(allTenantStats.stream()
                                                .map(AdProviderTenantUsageStats::getAdProviderTotalClicks)
                                                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add))
                                .adProviderTotalEarningsUsd(allTenantStats.stream()
                                                .map(AdProviderTenantUsageStats::getAdProviderTotalEarningsUsd)
                                                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add))
                                .adProviderActiveProviders(allTenantStats.stream()
                                                .mapToInt(AdProviderTenantUsageStats::getAdProviderActiveProviders)
                                                .sum())
                                .adProviderLastSyncTimestamp(allTenantStats.stream()
                                                .map(AdProviderTenantUsageStats::getAdProviderLastSyncTimestamp)
                                                .filter(java.util.Objects::nonNull)
                                                .max(java.time.LocalDateTime::compareTo)
                                                .orElse(java.time.LocalDateTime.now()))
                                .adProviderDataFreshnessMinutes(15L) // System-wide freshness
                                .adProviderStorageUsageMb(allTenantStats.stream()
                                                .map(AdProviderTenantUsageStats::getAdProviderStorageUsageMb)
                                                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add))
                                .adProviderApiCallsLast24Hours(allTenantStats.stream()
                                                .mapToLong(AdProviderTenantUsageStats::getAdProviderApiCallsLast24Hours)
                                                .sum())
                                .adProviderAverageResponseTimeMs(150L) // System average
                                .build();

                log.debug("Calculated system-wide usage stats: {} total records, {} total impressions",
                                systemStats.getAdProviderTotalMetricsRecords(),
                                systemStats.getAdProviderTotalImpressions());

                return systemStats;
        }
}