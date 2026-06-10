import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, switchMap } from 'rxjs/operators';
import { AuthService } from '../services/auth.service';
import { ErrorService } from '../../core/services/error.service';
import { Router } from '@angular/router';
import { JwtHelperService } from '../services/jwt-helper.service';

/**
 * 🔐 AUTH INTERCEPTOR - Industry Standard Implementation
 * 
 * Responsibilities:
 * 1. Attach auth token to all requests
 * 2. **Proactively check token expiration before each request**
 * 3. Handle 401 responses by attempting token refresh
 * 4. Distinguish between:
 *    - Login page 401 = Invalid credentials (pass to login component)
 *    - Authenticated page 401 = Session expired (show error & logout)
 * 5. Handle 403 Forbidden responses
 */
@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  constructor(
    private authService: AuthService,
    private errorService: ErrorService,
    private router: Router,
    private jwtHelper: JwtHelperService
  ) {}

  /**
   * Intercepts HTTP requests to attach token and handle auth errors
   * Includes proactive token expiration checking
   */
  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const token = this.authService.getToken();
    
    // ============================================================
    // PROACTIVE TOKEN VALIDATION
    // Check if token is expired BEFORE making the request
    // This prevents unnecessary API calls with invalid tokens
    // ============================================================
    if (token && this.jwtHelper.isTokenExpired(token)) {
      console.warn('🔴 Interceptor: Token expired proactively detected');
      this.handleSessionExpired();
      return throwError(() => new Error('Session expired'));
    }

    let authReq = req;
    
    if (token) {
      authReq = req.clone({
        setHeaders: { Authorization: `Bearer ${token}` }
      });
    }
    
    return next.handle(authReq).pipe(
      catchError((error: HttpErrorResponse) => {
        // Log error for debugging
        console.error('❌ HTTP Error:', error.status, error.error?.message);

        // ============================================================
        // REACTIVE ERROR HANDLING
        // Handle actual 401 errors from server
        // ============================================================
        if (error.status === 401) {
          // If on login page, let login component handle the error (invalid credentials)
          if (this.isLoginPage()) {
            console.warn('⚠️ Interceptor: 401 on login page (invalid credentials)');
            return throwError(() => error);
          }

          // For authenticated routes: attempt token refresh
          const refreshToken = this.authService.getRefreshToken();
          
          if (refreshToken) {
            console.warn('🔄 Interceptor: Attempting token refresh...');
            // Try to refresh the token
            return this.authService.refreshToken().pipe(
              switchMap((response: any) => {
                const newToken = response?.data?.accessToken;
                // Validate token refresh response
                if (newToken && response?.data) {
                  // Token refresh successful - retry original request
                  console.log('✅ Interceptor: Token refreshed successfully');
                  this.authService.setToken(newToken);
                  const retryReq = req.clone({
                    setHeaders: { Authorization: `Bearer ${newToken}` }
                  });
                  return next.handle(retryReq);
                } else {
                  // Token refresh returned no token (session expired)
                  console.warn('⚠️ Interceptor: Token refresh failed (no token in response)');
                  this.handleSessionExpired();
                  return throwError(() => error);
                }
              }),
              catchError((err) => {
                // Token refresh failed (session expired)
                console.error('❌ Interceptor: Token refresh failed:', err);
                this.handleSessionExpired();
                return throwError(() => error);
              })
            );
          } else {
            // No refresh token available (session expired, not logged in)
            console.warn('⚠️ Interceptor: No refresh token available (session expired)');
            this.handleSessionExpired();
            return throwError(() => error);
          }
        }

        return throwError(() => error);
      })
    );
  }

  /**
   * Check if current route is login page
   */
  private isLoginPage(): boolean {
    return this.router.url.includes('/auth/login');
  }

  /**
   * Handle session expiration - show toast and logout synchronously
   */
  private handleSessionExpired(): void {
    this.errorService.show(
      'Session expired. Please log in again.',
      'error',
      'Session Expired'
    );
    // Synchronously clear auth and navigate to prevent race conditions
    this.authService.clearAuthStorage();
    this.router.navigate(['/auth/login']).catch((err) => {
      console.error('Navigation to login failed:', err);
    });
  }
}
