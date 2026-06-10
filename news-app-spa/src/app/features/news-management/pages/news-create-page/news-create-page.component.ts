import { Component, OnInit, OnDestroy, ChangeDetectionStrategy, ChangeDetectorRef, ViewChild } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Subject, BehaviorSubject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

import { NewsFormComponent } from '../../components/news-form/news-form.component';
import { NewsFormService } from '../../services/news-form.service';
import { CanComponentDeactivate } from 'src/app/core/guards/unsaved-changes.guard';

/** Error state interface for proper type safety */
interface ErrorState {
  message: string;
  type: 'error' | 'warning';
  canRetry: boolean;
  timestamp: Date;
}

@Component({
  selector: 'app-news-create-page',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule, RouterModule, NewsFormComponent],
  template: `
    <main class="create-page" role="main">
      <!-- Header Bar - Compact Industry Standard -->
      <header class="page-header-bar">
        <a 
          routerLink="/news" 
          class="back-btn"
          aria-label="Go back to news list">
          <span class="back-icon" aria-hidden="true">←</span>
          Back
        </a>
        <h1 class="page-title">Create News Article</h1>
      </header>
      
      <!-- Error Banner -->
      <div 
        *ngIf="errorState$ | async as error" 
        class="error-banner" 
        role="alert"
        aria-live="assertive">
        <div class="error-content">
          <span class="error-icon">⚠️</span>
          <p class="error-message">{{ error.message }}</p>
          <button 
            *ngIf="error.canRetry"
            class="error-retry-btn"
            (click)="onRetry()"
            aria-label="Retry creating news">
            Retry
          </button>
          <button 
            class="error-close-btn"
            (click)="clearError()"
            aria-label="Close error message">
            ✕
          </button>
        </div>
      </div>
      
      <app-news-form 
        [isEditMode]="false"
        [isSubmitting]="(isSubmitting$ | async) ?? false"
        [fieldErrors]="(fieldErrors$ | async) ?? null"
        (save)="onSave($event)"
        (cancel)="onCancel()"
        aria-label="News creation form">
      </app-news-form>
    </main>
  `,
  styles: [`
    /* Compact Industry-Standard Layout */
    .create-page {
      width: 100%;
      max-width: 100%;
      padding: 0;
      margin: 0;
    }

    /* Header Bar - Horizontal Layout */
    .page-header-bar {
      display: flex;
      align-items: center;
      gap: 1rem;
      padding: 0.75rem 1.5rem;
      background: #ffffff;
      border-bottom: 1px solid #e5e7eb;
      margin-bottom: 1rem;
    }

    .back-btn {
      display: flex;
      align-items: center;
      gap: 0.375rem;
      padding: 0.5rem 0.75rem;
      background: #f3f4f6;
      border: 1px solid #d1d5db;
      border-radius: 6px;
      color: #374151;
      text-decoration: none;
      font-size: 0.875rem;
      font-weight: 500;
      transition: all 0.2s;
      white-space: nowrap;
    }

    .back-btn:hover {
      background: #e5e7eb;
      border-color: #9ca3af;
      color: #1f2937;
    }

    .back-icon {
      font-size: 1rem;
      line-height: 1;
    }

    .page-title {
      font-size: 1.5rem;
      font-weight: 600;
      color: #111827;
      margin: 0;
      line-height: 1.2;
    }
    
    .breadcrumb-link {


    /* Error Banner */
    .error-banner {
      margin: 0 1.5rem 1.5rem 1.5rem;
      animation: slideDown 0.3s ease-out;
    }

    /* Form Container */
    app-news-form {
      display: block;
      padding: 0 1.5rem 2rem 1.5rem;
    }

    @keyframes slideDown {
      from {
        opacity: 0;
        transform: translateY(-10px);
      }
      to {
        opacity: 1;
        transform: translateY(0);
      }
    }

    .error-content {
      background: #fef2f2;
      border: 1px solid #fecaca;
      border-radius: 8px;
      padding: 1rem;
      display: flex;
      align-items: flex-start;
      gap: 0.75rem;
      color: #991b1b;
    }

    .error-icon {
      font-size: 1.25rem;
      flex-shrink: 0;
    }

    .error-message {
      flex: 1;
      font-weight: 500;
      margin: 0;
      line-height: 1.5;
    }

    .error-retry-btn,
    .error-close-btn {
      background: none;
      border: none;
      color: #991b1b;
      cursor: pointer;
      padding: 0.5rem 0.75rem;
      border-radius: 4px;
      font-weight: 600;
      transition: background-color 0.2s ease;
      flex-shrink: 0;
    }

    .error-retry-btn:hover,
    .error-close-btn:hover {
      background: #fecaca;
    }

    .error-retry-btn:focus-visible,
    .error-close-btn:focus-visible {
      outline: 2px solid #991b1b;
      outline-offset: 1px;
    }

    @media (max-width: 768px) {
      .page-header-bar {
        padding: 0.625rem 1rem;
      }

      .page-title {
        font-size: 1.25rem;
      }

      .back-btn {
        padding: 0.375rem 0.625rem;
        font-size: 0.8125rem;
      }

      .error-banner {
        margin: 0 1rem 1rem 1rem;
      }

      app-news-form {
        padding: 0 1rem 1rem 1rem;

      .error-content {
        flex-direction: column;
        align-items: stretch;
      }

      .error-retry-btn,
      .error-close-btn {
        align-self: flex-end;
      }
    }
  `]
})
export class NewsCreatePageComponent implements OnInit, OnDestroy, CanComponentDeactivate {
  @ViewChild(NewsFormComponent) newsFormComponent?: NewsFormComponent;
  
  /** Observable for submission state (BehaviorSubject defaults to false) */
  readonly isSubmitting$ = new BehaviorSubject<boolean>(false);

  /** Observable for error state */
  readonly errorState$ = new BehaviorSubject<ErrorState | null>(null);

  /** Observable for field-level errors from backend (RFC 7807) */
  readonly fieldErrors$ = new BehaviorSubject<{ [key: string]: string } | null>(null);

  /** Last submitted form data for retry mechanism */
  private lastFormData: FormData | null = null;

  /** Subject for managing subscription cleanup */
  private readonly destroy$ = new Subject<void>();

  constructor(
    private newsService: NewsFormService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.testBackendConnection();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  /**
   * Test backend connectivity on component initialization
   * Logs warning if backend is unreachable but doesn't block UI
   */
  private testBackendConnection(): void {
    this.newsService.testConnection()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        error: (error: HttpErrorResponse) => {
          const message = this.getConnectionErrorMessage(error);
          this.setError(message, 'warning', false);
        }
      });
  }

  /**
   * Handle form submission
   * @param formData Form data from news-form component
   */
  onSave(formData: FormData): void {
    this.lastFormData = formData;
    this.isSubmitting$.next(true);
    this.clearError();
    this.fieldErrors$.next(null);

    this.newsService.createNews(formData)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.handleSuccess();
        },
        error: (error: any) => {
          console.log('🔴🔴🔴 COMPONENT ERROR HANDLER CALLED 🔴🔴🔴');
          console.log('Error received in component:', error);
          console.log('Error type:', typeof error);
          console.log('Error constructor:', error?.constructor?.name);
          console.log('Error keys:', Object.keys(error || {}));
          console.log('Error.fieldErrors:', error?.fieldErrors);
          console.log('Error.message:', error?.message);
          console.log('Full error object:', error);
          this.handleError(error);
        }
      });
  }

  /**
   * Handle successful news creation
   */
  private handleSuccess(): void {
    this.isSubmitting$.next(false);
    this.fieldErrors$.next(null);
    this.lastFormData = null;
    this.router.navigate(['/news'], { queryParams: { created: 'true' } });
  }

  /**
   * Handle creation error with user-friendly messages
   * Extracts field-level errors for RFC 7807 compliant responses
   * 
   * INDUSTRY STANDARD ERROR HANDLING:
   * - Validation Errors (409): Display ALL inline under fields, NO global banner
   * - Network/Server Errors: Display global banner for retry guidance
   * 
   * Pattern: Google Forms, Stripe, AWS Console all follow this approach
   * 
   * @param error HTTP error response or enhanced error with fieldErrors
   */
  private handleError(error: any): void {
    this.isSubmitting$.next(false);
    
    console.log('🔴 ERROR HANDLER: Processing error...');
    console.log('Error status:', error?.statusCode || error?.status);
    console.log('Error constructor:', error?.constructor?.name);
    
    // Extract fieldErrors from various possible error formats
    const fieldErrors = this.extractFieldErrors(error);
    
    // STEP 1: Validation Errors (Multiple Fields)
    // ═══════════════════════════════════════════════════════════════════════════
    if (fieldErrors && Object.keys(fieldErrors).length > 0) {
      console.log('✅ VALIDATION ERROR - Multiple field errors detected');
      console.log('Fields with errors:', Object.keys(fieldErrors));
      console.log('Field errors map:', fieldErrors);
      
      // Display all errors inline under their respective fields
      this.fieldErrors$.next(fieldErrors);
      this.cdr.markForCheck();
      
      // ⚠️ CRITICAL INDUSTRY STANDARD:
      // DO NOT show global error banner for validation errors
      // Users see errors exactly where they need to fix them
      this.errorState$.next(null);
      console.log('✅ All field errors queued for inline display');
      return;
    }
    
    // STEP 2: Network/Server Errors (Only Global Banner)
    // ═══════════════════════════════════════════════════════════════════════════
    console.log('⚠️ NETWORK/SERVER ERROR - showing global banner');
    this.fieldErrors$.next(null);
    const message = error?.message || this.getErrorMessage(error as HttpErrorResponse);
    const canRetry = this.isRetryable(error as HttpErrorResponse);
    this.setError(message, 'error', canRetry);
  }

  /**
   * Extract fieldErrors from error object (handles multiple formats)
   * 
   * Supported formats:
   * 1. Direct: error.fieldErrors = { field: "msg" }
   * 2. Wrapped by service: error.errorResponse.data.fieldErrors
   * 3. Wrapped by interceptor: error.originalError.error.data.fieldErrors
   * 4. Not present: returns null
   * 
   * @param error Error object from any layer
   * @returns Field errors map or null
   */
  private extractFieldErrors(error: any): { [key: string]: string } | null {
    // Format 1: Direct fieldErrors
    if (error?.fieldErrors && typeof error.fieldErrors === 'object') {
      console.log('📍 fieldErrors format 1 (direct): Found');
      return error.fieldErrors;
    }
    
    // Format 2: Service wrapped format
    if (error?.errorResponse?.data?.fieldErrors && typeof error.errorResponse.data.fieldErrors === 'object') {
      console.log('📍 fieldErrors format 2 (service wrapped): Found');
      return error.errorResponse.data.fieldErrors;
    }
    
    // Format 3: Interceptor wrapped format
    if (error?.originalError?.error?.data?.fieldErrors && typeof error.originalError.error.data.fieldErrors === 'object') {
      console.log('📍 fieldErrors format 3 (interceptor wrapped): Found');
      return error.originalError.error.data.fieldErrors;
    }
    
    // Format 4: Not found
    console.log('📍 No fieldErrors found in error object');
    return null;
  }

  /**
   * Retry the last failed submission
   */
  onRetry(): void {
    if (this.lastFormData) {
      this.onSave(this.lastFormData);
    }
  }

  /**
   * Clear error state
   */
  clearError(): void {
    this.errorState$.next(null);
  }

  /**
   * Set error state with user-friendly message
   * @param message Error message
   * @param type Error type
   * @param canRetry Whether retry is possible
   */
  private setError(message: string, type: 'error' | 'warning', canRetry: boolean): void {
    this.errorState$.next({
      message,
      type,
      canRetry,
      timestamp: new Date()
    });
    this.cdr.markForCheck();
  }

  /**
   * Navigate back to news list
   */
  onCancel(): void {
    if (this.lastFormData) {
      this.lastFormData = null;
    }
    this.router.navigate(['/news']);
  }

  /**
   * Translate HTTP error to user-friendly message
   * @param error HTTP error response
   * @returns User-friendly error message
   */
  private getErrorMessage(error: HttpErrorResponse): string {
    // Network error
    if (error.status === 0) {
      return 'Network connection failed. Please check your internet connection and try again.';
    }

    // Client errors
    if (error.status === 400) {
      return 'Please check your input and try again.';
    }

    if (error.status === 401) {
      return 'Your session expired. Please log in again.';
    }

    if (error.status === 403) {
      return 'You do not have permission to create news.';
    }

    if (error.status === 409) {
      return 'This news content already exists.';
    }

    if (error.status === 413) {
      return 'One or more files are too large. Please reduce image sizes and try again.';
    }

    // Server errors
    if (error.status >= 500) {
      return 'Server is temporarily unavailable. Please try again later.';
    }

    if (error.status >= 400) {
      return 'Failed to create news. Please try again.';
    }

    return 'An unexpected error occurred. Please try again.';
  }

  /**
   * Get connection error message
   * @param error HTTP error response
   * @returns Connection error message
   */
  private getConnectionErrorMessage(error: HttpErrorResponse): string {
    if (error.status === 0) {
      return 'Unable to connect to server. Some features may not work properly.';
    }
    return 'Server connection warning. Please ensure all services are running.';
  }

  /**
   * Determine if an error is retryable
   * @param error HTTP error response
   * @returns True if retry is recommended
   */
  private isRetryable(error: HttpErrorResponse): boolean {
    // Retryable errors: network errors and server errors
    return error.status === 0 || error.status >= 500;
  }

  /**
   * CanDeactivate guard implementation
   * Prevents navigation if form has unsaved changes
   * 
   * @returns true if safe to navigate away, false if form has unsaved changes
   */
  canDeactivate(): boolean {
    return !this.newsFormComponent?.hasUnsavedChanges();
  }
}