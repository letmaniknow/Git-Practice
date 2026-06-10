package com.mmva.newsapp.domain.newsletter.dto.core;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

/**
 * DTO for newsletter campaign content requests.
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Builder
@Data
@Schema(description = "Newsletter campaign content request")
public class NewsletterCampaignContentRequestDto {

    @Schema(description = "Language code (en/es)", example = "en", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Language is required")
    private String newsletterCampaignContentLanguage;

    @Schema(description = "Language-specific subject line", example = "Your Weekly Tech Roundup is Here!", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Subject is required")
    private String newsletterCampaignContentSubject;

    @Schema(description = "HTML content for the newsletter", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "HTML content is required")
    private String newsletterCampaignContentHtml;

    @Schema(description = "Plain text version (optional, auto-generated from HTML if not provided)")
    private String newsletterCampaignContentText;
}