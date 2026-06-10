/**
 * NEWS SCHEDULER NGRX EFFECTS
 * 
 * Handles side effects (async operations, API calls).
 * Effects listen to actions, perform side effects, and dispatch new actions.
 */

import { Injectable, inject } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { of } from 'rxjs';
import {
  catchError,
  map,
  mergeMap,
  switchMap,
  tap,
  withLatestFrom,
  debounceTime,
  distinctUntilChanged,
} from 'rxjs/operators';
import { Store } from '@ngrx/store';

import * as NewsSchedulerActions from './news-scheduler.actions';
import { NewsSchedulerService } from '../services/news-scheduler.service';
import {
  selectFilters,
  selectCurrentPage,
  selectPageSize,
  selectSort,
  selectMetricsTimePeriod,
} from './news-scheduler.selectors';

/**
 * NewsSchedulerEffects
 * 
 * Listens to scheduler actions and performs side effects.
 * All API calls happen here, keeping components pure.
 */
@Injectable()
export class NewsSchedulerEffects {
  private actions$ = inject(Actions);
  private schedulerService = inject(NewsSchedulerService);
  private store = inject(Store);

  /**
   * Maps snake_case API response to camelCase
   * Converts job_id → jobId, correlation_id → correlationId, etc.
   */
  private mapApiJobToModel(apiJob: any): any {
    if (!apiJob) return null;
    
    return {
      jobId: apiJob.job_id || apiJob.jobId,
      correlationId: apiJob.correlation_id || apiJob.correlationId,
      status: apiJob.status,
      priority: apiJob.priority,
      startedAt: apiJob.started_at || apiJob.startedAt,
      completedAt: apiJob.completed_at || apiJob.completedAt,
      durationMs: apiJob.duration_ms || apiJob.durationMs,
      totalArticles: apiJob.total_articles || apiJob.totalArticles,
      publishedCount: apiJob.published_count || apiJob.publishedCount,
      failedCount: apiJob.failed_count || apiJob.failedCount,
      skippedCount: apiJob.skipped_count || apiJob.skippedCount,
      errorMessage: apiJob.error_message || apiJob.errorMessage,
      errorCode: apiJob.error_code || apiJob.errorCode,
      backoffStrategy: apiJob.backoff_strategy || apiJob.backoffStrategy,
      retryAttempt: apiJob.retry_attempt || apiJob.retryAttempt,
      maxRetries: apiJob.max_retries || apiJob.maxRetries,
      queueName: apiJob.queue_name || apiJob.queueName,
      partitionKey: apiJob.partition_key || apiJob.partitionKey,
      source: apiJob.source,
      idempotencyKey: apiJob.idempotency_key || apiJob.idempotencyKey,
      executionLogs: apiJob.execution_logs || apiJob.executionLogs,
      executionMetrics: apiJob.execution_metrics || apiJob.executionMetrics,
      createdAt: apiJob.created_at || apiJob.createdAt,
      createdBy: apiJob.created_by || apiJob.createdBy,
      updatedAt: apiJob.updated_at || apiJob.updatedAt,
      updatedBy: apiJob.updated_by || apiJob.updatedBy,
      deletedAt: apiJob.deleted_at || apiJob.deletedAt,
      deletedBy: apiJob.deleted_by || apiJob.deletedBy,
    };
  }

  /**
   * Maps metrics API response snake_case to camelCase
   * Converts total_jobs → totalJobs, success_rate_percent → successRatePercent, etc.
   * Also translates API field names to component model expectations.
   */
  private mapApiMetricsToModel(apiMetrics: any): any {
    if (!apiMetrics) return null;
    
    // Extract values from API response
    const totalArticlesPublished = apiMetrics.total_articles_published || apiMetrics.totalArticlesPublished || 0;
    const totalArticlesFailed = apiMetrics.total_articles_failed || apiMetrics.totalArticlesFailed || 0;
    const totalArticlesSkipped = apiMetrics.total_articles_skipped || apiMetrics.totalArticlesSkipped || 0;
    const totalArticlesProcessed = totalArticlesPublished + totalArticlesFailed + totalArticlesSkipped;
    
    const totalJobs = apiMetrics.total_jobs || apiMetrics.totalJobs || 0;
    const successfulJobs = apiMetrics.successful_jobs || apiMetrics.successfulJobs || 0;
    const failedJobs = apiMetrics.failed_jobs || apiMetrics.failedJobs || 0;
    const partialSuccessJobs = apiMetrics.partial_success_jobs || apiMetrics.partialSuccessJobs || 0;
    
    const successRatePercent = apiMetrics.success_rate_percent || apiMetrics.successRatePercent || 0;
    const articlesSuccessRatePercent = apiMetrics.articles_success_rate_percent || apiMetrics.articlesSuccessRatePercent || 0;
    const retrySuccessRatePercent = apiMetrics.retry_success_rate_percent || apiMetrics.retrySuccessRatePercent || 0;
    const averageDurationMs = apiMetrics.average_duration_ms || apiMetrics.averageDurationMs || 0;
    
    const totalRetriedArticles = apiMetrics.total_retried_articles || apiMetrics.totalRetriedArticles || 0;
    const averageRetryAttempts = apiMetrics.average_retry_attempts || apiMetrics.averageRetryAttempts || 0;
    
    return {
      // Job Statistics - API field names
      totalJobs,
      successfulJobs,
      failedJobs,
      partialSuccessJobs,
      runningJobs: apiMetrics.running_jobs || apiMetrics.runningJobs || 0,
      
      // Article Statistics - API field names
      totalArticlesPublished,
      totalArticlesFailed,
      totalArticlesSkipped,
      
      // Computed fields for component expectations
      totalArticlesProcessed,
      successfullyPublished: totalArticlesPublished,
      failedArticles: totalArticlesFailed,
      skippedArticles: totalArticlesSkipped,
      
      // Success Rates
      successRatePercent,
      jobSuccessRate: successRatePercent,
      
      failureRatePercent: apiMetrics.failure_rate_percent || apiMetrics.failureRatePercent || 0,
      partialSuccessRatePercent: apiMetrics.partial_success_rate_percent || apiMetrics.partialSuccessRatePercent || 0,
      
      articlesSuccessRatePercent,
      articleSuccessRate: articlesSuccessRatePercent,
      
      // Performance/Duration - API uses averageDurationMs, component expects averageJobDurationMs
      averageDurationMs,
      averageJobDurationMs: averageDurationMs,
      minJobDurationMs: 0,  // Not provided by API
      maxJobDurationMs: 0,  // Not provided by API
      
      // Error Handling
      mostCommonErrorCode: apiMetrics.most_common_error_code || apiMetrics.mostCommonErrorCode || 'N/A',
      stuckJobsCount: apiMetrics.stuck_jobs_count || apiMetrics.stuckJobsCount || 0,
      jobsRequiringManualIntervention: apiMetrics.jobs_requiring_manual_intervention || apiMetrics.jobsRequiringManualIntervention || 0,
      
      // Retry Insights
      averageRetryAttempts,
      totalRetriedArticles,
      retrySuccessRatePercent,
      jobsRequiringRetry: totalRetriedArticles > 0 ? Math.ceil(totalRetriedArticles / (averageRetryAttempts || 1)) : 0,
      retriesPerformed: totalRetriedArticles,
      retrySuccessRate: retrySuccessRatePercent,
      
      // Metadata
      timestamp: apiMetrics.timestamp,
      periodLabel: apiMetrics.period_label || apiMetrics.periodLabel || 'Last 24 hours',
      timePeriod: '24h',  // Default, can be updated based on request
    };
  }

  /**
   * Load Jobs Effect
   * 
   * Listens to loadSchedulerJobs action and fetches jobs from API.
   */
  loadSchedulerJobs$ = createEffect(() =>
    this.actions$.pipe(
      ofType(NewsSchedulerActions.loadSchedulerJobs),
      withLatestFrom(
        this.store.select(selectFilters),
        this.store.select(selectCurrentPage),
        this.store.select(selectPageSize),
        this.store.select(selectSort)
      ),
      switchMap(([_, filters, page, size, sort]) =>
        this.schedulerService
          .getJobsList({
            ...filters,
            page,
            size,
            sort: `${sort.sortBy},${sort.direction}`,
            priority: filters.priority as any,
          } as any)
          .pipe(
            map((response) => {
              const mappedJobs = (response.data?.content ?? []).map((job: any) => this.mapApiJobToModel(job));
              return NewsSchedulerActions.loadSchedulerJobsSuccess({
                jobs: mappedJobs,
                total: response.data?.totalElements ?? 0,
                page: response.data?.pageable?.pageNumber ?? 0,
                pages: response.data?.totalPages ?? 0,
              });
            }),
            catchError((error) =>
              of(
                NewsSchedulerActions.loadSchedulerJobsFailure({
                  error: error.message || 'Failed to load jobs',
                })
              )
            )
          )
      )
    )
  );

  /**
   * Load Job Details Effect
   * 
   * Listens to loadSchedulerJobDetails and fetches single job.
   */
  loadSchedulerJobDetails$ = createEffect(() =>
    this.actions$.pipe(
      ofType(NewsSchedulerActions.loadSchedulerJobDetails),
      switchMap(({ jobId }) =>
        this.schedulerService.getJobDetails(jobId).pipe(
          map((response) =>
            NewsSchedulerActions.loadSchedulerJobDetailsSuccess({
              job: this.mapApiJobToModel(response.data),
            })
          ),
          catchError((error) =>
            of(
              NewsSchedulerActions.loadSchedulerJobDetailsFailure({
                error: error.message || 'Failed to load job details',
              })
            )
          )
        )
      )
    )
  );

  /**
   * Select Job Effect
   * 
   * When job is selected, also load its details.
   */
  selectSchedulerJob$ = createEffect(() =>
    this.actions$.pipe(
      ofType(NewsSchedulerActions.selectSchedulerJob),
      map(({ jobId }) =>
        NewsSchedulerActions.loadSchedulerJobDetails({ jobId })
      )
    )
  );

  /**
   * Trigger Job Effect
   * 
   * Listens to triggerSchedulerJob and calls API to create new job.
   */
  triggerSchedulerJob$ = createEffect(() =>
    this.actions$.pipe(
      ofType(NewsSchedulerActions.triggerSchedulerJob),
      switchMap(({ request }) =>
        this.schedulerService.triggerJob(request).pipe(
          map((response) =>
            NewsSchedulerActions.triggerSchedulerJobSuccess({
              job: this.mapApiJobToModel(response.data),
            })
          ),
          catchError((error) =>
            of(
              NewsSchedulerActions.triggerSchedulerJobFailure({
                error: error.message || 'Failed to trigger job',
              })
            )
          )
        )
      )
    )
  );

  /**
   * Trigger Job Success Effect
   * 
   * After successful job trigger, reload jobs list.
   */
  triggerSchedulerJobSuccess$ = createEffect(() =>
    this.actions$.pipe(
      ofType(NewsSchedulerActions.triggerSchedulerJobSuccess),
      map(() => NewsSchedulerActions.loadSchedulerJobs({}))
    )
  );

  /**
   * Cancel Job Effect
   * 
   * Listens to cancelSchedulerJob and calls API to cancel.
   */
  cancelSchedulerJob$ = createEffect(() =>
    this.actions$.pipe(
      ofType(NewsSchedulerActions.cancelSchedulerJob),
      switchMap(({ jobId }) =>
        this.schedulerService.cancelJob(jobId).pipe(
          map((response) =>
            NewsSchedulerActions.cancelSchedulerJobSuccess({
              job: this.mapApiJobToModel(response.data),
            })
          ),
          catchError((error) =>
            of(
              NewsSchedulerActions.cancelSchedulerJobFailure({
                error: error.message || 'Failed to cancel job',
              })
            )
          )
        )
      )
    )
  );

  /**
   * Delete Job Effect
   * 
   * Listens to deleteSchedulerJob and calls API to delete.
   */
  deleteSchedulerJob$ = createEffect(() =>
    this.actions$.pipe(
      ofType(NewsSchedulerActions.deleteSchedulerJob),
      switchMap(({ jobId }) =>
        this.schedulerService.deleteJob(jobId).pipe(
          map(() =>
            NewsSchedulerActions.deleteSchedulerJobSuccess({
              jobId,
            })
          ),
          catchError((error) =>
            of(
              NewsSchedulerActions.deleteSchedulerJobFailure({
                error: error.message || 'Failed to delete job',
              })
            )
          )
        )
      )
    )
  );

  /**
   * Load Failed Articles Effect
   * 
   * Listens to loadFailedArticles and fetches failed articles.
   */
  loadFailedArticles$ = createEffect(() =>
    this.actions$.pipe(
      ofType(NewsSchedulerActions.loadFailedArticles),
      switchMap(({ jobId, page, size }) =>
        this.schedulerService
          .getFailedArticles({
            jobId,
            page: page ?? 0,
            size: size ?? 20,
          })
          .pipe(
            map((response) =>
              NewsSchedulerActions.loadFailedArticlesSuccess({
                articles: response.data?.content ?? [],
                total: response.data?.totalElements ?? 0,
                page: response.data?.pageable?.pageNumber ?? 0,
              })
            ),
            catchError((error) =>
              of(
                NewsSchedulerActions.loadFailedArticlesFailure({
                  error: error.message || 'Failed to load failed articles',
                })
              )
            )
          )
      )
    )
  );

  /**
   * Set Failed Articles Page Effect
   * 
   * Listens to setFailedArticlesPage and reloads failed articles with new pagination.
   */
  setFailedArticlesPage$ = createEffect(() =>
    this.actions$.pipe(
      ofType(NewsSchedulerActions.setFailedArticlesPage),
      map(({ page, pageSize }) =>
        NewsSchedulerActions.loadFailedArticles({
          page,
          size: pageSize,
        })
      )
    )
  );

  /**
   * Retry Failed Articles Effect
   * 
   * Listens to retryFailedArticles and calls API to retry.
   */
  retryFailedArticles$ = createEffect(() =>
    this.actions$.pipe(
      ofType(NewsSchedulerActions.retryFailedArticles),
      switchMap(({ request }) =>
        this.schedulerService.retryFailedArticles(request).pipe(
          map((response) =>
            NewsSchedulerActions.retryFailedArticlesSuccess({
              retriedCount: response.data?.retriedArticleCount ?? 0,
              failedCount: response.data?.failedArticleCount ?? 0,
              message: response.data?.message ?? '',
            })
          ),
          catchError((error) =>
            of(
              NewsSchedulerActions.retryFailedArticlesFailure({
                error: error.message || 'Failed to retry articles',
              })
            )
          )
        )
      )
    )
  );

  /**
   * Retry Failed Articles Success Effect
   * 
   * After successful retry, reload failed articles.
   */
  retryFailedArticlesSuccess$ = createEffect(() =>
    this.actions$.pipe(
      ofType(NewsSchedulerActions.retryFailedArticlesSuccess),
      map(() => NewsSchedulerActions.loadFailedArticles({}))
    )
  );

  /**
   * Load Metrics Effect
   * 
   * Listens to loadSchedulerMetrics and fetches metrics.
   */
  loadSchedulerMetrics$ = createEffect(() =>
    this.actions$.pipe(
      ofType(NewsSchedulerActions.loadSchedulerMetrics),
      withLatestFrom(this.store.select(selectMetricsTimePeriod)),
      switchMap(([{ timePeriod, status }, defaultPeriod]) =>
        this.schedulerService
          .getMetrics(timePeriod ?? defaultPeriod, status)
          .pipe(
            map((response) =>
              NewsSchedulerActions.loadSchedulerMetricsSuccess({
                metrics: this.mapApiMetricsToModel(response.data),
              })
            ),
            catchError((error) =>
              of(
                NewsSchedulerActions.loadSchedulerMetricsFailure({
                  error: error.message || 'Failed to load metrics',
                })
              )
            )
          )
      )
    )
  );

  /**
   * Pagination Change Effect
   * 
   * When pagination changes, reload jobs.
   */
  paginationChange$ = createEffect(() =>
    this.actions$.pipe(
      ofType(
        NewsSchedulerActions.setSchedulerPage,
        NewsSchedulerActions.setSchedulerPageSize
      ),
      debounceTime(300),
      map(() => NewsSchedulerActions.loadSchedulerJobs({}))
    )
  );

  /**
   * Filter Change Effect
   * 
   * When filters change, reload jobs with new filters.
   */
  filterChange$ = createEffect(() =>
    this.actions$.pipe(
      ofType(
        NewsSchedulerActions.setSchedulerStatusFilter,
        NewsSchedulerActions.setSchedulerPriorityFilter,
        NewsSchedulerActions.setSchedulerDateRangeFilter,
        NewsSchedulerActions.clearSchedulerFilters
      ),
      debounceTime(300),
      map(() => NewsSchedulerActions.loadSchedulerJobs({}))
    )
  );

  /**
   * Sort Change Effect
   * 
   * When sort changes, reload jobs with new sorting.
   */
  sortChange$ = createEffect(() =>
    this.actions$.pipe(
      ofType(NewsSchedulerActions.setSortScheduler),
      debounceTime(300),
      map(() => NewsSchedulerActions.loadSchedulerJobs({}))
    )
  );

  /**
   * Auto Refresh Tick Effect
   * 
   * Periodic refresh of jobs and metrics.
   */
  autoRefreshTick$ = createEffect(() =>
    this.actions$.pipe(
      ofType(NewsSchedulerActions.autoRefreshTick),
      mergeMap(() => [
        NewsSchedulerActions.loadSchedulerJobs({}),
        NewsSchedulerActions.loadSchedulerMetrics({}),
      ])
    )
  );
}
