package com.mmva.newsapp.infrastructure.monetization.ads.local.dto;

import com.mmva.newsapp.infrastructure.monetization.ads.local.enums.AdType;
import com.mmva.newsapp.infrastructure.monetization.ads.local.enums.PlacementPosition;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for Ad Placement with computed fields.
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdPlacementResponseDto {

    private UUID adPlacementId;
    private String adPlacementCode;
    private String adPlacementName;
    private String adPlacementDescription;

    // ========================================
    // Placement Configuration
    // ========================================

    private AdType adPlacementAdType;
    private String adPlacementAdTypeDisplayName;
    private PlacementPosition adPlacementPosition;
    private String adPlacementPositionDisplayName;
    private String adPlacementPageType;
    private String adPlacementContainerId;

    // ========================================
    // Dimensions
    // ========================================

    private Integer adPlacementWidth;
    private Integer adPlacementHeight;
    private Boolean adPlacementIsResponsive;
    private String adPlacementDimensionsDisplay;

    // ========================================
    // Pricing
    // ========================================

    private BigDecimal adPlacementBaseCpmRate;
    private BigDecimal adPlacementBaseCpcRate;
    private BigDecimal adPlacementEffectiveCpmRate;
    private BigDecimal adPlacementMinBidAmount;
    private Boolean adPlacementIsPremium;
    private String adPlacementFormattedCpmRate;
    private String adPlacementFormattedCpcRate;

    // ========================================
    // Targeting Configuration
    // ========================================

    private String adPlacementAllowedCategoriesJson;
    private String adPlacementSupportedDevicesJson;
    private String adPlacementTargetTiersJson;

    // ========================================
    // Display Rules
    // ========================================

    private Integer adPlacementMaxAdsPerPage;
    private Integer adPlacementRefreshIntervalSeconds;
    private Boolean adPlacementLazyLoadEnabled;
    private Integer adPlacementViewabilityThreshold;

    // ========================================
    // Status
    // ========================================

    private Boolean adPlacementIsActive;
    private Boolean adPlacementIsServing;
    private Integer adPlacementDisplayOrder;

    // ========================================
    // Statistics
    // ========================================

    private Long adPlacementTotalImpressions;
    private Long adPlacementTotalClicks;
    private BigDecimal adPlacementTotalRevenue;
    private Double adPlacementClickThroughRate;
    private BigDecimal adPlacementRevenuePerMille;
    private Instant adPlacementLastImpressionAt;
    private String adPlacementFormattedRevenue;

    // ========================================
    // External Integration
    // ========================================

    private String adPlacementExternalPlacementId;
    private String adPlacementExternalAdServer;

    // ========================================
    // Audit
    // ========================================

    private Instant adPlacementCreatedAt;
    private UUID adPlacementCreatedBy;
    private Instant adPlacementUpdatedAt;
    private UUID adPlacementUpdatedBy;
    private String adPlacementTenantId;
}
