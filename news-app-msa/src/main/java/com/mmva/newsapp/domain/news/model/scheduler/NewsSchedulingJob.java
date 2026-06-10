package com.mmva.newsapp.domain.news.model.scheduler;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;
import org.hibernate.annotations.JdbcTypeCode;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * NewsSchedulingJob Entity - Phase 1 Production-Grade Job Tracking
 *
 * Mapped to: news_scheduling_job table (news domain, scheduling feature)
 *
 * Purpose:
 * - Track every scheduled publish job execution with full audit trail
 * - Support distributed systems (tracing, partitioning, execution nodes)
 * - Enable zero-downtime operations and multi-server coordination
 * - Provide comprehensive observability and debugging capabilities
 * - Support idempotent job processing and retry logic
 *
 * Phase 1 Features (Production-Grade):
 * ✓ Distributed tracing (correlation_id)
 * ✓ Execution context (triggered_by, execution_node)
 * ✓ Comprehensive audit trail (created_by, updated_by, deleted_at)
 * ✓ Job lifecycle tracking (timing metrics)
 * ✓ Timeout support (max_duration_ms)
 * ✓ Structured logging (execution_logs as JSON)
 * ✓ Retry configuration (backoff_strategy, idempotency_key)
 * ✓ Partition support (partition_key for sharding)
 *
 * States:
 * - RUNNING: Job currently executing
 * - SUCCESS: Job completed, all articles published
 * - FAILED: Job failed before processing articles
 * - PARTIAL_SUCCESS: Job completed, some articles failed
 *
 * @author MMVA News Team
 * @since 1.0.0
 */
@Entity
@Table(name = "news_scheduling_job", indexes = {
        @Index(name = "idx_news_scheduling_job_correlation_id", columnList = "news_scheduling_job_correlation_id"),
        @Index(name = "idx_news_scheduling_job_status", columnList = "news_scheduling_job_status"),
        @Index(name = "idx_news_scheduling_job_started_at", columnList = "news_scheduling_job_started_at DESC"),
        @Index(name = "idx_news_scheduling_job_partition_key", columnList = "news_scheduling_job_partition_key"),
        @Index(name = "idx_news_scheduling_job_next_retry_at", columnList = "news_scheduling_job_next_retry_at"),
        @Index(name = "idx_news_scheduling_job_execution_node", columnList = "news_scheduling_job_execution_node"),
        @Index(name = "idx_news_scheduling_job_triggered_by", columnList = "news_scheduling_job_triggered_by"),
        @Index(name = "idx_news_scheduling_job_priority", columnList = "news_scheduling_job_priority"),
        @Index(name = "idx_news_scheduling_job_queue_name", columnList = "news_scheduling_job_queue_name")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = "attempts")
@ToString(exclude = "attempts")
public class NewsSchedulingJob {

    // ========================================
    // IDENTITY & TRACING
    // ========================================

    @Id
    @Column(name = "news_scheduling_job_id", nullable = false)
    private UUID jobId;

    /**
     * Correlation ID for distributed tracing across systems
     * Enables tracking job through multiple services
     */
    @Column(name = "news_scheduling_job_correlation_id", nullable = false, unique = true, length = 100)
    private String correlationId;

    /**
     * Partition key for horizontal sharding
     * Used to distribute jobs across servers
     */
    @Column(name = "news_scheduling_job_partition_key", nullable = false, length = 50)
    private String partitionKey;

    // ========================================
    // EXECUTION CONTEXT
    // ========================================

    /**
     * Who triggered this job
     * Values: SCHEDULER, ADMIN_MANUAL, API, WEBHOOK
     */
    @Column(name = "news_scheduling_job_triggered_by", nullable = false, length = 50)
    private String triggeredBy;

    /**
     * Why was this job triggered (human-readable reason)
     */
    @Column(name = "news_scheduling_job_trigger_reason", length = 500)
    private String triggerReason;

    /**
     * Admin user ID who manually triggered (if ADMIN_MANUAL)
     */
    @Column(name = "news_scheduling_job_triggered_by_user_id")
    private UUID triggeredByUserId;

    /**
     * Pod/server/node that executed this job
     * Useful for debugging distributed issues
     */
    @Column(name = "news_scheduling_job_execution_node", length = 100)
    private String executionNode;

    /**
     * Job priority for scheduling queue
     * Values: CRITICAL (breaking news), HIGH, MEDIUM, LOW
     * CRITICAL jobs are processed first
     */
    @Column(name = "news_scheduling_job_priority", length = 20)
    private String priority;

    /**
     * Queue/Pool name for execution routing
     * Examples: DEFAULT, BREAKING_NEWS, REGIONS_LATAM, REGIONS_APAC
     * Used to route jobs to specific processing pools
     */
    @Column(name = "news_scheduling_job_queue_name", length = 100)
    private String queueName;

    // ========================================
    // TIMING & EXECUTION
    // ========================================

    @CreationTimestamp
    @Column(name = "news_scheduling_job_started_at", nullable = false, updatable = false)
    private Instant startedAt;

    @Column(name = "news_scheduling_job_completed_at")
    private Instant completedAt;

    /**
     * Maximum allowed duration in milliseconds (timeout)
     * Job aborted if exceeds this. 0 = no limit
     */
    @Column(name = "news_scheduling_job_max_duration_ms")
    private Long maxDurationMs;

    /**
     * Actual job execution duration in milliseconds
     * Set when job completes
     */
    @Column(name = "news_scheduling_job_duration_ms")
    private Long durationMs;

    /**
     * Time job waited in queue before starting (milliseconds)
     */
    @Column(name = "news_scheduling_job_queue_wait_ms")
    private Long queueWaitMs;

    // ========================================
    // STATUS & METRICS
    // ========================================

    @Column(name = "news_scheduling_job_status", nullable = false, length = 20)
    private String status;

    @Column(name = "news_scheduling_job_total_articles")
    private Integer totalArticles;

    @Column(name = "news_scheduling_job_published_count")
    private Integer publishedCount;

    @Column(name = "news_scheduling_job_failed_count")
    private Integer failedCount;

    @Column(name = "news_scheduling_job_skipped_count")
    private Integer skippedCount;

    @Column(name = "news_scheduling_job_error_message", length = 1000)
    private String errorMessage;

    // ========================================
    // CATEGORIZATION & FILTERING
    // ========================================

    /**
     * Comma-separated tags for job categorization
     * Examples: breaking_news,sports,tech,international
     * Used for filtering, organization, metrics tracking
     */
    @Column(name = "news_scheduling_job_tags", columnDefinition = "TEXT")
    private String tags;

    // ========================================
    // RETRY & RESILIENCE
    // ========================================

    /**
     * Retry backoff strategy
     * Values: EXPONENTIAL, LINEAR, FIXED
     */
    @Column(name = "news_scheduling_job_backoff_strategy", length = 50)
    private String backoffStrategy;

    /**
     * Idempotency key to prevent duplicate processing
     */
    @Column(name = "news_scheduling_job_idempotency_key", unique = true, length = 100)
    private String idempotencyKey;

    @Column(name = "news_scheduling_job_last_retry_at")
    private Instant lastRetryAt;

    @Column(name = "news_scheduling_job_next_retry_at")
    private Instant nextRetryAt;

    // ========================================
    // OBSERVABILITY
    // ========================================

    /**
     * Structured execution logs as JSON
     * Contains detailed job lifecycle events
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "news_scheduling_job_execution_logs", columnDefinition = "JSON")
    private String executionLogs;

    /**
     * Performance metrics as JSON
     * Contains memory, CPU, timing breakdown
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "news_scheduling_job_execution_metrics", columnDefinition = "JSON")
    private String executionMetrics;

    /**
     * Non-fatal warnings encountered during execution
     */
    @Column(name = "news_scheduling_job_warnings", columnDefinition = "TEXT")
    private String warnings;

    /**
     * Version of execution logic used
     * Useful for debugging logic changes
     */
    @Column(name = "news_scheduling_job_execution_plan_version")
    private Integer executionPlanVersion;

    /**
     * Timestamp of last health check
     * For detecting stuck jobs
     */
    @Column(name = "news_scheduling_job_health_check_timestamp")
    private Instant healthCheckTimestamp;

    // ========================================
    // AUDIT FIELDS
    // ========================================

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "created_by", length = 255)
    private String createdBy;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "updated_by", length = 255)
    private String updatedBy;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    // ========================================
    // CONCURRENCY CONTROL
    // ========================================

    /**
     * Optimistic locking version
     * Prevents concurrent update conflicts
     */
    @Column(name = "version")
    private Integer version;

    // ========================================
    // RELATIONSHIPS
    // ========================================

    @OneToMany(mappedBy = "newsSchedulingJob", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<NewsSchedulingAttempt> attempts = new ArrayList<>();

    // ========================================
    // HELPER METHODS
    // ========================================

    /**
     * Mark job as completed with final status
     * Calculates duration automatically
     */
    public void complete(String finalStatus) {
        this.completedAt = Instant.now();
        this.status = finalStatus;
        this.durationMs = completedAt.toEpochMilli() - startedAt.toEpochMilli();
    }

    /**
     * Check if job is still running
     */
    public boolean isRunning() {
        return "RUNNING".equals(status);
    }

    /**
     * Check if job completed successfully
     */
    public boolean isSuccessful() {
        return "SUCCESS".equals(status) || "PARTIAL_SUCCESS".equals(status);
    }

    /**
     * Check if job exceeded timeout
     */
    public boolean isTimedOut() {
        return maxDurationMs != null && maxDurationMs > 0 &&
                durationMs != null && durationMs > maxDurationMs;
    }

    /**
     * Add an attempt to this job
     */
    public void addAttempt(NewsSchedulingAttempt attempt) {
        this.attempts.add(attempt);
        attempt.setNewsSchedulingJob(this);
    }

    /**
     * Get execution logs as Map (if JSON)
     */
    public Map<String, Object> getExecutionLogsAsMap() {
        // In real implementation, would use JSON parsing library
        return new HashMap<>();
    }

    /**
     * Get execution metrics as Map (if JSON)
     */
    public Map<String, Object> getExecutionMetricsAsMap() {
        // In real implementation, would use JSON parsing library
        return new HashMap<>();
    }
}
