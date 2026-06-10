package com.mmva.newsapp.domain.news.controller;

import com.mmva.newsapp.domain.news.controller.dto.scheduler.NewsSchedulingAttemptResponseDto;
import com.mmva.newsapp.domain.news.controller.dto.scheduler.NewsSchedulingJobResponseDto;
import com.mmva.newsapp.domain.news.controller.dto.scheduler.SchedulerMetricsResponseDto;
import com.mmva.newsapp.domain.news.model.scheduler.NewsSchedulingAttempt;
import com.mmva.newsapp.domain.news.model.scheduler.NewsSchedulingJob;
import com.mmva.newsapp.domain.news.repository.scheduler.NewsSchedulingAttemptRepository;
import com.mmva.newsapp.domain.news.repository.scheduler.NewsSchedulingJobRepository;
import com.mmva.newsapp.domain.news.service.scheduler.ScheduledPublishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * DEPRECATED: This controller is superseded by NewsSchedulingAdminController
 * in the admin/scheduler package.
 * 
 * This file is kept for reference only and is NOT registered as a bean.
 * All scheduler admin endpoints are now managed by:
 * com.mmva.newsapp.domain.news.controller.admin.scheduler.NewsSchedulingAdminController
 * 
 * REST Controller for administrative scheduler operations.
 * 
 * Provides endpoints for:
 * - Manual job triggering with optional parameters (priority, tags)
 * - Job status monitoring and detail retrieval
 * - Failed article management and retry filtering
 * - Performance metrics and analytics
 * 
 * All endpoints require ADMIN role or scheduler.control permission.
 * Authorization: @PreAuthorize("hasRole('ADMIN') or hasPermission('scheduler',
 * 'control')")
 */
@Slf4j
// @RestController // DISABLED - Use NewsSchedulingAdminController instead
@RequestMapping("/api/v1/admin/scheduler/publish")
public class ScheduledPublishAdminController {

    private final ScheduledPublishService scheduledPublishService;
    private final NewsSchedulingJobRepository jobRepository;
    private final NewsSchedulingAttemptRepository attemptRepository;

    public ScheduledPublishAdminController(
            ScheduledPublishService scheduledPublishService,
            NewsSchedulingJobRepository jobRepository,
            NewsSchedulingAttemptRepository attemptRepository) {
        this.scheduledPublishService = scheduledPublishService;
        this.jobRepository = jobRepository;
        this.attemptRepository = attemptRepository;
    }

    /**
     * Manually trigger scheduled news publication.
     * 
     * Endpoint: POST /api/v1/admin/scheduler/publish/trigger
     * 
     * Optional query parameters:
     * - priority: Job priority (CRITICAL, HIGH, MEDIUM, LOW) - default: MEDIUM
     * - queueName: Queue routing (DEFAULT, BREAKING_NEWS, REGIONS_LATAM,
     * REGIONS_APAC) - default: DEFAULT
     * - tags: Comma-separated tags for classification (optional)
     * 
     * Response: 201 Created with job details
     * 
     * @param priority  optional job priority
     * @param queueName optional queue name for routing
     * @param tags      optional comma-separated tags
     * @return ResponseEntity with created job details
     */
    @PostMapping("/trigger")
    @PreAuthorize("hasRole('ADMIN') or hasPermission('scheduler', 'control')")
    public ResponseEntity<NewsSchedulingJobResponseDto> triggerManualPublish(
            @RequestParam(value = "priority", required = false, defaultValue = "MEDIUM") String priority,
            @RequestParam(value = "queueName", required = false, defaultValue = "DEFAULT") String queueName,
            @RequestParam(value = "tags", required = false) String tags) {

        try {
            log.info("[SCHEDULER-ADMIN] Manual publication triggered by admin - priority={}, queue={}", priority,
                    queueName);

            // Create job with admin trigger source
            var job = new NewsSchedulingJob();
            job.setJobId(UUID.randomUUID());
            job.setCorrelationId(UUID.randomUUID().toString());
            job.setStatus("RUNNING");
            job.setTriggeredBy("ADMIN_MANUAL");
            job.setTriggerReason("Manual publication triggered by admin");
            job.setStartedAt(Instant.now());
            job.setPriority(priority);
            job.setQueueName(queueName);
            if (tags != null && !tags.isBlank()) {
                job.setTags(tags);
            }
            job.setPartitionKey("admin-manual");
            job.setMaxDurationMs(600_000L); // 10 minute timeout
            job.setVersion(0);

            var savedJob = jobRepository.save(job);
            log.info("[SCHEDULER-ADMIN] Job created - jobId={}, correlationId={}", savedJob.getJobId(),
                    savedJob.getCorrelationId());

            return ResponseEntity.status(HttpStatus.CREATED).body(mapJobToDto(savedJob));

        } catch (Exception e) {
            log.error("[SCHEDULER-ADMIN] Error triggering manual publication", e);
            throw e;
        }
    }

    /**
     * Get detailed job status and execution results.
     * 
     * Endpoint: GET /api/v1/admin/scheduler/publish/jobs/{jobId}
     * 
     * Path parameter:
     * - jobId: UUID of the job to retrieve
     * 
     * Response: 200 OK with complete job details including:
     * - Job metadata (timing, status, trigger source)
     * - Aggregate metrics (total, published, failed, skipped counts)
     * - Success/failure/skip rates
     * - Associated attempts with error details
     * 
     * @param jobId UUID of job to retrieve
     * @return ResponseEntity with job details
     */
    @GetMapping("/jobs/{jobId}")
    @PreAuthorize("hasRole('ADMIN') or hasPermission('scheduler', 'view')")
    public ResponseEntity<NewsSchedulingJobResponseDto> getJobStatus(
            @PathVariable UUID jobId) {

        try {
            log.info("[SCHEDULER-ADMIN] Fetching job status - jobId={}", jobId);

            var job = jobRepository.findById(jobId)
                    .orElseThrow(() -> {
                        log.warn("[SCHEDULER-ADMIN] Job not found - jobId={}", jobId);
                        return new IllegalArgumentException("Job not found: " + jobId);
                    });

            // Fetch associated attempts
            var attempts = attemptRepository.findByNewsSchedulingJob_JobId(jobId);
            var jobDto = mapJobToDto(job);
            jobDto.setAttempts(attempts.stream()
                    .map(this::mapAttemptToDto)
                    .collect(Collectors.toList()));

            log.info("[SCHEDULER-ADMIN] Job retrieved - jobId={}, status={}, attempts={}",
                    jobId, job.getStatus(), attempts.size());

            return ResponseEntity.ok(jobDto);

        } catch (IllegalArgumentException e) {
            log.warn("[SCHEDULER-ADMIN] Invalid job ID - jobId={}", jobId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("[SCHEDULER-ADMIN] Error retrieving job status - jobId={}", jobId, e);
            throw e;
        }
    }

    /**
     * Get list of failed articles that can be retried.
     * 
     * Endpoint: GET /api/v1/admin/scheduler/publish/failed-articles
     * 
     * Query parameters:
     * - maxRetries: Maximum retry count filter (default: 3) - returns articles with
     * retryCount < maxRetries
     * - limit: Maximum number of results to return (default: 100, max: 1000)
     * 
     * Response: 200 OK with list of failed articles that:
     * - Have status FAILED
     * - Have shouldRetry = true
     * - Have retryCount < maxRetries
     * 
     * Includes error codes and backoff delays for admin decision making.
     * 
     * @param maxRetries maximum retry attempts allowed
     * @param limit      maximum results to return
     * @return ResponseEntity with list of retry-eligible failed attempts
     */
    @GetMapping("/failed-articles")
    @PreAuthorize("hasRole('ADMIN') or hasPermission('scheduler', 'view')")
    public ResponseEntity<List<NewsSchedulingAttemptResponseDto>> getFailedArticlesForRetry(
            @RequestParam(value = "maxRetries", required = false, defaultValue = "3") int maxRetries,
            @RequestParam(value = "limit", required = false, defaultValue = "100") int limit) {

        try {
            log.info("[SCHEDULER-ADMIN] Fetching failed articles for retry - maxRetries={}, limit={}", maxRetries,
                    limit);

            // Get all failed articles that should retry
            var failedAttempts = attemptRepository.findByStatus("FAILED");

            var retryEligible = failedAttempts.stream()
                    .filter(a -> Boolean.TRUE.equals(a.getShouldRetry()))
                    .filter(a -> a.getRetryCount() != null && a.getRetryCount() < maxRetries)
                    .limit(Math.min(limit, 1000))
                    .map(this::mapAttemptToDto)
                    .collect(Collectors.toList());

            log.info("[SCHEDULER-ADMIN] Retrieved {} failed articles eligible for retry", retryEligible.size());

            return ResponseEntity.ok(retryEligible);

        } catch (Exception e) {
            log.error("[SCHEDULER-ADMIN] Error retrieving failed articles", e);
            throw e;
        }
    }

    /**
     * Get scheduler performance metrics and analytics.
     * 
     * Endpoint: GET /api/v1/admin/scheduler/publish/metrics
     * 
     * Query parameters:
     * - timePeriod: Time period for metrics (default: 24h)
     * Valid values: 1h, 6h, 24h, 7d, 30d
     * - startAt: Optional explicit start timestamp (ISO 8601)
     * - endAt: Optional explicit end timestamp (ISO 8601)
     * 
     * Response: 200 OK with comprehensive metrics including:
     * - Job counts: total, successful, failed, partial_success, running
     * - Success/failure/partial rates
     * - Article counts: processed, published, failed, skipped
     * - Duration statistics: avg, min, max, median, p95, p99
     * - Retry queue info: jobs awaiting retry, articles awaiting retry
     * - Error analysis: most common error code and frequency
     * 
     * @param timePeriod time period for metrics calculation
     * @param startAt    optional explicit start timestamp
     * @param endAt      optional explicit end timestamp
     * @return ResponseEntity with metrics
     */
    @GetMapping("/metrics")
    @PreAuthorize("hasRole('ADMIN') or hasPermission('scheduler', 'view')")
    public ResponseEntity<SchedulerMetricsResponseDto> getMetrics(
            @RequestParam(value = "timePeriod", required = false, defaultValue = "24h") String timePeriod,
            @RequestParam(value = "startAt", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startAt,
            @RequestParam(value = "endAt", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endAt) {

        try {
            log.info("[SCHEDULER-ADMIN] Calculating metrics - timePeriod={}", timePeriod);

            // Calculate time range
            Instant now = Instant.now();
            Instant periodStart;
            Instant periodEnd;

            if (startAt != null && endAt != null) {
                periodStart = startAt;
                periodEnd = endAt;
            } else {
                periodEnd = now;
                periodStart = switch (timePeriod) {
                    case "1h" -> now.minus(1, ChronoUnit.HOURS);
                    case "6h" -> now.minus(6, ChronoUnit.HOURS);
                    case "7d" -> now.minus(7, ChronoUnit.DAYS);
                    case "30d" -> now.minus(30, ChronoUnit.DAYS);
                    default -> now.minus(24, ChronoUnit.HOURS); // default 24h
                };
            }

            // Fetch jobs in period
            var jobs = jobRepository.findByStartedAtBetweenOrderByStartedAtDesc(periodStart, periodEnd)
                    .orElse(new ArrayList<>());

            // Calculate metrics
            long totalJobs = jobs.size();
            long successfulJobs = jobs.stream().filter(j -> "SUCCESS".equals(j.getStatus())).count();
            long failedJobs = jobs.stream().filter(j -> "FAILED".equals(j.getStatus())).count();
            long partialSuccessJobs = jobs.stream().filter(j -> "PARTIAL_SUCCESS".equals(j.getStatus())).count();
            long runningJobs = jobs.stream().filter(j -> "RUNNING".equals(j.getStatus())).count();

            double successRate = totalJobs > 0 ? (successfulJobs * 100.0) / totalJobs : 0;
            double failureRate = totalJobs > 0 ? (failedJobs * 100.0) / totalJobs : 0;
            double partialSuccessRate = totalJobs > 0 ? (partialSuccessJobs * 100.0) / totalJobs : 0;

            // Article metrics
            long totalArticles = jobs.stream()
                    .mapToLong(j -> j.getTotalArticles() != null ? j.getTotalArticles() : 0)
                    .sum();
            long articlesPublished = jobs.stream()
                    .mapToLong(j -> j.getPublishedCount() != null ? j.getPublishedCount() : 0)
                    .sum();
            long articlesFailed = jobs.stream()
                    .mapToLong(j -> j.getFailedCount() != null ? j.getFailedCount() : 0)
                    .sum();
            long articlesSkipped = jobs.stream()
                    .mapToLong(j -> j.getSkippedCount() != null ? j.getSkippedCount() : 0)
                    .sum();

            // Duration statistics
            var durations = jobs.stream()
                    .filter(j -> j.getDurationMs() != null)
                    .map(NewsSchedulingJob::getDurationMs)
                    .sorted()
                    .collect(Collectors.toList());

            long avgDuration = durations.isEmpty() ? 0
                    : durations.stream().mapToLong(Long::longValue).sum() / durations.size();
            long minDuration = durations.isEmpty() ? 0 : durations.get(0);
            long maxDuration = durations.isEmpty() ? 0 : durations.get(durations.size() - 1);
            long medianDuration = durations.isEmpty() ? 0 : durations.get(durations.size() / 2);

            // Percentiles
            long p95Duration = durations.isEmpty() ? 0 : durations.get((int) (durations.size() * 0.95));
            long p99Duration = durations.isEmpty() ? 0 : durations.get((int) (durations.size() * 0.99));

            // Retry queue
            var retryEligible = attemptRepository.findByStatus("FAILED").stream()
                    .filter(a -> Boolean.TRUE.equals(a.getShouldRetry()))
                    .collect(Collectors.toList());

            long jobsInRetryQueue = retryEligible.stream()
                    .map(a -> a.getNewsSchedulingJob().getJobId())
                    .distinct()
                    .count();
            long articlesAwaitingRetry = retryEligible.size();

            // Most common error
            String mostCommonError = null;
            long mostCommonErrorCount = 0;

            var errorCodes = attemptRepository.findByStatus("FAILED").stream()
                    .collect(Collectors.groupingByConcurrent(
                            NewsSchedulingAttempt::getErrorCode,
                            Collectors.counting()));

            var maxError = errorCodes.entrySet().stream()
                    .max(Map.Entry.comparingByValue());

            if (maxError.isPresent()) {
                mostCommonError = maxError.get().getKey();
                mostCommonErrorCount = maxError.get().getValue();
            }

            double avgArticlesPerJob = totalJobs > 0 ? (double) totalArticles / totalJobs : 0;

            var metrics = SchedulerMetricsResponseDto.builder()
                    .totalJobs(totalJobs)
                    .successfulJobs(successfulJobs)
                    .failedJobs(failedJobs)
                    .partialSuccessJobs(partialSuccessJobs)
                    .runningJobs(runningJobs)
                    .successRate(Math.round(successRate * 100.0) / 100.0)
                    .failureRate(Math.round(failureRate * 100.0) / 100.0)
                    .partialSuccessRate(Math.round(partialSuccessRate * 100.0) / 100.0)
                    .totalArticlesProcessed(totalArticles)
                    .articlesSuccessfullyPublished(articlesPublished)
                    .articlesFailed(articlesFailed)
                    .articlesSkipped(articlesSkipped)
                    .avgJobDurationMs(avgDuration)
                    .minJobDurationMs(minDuration)
                    .maxJobDurationMs(maxDuration)
                    .medianJobDurationMs(medianDuration)
                    .p95JobDurationMs(p95Duration)
                    .p99JobDurationMs(p99Duration)
                    .avgArticlesPerJob(Math.round(avgArticlesPerJob * 100.0) / 100.0)
                    .jobsInRetryQueue(jobsInRetryQueue)
                    .articlesAwaitingRetry(articlesAwaitingRetry)
                    .mostCommonErrorCode(mostCommonError)
                    .mostCommonErrorCount(mostCommonErrorCount)
                    .calculatedAt(now)
                    .timePeriod(timePeriod)
                    .periodStartAt(periodStart)
                    .periodEndAt(periodEnd)
                    .build();

            log.info("[SCHEDULER-ADMIN] Metrics calculated - totalJobs={}, successRate={}%, avgDuration={}ms",
                    totalJobs, Math.round(successRate), avgDuration);

            return ResponseEntity.ok(metrics);

        } catch (Exception e) {
            log.error("[SCHEDULER-ADMIN] Error calculating metrics", e);
            throw e;
        }
    }

    /**
     * Map NewsSchedulingJob entity to response DTO.
     */
    private NewsSchedulingJobResponseDto mapJobToDto(NewsSchedulingJob job) {
        return NewsSchedulingJobResponseDto.builder()
                .jobId(job.getJobId())
                .correlationId(job.getCorrelationId())
                .status(job.getStatus())
                .triggeredBy(job.getTriggeredBy())
                .triggerReason(job.getTriggerReason())
                .triggeredByUserId(job.getTriggeredByUserId())
                .startedAt(job.getStartedAt())
                .completedAt(job.getCompletedAt())
                .durationMs(job.getDurationMs())
                .queueWaitMs(job.getQueueWaitMs())
                .totalArticles(job.getTotalArticles())
                .publishedCount(job.getPublishedCount())
                .failedCount(job.getFailedCount())
                .skippedCount(job.getSkippedCount())
                .errorMessage(job.getErrorMessage())
                .priority(job.getPriority())
                .queueName(job.getQueueName())
                .backoffStrategy(job.getBackoffStrategy())
                .maxDurationMs(job.getMaxDurationMs())
                .executionLogs(job.getExecutionLogs())
                .executionMetrics(job.getExecutionMetrics())
                .version(job.getVersion())
                .createdAt(job.getCreatedAt())
                .updatedAt(job.getUpdatedAt())
                .build();
    }

    /**
     * Map NewsSchedulingAttempt entity to response DTO.
     */
    private NewsSchedulingAttemptResponseDto mapAttemptToDto(NewsSchedulingAttempt attempt) {
        return NewsSchedulingAttemptResponseDto.builder()
                .attemptId(attempt.getAttemptId())
                .jobId(attempt.getNewsSchedulingJob() != null ? attempt.getNewsSchedulingJob().getJobId() : null)
                .articleId(attempt.getArticleId())
                .status(attempt.getStatus())
                .attemptNumber(attempt.getAttemptNumber())
                .error(attempt.getError())
                .errorCode(attempt.getErrorCode())
                .stackTrace(attempt.getStackTrace())
                .startedAt(attempt.getStartedAt())
                .completedAt(attempt.getCompletedAt())
                .durationMs(attempt.getDurationMs())
                .retryCount(attempt.getRetryCount())
                .shouldRetry(attempt.getShouldRetry())
                .backoffDelayMs(attempt.getBackoffDelayMs())
                .nextRetryAt(attempt.getNextRetryAt())
                .idempotencyKey(attempt.getIdempotencyKey())
                .circuitBreakerTriggered(attempt.getCircuitBreakerTriggered())
                .correlationIdAttempt(attempt.getCorrelationIdAttempt())
                .createdAt(attempt.getCreatedAt())
                .updatedAt(attempt.getUpdatedAt())
                .build();
    }
}
