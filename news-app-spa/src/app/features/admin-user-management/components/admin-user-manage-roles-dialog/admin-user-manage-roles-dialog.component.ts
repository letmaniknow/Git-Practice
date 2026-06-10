import { Component, Inject, OnInit, OnDestroy, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Subject, forkJoin } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

import { MatSnackBar } from '@angular/material/snack-bar';
import {
  MatDialogModule,
  MAT_DIALOG_DATA,
  MatDialogRef
} from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatTooltipModule } from '@angular/material/tooltip';

import { AdminUserService } from '../../services/admin-user.service';
import { AdminUserResponseDto } from '../../index';

export interface ManageRolesDialogData {
  user: AdminUserResponseDto;
}

export interface RoleDto {
  roleId: string;
  roleName: string;
  roleDescription?: string;
  permissions?: string[];
}

/**
 * Manage Roles Dialog Component
 * 
 * Handles role assignment and revocation for admin users.
 * Displays available roles with checkboxes for multi-select.
 * Shows currently assigned roles and permissions.
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Component({
  selector: 'app-admin-user-manage-roles-dialog',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatButtonModule,
    MatCheckboxModule,
    MatProgressSpinnerModule,
    MatIconModule,
    MatListModule,
    MatCardModule,
    MatChipsModule,
    MatTooltipModule
  ],
  template: `
    <div class="manage-roles-dialog">
      <!-- Dialog Header -->
      <h2 mat-dialog-title>
        Manage Roles
      </h2>

      <!-- Dialog Content -->
      <mat-dialog-content>
        <div class="user-info">
          <p><strong>User:</strong> {{ data.user.adminUsersUsername }}</p>
          <p><strong>Email:</strong> {{ data.user.adminUsersEmail }}</p>
          <p><strong>Current Role:</strong> {{ data.user.adminUsersRoleName || 'No role assigned' }}</p>
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

        <!-- Content -->
        <div *ngIf="!isLoading" class="roles-container">
          <!-- Available Roles Section -->
          <mat-card class="roles-section">
            <mat-card-header>
              <mat-card-title>Available Roles</mat-card-title>
              <p class="subtitle">Select roles to assign to this admin user</p>
            </mat-card-header>

            <mat-card-content *ngIf="availableRoles && availableRoles.length > 0">
              <mat-selection-list #roleList>
                <mat-list-option
                  *ngFor="let role of availableRoles"
                  [value]="role.roleId"
                  [selected]="isRoleSelected(role.roleId)"
                  (change)="onRoleToggle(role.roleId)"
                  class="role-option"
                >
                  <div class="role-content">
                    <div class="role-header">
                      <strong>{{ role.roleName }}</strong>
                      <span class="role-id" *ngIf="isRoleSelected(role.roleId)">
                        <mat-icon>check_circle</mat-icon>
                      </span>
                    </div>
                    <p class="role-description" *ngIf="role.roleDescription">
                      {{ role.roleDescription }}
                    </p>
                    <div class="role-permissions" *ngIf="role.permissions && role.permissions.length > 0">
                      <p class="permissions-label">Permissions:</p>
                      <div class="permissions-list">
                        <mat-chip *ngFor="let permission of role.permissions | slice:0:3">
                          {{ permission }}
                        </mat-chip>
                        <span *ngIf="role.permissions.length > 3" class="more-permissions">
                          +{{ role.permissions.length - 3 }} more
                        </span>
                      </div>
                    </div>
                  </div>
                </mat-list-option>
              </mat-selection-list>
            </mat-card-content>

            <mat-card-content *ngIf="!availableRoles || availableRoles.length === 0">
              <p class="no-roles">No roles available</p>
            </mat-card-content>
          </mat-card>

          <!-- Selected Roles Summary -->
          <mat-card class="roles-summary" *ngIf="selectedRoles.length > 0">
            <mat-card-header>
              <mat-card-title>Selected Roles ({{ selectedRoles.length }})</mat-card-title>
            </mat-card-header>
            <mat-card-content>
              <div class="selected-roles-list">
                <div *ngFor="let roleId of selectedRoles" class="selected-role-item">
                  <mat-icon>check</mat-icon>
                  <span>{{ getRoleNameById(roleId) }}</span>
                  <button 
                    type="button"
                    mat-icon-button
                    (click)="removeRole(roleId)"
                    matTooltip="Remove role"
                  >
                    <mat-icon>close</mat-icon>
                  </button>
                </div>
              </div>
            </mat-card-content>
          </mat-card>

          <!-- No Roles Selected Warning -->
          <div *ngIf="selectedRoles.length === 0" class="no-selection-warning">
            <mat-icon>info</mat-icon>
            <p>No roles selected. Admin will have default permissions only.</p>
          </div>
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
          <mat-icon *ngIf="!isSubmitting">save</mat-icon>
          <mat-spinner *ngIf="isSubmitting" diameter="20"></mat-spinner>
          <span>{{ isSubmitting ? 'Saving...' : 'Save Roles' }}</span>
        </button>
      </mat-dialog-actions>
    </div>
  `,
  styles: [`
    .manage-roles-dialog {
      min-width: 500px;
      max-width: 700px;
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
        color: var(--color-text-secondary, #757575);
      }
    }

    .roles-container {
      display: flex;
      flex-direction: column;
      gap: 1.5rem;
    }

    .roles-section,
    .roles-summary {
      mat-card-header {
        margin-bottom: 1rem;

        mat-card-title {
          font-size: 1rem;
          font-weight: 600;
          margin: 0;
        }

        .subtitle {
          margin: 0.5rem 0 0 0;
          font-size: 0.75rem;
          color: #757575;
        }
      }

      mat-card-content {
        padding: 0;
      }
    }

    mat-selection-list {
      border: 1px solid #e0e0e0;
      border-radius: 4px;
      padding: 0;
    }

    .role-option {
      padding: 0.75rem !important;
      border-bottom: 1px solid #f0f0f0;
      align-items: flex-start !important;

      &:last-child {
        border-bottom: none;
      }

      .role-content {
        flex: 1;
        margin-left: 0.5rem;
      }

      .role-header {
        display: flex;
        align-items: center;
        gap: 0.5rem;

        strong {
          font-size: 0.95rem;
        }

        .role-id {
          margin-left: auto;
          color: #4caf50;
          display: flex;
          align-items: center;

          mat-icon {
            font-size: 18px;
            width: 18px;
            height: 18px;
          }
        }
      }

      .role-description {
        margin: 0.25rem 0 0 0;
        font-size: 0.75rem;
        color: #757575;
      }

      .role-permissions {
        margin: 0.5rem 0 0 0;

        .permissions-label {
          margin: 0;
          font-size: 0.7rem;
          font-weight: 600;
          color: #9e9e9e;
          text-transform: uppercase;
        }

        .permissions-list {
          display: flex;
          gap: 0.5rem;
          flex-wrap: wrap;
          margin-top: 0.25rem;

          mat-chip {
            height: 24px;
            padding: 0 8px;
            font-size: 0.7rem;
          }

          .more-permissions {
            font-size: 0.7rem;
            color: #9e9e9e;
            padding: 2px 0;
          }
        }
      }
    }

    .no-roles {
      text-align: center;
      color: #9e9e9e;
      padding: 1rem;
      margin: 0;
    }

    .selected-roles-list {
      display: flex;
      flex-direction: column;
      gap: 0.75rem;

      .selected-role-item {
        display: flex;
        align-items: center;
        gap: 0.75rem;
        padding: 0.75rem;
        background: #e8f5e9;
        border-radius: 4px;
        border: 1px solid #c8e6c9;

        mat-icon {
          color: #4caf50;
          font-size: 18px;
          width: 18px;
          height: 18px;
          flex-shrink: 0;
        }

        span {
          flex: 1;
          font-size: 0.95rem;
          color: #1b5e20;
          font-weight: 500;
        }

        button {
          margin: 0;
          padding: 4px;

          mat-icon {
            color: #757575;
          }

          &:hover mat-icon {
            color: #f44336;
          }
        }
      }
    }

    .no-selection-warning {
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
export class AdminUserManageRolesDialogComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();

  availableRoles: RoleDto[] = [];
  selectedRoles: string[] = [];
  originalRoles: string[] = [];
  
  isLoading = false;
  isSubmitting = false;
  serverError: string | null = null;

  constructor(
    private adminUserService: AdminUserService,
    private snackBar: MatSnackBar,
    private cdr: ChangeDetectorRef,
    public dialogRef: MatDialogRef<AdminUserManageRolesDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: ManageRolesDialogData
  ) {}

  ngOnInit(): void {
    this.loadRolesAndAssignments();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  /**
   * Load available roles and current user role assignments
   */
  private loadRolesAndAssignments(): void {
    this.isLoading = true;
    this.serverError = null;

    // Fetch available roles and current user's roles in parallel
    forkJoin({
      roles: this.adminUserService.getAvailableRoles(),
      userRoles: this.adminUserService.getAdminRoles(this.data.user.adminUsersId)
    })
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (result) => {
          this.availableRoles = result.roles || [];
          this.selectedRoles = (result.userRoles || []).map((role: any) => role.roleId || role);
          this.originalRoles = [...this.selectedRoles];
          this.isLoading = false;
          console.log('✅ Dialog: Roles loaded:', this.availableRoles.length, 'Selected:', this.selectedRoles.length);
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
   * Check if a role is selected
   */
  isRoleSelected(roleId: string): boolean {
    return this.selectedRoles.includes(roleId);
  }

  /**
   * Toggle role selection
   */
  onRoleToggle(roleId: string): void {
    const index = this.selectedRoles.indexOf(roleId);
    if (index > -1) {
      this.selectedRoles.splice(index, 1);
    } else {
      this.selectedRoles.push(roleId);
    }
    this.cdr.markForCheck();
  }

  /**
   * Remove role from selection
   */
  removeRole(roleId: string): void {
    this.onRoleToggle(roleId);
  }

  /**
   * Get role name by ID
   */
  getRoleNameById(roleId: string): string {
    const role = this.availableRoles.find(r => r.roleId === roleId);
    return role?.roleName || roleId;
  }

  /**
   * Check if there are changes
   */
  get hasChanges(): boolean {
    if (this.selectedRoles.length !== this.originalRoles.length) {
      return true;
    }
    return !this.selectedRoles.every(role => this.originalRoles.includes(role));
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
    if (!this.hasChanges) {
      console.warn('⚠️ Dialog: No changes to save');
      return;
    }

    this.isSubmitting = true;
    this.serverError = null;
    const userId = this.data.user.adminUsersId;

    console.log('👥 Dialog: Updating roles for user:', userId, 'Roles:', this.selectedRoles);

    // Get roles to add and remove
    const rolesToAdd = this.selectedRoles.filter(r => !this.originalRoles.includes(r));
    const rolesToRemove = this.originalRoles.filter(r => !this.selectedRoles.includes(r));

    // Call service to update roles
    const updateObservables: any[] = [];

    rolesToAdd.forEach(roleId => {
      updateObservables.push(
        this.adminUserService.assignRole(userId, roleId)
      );
    });

    rolesToRemove.forEach(roleId => {
      updateObservables.push(
        this.adminUserService.revokeRole(userId, roleId)
      );
    });

    if (updateObservables.length === 0) {
      this.isSubmitting = false;
      return;
    }

    forkJoin(updateObservables)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          console.log('✅ Dialog: Roles updated successfully');
          this.isSubmitting = false;
          this.showSuccess('Roles updated successfully');
          this.dialogRef.close({ mode: 'manage-roles', success: true });
        },
        error: (error) => {
          console.error('❌ Dialog: Error updating roles:', error);
          this.isSubmitting = false;
          this.serverError = error?.error?.message || 'Failed to update roles. Please try again.';
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
