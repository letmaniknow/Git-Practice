package com.mmva.newsapp.domain.news.model.scheduler;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;
import org.hibernate.annotations.JdbcTypeCode;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * NewsSchedulingAttempt Entity - Phase 1 Production-Grade Attempt Tracking
 *
 * Mapped to: news_scheduling_attempt table (news domain, scheduling feature)
 *
 * Purpose:
 * - Track every attempt to publish a single article
 * - Record detailed error information with error codes
 * - Support sophisticated retry logic with backoff calculations
 * - Enable per-article debugging and failure analysis
 * - Support idempotent processing to prevent duplicate attempts
 *
 * Phase 1 Features (Production-Grade):
 * ✓ Detailed error tracking (error_code, stack_trace)
 * ✓ Per-attempt timing metrics
 * ✓ Smart retry logic (should_retry, backoff_delay, next_retry_at)
 * ✓ Idempotency support (idempotency_key)
 * ✓ Circuit breaker detection (circuit_breaker_triggered)
 * ✓ Attempt numbering (which attempt: 1st, 2nd, 3rd)
 * ✓ Audit trail (created_at, updated_at, created_by)
 * ✓ Execution context (JSON for article-specific data)
 * ✓ Per-attempt correlation ID for distributed tracing
 *
 * States:
 * - SUCCESS: Article published successfully
 * - FAILED: Article failed to publish (error recorded)
 * - SKIPPED: Article skipped (already published, duplicate, etc.)
 * - PENDING: Waiting to be processed
 *
 * @author MMVA News Team
 * @since 1.0.0
 */
@Entity
@Table(name = "news_scheduling_attempt", indexes = {
        @Index(name = "idx_news_scheduling_attempt_job_id", columnList = "news_scheduling_job_id"),
        @Index(name = "idx_news_scheduling_attempt_article_id", columnList = "news_scheduling_attempt_article_id"),
        @Index(name = "idx_news_scheduling_attempt_status", columnList = "news_scheduling_attempt_status"),
        @Index(name = "idx_news_scheduling_attempt_should_retry", columnList = "news_scheduling_attempt_should_retry"),
        @Index(name = "idx_news_scheduling_attempt_idempotency", columnList = "news_scheduling_attempt_idempotency_key"),
        @Index(name = "idx_news_scheduling_attempt_next_retry_at", columnList = "news_scheduling_attempt_next_retry_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = "newsSchedulingJob")
@ToString(exclude = "newsSchedulingJob")
public class NewsSchedulingAttempt {

    // ========================================
    // IDENTITY & RELATIONSHIPS
    // ========================================

    @Id
    @Column(name = "news_scheduling_attempt_id", nullable = false)
    private UUID attemptId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "news_scheduling_job_id", nullable = false)
    private NewsSchedulingJob newsSchedulingJob;

    @Column(name = "news_scheduling_attempt_article_id", nullable = false)
    private UUID articleId;

    // ========================================
    // ATTEMPT STATUS
    // ========================================

    @Column(name = "news_scheduling_attempt_status", nullable = false, length = 20)
    private String status;

    /**
     * Which attempt number is this (1st, 2nd, 3rd, etc.)
     */
    @Column(name = "news_scheduling_attempt_number")
    private Integer attemptNumber;

    /**
     * Human-readable error message if attempt failed
     */
    @Column(name = "news_scheduling_attempt_error", length = 1000)
    private String error;

    /**
     * Machine-readable error code for categorization
     * Examples: TIMEOUT, AUTH_FAILED, NETWORK_ERROR, VALIDATION_ERROR
     */
    @Column(name = "news_scheduling_attempt_error_code", length = 100)
    private String errorCode;

    /**
     * Full exception stack trace for debugging
     */
    @Column(name = "news_scheduling_attempt_stack_trace", columnDefinition = "TEXT")
    private String stackTrace;

    // ========================================
    // TIMING
    // ========================================

    @Column(name = "news_scheduling_attempt_started_at")
    private Instant startedAt;

    @Column(name = "news_scheduling_attempt_completed_at")
    private Instant completedAt;

    @Column(name = "news_scheduling_attempt_duration_ms")
    private Long durationMs;

    @CreationTimestamp
    @Column(name = "news_scheduling_attempt_timestamp", nullable = false, updatable = false)
    private Instant timestamp;

    // ========================================
    // RETRY LOGIC
    // ========================================

    /**
     * How many times this article has been retried
     */
    @Column(name = "news_scheduling_attempt_retry_count")
    private Integer retryCount;

    /**
     * Whether this article should be retried
     * true if error is transient (timeout, connection issue)
     * false if error is permanent (invalid data, authorization)
     */
    @Column(name = "news_scheduling_attempt_should_retry")
    private Boolean shouldRetry;

    /**
     * Calculated backoff delay for next retry (milliseconds)
     * Based on backoff_strategy: EXPONENTIAL, LINEAR, FIXED
     */
    @Column(name = "news_scheduling_attempt_backoff_delay_ms")
    private Long backoffDelayMs;

    /**
     * When to retry this article
     * Calculated as: now + backoffDelayMs
     */
    @Column(name = "news_scheduling_attempt_next_retry_at")
    private Instant nextRetryAt;

    /**
     * Per-attempt idempotency key
     * Prevents duplicate processing if same attempt retried
     */
    @Column(name = "news_scheduling_attempt_idempotency_key", unique = true, length = 100)
    private String idempotencyKey;

    /**
     * Whether circuit breaker was triggered for this article
     * Indicates repeated pattern failures detected
     */
    @Column(name = "news_scheduling_attempt_circuit_breaker_triggered")
    private Boolean circuitBreakerTriggered;

    // ========================================
    // AUDIT & METADATA
    // ========================================

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "created_by", length = 255)
    private String createdBy;

    /**
     * Per-attempt correlation ID for distributed tracing
     */
    @Column(name = "news_scheduling_attempt_correlation_id_attempt", length = 100)
    private String correlationIdAttempt;

    /**
     * Article-specific execution context as JSON
     * Can contain article-specific metadata needed for retry
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "news_scheduling_attempt_execution_context", columnDefinition = "JSON")
    private String executionContext;

    // ========================================
    // FACTORY METHODS
    // ========================================

    /**
     * Create a successful attempt
     */
    public static NewsSchedulingAttempt success(UUID jobId, UUID articleId) {
        return NewsSchedulingAttempt.builder()
                .attemptId(UUID.randomUUID())
                .articleId(articleId)
                .status("SUCCESS")
                .shouldRetry(false)
                .retryCount(0)
                .attemptNumber(1)
                .circuitBreakerTriggered(false)
                .build();
    }

    /**
     * Create a failed attempt with retry decision
     */
    public static NewsSchedulingAttempt failed(UUID jobId, UUID articleId, String errorMessage,
            String errorCode, boolean shouldRetry) {
        return NewsSchedulingAttempt.builder()
                .attemptId(UUID.randomUUID())
                .articleId(articleId)
                .status("FAILED")
                .error(errorMessage)
                .errorCode(errorCode)
                .shouldRetry(shouldRetry)
                .retryCount(0)
                .attemptNumber(1)
                .circuitBreakerTriggered(false)
                .build();
    }

    /**
     * Create a skipped attempt
     */
    public static NewsSchedulingAttempt skipped(UUID jobId, UUID articleId, String reason) {
        return NewsSchedulingAttempt.builder()
                .attemptId(UUID.randomUUID())
                .articleId(articleId)
                .status("SKIPPED")
                .error(reason)
                .shouldRetry(false)
                .retryCount(0)
                .attemptNumber(1)
                .circuitBreakerTriggered(false)
                .build();
    }

    // ========================================
    // HELPER METHODS
    // ========================================

    /**
     * Check if this attempt was successful
     */
    public boolean isSuccessful() {
        return "SUCCESS".equals(status);
    }

    /**
     * Check if this attempt failed
     */
    public boolean isFailed() {
        return "FAILED".equals(status);
    }

    /**
     * Check if this attempt was skipped
     */
    public boolean isSkipped() {
        return "SKIPPED".equals(status);
    }

    /**
     * Check if this article should be retried
     * true only if: failed, should retry, and under max retries
     */
    public boolean canRetry(int maxRetries) {
        return isFailed() && shouldRetry && (retryCount != null && retryCount < maxRetries);
    }

    /**
     * Increment retry count
     * Call when article is being retried
     */
    public void incrementRetryCount() {
        if (retryCount == null) {
            retryCount = 1;
        } else {
            retryCount++;
        }
        attemptNumber = retryCount + 1;
    }

    /**
     * Get execution context as Map (if JSON)
     */
    public Map<String, Object> getExecutionContextAsMap() {
        // In real implementation, would use JSON parsing library
        return new HashMap<>();
    }
}
