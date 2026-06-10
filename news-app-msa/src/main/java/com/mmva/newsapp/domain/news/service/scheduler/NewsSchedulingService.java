package com.mmva.newsapp.domain.news.service.scheduler;

import com.mmva.newsapp.domain.news.model.core.NewsMasterEntity;
import com.mmva.newsapp.domain.news.model.scheduler.NewsSchedulingAttempt;
import com.mmva.newsapp.domain.news.model.scheduler.NewsSchedulingJob;
import com.mmva.newsapp.domain.news.repository.scheduler.NewsSchedulingAttemptRepository;
import com.mmva.newsapp.domain.news.repository.scheduler.NewsSchedulingJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * NewsSchedulingService - Phase 1 Service Layer
 *
 * Purpose:
 * - Orchestrate scheduled news publication with full observability
 * - Manage job lifecycle (creation, processing, completion)
 * - Handle per-article errors with retry intelligence
 * - Track all publication attempts for audit and troubleshooting
 * - Support distributed systems with correlation tracking
 *
 * Integration Points:
 * - Called by NewsServiceImpl.publishScheduledNews() scheduled task
 * - Uses repositories for persistence
 * - Integrates with existing news publishing logic
 *
 * Architecture:
 * - Job-centric: Groups articles into single job execution
 * - Per-article error isolation: One article failure doesn't stop others
 * - Structured logging: [NEWS-SCHEDULING] [jobId] [correlationId] for tracing
 * - Transactional safety: All or nothing atomicity
 *
 * @author MMVA News Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NewsSchedulingService {

    // Dependencies
    private final NewsSchedulingJobRepository jobRepository;
    private final NewsSchedulingAttemptRepository attemptRepository;

    // Constants
    private static final String JOB_TRIGGER_SOURCE = "SCHEDULER";
    private static final String JOB_TRIGGER_REASON = "Automatic scheduled publication";
    private static final String PARTITION_KEY_DEFAULT = "default";
    private static final String EXECUTION_NODE = resolveExecutionNode();
    private static final long MAX_DURATION_MS = 600_000; // 10 minutes timeout

    /**
     * Start a new scheduled publish job
     * Creates job record with full execution context
     *
     * @return Created job with all metadata initialized
     */
    public NewsSchedulingJob startJob() {
        String correlationId = UUID.randomUUID().toString();
        String idempotencyKey = UUID.randomUUID().toString();

        NewsSchedulingJob job = NewsSchedulingJob.builder()
                .jobId(UUID.randomUUID())
                .correlationId(correlationId)
                .partitionKey(PARTITION_KEY_DEFAULT)
                .triggeredBy(JOB_TRIGGER_SOURCE)
                .triggerReason(JOB_TRIGGER_REASON)
                .executionNode(EXECUTION_NODE)
                .status("RUNNING")
                .startedAt(Instant.now())
                .priority("MEDIUM")
                .queueName("DEFAULT")
                .tags("scheduled_publish")
                .maxDurationMs(MAX_DURATION_MS)
                .totalArticles(0)
                .publishedCount(0)
                .failedCount(0)
                .skippedCount(0)
                .executionLogs("{}")
                .executionMetrics("{}")
                .backoffStrategy("EXPONENTIAL")
                .idempotencyKey(idempotencyKey)
                .executionPlanVersion(1)
                .version(0)
                .createdBy("SYSTEM")
                .build();

        NewsSchedulingJob savedJob = jobRepository.save(job);
        log.info("[NEWS-SCHEDULING] Job started - jobId={}, correlationId={}, executionNode={}",
                savedJob.getJobId(), correlationId, EXECUTION_NODE);

        return savedJob;
    }

    /**
     * Process single article with error isolation
     * Captures success/failure with detailed context
     *
     * @param article        Article to publish
     * @param job            Job context
     * @param publishHandler Callback to execute publish logic
     * @return Attempt record with result
     */
    public NewsSchedulingAttempt publishArticleWithErrorHandling(
            NewsMasterEntity article,
            NewsSchedulingJob job,
            ArticlePublishHandler publishHandler) {

        UUID articleId = article.getNewsNewsId();
        Instant attemptStart = Instant.now();

        try {
            // Execute publish logic with handler
            publishHandler.publish(article);

            // Success path
            Instant attemptEnd = Instant.now();
            long durationMs = attemptEnd.toEpochMilli() - attemptStart.toEpochMilli();

            NewsSchedulingAttempt attempt = NewsSchedulingAttempt.builder()
                    .attemptId(UUID.randomUUID())
                    .newsSchedulingJob(job)
                    .articleId(articleId)
                    .status("SUCCESS")
                    .attemptNumber(1)
                    .shouldRetry(false)
                    .retryCount(0)
                    .circuitBreakerTriggered(false)
                    .startedAt(attemptStart)
                    .completedAt(attemptEnd)
                    .durationMs(durationMs)
                    .idempotencyKey(UUID.randomUUID().toString())
                    .createdBy("SYSTEM")
                    .build();

            NewsSchedulingAttempt saved = attemptRepository.save(attempt);
            log.info("[NEWS-SCHEDULING] Article published - jobId={}, articleId={}, durationMs={}",
                    job.getJobId(), articleId, durationMs);

            return saved;

        } catch (Exception e) {
            // Failure path - capture error with categorization
            Instant attemptEnd = Instant.now();
            long durationMs = attemptEnd.toEpochMilli() - attemptStart.toEpochMilli();

            String errorCode = categorizeError(e);
            boolean shouldRetry = isRetryableError(errorCode);

            NewsSchedulingAttempt attempt = NewsSchedulingAttempt.builder()
                    .attemptId(UUID.randomUUID())
                    .newsSchedulingJob(job)
                    .articleId(articleId)
                    .status("FAILED")
                    .attemptNumber(1)
                    .error(e.getMessage())
                    .errorCode(errorCode)
                    .stackTrace(getStackTrace(e))
                    .shouldRetry(shouldRetry)
                    .retryCount(0)
                    .backoffDelayMs(calculateBackoff(0, "EXPONENTIAL"))
                    .nextRetryAt(shouldRetry ? attemptEnd.plusMillis(calculateBackoff(0, "EXPONENTIAL")) : null)
                    .circuitBreakerTriggered(false)
                    .startedAt(attemptStart)
                    .completedAt(attemptEnd)
                    .durationMs(durationMs)
                    .idempotencyKey(UUID.randomUUID().toString())
                    .createdBy("SYSTEM")
                    .build();

            NewsSchedulingAttempt saved = attemptRepository.save(attempt);
            log.warn("[NEWS-SCHEDULING] Article failed - jobId={}, articleId={}, errorCode={}, shouldRetry={}",
                    job.getJobId(), articleId, errorCode, shouldRetry, e);

            return saved;
        }
    }

    /**
     * Complete job with final status and metrics
     * Calculates aggregate statistics
     *
     * @param job      Job to finalize
     * @param attempts All attempts in this job
     */
    @Transactional
    public void completeJob(NewsSchedulingJob job, List<NewsSchedulingAttempt> attempts) {
        long successCount = attempts.stream().filter(a -> "SUCCESS".equals(a.getStatus())).count();
        long failedCount = attempts.stream().filter(a -> "FAILED".equals(a.getStatus())).count();
        long skippedCount = attempts.stream().filter(a -> "SKIPPED".equals(a.getStatus())).count();

        String finalStatus = (failedCount == 0) ? "SUCCESS" : (successCount > 0) ? "PARTIAL_SUCCESS" : "FAILED";

        job.setStatus(finalStatus);
        job.setCompletedAt(Instant.now());
        job.setTotalArticles((int) attempts.size());
        job.setPublishedCount((int) successCount);
        job.setFailedCount((int) failedCount);
        job.setSkippedCount((int) skippedCount);

        // Calculate duration safely - startedAt should always be set, but add defensive
        // null check
        if (job.getStartedAt() != null && job.getCompletedAt() != null) {
            long durationMs = job.getCompletedAt().toEpochMilli() - job.getStartedAt().toEpochMilli();
            job.setDurationMs(durationMs);
        } else {
            job.setDurationMs(0L);
        }

        job.setUpdatedBy("SYSTEM");
        job.setVersion(job.getVersion() != null ? job.getVersion() + 1 : 1);

        NewsSchedulingJob saved = jobRepository.save(job);
        log.info(
                "[NEWS-SCHEDULING] Job completed - jobId={}, status={}, total={}, success={}, failed={}, duration={}ms",
                job.getJobId(), finalStatus, attempts.size(), successCount, failedCount, job.getDurationMs());
    }

    /**
     * Get articles that failed but are retryable
     * Used for implementing retry logic
     *
     * @return List of failed attempts eligible for retry
     */
    public List<NewsSchedulingAttempt> getFailedArticlesForRetry() {
        return attemptRepository.findFailedArticlesForRetry();
    }

    /**
     * Get failed articles within retry limit
     * Prevents infinite retry loops
     *
     * @param maxRetries Maximum retry attempts allowed
     * @return Retryable attempts under limit
     */
    public List<NewsSchedulingAttempt> getFailedArticlesUnderRetryLimit(int maxRetries) {
        return attemptRepository.findFailedArticlesUnderRetryLimit(maxRetries);
    }

    /**
     * Get job by ID with all attempts
     *
     * @param jobId Job ID
     * @return Job with attempts loaded
     */
    public NewsSchedulingJob getJobWithAttempts(UUID jobId) {
        return jobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found: " + jobId));
    }

    /**
     * Get all attempts for a job
     *
     * @param jobId Job ID
     * @return All attempts in job
     */
    public List<NewsSchedulingAttempt> getJobAttempts(UUID jobId) {
        return attemptRepository.findByNewsSchedulingJob_JobId(jobId);
    }

    // ========================================
    // Helper Methods
    // ========================================

    /**
     * Categorize exception into machine-readable error code
     */
    private String categorizeError(Exception e) {
        String message = e.getClass().getSimpleName() + ": " + e.getMessage();

        if (e instanceof IllegalArgumentException || e instanceof IllegalStateException) {
            return "VALIDATION_ERROR";
        }
        if (e instanceof NullPointerException) {
            return "VALIDATION_ERROR";
        }
        if (message.contains("timeout") || message.contains("Timeout")) {
            return "TIMEOUT";
        }
        if (message.contains("Connection") || message.contains("Network")) {
            return "NETWORK_ERROR";
        }
        if (message.contains("Auth") || message.contains("Permission") || message.contains("Forbidden")) {
            return "AUTH_FAILED";
        }
        if (message.contains("NotFound") || message.contains("404")) {
            return "NOT_FOUND";
        }

        return "UNKNOWN_ERROR";
    }

    /**
     * Determine if error is retryable (transient) vs permanent
     */
    private boolean isRetryableError(String errorCode) {
        return switch (errorCode) {
            case "TIMEOUT", "NETWORK_ERROR" -> true;
            case "VALIDATION_ERROR", "AUTH_FAILED", "NOT_FOUND", "UNKNOWN_ERROR" -> false;
            default -> false;
        };
    }

    /**
     * Calculate backoff delay based on strategy and retry count
     */
    private long calculateBackoff(int retryCount, String strategy) {
        long baseDelayMs = 1_000; // Start with 1 second

        return switch (strategy) {
            case "EXPONENTIAL" -> baseDelayMs * (long) Math.pow(2, Math.min(retryCount, 5)); // Cap at 32 seconds
            case "LINEAR" -> baseDelayMs * (retryCount + 1);
            case "FIXED" -> baseDelayMs;
            default -> baseDelayMs;
        };
    }

    /**
     * Get full stack trace for debugging
     */
    private String getStackTrace(Exception e) {
        StringBuilder sb = new StringBuilder();
        sb.append(e.getClass().getName()).append(": ").append(e.getMessage()).append("\n");
        for (StackTraceElement element : e.getStackTrace()) {
            sb.append("\tat ").append(element.toString()).append("\n");
        }
        return sb.toString();
    }

    /**
     * Resolve execution node (pod/server name)
     */
    private static String resolveExecutionNode() {
        try {
            return java.net.InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return "unknown-node";
        }
    }

    /**
     * Functional interface for article publication logic
     * Allows NewsServiceImpl to inject its own publish logic
     */
    @FunctionalInterface
    public interface ArticlePublishHandler {
        /**
         * Execute publish logic for article
         *
         * @param article Article to publish
         * @throws Exception if publish fails
         */
        void publish(NewsMasterEntity article) throws Exception;
    }
}
