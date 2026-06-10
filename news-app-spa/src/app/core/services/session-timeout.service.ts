import { Injectable, NgZone } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from './auth.service';
import { ErrorService } from './error.service';

/**
 * 🔐 SESSION TIMEOUT SERVICE
 *
 * Industry-standard session management with:
 * 1. **Inactivity timeout** - Auto-logout after X minutes of no user activity
 * 2. **Warning notification** - Notify user 1 minute before logout
 * 3. **Activity monitoring** - Track mouse, keyboard, and touch events
 * 4. **Persistent session check** - Monitor token validity in real-time
 *
 * Configuration:
 * - SESSION_TIMEOUT: 30 minutes (standard for banking/admin apps)
 * - WARNING_TIME: 1 minute before timeout
 * - REFRESH_CHECK_INTERVAL: Check token every 5 minutes
 */
@Injectable({ providedIn: 'root' })
export class SessionTimeoutService {
  // Default timeouts (configurable)
  private readonly SESSION_TIMEOUT_MS = 30 * 60 * 1000; // 30 minutes
  private readonly WARNING_TIME_MS = 1 * 60 * 1000; // 1 minute before timeout
  private readonly TOKEN_CHECK_INTERVAL_MS = 5 * 60 * 1000; // Check token every 5 minutes

  // State tracking
  private activityTimeout: ReturnType<typeof setTimeout> | null = null;
  private warningTimeout: ReturnType<typeof setTimeout> | null = null;
  private tokenCheckInterval: ReturnType<typeof setInterval> | null = null;
  private isWarningShown = false;
  private isSessionActive = false;

  constructor(
    private authService: AuthService,
    private errorService: ErrorService,
    private router: Router,
    private ngZone: NgZone
  ) {}

  /**
   * Initialize session monitoring
   * - Start tracking user activity
   * - Start monitoring token validity
   * - Setup inactivity logout
   */
  initialize(): void {
    if (this.isSessionActive) {
      console.warn('⚠️ Session monitoring already initialized');
      return;
    }

    if (!this.authService.getToken()) {
      console.log('ℹ️ No token found, skipping session monitoring');
      return;
    }

    this.isSessionActive = true;
    console.log('✅ Session monitoring initialized (30 min timeout, 1 min warning)');

    // Run outside Angular zone for better performance
    this.ngZone.runOutsideAngular(() => {
      this.setupActivityMonitoring();
      this.setupTokenCheckInterval();
      this.resetActivityTimeout();
    });
  }

  /**
   * Destroy session monitoring
   * - Clear all timeouts and intervals
   * - Stop activity tracking
   */
  destroy(): void {
    this.isSessionActive = false;
    this.clearAllTimeouts();
    console.log('ℹ️ Session monitoring stopped');
  }

  /**
   * Reset activity timeout on user action
   * Called whenever user interacts with the page
   */
  private resetActivityTimeout(): void {
    // Clear existing timeouts
    if (this.activityTimeout) clearTimeout(this.activityTimeout);
    if (this.warningTimeout) clearTimeout(this.warningTimeout);
    this.isWarningShown = false;

    // Set warning timer (30 min - 1 min = 29 min)
    this.warningTimeout = setTimeout(() => {
      this.showSessionWarning();
    }, this.SESSION_TIMEOUT_MS - this.WARNING_TIME_MS);

    // Set logout timer (30 min)
    this.activityTimeout = setTimeout(() => {
      this.handleSessionTimeout();
    }, this.SESSION_TIMEOUT_MS);
  }

  /**
   * Setup activity monitoring
   * Track user interactions and reset timeout on activity
   */
  private setupActivityMonitoring(): void {
    const events = ['mousedown', 'keydown', 'scroll', 'touchstart', 'click'];
    let isActivityCooldown = false;

    const handleActivity = () => {
      // Debounce: only process activity every 5 seconds to avoid excessive resets
      if (isActivityCooldown) return;

      isActivityCooldown = true;
      setTimeout(() => {
        isActivityCooldown = false;
      }, 5000);

      // Run reset inside Angular zone for timer operations
      this.ngZone.run(() => {
        this.resetActivityTimeout();
      });
    };

    // Add event listeners
    events.forEach((event) => {
      window.addEventListener(event, handleActivity, { passive: true });
    });
  }

  /**
   * Setup periodic token validity check
   * Proactively detect if token has expired (from backend)
   */
  private setupTokenCheckInterval(): void {
    this.tokenCheckInterval = setInterval(() => {
      this.ngZone.run(() => {
        const token = this.authService.getToken();
        if (!token) {
          // Token was cleared (user logged out)
          this.destroy();
          return;
        }
      });
    }, this.TOKEN_CHECK_INTERVAL_MS);
  }

  /**
   * Show session expiration warning
   * Notify user that session will expire in 1 minute
   */
  private showSessionWarning(): void {
    if (this.isWarningShown) return;

    this.isWarningShown = true;

    this.ngZone.run(() => {
      this.errorService.show(
        'Your session will expire in 1 minute due to inactivity. Move your mouse or click to continue.',
        'warning',
        'Session Expiring Soon'
      );

      console.warn('⚠️ Session expiration warning shown to user');
    });
  }

  /**
   * Handle session timeout
   * - Clear auth data
   * - Navigate to login
   * - Show error message
   */
  private handleSessionTimeout(): void {
    this.ngZone.run(() => {
      console.error('❌ Session timeout: User was inactive for 30 minutes');

      this.errorService.show(
        'Your session has expired due to inactivity. Please log in again.',
        'error',
        'Session Expired'
      );

      // Immediately redirect to login
      this.authService.clearAuthStorage();
      this.router.navigate(['/auth/login']).catch((err) => {
        console.error('Navigation to login failed:', err);
      });

      this.destroy();
    });
  }

  /**
   * Clear all active timeouts and intervals
   */
  private clearAllTimeouts(): void {
    if (this.activityTimeout) clearTimeout(this.activityTimeout);
    if (this.warningTimeout) clearTimeout(this.warningTimeout);
    if (this.tokenCheckInterval) clearInterval(this.tokenCheckInterval);

    this.activityTimeout = null;
    this.warningTimeout = null;
    this.tokenCheckInterval = null;
  }

  /**
   * Check if session is currently active
   */
  isActive(): boolean {
    return this.isSessionActive;
  }
}
