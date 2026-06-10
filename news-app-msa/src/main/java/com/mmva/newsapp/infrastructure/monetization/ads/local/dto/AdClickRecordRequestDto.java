package com.mmva.newsapp.infrastructure.monetization.ads.local.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

/**
 * Request DTO for recording an ad click.
 * 
 * <p>
 * Naming follows PROJECT_PRINCIPLES.md Section 6.1:
 * {@code {Feature}{Action}{Direction}Dto} pattern where Feature=AdClick,
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
public class AdClickRecordRequestDto {

    /**
     * Campaign that received the click.
     */
    @NotNull(message = "Campaign ID is required")
    private UUID adClickCampaignId;

    /**
     * Placement where click occurred.
     */
    @NotNull(message = "Placement ID is required")
    private UUID adClickPlacementId;

    /**
     * Associated impression (for CTR tracking).
     */
    private UUID adClickImpressionId;

    /**
     * User who clicked (optional for anonymous).
     */
    private UUID adClickUserId;

    /**
     * Content where ad was displayed.
     */
    private UUID adClickContentId;

    /**
     * Destination URL.
     */
    private String adClickDestinationUrl;

    /**
     * Type of click: PRIMARY, SECONDARY, LOGO, etc.
     */
    private String adClickType;

    /**
     * Element that was clicked.
     */
    private String adClickClickedElement;

    /**
     * X coordinate (% of ad width).
     */
    private Double adClickX;

    /**
     * Y coordinate (% of ad height).
     */
    private Double adClickY;

    /**
     * Source page URL.
     */
    private String adClickSourcePageUrl;

    /**
     * Referrer URL.
     */
    private String adClickReferrerUrl;

    /**
     * Device type.
     */
    private String adClickDeviceType;

    /**
     * Browser name.
     */
    private String adClickBrowser;

    /**
     * Operating system.
     */
    private String adClickOs;

    /**
     * Session identifier.
     */
    private String adClickSessionId;

    /**
     * Time since impression in milliseconds.
     */
    private Long adClickTimeSinceImpressionMs;

    // ========================================
    // UTM Parameters
    // ========================================

    private String adClickUtmSource;
    private String adClickUtmMedium;
    private String adClickUtmCampaign;
    private String adClickUtmContent;
    private String adClickUtmTerm;
}
