package com.mmva.newsapp.infrastructure.monetization.ads.external.common.dto;

import com.mmva.newsapp.infrastructure.monetization.ads.external.common.enums.ProviderType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Response DTO for ad providers dashboard overview
 *
 * Contains aggregated statistics and summary data for all ad providers
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdProviderDashboardOverviewResponse {

    /**
     * Dashboard summary statistics
     */
    private AdProviderDashboardStats adProviderDashboardStats;

    /**
     * Metrics grouped by provider type
     */
    private Map<ProviderType, List<AdProviderMetricsSummary>> adProviderMetricsByProvider;

    /**
     * Recent activity (last 10 sync operations)
     */
    private List<AdProviderRecentActivity> adProviderRecentActivities;

    /**
     * System health indicators
     */
    private AdProviderDashboardHealth adProviderDashboardHealth;

    /**
     * Response metadata
     */
    private AdProviderDashboardMetadata adProviderDashboardMetadata;
}