import { Component, Inject, OnInit, OnDestroy, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

import { MatSnackBar } from '@angular/material/snack-bar';
import {
  MatDialogModule,
  MAT_DIALOG_DATA,
  MatDialogRef
} from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';

import { AdminUserService } from '../../services/admin-user.service';
import { AdminUserResponseDto } from '../../index';

export interface ChangeRoleDialogData {
  user: AdminUserResponseDto;
}

export interface RoleDto {
  roleId: string;
  roleName: string;
  roleDescription?: string;
  permissions?: string[];
}

/**
 * Change Role Dialog Component (Single Role Assignment)
 * 
 * Allows changing the assigned role for an admin user.
 * Only ONE role can be assigned per user.
 * Shows current role and new role selection with confirmation.
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Component({
  selector: 'app-admin-user-change-role-dialog',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatButtonModule,
    MatSelectModule,
    MatFormFieldModule,
    MatProgressSpinnerModule,
    MatIconModule,
    MatCardModule,
    MatChipsModule
  ],
  template: `
    <div class="change-role-dialog">
      <!-- Dialog Header -->
      <h2 mat-dialog-title>
        Change Role
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

        <!-- Loading State -->
        <div *ngIf="isLoading" class="loading-state">
          <mat-spinner diameter="40"></mat-spinner>
          <p>Loading roles...</p>
        </div>

        <!-- Current Role Display -->
        <div *ngIf="!isLoading" class="current-role-section">
          <mat-card class="role-card">
            <mat-card-header>
              <mat-card-title>Current Role</mat-card-title>
            </mat-card-header>
            <mat-card-content>
              <div *ngIf="currentRoleName" class="role-display">
                <mat-icon class="role-icon">assignment</mat-icon>
                <span class="role-name">{{ currentRoleName }}</span>
                <span class="role-badge">Active</span>
              </div>
              <div *ngIf="!currentRoleName" class="no-role">
                <p>No role currently assigned</p>
              </div>
            </mat-card-content>
          </mat-card>
        </div>

        <!-- Role Selection -->
        <div *ngIf="!isLoading" class="role-selection-section">
          <mat-form-field appearance="outline" class="full-width">
            <mat-label>Select New Role</mat-label>
            <mat-select [(value)]="selectedNewRoleId">
              <mat-option value="">-- No Role --</mat-option>
              <mat-option *ngFor="let role of availableRoles" [value]="role.roleId">
                {{ role.roleName }}
                <span *ngIf="role.roleDescription" class="role-desc">({{ role.roleDescription }})</span>
              </mat-option>
            </mat-select>
          </mat-form-field>
        </div>

        <!-- Confirmation Section -->
        <div *ngIf="!isLoading && selectedNewRoleId" class="confirmation-section">
          <mat-card class="confirmation-card">
            <mat-card-header>
              <mat-card-title>Confirm Change</mat-card-title>
            </mat-card-header>
            <mat-card-content>
              <div class="change-preview">
                <div class="role-change">
                  <div class="from-role">
                    <span class="label">FROM:</span>
                    <span class="value">{{ currentRoleName || 'No Role' }}</span>
                  </div>
                  <mat-icon class="arrow">arrow_forward</mat-icon>
                  <div class="to-role">
                    <span class="label">TO:</span>
                    <span class="value">{{ getSelectedRoleName() }}</span>
                  </div>
                </div>
              </div>

              <!-- New Role Details -->
              <div class="role-details">
                <div class="detail-item">
                  <strong>Role Description:</strong>
                  <p>{{ getSelectedRoleDescription() || 'N/A' }}</p>
                </div>
                <div class="detail-item" *ngIf="getSelectedRolePermissions()">
                  <strong>Permissions:</strong>
                  <div class="permissions">
                    <mat-chip *ngFor="let perm of getSelectedRolePermissions()">
                      {{ perm }}
                    </mat-chip>
                  </div>
                </div>
              </div>
            </mat-card-content>
          </mat-card>
        </div>

        <!-- No Changes Warning -->
        <div *ngIf="!isLoading && !selectedNewRoleId && currentRoleName" class="no-changes-warning">
          <mat-icon>info</mat-icon>
          <p>Select a role to make changes</p>
        </div>
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
          [disabled]="isSubmitting || isLoading || !hasChanges"
        >
          <mat-icon *ngIf="!isSubmitting">update</mat-icon>
          <mat-spinner *ngIf="isSubmitting" diameter="20"></mat-spinner>
          <span>{{ isSubmitting ? 'Updating...' : 'Update Role' }}</span>
        </button>
      </mat-dialog-actions>
    </div>
  `,
  styles: [`
    .change-role-dialog {
      min-width: 500px;
      max-width: 600px;
    }

    mat-dialog-content {
      padding: 20px;
      max-height: 600px;
      overflow-y: auto;
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

    .loading-state {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 1rem;
      padding: 3rem 1rem;

      p {
        margin: 0;
        color: #757575;
      }
    }

    .current-role-section,
    .role-selection-section {
      margin-bottom: 1.5rem;
    }

    .role-card {
      margin-bottom: 1rem;

      mat-card-header {
        margin-bottom: 1rem;

        mat-card-title {
          font-size: 1rem;
          font-weight: 600;
          margin: 0;
        }
      }
    }

    .role-display {
      display: flex;
      align-items: center;
      gap: 1rem;
      padding: 1rem;
      background: #e3f2fd;
      border-radius: 4px;
      border-left: 4px solid #1976d2;

      .role-icon {
        color: #1976d2;
        font-size: 24px;
        width: 24px;
        height: 24px;
        flex-shrink: 0;
      }

      .role-name {
        font-weight: 600;
        color: #1565c0;
        font-size: 1.05rem;
        flex: 1;
      }

      .role-badge {
        display: inline-block;
        background: #4caf50;
        color: white;
        padding: 4px 8px;
        border-radius: 12px;
        font-size: 0.75rem;
        font-weight: 600;
      }
    }

    .no-role {
      padding: 1rem;
      text-align: center;
      color: #999;

      p {
        margin: 0;
      }
    }

    .full-width {
      width: 100%;
    }

    ::ng-deep .full-width .mat-mdc-form-field {
      width: 100%;
    }

    .confirmation-section {
      animation: slideIn 0.3s ease-in-out;
    }

    @keyframes slideIn {
      from {
        opacity: 0;
        transform: translateY(-10px);
      }
      to {
        opacity: 1;
        transform: translateY(0);
      }
    }

    .confirmation-card {
      background: #f0f7ff;
      border: 2px solid #1976d2;
      margin-bottom: 1rem;

      mat-card-header {
        margin-bottom: 1rem;

        mat-card-title {
          font-size: 1rem;
          font-weight: 600;
          margin: 0;
          color: #1565c0;
        }
      }
    }

    .change-preview {
      padding: 1rem;
      background: white;
      border-radius: 4px;
      margin-bottom: 1rem;

      .role-change {
        display: flex;
        align-items: center;
        gap: 1rem;

        .from-role,
        .to-role {
          flex: 1;
          padding: 0.75rem;
          border-radius: 4px;
          display: flex;
          flex-direction: column;
          gap: 0.25rem;

          .label {
            font-size: 0.75rem;
            color: #757575;
            text-transform: uppercase;
            font-weight: 600;
          }

          .value {
            font-size: 1.05rem;
            font-weight: 600;
          }
        }

        .from-role {
          background: #fce4ec;
          border-left: 3px solid #c2185b;

          .value {
            color: #880e4f;
          }
        }

        .to-role {
          background: #e8f5e9;
          border-left: 3px solid #4caf50;

          .value {
            color: #1b5e20;
          }
        }

        .arrow {
          color: #1976d2;
          font-size: 24px;
          width: 24px;
          height: 24px;
          flex-shrink: 0;
        }
      }
    }

    .role-details {
      padding: 1rem;
      background: white;
      border-radius: 4px;

      .detail-item {
        margin-bottom: 0.75rem;

        &:last-child {
          margin-bottom: 0;
        }

        strong {
          font-size: 0.875rem;
          color: #424242;
          display: block;
          margin-bottom: 0.5rem;
        }

        p {
          margin: 0;
          font-size: 0.875rem;
          color: #616161;
          line-height: 1.4;
        }
      }

      .permissions {
        display: flex;
        gap: 0.5rem;
        flex-wrap: wrap;

        mat-chip {
          height: 24px;
          padding: 0 8px;
          font-size: 0.7rem;
        }
      }
    }

    .no-changes-warning {
      display: flex;
      align-items: center;
      gap: 0.75rem;
      padding: 0.75rem;
      background: #fff3e0;
      border: 1px solid #ffe0b2;
      border-radius: 4px;
      color: #e65100;

      mat-icon {
        color: #ff9800;
        flex-shrink: 0;
      }

      p {
        margin: 0;
        font-size: 0.875rem;
      }
    }

    mat-spinner {
      display: inline-block !important;
    }
  `]
})
export class AdminUserChangeRoleDialogComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();

  availableRoles: RoleDto[] = [];
  selectedNewRoleId: string = '';
  currentRoleName: string = '';
  
  isLoading = false;
  isSubmitting = false;
  serverError: string | null = null;

  constructor(
    private adminUserService: AdminUserService,
    private snackBar: MatSnackBar,
    private cdr: ChangeDetectorRef,
    public dialogRef: MatDialogRef<AdminUserChangeRoleDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: ChangeRoleDialogData
  ) {}

  ngOnInit(): void {
    this.loadRoles();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  /**
   * Load available roles and current user role
   */
  private loadRoles(): void {
    this.isLoading = true;
    this.serverError = null;

    this.adminUserService.getAvailableRoles()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (roles) => {
          this.availableRoles = roles || [];
          this.currentRoleName = this.data.user.adminUsersRoleName || 'No role assigned';
          this.isLoading = false;
          console.log('✅ Dialog: Roles loaded:', this.availableRoles.length);
          this.cdr.markForCheck();
        },
        error: (error) => {
          console.error('❌ Dialog: Error loading roles:', error);
          this.isLoading = false;
          this.serverError = 'Failed to load roles. Please try again.';
          this.cdr.markForCheck();
        }
      });
  }

  /**
   * Get selected role name
   */
  getSelectedRoleName(): string {
    const role = this.availableRoles.find(r => r.roleId === this.selectedNewRoleId);
    return role?.roleName || 'Unknown';
  }

  /**
   * Get selected role description
   */
  getSelectedRoleDescription(): string | undefined {
    const role = this.availableRoles.find(r => r.roleId === this.selectedNewRoleId);
    return role?.roleDescription;
  }

  /**
   * Get selected role permissions
   */
  getSelectedRolePermissions(): string[] | undefined {
    const role = this.availableRoles.find(r => r.roleId === this.selectedNewRoleId);
    return role?.permissions;
  }

  /**
   * Check if there are changes
   */
  get hasChanges(): boolean {
    return this.selectedNewRoleId !== '';
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
    if (!this.selectedNewRoleId) {
      console.warn('⚠️ Dialog: No role selected');
      return;
    }

    this.isSubmitting = true;
    this.serverError = null;
    const userId = this.data.user.adminUsersId;
    const selectedRoleName = this.getSelectedRoleName();

    console.log('👥 Dialog: Changing role for user:', userId, 'New role ID:', this.selectedNewRoleId, 'New role name:', selectedRoleName);

    this.adminUserService.assignRole(userId, this.selectedNewRoleId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          console.log('✅ Dialog: Role changed successfully');
          this.isSubmitting = false;
          this.showSuccess(`Role updated to ${selectedRoleName}`);
          this.dialogRef.close({ mode: 'change-role', success: true, newRoleId: this.selectedNewRoleId });
        },
        error: (error) => {
          console.error('❌ Dialog: Error changing role:', error);
          this.isSubmitting = false;
          this.serverError = error?.error?.message || 'Failed to update role. Please try again.';
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
