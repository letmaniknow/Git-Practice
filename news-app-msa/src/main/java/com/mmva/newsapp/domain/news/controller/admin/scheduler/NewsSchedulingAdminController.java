package com.mmva.newsapp.domain.news.controller.admin.scheduler;

import com.mmva.newsapp.domain.news.dto.scheduler.NewsSchedulingAttemptResponseDto;
import com.mmva.newsapp.domain.news.dto.scheduler.NewsSchedulingBulkRetryResponseDto;
import com.mmva.newsapp.domain.news.dto.scheduler.NewsSchedulingJobResponseDto;
import com.mmva.newsapp.domain.news.dto.scheduler.NewsSchedulingMetricsResponseDto;
import com.mmva.newsapp.domain.news.model.scheduler.NewsSchedulingAttempt;
import com.mmva.newsapp.domain.news.model.scheduler.NewsSchedulingJob;
import com.mmva.newsapp.domain.news.repository.scheduler.NewsSchedulingAttemptRepository;
import com.mmva.newsapp.domain.news.repository.scheduler.NewsSchedulingJobRepository;
import com.mmva.newsapp.domain.news.service.scheduler.NewsSchedulingService;
import com.mmva.newsapp.infrastructure.common.api.dto.ApiResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * NewsSchedulingAdminController
 *
 * REST endpoints for admin control of news article scheduling.
 * Provides job management, status visibility, and performance metrics.
 *
 * Base Path: /api/v1/admin/scheduler
 * Authorization: Multiple access patterns (class-level)
 * - Role-based: hasRole('ADMIN') | hasRole('SUPER_ADMIN')
 * - Authority-based: hasAuthority('scheduler.manage')
 *
 * Access available to:
 * - Super Admin (via SUPER_ADMIN role)
 * - Admin users (via ADMIN role)
 * - Specific users with 'scheduler.manage' authority (for future RBAC UI)
 *
 * Endpoints:
 * 1. POST /publish/trigger - Manual job trigger
 * 2. GET /publish/jobs/{jobId} - Job status details
 * 3. GET /publish/jobs - Paginated jobs list
 * 4. GET /publish/failed-articles - Failed articles list (with pagination)
 * 5. POST /publish/failed-articles/retry-all - Bulk retry failed articles
 * 6. GET /publish/metrics - Performance metrics
 * 7. POST /jobs/{jobId}/cancel - Cancel a running job
 * 8. DELETE /jobs/{jobId} - Delete/cleanup job record
 *
 * @author MMVA News Team
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/scheduler")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('scheduler.manage')")
public class NewsSchedulingAdminController {

        private final NewsSchedulingService newsSchedulingService;
        private final NewsSchedulingJobRepository jobRepository;
        private final NewsSchedulingAttemptRepository attemptRepository;

        /**
         * Trigger manual scheduled publish job
         *
         * POST /api/v1/admin/scheduler/publish/trigger
         *
         * Allows admins to manually trigger the scheduled publish job
         * instead of waiting for the automatic scheduled execution.
         * Useful for urgent publication needs.
         *
         * @param priority  Optional job priority (CRITICAL, HIGH, MEDIUM, LOW)
         * @param tags      Optional comma-separated tags
         * @param queueName Optional queue name (DEFAULT, BREAKING_NEWS, REGIONS_LATAM,
         *                  REGIONS_APAC)
         * @return NewsSchedulingJobResponseDto with job details
         */
        @PostMapping("/publish/trigger")
        public ResponseEntity<ApiResponseDto<NewsSchedulingJobResponseDto>> triggerManualPublish(
                        @RequestParam(value = "priority", required = false, defaultValue = "HIGH") String priority,
                        @RequestParam(value = "tags", required = false) String tags,
                        @RequestParam(value = "queueName", required = false, defaultValue = "DEFAULT") String queueName) {

                log.info("[NEWS-SCHEDULING-ADMIN] Manual publish trigger - priority={}, queueName={}", priority,
                                queueName);

                var job = newsSchedulingService.startJob();
                job.setPriority(priority);
                job.setQueueName(queueName);
                if (tags != null && !tags.isEmpty()) {
                        job.setTags(tags);
                }
                job.setTriggeredBy("ADMIN_MANUAL");

                var savedJob = jobRepository.save(job);
                var response = mapJobToResponse(savedJob);

                log.info("[NEWS-SCHEDULING-ADMIN] Manual job created - jobId={}, priority={}", job.getJobId(),
                                priority);

                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponseDto.success("Manual publish job triggered successfully", response));
        }

        /**
         * Get job status and details
         *
         * GET /api/v1/admin/scheduler/publish/jobs/{jobId}
         *
         * Returns complete job status including:
         * - Job metadata and execution context
         * - All attempt records
         * - Success/failure statistics
         * - Performance metrics (duration, throughput)
         *
         * @param jobId Job ID (UUID)
         * @return NewsSchedulingJobResponseDto with complete job details
         */
        @GetMapping("/publish/jobs/{jobId}")
        public ResponseEntity<ApiResponseDto<NewsSchedulingJobResponseDto>> getJobStatus(
                        @PathVariable UUID jobId) {

                log.debug("[NEWS-SCHEDULING-ADMIN] Fetching job status - jobId={}", jobId);

                var job = jobRepository.findById(jobId)
                                .orElseThrow(() -> {
                                        log.warn("[NEWS-SCHEDULING-ADMIN] Job not found - jobId={}", jobId);
                                        return new IllegalArgumentException("Job not found: " + jobId);
                                });

                var response = mapJobToResponse(job);
                return ResponseEntity.ok(ApiResponseDto.success("Job status retrieved successfully", response));
        }

        /**
         * Get paginated list of all scheduler jobs
         *
         * GET /api/v1/admin/scheduler/publish/jobs?page=0&size=20&sort=startedAt,desc
         *
         * Returns paginated list of all scheduler jobs with:
         * - Job metadata and execution status
         * - Success/failure summary
         * - Performance metrics per job
         *
         * Query Parameters:
         * - page: Page number (optional, default=0)
         * - size: Results per page (optional, default=20, max=100)
         * - sort: Sort field,direction (optional, default=startedAt,desc)
         * - status: Filter by job status (optional)
         * - priority: Filter by job priority (optional)
         *
         * @param pageable Pagination info (page, size, sort)
         * @param status   Optional status filter
         * @param priority Optional priority filter
         * @return Page of jobs with summary information
         */
        @GetMapping("/publish/jobs")
        public ResponseEntity<ApiResponseDto<Page<NewsSchedulingJobResponseDto>>> getJobs(
                        Pageable pageable,
                        @RequestParam(value = "status", required = false) String status,
                        @RequestParam(value = "priority", required = false) String priority) {

                log.debug("[NEWS-SCHEDULING-ADMIN] Fetching jobs list - page={}, size={}, status={}, priority={}",
                                pageable.getPageNumber(), pageable.getPageSize(), status, priority);

                // Fetch jobs (could optimize with database query for filtering)
                List<NewsSchedulingJob> allJobs = jobRepository.findAll();

                // Apply filters if provided
                if (status != null && !status.isEmpty()) {
                        allJobs = allJobs.stream()
                                        .filter(j -> status.equals(j.getStatus()))
                                        .toList();
                }
                if (priority != null && !priority.isEmpty()) {
                        allJobs = allJobs.stream()
                                        .filter(j -> priority.equals(j.getPriority()))
                                        .toList();
                }

                // Sort by the requested field
                String sortField = pageable.getSort().isEmpty() ? "startedAt"
                                : pageable.getSort().stream().findFirst().get().getProperty();
                boolean ascending = pageable.getSort().isEmpty() ? false
                                : pageable.getSort().stream().findFirst().get().isAscending();

                allJobs = allJobs.stream()
                                .sorted((a, b) -> {
                                        int comparison = 0;
                                        if ("startedAt".equals(sortField)) {
                                                comparison = a.getStartedAt().compareTo(b.getStartedAt());
                                        } else if ("status".equals(sortField)) {
                                                comparison = a.getStatus().compareTo(b.getStatus());
                                        } else if ("priority".equals(sortField)) {
                                                comparison = a.getPriority().compareTo(b.getPriority());
                                        } else if ("durationMs".equals(sortField)) {
                                                long aDuration = a.getDurationMs() != null ? a.getDurationMs() : 0;
                                                long bDuration = b.getDurationMs() != null ? b.getDurationMs() : 0;
                                                comparison = Long.compare(aDuration, bDuration);
                                        }
                                        return ascending ? comparison : -comparison;
                                })
                                .toList();

                // Apply pagination
                int start = pageable.getPageNumber() * pageable.getPageSize();
                int end = Math.min(start + pageable.getPageSize(), allJobs.size());
                List<NewsSchedulingJobResponseDto> pageContent = allJobs
                                .subList(start, end).stream()
                                .map(this::mapJobToResponse)
                                .toList();

                Page<NewsSchedulingJobResponseDto> result = new PageImpl<>(pageContent, pageable, allJobs.size());

                log.info("[NEWS-SCHEDULING-ADMIN] Retrieved jobs list - count={}, totalPages={}, status={}, priority={}",
                                pageContent.size(), result.getTotalPages(), status, priority);

                return ResponseEntity.ok(ApiResponseDto.success("Jobs retrieved successfully", result));
        }

        /**
         * Get failed articles requiring manual intervention
         *
         * GET /api/v1/admin/scheduler/publish/failed-articles?maxRetries=3
         *
         * Returns list of articles that failed publication with:
         * - Error details and categorization
         * - Retry eligibility
         * - Backoff delay calculations
         * - Stack traces for debugging
         *
         * Query Parameters:
         * - maxRetries: Filter attempts under retry limit (optional, default=3)
         * - page: Page number (optional, default=0)
         * - size: Results per page (optional, default=20)
         * - sort: Sort field (optional, default=timestamp,desc)
         *
         * @param maxRetries Maximum retries to filter (default: 3)
         * @param pageable   Pagination info (page, size, sort)
         * @return Page of failed attempts
         */
        @GetMapping("/publish/failed-articles")
        public ResponseEntity<ApiResponseDto<Page<NewsSchedulingAttemptResponseDto>>> getFailedArticles(
                        @RequestParam(value = "maxRetries", required = false, defaultValue = "3") int maxRetries,
                        Pageable pageable) {

                log.debug("[NEWS-SCHEDULING-ADMIN] Fetching failed articles - maxRetries={}, page={}, size={}",
                                maxRetries, pageable.getPageNumber(), pageable.getPageSize());

                // Get all failed attempts (could optimize with dedicated repository query)
                List<NewsSchedulingAttempt> allFailedAttempts = attemptRepository
                                .findFailedArticlesUnderRetryLimit(maxRetries);

                // Convert to DTOs
                List<NewsSchedulingAttemptResponseDto> allDtos = allFailedAttempts.stream()
                                .map(this::mapAttemptToResponse)
                                .toList();

                // Manual pagination
                int start = pageable.getPageNumber() * pageable.getPageSize();
                int end = Math.min(start + pageable.getPageSize(), allDtos.size());
                List<NewsSchedulingAttemptResponseDto> pageContent = allDtos.subList(start, end);

                Page<NewsSchedulingAttemptResponseDto> result = new PageImpl<>(pageContent, pageable, allDtos.size());

                log.info("[NEWS-SCHEDULING-ADMIN] Retrieved failed articles - count={}, totalPages={}",
                                pageContent.size(), result.getTotalPages());

                return ResponseEntity.ok(ApiResponseDto.success("Failed articles retrieved successfully", result));
        }

        /**
         * Get scheduler performance metrics
         *
         * GET /api/v1/admin/scheduler/publish/metrics?timePeriod=24h
         *
         * Returns aggregated metrics for monitoring and alerting:
         * - Job success/failure rates
         * - Article publication throughput
         * - Average execution duration
         * - Error categorization
         * - Retry effectiveness
         * - Stuck job detection
         *
         * Query Parameters:
         * - timePeriod: Time range (1h, 6h, 24h, 7d) (optional, default=24h)
         *
         * @param timePeriod Time period for metrics aggregation
         * @return NewsSchedulingMetricsResponseDto with aggregated metrics
         */
        @GetMapping("/publish/metrics")
        public ResponseEntity<ApiResponseDto<NewsSchedulingMetricsResponseDto>> getMetrics(
                        @RequestParam(value = "timePeriod", required = false, defaultValue = "24h") String timePeriod) {

                log.debug("[NEWS-SCHEDULING-ADMIN] Fetching metrics - timePeriod={}", timePeriod);

                Instant startTime = calculateStartTime(timePeriod);
                String periodLabel = formatPeriodLabel(timePeriod);

                // Fetch jobs in time period
                List<NewsSchedulingJob> jobs = jobRepository.findByStartedAtBetween(startTime, Instant.now());

                // Calculate metrics
                long totalJobs = jobs.size();
                long successfulJobs = jobs.stream().filter(j -> "SUCCESS".equals(j.getStatus())).count();
                long failedJobs = jobs.stream().filter(j -> "FAILED".equals(j.getStatus())).count();
                long partialSuccessJobs = jobs.stream().filter(j -> "PARTIAL_SUCCESS".equals(j.getStatus())).count();
                long runningJobs = jobs.stream().filter(j -> "RUNNING".equals(j.getStatus())).count();

                // Calculate success rates
                double successRatePercent = totalJobs > 0 ? (successfulJobs * 100.0) / totalJobs : 0;
                double failureRatePercent = totalJobs > 0 ? (failedJobs * 100.0) / totalJobs : 0;
                double partialSuccessRatePercent = totalJobs > 0 ? (partialSuccessJobs * 100.0) / totalJobs : 0;

                // Calculate average duration
                long averageDurationMs = (long) jobs.stream()
                                .filter(j -> j.getDurationMs() != null && j.getDurationMs() > 0)
                                .mapToLong(NewsSchedulingJob::getDurationMs)
                                .average()
                                .orElse(0.0);

                // Calculate article stats
                long totalArticlesPublished = jobs.stream()
                                .mapToLong(j -> j.getPublishedCount() != null ? j.getPublishedCount() : 0)
                                .sum();
                long totalArticlesFailed = jobs.stream()
                                .mapToLong(j -> j.getFailedCount() != null ? j.getFailedCount() : 0)
                                .sum();
                long totalArticlesSkipped = jobs.stream()
                                .mapToLong(j -> j.getSkippedCount() != null ? j.getSkippedCount() : 0)
                                .sum();

                long totalArticles = totalArticlesPublished + totalArticlesFailed + totalArticlesSkipped;
                double articlesSuccessRatePercent = totalArticles > 0 ? (totalArticlesPublished * 100.0) / totalArticles
                                : 0;

                // Detect stuck jobs (running for > 15 minutes)
                long stuckJobsCount = jobs.stream()
                                .filter(j -> "RUNNING".equals(j.getStatus()) &&
                                                j.getStartedAt().isBefore(Instant.now().minus(15, ChronoUnit.MINUTES)))
                                .count();

                // Detect jobs requiring intervention (stuck + failed)
                long jobsRequiringManualIntervention = stuckJobsCount + failedJobs;

                // Calculate retry stats
                List<NewsSchedulingAttempt> attempts = attemptRepository
                                .findByTimestampBetween(startTime, Instant.now());
                long totalRetriedArticles = attempts.stream()
                                .filter(a -> a.getRetryCount() != null && a.getRetryCount() > 0)
                                .count();
                long successfulRetries = attempts.stream()
                                .filter(a -> a.getRetryCount() != null && a.getRetryCount() > 0
                                                && "SUCCESS".equals(a.getStatus()))
                                .count();
                double retrySuccessRatePercent = totalRetriedArticles > 0
                                ? (successfulRetries * 100.0) / totalRetriedArticles
                                : 0;
                double averageRetryAttempts = attempts.stream()
                                .mapToInt(a -> a.getRetryCount() != null ? a.getRetryCount() : 0)
                                .average()
                                .orElse(0);

                // Find most common error code
                String mostCommonErrorCode = attempts.stream()
                                .map(NewsSchedulingAttempt::getErrorCode)
                                .filter(Objects::nonNull)
                                .collect(Collectors.groupingBy(e -> e, Collectors.counting()))
                                .entrySet().stream()
                                .max((a, b) -> a.getValue().compareTo(b.getValue()))
                                .map(java.util.Map.Entry::getKey)
                                .orElse("N/A");

                var metrics = NewsSchedulingMetricsResponseDto.builder()
                                .totalJobs(totalJobs)
                                .successfulJobs(successfulJobs)
                                .failedJobs(failedJobs)
                                .partialSuccessJobs(partialSuccessJobs)
                                .runningJobs(runningJobs)
                                .successRatePercent(Math.round(successRatePercent * 100.0) / 100.0)
                                .failureRatePercent(Math.round(failureRatePercent * 100.0) / 100.0)
                                .partialSuccessRatePercent(Math.round(partialSuccessRatePercent * 100.0) / 100.0)
                                .averageDurationMs(averageDurationMs)
                                .totalArticlesPublished(totalArticlesPublished)
                                .totalArticlesFailed(totalArticlesFailed)
                                .totalArticlesSkipped(totalArticlesSkipped)
                                .articlesSuccessRatePercent(Math.round(articlesSuccessRatePercent * 100.0) / 100.0)
                                .mostCommonErrorCode(mostCommonErrorCode)
                                .stuckJobsCount(stuckJobsCount)
                                .jobsRequiringManualIntervention(jobsRequiringManualIntervention)
                                .averageRetryAttempts(Math.round(averageRetryAttempts * 100.0) / 100.0)
                                .totalRetriedArticles(totalRetriedArticles)
                                .retrySuccessRatePercent(Math.round(retrySuccessRatePercent * 100.0) / 100.0)
                                .timestamp(Instant.now().toString())
                                .periodLabel(periodLabel)
                                .build();

                log.info("[NEWS-SCHEDULING-ADMIN] Metrics retrieved - totalJobs={}, successRate={}%, period={}",
                                totalJobs, successRatePercent, timePeriod);

                return ResponseEntity.ok(ApiResponseDto.success("Metrics retrieved successfully", metrics));
        }

        /**
         * Bulk retry all eligible failed articles
         *
         * POST /api/v1/admin/scheduler/publish/failed-articles/retry-all?maxRetries=3
         *
         * Re-queues all failed articles that:
         * - Have shouldRetry=true
         * - Are under retry limit
         * - Can recover from transient errors (TIMEOUT, NETWORK_ERROR)
         *
         * Creates new job and enqueues attempts for processing
         *
         * @param maxRetries Maximum retries allowed (default: 3)
         * @return Count of articles queued for retry
         */
        @PostMapping("/publish/failed-articles/retry-all")
        public ResponseEntity<ApiResponseDto<NewsSchedulingBulkRetryResponseDto>> retryAllFailedArticles(
                        @RequestParam(value = "maxRetries", required = false, defaultValue = "3") int maxRetries) {

                log.info("[NEWS-SCHEDULING-ADMIN] Bulk retry initiated - maxRetries={}", maxRetries);

                // Get all eligible failed attempts
                List<NewsSchedulingAttempt> failedAttempts = attemptRepository
                                .findFailedArticlesUnderRetryLimit(maxRetries);

                if (failedAttempts.isEmpty()) {
                        log.info("[NEWS-SCHEDULING-ADMIN] No failed articles to retry");
                        return ResponseEntity.ok(ApiResponseDto.success("No failed articles eligible for retry",
                                        NewsSchedulingBulkRetryResponseDto.builder()
                                                        .articlesToRetryCount(0)
                                                        .jobsCreatedCount(0)
                                                        .timestamp(Instant.now().toString())
                                                        .message("No failed articles eligible for retry")
                                                        .build()));
                }

                // Create new job for retry
                NewsSchedulingJob retryJob = newsSchedulingService.startJob();
                retryJob.setTriggeredBy("ADMIN_BULK_RETRY");
                retryJob.setTriggerReason("Manual bulk retry of failed articles");
                retryJob.setPriority("HIGH");
                var savedJob = jobRepository.save(retryJob);

                // Update attempts to reference new job and increment retry count
                for (NewsSchedulingAttempt attempt : failedAttempts) {
                        attempt.setNewsSchedulingJob(savedJob);
                        attempt.setRetryCount((attempt.getRetryCount() != null ? attempt.getRetryCount() : 0) + 1);
                        attempt.setAttemptNumber(attempt.getAttemptNumber() + 1);
                        attempt.setStatus("PENDING");
                        attemptRepository.save(attempt);
                }

                log.info("[NEWS-SCHEDULING-ADMIN] Bulk retry completed - jobId={}, articlesRetried={}",
                                savedJob.getJobId(), failedAttempts.size());

                return ResponseEntity.ok(ApiResponseDto.success(
                                String.format("Queued %d articles for retry", failedAttempts.size()),
                                NewsSchedulingBulkRetryResponseDto.builder()
                                                .articlesToRetryCount(failedAttempts.size())
                                                .jobsCreatedCount(1)
                                                .newJobId(savedJob.getJobId().toString())
                                                .timestamp(Instant.now().toString())
                                                .message(String.format("Queued %d articles for retry in job %s",
                                                                failedAttempts.size(), savedJob.getJobId()))
                                                .build()));
        }

        /**
         * Cancel a running job
         *
         * POST /api/v1/admin/scheduler/jobs/{jobId}/cancel
         *
         * Cancels a job in RUNNING state, preventing further article processing.
         * Used for emergency stops during high load or system issues.
         *
         * @param jobId Job ID to cancel
         * @return Updated job details
         */
        @PostMapping("/jobs/{jobId}/cancel")
        public ResponseEntity<ApiResponseDto<NewsSchedulingJobResponseDto>> cancelJob(@PathVariable UUID jobId) {

                log.info("[NEWS-SCHEDULING-ADMIN] Cancel job requested - jobId={}", jobId);

                var job = jobRepository.findById(jobId)
                                .orElseThrow(() -> {
                                        log.warn("[NEWS-SCHEDULING-ADMIN] Job not found for cancellation - jobId={}",
                                                        jobId);
                                        return new IllegalArgumentException("Job not found: " + jobId);
                                });

                // Only cancel if currently running
                if (!"RUNNING".equals(job.getStatus())) {
                        log.warn("[NEWS-SCHEDULING-ADMIN] Cannot cancel non-running job - jobId={}, status={}",
                                        jobId, job.getStatus());
                        throw new IllegalStateException("Job cannot be cancelled - current status: " + job.getStatus());
                }

                job.setStatus("CANCELLED");
                job.setCompletedAt(Instant.now());
                job.setDurationMs(job.getCompletedAt().toEpochMilli() - job.getStartedAt().toEpochMilli());
                job.setUpdatedBy("ADMIN_CANCEL");
                var cancelledJob = jobRepository.save(job);

                log.info("[NEWS-SCHEDULING-ADMIN] Job cancelled successfully - jobId={}", jobId);

                return ResponseEntity.ok(
                                ApiResponseDto.success("Job cancelled successfully", mapJobToResponse(cancelledJob)));
        }

        /**
         * Delete/cleanup job record
         *
         * DELETE /api/v1/admin/scheduler/jobs/{jobId}
         *
         * Permanently deletes a job record (soft delete via audit trail).
         * Used for compliance (GDPR) and database cleanup.
         * Only jobs in terminal state (SUCCESS, FAILED, CANCELLED) can be deleted.
         *
         * @param jobId Job ID to delete
         * @return No content
         */
        @DeleteMapping("/jobs/{jobId}")
        public ResponseEntity<ApiResponseDto<Void>> deleteJob(@PathVariable UUID jobId) {

                log.info("[NEWS-SCHEDULING-ADMIN] Delete job requested - jobId={}", jobId);

                var job = jobRepository.findById(jobId)
                                .orElseThrow(() -> {
                                        log.warn("[NEWS-SCHEDULING-ADMIN] Job not found for deletion - jobId={}",
                                                        jobId);
                                        return new IllegalArgumentException("Job not found: " + jobId);
                                });

                // Only allow deletion of terminal state jobs
                String status = job.getStatus();
                if (!(("SUCCESS".equals(status) || "FAILED".equals(status) || "CANCELLED".equals(status)))) {
                        log.warn("[NEWS-SCHEDULING-ADMIN] Cannot delete non-terminal job - jobId={}, status={}",
                                        jobId, status);
                        throw new IllegalStateException("Job cannot be deleted - current status: " + status +
                                        ". Only SUCCESS, FAILED, or CANCELLED jobs can be deleted.");
                }

                // Delete associated attempts first (cascade delete)
                List<NewsSchedulingAttempt> attempts = attemptRepository.findByNewsSchedulingJob_JobId(jobId);
                attemptRepository.deleteAll(attempts);

                // Delete job
                jobRepository.deleteById(jobId);

                log.info("[NEWS-SCHEDULING-ADMIN] Job deleted successfully - jobId={}, attemptsCleaned={}",
                                jobId, attempts.size());

                return ResponseEntity.ok(ApiResponseDto.success(
                                String.format("Job deleted successfully (%d attempts cleaned)", attempts.size()),
                                null));
        }

        // ========================================
        // Helper Methods
        // ========================================

        /**
         * Map JobSchedulingJob entity to response DTO
         */
        private NewsSchedulingJobResponseDto mapJobToResponse(NewsSchedulingJob job) {
                double successRate = 0;
                if (job.getTotalArticles() != null && job.getTotalArticles() > 0) {
                        successRate = (job.getPublishedCount() != null ? job.getPublishedCount() : 0) * 100.0
                                        / job.getTotalArticles();
                }

                return NewsSchedulingJobResponseDto.builder()
                                .jobId(job.getJobId())
                                .correlationId(job.getCorrelationId())
                                .partitionKey(job.getPartitionKey())
                                .triggeredBy(job.getTriggeredBy())
                                .triggerReason(job.getTriggerReason())
                                .executionNode(job.getExecutionNode())
                                .status(job.getStatus())
                                .priority(job.getPriority())
                                .queueName(job.getQueueName())
                                .tags(job.getTags())
                                .totalArticles(job.getTotalArticles())
                                .publishedCount(job.getPublishedCount())
                                .failedCount(job.getFailedCount())
                                .skippedCount(job.getSkippedCount())
                                .startedAt(job.getStartedAt())
                                .completedAt(job.getCompletedAt())
                                .durationMs(job.getDurationMs())
                                .maxDurationMs(job.getMaxDurationMs())
                                .backoffStrategy(job.getBackoffStrategy())
                                .errorMessage(job.getErrorMessage())
                                .executionPlanVersion(job.getExecutionPlanVersion())
                                .healthCheckTimestamp(job.getHealthCheckTimestamp())
                                .version(job.getVersion() != null ? job.getVersion() : 0L)
                                .createdAt(job.getCreatedAt())
                                .createdBy(job.getCreatedBy())
                                .updatedAt(job.getUpdatedAt())
                                .updatedBy(job.getUpdatedBy())
                                .successRatePercent(Math.round(successRate * 100.0) / 100.0)
                                .build();
        }

        /**
         * Map NewsSchedulingAttempt entity to response DTO
         */
        private NewsSchedulingAttemptResponseDto mapAttemptToResponse(NewsSchedulingAttempt attempt) {
                return NewsSchedulingAttemptResponseDto.builder()
                                .attemptId(attempt.getAttemptId())
                                .jobId(attempt.getNewsSchedulingJob().getJobId())
                                .articleId(attempt.getArticleId())
                                .status(attempt.getStatus())
                                .attemptNumber(attempt.getAttemptNumber())
                                .error(attempt.getError())
                                .errorCode(attempt.getErrorCode())
                                .stackTrace(attempt.getStackTrace())
                                .shouldRetry(attempt.getShouldRetry())
                                .retryCount(attempt.getRetryCount())
                                .backoffDelayMs(attempt.getBackoffDelayMs())
                                .nextRetryAt(attempt.getNextRetryAt())
                                .circuitBreakerTriggered(attempt.getCircuitBreakerTriggered())
                                .startedAt(attempt.getStartedAt())
                                .completedAt(attempt.getCompletedAt())
                                .durationMs(attempt.getDurationMs())
                                .correlationIdAttempt(attempt.getCorrelationIdAttempt())
                                .idempotencyKey(attempt.getIdempotencyKey())
                                .createdAt(attempt.getCreatedAt())
                                .createdBy(attempt.getCreatedBy())
                                .updatedAt(attempt.getUpdatedAt())
                                .timestamp(attempt.getTimestamp())
                                .build();
        }

        /**
         * Calculate start time based on period parameter
         */
        private Instant calculateStartTime(String timePeriod) {
                return switch (timePeriod) {
                        case "1h" -> Instant.now().minus(1, ChronoUnit.HOURS);
                        case "6h" -> Instant.now().minus(6, ChronoUnit.HOURS);
                        case "7d" -> Instant.now().minus(7, ChronoUnit.DAYS);
                        default -> Instant.now().minus(24, ChronoUnit.HOURS); // Default: 24h
                };
        }

        /**
         * Format human-readable period label
         */
        private String formatPeriodLabel(String timePeriod) {
                return switch (timePeriod) {
                        case "1h" -> "Last 1 hour";
                        case "6h" -> "Last 6 hours";
                        case "7d" -> "Last 7 days";
                        default -> "Last 24 hours";
                };
        }
}
