import { Component, Inject, ChangeDetectionStrategy, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subject } from 'rxjs';
import { takeUntil, tap } from 'rxjs/operators';

import {
  MatDialogModule,
  MAT_DIALOG_DATA,
  MatDialogRef
} from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';

import { AdminUserService, AdminUserResponseDto } from '../../index';

export interface AdminUserDeleteDialogData {
  userId: string;
}

/**
 * Admin User Delete Dialog Component
 * 
 * Confirmation dialog for deleting admin users.
 * Shows user details and confirms deletion action.
 * 
 * Uses soft delete (data can be restored).
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Component({
  selector: 'app-admin-user-delete-dialog',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    CommonModule,
    FormsModule,
    MatDialogModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule
  ],
  template: `
    <div class="delete-dialog">
      <!-- Warning Icon -->
      <div class="warning-icon">
        <mat-icon>warning</mat-icon>
      </div>

      <!-- Title -->
      <h2 mat-dialog-title>Delete Admin User?</h2>

      <!-- Content -->
      <mat-dialog-content>
        <!-- Loading State -->
        <div *ngIf="isLoading" class="loading-state">
          <mat-spinner></mat-spinner>
          <p>Loading user information...</p>
        </div>

        <!-- Error State -->
        <div *ngIf="loadError && !isLoading" class="error-state">
          <mat-icon>error_outline</mat-icon>
          <p>{{ loadError }}</p>
          <button mat-stroked-button (click)="loadUserForDelete()">
            Retry
          </button>
        </div>

        <!-- Delete Confirmation -->
        <div *ngIf="user && !isLoading && !loadError" class="delete-content">
          <p class="warning-message">
            You are about to delete the admin user:
          </p>

          <div class="user-info">
            <div class="info-item">
              <span class="label">Username:</span>
              <span class="value">{{ user.adminUsersUsername }}</span>
            </div>
            <div class="info-item">
              <span class="label">Email:</span>
              <span class="value">{{ user.adminUsersEmail }}</span>
            </div>
            <div class="info-item">
              <span class="label">Full Name:</span>
              <span class="value">{{ user.adminUsersFullName || '—' }}</span>
            </div>
            <div class="info-item">
              <span class="label">Role:</span>
              <span class="value">{{ user.adminUsersRoleName }}</span>
            </div>
          </div>

          <div class="notice">
            <mat-icon>info</mat-icon>
            <div>
              <strong>Note:</strong> This is a soft delete. The user can be restored from the deleted users list.
              All user data and activity logs will be preserved.
            </div>
          </div>

          <p class="confirm-text">
            Type <strong>{{ confirmationText }}</strong> to confirm deletion:
          </p>

          <input
            type="text"
            class="confirmation-input"
            [(ngModel)]="confirmationInput"
            placeholder="Type confirmation text here"
            [disabled]="isDeleting"
          />
        </div>

        <!-- Loading Overlay -->
        <div *ngIf="isDeleting" class="overlay">
          <mat-spinner></mat-spinner>
        </div>
      </mat-dialog-content>

      <!-- Actions -->
      <mat-dialog-actions align="end">
        <button
          mat-button
          (click)="onCancel()"
          [disabled]="isDeleting || isLoading"
        >
          Cancel
        </button>
        <button
          mat-raised-button
          color="warn"
          (click)="onDelete()"
          [disabled]="!user || confirmationInput !== confirmationText || isDeleting || isLoading"
        >
          <mat-icon *ngIf="!isDeleting">delete</mat-icon>
          <mat-spinner *ngIf="isDeleting" diameter="20"></mat-spinner>
          <span>{{ isDeleting ? 'Deleting...' : 'Delete User' }}</span>
        </button>
      </mat-dialog-actions>
    </div>
  `,
  styles: [`
    .delete-dialog {
      min-width: 450px;
      max-width: 550px;

      @media (max-width: 600px) {
        min-width: auto;
        width: 95vw;
      }
    }

    mat-dialog-content {
      position: relative;
      padding: 1.5rem;
    }

    mat-dialog-actions {
      padding: 1rem;
      border-top: 1px solid var(--color-border, #e0e0e0);
    }

    .warning-icon {
      display: flex;
      justify-content: center;
      margin: 0 0 1rem 0;

      mat-icon {
        font-size: 48px;
        width: 48px;
        height: 48px;
        color: #ff9800;
      }
    }

    .delete-content {
      display: flex;
      flex-direction: column;
      gap: 1rem;
    }

    .loading-state {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 1rem;
      padding: 2rem 1rem;

      p {
        margin: 0;
        color: var(--color-text-secondary, #757575);
      }
    }

    .error-state {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 1rem;
      padding: 1.5rem;
      background-color: #ffebee;
      border: 1px solid #f44336;
      border-radius: 4px;
      color: #c62828;

      mat-icon {
        font-size: 36px;
        width: 36px;
        height: 36px;
      }

      p {
        margin: 0;
        text-align: center;
      }
    }

    .warning-message {
      margin: 0;
      color: var(--color-text-secondary, #757575);
    }

    .user-info {
      background-color: var(--color-surface, #f5f5f5);
      border: 1px solid var(--color-border, #e0e0e0);
      border-radius: 4px;
      padding: 1rem;
      display: flex;
      flex-direction: column;
      gap: 0.75rem;
    }

    .info-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      gap: 1rem;

      .label {
        font-weight: 500;
        color: var(--color-text-secondary, #757575);
        min-width: 100px;
      }

      .value {
        color: var(--color-text-primary, #212121);
        font-weight: 500;
      }
    }

    .notice {
      display: flex;
      gap: 0.75rem;
      padding: 0.75rem;
      background-color: #e3f2fd;
      border-left: 4px solid #2196f3;
      border-radius: 4px;
      color: #0d47a1;

      mat-icon {
        flex-shrink: 0;
        margin-top: 2px;
      }

      div {
        flex: 1;

        strong {
          display: block;
          margin-bottom: 0.25rem;
        }
      }
    }

    .confirm-text {
      margin: 0.5rem 0 0.5rem 0;
      color: var(--color-text-primary, #212121);

      strong {
        color: #f44336;
        font-family: monospace;
        background-color: var(--color-surface, #f5f5f5);
        padding: 0.25rem 0.5rem;
        border-radius: 2px;
      }
    }

    .confirmation-input {
      width: 100%;
      padding: 0.75rem;
      border: 1px solid var(--color-border, #e0e0e0);
      border-radius: 4px;
      font-family: monospace;
      font-size: 0.875rem;

      &:focus {
        outline: none;
        border-color: #2196f3;
        box-shadow: 0 0 0 3px rgba(33, 150, 243, 0.1);
      }

      &:disabled {
        background-color: var(--color-surface, #f5f5f5);
        cursor: not-allowed;
      }
    }

    .overlay {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      display: flex;
      align-items: center;
      justify-content: center;
      background: rgba(255, 255, 255, 0.8);
      z-index: 100;
      border-radius: 4px;
    }

    mat-dialog-actions {
      display: flex;
      justify-content: flex-end;
      gap: 1rem;

      button {
        min-width: 100px;

        span {
          margin-left: 0.5rem;
        }
      }

      button[color="warn"] {
        &[disabled] {
          opacity: 0.5;
          cursor: not-allowed;
        }
      }
    }
  `]
})
export class AdminUserDeleteDialogComponent implements OnInit, OnDestroy {

  private destroy$ = new Subject<void>();

  confirmationText = 'DELETE';
  confirmationInput = '';
  isDeleting = false;
  user: AdminUserResponseDto | null = null;
  isLoading = false;
  loadError: string | null = null;

  constructor(
    private adminUserService: AdminUserService,
    private snackBar: MatSnackBar,
    public dialogRef: MatDialogRef<AdminUserDeleteDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: AdminUserDeleteDialogData
  ) {}

  ngOnInit(): void {
    console.log('🗑️ Dialog: Delete dialog opened for user:', this.data.userId);
    this.loadUserForDelete();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  // ========================================
  // Data Loading
  // ========================================

  /**
   * Load user data for deletion confirmation
   */
  loadUserForDelete(): void {
    this.isLoading = true;
    this.loadError = null;

    this.adminUserService.getAdminUser(this.data.userId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (user) => {
          this.user = user;
          this.isLoading = false;
          console.log('✅ Dialog: User loaded for deletion:', user.adminUsersUsername);
        },
        error: (error) => {
          console.error('❌ Dialog: Error loading user:', error);
          this.isLoading = false;
          this.loadError = 'Failed to load user information. Please try again.';
        }
      });
  }

  // ========================================
  // Actions
  // ========================================

  /**
   * Delete the user
   */
  onDelete(): void {
    if (!this.user) {
      console.warn('❌ Dialog: No user loaded for deletion');
      return;
    }

    if (this.confirmationInput !== this.confirmationText) {
      console.warn('❌ Dialog: Confirmation text does not match');
      return;
    }

    this.isDeleting = true;

    this.adminUserService.deleteAdminUser(this.user.adminUsersId)
      .pipe(
        tap(() => {
          console.log('✅ Dialog: User deleted successfully');
          this.isDeleting = false;
          this.showSuccess('Admin user deleted successfully');
          this.dialogRef.close(true); // Return true to indicate successful deletion
        }),
        takeUntil(this.destroy$)
      )
      .subscribe({
        error: (error: any) => {
          console.error('❌ Dialog: Error deleting user:', error);
          this.isDeleting = false;
          this.showError('Failed to delete admin user');
        }
      });
  }

  /**
   * Cancel deletion
   */
  onCancel(): void {
    this.dialogRef.close(false); // Return false to indicate cancellation
  }

  // ========================================
  // Notifications
  // ========================================

  /**
   * Show success notification
   */
  private showSuccess(message: string): void {
    this.snackBar.open(message, 'Close', {
      duration: 3000,
      horizontalPosition: 'end',
      verticalPosition: 'top',
      panelClass: ['success-snackbar']
    });
  }

  /**
   * Show error notification
   */
  private showError(message: string): void {
    this.snackBar.open(message, 'Close', {
      duration: 5000,
      horizontalPosition: 'end',
      verticalPosition: 'top',
      panelClass: ['error-snackbar']
    });
  }
}
