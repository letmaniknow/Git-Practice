package com.mmva.newsapp.domain.newsletter.dto.core;

import com.mmva.newsapp.domain.newsletter.enums.core.NewsletterSubscriptionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO for newsletter subscriber requests.
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Builder
@Data
@Schema(description = "Newsletter subscription request")
public class NewsletterSubscriberRequestDto {

    @Schema(description = "User ID (optional, for registered users)", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID userId;

    @Schema(description = "Subscriber email address", example = "user@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String newsletterSubscriberEmail;

    @Schema(description = "Subscriber first name", example = "John")
    @Size(max = 100, message = "First name must not exceed 100 characters")
    private String newsletterSubscriberFirstName;

    @Schema(description = "Subscriber last name", example = "Doe")
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    private String newsletterSubscriberLastName;

    @Schema(description = "Preferred language (en/es)", example = "en")
    private String newsletterSubscriberPreferredLanguage;

    @Schema(description = "Interests as JSON array", example = "[\"technology\", \"sports\"]")
    private String newsletterSubscriberInterests;

    @Schema(description = "Subscription source", example = "website")
    private String newsletterSubscriberSource;
}