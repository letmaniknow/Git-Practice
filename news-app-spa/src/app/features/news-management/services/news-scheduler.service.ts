/**
 * NEWS SCHEDULER SERVICE
 * 
 * Centralized business logic for scheduler operations.
 * Handles all API communication with proper error handling and typing.
 * 
 * Responsibilities:
 * - Trigger scheduler jobs
 * - Fetch job status and metrics
 * - Manage retry operations
 * - Handle error responses
 */

import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';

import { NEWS_SCHEDULER_API_ENDPOINTS } from '../constants/news-scheduler-api.constant';
import {
  NewsSchedulerJob,
  NewsSchedulerAttempt,
  NewsSchedulerMetrics,
  NewsSchedulerJobResponseDto,
  NewsSchedulerBulkRetryResponseDto,
  SchedulerJobTriggerRequest,
  SchedulerBulkRetryRequest,
  SchedulerJobFilter,
  SchedulerFailedArticleFilter,
} from '../models/news-scheduler.model';
import { ApiResponse } from '../models/api-response.model';
import {
  HttpErrorCategorizationService,
  CategorizedError,
} from '../../../core/services/http-error-categorization.service';

/**
 * NewsSchedulerService
 * 
 * Provides type-safe access to all scheduler endpoints.
 * All methods return strongly-typed Observables.
 * 
 * Usage Example:
 * ```typescript
 * this.schedulerService.triggerJob({ priority: 'HIGH' })
 *   .pipe(
 *     tap(job => console.log('Job triggered:', job)),
 *     catchError(err => this.handleError(err))
 *   )
 *   .subscribe();
 * ```
 */
@Injectable({
  providedIn: 'root',
})
export class NewsSchedulerService {
  constructor(
    private http: HttpClient,
    private errorService: HttpErrorCategorizationService
  ) {}

  // ==================== JOB TRIGGER ====================

  /**
   * Trigger a new scheduler job
   * 
   * POST /api/v1/admin/scheduler/publish/trigger
   * 
   * @param request - Job configuration (priority, tags, queue, retries, backoff strategy)
   * @returns Observable<NewsSchedulingJobResponseDto> - Created job with jobId
   * @throws 400 - Invalid request
   * @throws 401 - Unauthorized
   * @throws 403 - Forbidden
   * @throws 429 - Rate limited
   * @throws 500 - Server error
   * 
   * Example:
   * ```typescript
   * const request: SchedulerJobTriggerRequest = {
   *   priority: 'HIGH',
   *   tags: ['urgent'],
   *   maxRetries: 5
   * };
   * this.schedulerService.triggerJob(request).subscribe(
   *   job => console.log('Job ID:', job.jobId)
   * );
   * ```
   */
  triggerJob(
    request: SchedulerJobTriggerRequest
  ): Observable<ApiResponse<NewsSchedulerJobResponseDto>> {
    return this.http
      .post<ApiResponse<NewsSchedulerJobResponseDto>>(
        NEWS_SCHEDULER_API_ENDPOINTS.scheduler.trigger,
        request
      )
      .pipe(
        tap((response) =>
          console.log('[NewsScheduler] Job triggered:', response.data?.jobId)
        ),
        catchError((error) => this.handleError(error))
      );
  }

  // ==================== JOB QUERIES ====================

  /**
   * Get single job with full details and attempts
   * 
   * GET /api/v1/admin/scheduler/publish/jobs/{jobId}
   * 
   * @param jobId - UUID of the job
   * @returns Observable<NewsSchedulingJobResponseDto> - Job with attempts array
   * @throws 404 - Job not found
   * @throws 401 - Unauthorized
   * 
   * Example:
   * ```typescript
   * this.schedulerService.getJobDetails('550e8400-e29b-41d4-a716-446655440000')
   *   .subscribe(job => {
   *     console.log('Job status:', job.status);
   *     console.log('Attempts:', job.attempts);
   *   });
   * ```
   */
  getJobDetails(jobId: string): Observable<ApiResponse<NewsSchedulerJobResponseDto>> {
    return this.http
      .get<ApiResponse<NewsSchedulerJobResponseDto>>(
        NEWS_SCHEDULER_API_ENDPOINTS.scheduler.jobs.getById(jobId)
      )
      .pipe(
        tap((response) =>
          console.log('[NewsScheduler] Job details fetched:', jobId)
        ),
        catchError((error) => this.handleError(error))
      );
  }

  /**
   * Get paginated list of all scheduler jobs
   * 
   * GET /api/v1/admin/scheduler/publish/jobs
   * 
   * @param filter - Pagination and filtering options
   * @returns Observable<Page<NewsSchedulingJobResponseDto>> - Paginated jobs
   * @throws 400 - Invalid parameters
   * @throws 401 - Unauthorized
   * 
   * Example:
   * ```typescript
   * const filter: SchedulerJobFilter = {
   *   status: 'FAILED',
   *   page: 0,
   *   size: 20,
   *   sort: 'startedAt,desc'
   * };
   * this.schedulerService.getJobsList(filter).subscribe(
   *   page => console.log('Total jobs:', page.totalElements)
   * );
   * ```
   */
  getJobsList(
    filter?: SchedulerJobFilter
  ): Observable<ApiResponse<any>> {
    let params = new HttpParams();

    if (filter) {
      if (filter.page !== undefined) {
        params = params.set('page', filter.page.toString());
      }
      if (filter.size !== undefined) {
        params = params.set('size', filter.size.toString());
      }
      if (filter.sort) {
        params = params.set('sort', filter.sort);
      }
      if (filter.status) {
        params = params.set('status', filter.status);
      }
      if (filter.priority) {
        params = params.set('priority', filter.priority);
      }
      if (filter.dateFrom) {
        params = params.set('dateFrom', filter.dateFrom.toISOString());
      }
      if (filter.dateTo) {
        params = params.set('dateTo', filter.dateTo.toISOString());
      }
    }

    return this.http
      .get<ApiResponse<any>>(
        NEWS_SCHEDULER_API_ENDPOINTS.scheduler.jobs.list,
        { params }
      )
      .pipe(
        tap((response) =>
          console.log('[NewsScheduler] Jobs list fetched:', response)
        ),
        catchError((error) => this.handleError(error))
      );
  }

  // ==================== JOB CONTROL ====================

  /**
   * Cancel a running scheduler job
   * 
   * POST /api/v1/admin/scheduler/jobs/{jobId}/cancel
   * 
   * @param jobId - UUID of the job to cancel
   * @returns Observable<NewsSchedulingJobResponseDto> - Job with status=CANCELLED
   * @throws 404 - Job not found
   * @throws 409 - Job not in RUNNING state
   * @throws 401 - Unauthorized
   * 
   * Example:
   * ```typescript
   * this.schedulerService.cancelJob(jobId).subscribe(
   *   job => console.log('Job cancelled at:', job.completedAt)
   * );
   * ```
   */
  cancelJob(jobId: string): Observable<ApiResponse<NewsSchedulerJobResponseDto>> {
    return this.http
      .post<ApiResponse<NewsSchedulerJobResponseDto>>(
        NEWS_SCHEDULER_API_ENDPOINTS.scheduler.jobs.cancel(jobId),
        {}
      )
      .pipe(
        tap((response) =>
          console.log('[NewsScheduler] Job cancelled:', jobId)
        ),
        catchError((error) => this.handleError(error))
      );
  }

  /**
   * Delete a job permanently (GDPR cleanup)
   * 
   * DELETE /api/v1/admin/scheduler/jobs/{jobId}
   * 
   * Only allowed for jobs in terminal state (SUCCESS, FAILED, CANCELLED).
   * 
   * @param jobId - UUID of the job to delete
   * @returns Observable<void> - Empty response on success (204 NO_CONTENT)
   * @throws 404 - Job not found
   * @throws 409 - Job not in terminal state
   * @throws 401 - Unauthorized
   * 
   * Example:
   * ```typescript
   * this.schedulerService.deleteJob(jobId).subscribe(
   *   () => console.log('Job deleted')
   * );
   * ```
   */
  deleteJob(jobId: string): Observable<ApiResponse<void>> {
    return this.http
      .delete<ApiResponse<void>>(
        NEWS_SCHEDULER_API_ENDPOINTS.scheduler.jobs.delete(jobId)
      )
      .pipe(
        tap(() => console.log('[NewsScheduler] Job deleted:', jobId)),
        catchError((error) => this.handleError(error))
      );
  }

  // ==================== FAILED ARTICLES ====================

  /**
   * Get paginated list of failed articles
   * 
   * GET /api/v1/admin/scheduler/publish/failed-articles
   * 
   * @param filter - Pagination and filtering options
   * @returns Observable<Page<NewsSchedulingAttemptResponseDto>> - Paginated failed articles
   * @throws 400 - Invalid parameters
   * @throws 401 - Unauthorized
   * 
   * Example:
   * ```typescript
   * const filter: SchedulerFailedArticleFilter = {
   *   jobId: '550e8400-e29b-41d4-a716-446655440000',
   *   shouldRetry: true,
   *   page: 0,
   *   size: 10
   * };
   * this.schedulerService.getFailedArticles(filter).subscribe(
   *   page => console.log('Failed articles:', page.content)
   * );
   * ```
   */
  getFailedArticles(
    filter?: SchedulerFailedArticleFilter
  ): Observable<ApiResponse<any>> {
    let params = new HttpParams();

    if (filter) {
      if (filter.jobId) {
        params = params.set('jobId', filter.jobId);
      }
      if (filter.shouldRetry !== undefined) {
        params = params.set('shouldRetry', filter.shouldRetry.toString());
      }
      if (filter.errorCode) {
        params = params.set('errorCode', filter.errorCode);
      }
      if (filter.page !== undefined) {
        params = params.set('page', filter.page.toString());
      }
      if (filter.size !== undefined) {
        params = params.set('size', filter.size.toString());
      }
      if (filter.sort) {
        params = params.set('sort', filter.sort);
      }
    }

    return this.http
      .get<ApiResponse<any>>(
        NEWS_SCHEDULER_API_ENDPOINTS.scheduler.failedArticles.list,
        { params }
      )
      .pipe(
        tap((response) =>
          console.log('[NewsScheduler] Failed articles fetched')
        ),
        catchError((error) => this.handleError(error))
      );
  }

  /**
   * Retry all failed articles from a job
   * 
   * POST /api/v1/admin/scheduler/publish/failed-articles/retry-all
   * 
   * @param request - Retry configuration (jobId, maxRetries, includeSkipped)
   * @returns Observable<NewsSchedulingBulkRetryResponseDto> - Retry statistics
   * @throws 404 - Job not found
   * @throws 400 - Invalid request
   * @throws 401 - Unauthorized
   * 
   * Example:
   * ```typescript
   * const request: SchedulerBulkRetryRequest = {
   *   jobId: '550e8400-e29b-41d4-a716-446655440000',
   *   maxRetries: 5,
   *   includeSkipped: false
   * };
   * this.schedulerService.retryFailedArticles(request).subscribe(
   *   result => console.log('Retried:', result.retriedArticleCount)
   * );
   * ```
   */
  retryFailedArticles(
    request: SchedulerBulkRetryRequest
  ): Observable<ApiResponse<NewsSchedulerBulkRetryResponseDto>> {
    return this.http
      .post<ApiResponse<NewsSchedulerBulkRetryResponseDto>>(
        NEWS_SCHEDULER_API_ENDPOINTS.scheduler.failedArticles.retryAll,
        request
      )
      .pipe(
        tap((response) =>
          console.log(
            '[NewsScheduler] Failed articles retried:',
            response.data?.retriedArticleCount
          )
        ),
        catchError((error) => this.handleError(error))
      );
  }

  // ==================== METRICS ====================

  /**
   * Get performance metrics for scheduler
   * 
   * GET /api/v1/admin/scheduler/publish/metrics
   * 
   * @param timePeriod - '24h', '7d', or '30d' (defaults to '24h')
   * @param status - Optional filter by job status
   * @returns Observable<NewsSchedulingMetricsResponseDto> - Aggregated metrics
   * @throws 400 - Invalid parameters
   * @throws 401 - Unauthorized
   * 
   * Example:
   * ```typescript
   * this.schedulerService.getMetrics('24h', 'SUCCESS').subscribe(
   *   metrics => {
   *     console.log('Success rate:', metrics.jobSuccessRate + '%');
   *     console.log('Avg duration:', metrics.averageJobDurationMs + 'ms');
   *   }
   * );
   * ```
   */
  getMetrics(
    timePeriod: '24h' | '7d' | '30d' = '24h',
    status?: string
  ): Observable<ApiResponse<NewsSchedulerMetrics>> {
    let params = new HttpParams();
    params = params.set('timePeriod', timePeriod);

    if (status) {
      params = params.set('status', status);
    }

    return this.http
      .get<ApiResponse<NewsSchedulerMetrics>>(
        NEWS_SCHEDULER_API_ENDPOINTS.scheduler.metrics,
        { params }
      )
      .pipe(
        tap((response) =>
          console.log(
            '[NewsScheduler] Metrics fetched:',
            response.data?.jobSuccessRate + '%'
          )
        ),
        catchError((error) => this.handleError(error))
      );
  }

  // ==================== ERROR HANDLING ====================

  /**
   * Centralized error handling
   * 
   * Categorizes HTTP errors and provides user-friendly messages.
   * Logs errors for debugging.
   * 
   * @param error - HTTP error response
   * @returns Observable - Throws categorized error
   */
  private handleError(error: any): Observable<never> {
    const categorizedError: CategorizedError = this.errorService.categorizeError(error);

    console.error('[NewsScheduler] Error:', {
      status: error.status,
      message: error.message,
      category: categorizedError.category,
      details: error.error,
    });

    return throwError(() => categorizedError);
  }
}
