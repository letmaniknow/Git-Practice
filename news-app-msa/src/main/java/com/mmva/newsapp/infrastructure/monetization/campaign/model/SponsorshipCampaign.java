package com.mmva.newsapp.infrastructure.monetization.campaign.model;

import com.mmva.newsapp.infrastructure.common.audit.model.BaseAuditEntity;
import com.mmva.newsapp.infrastructure.monetization.campaign.enums.SponsorshipCampaignStatus;
import com.mmva.newsapp.infrastructure.monetization.campaign.enums.SponsorshipCampaignType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Entity representing a sponsorship/advertising campaign.
 * 
 * <p>
 * A campaign is a container for sponsored content, ads, or brand partnerships.
 * It defines the budget, duration, targeting, and creative assets.
 * </p>
 * 
 * <h3>Portability Note:</h3>
 * <p>
 * Uses {@code UUID sponsorshipCampaignAdvertiserId} for sponsor identification
 * instead of entity
 * reference.
 * Uses {@code UUID sponsorshipCampaignContentId} for linked content instead of
 * NewsMasterEntity
 * reference.
 * </p>
 * 
 * <h3>Campaign Lifecycle:</h3>
 * 
 * <pre>
 * DRAFT -> PENDING_APPROVAL -> APPROVED -> ACTIVE -> COMPLETED / CANCELLED -> REJECTED
 * </pre>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Entity
@Table(name = "monetization_sponsorship_campaign", indexes = {
        @Index(name = "idx_sponsorship_campaign_advertiser", columnList = "sponsorship_campaign_advertiser_id"),
        @Index(name = "idx_sponsorship_campaign_status", columnList = "sponsorship_campaign_status"),
        @Index(name = "idx_sponsorship_campaign_type", columnList = "sponsorship_campaign_type"),
        @Index(name = "idx_sponsorship_campaign_dates", columnList = "sponsorship_campaign_start_date, sponsorship_campaign_end_date"),
        @Index(name = "idx_sponsorship_campaign_tenant", columnList = "sponsorship_campaign_tenant_id")
})
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class SponsorshipCampaign extends BaseAuditEntity {

    /**
     * Primary key - UUID for global uniqueness.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "sponsorship_campaign_id", updatable = false, nullable = false)
    private UUID sponsorshipCampaignId;

    /**
     * Campaign name for internal reference.
     */
    @Column(name = "sponsorship_campaign_name", nullable = false, length = 200)
    private String sponsorshipCampaignName;

    /**
     * External campaign code for tracking.
     */
    @Column(name = "sponsorship_campaign_code", length = 50)
    private String sponsorshipCampaignCode;

    /**
     * Detailed description of campaign goals and content.
     */
    @Column(name = "sponsorship_campaign_description", length = 2000)
    private String sponsorshipCampaignDescription;

    /**
     * Type of campaign.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "sponsorship_campaign_type", nullable = false, length = 30)
    private SponsorshipCampaignType sponsorshipCampaignType;

    /**
     * Current campaign status.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "sponsorship_campaign_status", nullable = false, length = 20)
    @Builder.Default
    private SponsorshipCampaignStatus sponsorshipCampaignStatus = SponsorshipCampaignStatus.DRAFT;

    // ========================================
    // Advertiser Information
    // ========================================

    /**
     * Advertiser/Sponsor user ID.
     * Portable: Uses UUID instead of entity reference.
     */
    @Column(name = "sponsorship_campaign_advertiser_id", nullable = false)
    private UUID sponsorshipCampaignAdvertiserId;

    /**
     * Advertiser company name.
     */
    @Column(name = "sponsorship_campaign_advertiser_name", nullable = false, length = 200)
    private String sponsorshipCampaignAdvertiserName;

    /**
     * Advertiser contact email.
     */
    @Column(name = "sponsorship_campaign_advertiser_email", length = 255)
    private String sponsorshipCampaignAdvertiserEmail;

    /**
     * Brand name to display on sponsored content.
     */
    @Column(name = "sponsorship_campaign_brand_name", length = 100)
    private String sponsorshipCampaignBrandName;

    /**
     * Brand logo URL.
     */
    @Column(name = "sponsorship_campaign_brand_logo_url", length = 500)
    private String sponsorshipCampaignBrandLogoUrl;

    // ========================================
    // Schedule
    // ========================================

    /**
     * Campaign start date.
     */
    @Column(name = "sponsorship_campaign_start_date", nullable = false)
    private Instant sponsorshipCampaignStartDate;

    /**
     * Campaign end date.
     */
    @Column(name = "sponsorship_campaign_end_date", nullable = false)
    private Instant sponsorshipCampaignEndDate;

    /**
     * Actual date campaign was activated.
     */
    @Column(name = "sponsorship_campaign_activated_at")
    private Instant sponsorshipCampaignActivatedAt;

    /**
     * Actual date campaign was completed/cancelled.
     */
    @Column(name = "sponsorship_campaign_completed_at")
    private Instant sponsorshipCampaignCompletedAt;

    // ========================================
    // Budget & Pricing
    // ========================================

    /**
     * Total campaign budget.
     */
    @Column(name = "sponsorship_campaign_total_budget", nullable = false, precision = 12, scale = 2)
    private BigDecimal sponsorshipCampaignTotalBudget;

    /**
     * Daily budget limit (optional).
     */
    @Column(name = "sponsorship_campaign_daily_budget", precision = 10, scale = 2)
    private BigDecimal sponsorshipCampaignDailyBudget;

    /**
     * Amount spent so far.
     */
    @Column(name = "sponsorship_campaign_amount_spent", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal sponsorshipCampaignAmountSpent = BigDecimal.ZERO;

    /**
     * ISO 4217 currency code.
     */
    @Column(name = "sponsorship_campaign_currency", nullable = false, length = 3)
    @Builder.Default
    private String sponsorshipCampaignCurrency = "USD";

    /**
     * Pricing model: CPM, CPC, FLAT_FEE, COMMISSION.
     */
    @Column(name = "sponsorship_campaign_pricing_model", length = 20)
    @Builder.Default
    private String sponsorshipCampaignPricingModel = "CPM";

    /**
     * Rate for CPM/CPC pricing.
     */
    @Column(name = "sponsorship_campaign_rate", precision = 10, scale = 4)
    private BigDecimal sponsorshipCampaignRate;

    /**
     * Commission percentage for affiliate campaigns.
     */
    @Column(name = "sponsorship_campaign_commission_percent", precision = 5, scale = 2)
    private BigDecimal sponsorshipCampaignCommissionPercent;

    // ========================================
    // Goals & Metrics
    // ========================================

    /**
     * Target number of impressions.
     */
    @Column(name = "sponsorship_campaign_target_impressions")
    private Long sponsorshipCampaignTargetImpressions;

    /**
     * Target number of clicks.
     */
    @Column(name = "sponsorship_campaign_target_clicks")
    private Long sponsorshipCampaignTargetClicks;

    /**
     * Current impression count.
     */
    @Column(name = "sponsorship_campaign_impression_count")
    @Builder.Default
    private Long sponsorshipCampaignImpressionCount = 0L;

    /**
     * Current click count.
     */
    @Column(name = "sponsorship_campaign_click_count")
    @Builder.Default
    private Long sponsorshipCampaignClickCount = 0L;

    /**
     * Current conversion count.
     */
    @Column(name = "sponsorship_campaign_conversion_count")
    @Builder.Default
    private Long sponsorshipCampaignConversionCount = 0L;

    // ========================================
    // Targeting
    // ========================================

    /**
     * Target newscategory IDs (JSON array).
     */
    @Column(name = "sponsorship_campaign_target_categories_json", length = 500)
    private String sponsorshipCampaignTargetCategoriesJson;

    /**
     * Target geo locations (JSON array of country/region codes).
     */
    @Column(name = "sponsorship_campaign_target_locations_json", length = 500)
    private String sponsorshipCampaignTargetLocationsJson;

    /**
     * Target devices: "all", "mobile", "desktop", or JSON array.
     */
    @Column(name = "sponsorship_campaign_target_devices", length = 100)
    @Builder.Default
    private String sponsorshipCampaignTargetDevices = "all";

    /**
     * Target user tiers (JSON array of tier codes).
     * Empty = all tiers.
     */
    @Column(name = "sponsorship_campaign_target_tiers_json", length = 100)
    private String sponsorshipCampaignTargetTiersJson;

    // ========================================
    // Content Reference
    // ========================================

    /**
     * Linked content ID (for sponsored articles).
     * Portable: Uses UUID instead of NewsMasterEntity reference.
     */
    @Column(name = "sponsorship_campaign_content_id")
    private UUID sponsorshipCampaignContentId;

    /**
     * Destination URL for clicks.
     */
    @Column(name = "sponsorship_campaign_destination_url", length = 1000)
    private String sponsorshipCampaignDestinationUrl;

    /**
     * Call-to-action text.
     */
    @Column(name = "sponsorship_campaign_cta_text", length = 50)
    private String sponsorshipCampaignCtaText;

    // ========================================
    // Creative Assets
    // ========================================

    /**
     * Headline text for ads.
     */
    @Column(name = "sponsorship_campaign_headline", length = 150)
    private String sponsorshipCampaignHeadline;

    /**
     * Body/description text.
     */
    @Column(name = "sponsorship_campaign_body_text", length = 500)
    private String sponsorshipCampaignBodyText;

    /**
     * Primary image URL.
     */
    @Column(name = "sponsorship_campaign_image_url", length = 500)
    private String sponsorshipCampaignImageUrl;

    /**
     * Video URL (for video campaigns).
     */
    @Column(name = "sponsorship_campaign_video_url", length = 500)
    private String sponsorshipCampaignVideoUrl;

    /**
     * Additional creative assets JSON.
     */
    @Column(name = "sponsorship_campaign_creative_assets_json", columnDefinition = "TEXT")
    private String sponsorshipCampaignCreativeAssetsJson;

    // ========================================
    // Approval & Tracking
    // ========================================

    /**
     * Who approved the campaign.
     */
    @Column(name = "sponsorship_campaign_approved_by")
    private UUID sponsorshipCampaignApprovedBy;

    /**
     * When the campaign was approved.
     */
    @Column(name = "sponsorship_campaign_approved_at")
    private Instant sponsorshipCampaignApprovedAt;

    /**
     * Rejection reason if rejected.
     */
    @Column(name = "sponsorship_campaign_rejection_reason", length = 500)
    private String sponsorshipCampaignRejectionReason;

    /**
     * UTM campaign code for tracking.
     */
    @Column(name = "sponsorship_campaign_utm_campaign", length = 100)
    private String sponsorshipCampaignUtmCampaign;

    /**
     * UTM source.
     */
    @Column(name = "sponsorship_campaign_utm_source", length = 100)
    private String sponsorshipCampaignUtmSource;

    /**
     * UTM medium.
     */
    @Column(name = "sponsorship_campaign_utm_medium", length = 100)
    private String sponsorshipCampaignUtmMedium;

    // ========================================
    // Multi-tenant
    // ========================================

    /**
     * Tenant identifier for multi-app support.
     */
    @Column(name = "sponsorship_campaign_tenant_id", nullable = false, length = 50)
    @Builder.Default
    private String sponsorshipCampaignTenantId = "default";

    /**
     * Metadata JSON for additional flexible data.
     */
    @Column(name = "sponsorship_campaign_metadata_json", columnDefinition = "TEXT")
    private String sponsorshipCampaignMetadataJson;

    // ========================================
    // Business Methods
    // ========================================

    /**
     * Checks if campaign is currently active.
     *
     * @return true if serving content
     */
    public boolean isActive() {
        return sponsorshipCampaignStatus != null && sponsorshipCampaignStatus.isServingContent();
    }

    /**
     * Checks if campaign is within its scheduled dates.
     *
     * @return true if current time is between start and end dates
     */
    public boolean isWithinSchedule() {
        Instant now = Instant.now();
        return sponsorshipCampaignStartDate != null && sponsorshipCampaignEndDate != null
                && now.isAfter(sponsorshipCampaignStartDate) && now.isBefore(sponsorshipCampaignEndDate);
    }

    /**
     * Checks if campaign has remaining budget.
     *
     * @return true if budget not exhausted
     */
    public boolean hasRemainingBudget() {
        if (sponsorshipCampaignTotalBudget == null)
            return false;
        BigDecimal spent = sponsorshipCampaignAmountSpent != null ? sponsorshipCampaignAmountSpent : BigDecimal.ZERO;
        return spent.compareTo(sponsorshipCampaignTotalBudget) < 0;
    }

    /**
     * Gets remaining budget amount.
     *
     * @return remaining budget
     */
    public BigDecimal getRemainingBudget() {
        if (sponsorshipCampaignTotalBudget == null)
            return BigDecimal.ZERO;
        BigDecimal spent = sponsorshipCampaignAmountSpent != null ? sponsorshipCampaignAmountSpent : BigDecimal.ZERO;
        return sponsorshipCampaignTotalBudget.subtract(spent);
    }

    /**
     * Calculates budget utilization percentage.
     *
     * @return utilization 0-100
     */
    public BigDecimal getBudgetUtilization() {
        if (sponsorshipCampaignTotalBudget == null || sponsorshipCampaignTotalBudget.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal spent = sponsorshipCampaignAmountSpent != null ? sponsorshipCampaignAmountSpent : BigDecimal.ZERO;
        return spent.divide(sponsorshipCampaignTotalBudget, 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    /**
     * Calculates click-through rate (CTR).
     *
     * @return CTR as percentage
     */
    public BigDecimal getClickThroughRate() {
        if (sponsorshipCampaignImpressionCount == null || sponsorshipCampaignImpressionCount == 0) {
            return BigDecimal.ZERO;
        }
        long clicks = sponsorshipCampaignClickCount != null ? sponsorshipCampaignClickCount : 0L;
        return BigDecimal.valueOf(clicks)
                .divide(BigDecimal.valueOf(sponsorshipCampaignImpressionCount), 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    /**
     * Gets days remaining in campaign.
     *
     * @return days remaining, 0 if ended
     */
    public long getDaysRemaining() {
        if (sponsorshipCampaignEndDate == null)
            return 0;
        long seconds = sponsorshipCampaignEndDate.getEpochSecond() - Instant.now().getEpochSecond();
        return Math.max(0, seconds / 86400);
    }
}
