package com.mmva.newsapp.domain.newsletter.dto.core;

import com.mmva.newsapp.domain.newsletter.enums.core.NewsletterCampaignStatus;
import com.mmva.newsapp.domain.newsletter.enums.core.NewsletterCampaignType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * DTO for newsletter campaign responses.
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Builder
@Data
@Schema(description = "Newsletter campaign response")
public class NewsletterCampaignResponseDto {

    @Schema(description = "Campaign ID", example = "1")
    private Long newsletterCampaignId;

    @Schema(description = "Campaign name", example = "Weekly Tech Roundup")
    private String newsletterCampaignName;

    @Schema(description = "Campaign subject line", example = "Your Weekly Tech Roundup is Here!")
    private String newsletterCampaignSubject;

    @Schema(description = "Campaign description", example = "Weekly summary of technology news and trends")
    private String newsletterCampaignDescription;

    @Schema(description = "Campaign status", example = "SENT")
    private NewsletterCampaignStatus newsletterCampaignStatus;

    @Schema(description = "Campaign type", example = "REGULAR")
    private NewsletterCampaignType newsletterCampaignType;

    @Schema(description = "Scheduled send timestamp", example = "2024-01-20T09:00:00Z")
    private Instant newsletterCampaignScheduledAt;

    @Schema(description = "Actual sent timestamp", example = "2024-01-20T09:05:00Z")
    private Instant newsletterCampaignSentAt;

    @Schema(description = "Target audience criteria", example = "{\"language\": \"en\", \"interests\": [\"technology\"]}")
    private String newsletterCampaignTargetAudience;

    @Schema(description = "Total recipients", example = "1500")
    private Integer newsletterCampaignTotalRecipients;

    @Schema(description = "Successful deliveries", example = "1450")
    private Integer newsletterCampaignSuccessfulDeliveries;

    @Schema(description = "Failed deliveries", example = "50")
    private Integer newsletterCampaignFailedDeliveries;

    @Schema(description = "Open rate percentage", example = "24.50")
    private BigDecimal newsletterCampaignOpenRate;

    @Schema(description = "Click rate percentage", example = "8.75")
    private BigDecimal newsletterCampaignClickRate;

    @Schema(description = "Created timestamp", example = "2024-01-15T10:00:00Z")
    private Instant createdAt;

    @Schema(description = "Last updated timestamp", example = "2024-01-20T09:05:00Z")
    private Instant updatedAt;
}