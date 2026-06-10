package com.mmva.newsapp.infrastructure.monetization.ads.local.dto;

import com.mmva.newsapp.infrastructure.monetization.ads.local.enums.AdType;
import com.mmva.newsapp.infrastructure.monetization.ads.local.enums.PlacementPosition;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Request DTO for creating or updating an Ad Placement.
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdPlacementRequestDto {

    /**
     * Unique code for this placement.
     */
    @NotBlank(message = "Placement code is required")
    @Pattern(regexp = "^[A-Z0-9_-]{1,100}$", message = "Placement code must contain only uppercase letters, numbers, underscores, and hyphens")
    private String adPlacementCode;

    /**
     * Human-readable name for the placement.
     */
    @NotBlank(message = "Placement name is required")
    @Size(max = 200, message = "Placement name must not exceed 200 characters")
    private String adPlacementName;

    /**
     * Detailed description.
     */
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String adPlacementDescription;

    // ========================================
    // Placement Configuration
    // ========================================

    /**
     * Type of ad this placement supports.
     */
    @NotNull(message = "Ad type is required")
    private AdType adPlacementAdType;

    /**
     * Position/location on the page.
     */
    @NotNull(message = "Position is required")
    private PlacementPosition adPlacementPosition;

    /**
     * Page or section where this placement exists.
     */
    @NotBlank(message = "Page type is required")
    @Size(max = 100, message = "Page type must not exceed 100 characters")
    private String adPlacementPageType;

    /**
     * Custom container ID in the frontend.
     */
    @Size(max = 100, message = "Container ID must not exceed 100 characters")
    private String adPlacementContainerId;

    // ========================================
    // Dimensions
    // ========================================

    /**
     * Width in pixels (null if responsive).
     */
    @Min(value = 0, message = "Width must be non-negative")
    @Max(value = 10000, message = "Width must not exceed 10000")
    private Integer adPlacementWidth;

    /**
     * Height in pixels (null if responsive).
     */
    @Min(value = 0, message = "Height must be non-negative")
    @Max(value = 10000, message = "Height must not exceed 10000")
    private Integer adPlacementHeight;

    /**
     * Whether dimensions are responsive/flexible.
     */
    private Boolean adPlacementIsResponsive;

    // ========================================
    // Pricing
    // ========================================

    /**
     * Base CPM rate for this placement.
     */
    @DecimalMin(value = "0.00", message = "CPM rate must be non-negative")
    @Digits(integer = 10, fraction = 4, message = "Invalid CPM rate format")
    private BigDecimal adPlacementBaseCpmRate;

    /**
     * Base CPC rate for this placement.
     */
    @DecimalMin(value = "0.00", message = "CPC rate must be non-negative")
    @Digits(integer = 10, fraction = 4, message = "Invalid CPC rate format")
    private BigDecimal adPlacementBaseCpcRate;

    /**
     * Minimum bid amount.
     */
    @DecimalMin(value = "0.00", message = "Minimum bid must be non-negative")
    @Digits(integer = 10, fraction = 2, message = "Invalid minimum bid format")
    private BigDecimal adPlacementMinBidAmount;

    /**
     * Whether this is a premium placement.
     */
    private Boolean adPlacementIsPremium;

    // ========================================
    // Targeting Configuration
    // ========================================

    /**
     * Category codes this placement is restricted to (JSON array).
     */
    @Size(max = 4000, message = "Allowed categories JSON must not exceed 4000 characters")
    private String adPlacementAllowedCategoriesJson;

    /**
     * Device types this placement supports (JSON array).
     */
    @Size(max = 500, message = "Supported devices JSON must not exceed 500 characters")
    private String adPlacementSupportedDevicesJson;

    /**
     * User tiers allowed to see ads.
     */
    @Size(max = 200, message = "Target tiers JSON must not exceed 200 characters")
    private String adPlacementTargetTiersJson;

    // ========================================
    // Display Rules
    // ========================================

    /**
     * Maximum number of ads to show per page.
     */
    @Min(value = 1, message = "Max ads per page must be at least 1")
    @Max(value = 100, message = "Max ads per page must not exceed 100")
    private Integer adPlacementMaxAdsPerPage;

    /**
     * Refresh interval in seconds (0 means no refresh).
     */
    @Min(value = 0, message = "Refresh interval must be non-negative")
    @Max(value = 3600, message = "Refresh interval must not exceed 3600 seconds")
    private Integer adPlacementRefreshIntervalSeconds;

    /**
     * Whether lazy loading is enabled.
     */
    private Boolean adPlacementLazyLoadEnabled;

    /**
     * Viewability threshold percentage (0-100).
     */
    @Min(value = 0, message = "Viewability threshold must be at least 0")
    @Max(value = 100, message = "Viewability threshold must not exceed 100")
    private Integer adPlacementViewabilityThreshold;

    // ========================================
    // Status
    // ========================================

    /**
     * Whether this placement is active.
     */
    private Boolean adPlacementIsActive;

    /**
     * Display order for sorting.
     */
    @Min(value = 0, message = "Display order must be non-negative")
    private Integer adPlacementDisplayOrder;

    // ========================================
    // External Integration
    // ========================================

    /**
     * External ad server placement ID.
     */
    @Size(max = 200, message = "External placement ID must not exceed 200 characters")
    private String adPlacementExternalPlacementId;

    /**
     * External ad server name.
     */
    @Size(max = 100, message = "External ad server must not exceed 100 characters")
    private String adPlacementExternalAdServer;
}
