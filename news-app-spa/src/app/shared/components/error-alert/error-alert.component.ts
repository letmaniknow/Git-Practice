/**
 * ErrorAlertComponent
 * Reusable error alert card component with retry functionality
 *
 * ARCHITECT PATTERN: Dumb/Presentational Component
 * ✅ NO business logic
 * ✅ NO API calls
 * ✅ Pure Input/Output pattern
 * ✅ Reusable across all features
 *
 * SINGLE SOURCE OF TRUTH: Error display and styling
 * Replaces duplicated error cards across all feature templates
 *
 * USAGE:
 * ```typescript
 * isLoadingCategories = false;
 * categoriesError = 'Failed to load categories...';
 *
 * loadCategories(): void {
 *   // load logic
 * }
 * ```
 *
 * ```html
 * <app-error-alert
 *   *ngIf="categoriesError"
 *   [error]="categoriesError"
 *   [isRetryLoading]="isLoadingCategories"
 *   (onRetry)="loadCategories()"
 *   icon="category"
 * ></app-error-alert>
 * ```
 *
 * @category SharedComponents
 * @module ErrorAlertComponent
 */

import { Component, Input, Output, EventEmitter, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-error-alert',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatButtonModule, MatIconModule],
  template: `
    <mat-card
      class="error-alert"
      [class.error-alert--loading]="isRetryLoading"
      role="alert"
      aria-live="polite"
      [attr.aria-busy]="isRetryLoading"
    >
      <mat-card-content class="error-alert__content">
        <!-- Error Icon -->
        <mat-icon class="error-alert__icon" [attr.aria-hidden]="true">
          {{ icon || 'error' }}
        </mat-icon>

        <!-- Error Message -->
        <div class="error-alert__message">
          <!-- Optional Title -->
          <strong class="error-alert__title" *ngIf="title">{{ title }}</strong>
          <!-- Main Message -->
          <span class="error-alert__text">{{ error }}</span>
        </div>

        <!-- Action Buttons Container -->
        <div class="error-alert__actions">
          <!-- Retry Button (Optional) -->
          <button
            *ngIf="showRetry"
            mat-stroked-button
            class="error-alert__retry-button"
            (click)="onRetryClicked()"
            [disabled]="isRetryLoading"
            [attr.aria-label]="'Retry ' + (error || 'loading')"
          >
            <mat-icon *ngIf="isRetryLoading">hourglass_empty</mat-icon>
            <span>{{ isRetryLoading ? retryLoadingLabel : retryLabel }}</span>
          </button>

          <!-- Dismiss Button (Optional) -->
          <button
            *ngIf="showDismiss"
            mat-icon-button
            class="error-alert__dismiss-button"
            (click)="onDismissClicked()"
            [attr.aria-label]="'Dismiss error'"
          >
            <mat-icon>close</mat-icon>
          </button>
        </div>
      </mat-card-content>
    </mat-card>
  `,
  styles: [
    `
      .error-alert {
        border-left: 5px solid #b91c1c !important;
        background-color: #fee2e2 !important;
        margin-bottom: 16px !important;
        border-radius: 8px !important;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1) !important;
        transition: all 0.2s ease !important;
        overflow: hidden !important;
        position: relative !important;
      }

      .error-alert:hover:not(.error-alert--loading) {
        box-shadow: 0 4px 12px rgba(185, 28, 28, 0.15) !important;
      }

      .error-alert--loading {
        opacity: 0.85 !important;
      }

      .error-alert__content {
        display: flex !important;
        flex-direction: row !important;
        align-items: center !important;
        justify-content: space-between !important;
        gap: 16px !important;
        padding: 16px 20px !important;
      }

      @media (max-width: 600px) {
        .error-alert__content {
          flex-direction: column !important;
          align-items: flex-start !important;
          gap: 12px !important;
        }
      }

      .error-alert__icon {
        color: #b91c1c !important;
        font-size: 24px !important;
        width: 24px !important;
        height: 24px !important;
        min-width: 24px !important;
        min-height: 24px !important;
        display: flex !important;
        align-items: center !important;
        justify-content: center !important;
        flex-shrink: 0 !important;
      }

      .error-alert__message {
        flex: 1 !important;
        min-width: 0 !important;
        display: flex !important;
        flex-direction: column !important;
        gap: 4px !important;
      }

      .error-alert__title {
        display: block !important;
        color: #7f1d1d !important;
        font-size: 15px !important;
        font-weight: 700 !important;
        line-height: 1.3 !important;
        word-break: break-word !important;
      }

      .error-alert__text {
        display: block !important;
        color: #991b1b !important;
        font-size: 13px !important;
        font-weight: 500 !important;
        line-height: 1.5 !important;
        word-break: break-word !important;
      }

      .error-alert__actions {
        display: flex !important;
        gap: 12px !important;
        align-items: center !important;
        flex-shrink: 0 !important;
      }

      .error-alert__retry-button {
        border: 1px solid #b91c1c !important;
        color: #b91c1c !important;
        background-color: transparent !important;
        white-space: nowrap !important;
        min-width: 100px !important;
        flex-shrink: 0 !important;
        font-weight: 600 !important;
        font-size: 13px !important;
        border-radius: 6px !important;
        padding: 8px 14px !important;
        transition: all 0.2s ease !important;
      }

      .error-alert__retry-button:not(:disabled):hover {
        background-color: #b91c1c !important;
        color: white !important;
        box-shadow: 0 2px 8px rgba(185, 28, 28, 0.25) !important;
      }

      .error-alert__retry-button:not(:disabled):active {
        transform: translateY(0) !important;
      }

      .error-alert__retry-button:disabled {
        opacity: 0.6 !important;
        cursor: not-allowed !important;
      }

      .error-alert__retry-button:disabled mat-icon {
        animation: spin 2s linear infinite !important;
      }

      .error-alert__dismiss-button {
        color: #b91c1c !important;
        width: 32px !important;
        height: 32px !important;
        display: flex !important;
        align-items: center !important;
        justify-content: center !important;
        border-radius: 4px !important;
        transition: all 0.2s ease !important;
      }

      .error-alert__dismiss-button:hover {
        background-color: rgba(185, 28, 28, 0.08) !important;
      }

      @media (max-width: 600px) {
        .error-alert__retry-button {
          width: 100% !important;
          min-width: unset !important;
        }

        .error-alert__actions {
          width: 100% !important;
          justify-content: space-between !important;
        }
      }

      @keyframes spin {
        from {
          transform: rotate(0deg);
        }
        to {
          transform: rotate(360deg);
        }
      }
    `,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ErrorAlertComponent {
  /**
   * INPUTS: Data passed from parent component
   */

  /** Error title/heading (optional) */
  @Input() title: string = '';

  /** Error message to display (can be null) */
  @Input() error: string | null = '';

  /** Is the retry button disabled (shows loading state) */
  @Input() isRetryLoading: boolean = false;

  /** Show the retry button */
  @Input() showRetry: boolean = true;

  /** Material Icon name for the error icon */
  @Input() icon: string = 'error';

  /** Label for retry button */
  @Input() retryLabel: string = 'Retry';

  /** Label for retry button while loading */
  @Input() retryLoadingLabel: string = 'Retrying...';

  /** Show the dismiss button */
  @Input() showDismiss: boolean = false;

  /**
   * OUTPUTS: Events emitted to parent component
   */

  /** Emitted when retry button is clicked */
  @Output() onRetry = new EventEmitter<void>();

  /** Emitted when dismiss button is clicked */
  @Output() onDismiss = new EventEmitter<void>();

  /**
   * EVENT HANDLERS
   */

  /** Called when retry button is clicked */
  onRetryClicked(): void {
    if (!this.isRetryLoading) {
      this.onRetry.emit();
    }
  }

  /** Called when dismiss button is clicked */
  onDismissClicked(): void {
    this.onDismiss.emit();
  }
}
