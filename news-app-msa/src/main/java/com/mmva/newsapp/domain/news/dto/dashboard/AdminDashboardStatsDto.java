package com.mmva.newsapp.domain.news.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * DTO for Admin Dashboard Statistics
 * Aggregates key metrics for the news management dashboard
 * 
 * @author MMVA Team
 * @version 1.0
 * @since 2026-05-10
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Admin Dashboard Statistics - Aggregated news metrics")
public class AdminDashboardStatsDto {

    @Schema(
        description = "Total number of articles (all statuses)",
        example = "156",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long totalArticles;

    @Schema(
        description = "Number of articles published in the current month",
        example = "42",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long publishedThisMonth;

    @Schema(
        description = "Number of draft articles",
        example = "18",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long draftCount;

    @Schema(
        description = "Number of scheduled articles waiting to be published",
        example = "7",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long scheduledCount;

    @Schema(
        description = "Number of archived/deleted articles",
        example = "89",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long archivedCount;

    @Schema(
        description = "System health status: healthy, warning, or critical",
        example = "healthy",
        requiredMode = Schema.RequiredMode.REQUIRED,
        allowableValues = {"healthy", "warning", "critical"}
    )
    private String systemHealth;

    @Schema(
        description = "Total page views across all articles",
        example = "45230",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private Long totalPageViews;

    @Schema(
        description = "Total engagement (likes, shares, comments)",
        example = "1205",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private Long totalEngagement;
}
