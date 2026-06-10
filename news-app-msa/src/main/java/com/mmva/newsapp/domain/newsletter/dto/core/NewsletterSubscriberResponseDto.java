package com.mmva.newsapp.domain.newsletter.dto.core;

import com.mmva.newsapp.domain.newsletter.enums.core.NewsletterSubscriptionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO for newsletter subscriber responses.
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Builder
@Data
@Schema(description = "Newsletter subscriber response")
public class NewsletterSubscriberResponseDto {

    @Schema(description = "Subscriber ID", example = "1")
    private Long newsletterSubscriberId;

    @Schema(description = "User ID (if linked to registered user)", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID userId;

    @Schema(description = "Subscriber email address", example = "user@example.com")
    private String newsletterSubscriberEmail;

    @Schema(description = "Subscriber first name", example = "John")
    private String newsletterSubscriberFirstName;

    @Schema(description = "Subscriber last name", example = "Doe")
    private String newsletterSubscriberLastName;

    @Schema(description = "Preferred language", example = "en")
    private String newsletterSubscriberPreferredLanguage;

    @Schema(description = "Subscription status", example = "ACTIVE")
    private NewsletterSubscriptionStatus newsletterSubscriberSubscriptionStatus;

    @Schema(description = "Interests as JSON array", example = "[\"technology\", \"sports\"]")
    private String newsletterSubscriberInterests;

    @Schema(description = "Subscription source", example = "website")
    private String newsletterSubscriberSource;

    @Schema(description = "Confirmation timestamp", example = "2024-01-15T10:30:00Z")
    private Instant newsletterSubscriberConfirmedAt;

    @Schema(description = "Unsubscription timestamp", example = "2024-01-20T15:45:00Z")
    private Instant newsletterSubscriberUnsubscribedAt;

    @Schema(description = "Created timestamp", example = "2024-01-15T10:00:00Z")
    private Instant createdAt;

    @Schema(description = "Last updated timestamp", example = "2024-01-15T10:30:00Z")
    private Instant updatedAt;
}