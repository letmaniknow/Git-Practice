import { Injectable } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';

/**
 * HTTP Error Status Categories
 * Provides semantic categorization of HTTP errors for appropriate UI/UX handling
 */
export enum HttpErrorCategory {
    // Client Errors (4xx)
    BadRequest = 'BAD_REQUEST',           // 400
    Unauthorized = 'UNAUTHORIZED',         // 401
    Forbidden = 'FORBIDDEN',               // 403
    NotFound = 'NOT_FOUND',                // 404
    Conflict = 'CONFLICT',                 // 409
    UnprocessableEntity = 'UNPROCESSABLE', // 422
    ClientError = 'CLIENT_ERROR',          // Other 4xx

    // Server Errors (5xx)
    ServerError = 'SERVER_ERROR',          // 500
    ServiceUnavailable = 'SERVICE_UNAVAILABLE', // 503
    GatewayTimeout = 'GATEWAY_TIMEOUT',    // 504
    ServerErrorGeneric = 'SERVER_ERROR_GENERIC', // Other 5xx

    // Network Errors
    NetworkError = 'NETWORK_ERROR',        // No response (connection failed)
    Timeout = 'TIMEOUT',                   // Request timed out
    Abort = 'ABORT',                       // Request aborted

    // Unknown
    Unknown = 'UNKNOWN'
}

/**
 * User-friendly error message based on category
 */
export interface CategorizedError {
    category: HttpErrorCategory;
    statusCode: number | null;
    userMessage: string;
    technicalMessage: string;
    severity: 'info' | 'warning' | 'error' | 'critical';
    retryable: boolean;
    action?: 'retry' | 'login' | 'refresh' | 'navigate' | 'none';
}

/**
 * HTTP Error Categorization Service
 *
 * Transforms raw HTTP errors into semantic categories with appropriate
 * user-friendly messages, severity levels, and suggested actions.
 *
 * USAGE:
 * ```typescript
 * import { HttpErrorCategorizationService } from '@core/services/http-error-categorization.service';
 *
 * constructor(private errorCategorizer: HttpErrorCategorizationService) {}
 *
 * // In error handler
 * catchError(error => {
 *   const categorized = this.errorCategorizer.categorizeError(error);
 *   this.logger.log(categorized.category);
 *   this.showUserMessage(categorized.userMessage, categorized.severity);
 *   if (categorized.retryable) { this.enableRetryButton(); }
 *   return throwError(() => categorized);
 * })
 * ```
 *
 * @author MMVA Team
 * @version 1.0
 * @since 2026-04-26
 */
@Injectable({ providedIn: 'root' })
export class HttpErrorCategorizationService {
    constructor() { }

    /**
     * Categorize an HTTP error into semantic category with user-friendly messaging
     *
     * @param error - HTTP error response or error event
     * @returns CategorizedError with category, messages, and suggested action
     */
    categorizeError(error: any): CategorizedError {
        if (error instanceof HttpErrorResponse) {
            return this.categorizeHttpError(error);
        }

        // Network error or unknown error event
        if (error instanceof ProgressEvent || error instanceof ErrorEvent) {
            return this.buildCategorizedError(
                HttpErrorCategory.NetworkError,
                null,
                'Connection Failed',
                'Unable to connect to the server. Check your internet connection and try again.',
                'error',
                true,
                'retry'
            );
        }

        // Timeout error
        if (error instanceof Error && error.message.includes('timeout')) {
            return this.buildCategorizedError(
                HttpErrorCategory.Timeout,
                null,
                'Request Timeout',
                'The request took too long to complete. Please try again.',
                'warning',
                true,
                'retry'
            );
        }

        // Unknown error
        return this.buildCategorizedError(
            HttpErrorCategory.Unknown,
            null,
            'An Unexpected Error Occurred',
            'Please try again or contact support if the problem persists.',
            'error',
            true,
            'retry'
        );
    }

    /**
     * Categorize HTTP error response by status code
     *
     * @private
     * @param error - HTTP error response
     * @returns CategorizedError
     */
    private categorizeHttpError(error: HttpErrorResponse): CategorizedError {
        const status = error.status;
        const errorBody = error.error;

        switch (status) {
            // ====== 400 Bad Request ======
            case 400:
                return this.buildCategorizedError(
                    HttpErrorCategory.BadRequest,
                    400,
                    'Invalid Request',
                    this.extractErrorMessage(errorBody, 'The request contains invalid data. Please check your input.'),
                    'warning',
                    false,
                    'none'
                );

            // ====== 401 Unauthorized ======
            case 401:
                return this.buildCategorizedError(
                    HttpErrorCategory.Unauthorized,
                    401,
                    'Session Expired',
                    'Your session has expired. Please log in again to continue.',
                    'warning',
                    true,
                    'login'
                );

            // ====== 403 Forbidden ======
            case 403:
                return this.buildCategorizedError(
                    HttpErrorCategory.Forbidden,
                    403,
                    'Permission Denied',
                    'You do not have permission to perform this action. Contact your administrator for access.',
                    'warning',
                    false,
                    'none'
                );

            // ====== 404 Not Found ======
            case 404:
                return this.buildCategorizedError(
                    HttpErrorCategory.NotFound,
                    404,
                    'Resource Not Found',
                    'The requested resource does not exist or has been deleted.',
                    'info',
                    false,
                    'navigate'
                );

            // ====== 409 Conflict ======
            case 409:
                return this.buildCategorizedError(
                    HttpErrorCategory.Conflict,
                    409,
                    'Data Conflict',
                    this.extractErrorMessage(errorBody, 'This action conflicts with existing data. Refresh and try again.'),
                    'warning',
                    true,
                    'refresh'
                );

            // ====== 422 Unprocessable Entity ======
            case 422:
                return this.buildCategorizedError(
                    HttpErrorCategory.UnprocessableEntity,
                    422,
                    'Validation Failed',
                    this.extractErrorMessage(errorBody, 'One or more fields contain invalid data.'),
                    'warning',
                    false,
                    'none'
                );

            // ====== 500 Internal Server Error ======
            case 500:
                return this.buildCategorizedError(
                    HttpErrorCategory.ServerError,
                    500,
                    'Server Error',
                    'An error occurred on the server. Our team has been notified. Please try again later.',
                    'error',
                    true,
                    'retry'
                );

            // ====== 503 Service Unavailable ======
            case 503:
                return this.buildCategorizedError(
                    HttpErrorCategory.ServiceUnavailable,
                    503,
                    'Service Unavailable',
                    'The server is temporarily unavailable for maintenance. Please try again in a few moments.',
                    'warning',
                    true,
                    'retry'
                );

            // ====== 504 Gateway Timeout ======
            case 504:
                return this.buildCategorizedError(
                    HttpErrorCategory.GatewayTimeout,
                    504,
                    'Server Not Responding',
                    'The server is not responding. Please try again.',
                    'error',
                    true,
                    'retry'
                );

            // ====== Other 4xx errors ======
            case 405:
            case 410:
            case 429: // Rate limit
                if (status >= 400 && status < 500) {
                    return this.buildCategorizedError(
                        HttpErrorCategory.ClientError,
                        status,
                        'Request Error',
                        this.extractErrorMessage(errorBody, 'Your request could not be processed.'),
                        'warning',
                        status !== 429, // 429 is rate limit, not retryable
                        'retry'
                    );
                }
                break;

            // ====== Other 5xx errors ======
            default:
                if (status >= 500 && status < 600) {
                    return this.buildCategorizedError(
                        HttpErrorCategory.ServerErrorGeneric,
                        status,
                        'Server Error',
                        'A server error occurred. Please try again later.',
                        'error',
                        true,
                        'retry'
                    );
                }
        }

        // Fallback for unmapped status codes
        return this.buildCategorizedError(
            HttpErrorCategory.Unknown,
            status,
            'Unknown Error',
            'An unexpected error occurred. Please try again.',
            'error',
            true,
            'retry'
        );
    }

    /**
     * Build a categorized error object
     *
     * @private
     */
    private buildCategorizedError(
        category: HttpErrorCategory,
        statusCode: number | null,
        userMessage: string,
        technicalMessage: string,
        severity: 'info' | 'warning' | 'error' | 'critical',
        retryable: boolean,
        action?: 'retry' | 'login' | 'refresh' | 'navigate' | 'none'
    ): CategorizedError {
        return {
            category,
            statusCode,
            userMessage,
            technicalMessage,
            severity,
            retryable,
            action: action || 'none'
        };
    }

    /**
     * Extract user-friendly error message from backend response
     *
     * @private
     */
    private extractErrorMessage(errorBody: any, defaultMessage: string): string {
        if (!errorBody) {
            return defaultMessage;
        }

        // Common backend error response formats
        if (typeof errorBody === 'string') {
            return errorBody;
        }

        if (errorBody.message) {
            return errorBody.message;
        }

        if (errorBody.error && typeof errorBody.error === 'string') {
            return errorBody.error;
        }

        if (errorBody.errors && Array.isArray(errorBody.errors)) {
            const firstError = errorBody.errors[0];
            if (typeof firstError === 'string') {
                return firstError;
            }
            if (firstError.message) {
                return firstError.message;
            }
        }

        return defaultMessage;
    }

    /**
     * Check if error is retryable
     *
     * @param category - Error category
     * @returns true if operation should be retryable
     */
    isRetryable(category: HttpErrorCategory): boolean {
        return [
            HttpErrorCategory.NetworkError,
            HttpErrorCategory.Timeout,
            HttpErrorCategory.ServerError,
            HttpErrorCategory.ServiceUnavailable,
            HttpErrorCategory.GatewayTimeout,
            HttpErrorCategory.ServerErrorGeneric
        ].includes(category);
    }

    /**
     * Get appropriate aria-live politeness level based on severity
     *
     * @param severity - Error severity
     * @returns 'polite' or 'assertive'
     */
    getAriaLive(severity: 'info' | 'warning' | 'error' | 'critical'): 'polite' | 'assertive' {
        return severity === 'critical' || severity === 'error' ? 'assertive' : 'polite';
    }

    /**
     * Get appropriate Material Design color based on severity
     *
     * @param severity - Error severity
     * @returns Material color name
     */
    getMaterialColor(severity: 'info' | 'warning' | 'error' | 'critical'): string {
        const colorMap = {
            'info': 'primary',
            'warning': 'warn',
            'error': 'warn',
            'critical': 'warn'
        };
        return colorMap[severity];
    }
}
