package com.mmva.newsapp.domain.newsletter.dto.core;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

/**
 * DTO for newsletter campaign content responses.
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Builder
@Data
@Schema(description = "Newsletter campaign content response")
public class NewsletterCampaignContentResponseDto {

    @Schema(description = "Content ID", example = "1")
    private Long newsletterCampaignContentId;

    @Schema(description = "Campaign ID", example = "1")
    private Long newsletterCampaignId;

    @Schema(description = "Language code", example = "en")
    private String newsletterCampaignContentLanguage;

    @Schema(description = "Language-specific subject line", example = "Your Weekly Tech Roundup is Here!")
    private String newsletterCampaignContentSubject;

    @Schema(description = "HTML content preview (truncated)", example = "<h1>Weekly Tech Roundup</h1><p>This week in tech...</p>")
    private String newsletterCampaignContentHtml;

    @Schema(description = "Has plain text version", example = "true")
    private Boolean hasTextVersion;

    @Schema(description = "Created timestamp", example = "2024-01-15T10:00:00Z")
    private Instant createdAt;

    @Schema(description = "Last updated timestamp", example = "2024-01-15T11:00:00Z")
    private Instant updatedAt;
}