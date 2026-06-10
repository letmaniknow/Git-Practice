package com.mmva.newsapp.domain.news.controller.dto.scheduler;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for NewsSchedulingAttempt entity.
 * Used in admin endpoints to expose individual article publication attempt
 * details.
 * 
 * Includes retry information for retry management and error diagnostics.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsSchedulingAttemptResponseDto {

    /**
     * Unique attempt identifier (UUID)
     */
    private UUID attemptId;

    /**
     * Parent job ID (FK reference)
     */
    private UUID jobId;

    /**
     * Article being published (UUID)
     */
    private UUID articleId;

    /**
     * Attempt status: SUCCESS, FAILED, SKIPPED, PENDING
     */
    private String status;

    /**
     * Attempt number (1 for first try, 2+ for retries)
     */
    private Integer attemptNumber;

    /**
     * Human-readable error message (max 1000 chars)
     */
    private String error;

    /**
     * Categorized error code for retry decisions:
     * TIMEOUT, AUTH_FAILED, NETWORK_ERROR, VALIDATION_ERROR, NOT_FOUND
     */
    private String errorCode;

    /**
     * Full exception stack trace for debugging
     */
    private String stackTrace;

    /**
     * When attempt started
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private Instant startedAt;

    /**
     * When attempt completed
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private Instant completedAt;

    /**
     * Duration in milliseconds
     */
    private Long durationMs;

    /**
     * Current retry count (0 for first attempt)
     */
    private Integer retryCount;

    /**
     * Whether this attempt should be retried if failed
     */
    private Boolean shouldRetry;

    /**
     * Backoff delay in milliseconds before next retry
     */
    private Long backoffDelayMs;

    /**
     * When to retry this attempt
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private Instant nextRetryAt;

    /**
     * Idempotency key to prevent duplicate processing
     */
    private String idempotencyKey;

    /**
     * Whether circuit breaker was triggered (indicating service degradation)
     */
    private Boolean circuitBreakerTriggered;

    /**
     * Correlation ID for tracing this attempt across services
     */
    private String correlationIdAttempt;

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
}
