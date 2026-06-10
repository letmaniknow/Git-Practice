import { Component, Inject, OnInit, OnDestroy, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators, AbstractControl } from '@angular/forms';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

import { MatSnackBar } from '@angular/material/snack-bar';
import {
  MatDialogModule,
  MAT_DIALOG_DATA,
  MatDialogRef
} from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';

import { AdminUserService } from '../../services/admin-user.service';
import { AdminUserResponseDto } from '../../index';

export interface ChangePasswordDialogData {
  user: AdminUserResponseDto;
}

/**
 * Change Password Dialog Component
 * 
 * Handles password change for admin users.
 * Includes password strength validation and confirmation.
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Component({
  selector: 'app-admin-user-change-password-dialog',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatIconModule,
    MatProgressBarModule
  ],
  template: `
    <div class="change-password-dialog">
      <!-- Dialog Header -->
      <h2 mat-dialog-title>
        Change Password
      </h2>

      <!-- Dialog Content -->
      <mat-dialog-content>
        <div class="user-info">
          <p><strong>User:</strong> {{ data.user.adminUsersUsername }}</p>
          <p><strong>Email:</strong> {{ data.user.adminUsersEmail }}</p>
        </div>

        <!-- Server Error Alert -->
        <div *ngIf="serverError" class="server-error-alert">
          <mat-icon>error</mat-icon>
          <span>{{ serverError }}</span>
          <button type="button" (click)="clearServerError()" class="close-btn">
            <mat-icon>close</mat-icon>
          </button>
        </div>

        <form [formGroup]="form" class="password-form">
          <!-- Loading Overlay -->
          <div *ngIf="isSubmitting" class="overlay">
            <mat-spinner></mat-spinner>
          </div>

          <!-- Current Password -->
          <mat-form-field class="full-width">
            <mat-label>Current Password</mat-label>
            <input
              matInput
              formControlName="adminUsersCurrentPassword"
              type="password"
              required
            />
            <mat-error>{{ getFieldError('adminUsersCurrentPassword') }}</mat-error>
            <mat-hint>Required for security verification</mat-hint>
          </mat-form-field>

          <!-- New Password -->
          <mat-form-field class="full-width">
            <mat-label>New Password</mat-label>
            <input
              matInput
              formControlName="adminUsersNewPassword"
              type="password"
              required
              (input)="updatePasswordStrength()"
            />
            <mat-error>{{ getFieldError('adminUsersNewPassword') }}</mat-error>
            <mat-hint>Minimum 8 characters, include uppercase, lowercase, number, and special character</mat-hint>
          </mat-form-field>

          <!-- Password Strength Indicator -->
          <div *ngIf="passwordStrength > 0" class="password-strength">
            <label>Password Strength:</label>
            <div class="strength-bar">
              <mat-progress-bar 
                [value]="passwordStrength" 
                [color]="getPasswordStrengthColor()"
                mode="determinate"
              ></mat-progress-bar>
            </div>
            <p class="strength-text" [ngClass]="'strength-' + getPasswordStrengthLevel()">
              {{ getPasswordStrengthLabel() }}
            </p>
          </div>

          <!-- Password Requirements Checklist -->
          <div class="password-requirements">
            <p><strong>Password must contain:</strong></p>
            <ul>
              <li [class.met]="hasMinLength">
                <mat-icon>{{ hasMinLength ? 'check_circle' : 'radio_button_unchecked' }}</mat-icon>
                At least 8 characters
              </li>
              <li [class.met]="hasUppercase">
                <mat-icon>{{ hasUppercase ? 'check_circle' : 'radio_button_unchecked' }}</mat-icon>
                Uppercase letter (A-Z)
              </li>
              <li [class.met]="hasLowercase">
                <mat-icon>{{ hasLowercase ? 'check_circle' : 'radio_button_unchecked' }}</mat-icon>
                Lowercase letter (a-z)
              </li>
              <li [class.met]="hasNumber">
                <mat-icon>{{ hasNumber ? 'check_circle' : 'radio_button_unchecked' }}</mat-icon>
                Number (0-9)
              </li>
              <li [class.met]="hasSpecialChar">
                <mat-icon>{{ hasSpecialChar ? 'check_circle' : 'radio_button_unchecked' }}</mat-icon>
                Special character (! &#64; # $ % ^ &amp; *)
              </li>
            </ul>
          </div>

          <!-- Confirm Password -->
          <mat-form-field class="full-width">
            <mat-label>Confirm Password</mat-label>
            <input
              matInput
              formControlName="adminUsersConfirmPassword"
              type="password"
              required
            />
            <mat-error>{{ getFieldError('adminUsersConfirmPassword') }}</mat-error>
          </mat-form-field>

          <!-- Password Match Error -->
          <div *ngIf="form.get('adminUsersConfirmPassword')?.touched && !passwordsMatch" class="password-mismatch">
            <mat-icon>warning</mat-icon>
            <span>Passwords do not match</span>
          </div>
        </form>
      </mat-dialog-content>

      <!-- Dialog Actions -->
      <mat-dialog-actions align="end">
        <button 
          mat-button 
          (click)="onCancel()"
          [disabled]="isSubmitting"
        >
          Cancel
        </button>
        <button 
          mat-raised-button 
          color="primary" 
          (click)="onSubmit()"
          [disabled]="!form.valid || isSubmitting || !passwordsMatch"
        >
          <mat-icon *ngIf="!isSubmitting">security</mat-icon>
          <mat-spinner *ngIf="isSubmitting" diameter="20"></mat-spinner>
          <span>{{ isSubmitting ? 'Changing...' : 'Change Password' }}</span>
        </button>
      </mat-dialog-actions>
    </div>
  `,
  styles: [`
    .change-password-dialog {
      min-width: 400px;
      max-width: 500px;
    }

    mat-dialog-content {
      padding: 20px;
    }

    mat-dialog-actions {
      padding: 16px 8px;
      margin: 0;
    }

    .user-info {
      background: #f5f5f5;
      padding: 12px 16px;
      border-radius: 4px;
      margin-bottom: 20px;

      p {
        margin: 4px 0;
        font-size: 0.875rem;

        strong {
          color: #424242;
        }
      }
    }

    .server-error-alert {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 12px 16px;
      background-color: #ffebee;
      border: 1px solid #f44336;
      border-radius: 4px;
      margin-bottom: 16px;
      position: relative;

      mat-icon {
        color: #f44336;
        font-size: 20px;
        width: 20px;
        height: 20px;
        flex-shrink: 0;
      }

      span {
        color: #d32f2f;
        font-size: 0.875rem;
        flex: 1;
      }

      .close-btn {
        background: none;
        border: none;
        cursor: pointer;
        padding: 4px;
        display: flex;
        align-items: center;
        color: #f44336;

        mat-icon {
          font-size: 16px;
          width: 16px;
          height: 16px;
        }
      }
    }

    .password-form {
      position: relative;
      padding: 16px 0;
    }

    .overlay {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: rgba(255, 255, 255, 0.7);
      display: flex;
      align-items: center;
      justify-content: center;
      z-index: 1000;
      border-radius: 4px;
    }

    .full-width {
      width: 100%;
      margin-bottom: 16px;
    }

    .password-strength {
      margin-bottom: 16px;
      padding: 12px;
      background: #f5f5f5;
      border-radius: 4px;

      label {
        display: block;
        font-weight: 600;
        font-size: 0.875rem;
        margin-bottom: 8px;
        color: #424242;
      }

      .strength-bar {
        margin-bottom: 8px;
        height: 6px;
        border-radius: 3px;
        overflow: hidden;
      }

      .strength-text {
        margin: 0;
        font-size: 0.75rem;
        font-weight: 600;

        &.strength-weak {
          color: #f44336;
        }

        &.strength-fair {
          color: #ff9800;
        }

        &.strength-good {
          color: #2196f3;
        }

        &.strength-strong {
          color: #4caf50;
        }
      }
    }

    .password-requirements {
      margin-bottom: 16px;
      padding: 12px;
      background: #f5f5f5;
      border-radius: 4px;

      p {
        margin: 0 0 8px 0;
        font-weight: 600;
        font-size: 0.875rem;
        color: #424242;
      }

      ul {
        list-style: none;
        padding: 0;
        margin: 0;

        li {
          display: flex;
          align-items: center;
          gap: 8px;
          font-size: 0.75rem;
          color: #757575;
          margin-bottom: 4px;

          mat-icon {
            font-size: 16px;
            width: 16px;
            height: 16px;
            flex-shrink: 0;
          }

          &.met {
            color: #4caf50;

            mat-icon {
              color: #4caf50;
            }
          }
        }
      }
    }

    .password-mismatch {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 8px 12px;
      background: #fff3e0;
      border: 1px solid #ff9800;
      border-radius: 4px;
      margin-bottom: 16px;

      mat-icon {
        color: #ff9800;
        font-size: 18px;
        width: 18px;
        height: 18px;
        flex-shrink: 0;
      }

      span {
        color: #e65100;
        font-size: 0.875rem;
      }
    }

    mat-spinner {
      display: inline-block !important;
    }
  `]
})
export class AdminUserChangePasswordDialogComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();

  form!: FormGroup;
  isSubmitting = false;
  serverError: string | null = null;

  // Password strength tracking
  passwordStrength = 0;
  hasMinLength = false;
  hasUppercase = false;
  hasLowercase = false;
  hasNumber = false;
  hasSpecialChar = false;

  constructor(
    private fb: FormBuilder,
    private adminUserService: AdminUserService,
    private snackBar: MatSnackBar,
    private cdr: ChangeDetectorRef,
    public dialogRef: MatDialogRef<AdminUserChangePasswordDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: ChangePasswordDialogData
  ) {}

  ngOnInit(): void {
    this.initializeForm();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  /**
   * Initialize password form
   */
  private initializeForm(): void {
    this.form = this.fb.group({
      adminUsersCurrentPassword: ['', [
        Validators.required,
        Validators.minLength(1)
      ]],
      adminUsersNewPassword: ['', [
        Validators.required,
        Validators.minLength(8)
      ]],
      adminUsersConfirmPassword: ['', Validators.required]
    }, { validators: this.passwordsMatchValidator });
  }

  /**
   * Custom validator for matching passwords
   */
  private passwordsMatchValidator(control: AbstractControl): { [key: string]: any } | null {
    const newPassword = control.get('adminUsersNewPassword')?.value;
    const confirmPassword = control.get('adminUsersConfirmPassword')?.value;

    if (!newPassword || !confirmPassword) {
      return null;
    }

    return newPassword === confirmPassword ? null : { passwordMismatch: true };
  }

  /**
   * Update password strength indicator
   */
  updatePasswordStrength(): void {
    const password = this.form.get('adminUsersNewPassword')?.value || '';

    // Check requirements
    this.hasMinLength = password.length >= 8;
    this.hasUppercase = /[A-Z]/.test(password);
    this.hasLowercase = /[a-z]/.test(password);
    this.hasNumber = /[0-9]/.test(password);
    this.hasSpecialChar = /[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(password);

    // Calculate strength (0-100)
    const metRequirements = [
      this.hasMinLength,
      this.hasUppercase,
      this.hasLowercase,
      this.hasNumber,
      this.hasSpecialChar
    ].filter(Boolean).length;

    this.passwordStrength = (metRequirements / 5) * 100;
    this.cdr.markForCheck();
  }

  /**
   * Get password strength label
   */
  getPasswordStrengthLabel(): string {
    const level = this.getPasswordStrengthLevel();
    const labels: { [key: string]: string } = {
      weak: 'Weak',
      fair: 'Fair',
      good: 'Good',
      strong: 'Strong'
    };
    return labels[level] || 'Weak';
  }

  /**
   * Get password strength level
   */
  getPasswordStrengthLevel(): string {
    if (this.passwordStrength < 40) return 'weak';
    if (this.passwordStrength < 60) return 'fair';
    if (this.passwordStrength < 80) return 'good';
    return 'strong';
  }

  /**
   * Get password strength color
   */
  getPasswordStrengthColor(): 'warn' | 'accent' | 'primary' {
    const level = this.getPasswordStrengthLevel();
    const colors: { [key: string]: 'warn' | 'accent' | 'primary' } = {
      weak: 'warn',
      fair: 'warn',
      good: 'accent',
      strong: 'primary'
    };
    return colors[level] || 'warn';
  }

  /**
   * Check if passwords match
   */
  get passwordsMatch(): boolean {
    return this.form.get('adminUsersNewPassword')?.value === this.form.get('adminUsersConfirmPassword')?.value;
  }

  /**
   * Get field error message
   */
  getFieldError(fieldName: string): string {
    const control = this.form.get(fieldName);
    if (!control || !control.errors || !control.touched) {
      return '';
    }

    const errors = control.errors as any;
    if (errors['required']) return `${fieldName} is required`;
    if (errors['minlength']) return `${fieldName} must be at least ${errors['minlength'].requiredLength} characters`;
    if (errors['email']) return `${fieldName} must be a valid email`;

    return 'Invalid input';
  }

  /**
   * Clear server error
   */
  clearServerError(): void {
    this.serverError = null;
  }

  /**
   * Handle form submission
   */
  onSubmit(): void {
    if (!this.form.valid || !this.passwordsMatch) {
      console.warn('❌ Dialog: Form is invalid or passwords do not match');
      return;
    }

    this.isSubmitting = true;
    this.serverError = null;

    const { adminUsersNewPassword, adminUsersCurrentPassword, adminUsersConfirmPassword } = this.form.getRawValue();
    const userId = this.data.user.adminUsersId;

    console.log('🔐 Dialog: Changing password for user:', userId);

    this.adminUserService.changePassword(userId, adminUsersNewPassword, adminUsersCurrentPassword)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response) => {
          console.log('✅ Dialog: Password changed successfully');
          this.isSubmitting = false;
          this.showSuccess('Password changed successfully');
          this.dialogRef.close({ mode: 'change-password', success: true });
        },
        error: (error) => {
          console.error('❌ Dialog: Error changing password:', error);
          this.isSubmitting = false;
          this.serverError = error?.error?.message || 'Failed to change password. Please try again.';
          this.cdr.markForCheck();
        }
      });
  }

  /**
   * Handle cancel
   */
  onCancel(): void {
    this.dialogRef.close();
  }

  /**
   * Show success message
   */
  private showSuccess(message: string): void {
    this.snackBar.open(message, 'Close', {
      duration: 5000,
      verticalPosition: 'top',
      horizontalPosition: 'end',
      panelClass: ['success-snackbar']
    });
  }
}
