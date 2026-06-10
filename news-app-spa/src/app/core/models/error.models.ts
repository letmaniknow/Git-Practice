/**
 * Error Models
 * Centralized type definitions for error handling across the application
 * Ensures type-safe error handling with consistent interfaces
 */

/**
 * Generic API Error Response
 * Returned from HttpErrorResponse or thrown by services
 */
export interface IApiError {
  /** Human-readable error message for user display */
  message: string;

  /** HTTP status code (400, 401, 403, 500, etc.) */
  statusCode: number;

  /** Can this error be retried? */
  retryable: boolean;

  /** Specific context where the error occurred (for analytics/logging) */
  context?: string;

  /** Original HTTP error object */
  originalError?: any;

  /** Timestamp when the error occurred */
  timestamp?: Date;

  /** Error code/type for analytics */
  errorCode?: string;
}

/**
 * Resource Loading State
 * Tracks loading and error state for any async resource
 */
export interface IResourceLoadingState {
  /** Is the resource currently loading */
  isLoading: boolean;

  /** Error message if load failed (null if no error) */
  error: string | null;

  /** Successfully loaded data (can be any type) */
  data?: any;

  /** Number of retry attempts made */
  retryCount?: number;

  /** Maximum retries allowed before giving up */
  maxRetries?: number;
}

/**
 * Retry Configuration
 * Settings for automatic retry behavior
 */
export interface IRetryConfig {
  /** Maximum number of retry attempts */
  maxAttempts: number;

  /** Initial delay in milliseconds */
  initialDelayMs: number;

  /** Maximum delay in milliseconds (for exponential backoff) */
  maxDelayMs?: number;

  /** Multiplier for exponential backoff (e.g., 2 for double each time) */
  backoffMultiplier?: number;

  /** Should retry on specific status codes (e.g., [500, 502, 503]) */
  retryOnStatus?: number[];

  /** Should NOT retry on specific status codes (e.g., [400, 401, 403, 404]) */
  doNotRetryOnStatus?: number[];
}

/**
 * Default Retry Configuration
 * Safe defaults: 3 attempts with exponential backoff
 */
export const DEFAULT_RETRY_CONFIG: IRetryConfig = {
  maxAttempts: 3,
  initialDelayMs: 1000,
  maxDelayMs: 10000,
  backoffMultiplier: 2,
  retryOnStatus: [500, 502, 503, 504, 0], // 0 = network error
  doNotRetryOnStatus: [400, 401, 403, 404],
};
