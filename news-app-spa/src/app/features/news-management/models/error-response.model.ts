/**
 * Error response model matching backend ErrorResponse (RFC 7807 compliant)
 * 
 * Represents structured error responses from the API with optional
 * field-level validation errors for better UX and error handling.
 */

/**
 * Field-level error information
 * Used for form validation and inline error display
 */
export interface FieldError {
  field: string;
  message: string;
  code?: string;
  rejectedValue?: any;
}

/**
 * Standard error response structure
 * Follows REST best practices (RFC 7807 - Problem Details for HTTP APIs)
 */
export interface ErrorResponse {
  /**
   * ISO 8601 timestamp when error occurred
   * @example "2025-12-30T22:50:00Z"
   */
  timestamp: string;

  /**
   * HTTP status code
   * @example 400
   */
  status: number;

  /**
   * Error type/category for classification
   * @example "Validation Error"
   */
  error: string;

  /**
   * Machine-readable error code for client-side handling
   * Enables consistent error handling across clients
   * @example "VALIDATION_001"
   */
  code: string;

  /**
   * Human-readable error message for users
   * @example "Validation failed. See fieldErrors for details."
   */
  message: string;

  /**
   * Request URI that caused the error
   * @example "/api/v1/admin/news"
   */
  path: string;

  /**
   * Field-level validation errors (optional)
   * Maps field names to their error messages
   * Enables per-field error display in forms
   * @example { "newsContent": "Content must be at least 100 characters", "newsTitle": "Title is required" }
   */
  fieldErrors?: { [fieldName: string]: string };
}

/**
 * Wrapper response for API errors
 * Wraps ErrorResponse in ApiResponseDto structure
 */
export interface ApiErrorResponse {
  status: string;      // "error"
  message: string;     // Error summary
  timestamp: string;   // ISO 8601
  data: ErrorResponse; // Detailed error info
}
