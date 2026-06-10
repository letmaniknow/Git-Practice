import { Component, OnInit, OnDestroy, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil, tap } from 'rxjs/operators';

import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';

// Feature imports
import { AdminUserService, ADMIN_USER_OPERATION_MESSAGES } from '../index';
import { AdminUserListComponent } from '../components/admin-user-list/admin-user-list.component';
import { AdminUserFormDialogComponent } from '../components/admin-user-form-dialog/admin-user-form-dialog.component';
import { AdminUserDeleteDialogComponent } from '../components/admin-user-delete-dialog/admin-user-delete-dialog.component';
import { AdminUserChangePasswordDialogComponent } from '../components/admin-user-change-password-dialog/admin-user-change-password-dialog.component';
import { AdminUserChangeRoleDialogComponent } from '../components/admin-user-change-role-dialog/admin-user-change-role-dialog.component';

/**
 * Admin User List Page Component
 * 
 * Main page container for admin user management.
 * Handles:
 * - Page layout and structure
 * - Dialog management for add/edit/delete
 * - Error/success notifications
 * - Permission checking (ADMIN/SUPER_ADMIN only)
 * 
 * Uses Standalone Components with OnPush change detection
 * and proper memory leak prevention with destroy$ Subject.
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Component({
  selector: 'app-admin-user-list-page',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    CommonModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatIconModule,
    MatTooltipModule,
    MatButtonModule,
    MatCardModule,
    MatDialogModule,
    AdminUserListComponent
  ],
  template: `
    <div class="admin-user-management-container">
      <!-- Page Header -->
      <header class="page-header">
        <div class="header-content">
          <h1>Admin User Management</h1>
          <p class="subtitle">Manage system administrators and their permissions</p>
        </div>
        <div class="header-actions">
          <button
            class="add-admin-btn"
            (click)="onAddAdminClick()"
            matTooltip="Create a new admin user"
            mat-raised-button
            color="primary"
          >
            <mat-icon>person_add</mat-icon>
            <span>Add Admin User</span>
          </button>
        </div>
      </header>

      <!-- Content Area -->
      <main class="page-content">
        <!-- Loading Spinner -->
        <div *ngIf="(adminUserService.loading$ | async)" class="loading-overlay">
          <mat-spinner></mat-spinner>
        </div>

        <!-- Error Alert -->
        <mat-card *ngIf="(adminUserService.error$ | async) as error" class="error-card">
          <mat-card-content>
            <div class="error-message">
              <mat-icon>error_outline</mat-icon>
              <span>{{ error }}</span>
              <button mat-icon-button (click)="onDismissError()" title="Close">
                <mat-icon>close</mat-icon>
              </button>
            </div>
          </mat-card-content>
        </mat-card>

        <!-- Admin User List Component -->
        <app-admin-user-list
          [users]="(adminUserService.users$ | async) || []"
          [loading]="(adminUserService.loading$ | async) || false"
          [totalCount]="(adminUserService.totalCount$ | async) || 0"
          [currentPage]="(adminUserService.currentPage$ | async) || 0"
          [totalPages]="(adminUserService.totalPages$ | async) || 0"
          (pageChanged)="onPageChanged($event)"
          (pageSizeChanged)="onPageSizeChanged($event)"
          (rowClicked)="onRowClicked($event)"
          (editClicked)="onEditClick($event)"
          (deleteClicked)="onDeleteClick($event)"
          (statusChanged)="onStatusChanged($event)"
          (refreshClicked)="onRefreshClick()"
          (restoreClicked)="onRestoreClick($event)"
          (viewDetailsClicked)="onViewDetailsClick($event)"
          (changePasswordClicked)="onChangePasswordClick($event)"
          (manageRolesClicked)="onManageRolesClick($event)"
          (viewAuditLogsClicked)="onViewAuditLogsClick($event)"
        ></app-admin-user-list>
      </main>
    </div>
  `,
  styles: [`
    .admin-user-management-container {
      display: flex;
      flex-direction: column;
      height: 100%;
      gap: 1.5rem;
      padding: 1.5rem;
    }

    .page-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      gap: 2rem;
      padding-bottom: 1rem;
      border-bottom: 1px solid var(--color-border, #e0e0e0);

      @media (max-width: 768px) {
        flex-direction: column;
        align-items: stretch;
      }
    }

    .header-content h1 {
      margin: 0 0 0.5rem 0;
      font-size: 1.75rem;
      font-weight: 500;
      color: var(--color-text-primary, #212121);
    }

    .subtitle {
      margin: 0;
      font-size: 0.875rem;
      color: var(--color-text-secondary, #757575);
    }

    .header-actions {
      display: flex;
      gap: 0.75rem;

      @media (max-width: 768px) {
        width: 100%;

        button {
          flex: 1;
        }
      }
    }

    .add-admin-btn {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      white-space: nowrap;
    }

    .page-content {
      flex: 1;
      display: flex;
      flex-direction: column;
      gap: 1rem;
      position: relative;
    }

    .loading-overlay {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      display: flex;
      align-items: center;
      justify-content: center;
      background: rgba(255, 255, 255, 0.7);
      z-index: 100;
      border-radius: 4px;
    }

    .error-card {
      background-color: #ffebee;
      border: 1px solid #f44336;

      ::ng-deep .mat-mdc-card-content {
        padding: 1rem;
      }
    }

    .error-message {
      display: flex;
      align-items: center;
      gap: 1rem;
      color: #c62828;

      mat-icon {
        flex-shrink: 0;
      }

      span {
        flex: 1;
      }

      button {
        flex-shrink: 0;
      }
    }

    app-admin-user-list {
      flex: 1;
      display: block;
    }
  `]
})
export class AdminUserListPageComponent implements OnInit, OnDestroy {

  /** Memory leak prevention subject */
  private destroy$ = new Subject<void>();

  constructor(
    public adminUserService: AdminUserService,
    private router: Router,
    private snackBar: MatSnackBar,
    private matDialog: MatDialog
  ) {}

  // ========================================
  // Lifecycle
  // ========================================

  ngOnInit(): void {
    console.log('📄 Page: Admin User List Page initialized');
    this.loadAdminUsers();
  }

  ngOnDestroy(): void {
    console.log('🗑️ Page: Admin User List Page destroyed');
    this.destroy$.next();
    this.destroy$.complete();
  }

  // ========================================
  // Data Loading
  // ========================================

  /**
   * Load admin users from service
   */
  private loadAdminUsers(): void {
    this.adminUserService.loadAdminUsers()
      .pipe(
        tap(() => {
          console.log('✅ Page: Admin users loaded successfully');
        }),
        takeUntil(this.destroy$)
      )
      .subscribe({
        next: () => {
          // Data updated via service's BehaviorSubjects
        },
        error: (error: any) => {
          console.error('❌ Page: Error loading admin users:', error);
          this.showError(ADMIN_USER_OPERATION_MESSAGES.loadError);
        }
      });
  }

  // ========================================
  // User Interactions
  // ========================================

  /**
   * Handle add admin button click - Open create dialog
   */
  onAddAdminClick(): void {
    console.log('➕ Page: Add admin clicked');
    
    const dialogRef = this.matDialog.open(AdminUserFormDialogComponent, {
      width: '90%',
      maxWidth: '800px',
      data: { mode: 'create' },
      disableClose: true
    });

    dialogRef.afterClosed()
      .pipe(takeUntil(this.destroy$))
      .subscribe((result) => {
        if (result && result.mode === 'create') {
          console.log('✅ Page: Admin user created successfully');
          this.showSuccess('Admin user created successfully');
          this.loadAdminUsers();
        }
      });
  }

  /**
   * Handle page change from table pagination
   */
  onPageChanged(page: number): void {
    console.log('📄 Page: Page changed to:', page);
    this.adminUserService.setPagination(page, 10);
    this.loadAdminUsers();
  }

  /**
   * Handle page size change from table pagination
   */
  onPageSizeChanged(pageSize: number): void {
    console.log('📊 Page: Page size changed to:', pageSize);
    this.adminUserService.setPagination(0, pageSize);
    this.loadAdminUsers();
  }

  /**
   * Handle table row click
   */
  onRowClicked(userId: string): void {
    console.log('👤 Page: Row clicked for user:', userId);
    // TODO: Navigate to detail view or open detail dialog
  }

  /**
   * Handle edit button click - Always fetch fresh user data
   */
  onEditClick(userId: string): void {
    console.log('✏️ Page: Edit clicked for user:', userId);
    
    // Always fetch fresh data from backend to ensure we have all fields
    this.adminUserService.getAdminUser(userId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (retrievedUser) => {
          this.openEditDialog(retrievedUser);
        },
        error: (error) => {
          console.error('❌ Page: Error loading user for edit:', error);
          this.showError('Failed to load user for editing');
        }
      });
  }

  /**
   * Open edit dialog with user data
   */
  private openEditDialog(user: any): void {
    const dialogRef = this.matDialog.open(AdminUserFormDialogComponent, {
      width: '90%',
      maxWidth: '800px',
      data: { mode: 'edit', user },
      disableClose: true
    });

    dialogRef.afterClosed()
      .pipe(takeUntil(this.destroy$))
      .subscribe((result) => {
        if (result && result.mode === 'edit') {
          console.log('✅ Page: Admin user updated successfully');
          this.showSuccess('Admin user updated successfully');
          this.loadAdminUsers();
        }
      });
  }

  /**
   * Open change password dialog
   */
  private openChangePasswordDialog(user: any): void {
    const dialogRef = this.matDialog.open(AdminUserChangePasswordDialogComponent, {
      width: '90%',
      maxWidth: '500px',
      data: { user },
      disableClose: true
    });

    dialogRef.afterClosed()
      .pipe(takeUntil(this.destroy$))
      .subscribe((result) => {
        if (result && result.mode === 'change-password' && result.success) {
          console.log('✅ Page: Password changed successfully');
          this.showSuccess('Password changed successfully');
        }
      });
  }

  /**
   * Open change role dialog
   */
  private openChangeRoleDialog(user: any): void {
    const dialogRef = this.matDialog.open(AdminUserChangeRoleDialogComponent, {
      width: '90%',
      maxWidth: '600px',
      data: { user },
      disableClose: true
    });

    dialogRef.afterClosed()
      .pipe(takeUntil(this.destroy$))
      .subscribe((result) => {
        if (result && result.mode === 'change-role' && result.success) {
          console.log('✅ Page: Role changed successfully, refreshing list...');
          this.showSuccess('Admin role changed successfully');
          
          // Force refresh with a small delay to ensure backend cache is evicted
          setTimeout(() => {
            this.loadAdminUsers();
          }, 500);
        }
      });
  }

  /**
   * Handle delete button click - Open delete confirmation dialog
   */
  onDeleteClick(userId: string): void {
    console.log('🗑️ Page: Delete clicked for user:', userId);
    
    const dialogRef = this.matDialog.open(AdminUserDeleteDialogComponent, {
      width: '90%',
      maxWidth: '500px',
      data: { userId },
      disableClose: false
    });

    dialogRef.afterClosed()
      .pipe(takeUntil(this.destroy$))
      .subscribe((confirmed: boolean) => {
        if (confirmed) {
          console.log('✅ Page: Admin user deleted successfully');
          this.showSuccess('Admin user deleted successfully');
          this.loadAdminUsers();
        }
      });
  }

  /**
   * Handle status change action
   */
  onStatusChanged(event: { userId: string; newStatus: string }): void {
    console.log('🔄 Page: Status changed for user:', event.userId, 'to:', event.newStatus);
    
    const { userId, newStatus } = event;
    let statusObservable;

    switch (newStatus) {
      case 'ACTIVATE':
        statusObservable = this.adminUserService.activateAdminUser(userId);
        break;
      case 'DEACTIVATE':
        statusObservable = this.adminUserService.deactivateAdminUser(userId);
        break;
      case 'SUSPEND':
        statusObservable = this.adminUserService.suspendAdminUser(userId);
        break;
      default:
        console.warn('Unknown status:', newStatus);
        return;
    }

    statusObservable
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.showSuccess(`Admin user ${newStatus.toLowerCase()}ed successfully`);
          this.loadAdminUsers();
        },
        error: (error: any) => {
          console.error('❌ Page: Error changing status:', error);
          this.showError(`Failed to ${newStatus.toLowerCase()} admin user`);
        }
      });
  }

  /**
   * Handle restore button click
   */
  onRestoreClick(userId: string): void {
    console.log('🔄 Page: Restore clicked for user:', userId);
    
    if (!confirm('Are you sure you want to restore this admin user?')) {
      return;
    }

    this.adminUserService.restoreAdminUser(userId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.showSuccess('Admin user restored successfully');
          this.loadAdminUsers();
        },
        error: (error: any) => {
          console.error('❌ Page: Error restoring admin:', error);
          this.showError('Failed to restore admin user');
        }
      });
  }

  /**
   * Handle view details click
   */
  onViewDetailsClick(userId: string): void {
    console.log('👁️ Page: View details clicked for user:', userId);
    this.router.navigate(['/admin/users', userId, 'profile']);
  }

  /**
   * Handle change password click
   */
  onChangePasswordClick(userId: string): void {
    console.log('🔐 Page: Change password clicked for user:', userId);
    
    // Fetch the user first
    this.adminUserService.getAdminUser(userId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (user) => {
          this.openChangePasswordDialog(user);
        },
        error: (error) => {
          console.error('❌ Page: Error loading user for password change:', error);
          this.showError('Failed to load user for password change');
        }
      });
  }

  /**
   * Handle manage roles click (change role for user)
   */
  onManageRolesClick(userId: string): void {
    console.log('👮 Page: Change role clicked for user:', userId);
    
    // Fetch the user first
    this.adminUserService.getAdminUser(userId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (user) => {
          this.openChangeRoleDialog(user);
        },
        error: (error) => {
          console.error('❌ Page: Error loading user for role change:', error);
          this.showError('Failed to load user for role change');
        }
      });
  }

  /**
   * Handle view audit logs click
   */
  onViewAuditLogsClick(userId: string): void {
    console.log('📋 Page: View audit logs clicked for user:', userId);
    // TODO: Open audit logs dialog or navigate to audit page
  }

  /**
   * Handle refresh button click
   */
  onRefreshClick(): void {
    console.log('🔃 Page: Refresh clicked');
    this.loadAdminUsers();
  }

  /**
   * Dismiss error message
   */
  onDismissError(): void {
    // Clear error by calling loadAdminUsers which resets error on start
    this.loadAdminUsers();
  }

  // ========================================
  // Notifications
  // ========================================

  /**
   * Show error message
   */
  private showError(message: string): void {
    this.snackBar.open(message, 'Close', {
      duration: 5000,
      horizontalPosition: 'end',
      verticalPosition: 'top',
      panelClass: ['error-snackbar']
    });
  }

  /**
   * Show success message
   */
  private showSuccess(message: string): void {
    this.snackBar.open(message, 'Close', {
      duration: 3000,
      horizontalPosition: 'end',
      verticalPosition: 'top',
      panelClass: ['success-snackbar']
    });
  }
}
