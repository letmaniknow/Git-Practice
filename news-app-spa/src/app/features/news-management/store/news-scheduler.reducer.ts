/**
 * NEWS SCHEDULER NGRX REDUCER
 * 
 * Pure functions that handle state mutations.
 * Reducers respond to actions and return new state.
 */

import { createReducer, on } from '@ngrx/store';
import { initialNewsSchedulerState, NewsSchedulerState } from './news-scheduler.state';
import * as NewsSchedulerActions from './news-scheduler.actions';

/**
 * NewsScheduler Reducer
 * 
 * Handles all state mutations for the scheduler feature.
 * Each case handler receives current state and action payload,
 * and returns new immutable state.
 */
export const newsSchedulerReducer = createReducer(
  initialNewsSchedulerState,

  // ==================== JOB LOADING ====================

  on(NewsSchedulerActions.loadSchedulerJobs, (state) => ({
    ...state,
    loading: true,
    error: null,
  })),

  on(
    NewsSchedulerActions.loadSchedulerJobsSuccess,
    (state, { jobs, total, page, pages }) => ({
      ...state,
      jobs,
      totalJobs: total,
      currentPage: page,
      totalPages: pages,
      loading: false,
      error: null,
      lastRefreshed: new Date(),
    })
  ),

  on(NewsSchedulerActions.loadSchedulerJobsFailure, (state, { error }) => ({
    ...state,
    loading: false,
    error,
  })),

  // ==================== JOB SELECTION ====================

  on(NewsSchedulerActions.selectSchedulerJob, (state, { jobId }) => ({
    ...state,
    selectedJobId: jobId,
    showJobDetails: true,
  })),

  on(
    NewsSchedulerActions.loadSchedulerJobDetailsSuccess,
    (state, { job }) => ({
      ...state,
      selectedJobDetails: job,
      loading: false,
      error: null,
    })
  ),

  on(NewsSchedulerActions.loadSchedulerJobDetailsFailure, (state, { error }) => ({
    ...state,
    loading: false,
    error,
    selectedJobDetails: null,
  })),

  // ==================== JOB TRIGGER ====================

  on(NewsSchedulerActions.triggerSchedulerJob, (state) => ({
    ...state,
    triggering: true,
    error: null,
  })),

  on(NewsSchedulerActions.triggerSchedulerJobSuccess, (state, { job }) => ({
    ...state,
    triggering: false,
    error: null,
    jobs: [job, ...state.jobs],
    totalJobs: state.totalJobs + 1,
    showTriggerForm: false,
  })),

  on(NewsSchedulerActions.triggerSchedulerJobFailure, (state, { error }) => ({
    ...state,
    triggering: false,
    error,
  })),

  // ==================== JOB CANCELLATION ====================

  on(NewsSchedulerActions.cancelSchedulerJob, (state) => ({
    ...state,
    cancelling: true,
    error: null,
  })),

  on(NewsSchedulerActions.cancelSchedulerJobSuccess, (state, { job }) => ({
    ...state,
    cancelling: false,
    error: null,
    jobs: state.jobs.map((j) => (j.jobId === job.jobId ? job : j)),
    selectedJobDetails:
      state.selectedJobDetails?.jobId === job.jobId
        ? job
        : state.selectedJobDetails,
  })),

  on(NewsSchedulerActions.cancelSchedulerJobFailure, (state, { error }) => ({
    ...state,
    cancelling: false,
    error,
  })),

  // ==================== JOB DELETION ====================

  on(NewsSchedulerActions.deleteSchedulerJob, (state) => ({
    ...state,
    loading: true,
    error: null,
  })),

  on(NewsSchedulerActions.deleteSchedulerJobSuccess, (state, { jobId }) => ({
    ...state,
    loading: false,
    error: null,
    jobs: state.jobs.filter((j) => j.jobId !== jobId),
    totalJobs: state.totalJobs - 1,
    selectedJobId:
      state.selectedJobId === jobId ? null : state.selectedJobId,
    selectedJobDetails:
      state.selectedJobDetails?.jobId === jobId
        ? null
        : state.selectedJobDetails,
    showJobDetails: false,
  })),

  on(NewsSchedulerActions.deleteSchedulerJobFailure, (state, { error }) => ({
    ...state,
    loading: false,
    error,
  })),

  // ==================== FAILED ARTICLES ====================

  on(NewsSchedulerActions.loadFailedArticles, (state) => ({
    ...state,
    loading: true,
    error: null,
  })),

  on(
    NewsSchedulerActions.loadFailedArticlesSuccess,
    (state, { articles, total, page }) => ({
      ...state,
      failedArticles: articles,
      totalFailedArticles: total,
      failedArticlesPage: page,
      loading: false,
      error: null,
    })
  ),

  on(NewsSchedulerActions.loadFailedArticlesFailure, (state, { error }) => ({
    ...state,
    loading: false,
    error,
  })),

  on(NewsSchedulerActions.setFailedArticlesPage, (state, { page, pageSize }) => ({
    ...state,
    failedArticlesPage: page,
    failedArticlesPageSize: pageSize,
    loading: true,
  })),

  on(NewsSchedulerActions.retryFailedArticles, (state) => ({
    ...state,
    retrying: true,
    error: null,
  })),

  on(
    NewsSchedulerActions.retryFailedArticlesSuccess,
    (state, { retriedCount }) => ({
      ...state,
      retrying: false,
      error: null,
      // Refresh failed articles after retry
      failedArticles: state.failedArticles.slice(retriedCount),
      totalFailedArticles: Math.max(0, state.totalFailedArticles - retriedCount),
    })
  ),

  on(NewsSchedulerActions.retryFailedArticlesFailure, (state, { error }) => ({
    ...state,
    retrying: false,
    error,
  })),

  // ==================== METRICS ====================

  on(NewsSchedulerActions.loadSchedulerMetrics, (state) => ({
    ...state,
    loading: true,
    error: null,
  })),

  on(NewsSchedulerActions.loadSchedulerMetricsSuccess, (state, { metrics }) => ({
    ...state,
    metrics,
    loading: false,
    error: null,
  })),

  on(NewsSchedulerActions.loadSchedulerMetricsFailure, (state, { error }) => ({
    ...state,
    loading: false,
    error,
    metrics: null,
  })),

  // ==================== UI STATE ====================

  on(NewsSchedulerActions.toggleJobDetails, (state, { show }) => ({
    ...state,
    showJobDetails: show,
  })),

  on(NewsSchedulerActions.toggleFailedArticles, (state, { show }) => ({
    ...state,
    showFailedArticles: show,
  })),

  on(NewsSchedulerActions.toggleMetricsDashboard, (state, { show }) => ({
    ...state,
    showMetrics: show,
  })),

  on(NewsSchedulerActions.toggleTriggerForm, (state, { show }) => ({
    ...state,
    showTriggerForm: show,
  })),

  // ==================== PAGINATION ====================

  on(NewsSchedulerActions.setSchedulerPage, (state, { page }) => ({
    ...state,
    currentPage: page,
  })),

  on(NewsSchedulerActions.setSchedulerPageSize, (state, { size }) => ({
    ...state,
    pageSize: size,
    currentPage: 0,
  })),

  // ==================== FILTERING ====================

  on(NewsSchedulerActions.setSchedulerStatusFilter, (state, { status }) => ({
    ...state,
    filterStatus: status ? (status as any) : undefined,
    currentPage: 0,
  })),

  on(NewsSchedulerActions.setSchedulerPriorityFilter, (state, { priority }) => ({
    ...state,
    filterPriority: priority,
    currentPage: 0,
  })),

  on(
    NewsSchedulerActions.setSchedulerDateRangeFilter,
    (state, { dateFrom, dateTo }) => ({
      ...state,
      filterDateFrom: dateFrom,
      filterDateTo: dateTo,
      currentPage: 0,
    })
  ),

  on(NewsSchedulerActions.clearSchedulerFilters, (state) => ({
    ...state,
    filterStatus: undefined,
    filterPriority: undefined,
    filterDateFrom: undefined,
    filterDateTo: undefined,
    currentPage: 0,
  })),

  // ==================== SORTING ====================

  on(NewsSchedulerActions.setSortScheduler, (state, { sortBy, direction }) => ({
    ...state,
    sortBy,
    sortDirection: direction,
  })),

  // ==================== AUTO-REFRESH ====================

  on(NewsSchedulerActions.setAutoRefreshInterval, (state, { interval }) => ({
    ...state,
    autoRefreshInterval: interval,
  })),

  on(NewsSchedulerActions.startAutoRefresh, (state) => ({
    ...state,
    autoRefreshActive: true,
  })),

  on(NewsSchedulerActions.stopAutoRefresh, (state) => ({
    ...state,
    autoRefreshActive: false,
  })),

  // ==================== ERROR HANDLING ====================

  on(NewsSchedulerActions.clearSchedulerError, (state) => ({
    ...state,
    error: null,
    errorCode: undefined,
  })),

  // ==================== STATE RESET ====================

  on(NewsSchedulerActions.resetSchedulerState, () => initialNewsSchedulerState)
);
