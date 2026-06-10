package com.mmva.newsapp.domain.news.controller.dto.scheduler;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Response DTO for NewsSchedulingJob entity.
 * Used in admin endpoints to expose job status, metrics, and execution details.
 * 
 * Excludes sensitive internal fields like executionNode for public API.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsSchedulingJobResponseDto {

    /**
     * Unique job identifier (UUID)
     */
    private UUID jobId;

    /**
     * Correlation ID for distributed tracing across services
     */
    private String correlationId;

    /**
     * Current job status: RUNNING, SUCCESS, FAILED, PARTIAL_SUCCESS
     */
    private String status;

    /**
     * Source trigger: SCHEDULER, ADMIN_MANUAL, API, WEBHOOK
     */
    private String triggeredBy;

    /**
     * Reason for job trigger (e.g., "Automatic scheduled publication")
     */
    private String triggerReason;

    /**
     * User ID of admin/service that triggered job
     */
    private UUID triggeredByUserId;

    /**
     * Timestamp when job started execution
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private Instant startedAt;

    /**
     * Timestamp when job completed (null if still running)
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private Instant completedAt;

    /**
     * Duration in milliseconds (null if running)
     */
    private Long durationMs;

    /**
     * Queue wait time in milliseconds
     */
    private Long queueWaitMs;

    /**
     * Total articles processed in job
     */
    private Integer totalArticles;

    /**
     * Count of successfully published articles
     */
    private Integer publishedCount;

    /**
     * Count of failed publication attempts
     */
    private Integer failedCount;

    /**
     * Count of skipped articles
     */
    private Integer skippedCount;

    /**
     * Error message if job failed (null if successful)
     */
    private String errorMessage;

    /**
     * Job priority: CRITICAL, HIGH, MEDIUM, LOW
     */
    private String priority;

    /**
     * Queue name for job routing: DEFAULT, BREAKING_NEWS, REGIONS_LATAM,
     * REGIONS_APAC
     */
    private String queueName;

    /**
     * Retry backoff strategy: EXPONENTIAL, LINEAR, FIXED
     */
    private String backoffStrategy;

    /**
     * Maximum allowed duration in milliseconds
     */
    private Long maxDurationMs;

    /**
     * Execution logs (may be large, consider excluding in list endpoints)
     */
    private String executionLogs;

    /**
     * Execution metrics (may be large, consider excluding in list endpoints)
     */
    private String executionMetrics;

    /**
     * Version for optimistic locking
     */
    private Integer version;

    /**
     * Creation timestamp (audit)
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private Instant createdAt;

    /**
     * Last update timestamp (audit)
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private Instant updatedAt;

    /**
     * Associated attempts for detailed view (populated only in GET /jobs/{jobId})
     */
    private List<NewsSchedulingAttemptResponseDto> attempts;

    /**
     * Success rate percentage (calculated: publishedCount / totalArticles * 100)
     */
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_FLOAT, pattern = "#.##")
    public Double getSuccessRate() {
        if (totalArticles == null || totalArticles == 0) {
            return null;
        }
        return (publishedCount != null ? publishedCount : 0.0) / totalArticles * 100.0;
    }

    /**
     * Failure rate percentage (calculated: failedCount / totalArticles * 100)
     */
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_FLOAT, pattern = "#.##")
    public Double getFailureRate() {
        if (totalArticles == null || totalArticles == 0) {
            return null;
        }
        return (failedCount != null ? failedCount : 0.0) / totalArticles * 100.0;
    }

    /**
     * Skip rate percentage (calculated: skippedCount / totalArticles * 100)
     */
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_FLOAT, pattern = "#.##")
    public Double getSkipRate() {
        if (totalArticles == null || totalArticles == 0) {
            return null;
        }
        return (skippedCount != null ? skippedCount : 0.0) / totalArticles * 100.0;
    }
}
