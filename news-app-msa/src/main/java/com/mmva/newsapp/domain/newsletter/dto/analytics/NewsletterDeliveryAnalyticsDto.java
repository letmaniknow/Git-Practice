package com.mmva.newsapp.domain.newsletter.dto.analytics;

import com.mmva.newsapp.domain.newsletter.enums.core.NewsletterDeliveryStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

/**
 * DTO for newsletter delivery analytics.
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Builder
@Data
@Schema(description = "Newsletter delivery analytics")
public class NewsletterDeliveryAnalyticsDto {

    @Schema(description = "Delivery log ID", example = "1")
    private Long newsletterDeliveryLogId;

    @Schema(description = "Campaign ID", example = "1")
    private Long newsletterCampaignId;

    @Schema(description = "Subscriber ID", example = "123")
    private Long newsletterSubscriberId;

    @Schema(description = "Subscriber email", example = "user@example.com")
    private String newsletterSubscriberEmail;

    @Schema(description = "Delivery status", example = "DELIVERED")
    private NewsletterDeliveryStatus newsletterDeliveryLogStatus;

    @Schema(description = "Sent timestamp", example = "2024-01-20T09:05:00Z")
    private Instant newsletterDeliveryLogSentAt;

    @Schema(description = "Delivered timestamp", example = "2024-01-20T09:06:00Z")
    private Instant newsletterDeliveryLogDeliveredAt;

    @Schema(description = "Opened timestamp", example = "2024-01-20T10:15:00Z")
    private Instant newsletterDeliveryLogOpenedAt;

    @Schema(description = "Clicked timestamp", example = "2024-01-20T10:20:00Z")
    private Instant newsletterDeliveryLogClickedAt;

    @Schema(description = "Bounced timestamp", example = "2024-01-20T09:10:00Z")
    private Instant newsletterDeliveryLogBouncedAt;

    @Schema(description = "Error message (if any)", example = "Mailbox full")
    private String newsletterDeliveryLogErrorMessage;

    @Schema(description = "Provider message ID", example = "abc123@email-provider.com")
    private String newsletterDeliveryLogProviderMessageId;

    @Schema(description = "Created timestamp", example = "2024-01-20T09:05:00Z")
    private Instant createdAt;
}