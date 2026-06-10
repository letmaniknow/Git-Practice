package com.mmva.newsapp.infrastructure.monetization.ads.external.common.dto;

import com.mmva.newsapp.infrastructure.monetization.ads.external.common.enums.ProviderType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Unified DTO for metrics from ANY ad provider
 * 
 * This is a normalized representation that all providers must implement.
 * Provider-specific fields stored in {@link #providerMetadata}
 * 
 * Naming Convention:
 * - adProvider{MetricName} (provider-specific fields)
 * - {metricName}Usd (fields in USD currency)
 * - {metricName}Percentage (percentage fields)
 * - ProviderXxx (provider-related fields)
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdProviderMetricsDto {

    // ========================================
    // Provider Identification
    // ========================================

    /**
     * Which provider this metrics comes from
     * Example: GOOGLE_ADSENSE, GOOGLE_ADMOB
     */
    private ProviderType adProviderType;

    /**
     * Provider-specific account/property ID
     * Example: "pub-123456", "ca-app-123456"
     */
    private String adProviderAccountId;

    /**
     * Provider-specific account name (for display)
     */
    private String adProviderAccountName;

    // ========================================
    // Core Metrics (All providers support these)
    // ========================================

    /**
     * Total ad impressions (view count)
     * Definition: Number of times an ad was displayed to a user
     */
    private Long adProviderTotalImpressions;

    /**
     * Total ad clicks
     * Definition: Number of times a user clicked on an ad
     */
    private Long adProviderTotalClicks;

    /**
     * Estimated revenue in USD
     * Definition: Google's estimate of earnings from this provider
     */
    private Double adProviderEstimatedEarningsUsd;

    // ========================================
    // Calculated Metrics (Derived from core)
    // ========================================

    /**
     * Click-Through Rate (CTR) as percentage
     * Formula: (clicks / impressions) * 100
     * Example: 2.5 = 2.5%
     */
    private Double adProviderCtrPercentage;

    /**
     * Cost Per Mille (CPM) in USD
     * Definition: Earnings per 1000 impressions
     * Formula: (estimatedEarnings / impressions) * 1000
     * Example: 2.50 = $2.50 per 1000 impressions
     */
    private Double adProviderCpmUsd;

    /**
     * Cost Per Click (CPC) in USD
     * Definition: Average earnings per click
     * Formula: estimatedEarnings / clicks
     * Example: 0.50 = $0.50 per click
     */
    private Double adProviderCpcUsd;

    // ========================================
    // Timestamp & Period
    // ========================================

    /**
     * Start date of metrics period
     * Format: YYYY-MM-DD
     */
    private LocalDate adProviderMetricsPeriodStart;

    /**
     * End date of metrics period
     * Format: YYYY-MM-DD
     */
    private LocalDate adProviderMetricsPeriodEnd;

    /**
     * When these metrics were synchronized from provider
     * ISO 8601 format with timezone
     */
    private Instant adProviderSyncedAt;

    // ========================================
    // Provider-Specific Extended Data
    // ========================================

    /**
     * Provider-specific metadata stored as key-value pairs
     * 
     * Examples:
     * - Google AdSense: {"region": "US", "account_status": "ACTIVE"}
     * - Google AdMob: {"app_id": "xyz", "ad_unit_count": "5"}
     * - Facebook: {"audience_size": "10000", "cpm_range": "0.5-5.0"}
     */
    @Builder.Default
    private Map<String, Object> adProviderMetadata = new HashMap<>();

    // ========================================
    // Sync Status & Audit
    // ========================================

    /**
     * Source of sync: MANUAL, SCHEDULED, WEBHOOK, API
     */
    private String adProviderSyncSource;

    /**
     * Sync status: SUCCESS, FAILED, PARTIAL
     */
    private String adProviderSyncStatus;

    /**
     * Error message if sync failed
     */
    private String adProviderSyncErrorMessage;

    /**
     * Multi-tenant isolation
     */
    private String tenantId;

    // ========================================
    // Utility Methods
    // ========================================

    /**
     * Check if metrics are valid
     */
    public boolean isValid() {
        return adProviderTotalImpressions != null && adProviderTotalImpressions >= 0
                && adProviderTotalClicks != null && adProviderTotalClicks >= 0
                && adProviderEstimatedEarningsUsd != null && adProviderEstimatedEarningsUsd >= 0;
    }

    /**
     * Get provider display name
     */
    public String getProviderDisplayName() {
        return adProviderType != null ? adProviderType.getDisplayName() : "Unknown";
    }

    /**
     * Add provider-specific metadata
     */
    public void addProviderMetadata(String key, Object value) {
        if (adProviderMetadata == null) {
            adProviderMetadata = new HashMap<>();
        }
        adProviderMetadata.put(key, value);
    }

    /**
     * Get provider-specific metadata
     */
    public Object getProviderMetadata(String key) {
        return adProviderMetadata != null ? adProviderMetadata.get(key) : null;
    }
}
