package com.mmva.newsapp.domain.news.dto.scheduler;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * NewsSchedulingMetricsResponseDto
 *
 * Response DTO for aggregated scheduler metrics.
 * Provides insights into job execution performance and health.
 *
 * Used in:
 * - GET /api/v1/admin/scheduler/metrics - Performance metrics endpoint
 * - Monitoring and alerting dashboards
 *
 * @author MMVA News Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsSchedulingMetricsResponseDto {

    @JsonProperty("total_jobs")
    private Long totalJobs;

    @JsonProperty("successful_jobs")
    private Long successfulJobs;

    @JsonProperty("failed_jobs")
    private Long failedJobs;

    @JsonProperty("partial_success_jobs")
    private Long partialSuccessJobs;

    @JsonProperty("running_jobs")
    private Long runningJobs;

    @JsonProperty("success_rate_percent")
    private Double successRatePercent;

    @JsonProperty("failure_rate_percent")
    private Double failureRatePercent;

    @JsonProperty("partial_success_rate_percent")
    private Double partialSuccessRatePercent;

    @JsonProperty("average_duration_ms")
    private Long averageDurationMs;

    @JsonProperty("total_articles_published")
    private Long totalArticlesPublished;

    @JsonProperty("total_articles_failed")
    private Long totalArticlesFailed;

    @JsonProperty("total_articles_skipped")
    private Long totalArticlesSkipped;

    @JsonProperty("articles_success_rate_percent")
    private Double articlesSuccessRatePercent;

    @JsonProperty("most_common_error_code")
    private String mostCommonErrorCode;

    @JsonProperty("stuck_jobs_count")
    private Long stuckJobsCount;

    @JsonProperty("jobs_requiring_manual_intervention")
    private Long jobsRequiringManualIntervention;

    @JsonProperty("average_retry_attempts")
    private Double averageRetryAttempts;

    @JsonProperty("total_retried_articles")
    private Long totalRetriedArticles;

    @JsonProperty("retry_success_rate_percent")
    private Double retrySuccessRatePercent;

    @JsonProperty("timestamp")
    private String timestamp;

    @JsonProperty("period_label")
    private String periodLabel; // "Last 24 hours", "Last 7 days", etc.
}
