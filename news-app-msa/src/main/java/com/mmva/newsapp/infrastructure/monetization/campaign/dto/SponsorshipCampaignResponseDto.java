package com.mmva.newsapp.infrastructure.monetization.campaign.dto;

import com.mmva.newsapp.infrastructure.monetization.campaign.enums.SponsorshipCampaignStatus;
import com.mmva.newsapp.infrastructure.monetization.campaign.enums.SponsorshipCampaignType;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for Sponsorship Campaign with computed fields.
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
public class SponsorshipCampaignResponseDto {

    private UUID sponsorshipCampaignId;
    private String sponsorshipCampaignCode;
    private String sponsorshipCampaignName;
    private String sponsorshipCampaignDescription;

    // ========================================
    // Status & Type
    // ========================================

    private SponsorshipCampaignStatus sponsorshipCampaignStatus;
    private String sponsorshipCampaignStatusDisplayName;
    private SponsorshipCampaignType sponsorshipCampaignType;
    private String sponsorshipCampaignTypeDisplayName;
    private Boolean sponsorshipCampaignIsActive;
    private Boolean sponsorshipCampaignIsServingContent;

    // ========================================
    // Advertiser Information
    // ========================================

    private UUID sponsorshipCampaignAdvertiserId;
    private String sponsorshipCampaignAdvertiserName;
    private String sponsorshipCampaignAdvertiserEmail;
    private String sponsorshipCampaignBrandName;
    private String sponsorshipCampaignBrandLogoUrl;

    // ========================================
    // Schedule
    // ========================================

    private Instant sponsorshipCampaignStartDate;
    private Instant sponsorshipCampaignEndDate;
    private Instant sponsorshipCampaignActivatedAt;
    private Instant sponsorshipCampaignCompletedAt;
    private Integer sponsorshipCampaignDaysRemaining;
    private Integer sponsorshipCampaignTotalDays;
    private Double sponsorshipCampaignPercentComplete;

    // ========================================
    // Budget & Pricing
    // ========================================

    private BigDecimal sponsorshipCampaignTotalBudget;
    private BigDecimal sponsorshipCampaignDailyBudget;
    private BigDecimal sponsorshipCampaignAmountSpent;
    private BigDecimal sponsorshipCampaignRemainingBudget;
    private String sponsorshipCampaignPricingModel;
    private BigDecimal sponsorshipCampaignRate;
    private Double sponsorshipCampaignBudgetUtilization;
    private String sponsorshipCampaignFormattedBudget;
    private String sponsorshipCampaignFormattedSpent;

    // ========================================
    // Goals & Performance
    // ========================================

    private Long sponsorshipCampaignTargetImpressions;
    private Long sponsorshipCampaignTargetClicks;
    private Long sponsorshipCampaignImpressionCount;
    private Long sponsorshipCampaignClickCount;
    private Long sponsorshipCampaignConversionCount;
    private Double sponsorshipCampaignClickThroughRate;
    private Double sponsorshipCampaignConversionRate;
    private Double sponsorshipCampaignImpressionGoalProgress;
    private Double sponsorshipCampaignClickGoalProgress;

    // ========================================
    // Targeting
    // ========================================

    private String sponsorshipCampaignTargetCategoriesJson;
    private String sponsorshipCampaignTargetLocationsJson;
    private String sponsorshipCampaignTargetDevices;
    private String sponsorshipCampaignTargetTiersJson;

    // ========================================
    // Content Reference
    // ========================================

    private UUID sponsorshipCampaignContentId;
    private String sponsorshipCampaignDestinationUrl;
    private String sponsorshipCampaignCtaText;

    // ========================================
    // Creative Assets
    // ========================================

    private String sponsorshipCampaignHeadline;
    private String sponsorshipCampaignBodyText;
    private String sponsorshipCampaignImageUrl;
    private String sponsorshipCampaignVideoUrl;
    private String sponsorshipCampaignCreativeAssetsJson;

    // ========================================
    // Approval
    // ========================================

    private UUID sponsorshipCampaignApprovedBy;
    private Instant sponsorshipCampaignApprovedAt;
    private String sponsorshipCampaignRejectionReason;
    private Boolean sponsorshipCampaignIsPendingApproval;

    // ========================================
    // UTM Tracking
    // ========================================

    private String sponsorshipCampaignUtmCampaign;
    private String sponsorshipCampaignUtmSource;
    private String sponsorshipCampaignUtmMedium;
    private String sponsorshipCampaignTrackingUrl;

    // ========================================
    // Audit
    // ========================================

    private Instant sponsorshipCampaignCreatedAt;
    private UUID sponsorshipCampaignCreatedBy;
    private Instant sponsorshipCampaignUpdatedAt;
    private UUID sponsorshipCampaignUpdatedBy;
    private String sponsorshipCampaignTenantId;
}
