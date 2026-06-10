/**
 * NEWS SCHEDULER NGRX ACTIONS
 * 
 * All action creators for scheduler state mutations.
 * Actions are dispatched by components and effects.
 */

import { createAction, props } from '@ngrx/store';
import {
  NewsSchedulerJob,
  NewsSchedulerAttempt,
  NewsSchedulerMetrics,
  SchedulerJobTriggerRequest,
  SchedulerBulkRetryRequest,
  SchedulerJobFilter,
} from '../models/news-scheduler.model';

// ==================== JOB LOADING ====================

export const loadSchedulerJobs = createAction(
  '[News Scheduler] Load Jobs',
  props<{ filter?: SchedulerJobFilter }>()
);

export const loadSchedulerJobsSuccess = createAction(
  '[News Scheduler] Load Jobs Success',
  props<{ jobs: NewsSchedulerJob[]; total: number; page: number; pages: number }>()
);

export const loadSchedulerJobsFailure = createAction(
  '[News Scheduler] Load Jobs Failure',
  props<{ error: string }>()
);

// ==================== JOB DETAILS ====================

export const selectSchedulerJob = createAction(
  '[News Scheduler] Select Job',
  props<{ jobId: string }>()
);

export const loadSchedulerJobDetails = createAction(
  '[News Scheduler] Load Job Details',
  props<{ jobId: string }>()
);

export const loadSchedulerJobDetailsSuccess = createAction(
  '[News Scheduler] Load Job Details Success',
  props<{ job: NewsSchedulerJob }>()
);

export const loadSchedulerJobDetailsFailure = createAction(
  '[News Scheduler] Load Job Details Failure',
  props<{ error: string }>()
);

// ==================== JOB TRIGGER ====================

export const triggerSchedulerJob = createAction(
  '[News Scheduler] Trigger Job',
  props<{ request: SchedulerJobTriggerRequest }>()
);

export const triggerSchedulerJobSuccess = createAction(
  '[News Scheduler] Trigger Job Success',
  props<{ job: NewsSchedulerJob }>()
);

export const triggerSchedulerJobFailure = createAction(
  '[News Scheduler] Trigger Job Failure',
  props<{ error: string }>()
);

// ==================== JOB CANCELLATION ====================

export const cancelSchedulerJob = createAction(
  '[News Scheduler] Cancel Job',
  props<{ jobId: string }>()
);

export const cancelSchedulerJobSuccess = createAction(
  '[News Scheduler] Cancel Job Success',
  props<{ job: NewsSchedulerJob }>()
);

export const cancelSchedulerJobFailure = createAction(
  '[News Scheduler] Cancel Job Failure',
  props<{ error: string }>()
);

// ==================== JOB DELETION ====================

export const deleteSchedulerJob = createAction(
  '[News Scheduler] Delete Job',
  props<{ jobId: string }>()
);

export const deleteSchedulerJobSuccess = createAction(
  '[News Scheduler] Delete Job Success',
  props<{ jobId: string }>()
);

export const deleteSchedulerJobFailure = createAction(
  '[News Scheduler] Delete Job Failure',
  props<{ error: string }>()
);

// ==================== FAILED ARTICLES ====================

export const loadFailedArticles = createAction(
  '[News Scheduler] Load Failed Articles',
  props<{ jobId?: string; page?: number; size?: number }>()
);

export const loadFailedArticlesSuccess = createAction(
  '[News Scheduler] Load Failed Articles Success',
  props<{ articles: NewsSchedulerAttempt[]; total: number; page: number }>()
);

export const loadFailedArticlesFailure = createAction(
  '[News Scheduler] Load Failed Articles Failure',
  props<{ error: string }>()
);

export const setFailedArticlesPage = createAction(
  '[News Scheduler] Set Failed Articles Page',
  props<{ page: number; pageSize: number }>()
);

export const retryFailedArticles = createAction(
  '[News Scheduler] Retry Failed Articles',
  props<{ request: SchedulerBulkRetryRequest }>()
);

export const retryFailedArticlesSuccess = createAction(
  '[News Scheduler] Retry Failed Articles Success',
  props<{ retriedCount: number; failedCount: number; message: string }>()
);

export const retryFailedArticlesFailure = createAction(
  '[News Scheduler] Retry Failed Articles Failure',
  props<{ error: string }>()
);

// ==================== METRICS ====================

export const loadSchedulerMetrics = createAction(
  '[News Scheduler] Load Metrics',
  props<{ timePeriod?: '24h' | '7d' | '30d'; status?: string }>()
);

export const loadSchedulerMetricsSuccess = createAction(
  '[News Scheduler] Load Metrics Success',
  props<{ metrics: NewsSchedulerMetrics }>()
);

export const loadSchedulerMetricsFailure = createAction(
  '[News Scheduler] Load Metrics Failure',
  props<{ error: string }>()
);

// ==================== UI STATE ====================

export const toggleJobDetails = createAction(
  '[News Scheduler] Toggle Job Details',
  props<{ show: boolean }>()
);

export const toggleFailedArticles = createAction(
  '[News Scheduler] Toggle Failed Articles',
  props<{ show: boolean }>()
);

export const toggleMetricsDashboard = createAction(
  '[News Scheduler] Toggle Metrics Dashboard',
  props<{ show: boolean }>()
);

export const toggleTriggerForm = createAction(
  '[News Scheduler] Toggle Trigger Form',
  props<{ show: boolean }>()
);

// ==================== PAGINATION ====================

export const setSchedulerPage = createAction(
  '[News Scheduler] Set Page',
  props<{ page: number }>()
);

export const setSchedulerPageSize = createAction(
  '[News Scheduler] Set Page Size',
  props<{ size: number }>()
);

// ==================== FILTERING ====================

export const setSchedulerStatusFilter = createAction(
  '[News Scheduler] Set Status Filter',
  props<{ status?: string }>()
);

export const setSchedulerPriorityFilter = createAction(
  '[News Scheduler] Set Priority Filter',
  props<{ priority?: string }>()
);

export const setSchedulerDateRangeFilter = createAction(
  '[News Scheduler] Set Date Range Filter',
  props<{ dateFrom?: Date; dateTo?: Date }>()
);

export const clearSchedulerFilters = createAction(
  '[News Scheduler] Clear Filters'
);

// ==================== SORTING ====================

export const setSortScheduler = createAction(
  '[News Scheduler] Set Sort',
  props<{ sortBy: string; direction: 'asc' | 'desc' }>()
);

// ==================== AUTO-REFRESH ====================

export const setAutoRefreshInterval = createAction(
  '[News Scheduler] Set Auto Refresh Interval',
  props<{ interval: number }>()
);

export const startAutoRefresh = createAction(
  '[News Scheduler] Start Auto Refresh'
);

export const stopAutoRefresh = createAction(
  '[News Scheduler] Stop Auto Refresh'
);

export const autoRefreshTick = createAction(
  '[News Scheduler] Auto Refresh Tick'
);

// ==================== ERROR HANDLING ====================

export const clearSchedulerError = createAction(
  '[News Scheduler] Clear Error'
);

// ==================== STATE RESET ====================

export const resetSchedulerState = createAction(
  '[News Scheduler] Reset State'
);
