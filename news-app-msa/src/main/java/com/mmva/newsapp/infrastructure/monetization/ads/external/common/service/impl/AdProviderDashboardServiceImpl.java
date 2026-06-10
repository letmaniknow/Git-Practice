package com.mmva.newsapp.infrastructure.monetization.ads.external.common.service.impl;

import com.mmva.newsapp.infrastructure.monetization.ads.external.common.dto.*;
import com.mmva.newsapp.infrastructure.monetization.ads.external.common.enums.ProviderType;
import com.mmva.newsapp.infrastructure.monetization.ads.external.common.service.AdProviderDashboardService;
import com.mmva.newsapp.infrastructure.monetization.ads.external.persistence.entity.ProviderMetricsSync;
import com.mmva.newsapp.infrastructure.monetization.ads.external.persistence.service.ProviderMetricsSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of ad providers dashboard service
 *
 * Provides business logic for dashboard data aggregation, statistics
 * calculation,
 * and analytics operations
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdProviderDashboardServiceImpl implements AdProviderDashboardService {

        private final ProviderMetricsSyncService providerMetricsSyncService;

        private static final int DEFAULT_ACTIVITY_LIMIT = 10;
        private static final int DEFAULT_LOOKBACK_DAYS = 30;

        @Override
        @Transactional(readOnly = true)
        @Cacheable(value = "dashboardOverview", key = "#tenantId + '_' + #startDate + '_' + #endDate", unless = "#result == null")
        public AdProviderDashboardOverviewResponse getDashboardOverview(
                        String tenantId,
                        LocalDate startDate,
                        LocalDate endDate) {

                log.debug("📊 Generating dashboard overview for tenant: {}, date range: {} to {}",
                                tenantId, startDate, endDate);

                LocalDateTime requestStart = LocalDateTime.now();

                // Set default date range if not provided
                if (startDate == null) {
                        startDate = LocalDate.now().minusDays(DEFAULT_LOOKBACK_DAYS);
                }
                if (endDate == null) {
                        endDate = LocalDate.now();
                }

                // Get all metrics for the date range
                List<AdProviderMetricsDto> allMetrics = providerMetricsSyncService.getMetricsByTenantAndDateRange(
                                tenantId, startDate, endDate);

                // Calculate dashboard statistics
                AdProviderDashboardStats stats = calculateDashboardStats(allMetrics, tenantId, startDate, endDate);

                // Group metrics by provider
                Map<ProviderType, List<AdProviderMetricsSummary>> metricsByProvider = groupMetricsByProvider(
                                allMetrics);

                // Get recent activity
                List<AdProviderRecentActivity> recentActivities = getRecentActivity(tenantId, DEFAULT_ACTIVITY_LIMIT);

                // Get health indicators
                AdProviderDashboardHealth health = getDashboardHealth(tenantId);

                // Create metadata
                AdProviderDashboardMetadata metadata = AdProviderDashboardMetadata.builder()
                                .adProviderRequestTimestamp(requestStart)
                                .adProviderResponseTimestamp(LocalDateTime.now())
                                .adProviderProcessingTimeMs(
                                                ChronoUnit.MILLIS.between(requestStart, LocalDateTime.now()))
                                .adProviderDataStartDate(startDate)
                                .adProviderDataEndDate(endDate)
                                .adProviderTenantId(tenantId)
                                .adProviderApiVersion("v1")
                                .adProviderRequestId(UUID.randomUUID().toString())
                                .adProviderCacheStatus("CALCULATED")
                                .adProviderDataSource("DATABASE")
                                .adProviderTotalRecordsProcessed((long) allMetrics.size())
                                .build();

                return AdProviderDashboardOverviewResponse.builder()
                                .adProviderDashboardStats(stats)
                                .adProviderMetricsByProvider(metricsByProvider)
                                .adProviderRecentActivities(recentActivities)
                                .adProviderDashboardHealth(health)
                                .adProviderDashboardMetadata(metadata)
                                .build();
        }

        @Override
        @Transactional(readOnly = true)
        @Cacheable(value = "dashboardStats", key = "#tenantId + '_' + #startDate + '_' + #endDate", unless = "#result == null")
        public AdProviderDashboardStats getDashboardStats(
                        String tenantId,
                        LocalDate startDate,
                        LocalDate endDate) {

                log.debug("📈 Calculating dashboard stats for tenant: {}, date range: {} to {}",
                                tenantId, startDate, endDate);

                List<AdProviderMetricsDto> metrics = providerMetricsSyncService.getMetricsByTenantAndDateRange(
                                tenantId, startDate, endDate);

                return calculateDashboardStats(metrics, tenantId, startDate, endDate);
        }

        @Override
        @Transactional(readOnly = true)
        @Cacheable(value = "metricsByProvider", key = "#tenantId + '_' + #startDate + '_' + #endDate", unless = "#result.isEmpty()")
        public Map<ProviderType, List<AdProviderMetricsSummary>> getMetricsByProvider(
                        String tenantId,
                        LocalDate startDate,
                        LocalDate endDate) {

                log.debug("📋 Grouping metrics by provider for tenant: {}, date range: {} to {}",
                                tenantId, startDate, endDate);

                List<AdProviderMetricsDto> metrics = providerMetricsSyncService.getMetricsByTenantAndDateRange(
                                tenantId, startDate, endDate);

                return groupMetricsByProvider(metrics);
        }

        @Override
        @Transactional(readOnly = true)
        @Cacheable(value = "recentActivity", key = "#tenantId + '_' + #limit", unless = "#result.isEmpty()")
        public List<AdProviderRecentActivity> getRecentActivity(String tenantId, Integer limit) {
                log.debug("🔄 Getting recent activity for tenant: {}, limit: {}", tenantId, limit);

                if (limit == null || limit <= 0) {
                        limit = DEFAULT_ACTIVITY_LIMIT;
                }

                // Get recent metrics syncs (this is a simplified implementation)
                // In a real implementation, you'd have an audit log table
                List<AdProviderMetricsDto> recentMetrics = providerMetricsSyncService.getRecentMetricsByTenant(
                                tenantId, limit);

                return recentMetrics.stream()
                                .map(this::convertToRecentActivity)
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional(readOnly = true)
        @Cacheable(value = "dashboardHealth", key = "#tenantId", unless = "#result == null")
        public AdProviderDashboardHealth getDashboardHealth(String tenantId) {
                log.debug("🏥 Checking dashboard health for tenant: {}", tenantId);

                // Get all enabled providers
                List<ProviderType> enabledProviders = Arrays.stream(ProviderType.values())
                                .filter(ProviderType::isSupported)
                                .collect(Collectors.toList());

                // Get last sync times
                LocalDateTime lastSuccessfulSync = providerMetricsSyncService.getLastSuccessfulSyncTime(tenantId);
                long minutesSinceLastSync = lastSuccessfulSync != null
                                ? ChronoUnit.MINUTES.between(lastSuccessfulSync, LocalDateTime.now())
                                : Long.MAX_VALUE;

                // Calculate health status
                String healthStatus = calculateHealthStatus(minutesSinceLastSync);

                // Check for stale providers (no sync in last 24 hours)
                int staleProviders = providerMetricsSyncService.countStaleProviders(tenantId, 24);

                // Generate alerts
                List<AdProviderHealthAlert> alerts = generateHealthAlerts(tenantId, minutesSinceLastSync,
                                staleProviders);

                // Calculate freshness score (0-100)
                int freshnessScore = calculateFreshnessScore(minutesSinceLastSync);

                return AdProviderDashboardHealth.builder()
                                .adProviderSystemHealthStatus(healthStatus)
                                .adProviderLastSuccessfulSync(lastSuccessfulSync)
                                .adProviderProvidersWithFailures(0) // TODO: Implement failure tracking
                                .adProviderStaleProviders(staleProviders)
                                .adProviderDataFreshnessScore(freshnessScore)
                                .adProviderActiveAlerts(alerts)
                                .adProviderSystemRecommendations(generateRecommendations(healthStatus, alerts))
                                .build();
        }

        @Override
        @Transactional(readOnly = true)
        public List<AdProviderMetricsSummary> getProviderMetrics(
                        String tenantId,
                        ProviderType providerType,
                        LocalDate startDate,
                        LocalDate endDate) {

                log.debug("📊 Getting metrics for provider: {} in tenant: {}, date range: {} to {}",
                                providerType, tenantId, startDate, endDate);

                List<AdProviderMetricsDto> metrics = providerMetricsSyncService.getMetricsByProviderAndDateRange(
                                tenantId, providerType, startDate, endDate);

                return metrics.stream()
                                .map(this::convertToMetricsSummary)
                                .collect(Collectors.toList());
        }

        // Private helper methods

        private AdProviderDashboardStats calculateDashboardStats(
                        List<AdProviderMetricsDto> metrics,
                        String tenantId,
                        LocalDate startDate,
                        LocalDate endDate) {

                if (metrics.isEmpty()) {
                        return createEmptyStats();
                }

                // Calculate totals
                BigDecimal totalImpressions = metrics.stream()
                                .map(AdProviderMetricsDto::getAdProviderTotalImpressions)
                                .filter(Objects::nonNull)
                                .map(BigDecimal::valueOf)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal totalClicks = metrics.stream()
                                .map(AdProviderMetricsDto::getAdProviderTotalClicks)
                                .filter(Objects::nonNull)
                                .map(BigDecimal::valueOf)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal totalEarnings = metrics.stream()
                                .map(AdProviderMetricsDto::getAdProviderEstimatedEarningsUsd)
                                .filter(Objects::nonNull)
                                .map(BigDecimal::valueOf)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                // Calculate averages
                BigDecimal avgCtr = calculateAverage(metrics, AdProviderMetricsDto::getAdProviderCtrPercentage);
                BigDecimal avgCpm = calculateAverage(metrics, AdProviderMetricsDto::getAdProviderCpmUsd);
                BigDecimal avgCpc = calculateAverage(metrics, AdProviderMetricsDto::getAdProviderCpcUsd);

                // Find top performers
                String topByEarnings = findTopProviderByMetric(metrics,
                                AdProviderMetricsDto::getAdProviderEstimatedEarningsUsd);
                String topByCtr = findTopProviderByMetric(metrics, AdProviderMetricsDto::getAdProviderCtrPercentage);

                return AdProviderDashboardStats.builder()
                                .adProviderTotalProviders(getUniqueProviderCount(metrics))
                                .adProviderActiveProviders(getActiveProviderCount())
                                .adProviderTotalMetricsRecords((long) metrics.size())
                                .adProviderTotalImpressions(totalImpressions)
                                .adProviderTotalClicks(totalClicks)
                                .adProviderTotalEarningsUsd(totalEarnings)
                                .adProviderAverageCtrPercentage(avgCtr)
                                .adProviderAverageCpmUsd(avgCpm)
                                .adProviderAverageCpcUsd(avgCpc)
                                .adProviderTopProviderByEarnings(topByEarnings)
                                .adProviderTopProviderByCtr(topByCtr)
                                .adProviderSyncFailuresLast24Hours(0) // TODO: Implement sync failure tracking
                                .adProviderDataFreshnessMinutes(0L) // TODO: Implement data freshness calculation
                                .build();
        }

        private Map<ProviderType, List<AdProviderMetricsSummary>> groupMetricsByProvider(
                        List<AdProviderMetricsDto> metrics) {
                return metrics.stream()
                                .collect(Collectors.groupingBy(
                                                AdProviderMetricsDto::getAdProviderType,
                                                Collectors.mapping(this::convertToMetricsSummary,
                                                                Collectors.toList())));
        }

        private AdProviderMetricsSummary convertToMetricsSummary(AdProviderMetricsDto dto) {
                return AdProviderMetricsSummary.builder()
                                .adProviderType(dto.getAdProviderType())
                                .adProviderAccountId(dto.getAdProviderAccountId())
                                .adProviderAccountName(dto.getAdProviderAccountName())
                                .adProviderMetricsPeriodStart(dto.getAdProviderMetricsPeriodStart())
                                .adProviderMetricsPeriodEnd(dto.getAdProviderMetricsPeriodEnd())
                                .adProviderTotalImpressions(BigDecimal.valueOf(dto.getAdProviderTotalImpressions()))
                                .adProviderTotalClicks(BigDecimal.valueOf(dto.getAdProviderTotalClicks()))
                                .adProviderEstimatedEarningsUsd(dto.getAdProviderEstimatedEarningsUsd() != null
                                                ? BigDecimal.valueOf(dto.getAdProviderEstimatedEarningsUsd())
                                                : BigDecimal.ZERO)
                                .adProviderCtrPercentage(dto.getAdProviderCtrPercentage() != null
                                                ? BigDecimal.valueOf(dto.getAdProviderCtrPercentage())
                                                : BigDecimal.ZERO)
                                .adProviderCpmUsd(dto.getAdProviderCpmUsd() != null
                                                ? BigDecimal.valueOf(dto.getAdProviderCpmUsd())
                                                : BigDecimal.ZERO)
                                .adProviderCpcUsd(dto.getAdProviderCpcUsd() != null
                                                ? BigDecimal.valueOf(dto.getAdProviderCpcUsd())
                                                : BigDecimal.ZERO)
                                .adProviderSyncedAt(dto.getAdProviderSyncedAt() != null
                                                ? LocalDateTime.ofInstant(dto.getAdProviderSyncedAt(),
                                                                java.time.ZoneOffset.UTC)
                                                : LocalDateTime.now() != null
                                                                ? LocalDateTime.ofInstant(dto.getAdProviderSyncedAt(),
                                                                                java.time.ZoneOffset.UTC)
                                                                : LocalDateTime.now())
                                .adProviderSyncStatus(dto.getAdProviderSyncStatus())
                                .adProviderMetadataJson(
                                                dto.getAdProviderMetadata() != null
                                                                ? dto.getAdProviderMetadata().toString()
                                                                : null)
                                .build();
        }

        private AdProviderRecentActivity convertToRecentActivity(AdProviderMetricsDto dto) {
                return AdProviderRecentActivity.builder()
                                .adProviderType(dto.getAdProviderType())
                                .adProviderAccountId(dto.getAdProviderAccountId())
                                .adProviderActivityType("SYNC_SUCCESS")
                                .adProviderActivityDescription(String.format("Synced metrics for %s (%s to %s)",
                                                dto.getAdProviderType().getDisplayName(),
                                                dto.getAdProviderMetricsPeriodStart(),
                                                dto.getAdProviderMetricsPeriodEnd()))
                                .adProviderActivityTimestamp(dto.getAdProviderSyncedAt() != null
                                                ? LocalDateTime.ofInstant(dto.getAdProviderSyncedAt(),
                                                                java.time.ZoneOffset.UTC)
                                                : LocalDateTime.now())
                                .adProviderActivityImpressions(BigDecimal.valueOf(dto.getAdProviderTotalImpressions()))
                                .adProviderActivityClicks(BigDecimal.valueOf(dto.getAdProviderTotalClicks()))
                                .adProviderActivityEarningsUsd(dto.getAdProviderEstimatedEarningsUsd() != null
                                                ? BigDecimal.valueOf(dto.getAdProviderEstimatedEarningsUsd())
                                                : BigDecimal.ZERO)
                                .adProviderActivityStatus("SUCCESS")
                                .build();
        }

        private AdProviderDashboardStats createEmptyStats() {
                return AdProviderDashboardStats.builder()
                                .adProviderTotalProviders(0)
                                .adProviderActiveProviders(0)
                                .adProviderTotalMetricsRecords(0L)
                                .adProviderTotalImpressions(BigDecimal.ZERO)
                                .adProviderTotalClicks(BigDecimal.ZERO)
                                .adProviderTotalEarningsUsd(BigDecimal.ZERO)
                                .adProviderAverageCtrPercentage(BigDecimal.ZERO)
                                .adProviderAverageCpmUsd(BigDecimal.ZERO)
                                .adProviderAverageCpcUsd(BigDecimal.ZERO)
                                .adProviderTopProviderByEarnings(null)
                                .adProviderTopProviderByCtr(null)
                                .adProviderSyncFailuresLast24Hours(0)
                                .adProviderDataFreshnessMinutes(0L)
                                .build();
        }

        private int getUniqueProviderCount(List<AdProviderMetricsDto> metrics) {
                return (int) metrics.stream()
                                .map(AdProviderMetricsDto::getAdProviderType)
                                .distinct()
                                .count();
        }

        private int getActiveProviderCount() {
                return (int) Arrays.stream(ProviderType.values())
                                .filter(ProviderType::isSupported)
                                .count();
        }

        private BigDecimal calculateAverage(List<AdProviderMetricsDto> metrics,
                        java.util.function.Function<AdProviderMetricsDto, Double> extractor) {
                return metrics.stream()
                                .map(extractor)
                                .filter(Objects::nonNull)
                                .mapToDouble(Double::doubleValue)
                                .average()
                                .stream()
                                .mapToObj(BigDecimal::valueOf)
                                .findFirst()
                                .orElse(BigDecimal.ZERO)
                                .setScale(2, RoundingMode.HALF_UP);
        }

        private String findTopProviderByMetric(List<AdProviderMetricsDto> metrics,
                        java.util.function.Function<AdProviderMetricsDto, Double> extractor) {
                return metrics.stream()
                                .filter(dto -> extractor.apply(dto) != null)
                                .max(Comparator.comparing(extractor))
                                .map(dto -> dto.getAdProviderType().getDisplayName())
                                .orElse(null);
        }

        private String calculateHealthStatus(long minutesSinceLastSync) {
                if (minutesSinceLastSync < 60)
                        return "HEALTHY"; // Less than 1 hour
                if (minutesSinceLastSync < 1440)
                        return "WARNING"; // Less than 24 hours
                return "CRITICAL"; // More than 24 hours
        }

        private List<AdProviderHealthAlert> generateHealthAlerts(String tenantId, long minutesSinceLastSync,
                        int staleProviders) {
                List<AdProviderHealthAlert> alerts = new ArrayList<>();

                if (minutesSinceLastSync > 1440) { // 24 hours
                        alerts.add(AdProviderHealthAlert.builder()
                                        .adProviderAlertId(UUID.randomUUID().toString())
                                        .adProviderAlertType("DATA_STALE")
                                        .adProviderAlertSeverity("CRITICAL")
                                        .adProviderAlertTitle("Data is stale")
                                        .adProviderAlertDescription("No successful sync in the last 24 hours")
                                        .adProviderAlertTimestamp(LocalDateTime.now())
                                        .adProviderRecommendedAction(
                                                        "Check sync job status and provider configurations")
                                        .adProviderAlertAcknowledged(false)
                                        .build());
                }

                if (staleProviders > 0) {
                        alerts.add(AdProviderHealthAlert.builder()
                                        .adProviderAlertId(UUID.randomUUID().toString())
                                        .adProviderAlertType("STALE_PROVIDERS")
                                        .adProviderAlertSeverity("WARNING")
                                        .adProviderAlertTitle("Stale provider data")
                                        .adProviderAlertDescription(
                                                        staleProviders + " providers have not synced in 24 hours")
                                        .adProviderAlertTimestamp(LocalDateTime.now())
                                        .adProviderRecommendedAction("Review provider configurations and API access")
                                        .adProviderAlertAcknowledged(false)
                                        .build());
                }

                return alerts;
        }

        private int calculateFreshnessScore(long minutesSinceLastSync) {
                if (minutesSinceLastSync < 60)
                        return 100; // Very fresh
                if (minutesSinceLastSync < 360)
                        return 80; // Fresh
                if (minutesSinceLastSync < 1440)
                        return 60; // Getting stale
                if (minutesSinceLastSync < 4320)
                        return 30; // Stale
                return 0; // Very stale
        }

        private List<String> generateRecommendations(String healthStatus, List<AdProviderHealthAlert> alerts) {
                List<String> recommendations = new ArrayList<>();

                if ("CRITICAL".equals(healthStatus)) {
                        recommendations.add("Immediate attention required: Check sync job and provider configurations");
                }

                if (alerts.stream().anyMatch(alert -> "DATA_STALE".equals(alert.getAdProviderAlertType()))) {
                        recommendations.add("Review scheduled sync job configuration");
                        recommendations.add("Verify API credentials for all providers");
                }

                if (alerts.stream().anyMatch(alert -> "STALE_PROVIDERS".equals(alert.getAdProviderAlertType()))) {
                        recommendations.add("Check individual provider API access and rate limits");
                }

                if (recommendations.isEmpty()) {
                        recommendations.add("System is operating normally");
                }

                return recommendations;
        }
}