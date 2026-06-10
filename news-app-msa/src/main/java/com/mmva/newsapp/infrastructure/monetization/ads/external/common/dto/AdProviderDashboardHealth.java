package com.mmva.newsapp.infrastructure.monetization.ads.external.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Dashboard health indicators DTO
 *
 * Contains system health information and alerts
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdProviderDashboardHealth {

    /**
     * Overall system health status (HEALTHY, WARNING, CRITICAL)
     */
    private String adProviderSystemHealthStatus;

    /**
     * Last successful sync timestamp
     */
    private LocalDateTime adProviderLastSuccessfulSync;

    /**
     * Number of providers with sync failures
     */
    private Integer adProviderProvidersWithFailures;

    /**
     * Number of stale providers (no sync in last 24 hours)
     */
    private Integer adProviderStaleProviders;

    /**
     * Data freshness score (0-100, higher is better)
     */
    private Integer adProviderDataFreshnessScore;

    /**
     * Active alerts list
     */
    private List<AdProviderHealthAlert> adProviderActiveAlerts;

    /**
     * System recommendations
     */
    private List<String> adProviderSystemRecommendations;
}