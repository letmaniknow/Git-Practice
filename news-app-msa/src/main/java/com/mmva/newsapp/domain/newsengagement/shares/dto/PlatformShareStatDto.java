package com.mmva.newsapp.domain.newsengagement.shares.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for share statistics grouped by platform.
 * 
 * <p>
 * Used for analytics dashboards to show which social media platforms
 * users are sharing news articles to.
 * </p>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Share statistics for a specific platform")
public class PlatformShareStatDto {

    @Schema(description = "Platform name (e.g., facebook, twitter, whatsapp, linkedin, email, copy_link)", example = "facebook")
    private String platform;

    @Schema(description = "Total number of shares on this platform", example = "1523")
    private Long shareCount;

    @Schema(description = "Percentage of total shares (calculated if totalShares > 0)", example = "35.8")
    private Double percentage;
}
