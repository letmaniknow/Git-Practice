/**
 * GlobalErrorInterceptor
 * Centralized HTTP error handling for all API requests
 *
 * RESPONSIBILITY: Catch and standardize all HTTP errors
 * BENEFITS:
 * - Prevents duplicate error handling in services
 * - Consistent error responses across features
 * - Centralized logging and analytics
 * - Automatic error message generation
 * - Separation of concerns (HTTP errors vs business logic)
 *
 * ERROR HANDLING FLOW:
 * 1. HTTP request fails
 * 2. GlobalErrorInterceptor catches error
 * 3. Converts to IApiError
 * 4. Categorizes error (client vs server vs network)
 * 5. Generates user-friendly message
 * 6. Logs error for debugging
 * 7. Passes error to ErrorService for display
 * 8. Re-throws error for service-level handling if needed
 *
 * @category CoreInterceptors
 * @see ErrorService
 * @see IApiError
 */

import { Injectable } from '@angular/core';
import {
  HttpInterceptor,
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpErrorResponse,
} from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';

import { ErrorService } from '../services/error.service';
import { IApiError } from '../models/error.models';
import { ERROR_MESSAGES, HTTP_STATUS_MESSAGES } from '../../shared/constants/error.constants';

@Injectable()
export class GlobalErrorInterceptor implements HttpInterceptor {
  constructor(private errorService: ErrorService) {}

  /**
   * Intercept all HTTP requests and handle errors
   */
  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(req).pipe(
      catchError((error: HttpErrorResponse) => {
        // Convert HTTP error to IApiError
        const apiError = this.handleError(error, req);

        // Log error for debugging
        console.error('[GlobalErrorInterceptor]', {
          statusCode: apiError.statusCode,
          message: apiError.message,
          context: apiError.context,
          url: req.url,
          method: req.method,
          timestamp: new Date().toISOString(),
        });

        // Determine if error should be shown to user
        // 🎯 CRITICAL: Don't show 409 Conflict errors (field validation errors) at interceptor level
        //    These contain fieldErrors that should be shown inline by components
        //    Only show them if there are NO fieldErrors (network/generic error)
        const isFieldValidationError = error.status === 409 && error.error?.data?.fieldErrors;
        
        if (!this.shouldSuppressError(req) && !isFieldValidationError) {
          // Extract feature context from URL for better error messages
          const context = this.extractContextFromUrl(req.url);
          apiError.context = context;

          // Show error to user via ErrorService
          this.errorService.show(apiError.message, 'error');
        }

        // Re-throw error for service-level error handling if needed
        return throwError(() => apiError);
      })
    );
  }

  /**
   * Convert HttpErrorResponse to IApiError
   * Categorize error and generate user-friendly message
   */
  private handleError(error: HttpErrorResponse, req: HttpRequest<any>): IApiError {
    let message: string;
    let retryable: boolean = false;
    let errorCode = 'UNKNOWN_ERROR';

    if (error.status === 0) {
      // Network error (no response from server)
      message = ERROR_MESSAGES.NETWORK_ERROR;
      retryable = true;
      errorCode = 'NETWORK_ERROR';
    } else if (error.status === 401) {
      // Unauthorized
      message = ERROR_MESSAGES.UNAUTHORIZED;
      retryable = false;
      errorCode = 'UNAUTHORIZED';
    } else if (error.status === 403) {
      // Forbidden
      message = ERROR_MESSAGES.FORBIDDEN;
      retryable = false;
      errorCode = 'FORBIDDEN';
    } else if (error.status === 404) {
      // Not found
      message = ERROR_MESSAGES.NOT_FOUND;
      retryable = false;
      errorCode = 'NOT_FOUND';
    } else if (error.status >= 500) {
      // Server error - retryable
      message = HTTP_STATUS_MESSAGES[error.status] || ERROR_MESSAGES.SERVER_ERROR;
      retryable = true;
      errorCode = `SERVER_ERROR_${error.status}`;
    } else if (error.status >= 400) {
      // Client error (4xx except 401, 403, 404)
      message = HTTP_STATUS_MESSAGES[error.status] || ERROR_MESSAGES.VALIDATION_ERROR;
      retryable = false;
      errorCode = `CLIENT_ERROR_${error.status}`;
    } else {
      // Unknown error
      message = ERROR_MESSAGES.GENERIC_ERROR;
      retryable = false;
      errorCode = 'UNKNOWN_ERROR';
    }

    return {
      message,
      statusCode: error.status,
      retryable,
      errorCode,
      originalError: error,
      timestamp: new Date(),
      context: this.extractContextFromUrl(req.url),
    };
  }

  /**
   * Extract feature context from API URL
   * Used for better error messages and logging
   * Example: '/api/v1/admin/news/categories' → 'news-categories'
   */
  private extractContextFromUrl(url: string): string {
    try {
      const parts = url.split('/').filter((p) => p && !p.startsWith('{'));
      // Remove protocol, domain, api, v1, admin
      const cleaned = parts.slice(3);
      return cleaned.slice(-2).join('-').toLowerCase();
    } catch {
      return 'unknown-context';
    }
  }

  /**
   * Determine if error should be suppressed
   * Some errors should not show user notifications (handled silently at service level)
   */
  private shouldSuppressError(req: HttpRequest<any>): boolean {
    // Don't show errors for OPTIONS requests (CORS preflight)
    if (req.method === 'OPTIONS') {
      return true;
    }

    // Can add custom logic here for specific URLs or request types
    // Example: suppress errors for specific endpoints that handle errors locally
    const suppressedUrls: string[] = [
      // Add URLs here that should suppress global error notification
      // Example: '/api/v1/auth/refresh-token' (handled by auth interceptor)
    ];

    return suppressedUrls.some((url) => req.url.includes(url));
  }
}
