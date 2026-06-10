package com.mmva.newsapp.infrastructure.monetization.ads.local.model;

import com.mmva.newsapp.infrastructure.common.audit.model.BaseAuditEntity;
import com.mmva.newsapp.infrastructure.monetization.ads.local.enums.AdType;
import com.mmva.newsapp.infrastructure.monetization.ads.local.enums.PlacementPosition;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Entity representing an Ad Placement - a designated slot where ads can be
 * displayed.
 * 
 * Ad placements define WHERE ads appear in the application.
 * They are linked to campaigns which provide WHAT content to display.
 * 
 * Portability Note: This entity uses UUID references instead of entity
 * relationships
 * to ensure the monetization module can be copied to other applications.
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Entity
@Table(name = "monetization_ad_placement", indexes = {
        @Index(name = "idx_ad_placement_code", columnList = "ad_placement_code"),
        @Index(name = "idx_ad_placement_position", columnList = "ad_placement_position"),
        @Index(name = "idx_ad_placement_tenant", columnList = "ad_placement_tenant_id"),
        @Index(name = "idx_ad_placement_active", columnList = "ad_placement_is_active")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_ad_placement_code_tenant", columnNames = { "ad_placement_code",
                "ad_placement_tenant_id" })
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class AdPlacement extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ad_placement_id", updatable = false, nullable = false)
    private UUID adPlacementId;

    /**
     * Unique code for this placement (e.g., "HOME_HEADER_BANNER",
     * "ARTICLE_SIDEBAR_1").
     */
    @Column(name = "ad_placement_code", nullable = false, length = 100)
    private String adPlacementCode;

    /**
     * Human-readable name for the placement.
     */
    @Column(name = "ad_placement_name", nullable = false, length = 200)
    private String adPlacementName;

    /**
     * Detailed description of where this placement appears.
     */
    @Column(name = "ad_placement_description", length = 1000)
    private String adPlacementDescription;

    // ========================================
    // Placement Configuration
    // ========================================

    /**
     * Type of ad this placement supports.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "ad_placement_ad_type", nullable = false, length = 50)
    private AdType adPlacementAdType;

    /**
     * Position/location of this placement on the page.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "ad_placement_position", nullable = false, length = 50)
    private PlacementPosition adPlacementPosition;

    /**
     * Page or section where this placement exists.
     * Examples: "home", "newsapp", "newscategory", "search_results"
     */
    @Column(name = "ad_placement_page_type", nullable = false, length = 100)
    private String adPlacementPageType;

    /**
     * Custom container ID in the frontend (for integration).
     */
    @Column(name = "ad_placement_container_id", length = 100)
    private String adPlacementContainerId;

    // ========================================
    // Dimensions
    // ========================================

    /**
     * Width in pixels (null if responsive).
     */
    @Column(name = "ad_placement_width")
    private Integer adPlacementWidth;

    /**
     * Height in pixels (null if responsive).
     */
    @Column(name = "ad_placement_height")
    private Integer adPlacementHeight;

    /**
     * Whether dimensions are responsive/flexible.
     */
    @Column(name = "ad_placement_is_responsive", nullable = false)
    @Builder.Default
    private Boolean adPlacementIsResponsive = true;

    // ========================================
    // Pricing
    // ========================================

    /**
     * Base CPM rate for this placement.
     */
    @Column(name = "ad_placement_base_cpm_rate", precision = 10, scale = 4)
    @Builder.Default
    private BigDecimal adPlacementBaseCpmRate = BigDecimal.ZERO;

    /**
     * Base CPC rate for this placement.
     */
    @Column(name = "ad_placement_base_cpc_rate", precision = 10, scale = 4)
    @Builder.Default
    private BigDecimal adPlacementBaseCpcRate = BigDecimal.ZERO;

    /**
     * Minimum bid amount for this placement (for auction model).
     */
    @Column(name = "ad_placement_min_bid_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal adPlacementMinBidAmount = BigDecimal.ZERO;

    /**
     * Whether this is a premium placement with higher rates.
     */
    @Column(name = "ad_placement_is_premium", nullable = false)
    @Builder.Default
    private Boolean adPlacementIsPremium = false;

    // ========================================
    // Targeting Configuration
    // ========================================

    /**
     * Category codes this placement is restricted to (JSON array).
     * Empty means all categories.
     */
    @Column(name = "ad_placement_allowed_categories_json", columnDefinition = "TEXT")
    private String adPlacementAllowedCategoriesJson;

    /**
     * Device types this placement supports (JSON array).
     * Examples: ["DESKTOP", "MOBILE", "TABLET"]
     */
    @Column(name = "ad_placement_supported_devices_json", length = 500)
    private String adPlacementSupportedDevicesJson;

    /**
     * User tiers allowed to see ads on this placement.
     * Empty means show to all users.
     */
    @Column(name = "ad_placement_target_tiers_json", length = 200)
    private String adPlacementTargetTiersJson;

    // ========================================
    // Display Rules
    // ========================================

    /**
     * Maximum number of ads to show in this placement per page.
     */
    @Column(name = "ad_placement_max_ads_per_page", nullable = false)
    @Builder.Default
    private Integer adPlacementMaxAdsPerPage = 1;

    /**
     * Refresh interval in seconds (0 means no refresh).
     */
    @Column(name = "ad_placement_refresh_interval_seconds", nullable = false)
    @Builder.Default
    private Integer adPlacementRefreshIntervalSeconds = 0;

    /**
     * Whether lazy loading is enabled for this placement.
     */
    @Column(name = "ad_placement_lazy_load_enabled", nullable = false)
    @Builder.Default
    private Boolean adPlacementLazyLoadEnabled = false;

    /**
     * Viewability threshold percentage (0-100).
     * Impressions only count when this percentage is visible.
     */
    @Column(name = "ad_placement_viewability_threshold", nullable = false)
    @Builder.Default
    private Integer adPlacementViewabilityThreshold = 50;

    // ========================================
    // Status
    // ========================================

    /**
     * Whether this placement is active.
     */
    @Column(name = "ad_placement_is_active", nullable = false)
    @Builder.Default
    private Boolean adPlacementIsActive = true;

    /**
     * Display order (for sorting placements).
     */
    @Column(name = "ad_placement_display_order", nullable = false)
    @Builder.Default
    private Integer adPlacementDisplayOrder = 0;

    // ========================================
    // Statistics (Denormalized for Performance)
    // ========================================

    /**
     * Total impressions served on this placement.
     */
    @Column(name = "ad_placement_total_impressions", nullable = false)
    @Builder.Default
    private Long adPlacementTotalImpressions = 0L;

    /**
     * Total clicks on this placement.
     */
    @Column(name = "ad_placement_total_clicks", nullable = false)
    @Builder.Default
    private Long adPlacementTotalClicks = 0L;

    /**
     * Total revenue generated from this placement.
     */
    @Column(name = "ad_placement_total_revenue", precision = 15, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal adPlacementTotalRevenue = BigDecimal.ZERO;

    /**
     * Last time an ad was served on this placement.
     */
    @Column(name = "ad_placement_last_impression_at")
    private Instant adPlacementLastImpressionAt;

    // ========================================
    // External Integration
    // ========================================

    /**
     * External ad server placement ID (e.g., Google Ad Manager).
     */
    @Column(name = "ad_placement_external_placement_id", length = 200)
    private String adPlacementExternalPlacementId;

    /**
     * External ad server name.
     */
    @Column(name = "ad_placement_external_ad_server", length = 100)
    private String adPlacementExternalAdServer;

    // ========================================
    // Multi-tenancy
    // ========================================

    /**
     * Tenant identifier for multi-app support.
     */
    @Column(name = "ad_placement_tenant_id", nullable = false, length = 100)
    @Builder.Default
    private String adPlacementTenantId = "default";

    // ========================================
    // Business Methods
    // ========================================

    /**
     * Checks if this placement is currently serving ads.
     */
    public boolean isServing() {
        return Boolean.TRUE.equals(adPlacementIsActive) && !isDeleted();
    }

    /**
     * Gets the effective CPM rate based on position premium.
     */
    public BigDecimal getEffectiveCpmRate() {
        if (adPlacementBaseCpmRate == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal multiplier = adPlacementPosition != null
                ? BigDecimal.valueOf(adPlacementPosition.getCpmMultiplier())
                : BigDecimal.ONE;
        return adPlacementBaseCpmRate.multiply(multiplier);
    }

    /**
     * Gets click-through rate for this placement.
     */
    public double getClickThroughRate() {
        if (adPlacementTotalImpressions == null || adPlacementTotalImpressions == 0) {
            return 0.0;
        }
        return (double) (adPlacementTotalClicks != null ? adPlacementTotalClicks : 0)
                / adPlacementTotalImpressions * 100;
    }

    /**
     * Gets dimensions as a string (e.g., "728x90" or "Responsive").
     */
    public String getDimensionsDisplay() {
        if (Boolean.TRUE.equals(adPlacementIsResponsive)) {
            return "Responsive";
        }
        if (adPlacementWidth != null && adPlacementHeight != null) {
            return adPlacementWidth + "x" + adPlacementHeight;
        }
        return adPlacementAdType != null ? adPlacementAdType.getDimensions() : "Unknown";
    }

    /**
     * Gets average revenue per impression (RPM).
     */
    public BigDecimal getRevenuePerMille() {
        if (adPlacementTotalImpressions == null || adPlacementTotalImpressions == 0) {
            return BigDecimal.ZERO;
        }
        return adPlacementTotalRevenue.multiply(BigDecimal.valueOf(1000))
                .divide(BigDecimal.valueOf(adPlacementTotalImpressions), 4,
                        java.math.RoundingMode.HALF_UP);
    }
}
