package com.mmva.newsapp.infrastructure.monetization.ads.external.common.dto;

import com.mmva.newsapp.infrastructure.monetization.ads.external.common.enums.ProviderType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Summary DTO for provider metrics data
 *
 * Contains aggregated metrics for a specific provider and date range
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdProviderMetricsSummary {

    /**
     * Provider type
     */
    private ProviderType adProviderType;

    /**
     * Provider account ID
     */
    private String adProviderAccountId;

    /**
     * Provider account name (human readable)
     */
    private String adProviderAccountName;

    /**
     * Metrics period start date
     */
    private LocalDate adProviderMetricsPeriodStart;

    /**
     * Metrics period end date
     */
    private LocalDate adProviderMetricsPeriodEnd;

    /**
     * Total impressions for this period
     */
    private BigDecimal adProviderTotalImpressions;

    /**
     * Total clicks for this period
     */
    private BigDecimal adProviderTotalClicks;

    /**
     * Estimated earnings in USD for this period
     */
    private BigDecimal adProviderEstimatedEarningsUsd;

    /**
     * Click-through rate percentage
     */
    private BigDecimal adProviderCtrPercentage;

    /**
     * Cost per mille (CPM) in USD
     */
    private BigDecimal adProviderCpmUsd;

    /**
     * Cost per click (CPC) in USD
     */
    private BigDecimal adProviderCpcUsd;

    /**
     * When these metrics were last synced
     */
    private LocalDateTime adProviderSyncedAt;

    /**
     * Sync status (SUCCESS, FAILED, PARTIAL)
     */
    private String adProviderSyncStatus;

    /**
     * Provider-specific metadata (key-value pairs)
     */
    private String adProviderMetadataJson;
}