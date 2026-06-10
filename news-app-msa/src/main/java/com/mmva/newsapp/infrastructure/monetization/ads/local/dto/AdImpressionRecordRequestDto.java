package com.mmva.newsapp.infrastructure.monetization.ads.local.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

/**
 * Request DTO for recording an ad impression.
 * 
 * <p>
 * Naming follows PROJECT_PRINCIPLES.md Section 6.1:
 * {@code {Feature}{Action}{Direction}Dto} pattern where Feature=AdImpression,
 * Action=Record, Direction=Request.
 * </p>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdImpressionRecordRequestDto {

    /**
     * Campaign being served.
     */
    @NotNull(message = "Campaign ID is required")
    private UUID adImpressionCampaignId;

    /**
     * Placement where ad is displayed.
     */
    @NotNull(message = "Placement ID is required")
    private UUID adImpressionPlacementId;

    /**
     * User viewing the ad (optional for anonymous).
     */
    private UUID adImpressionUserId;

    /**
     * Content where ad is displayed.
     */
    private UUID adImpressionContentId;

    /**
     * Whether the ad is viewable (meets threshold).
     */
    private Boolean adImpressionIsViewable;

    /**
     * How long the ad was visible (ms).
     */
    private Long adImpressionViewDurationMs;

    /**
     * Percentage of ad visible (0-100).
     */
    private Integer adImpressionVisibilityPercent;

    /**
     * Page URL.
     */
    private String adImpressionPageUrl;

    /**
     * Page title.
     */
    private String adImpressionPageTitle;

    /**
     * Content newscategory.
     */
    private String adImpressionContentCategory;

    /**
     * Device type: DESKTOP, MOBILE, TABLET.
     */
    private String adImpressionDeviceType;

    /**
     * Browser name.
     */
    private String adImpressionBrowser;

    /**
     * Operating system.
     */
    private String adImpressionOs;

    /**
     * Screen resolution.
     */
    private String adImpressionScreenResolution;

    /**
     * Session identifier.
     */
    private String adImpressionSessionId;
}
