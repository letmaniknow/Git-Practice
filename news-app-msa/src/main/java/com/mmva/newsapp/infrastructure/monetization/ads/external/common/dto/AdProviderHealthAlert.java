package com.mmva.newsapp.infrastructure.monetization.ads.external.common.dto;

import com.mmva.newsapp.infrastructure.monetization.ads.external.common.enums.ProviderType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Health alert DTO for ad provider system issues
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdProviderHealthAlert {

    /**
     * Alert ID (unique identifier)
     */
    private String adProviderAlertId;

    /**
     * Alert type (SYNC_FAILURE, AUTH_FAILURE, DATA_STALE, PERFORMANCE_DROP, etc.)
     */
    private String adProviderAlertType;

    /**
     * Alert severity (CRITICAL, WARNING, INFO)
     */
    private String adProviderAlertSeverity;

    /**
     * Alert title
     */
    private String adProviderAlertTitle;

    /**
     * Alert description
     */
    private String adProviderAlertDescription;

    /**
     * Affected provider (if applicable)
     */
    private ProviderType adProviderAffectedProvider;

    /**
     * Affected account ID (if applicable)
     */
    private String adProviderAffectedAccountId;

    /**
     * Alert timestamp
     */
    private LocalDateTime adProviderAlertTimestamp;

    /**
     * Recommended action
     */
    private String adProviderRecommendedAction;

    /**
     * Alert acknowledged status
     */
    private Boolean adProviderAlertAcknowledged;
}