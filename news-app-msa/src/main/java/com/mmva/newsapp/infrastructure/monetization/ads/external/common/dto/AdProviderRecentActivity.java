package com.mmva.newsapp.infrastructure.monetization.ads.external.common.dto;

import com.mmva.newsapp.infrastructure.monetization.ads.external.common.enums.ProviderType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for recent ad provider activity
 *
 * Shows the most recent sync operations and their results
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdProviderRecentActivity {

    /**
     * Provider type
     */
    private ProviderType adProviderType;

    /**
     * Provider account ID
     */
    private String adProviderAccountId;

    /**
     * Activity type (SYNC_SUCCESS, SYNC_FAILED, SYNC_PARTIAL, AUTH_SUCCESS, etc.)
     */
    private String adProviderActivityType;

    /**
     * Activity description
     */
    private String adProviderActivityDescription;

    /**
     * Activity timestamp
     */
    private LocalDateTime adProviderActivityTimestamp;

    /**
     * Metrics summary (if applicable)
     */
    private BigDecimal adProviderActivityImpressions;

    /**
     * Clicks summary (if applicable)
     */
    private BigDecimal adProviderActivityClicks;

    /**
     * Earnings summary (if applicable)
     */
    private BigDecimal adProviderActivityEarningsUsd;

    /**
     * Activity status (SUCCESS, FAILED, WARNING)
     */
    private String adProviderActivityStatus;

    /**
     * Error message (if activity failed)
     */
    private String adProviderActivityErrorMessage;
}