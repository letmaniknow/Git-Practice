/**
 * NEWS SCHEDULER NGRX SELECTORS
 * 
 * Memoized selectors for efficient state queries.
 * Selectors are used by components to subscribe to state updates.
 */

import { createFeatureSelector, createSelector } from '@ngrx/store';
import { NewsSchedulerState } from './news-scheduler.state';
import { NewsSchedulerJob, JobSummary } from '../models/news-scheduler.model';

/**
 * Feature Selector
 * 
 * Gets the entire NewsSchedulerState from app state.
 */
export const selectNewsSchedulerFeature =
  createFeatureSelector<NewsSchedulerState>('newsScheduler');

// ==================== JOB SELECTORS ====================

/**
 * Select all jobs
 */
export const selectSchedulerJobs = createSelector(
  selectNewsSchedulerFeature,
  (state: NewsSchedulerState) => state.jobs
);

/**
 * Select jobs with calculated success rates
 */
export const selectSchedulerJobsSummary = createSelector(
  selectSchedulerJobs,
  (jobs: NewsSchedulerJob[]): JobSummary[] =>
    jobs.map((job) => ({
      jobId: job.jobId,
      status: job.status,
      totalArticles: job.totalArticles,
      publishedCount: job.publishedCount,
      failedCount: job.failedCount,
      startedAt: job.startedAt,
      durationMs: job.durationMs,
      successRate:
        job.totalArticles > 0
          ? Math.round((job.publishedCount / job.totalArticles) * 100)
          : 0,
    }))
);

/**
 * Select selected job ID
 */
export const selectSelectedJobId = createSelector(
  selectNewsSchedulerFeature,
  (state: NewsSchedulerState) => state.selectedJobId
);

/**
 * Select selected job details
 */
export const selectSelectedJobDetails = createSelector(
  selectNewsSchedulerFeature,
  (state: NewsSchedulerState) => state.selectedJobDetails
);

/**
 * Select job count
 */
export const selectJobCount = createSelector(
  selectNewsSchedulerFeature,
  (state: NewsSchedulerState) => state.totalJobs
);

// ==================== PAGINATION SELECTORS ====================

/**
 * Select current page
 */
export const selectCurrentPage = createSelector(
  selectNewsSchedulerFeature,
  (state: NewsSchedulerState) => state.currentPage
);

/**
 * Select page size
 */
export const selectPageSize = createSelector(
  selectNewsSchedulerFeature,
  (state: NewsSchedulerState) => state.pageSize
);

/**
 * Select total pages
 */
export const selectTotalPages = createSelector(
  selectNewsSchedulerFeature,
  (state: NewsSchedulerState) => state.totalPages
);

/**
 * Select pagination info object
 */
export const selectPagination = createSelector(
  selectCurrentPage,
  selectPageSize,
  selectJobCount,
  selectTotalPages,
  (page, size, total, pages) => ({
    page,
    size,
    total,
    pages,
    hasNextPage: page < pages - 1,
    hasPrevPage: page > 0,
  })
);

// ==================== FILTER SELECTORS ====================

/**
 * Select all filters as object
 */
export const selectFilters = createSelector(
  selectNewsSchedulerFeature,
  (state: NewsSchedulerState) => ({
    status: state.filterStatus,
    priority: state.filterPriority,
    dateFrom: state.filterDateFrom,
    dateTo: state.filterDateTo,
  })
);

/**
 * Select has active filters
 */
export const selectHasActiveFilters = createSelector(
  selectFilters,
  (filters) =>
    !!filters.status ||
    !!filters.priority ||
    !!filters.dateFrom ||
    !!filters.dateTo
);

// ==================== SORTING SELECTORS ====================

/**
 * Select sort configuration
 */
export const selectSort = createSelector(
  selectNewsSchedulerFeature,
  (state: NewsSchedulerState) => ({
    sortBy: state.sortBy,
    direction: state.sortDirection,
  })
);

// ==================== LOADING STATE SELECTORS ====================

/**
 * Select loading state
 */
export const selectLoading = createSelector(
  selectNewsSchedulerFeature,
  (state: NewsSchedulerState) => state.loading
);

/**
 * Select triggering state
 */
export const selectTriggering = createSelector(
  selectNewsSchedulerFeature,
  (state: NewsSchedulerState) => state.triggering
);

/**
 * Select cancelling state
 */
export const selectCancelling = createSelector(
  selectNewsSchedulerFeature,
  (state: NewsSchedulerState) => state.cancelling
);

/**
 * Select retrying state
 */
export const selectRetrying = createSelector(
  selectNewsSchedulerFeature,
  (state: NewsSchedulerState) => state.retrying
);

/**
 * Select any operation in progress
 */
export const selectAnyOperationInProgress = createSelector(
  selectLoading,
  selectTriggering,
  selectCancelling,
  selectRetrying,
  (loading, triggering, cancelling, retrying) =>
    loading || triggering || cancelling || retrying
);

// ==================== ERROR SELECTORS ====================

/**
 * Select error message
 */
export const selectError = createSelector(
  selectNewsSchedulerFeature,
  (state: NewsSchedulerState) => state.error
);

/**
 * Select has error
 */
export const selectHasError = createSelector(
  selectError,
  (error) => !!error
);

// ==================== UI STATE SELECTORS ====================

/**
 * Select show job details
 */
export const selectShowJobDetails = createSelector(
  selectNewsSchedulerFeature,
  (state: NewsSchedulerState) => state.showJobDetails
);

/**
 * Select show failed articles
 */
export const selectShowFailedArticles = createSelector(
  selectNewsSchedulerFeature,
  (state: NewsSchedulerState) => state.showFailedArticles
);

/**
 * Select show metrics
 */
export const selectShowMetrics = createSelector(
  selectNewsSchedulerFeature,
  (state: NewsSchedulerState) => state.showMetrics
);

/**
 * Select show trigger form
 */
export const selectShowTriggerForm = createSelector(
  selectNewsSchedulerFeature,
  (state: NewsSchedulerState) => state.showTriggerForm
);

// ==================== FAILED ARTICLES SELECTORS ====================

/**
 * Select failed articles
 */
export const selectFailedArticles = createSelector(
  selectNewsSchedulerFeature,
  (state: NewsSchedulerState) => state.failedArticles
);

/**
 * Select failed articles count
 */
export const selectFailedArticlesCount = createSelector(
  selectNewsSchedulerFeature,
  (state: NewsSchedulerState) => state.totalFailedArticles
);

/**
 * Select failed articles pagination
 */
export const selectFailedArticlesPagination = createSelector(
  selectNewsSchedulerFeature,
  (state: NewsSchedulerState) => ({
    page: state.failedArticlesPage,
    size: state.failedArticlesPageSize,
    total: state.totalFailedArticles,
  })
);

// ==================== METRICS SELECTORS ====================

/**
 * Select metrics
 */
export const selectMetrics = createSelector(
  selectNewsSchedulerFeature,
  (state: NewsSchedulerState) => state.metrics
);

/**
 * Select metrics time period
 */
export const selectMetricsTimePeriod = createSelector(
  selectNewsSchedulerFeature,
  (state: NewsSchedulerState) => state.metricsTimePeriod
);

/**
 * Select success rate from metrics
 */
export const selectSuccessRate = createSelector(
  selectMetrics,
  (metrics) => metrics?.jobSuccessRate ?? 0
);

/**
 * Select article success rate from metrics
 */
export const selectArticleSuccessRate = createSelector(
  selectMetrics,
  (metrics) => metrics?.articleSuccessRate ?? 0
);

/**
 * Select average job duration
 */
export const selectAverageJobDuration = createSelector(
  selectMetrics,
  (metrics) => metrics?.averageJobDurationMs ?? 0
);

// ==================== AUTO-REFRESH SELECTORS ====================

/**
 * Select auto refresh active
 */
export const selectAutoRefreshActive = createSelector(
  selectNewsSchedulerFeature,
  (state: NewsSchedulerState) => state.autoRefreshActive
);

/**
 * Select auto refresh interval
 */
export const selectAutoRefreshInterval = createSelector(
  selectNewsSchedulerFeature,
  (state: NewsSchedulerState) => state.autoRefreshInterval
);

/**
 * Select last refreshed time
 */
export const selectLastRefreshed = createSelector(
  selectNewsSchedulerFeature,
  (state: NewsSchedulerState) => state.lastRefreshed
);

// ==================== COMPLEX COMPUTED SELECTORS ====================

/**
 * Select job success badge information
 */
export const selectJobSuccessBadge = createSelector(
  selectSelectedJobDetails,
  (job) => {
    if (!job) return null;

    const successRate =
      job.totalArticles > 0
        ? Math.round((job.publishedCount / job.totalArticles) * 100)
        : 0;

    return {
      label: `${successRate}% Success (${job.publishedCount}/${job.totalArticles})`,
      percentage: successRate,
      color: successRate === 100 ? 'success' : successRate >= 75 ? 'warn' : 'error',
    };
  }
);

/**
 * Select ready for retry (has failed articles)
 */
export const selectCanRetry = createSelector(
  selectFailedArticlesCount,
  (count) => count > 0
);

/**
 * Select selected job with summary
 */
export const selectSelectedJobSummary = createSelector(
  selectSelectedJobDetails,
  (job): JobSummary | null =>
    job
      ? {
          jobId: job.jobId,
          status: job.status,
          totalArticles: job.totalArticles,
          publishedCount: job.publishedCount,
          failedCount: job.failedCount,
          startedAt: job.startedAt,
          durationMs: job.durationMs,
          successRate:
            job.totalArticles > 0
              ? Math.round((job.publishedCount / job.totalArticles) * 100)
              : 0,
        }
      : null
);
