/**
 * NEWS SCHEDULER API CONSTANTS
 * 
 * Centralized API endpoints for scheduler operations.
 * Single source of truth for all scheduler URLs.
 * 
 * Organized by operational domain for easy discovery.
 */

import { environment } from '@environments/environment';

const BASE_URL = environment.apiBaseUrl;

/**
 * NEWS_SCHEDULER_API_ENDPOINTS
 * 
 * All scheduler-related API endpoints organized by functional area.
 * Use these in services to avoid hardcoding URLs throughout the app.
 * 
 * Example usage:
 *   this.http.post(NEWS_SCHEDULER_API_ENDPOINTS.scheduler.trigger, {...})
 *   this.http.get(NEWS_SCHEDULER_API_ENDPOINTS.scheduler.jobs.list)
 */
export const NEWS_SCHEDULER_API_ENDPOINTS = {
  // ==================== JOB TRIGGER ENDPOINTS ====================
  scheduler: {
    /**
     * POST /api/v1/admin/scheduler/publish/trigger
     * Manually trigger a scheduler job
     * 
     * Request: SchedulerJobTriggerRequest
     * Response: NewsSchedulingJobResponseDto
     * Status: 201 CREATED
     */
    trigger: `${BASE_URL}/api/v1/admin/scheduler/publish/trigger`,

    // ==================== JOB QUERY ENDPOINTS ====================
    jobs: {
      /**
       * GET /api/v1/admin/scheduler/publish/jobs/{jobId}
       * Get single job with full details and attempts
       * 
       * Response: NewsSchedulingJobResponseDto (with attempts array)
       * Status: 200 OK | 404 NOT_FOUND
       */
      getById: (jobId: string) =>
        `${BASE_URL}/api/v1/admin/scheduler/publish/jobs/${jobId}`,

      /**
       * GET /api/v1/admin/scheduler/publish/jobs?page=0&size=20&sort=startedAt,desc
       * Paginated list of all jobs
       * 
       * Query Params:
       *   - page: 0-indexed page number
       *   - size: items per page (max 100)
       *   - sort: field,direction (e.g., startedAt,desc)
       *   - status: filter by job status (optional)
       * 
       * Response: Page<NewsSchedulingJobResponseDto>
       * Status: 200 OK
       */
      list: `${BASE_URL}/api/v1/admin/scheduler/publish/jobs`,

      /**
       * POST /api/v1/admin/scheduler/jobs/{jobId}/cancel
       * Cancel a running job (only if status=RUNNING)
       * 
       * Response: NewsSchedulingJobResponseDto (updated status=CANCELLED)
       * Status: 200 OK | 404 NOT_FOUND | 409 CONFLICT (if not running)
       */
      cancel: (jobId: string) =>
        `${BASE_URL}/api/v1/admin/scheduler/jobs/${jobId}/cancel`,

      /**
       * DELETE /api/v1/admin/scheduler/jobs/{jobId}
       * Delete a job permanently (GDPR cleanup)
       * Only allowed for jobs in terminal state (SUCCESS, FAILED, CANCELLED)
       * 
       * Response: empty (204 NO_CONTENT)
       * Status: 204 NO_CONTENT | 404 NOT_FOUND | 409 CONFLICT (if not terminal)
       */
      delete: (jobId: string) =>
        `${BASE_URL}/api/v1/admin/scheduler/jobs/${jobId}`,
    },

    // ==================== FAILED ARTICLES ENDPOINTS ====================
    failedArticles: {
      /**
       * GET /api/v1/admin/scheduler/publish/failed-articles?page=0&size=20
       * Get paginated list of failed articles across all jobs
       * 
       * Query Params:
       *   - page: 0-indexed page number
       *   - size: items per page
       *   - jobId: filter by specific job (optional)
       *   - shouldRetry: filter to only retryable failures (optional)
       *   - errorCode: filter by error type (optional)
       * 
       * Response: Page<NewsSchedulingAttemptResponseDto>
       * Status: 200 OK
       */
      list: `${BASE_URL}/api/v1/admin/scheduler/publish/failed-articles`,

      /**
       * POST /api/v1/admin/scheduler/publish/failed-articles/retry-all
       * Retry all failed articles (or filtered subset)
       * 
       * Request: SchedulerBulkRetryRequest { jobId, maxRetries?, includeSkipped? }
       * Response: NewsSchedulingBulkRetryResponseDto
       *   { jobId, retriedArticleCount, failedArticleCount, message, timestamp }
       * Status: 200 OK
       */
      retryAll: `${BASE_URL}/api/v1/admin/scheduler/publish/failed-articles/retry-all`,
    },

    // ==================== METRICS ENDPOINTS ====================
    /**
     * GET /api/v1/admin/scheduler/publish/metrics?timePeriod=24h
     * Get aggregated performance metrics
     * 
     * Query Params:
     *   - timePeriod: '24h', '7d', '30d' (defaults to 24h)
     *   - status: filter jobs by status (optional)
     * 
     * Response: NewsSchedulingMetricsResponseDto
     *   {
     *     timePeriod, periodLabel, startTime, endTime,
     *     totalJobs, successfulJobs, failedJobs, partialSuccessJobs,
     *     totalArticlesProcessed, successfullyPublished, failedArticles,
     *     averageJobDurationMs, jobSuccessRate, articleSuccessRate,
     *     errorsByCode: { TIMEOUT: 5, NETWORK_ERROR: 2, ... }
     *   }
     * Status: 200 OK
     */
    metrics: `${BASE_URL}/api/v1/admin/scheduler/publish/metrics`,
  },
};

// ==================== ENDPOINT CONSTANTS FOR EASY REFERENCE ====================

/**
 * Trigger a new scheduler job
 * POST {trigger}
 */
export const SCHEDULER_ENDPOINT_TRIGGER = NEWS_SCHEDULER_API_ENDPOINTS.scheduler.trigger;

/**
 * Get all jobs (paginated)
 * GET {list}
 */
export const SCHEDULER_ENDPOINT_JOBS_LIST =
  NEWS_SCHEDULER_API_ENDPOINTS.scheduler.jobs.list;

/**
 * Get single job details
 * GET {getById(jobId)}
 */
export const SCHEDULER_ENDPOINT_JOB_DETAILS = (jobId: string) =>
  NEWS_SCHEDULER_API_ENDPOINTS.scheduler.jobs.getById(jobId);

/**
 * Cancel running job
 * POST {cancel(jobId)}
 */
export const SCHEDULER_ENDPOINT_CANCEL_JOB = (jobId: string) =>
  NEWS_SCHEDULER_API_ENDPOINTS.scheduler.jobs.cancel(jobId);

/**
 * Delete job (permanent)
 * DELETE {delete(jobId)}
 */
export const SCHEDULER_ENDPOINT_DELETE_JOB = (jobId: string) =>
  NEWS_SCHEDULER_API_ENDPOINTS.scheduler.jobs.delete(jobId);

/**
 * Get failed articles (paginated)
 * GET {list}
 */
export const SCHEDULER_ENDPOINT_FAILED_ARTICLES =
  NEWS_SCHEDULER_API_ENDPOINTS.scheduler.failedArticles.list;

/**
 * Retry all failed articles
 * POST {retryAll}
 */
export const SCHEDULER_ENDPOINT_RETRY_FAILED =
  NEWS_SCHEDULER_API_ENDPOINTS.scheduler.failedArticles.retryAll;

/**
 * Get performance metrics
 * GET {metrics}
 */
export const SCHEDULER_ENDPOINT_METRICS =
  NEWS_SCHEDULER_API_ENDPOINTS.scheduler.metrics;
