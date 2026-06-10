/**
 * NEWS SCHEDULER MODELS
 * 
 * DTOs matching backend NewsSchedulingJob, NewsSchedulingAttempt,
 * and related response models for the 3-hour scheduler feature.
 * 
 * Backend Reference:
 * - NewsSchedulingJobResponseDto (35 fields)
 * - NewsSchedulingAttemptResponseDto (21 fields)
 * - NewsSchedulingMetricsResponseDto (22 fields)
 * - NewsSchedulingBulkRetryResponseDto (5 fields)
 */

// ==================== JOB STATUS ENUMS ====================

export enum NewsSchedulerJobStatus {
  RUNNING = 'RUNNING',
  SUCCESS = 'SUCCESS',
  FAILED = 'FAILED',
  PARTIAL_SUCCESS = 'PARTIAL_SUCCESS',
  CANCELLED = 'CANCELLED',
}

export enum NewsSchedulerAttemptStatus {
  SUCCESS = 'SUCCESS',
  FAILED = 'FAILED',
  SKIPPED = 'SKIPPED',
  PENDING = 'PENDING',
}

export enum NewsSchedulerErrorCode {
  TIMEOUT = 'TIMEOUT',
  AUTH_FAILED = 'AUTH_FAILED',
  NETWORK_ERROR = 'NETWORK_ERROR',
  VALIDATION_ERROR = 'VALIDATION_ERROR',
  NOT_FOUND = 'NOT_FOUND',
  UNKNOWN_ERROR = 'UNKNOWN_ERROR',
}

export enum NewsSchedulerPriority {
  CRITICAL = 'CRITICAL',
  HIGH = 'HIGH',
  MEDIUM = 'MEDIUM',
  LOW = 'LOW',
}

export enum NewsSchedulerBackoffStrategy {
  EXPONENTIAL = 'EXPONENTIAL',
  LINEAR = 'LINEAR',
  FIXED = 'FIXED',
}

// ==================== MAIN SCHEDULER JOB MODEL ====================

/**
 * NewsSchedulerJob - Main job entity
 * 
 * Represents a scheduled news publishing job with full observability,
 * error tracking, and retry logic. Correlates with backend NewsSchedulingJob.
 */
export interface NewsSchedulerJob {
  // Identity
  jobId: string;                          // UUID
  correlationId: string;                  // Unique tracking ID (100 char)
  
  // Status & Lifecycle
  status: NewsSchedulerJobStatus;         // RUNNING, SUCCESS, FAILED, PARTIAL_SUCCESS, CANCELLED
  priority: NewsSchedulerPriority;        // CRITICAL, HIGH, MEDIUM, LOW
  
  // Timing
  startedAt: Date;                        // When job started
  completedAt?: Date;                     // When job completed
  durationMs: number;                     // Execution time in milliseconds
  nextRetryAt?: Date;                     // Next scheduled retry
  
  // Article Metrics
  totalArticles: number;                  // Total articles in this job
  publishedCount: number;                 // Successfully published
  failedCount: number;                    // Failed during execution
  skippedCount: number;                   // Skipped (validation/business logic)
  
  // Error Handling
  errorMessage?: string;                  // Error details (1000 char max)
  errorCode?: NewsSchedulerErrorCode;     // Categorized error type
  
  // Retry Strategy
  backoffStrategy: NewsSchedulerBackoffStrategy;  // EXPONENTIAL, LINEAR, FIXED
  retryAttempt: number;                   // Current retry attempt number
  maxRetries: number;                     // Maximum retry attempts allowed
  
  // Configuration
  queueName: string;                      // Queue identifier (DEFAULT, etc.)
  partitionKey: string;                   // For distributed processing
  source: string;                         // Trigger source (SCHEDULER, MANUAL, API)
  
  // Tracking
  idempotencyKey: string;                 // For idempotent operations
  executionLogs?: string;                 // JSON stringified logs array
  executionMetrics?: string;              // JSON stringified metrics object
  
  // Audit
  createdAt: Date;
  createdBy: string;
  updatedAt: Date;
  updatedBy: string;
  deletedAt?: Date;
  deletedBy?: string;
}

// ==================== SCHEDULER ATTEMPT MODEL ====================

/**
 * NewsSchedulerAttempt - Individual article publication attempt
 * 
 * Tracks each article's publication attempt within a job, including
 * retry logic, error details, and backoff strategy.
 */
export interface NewsSchedulerAttempt {
  // Identity
  attemptId: string;                      // UUID
  jobId: string;                          // FK to NewsSchedulerJob
  articleId: string;                      // UUID of article being published
  
  // Status
  status: NewsSchedulerAttemptStatus;     // SUCCESS, FAILED, SKIPPED, PENDING
  attemptNumber: number;                  // Which attempt (1st, 2nd, 3rd, etc.)
  
  // Error Tracking
  error?: string;                         // Error message (1000 char max)
  errorCode?: NewsSchedulerErrorCode;     // TIMEOUT, AUTH_FAILED, NETWORK_ERROR, etc.
  
  // Retry Logic
  retryCount: number;                     // How many times retried
  shouldRetry: boolean;                   // Can this attempt be retried?
  backoffDelayMs: number;                 // Milliseconds before next retry
  nextRetryAt?: Date;                     // When to retry
  
  // Idempotency
  idempotencyKey: string;                 // Unique per article + job
  
  // Audit
  timestamp: Date;                        // When attempt was recorded
  createdAt: Date;
  createdBy: string;
  updatedAt: Date;
  updatedBy: string;
}

// ==================== SCHEDULER METRICS MODEL ====================

/**
 * NewsSchedulerMetrics - Performance and reliability metrics
 * 
 * Aggregated metrics for monitoring scheduler health and performance
 * over specified time periods.
 */
export interface NewsSchedulerMetrics {
  // Time Period
  timePeriod: string;                     // '24h', '7d', '30d'
  periodLabel: string;                    // 'Last 24 hours', 'Last 7 days'
  startTime: Date;
  endTime: Date;
  
  // Job Statistics
  totalJobs: number;                      // Total jobs in period
  successfulJobs: number;                 // Jobs with SUCCESS status
  failedJobs: number;                     // Jobs with FAILED status
  partialSuccessJobs: number;             // Jobs with PARTIAL_SUCCESS
  cancelledJobs: number;                  // Jobs with CANCELLED status
  
  // Article Statistics
  totalArticlesProcessed: number;         // Total articles attempted
  successfullyPublished: number;          // Articles with SUCCESS status
  failedArticles: number;                 // Articles with FAILED status
  skippedArticles: number;                // Articles with SKIPPED status
  
  // Performance
  averageJobDurationMs: number;           // Average execution time
  minJobDurationMs: number;               // Fastest job
  maxJobDurationMs: number;               // Slowest job
  
  // Success Rates
  jobSuccessRate: number;                 // Percentage (0-100)
  articleSuccessRate: number;             // Percentage (0-100)
  
  // Error Distribution
  errorsByCode: Record<NewsSchedulerErrorCode, number>;  // Count per error type
  
  // Retry Insights
  jobsRequiringRetry: number;             // Jobs that needed retries
  retriesPerformed: number;               // Total retry attempts
  retrySuccessRate: number;               // Percentage of successful retries
}

// ==================== API RESPONSE MODELS ====================

/**
 * NewsSchedulerJobResponseDto - API response for job operations
 */
export interface NewsSchedulerJobResponseDto extends NewsSchedulerJob {
  attempts?: NewsSchedulerAttempt[];      // Include attempts if requested
}

/**
 * NewsSchedulerBulkRetryResponseDto - Response from bulk retry operation
 */
export interface NewsSchedulerBulkRetryResponseDto {
  jobId: string;
  retriedArticleCount: number;            // How many articles queued for retry
  failedArticleCount: number;             // How many couldn't be retried
  message: string;                        // Status message
  timestamp: Date;
}

// ==================== SCHEDULER REQUEST MODELS ====================

/**
 * SchedulerJobTriggerRequest - Request to trigger a scheduler job
 */
export interface SchedulerJobTriggerRequest {
  priority?: NewsSchedulerPriority;       // Defaults to HIGH
  tags?: string[];                        // Max 5 tags for filtering
  queueName?: string;                     // Defaults to DEFAULT
  maxRetries?: number;                    // Defaults to 3
  backoffStrategy?: NewsSchedulerBackoffStrategy;  // Defaults to EXPONENTIAL
}

/**
 * SchedulerBulkRetryRequest - Request to retry failed articles
 */
export interface SchedulerBulkRetryRequest {
  jobId: string;
  maxRetries?: number;                    // Retry limit (defaults to 3)
  includeSkipped?: boolean;               // Include skipped articles (defaults to false)
}

/**
 * SchedulerMetricsRequest - Request for metrics with filters
 */
export interface SchedulerMetricsRequest {
  timePeriod?: '24h' | '7d' | '30d';     // Defaults to '24h'
  status?: NewsSchedulerJobStatus;        // Filter by status (optional)
}

// ==================== PAGINATION & FILTERING ====================

/**
 * SchedulerJobFilter - Filters for job list queries
 */
export interface SchedulerJobFilter {
  status?: NewsSchedulerJobStatus;
  priority?: NewsSchedulerPriority;
  dateFrom?: Date;
  dateTo?: Date;
  page?: number;                          // Defaults to 0
  size?: number;                          // Defaults to 20
  sort?: string;                          // e.g., 'startedAt,desc'
}

/**
 * SchedulerFailedArticleFilter - Filters for failed articles list
 */
export interface SchedulerFailedArticleFilter {
  jobId?: string;
  shouldRetry?: boolean;
  errorCode?: NewsSchedulerErrorCode;
  page?: number;                          // Defaults to 0
  size?: number;                          // Defaults to 20
  sort?: string;
}

// ==================== HELPER TYPES ====================

/**
 * JobStatusBadge - For UI badge display
 */
export interface JobStatusBadge {
  status: NewsSchedulerJobStatus;
  label: string;
  color: 'primary' | 'accent' | 'warn' | 'success';
  icon: string;
}

/**
 * JobSummary - Quick view summary for list display
 */
export interface JobSummary {
  jobId: string;
  status: NewsSchedulerJobStatus;
  totalArticles: number;
  publishedCount: number;
  failedCount: number;
  startedAt: Date;
  durationMs: number;
  successRate: number;                    // Calculated percentage
}
