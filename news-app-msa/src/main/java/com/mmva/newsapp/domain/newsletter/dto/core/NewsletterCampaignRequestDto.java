package com.mmva.newsapp.domain.newsletter.dto.core;

import com.mmva.newsapp.domain.newsletter.enums.core.NewsletterCampaignType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

/**
 * DTO for newsletter campaign requests.
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Builder
@Data
@Schema(description = "Newsletter campaign request")
public class NewsletterCampaignRequestDto {

    @Schema(description = "Campaign name", example = "Weekly Tech Roundup", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Campaign name is required")
    @Size(max = 255, message = "Campaign name must not exceed 255 characters")
    private String newsletterCampaignName;

    @Schema(description = "Campaign subject line", example = "Your Weekly Tech Roundup is Here!", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Campaign subject is required")
    @Size(max = 255, message = "Campaign subject must not exceed 255 characters")
    private String newsletterCampaignSubject;

    @Schema(description = "Campaign description", example = "Weekly summary of technology news and trends")
    private String newsletterCampaignDescription;

    @Schema(description = "Campaign type", example = "REGULAR")
    private NewsletterCampaignType newsletterCampaignType;

    @Schema(description = "Scheduled send timestamp", example = "2024-01-20T09:00:00Z")
    private Instant newsletterCampaignScheduledAt;

    @Schema(description = "Target audience criteria as JSON", example = "{\"language\": \"en\", \"interests\": [\"technology\"]}")
    private String newsletterCampaignTargetAudience;
}