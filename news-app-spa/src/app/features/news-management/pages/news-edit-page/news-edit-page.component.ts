import { Component, OnInit, OnDestroy, ChangeDetectorRef, ViewChild } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { SuccessErrorDialogComponent } from 'src/app/shared/components/success-error-dialog/success-error-dialog.component';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute, RouterModule } from '@angular/router';
import { Subject, BehaviorSubject, takeUntil, switchMap, forkJoin } from 'rxjs';

import { NewsFormService } from '../../services/news-form.service';
import { NewsItem } from '../../models/news-item.model';
import { AppMasterDataService, NewsCategory, AdminUser } from 'src/app/core/services/app-master-data.service';
import { CanComponentDeactivate } from 'src/app/core/guards/unsaved-changes.guard';

import { NewsFormComponent } from '../../components/news-form/news-form.component'; // standalone
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { ChangeDetectionStrategy } from '@angular/core';

@Component({
  selector: 'app-news-edit-page',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    CommonModule,
    RouterModule,
    NewsFormComponent,
    MatIconModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './news-edit-page.component.html',
  styleUrl: './news-edit-page.component.css'
})
export class NewsEditPageComponent implements OnInit, OnDestroy, CanComponentDeactivate {
  private destroy$ = new Subject<void>();
  
  @ViewChild(NewsFormComponent) newsFormComponent?: NewsFormComponent;
  
  news: NewsItem | null = null;
  readonly fieldErrors$ = new BehaviorSubject<{ [key: string]: string } | null>(null);
  newsId: string | null = null;

  // Master data for display
  private categories: NewsCategory[] = [];
  private adminUsers: AdminUser[] = [];
  
  categoryName: string | null = null;
  authorName: string | null = null;
  
  readonly isSubmitting$ = new BehaviorSubject<boolean>(false);
  readonly errorState$ = new BehaviorSubject<{ message: string; type: 'error' | 'warning'; canRetry: boolean; timestamp: Date } | null>(null);
  private lastFormData: FormData | null = null;

  constructor(
    private newsService: NewsFormService,
    private router: Router,
    private route: ActivatedRoute,
    private masterDataService: AppMasterDataService,
    private cdr: ChangeDetectorRef,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    this.loadNewsFromRoute();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private loadNewsFromRoute(): void {
    this.isSubmitting$.next(true);
    
    // First, load master data in parallel
    const masterData$ = forkJoin({
      categories: this.masterDataService.getAllCategories(),
      adminUsers: this.masterDataService.getAdminUsers()
    });

    // Then get news ID and load news
    this.route.paramMap.pipe(
      takeUntil(this.destroy$),
      switchMap(params => {
        this.newsId = params.get('id');
        if (!this.newsId) {
          this.router.navigate(['/news']);
          throw new Error('No news ID provided');
        }
        // Load master data first, then news
        return forkJoin({
          masterData: masterData$,
          news: this.newsService.getNewsById(this.newsId)
        });
      })
    ).subscribe({
      next: ({ masterData, news }) => {
        this.isSubmitting$.next(false);
        
        // Store master data
        this.categories = masterData.categories || [];
        this.adminUsers = masterData.adminUsers || [];
        
        if (news) {
          this.news = news;
          
          // Now lookup names from pre-loaded master data
          this.categoryName = this.getCategoryNameById(news.newsNewsCategoryId);
          this.authorName = this.getAdminUserNameById(news.newsCreatedBy);
          
          this.cdr.markForCheck();
        } else {
          this.router.navigate(['/news']);
        }
      },
      error: (error) => {
        this.isSubmitting$.next(false);
        this.setError(error.message || 'Failed to load news', 'error', false);
        this.cdr.markForCheck();
        console.error('Failed to load news:', error);
      }
    });
  }

  /**
   * Get category name by ID from pre-loaded master data
   */
  private getCategoryNameById(categoryId: string | null): string | null {
    if (!categoryId) return null;
    const category = this.categories.find(c => c.id === categoryId);
    return category?.categoryNameEn || 'Unknown';
  }

  /**
   * Get admin user name by ID from pre-loaded master data
   */
  private getAdminUserNameById(userId: string | null): string | null {
    if (!userId) return null;
    const user = this.adminUsers.find(u => u.id === userId);
    return user?.name || 'Unknown';
  }

  onSave(formData: FormData): void {
    if (!this.newsId) {
      return;
    }
    this.lastFormData = formData;
    this.isSubmitting$.next(true);
    this.clearError();
    this.fieldErrors$.next(null);
    // Guard: Only update this.news on success, never on error
    this.newsService.updateNews(this.newsId, formData).subscribe({
      next: (updatedNews: any) => {
        this.isSubmitting$.next(false);
        // Only update the news object if the backend returns new/updated data
        if (updatedNews && typeof updatedNews === 'object') {
          this.news = updatedNews;
        }
        this.fieldErrors$.next(null);
        this.lastFormData = null;
        this.cdr.markForCheck();
        window.scrollTo({ top: 0, behavior: 'smooth' });
        // Show improved success dialog/modal with 3 actions
        const dialogRef = this.dialog.open(SuccessErrorDialogComponent, {
          data: {
            type: 'success',
            title: 'News Updated',
            message: 'The news article was updated successfully.',
            okText: 'Back to News List',
            showCancel: true,
            cancelText: 'Keep Editing',
            extraActions: [
              {
                label: 'View News',
                color: 'accent',
                action: 'view'
              }
            ]
          }
        });
        dialogRef.afterClosed().subscribe((result) => {
          if (result === true) {
            // Back to News List
            this.router.navigate(['/news/admin']);
          } else if (result === 'view') {
            // View single news page
            if (this.newsId) {
              this.router.navigate(['/news', this.newsId]);
            }
          }
          // else: Keep Editing (do nothing)
        });
      },
      error: (error: any) => {
        this.isSubmitting$.next(false);
        // Do NOT update this.news or re-patch the form on error; just set field errors
        // Guard: Do not change this.news here
        this.handleError(error);
        window.scrollTo({ top: 0, behavior: 'smooth' });
      }
    });
  }

  /**
   * Handle update error with user-friendly messages
   * Extracts field-level errors for RFC 7807 compliant responses
   *
   * - Validation Errors: Display ALL inline under fields, NO global banner
   * - Network/Server Errors: Display global banner for retry guidance
   */
  /**
   * Handle update error with user-friendly messages
   * Extracts field-level errors for RFC 7807 compliant responses
   *
   * - Validation Errors: Display ALL inline under fields, NO global banner
   * - Network/Server Errors: Display global banner for retry guidance
   */
  private handleError(error: any): void {
    this.isSubmitting$.next(false);
    // Extract fieldErrors from various possible error formats (copied from create page)
    const fieldErrors = this.extractFieldErrors(error);
    if (fieldErrors && Object.keys(fieldErrors).length > 0) {
      this.fieldErrors$.next(fieldErrors);
      this.errorState$.next(null);
      this.cdr.markForCheck();
      return;
    }
    // STEP 2: Network/Server Errors (Only Global Banner)
    this.fieldErrors$.next(null);
    const message = this.getErrorMessage(error);
    const canRetry = this.isRetryable(error);
    this.setError(message, 'error', canRetry);
    this.cdr.markForCheck();
  }

  /**
   * Extract fieldErrors from error object (handles multiple formats)
   * Supported formats:
   * 1. Direct: error.fieldErrors = { field: "msg" }
   * 2. Wrapped by service: error.errorResponse.data.fieldErrors
   * 3. Wrapped by interceptor: error.originalError.error.data.fieldErrors
   * 4. Not present: returns null
   */


  /**
   * Translate HTTP error to user-friendly message (copied from create page)
   * @param error HTTP error response
   * @returns User-friendly error message
   */
  private getErrorMessage(error: any): string {
    // Network error
    if (error?.status === 0) {
      return 'Network connection failed. Please check your internet connection and try again.';
    }
    // Client errors
    if (error?.status === 400) {
      return 'Please check your input and try again.';
    }
    if (error?.status === 401) {
      return 'Your session expired. Please log in again.';
    }
    if (error?.status === 403) {
      return 'You do not have permission to update news.';
    }
    if (error?.status === 409) {
      return 'This news content already exists.';
    }
    if (error?.status === 413) {
      return 'One or more files are too large. Please reduce image sizes and try again.';
    }
    // Server errors
    if (error?.status >= 500) {
      return 'Server is temporarily unavailable. Please try again later.';
    }
    if (error?.status >= 400) {
      return 'Failed to update news. Please try again.';
    }
    return error?.message || 'An unexpected error occurred. Please try again.';
  }

  /**
   * Extract fieldErrors from error object (handles multiple formats, matches create page)
   * Supported formats:
   * 1. Direct: error.fieldErrors = { field: "msg" }
   * 2. Wrapped by service: error.errorResponse.data.fieldErrors
   * 3. Wrapped by interceptor: error.originalError.error.data.fieldErrors
   * 4. error.data.fieldErrors (sometimes backend returns this)
   * 5. Not present: returns null
   */
  private extractFieldErrors(error: any): { [key: string]: string } | null {
    // Helper to filter out global keys
    const filterFieldErrors = (obj: any) => {
      if (!obj || typeof obj !== 'object') return null;
      const filtered: { [key: string]: string } = {};
      for (const key of Object.keys(obj)) {
        if (key !== 'message' && key !== 'timestamp') {
          filtered[key] = obj[key];
        }
      }
      return Object.keys(filtered).length > 0 ? filtered : null;
    };
    // Format 1: Direct fieldErrors
    if (error?.fieldErrors && typeof error.fieldErrors === 'object') {
      return filterFieldErrors(error.fieldErrors);
    }
    // Format 2: Service wrapped format
    if (error?.errorResponse?.data?.fieldErrors && typeof error.errorResponse.data.fieldErrors === 'object') {
      return filterFieldErrors(error.errorResponse.data.fieldErrors);
    }
    // Format 3: Interceptor wrapped format
    if (error?.originalError?.error?.data?.fieldErrors && typeof error.originalError.error.data.fieldErrors === 'object') {
      return filterFieldErrors(error.originalError.error.data.fieldErrors);
    }
    // Format 4: error.data.fieldErrors (sometimes backend returns this)
    if (error?.data?.fieldErrors && typeof error.data.fieldErrors === 'object') {
      return filterFieldErrors(error.data.fieldErrors);
    }
    // Format 5: Not found
    return null;
  }


  onCancel(): void {
    if (this.lastFormData) {
      this.lastFormData = null;
    }
    this.router.navigate(['/news']);
  }

  onDelete(): void {
    if (!this.newsId || !this.news) {
      return;
    }
    if (confirm(`Are you sure you want to delete "${this.news.newsTitleEn}"? This action cannot be undone.`)) {
      this.newsService.deleteNews(this.newsId).subscribe({
        next: () => {
          this.router.navigate(['/news']);
        },
        error: (error: any) => {
          this.setError('Failed to delete news', 'error', false);
          console.error('Failed to delete news:', error);
        }
      });
    }
  }
  /**
   * Retry the last failed submission
   */
  onRetry(): void {
    if (this.lastFormData) {
      this.onSave(this.lastFormData);
      this.cdr.markForCheck();
    }
  }

  /**
   * Clear error state
   */
  clearError(): void {
    this.errorState$.next(null);
    this.cdr.markForCheck();
  }
  /**
   * Determine if an error is retryable (copied from create page)
   * @param error HTTP error response
   * @returns True if retry is recommended
   */
  private isRetryable(error: any): boolean {
    // Retryable errors: network errors and server errors
    return error?.status === 0 || error?.status >= 500;
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
   * CanDeactivate guard implementation
   * Prevents navigation if form has unsaved changes
   * 
   * @returns true if safe to navigate away, false if form has unsaved changes
   */
  canDeactivate(): boolean {
    return !this.newsFormComponent?.hasUnsavedChanges();
  }
}