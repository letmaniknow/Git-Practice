import { Component, Inject, ViewEncapsulation } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import {
  MAT_SNACK_BAR_DATA,
  MatSnackBarRef,
} from '@angular/material/snack-bar';

/**
 * Custom Snackbar Component
 * Two-button pattern: Dismiss + Retry
 * Industry standard UX for error notifications
 */

export interface CustomSnackbarData {
  message: string;
  type: 'error' | 'info' | 'success' | 'warning';
  showRetry: boolean;
  icon?: string;
}

@Component({
  selector: 'app-custom-snackbar',
  standalone: true,
  imports: [CommonModule, MatButtonModule, MatIconModule],
  encapsulation: ViewEncapsulation.None, // CRITICAL: Disable encapsulation
  host: {
    'class': 'app-custom-snackbar-host'
  },
  template: `
    <div class="custom-snackbar-container" [ngClass]="'snackbar-' + data.type">
      <!-- Message with inline icon -->
      <span class="snackbar-message">
        <mat-icon class="snackbar-icon">{{ data.icon }}</mat-icon>
        {{ data.message }}
      </span>
      
      <!-- Actions -->
      <div class="snackbar-actions">
        <button
          *ngIf="data.showRetry"
          mat-button
          class="snackbar-btn-retry"
          (click)="onRetry()"
        >
          Retry
        </button>
        <button
          mat-button
          class="snackbar-btn-dismiss"
          (click)="onDismiss()"
        >
          Dismiss
        </button>
      </div>
    </div>
  `,
  styles: [
    `
      /* ============================================
         MODERN COMPACT SNACKBAR - Industry Standard
         Similar to: GitHub, Linear, Vercel
         ============================================ */
      
      .custom-snackbar-container {
        display: flex;
        align-items: center;
        justify-content: space-between;
        gap: 20px;
        min-width: 450px;
        max-width: 550px;
        padding: 12px 20px;
        border-radius: 8px;
        /* Modern elevated shadow - no white wrapper needed */
        box-shadow: 
          0 10px 25px rgba(0, 0, 0, 0.25),
          0 4px 10px rgba(0, 0, 0, 0.15),
          0 0 0 1px rgba(0, 0, 0, 0.1);
      }

      .snackbar-message {
        display: flex;
        align-items: center;
        gap: 10px;
        flex: 1;
        font-size: 14px;
        line-height: 1.4;
        font-weight: 500;
      }

      .snackbar-icon {
        font-size: 20px;
        width: 20px;
        height: 20px;
        flex-shrink: 0;
      }

      .snackbar-actions {
        display: flex;
        gap: 8px;
        flex-shrink: 0;
      }

      .snackbar-btn-retry,
      .snackbar-btn-dismiss {
        background-color: rgba(255, 255, 255, 0.2) !important;
        border: 1px solid rgba(255, 255, 255, 0.3) !important;
        font-weight: 600 !important;
        min-width: 70px !important;
        height: 32px !important;
        padding: 0 14px !important;
        border-radius: 6px !important;
        text-transform: capitalize !important;
        letter-spacing: 0.3px !important;
        font-size: 13px !important;
        transition: all 0.15s ease !important;
        cursor: pointer;
        backdrop-filter: blur(4px);
      }

      .snackbar-btn-retry:hover,
      .snackbar-btn-dismiss:hover {
        background-color: rgba(255, 255, 255, 0.25) !important;
        border-color: rgba(255, 255, 255, 0.4) !important;
        transform: translateY(-1px);
      }

      /* ============================================
         ERROR TYPE - Material Red (Compact)
         ============================================ */
      .snackbar-error {
        background: linear-gradient(135deg, #d32f2f 0%, #c62828 100%) !important;
        color: #ffffff !important;
      }

      .snackbar-error .snackbar-icon,
      .snackbar-error .snackbar-message {
        color: #ffffff !important;
      }

      .snackbar-error .snackbar-btn-retry,
      .snackbar-error .snackbar-btn-dismiss {
        color: #ffffff !important;
      }

      /* ============================================
         INFO TYPE - Material Blue (Compact)
         ============================================ */
      .snackbar-info {
        background: linear-gradient(135deg, #1976d2 0%, #1565c0 100%) !important;
        color: #ffffff !important;
      }

      .snackbar-info .snackbar-icon,
      .snackbar-info .snackbar-message {
        color: #ffffff !important;
      }

      .snackbar-info .snackbar-btn-retry,
      .snackbar-info .snackbar-btn-dismiss {
        color: #ffffff !important;
      }

      /* ============================================
         SUCCESS TYPE - Material Green (Compact)
         ============================================ */
      .snackbar-success {
        background: linear-gradient(135deg, #388e3c 0%, #2e7d32 100%) !important;
        color: #ffffff !important;
      }

      .snackbar-success .snackbar-icon,
      .snackbar-success .snackbar-message {
        color: #ffffff !important;
      }

      .snackbar-success .snackbar-btn-retry,
      .snackbar-success .snackbar-btn-dismiss {
        color: #ffffff !important;
      }

      /* ============================================
         WARNING TYPE - Material Orange (Compact)
         ============================================ */
      .snackbar-warning {
        background: linear-gradient(135deg, #f57c00 0%, #ef6c00 100%) !important;
        color: #ffffff !important;
      }

      .snackbar-warning .snackbar-icon,
      .snackbar-warning .snackbar-message {
        color: #ffffff !important;
      }

      .snackbar-warning .snackbar-btn-retry,
      .snackbar-warning .snackbar-btn-dismiss {
        color: #ffffff !important;
      }

      /* ============================================
         RESPONSIVE - Mobile
         ============================================ */
      @media (max-width: 600px) {
        .custom-snackbar-container {
          min-width: 90vw;
          max-width: 90vw;
          padding: 10px 16px;
          flex-direction: column;
          align-items: flex-start;
          gap: 12px;
        }

        .snackbar-actions {
          width: 100%;
          justify-content: flex-end;
        }
      }
    `,
  ],
})
export class CustomSnackbarComponent {
  constructor(
    public snackBarRef: MatSnackBarRef<CustomSnackbarComponent>,
    @Inject(MAT_SNACK_BAR_DATA) public data: CustomSnackbarData
  ) {}

  onRetry(): void {
    this.snackBarRef.dismissWithAction();
  }

  onDismiss(): void {
    this.snackBarRef.dismiss();
  }
}
