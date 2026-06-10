/**
 * NEWS SCHEDULER NGRX STATE
 * 
 * Central state management for scheduler feature.
 * Defines the shape of the state tree and related types.
 */

import {
  NewsSchedulerJob,
  NewsSchedulerAttempt,
  NewsSchedulerMetrics,
  NewsSchedulerJobStatus,
} from '../models/news-scheduler.model';

/**
 * NewsSchedulerState - Root state for scheduler feature
 * 
 * This represents the complete state tree for all scheduler-related data.
 * Each property is managed by reducers and selectors.
 */
export interface NewsSchedulerState {
  // ==================== JOB MANAGEMENT ====================
  
  /** All jobs loaded from server (paginated) */
  jobs: NewsSchedulerJob[];
  
  /** Currently selected job ID for viewing details */
  selectedJobId: string | null;
  
  /** Full details of selected job (including attempts) */
  selectedJobDetails: NewsSchedulerJob | null;

  // ==================== UI STATE ====================

  /** Whether data is being fetched */
  loading: boolean;
  
  /** Whether a trigger operation is in progress */
  triggering: boolean;
  
  /** Whether a cancel operation is in progress */
  cancelling: boolean;
  
  /** Whether retry operation is in progress */
  retrying: boolean;

  // ==================== ERROR HANDLING ====================

  /** Error message from failed operation */
  error: string | null;
  
  /** Error code for categorized error handling */
  errorCode?: string;

  // ==================== PAGINATION ====================

  /** Current page number (0-indexed) */
  currentPage: number;
  
  /** Items per page */
  pageSize: number;
  
  /** Total number of jobs */
  totalJobs: number;
  
  /** Total number of pages */
  totalPages: number;

  // ==================== FILTERING ====================

  /** Filter: Job status (optional) */
  filterStatus?: NewsSchedulerJobStatus;
  
  /** Filter: Priority level (optional) */
  filterPriority?: string;
  
  /** Filter: Date range start (optional) */
  filterDateFrom?: Date;
  
  /** Filter: Date range end (optional) */
  filterDateTo?: Date;

  // ==================== SORTING ====================

  /** Sort field (e.g., 'startedAt') */
  sortBy: string;
  
  /** Sort direction ('asc' or 'desc') */
  sortDirection: 'asc' | 'desc';

  // ==================== FAILED ARTICLES ====================

  /** List of failed articles for retry management */
  failedArticles: NewsSchedulerAttempt[];
  
  /** Pagination for failed articles list */
  failedArticlesPage: number;
  
  /** Page size for failed articles list */
  failedArticlesPageSize: number;
  
  /** Total failed articles count */
  totalFailedArticles: number;

  // ==================== METRICS ====================

  /** Performance metrics for current time period */
  metrics: NewsSchedulerMetrics | null;
  
  /** Selected time period for metrics ('24h', '7d', '30d') */
  metricsTimePeriod: '24h' | '7d' | '30d';

  // ==================== UI FLAGS ====================

  /** Show job details panel/dialog */
  showJobDetails: boolean;
  
  /** Show failed articles list */
  showFailedArticles: boolean;
  
  /** Show metrics dashboard */
  showMetrics: boolean;
  
  /** Show trigger job form */
  showTriggerForm: boolean;

  // ==================== REFRESH STATE ====================

  /** Last refresh timestamp */
  lastRefreshed: Date | null;
  
  /** Auto-refresh interval in seconds (0 = disabled) */
  autoRefreshInterval: number;
  
  /** Whether auto-refresh is active */
  autoRefreshActive: boolean;
}

/**
 * Initial state for scheduler feature
 * 
 * All properties initialized with empty/default values.
 * State is immutable and only modified through reducers.
 */
export const initialNewsSchedulerState: NewsSchedulerState = {
  jobs: [],
  selectedJobId: null,
  selectedJobDetails: null,

  loading: false,
  triggering: false,
  cancelling: false,
  retrying: false,

  error: null,
  errorCode: undefined,

  currentPage: 0,
  pageSize: 20,
  totalJobs: 0,
  totalPages: 0,

  filterStatus: undefined,
  filterPriority: undefined,
  filterDateFrom: undefined,
  filterDateTo: undefined,

  sortBy: 'startedAt',
  sortDirection: 'desc',

  failedArticles: [],
  failedArticlesPage: 0,
  failedArticlesPageSize: 20,
  totalFailedArticles: 0,

  metrics: null,
  metricsTimePeriod: '24h',

  showJobDetails: false,
  showFailedArticles: false,
  showMetrics: false,
  showTriggerForm: false,

  lastRefreshed: null,
  autoRefreshInterval: 0,
  autoRefreshActive: false,
};

/**
 * Loaded state - Used during async operations to track loading state
 */
export interface LoadingState {
  isLoading: boolean;
  error?: string | null;
}
