package com.mmva.newsapp.domain.newsletter.dto.audit;

import com.mmva.newsapp.domain.newsletter.enums.core.NewsletterUnsubscribeReason;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

/**
 * DTO for newsletter unsubscribe responses.
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Builder
@Data
@Schema(description = "Newsletter unsubscribe response")
public class NewsletterUnsubscribeResponseDto {

    @Schema(description = "Unsubscribe ID", example = "1")
    private Long newsletterUnsubscribeId;

    @Schema(description = "Subscriber ID", example = "123")
    private Long newsletterSubscriberId;

    @Schema(description = "Subscriber email", example = "user@example.com")
    private String newsletterSubscriberEmail;

    @Schema(description = "Unsubscribe reason", example = "NO_LONGER_INTERESTED")
    private NewsletterUnsubscribeReason newsletterUnsubscribeReason;

    @Schema(description = "Additional feedback", example = "Content not relevant to my interests")
    private String newsletterUnsubscribeFeedback;

    @Schema(description = "IP address", example = "192.168.1.100")
    private String newsletterUnsubscribeIpAddress;

    @Schema(description = "User agent", example = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
    private String newsletterUnsubscribeUserAgent;

    @Schema(description = "Unsubscribe timestamp", example = "2024-01-20T15:45:00Z")
    private Instant newsletterUnsubscribeCreatedAt;
}