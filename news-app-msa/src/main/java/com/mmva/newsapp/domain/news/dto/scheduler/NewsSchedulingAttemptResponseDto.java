package com.mmva.newsapp.domain.news.dto.scheduler;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * NewsSchedulingAttemptResponseDto
 *
 * Response DTO for publication attempt details.
 * Maps NewsSchedulingAttempt entity to API response format.
 *
 * Used in:
 * - GET /api/v1/admin/scheduler/failed-articles - Failed attempts list
 * - Included in job details response
 *
 * @author MMVA News Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsSchedulingAttemptResponseDto {

    @JsonProperty("attempt_id")
    private UUID attemptId;

    @JsonProperty("job_id")
    private UUID jobId;

    @JsonProperty("article_id")
    private UUID articleId;

    @JsonProperty("status")
    private String status; // SUCCESS, FAILED, SKIPPED, PENDING

    @JsonProperty("attempt_number")
    private Integer attemptNumber;

    @JsonProperty("error")
    private String error;

    @JsonProperty("error_code")
    private String errorCode; // TIMEOUT, AUTH_FAILED, NETWORK_ERROR, VALIDATION_ERROR

    @JsonProperty("stack_trace")
    private String stackTrace;

    @JsonProperty("should_retry")
    private Boolean shouldRetry;

    @JsonProperty("retry_count")
    private Integer retryCount;

    @JsonProperty("backoff_delay_ms")
    private Long backoffDelayMs;

    @JsonProperty("next_retry_at")
    private Instant nextRetryAt;

    @JsonProperty("circuit_breaker_triggered")
    private Boolean circuitBreakerTriggered;

    @JsonProperty("started_at")
    private Instant startedAt;

    @JsonProperty("completed_at")
    private Instant completedAt;

    @JsonProperty("duration_ms")
    private Long durationMs;

    @JsonProperty("correlation_id_attempt")
    private String correlationIdAttempt;

    @JsonProperty("idempotency_key")
    private String idempotencyKey;

    @JsonProperty("created_at")
    private Instant createdAt;

    @JsonProperty("created_by")
    private String createdBy;

    @JsonProperty("updated_at")
    private Instant updatedAt;

    @JsonProperty("timestamp")
    private Instant timestamp;
}
