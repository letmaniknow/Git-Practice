package com.mmva.newsapp.infrastructure.monetization.campaign.dto;

import com.mmva.newsapp.infrastructure.monetization.campaign.enums.SponsorshipCampaignType;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Request DTO for creating or updating a Sponsorship Campaign.
 * All fields use sponsorshipCampaign prefix for consistency.
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SponsorshipCampaignRequestDto {

    /**
     * Unique campaign code (auto-generated if not provided on create).
     */
    @Pattern(regexp = "^[A-Z0-9_-]{0,50}$", message = "Campaign code must contain only uppercase letters, numbers, underscores, and hyphens")
    private String sponsorshipCampaignCode;

    /**
     * Campaign display name.
     */
    @NotBlank(message = "Campaign name is required")
    @Size(max = 200, message = "Campaign name must not exceed 200 characters")
    private String sponsorshipCampaignName;

    /**
     * Campaign description.
     */
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String sponsorshipCampaignDescription;

    /**
     * Type of campaign.
     */
    @NotNull(message = "Campaign type is required")
    private SponsorshipCampaignType sponsorshipCampaignType;

    // ========================================
    // Advertiser Information
    // ========================================

    /**
     * Advertiser ID (external user/company reference).
     */
    @NotNull(message = "Advertiser ID is required")
    private UUID sponsorshipCampaignAdvertiserId;

    /**
     * Advertiser name/company.
     */
    @NotBlank(message = "Advertiser name is required")
    @Size(max = 200, message = "Advertiser name must not exceed 200 characters")
    private String sponsorshipCampaignAdvertiserName;

    /**
     * Contact email for the advertiser.
     */
    @Email(message = "Advertiser email must be valid")
    @Size(max = 200, message = "Advertiser email must not exceed 200 characters")
    private String sponsorshipCampaignAdvertiserEmail;

    /**
     * Brand name (may differ from advertiser name).
     */
    @Size(max = 200, message = "Brand name must not exceed 200 characters")
    private String sponsorshipCampaignBrandName;

    /**
     * Brand logo URL.
     */
    @Size(max = 500, message = "Brand logo URL must not exceed 500 characters")
    private String sponsorshipCampaignBrandLogoUrl;

    // ========================================
    // Schedule
    // ========================================

    /**
     * Campaign start date.
     */
    @NotNull(message = "Start date is required")
    @FutureOrPresent(message = "Start date must be in the present or future")
    private Instant sponsorshipCampaignStartDate;

    /**
     * Campaign end date.
     */
    @NotNull(message = "End date is required")
    @Future(message = "End date must be in the future")
    private Instant sponsorshipCampaignEndDate;

    // ========================================
    // Budget & Pricing
    // ========================================

    /**
     * Total campaign budget.
     */
    @NotNull(message = "Total budget is required")
    @DecimalMin(value = "1.00", message = "Total budget must be at least $1.00")
    @Digits(integer = 13, fraction = 2, message = "Invalid budget format")
    private BigDecimal sponsorshipCampaignTotalBudget;

    /**
     * Daily spending limit (optional).
     */
    @DecimalMin(value = "0.00", message = "Daily budget must be non-negative")
    @Digits(integer = 10, fraction = 2, message = "Invalid daily budget format")
    private BigDecimal sponsorshipCampaignDailyBudget;

    /**
     * Pricing model: CPM, CPC, or FLAT_FEE.
     */
    @NotBlank(message = "Pricing model is required")
    @Pattern(regexp = "^(CPM|CPC|FLAT_FEE)$", message = "Pricing model must be CPM, CPC, or FLAT_FEE")
    private String sponsorshipCampaignPricingModel;

    /**
     * Rate based on pricing model (CPM rate, CPC rate, or flat fee).
     */
    @NotNull(message = "Rate is required")
    @DecimalMin(value = "0.01", message = "Rate must be at least $0.01")
    @Digits(integer = 10, fraction = 4, message = "Invalid rate format")
    private BigDecimal sponsorshipCampaignRate;

    // ========================================
    // Goals
    // ========================================

    /**
     * Target number of impressions.
     */
    @Min(value = 0, message = "Target impressions must be non-negative")
    private Long sponsorshipCampaignTargetImpressions;

    /**
     * Target number of clicks.
     */
    @Min(value = 0, message = "Target clicks must be non-negative")
    private Long sponsorshipCampaignTargetClicks;

    // ========================================
    // Targeting
    // ========================================

    /**
     * Target newscategory codes (JSON array).
     */
    @Size(max = 2000, message = "Target categories JSON must not exceed 2000 characters")
    private String sponsorshipCampaignTargetCategoriesJson;

    /**
     * Target geographic locations (JSON array).
     */
    @Size(max = 2000, message = "Target locations JSON must not exceed 2000 characters")
    private String sponsorshipCampaignTargetLocationsJson;

    /**
     * Target devices (comma-separated: DESKTOP,MOBILE,TABLET).
     */
    @Size(max = 200, message = "Target devices must not exceed 200 characters")
    private String sponsorshipCampaignTargetDevices;

    /**
     * Target subscription tiers (JSON array).
     */
    @Size(max = 200, message = "Target tiers JSON must not exceed 200 characters")
    private String sponsorshipCampaignTargetTiersJson;

    // ========================================
    // Content Reference
    // ========================================

    /**
     * Associated content ID for sponsored articles.
     */
    private UUID sponsorshipCampaignContentId;

    /**
     * Destination URL when ad is clicked.
     */
    @Size(max = 1000, message = "Destination URL must not exceed 1000 characters")
    private String sponsorshipCampaignDestinationUrl;

    /**
     * Call-to-action button text.
     */
    @Size(max = 100, message = "CTA text must not exceed 100 characters")
    private String sponsorshipCampaignCtaText;

    // ========================================
    // Creative Assets
    // ========================================

    /**
     * Ad headline.
     */
    @Size(max = 200, message = "Headline must not exceed 200 characters")
    private String sponsorshipCampaignHeadline;

    /**
     * Ad body text.
     */
    @Size(max = 1000, message = "Body text must not exceed 1000 characters")
    private String sponsorshipCampaignBodyText;

    /**
     * Primary image URL.
     */
    @Size(max = 500, message = "Image URL must not exceed 500 characters")
    private String sponsorshipCampaignImageUrl;

    /**
     * Video URL (if applicable).
     */
    @Size(max = 500, message = "Video URL must not exceed 500 characters")
    private String sponsorshipCampaignVideoUrl;

    /**
     * Additional creative assets (JSON).
     */
    @Size(max = 5000, message = "Creative assets JSON must not exceed 5000 characters")
    private String sponsorshipCampaignCreativeAssetsJson;

    // ========================================
    // UTM Tracking
    // ========================================

    /**
     * UTM campaign parameter.
     */
    @Size(max = 200, message = "UTM campaign must not exceed 200 characters")
    private String sponsorshipCampaignUtmCampaign;

    /**
     * UTM source parameter.
     */
    @Size(max = 200, message = "UTM source must not exceed 200 characters")
    private String sponsorshipCampaignUtmSource;

    /**
     * UTM medium parameter.
     */
    @Size(max = 200, message = "UTM medium must not exceed 200 characters")
    private String sponsorshipCampaignUtmMedium;
}
