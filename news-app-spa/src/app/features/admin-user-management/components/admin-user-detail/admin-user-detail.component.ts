import { Component, OnInit, OnDestroy, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil, tap } from 'rxjs/operators';

import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatDividerModule } from '@angular/material/divider';
import { MatTooltipModule } from '@angular/material/tooltip';

import { AdminUserService } from '../../services/admin-user.service';
import { AdminUserResponseDto, AdminStatus, ADMIN_STATUS_LABELS, ADMIN_STATUS_COLORS } from '../../index';

/**
 * Admin User Detail Component
 * 
 * Displays complete admin user profile with all fields from backend.
 * Read-only view showing identity, contact, security, audit, and activity data.
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Component({
  selector: 'app-admin-user-detail',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    CommonModule,
    MatProgressSpinnerModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatDividerModule,
    MatTooltipModule
  ],
  template: `
    <div class="admin-detail-container">
      <!-- Header with Back Button -->
      <div class="detail-header">
        <button mat-icon-button (click)="goBack()" matTooltip="Go back">
          <mat-icon>arrow_back</mat-icon>
        </button>
        <div class="header-title">
          <h1>Admin User Profile</h1>
          <p class="subtitle" *ngIf="user">{{ user.adminUsersUsername }} ({{ user.adminUsersEmail }})</p>
        </div>
        <div class="header-actions">
          <button mat-raised-button color="primary" (click)="editUser()" *ngIf="user">
            <mat-icon>edit</mat-icon>
            <span>Edit</span>
          </button>
        </div>
      </div>

      <!-- Loading State -->
      <div *ngIf="isLoading" class="loading-state">
        <mat-spinner></mat-spinner>
        <p>Loading user details...</p>
      </div>

      <!-- Error State -->
      <mat-card *ngIf="error && !isLoading" class="error-card">
        <mat-card-content>
          <div class="error-content">
            <mat-icon>error_outline</mat-icon>
            <div>
              <strong>Error Loading User</strong>
              <p>{{ error }}</p>
            </div>
            <button mat-stroked-button (click)="loadUserDetails()">
              Retry
            </button>
          </div>
        </mat-card-content>
      </mat-card>

      <!-- User Details -->
      <div *ngIf="user && !isLoading" class="details-grid">
        <!-- Section 1: Identity & Contact -->
        <mat-card class="detail-section">
          <mat-card-header>
            <mat-card-title>Identity & Contact</mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <div class="field-group">
              <div class="field">
                <label>User ID</label>
                <span class="value">{{ user.adminUsersId }}</span>
              </div>
              <div class="field">
                <label>Username</label>
                <span class="value">{{ user.adminUsersUsername }}</span>
              </div>
              <div class="field">
                <label>Email</label>
                <span class="value">{{ user.adminUsersEmail }}</span>
              </div>
              <div class="field">
                <label>Email Verified</label>
                <span class="value">
                  <mat-chip [highlighted]="user.adminUsersEmailVerified">
                    {{ user.adminUsersEmailVerified ? 'Yes' : 'No' }}
                  </mat-chip>
                </span>
              </div>
              <div class="field">
                <label>Phone Number</label>
                <span class="value">{{ user.adminUsersPhoneNumber || '—' }}</span>
              </div>
              <div class="field">
                <label>Phone Verified</label>
                <span class="value">
                  <mat-chip [highlighted]="user.adminUsersPhoneVerified">
                    {{ user.adminUsersPhoneVerified ? 'Yes' : 'No' }}
                  </mat-chip>
                </span>
              </div>
            </div>
          </mat-card-content>
        </mat-card>

        <!-- Section 2: Profile Information -->
        <mat-card class="detail-section">
          <mat-card-header>
            <mat-card-title>Profile Information</mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <div class="field-group">
              <div class="field">
                <label>First Name</label>
                <span class="value">{{ user.adminUsersFirstName || '—' }}</span>
              </div>
              <div class="field">
                <label>Last Name</label>
                <span class="value">{{ user.adminUsersLastName || '—' }}</span>
              </div>
              <div class="field">
                <label>Full Name</label>
                <span class="value">{{ user.adminUsersFullName || '—' }}</span>
              </div>
              <div class="field">
                <label>Avatar URL</label>
                <span class="value">
                  <a *ngIf="user.adminUsersAvatarUrl" [href]="user.adminUsersAvatarUrl" target="_blank">
                    View Avatar
                    <mat-icon>open_in_new</mat-icon>
                  </a>
                  <span *ngIf="!user.adminUsersAvatarUrl">—</span>
                </span>
              </div>
            </div>
          </mat-card-content>
        </mat-card>

        <!-- Section 3: Status & Role -->
        <mat-card class="detail-section">
          <mat-card-header>
            <mat-card-title>Status & Role</mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <div class="field-group">
              <div class="field">
                <label>Account Status</label>
                <span class="value">
                  <mat-chip [style.background-color]="getStatusColor(user.adminUsersStatus)">
                    {{ getStatusLabel(user.adminUsersStatus) }}
                  </mat-chip>
                </span>
              </div>
              <div class="field">
                <label>Account Locked</label>
                <span class="value">
                  <mat-chip [highlighted]="user.adminUsersAccountLocked">
                    {{ user.adminUsersAccountLocked ? 'Locked' : 'Unlocked' }}
                  </mat-chip>
                </span>
              </div>
              <div class="field">
                <label>Role ID</label>
                <span class="value">{{ user.adminUsersRoleId || '—' }}</span>
              </div>
              <div class="field">
                <label>Role Name</label>
                <span class="value">{{ user.adminUsersRoleName || '—' }}</span>
              </div>
            </div>
          </mat-card-content>
        </mat-card>

        <!-- Section 4: Security & MFA -->
        <mat-card class="detail-section">
          <mat-card-header>
            <mat-card-title>Security & MFA</mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <div class="field-group">
              <div class="field">
                <label>MFA Enabled</label>
                <span class="value">
                  <mat-chip [highlighted]="user.adminUsersMfaEnabled">
                    {{ user.adminUsersMfaEnabled ? 'Yes' : 'No' }}
                  </mat-chip>
                </span>
              </div>
              <div class="field">
                <label>Failed Login Attempts</label>
                <span class="value">{{ user.adminUsersFailedLoginAttempts }}</span>
              </div>
              <div class="field">
                <label>Account Lock Expires</label>
                <span class="value">{{ user.adminUsersAccountLockExpiresAt ? (user.adminUsersAccountLockExpiresAt | date:'medium') : '—' }}</span>
              </div>
              <div class="field">
                <label>Last Login</label>
                <span class="value">{{ user.adminUsersLastLogin ? (user.adminUsersLastLogin | date:'medium') : '—' }}</span>
              </div>
            </div>
          </mat-card-content>
        </mat-card>

        <!-- Section 5: Audit Trail -->
        <mat-card class="detail-section">
          <mat-card-header>
            <mat-card-title>Audit Trail</mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <div class="field-group">
              <div class="field">
                <label>Created At</label>
                <span class="value">{{ user.createdAt | date:'medium' }}</span>
              </div>
              <div class="field">
                <label>Created By</label>
                <span class="value">{{ user.createdBy || '—' }}</span>
              </div>
              <div class="field">
                <label>Updated At</label>
                <span class="value">{{ user.updatedAt ? (user.updatedAt | date:'medium') : ('—') }}</span>
              </div>
              <div class="field">
                <label>Updated By</label>
                <span class="value">{{ user.updatedBy || '—' }}</span>
              </div>
              <div class="field">
                <label>Deleted At</label>
                <span class="value">{{ user.deletedAt ? (user.deletedAt | date:'medium') : ('—') }}</span>
              </div>
              <div class="field">
                <label>Deleted By</label>
                <span class="value">{{ user.deletedBy || '—' }}</span>
              </div>
            </div>
          </mat-card-content>
        </mat-card>

        <!-- Section 6: Additional Information -->
        <mat-card class="detail-section">
          <mat-card-header>
            <mat-card-title>Additional Information</mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <div class="field-group">
              <div class="field">
                <label>Auth Provider</label>
                <span class="value">{{ user.adminUsersAuthProvider || 'Internal' }}</span>
              </div>
              <div class="field full-width">
                <label>Notes</label>
                <span class="value">{{ user.adminUsersNotes || '—' }}</span>
              </div>
            </div>
          </mat-card-content>
        </mat-card>
      </div>
    </div>
  `,
  styles: [`
    .admin-detail-container {
      display: flex;
      flex-direction: column;
      height: 100%;
      gap: 1.5rem;
      padding: 1.5rem;
    }

    .detail-header {
      display: flex;
      align-items: center;
      gap: 1rem;
      padding-bottom: 1rem;
      border-bottom: 1px solid var(--color-border, #e0e0e0);

      .header-title {
        flex: 1;

        h1 {
          margin: 0;
          font-size: 1.5rem;
          font-weight: 500;
        }

        .subtitle {
          margin: 0.25rem 0 0 0;
          font-size: 0.875rem;
          color: var(--color-text-secondary, #757575);
        }
      }

      .header-actions {
        display: flex;
        gap: 0.75rem;
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

    .error-card {
      background-color: #ffebee;
      border: 1px solid #f44336;
    }

    .error-content {
      display: flex;
      align-items: center;
      gap: 1rem;

      mat-icon {
        color: #f44336;
        font-size: 32px;
        width: 32px;
        height: 32px;
      }

      div {
        flex: 1;

        strong {
          display: block;
          color: #c62828;
        }

        p {
          margin: 0.25rem 0 0 0;
          color: #d32f2f;
        }
      }
    }

    .details-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(500px, 1fr));
      gap: 1.5rem;
      flex: 1;
      overflow-y: auto;

      @media (max-width: 900px) {
        grid-template-columns: 1fr;
      }
    }

    .detail-section {
      mat-card-header {
        margin-bottom: 1rem;

        mat-card-title {
          font-size: 1rem;
          font-weight: 600;
          margin: 0;
        }
      }

      mat-card-content {
        padding: 0;
      }
    }

    .field-group {
      display: flex;
      flex-direction: column;
      gap: 1rem;
    }

    .field {
      display: flex;
      flex-direction: column;
      gap: 0.25rem;

      &.full-width {
        grid-column: 1 / -1;
      }

      label {
        font-weight: 600;
        font-size: 0.875rem;
        color: var(--color-text-secondary, #757575);
        text-transform: uppercase;
        letter-spacing: 0.5px;
      }

      .value {
        font-size: 0.95rem;
        color: var(--color-text-primary, #212121);
        word-break: break-word;

        a {
          display: inline-flex;
          align-items: center;
          gap: 0.5rem;
          color: #2196f3;
          text-decoration: none;

          &:hover {
            text-decoration: underline;
          }

          mat-icon {
            font-size: 16px;
            width: 16px;
            height: 16px;
          }
        }

        mat-chip {
          max-width: fit-content;
        }
      }
    }
  `]
})
export class AdminUserDetailComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();
  
  user: AdminUserResponseDto | null = null;
  isLoading = false;
  error: string | null = null;
  private userId: string | null = null;

  constructor(
    private adminUserService: AdminUserService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.userId = this.route.snapshot.paramMap.get('id');
    if (this.userId) {
      this.loadUserDetails();
    } else {
      this.error = 'No user ID provided';
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  /**
   * Load user details from backend
   */
  loadUserDetails(): void {
    if (!this.userId) return;
    
    this.isLoading = true;
    this.error = null;

    this.adminUserService.getAdminUser(this.userId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (user) => {
          this.user = user;
          this.isLoading = false;
          console.log('✅ Detail: User loaded:', user.adminUsersUsername);
        },
        error: (error) => {
          console.error('❌ Detail: Error loading user:', error);
          this.error = 'Failed to load user details. Please try again.';
          this.isLoading = false;
        }
      });
  }

  /**
   * Get status label
   */
  getStatusLabel(status: AdminStatus): string {
    return ADMIN_STATUS_LABELS[status] || status;
  }

  /**
   * Get status color
   */
  getStatusColor(status: AdminStatus): string {
    return ADMIN_STATUS_COLORS[status] || '#9e9e9e';
  }

  /**
   * Go back to admin user list
   */
  goBack(): void {
    this.router.navigate(['/admin/users']);
  }

  /**
   * Edit this user (open edit dialog)
   */
  editUser(): void {
    if (this.userId) {
      // Navigate back to list with edit intent or emit event
      // For now, just go back
      this.router.navigate(['/admin/users']);
    }
  }
}
