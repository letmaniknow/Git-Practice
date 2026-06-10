package com.mmva.newsapp.domain.news.dto.scheduler;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * NewsSchedulingJobResponseDto
 *
 * Response DTO for job status and details.
 * Maps NewsSchedulingJob entity to API response format.
 *
 * Used in:
 * - GET /api/v1/admin/scheduler/jobs/{jobId} - Job status endpoint
 * - GET /api/v1/admin/scheduler/metrics - Aggregated metrics
 *
 * @author MMVA News Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsSchedulingJobResponseDto {

    @JsonProperty("job_id")
    private UUID jobId;

    @JsonProperty("correlation_id")
    private String correlationId;

    @JsonProperty("partition_key")
    private String partitionKey;

    @JsonProperty("triggered_by")
    private String triggeredBy;

    @JsonProperty("trigger_reason")
    private String triggerReason;

    @JsonProperty("execution_node")
    private String executionNode;

    @JsonProperty("status")
    private String status; // RUNNING, SUCCESS, FAILED, PARTIAL_SUCCESS

    @JsonProperty("priority")
    private String priority; // CRITICAL, HIGH, MEDIUM, LOW

    @JsonProperty("queue_name")
    private String queueName; // DEFAULT, BREAKING_NEWS, REGIONS_LATAM, REGIONS_APAC

    @JsonProperty("tags")
    private String tags;

    @JsonProperty("total_articles")
    private Integer totalArticles;

    @JsonProperty("published_count")
    private Integer publishedCount;

    @JsonProperty("failed_count")
    private Integer failedCount;

    @JsonProperty("skipped_count")
    private Integer skippedCount;

    @JsonProperty("started_at")
    private Instant startedAt;

    @JsonProperty("completed_at")
    private Instant completedAt;

    @JsonProperty("duration_ms")
    private Long durationMs;

    @JsonProperty("max_duration_ms")
    private Long maxDurationMs;

    @JsonProperty("backoff_strategy")
    private String backoffStrategy; // EXPONENTIAL, LINEAR, FIXED

    @JsonProperty("error_message")
    private String errorMessage;

    @JsonProperty("execution_plan_version")
    private Integer executionPlanVersion;

    @JsonProperty("health_check_timestamp")
    private Instant healthCheckTimestamp;

    @JsonProperty("version")
    private Long version;

    @JsonProperty("created_at")
    private Instant createdAt;

    @JsonProperty("created_by")
    private String createdBy;

    @JsonProperty("updated_at")
    private Instant updatedAt;

    @JsonProperty("updated_by")
    private String updatedBy;

    @JsonProperty("success_rate_percent")
    private Double successRatePercent;
}
