import { Component, Inject, ChangeDetectionStrategy, ChangeDetectorRef, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Subject } from 'rxjs';
import { takeUntil, tap } from 'rxjs/operators';

import { MatSnackBar } from '@angular/material/snack-bar';

import {
  MatDialogModule,
  MAT_DIALOG_DATA,
  MatDialogRef
} from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTabsModule } from '@angular/material/tabs';
import { MatIconModule } from '@angular/material/icon';

import {
  AdminUserService,
  AdminUserCreateRequestDto,
  AdminUserUpdateRequestDto,
  AdminUserResponseDto,
  AdminStatus,
  ADMIN_USER_FORM_VALIDATION_MESSAGES,
  ADMIN_STATUS_LABELS
} from '../../index';

export interface AdminUserFormDialogData {
  mode: 'create' | 'edit';
  user?: AdminUserResponseDto;
}

/**
 * Admin User Form Dialog Component
 * 
 * Handles creation and editing of admin users.
 * Separate forms for create (with password) and edit (without password).
 * 
 * Uses:
 * - Reactive Forms with strong typing
 * - Real-time validation feedback
 * - Server-side error handling (username/email duplicates)
 * - OnPush change detection strategy
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Component({
  selector: 'app-admin-user-form-dialog',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatCheckboxModule,
    MatProgressSpinnerModule,
    MatTabsModule,
    MatIconModule
  ],
  template: `
    <div class="admin-user-form-dialog">
      <!-- Dialog Header -->
      <h2 mat-dialog-title>
        {{ isCreateMode ? 'Create Admin User' : 'Edit Admin User' }}
      </h2>

      <!-- Form Content -->
      <mat-dialog-content>
        <!-- Server Error Alert -->
        <div *ngIf="serverError" class="server-error-alert">
          <mat-icon>error</mat-icon>
          <span>{{ serverError }}</span>
          <button type="button" (click)="clearServerError()" class="close-btn">
            <mat-icon>close</mat-icon>
          </button>
        </div>

        <form [formGroup]="form" class="admin-user-form">
          <!-- Loading Overlay -->
          <div *ngIf="isSubmitting" class="overlay">
            <mat-spinner></mat-spinner>
          </div>

          <!-- Tab 1: Basic Information -->
          <mat-tab-group>
            <mat-tab label="Basic Information">
              <div class="form-section">
                <!-- Username -->
                <mat-form-field>
                  <mat-label>Username</mat-label>
                  <input
                    matInput
                    formControlName="adminUsersUsername"
                    [readonly]="!isCreateMode"
                    required
                  />
                  <mat-error>{{ getFieldError('adminUsersUsername') }}</mat-error>
                  <mat-hint>3-255 characters</mat-hint>
                </mat-form-field>

                <!-- Email -->
                <mat-form-field>
                  <mat-label>Email</mat-label>
                  <input
                    matInput
                    formControlName="adminUsersEmail"
                    type="email"
                    required
                  />
                  <mat-error>{{ getFieldError('adminUsersEmail') }}</mat-error>
                </mat-form-field>

                <!-- Password (Create Mode Only) -->
                <mat-form-field *ngIf="isCreateMode">
                  <mat-label>Password</mat-label>
                  <input
                    matInput
                    formControlName="adminUsersPassword"
                    type="password"
                    required
                  />
                  <mat-error>{{ getFieldError('adminUsersPassword') }}</mat-error>
                  <mat-hint>Minimum 8 characters</mat-hint>
                </mat-form-field>

                <!-- First Name -->
                <mat-form-field>
                  <mat-label>First Name</mat-label>
                  <input matInput formControlName="adminUsersFirstName" />
                  <mat-error>{{ getFieldError('adminUsersFirstName') }}</mat-error>
                </mat-form-field>

                <!-- Last Name -->
                <mat-form-field>
                  <mat-label>Last Name</mat-label>
                  <input matInput formControlName="adminUsersLastName" />
                  <mat-error>{{ getFieldError('adminUsersLastName') }}</mat-error>
                </mat-form-field>

                <!-- Full Name -->
                <mat-form-field>
                  <mat-label>Full Name</mat-label>
                  <input matInput formControlName="adminUsersFullName" />
                  <mat-error>{{ getFieldError('adminUsersFullName') }}</mat-error>
                </mat-form-field>

                <!-- Phone Number -->
                <mat-form-field>
                  <mat-label>Phone Number</mat-label>
                  <input matInput formControlName="adminUsersPhoneNumber" />
                  <mat-error>{{ getFieldError('adminUsersPhoneNumber') }}</mat-error>
                </mat-form-field>
              </div>
            </mat-tab>

            <!-- Tab 2: Role & Status -->
            <mat-tab label="Role & Status">
              <div class="form-section">
                <!-- Role ID -->
                <mat-form-field>
                  <mat-label>Role (Optional)</mat-label>
                  <mat-select formControlName="adminUsersRoleId" [disabled]="rolesLoading">
                    <mat-option value="">No role assigned</mat-option>
                    <mat-optgroup *ngIf="!rolesLoading" [label]="'Available Roles'">
                      <mat-option *ngFor="let role of availableRoles" [value]="role.roleId">
                        {{ role.roleName }}
                      </mat-option>
                    </mat-optgroup>
                    <mat-optgroup *ngIf="rolesLoading" [label]="'Loading...'">
                      <mat-option disabled>Loading roles...</mat-option>
                    </mat-optgroup>
                  </mat-select>
                  <mat-hint *ngIf="!rolesLoading && availableRoles.length > 0">
                    {{ availableRoles.length }} role(s) available
                  </mat-hint>
                  <mat-hint *ngIf="availableRoles.length === 0 && !rolesLoading" class="text-warning">
                    No roles available. Role can be assigned later.
                  </mat-hint>
                </mat-form-field>

                <!-- Status -->
                <mat-form-field>
                  <mat-label>Status</mat-label>
                  <mat-select formControlName="adminUsersStatus">
                    <mat-option>Select a status</mat-option>
                    <mat-option *ngFor="let status of statusOptions" [value]="status">
                      {{ getStatusLabel(status) }}
                    </mat-option>
                  </mat-select>
                  <mat-error>{{ getFieldError('adminUsersStatus') }}</mat-error>
                </mat-form-field>

                <!-- Account Locked -->
                <mat-checkbox formControlName="adminUsersAccountLocked">
                  Account Locked
                </mat-checkbox>

                <!-- Phone Verified -->
                <mat-checkbox formControlName="adminUsersPhoneVerified">
                  Phone Verified
                </mat-checkbox>

                <!-- MFA Enabled -->
                <mat-checkbox disabled>
                  MFA Enabled (managed separately)
                </mat-checkbox>
              </div>
            </mat-tab>

            <!-- Tab 3: Additional Information -->
            <mat-tab label="Additional Info">
              <div class="form-section">
                <!-- Avatar URL -->
                <mat-form-field>
                  <mat-label>Avatar URL</mat-label>
                  <input matInput formControlName="adminUsersAvatarUrl" />
                  <mat-error>{{ getFieldError('adminUsersAvatarUrl') }}</mat-error>
                </mat-form-field>

                <!-- Auth Provider -->
                <mat-form-field>
                  <mat-label>Authentication Provider</mat-label>
                  <input matInput formControlName="adminUsersAuthProvider" />
                  <mat-error>{{ getFieldError('adminUsersAuthProvider') }}</mat-error>
                </mat-form-field>

                <!-- Notes -->
                <mat-form-field class="full-width">
                  <mat-label>Notes</mat-label>
                  <textarea
                    matInput
                    formControlName="adminUsersNotes"
                    rows="4"
                  ></textarea>
                  <mat-error>{{ getFieldError('adminUsersNotes') }}</mat-error>
                  <mat-hint>Maximum 1000 characters</mat-hint>
                </mat-form-field>
              </div>
            </mat-tab>
          </mat-tab-group>
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
          [disabled]="!form.valid || isSubmitting"
        >
          <mat-icon *ngIf="!isSubmitting">
            {{ isCreateMode ? 'person_add' : 'save' }}
          </mat-icon>
          <mat-spinner *ngIf="isSubmitting" diameter="20"></mat-spinner>
          <span>{{ isCreateMode ? 'Create' : 'Update' }}</span>
        </button>
      </mat-dialog-actions>
    </div>
  `,
  styles: [`
    .admin-user-form-dialog {
      min-width: 600px;
      max-width: 800px;

      @media (max-width: 768px) {
        min-width: auto;
        width: 95vw;
      }
    }

    mat-dialog-content {
      max-height: 70vh;
      overflow-y: auto;
      padding: 1.5rem;
    }

    mat-dialog-actions {
      padding: 1rem;
      border-top: 1px solid var(--color-border, #e0e0e0);
    }

    .admin-user-form {
      display: flex;
      flex-direction: column;
      gap: 1.5rem;
      position: relative;

      ::ng-deep .mat-mdc-tab-body-wrapper {
        padding: 1.5rem 0;
      }
    }

    .form-section {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 1rem;

      @media (max-width: 600px) {
        grid-template-columns: 1fr;
      }

      mat-form-field {
        width: 100%;

        &.full-width {
          grid-column: 1 / -1;
        }
      }

      mat-checkbox {
        grid-column: 1;
        margin: 1rem 0;
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

    .server-error-alert {
      display: flex;
      align-items: center;
      gap: 1rem;
      padding: 1rem;
      margin-bottom: 1rem;
      background-color: #ffebee;
      border: 1px solid #f44336;
      border-radius: 4px;
      color: #c62828;

      mat-icon {
        flex-shrink: 0;
      }

      span {
        flex: 1;
      }

      .close-btn {
        background: none;
        border: none;
        cursor: pointer;
        color: #c62828;
        display: flex;
        align-items: center;
        padding: 0;

        mat-icon {
          font-size: 20px;
        }
      }
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
    }

    .text-warning {
      color: #ff9800;
    }

    ::ng-deep {
      .mat-mdc-form-field-error {
        font-size: 0.75rem;
      }

      .mat-mdc-form-field-hint {
        font-size: 0.75rem;
      }
    }
  `]
})
export class AdminUserFormDialogComponent implements OnInit, OnDestroy {

  private destroy$ = new Subject<void>();

  form!: FormGroup;
  isCreateMode = true;
  isSubmitting = false;
  statusOptions = Object.values(AdminStatus);
  serverError: string | null = null;
  availableRoles: any[] = [];
  rolesLoading = false;

  constructor(
    private fb: FormBuilder,
    private adminUserService: AdminUserService,
    private snackBar: MatSnackBar,
    private cdr: ChangeDetectorRef,
    public dialogRef: MatDialogRef<AdminUserFormDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: AdminUserFormDialogData
  ) {
    this.isCreateMode = data.mode === 'create';
  }

  ngOnInit(): void {
    this.initializeForm();
    this.loadAvailableRoles();
    if (!this.isCreateMode && this.data.user) {
      this.populateForm(this.data.user);
    }
  }

  /**
   * Load available roles from backend RBAC service
   */
  private loadAvailableRoles(): void {
    this.rolesLoading = true;
    this.adminUserService.getAvailableRoles()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (roles) => {
          this.availableRoles = roles || [];
          this.rolesLoading = false;
          console.log('✅ Dialog: Available roles loaded:', this.availableRoles.length);
        },
        error: (error) => {
          console.error('❌ Dialog: Error loading roles:', error);
          this.rolesLoading = false;
          this.availableRoles = [];
        }
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  // ========================================
  // Form Initialization
  // ========================================

  private initializeForm(): void {
    if (this.isCreateMode) {
      this.form = this.fb.group({
        adminUsersUsername: ['', [
          Validators.required,
          Validators.minLength(3),
          Validators.maxLength(255)
        ]],
        adminUsersEmail: ['', [
          Validators.required,
          Validators.email
        ]],
        adminUsersPassword: ['', [
          Validators.required,
          Validators.minLength(8)
        ]],
        adminUsersFirstName: ['', [Validators.maxLength(100)]],
        adminUsersLastName: ['', [Validators.maxLength(100)]],
        adminUsersFullName: ['', [Validators.maxLength(255)]],
        adminUsersPhoneNumber: ['', [Validators.maxLength(30)]],
        adminUsersRoleId: [''],
        adminUsersStatus: [AdminStatus.ACTIVE],
        adminUsersAccountLocked: [false],
        adminUsersPhoneVerified: [false],
        adminUsersAvatarUrl: [''],
        adminUsersAuthProvider: [''],
        adminUsersNotes: ['', [Validators.maxLength(1000)]]
      });
    } else {
      this.form = this.fb.group({
        adminUsersUsername: [{ value: '', disabled: true }],
        adminUsersEmail: ['', [
          Validators.required,
          Validators.email
        ]],
        adminUsersFirstName: ['', [Validators.maxLength(100)]],
        adminUsersLastName: ['', [Validators.maxLength(100)]],
        adminUsersFullName: ['', [Validators.maxLength(255)]],
        adminUsersPhoneNumber: ['', [Validators.maxLength(30)]],
        adminUsersRoleId: [''],
        adminUsersStatus: [''],
        adminUsersAccountLocked: [false],
        adminUsersPhoneVerified: [false],
        adminUsersAvatarUrl: [''],
        adminUsersAuthProvider: [''],
        adminUsersNotes: ['', [Validators.maxLength(1000)]]
      });
    }
  }

  /**
   * Populate form with existing user data
   */
  private populateForm(user: AdminUserResponseDto): void {
    this.form.patchValue({
      adminUsersUsername: user.adminUsersUsername,
      adminUsersEmail: user.adminUsersEmail,
      adminUsersFirstName: user.adminUsersFirstName,
      adminUsersLastName: user.adminUsersLastName,
      adminUsersFullName: user.adminUsersFullName,
      adminUsersPhoneNumber: user.adminUsersPhoneNumber,
      adminUsersRoleId: user.adminUsersRoleId,
      adminUsersStatus: user.adminUsersStatus,
      adminUsersAccountLocked: user.adminUsersAccountLocked,
      adminUsersPhoneVerified: user.adminUsersPhoneVerified,
      adminUsersAvatarUrl: user.adminUsersAvatarUrl,
      adminUsersAuthProvider: user.adminUsersAuthProvider,
      adminUsersNotes: user.adminUsersNotes
    });
    // Trigger change detection for OnPush strategy
    this.cdr.markForCheck();
    console.log('✅ Dialog: Form populated with user data');
  }

  // ========================================
  // Form Submission
  // ========================================

  onSubmit(): void {
    if (!this.form.valid) {
      console.warn('❌ Dialog: Form is invalid');
      return;
    }

    this.isSubmitting = true;
    let formValue = this.form.getRawValue();

    // Clean up optional UUID fields - convert empty strings to null
    if (formValue.adminUsersRoleId === '') {
      formValue.adminUsersRoleId = null;
    }

    if (this.isCreateMode) {
      this.createUser(formValue);
    } else if (this.data.user) {
      this.updateUser(this.data.user.adminUsersId, formValue);
    }
  }

  /**
   * Create new admin user
   */
  private createUser(request: AdminUserCreateRequestDto): void {
    this.adminUserService.createAdminUser(request)
      .pipe(
        tap((response) => {
          console.log('✅ Dialog: User created successfully');
          this.isSubmitting = false;
          this.showSuccess('Admin user created successfully');
          this.dialogRef.close({ mode: 'create', data: response });
        }),
        takeUntil(this.destroy$)
      )
      .subscribe({
        error: (error: any) => {
          console.error('❌ Dialog: Error creating user:', error);
          this.isSubmitting = false;
          this.handleServerError(error);
        }
      });
  }

  /**
   * Update existing admin user
   */
  private updateUser(userId: string, request: AdminUserUpdateRequestDto): void {
    this.adminUserService.updateAdminUser(userId, request)
      .pipe(
        tap((response) => {
          console.log('✅ Dialog: User updated successfully');
          this.isSubmitting = false;
          this.showSuccess('Admin user updated successfully');
          this.dialogRef.close({ mode: 'edit', data: response });
        }),
        takeUntil(this.destroy$)
      )
      .subscribe({
        error: (error: any) => {
          console.error('❌ Dialog: Error updating user:', error);
          this.isSubmitting = false;
          this.handleServerError(error);
        }
      });
  }

  /**
   * Handle server errors with user-friendly messages
   */
  private handleServerError(error: any): void {
    let errorMessage = 'An error occurred';

    // Check for specific error patterns
    if (error?.status === 409) {
      // Conflict error (duplicate username/email)
      const errorData = error?.error;
      if (errorData?.message?.includes('username')) {
        errorMessage = 'This username is already taken';
      } else if (errorData?.message?.includes('email')) {
        errorMessage = 'This email is already in use';
      } else {
        errorMessage = errorData?.message || 'Conflict: Resource already exists';
      }
    } else if (error?.status === 400) {
      // Bad request
      const errorData = error?.error;
      errorMessage = errorData?.message || 'Invalid request data';
    } else if (error?.status === 401) {
      errorMessage = 'Unauthorized - Please log in again';
    } else if (error?.status === 403) {
      errorMessage = 'You do not have permission to perform this action';
    } else if (error?.status === 500) {
      errorMessage = 'Server error - Please try again later';
    } else if (error?.error?.message) {
      errorMessage = error.error.message;
    }

    this.serverError = errorMessage;
  }

  /**
   * Clear server error message
   */
  clearServerError(): void {
    this.serverError = null;
  }

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

  // ========================================
  // Form Utilities
  // ========================================

  /**
   * Get error message for form field
   */
  getFieldError(fieldName: string): string {
    const control = this.form.get(fieldName);
    if (!control || !control.errors) {
      return '';
    }

    const errors = ADMIN_USER_FORM_VALIDATION_MESSAGES[fieldName as keyof typeof ADMIN_USER_FORM_VALIDATION_MESSAGES];
    if (!errors) {
      return 'Invalid field';
    }

    // Cast to any to safely access union type properties
    const errorMessages = errors as any;

    if (control.hasError('required')) {
      return errorMessages['required'] || 'This field is required';
    }
    if (control.hasError('minlength')) {
      return errorMessages['minlength'] || 'Minimum length not met';
    }
    if (control.hasError('maxlength')) {
      return errorMessages['maxlength'] || 'Maximum length exceeded';
    }
    if (control.hasError('email')) {
      return errorMessages['email'] || 'Invalid email format';
    }
    if (control.hasError('pattern')) {
      return errorMessages['pattern'] || 'Invalid format';
    }

    return 'Invalid field';
  }

  /**
   * Get status label
   */
  getStatusLabel(status: AdminStatus): string {
    return ADMIN_STATUS_LABELS[status] || status;
  }

  /**
   * Cancel dialog
   */
  onCancel(): void {
    this.dialogRef.close();
  }
}
