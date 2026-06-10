package com.mmva.newsapp.infrastructure.monetization.ads.external.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Tenant usage statistics DTO
 *
 * Contains usage metrics for a specific tenant
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdProviderTenantUsageStats {

    /**
     * Tenant identifier
     */
    private String adProviderTenantId;

    /**
     * Total number of metrics records for this tenant
     */
    private Long adProviderTotalMetricsRecords;

    /**
     * Total impressions across all providers for this tenant
     */
    private BigDecimal adProviderTotalImpressions;

    /**
     * Total clicks across all providers for this tenant
     */
    private BigDecimal adProviderTotalClicks;

    /**
     * Total earnings across all providers for this tenant
     */
    private BigDecimal adProviderTotalEarningsUsd;

    /**
     * Number of active providers for this tenant
     */
    private Integer adProviderActiveProviders;

    /**
     * Last sync timestamp for this tenant
     */
    private LocalDateTime adProviderLastSyncTimestamp;

    /**
     * Data freshness in minutes (how old is the latest data)
     */
    private Long adProviderDataFreshnessMinutes;

    /**
     * Storage usage in MB (estimated based on metrics records)
     */
    private BigDecimal adProviderStorageUsageMb;

    /**
     * API calls made in last 24 hours
     */
    private Long adProviderApiCallsLast24Hours;

    /**
     * Average response time for dashboard API calls (ms)
     */
    private Long adProviderAverageResponseTimeMs;
}