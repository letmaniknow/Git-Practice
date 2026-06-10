package com.mmva.newsapp.infrastructure.monetization.ads.external.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Dashboard statistics DTO containing aggregated metrics across all providers
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdProviderDashboardStats {

    /**
     * Total number of configured providers
     */
    private Integer adProviderTotalProviders;

    /**
     * Number of active/enabled providers
     */
    private Integer adProviderActiveProviders;

    /**
     * Total number of metrics records
     */
    private Long adProviderTotalMetricsRecords;

    /**
     * Total impressions across all providers
     */
    private BigDecimal adProviderTotalImpressions;

    /**
     * Total clicks across all providers
     */
    private BigDecimal adProviderTotalClicks;

    /**
     * Total earnings across all providers in USD
     */
    private BigDecimal adProviderTotalEarningsUsd;

    /**
     * Average CTR across all providers (percentage)
     */
    private BigDecimal adProviderAverageCtrPercentage;

    /**
     * Average CPM across all providers in USD
     */
    private BigDecimal adProviderAverageCpmUsd;

    /**
     * Average CPC across all providers in USD
     */
    private BigDecimal adProviderAverageCpcUsd;

    /**
     * Best performing provider by earnings
     */
    private String adProviderTopProviderByEarnings;

    /**
     * Best performing provider by CTR
     */
    private String adProviderTopProviderByCtr;

    /**
     * Total sync failures in last 24 hours
     */
    private Integer adProviderSyncFailuresLast24Hours;

    /**
     * Data freshness indicator (minutes since last successful sync)
     */
    private Long adProviderDataFreshnessMinutes;
}