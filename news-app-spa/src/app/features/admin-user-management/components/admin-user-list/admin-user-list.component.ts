import {
  Component,
  Input,
  Output,
  EventEmitter,
  ChangeDetectionStrategy,
  ViewChild,
  AfterViewInit
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatPaginatorModule, PageEvent, MatPaginator } from '@angular/material/paginator';
import { MatSortModule, MatSort } from '@angular/material/sort';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatChipsModule } from '@angular/material/chips';
import { MatMenuModule } from '@angular/material/menu';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatDividerModule } from '@angular/material/divider';

import {
  AdminUserViewModel,
  ADMIN_USER_TABLE_COLUMNS,
  ADMIN_USER_COLUMN_HEADERS,
  ADMIN_STATUS_LABELS,
  ADMIN_STATUS_COLORS,
  ADMIN_USER_PAGINATION_DEFAULTS
} from '../../index';

/**
 * Admin User List Component
 * 
 * Displays admin users in a Material Data Table with:
 * - Sortable columns
 * - Pagination
 * - Row actions (edit, delete, status change)
 * - Search/filter
 * - Responsive design
 * 
 * Input: Admin users array
 * Outputs: Page changes, row/action clicks, etc.
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Component({
  selector: 'app-admin-user-list',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    CommonModule,
    FormsModule,
    MatTableModule,
    MatPaginatorModule,
    MatSortModule,
    MatIconModule,
    MatButtonModule,
    MatTooltipModule,
    MatChipsModule,
    MatMenuModule,
    MatInputModule,
    MatFormFieldModule,
    MatProgressBarModule,
    MatDividerModule
  ],
  template: `
    <div class="admin-user-list-container">
      <!-- Search Bar -->
      <div class="search-bar">
        <mat-form-field class="search-field">
          <mat-label>Search by username, email, or name</mat-label>
          <mat-icon matPrefix>search</mat-icon>
          <input
            matInput
            [(ngModel)]="searchQuery"
            (ngModelChange)="onSearch($event)"
            placeholder="Search..."
          />
          <button
            mat-icon-button
            matSuffix
            *ngIf="searchQuery"
            (click)="clearSearch()"
            title="Clear search"
          >
            <mat-icon>close</mat-icon>
          </button>
        </mat-form-field>

        <button
          mat-icon-button
          (click)="onRefresh()"
          matTooltip="Refresh list"
          [disabled]="loading"
        >
          <mat-icon [class.rotating]="loading">refresh</mat-icon>
        </button>
      </div>

      <!-- Loading Bar -->
      <mat-progress-bar *ngIf="loading" mode="indeterminate"></mat-progress-bar>

      <!-- Data Table -->
      <div class="table-wrapper">
        <table
          mat-table
          [dataSource]="dataSource"
          matSort
          (matSortChange)="onSortChange($event)"
          class="admin-user-table"
        >
          <!-- Username Column -->
          <ng-container matColumnDef="username">
            <th mat-header-cell *matHeaderCellDef mat-sort-header>Username</th>
            <td mat-cell *matCellDef="let element">
              <div class="cell-content username-cell">
                <span class="username-value">{{ element.adminUsersUsername }}</span>
              </div>
            </td>
          </ng-container>

          <!-- Email Column -->
          <ng-container matColumnDef="email">
            <th mat-header-cell *matHeaderCellDef mat-sort-header>Email</th>
            <td mat-cell *matCellDef="let element">
              <div class="cell-content email-cell">
                <a [href]="'mailto:' + element.adminUsersEmail">{{ element.adminUsersEmail }}</a>
              </div>
            </td>
          </ng-container>

          <!-- Full Name Column -->
          <ng-container matColumnDef="fullName">
            <th mat-header-cell *matHeaderCellDef>Full Name</th>
            <td mat-cell *matCellDef="let element">
              <div class="cell-content full-name-cell">
                {{ element.adminUsersFullName || '—' }}
              </div>
            </td>
          </ng-container>

          <!-- Status Column -->
          <ng-container matColumnDef="status">
            <th mat-header-cell *matHeaderCellDef mat-sort-header>Status</th>
            <td mat-cell *matCellDef="let element">
              <mat-chip
                class="status-chip"
                [style.background-color]="getStatusColor(element.adminUsersStatus)"
                [style.color]="getStatusTextColor(element.adminUsersStatus)"
              >
                {{ getStatusLabel(element.adminUsersStatus) }}
              </mat-chip>
            </td>
          </ng-container>

          <!-- Role Column -->
          <ng-container matColumnDef="role">
            <th mat-header-cell *matHeaderCellDef>Role</th>
            <td mat-cell *matCellDef="let element">
              <div class="cell-content role-cell">
                <span class="role-badge">{{ element.adminUsersRoleName }}</span>
              </div>
            </td>
          </ng-container>

          <!-- Last Login Column -->
          <ng-container matColumnDef="lastLogin">
            <th mat-header-cell *matHeaderCellDef mat-sort-header>Last Login</th>
            <td mat-cell *matCellDef="let element">
              <div class="cell-content last-login-cell">
                {{ element.adminUsersLastLogin ? (element.adminUsersLastLogin | date: 'short') : '—' }}
              </div>
            </td>
          </ng-container>

          <!-- Created Column -->
          <ng-container matColumnDef="createdAt">
            <th mat-header-cell *matHeaderCellDef mat-sort-header>Created</th>
            <td mat-cell *matCellDef="let element">
              <div class="cell-content created-at-cell">
                {{ element.createdAt | date: 'short' }}
              </div>
            </td>
          </ng-container>

          <!-- Actions Column -->
          <ng-container matColumnDef="actions">
            <th mat-header-cell *matHeaderCellDef>Actions</th>
            <td mat-cell *matCellDef="let element">
              <div class="actions-cell">
                <button
                  mat-icon-button
                  (click)="onEdit(element.adminUsersId)"
                  matTooltip="Edit"
                  matTooltipPosition="right"
                  class="edit-btn"
                >
                  <mat-icon>edit</mat-icon>
                </button>

                <button
                  mat-icon-button
                  [matMenuTriggerFor]="menu"
                  matTooltip="More"
                  matTooltipPosition="right"
                  class="more-btn"
                >
                  <mat-icon>more_vert</mat-icon>
                </button>

                <mat-menu #menu="matMenu">
                  <!-- Status Actions -->
                  <button
                    mat-menu-item
                    (click)="onStatusChange(element, 'ACTIVATE')"
                    [disabled]="element.adminUsersStatus === 'ACTIVE'"
                  >
                    <mat-icon>check_circle</mat-icon>
                    <span>Activate</span>
                  </button>
                  <button
                    mat-menu-item
                    (click)="onStatusChange(element, 'SUSPEND')"
                    [disabled]="element.adminUsersStatus === 'SUSPENDED'"
                  >
                    <mat-icon>pause_circle</mat-icon>
                    <span>Suspend</span>
                  </button>
                  <button
                    mat-menu-item
                    (click)="onStatusChange(element, 'DEACTIVATE')"
                    [disabled]="element.adminUsersStatus === 'INACTIVE'"
                  >
                    <mat-icon>cancel</mat-icon>
                    <span>Deactivate</span>
                  </button>

                  <!-- Restore Action (visible only for deleted users) -->
                  <button
                    mat-menu-item
                    (click)="onRestore(element.adminUsersId)"
                    *ngIf="element.adminUsersStatus === 'DELETED' || element.deletedAt"
                  >
                    <mat-icon>restore</mat-icon>
                    <span>Restore</span>
                  </button>

                  <mat-divider></mat-divider>

                  <!-- Management Actions -->
                  <button mat-menu-item (click)="onViewDetails(element.adminUsersId)">
                    <mat-icon>info</mat-icon>
                    <span>View Details</span>
                  </button>
                  <button mat-menu-item (click)="onChangePassword(element.adminUsersId)">
                    <mat-icon>lock</mat-icon>
                    <span>Change Password</span>
                  </button>
                  <button mat-menu-item (click)="onManageRoles(element.adminUsersId)">
                    <mat-icon>security</mat-icon>
                    <span>Manage Roles</span>
                  </button>
                  <button mat-menu-item (click)="onViewAuditLogs(element.adminUsersId)">
                    <mat-icon>history</mat-icon>
                    <span>Audit Logs</span>
                  </button>

                  <mat-divider></mat-divider>

                  <!-- Danger Zone -->
                  <button mat-menu-item (click)="onDelete(element.adminUsersId)" class="delete-action">
                    <mat-icon>delete</mat-icon>
                    <span>Delete Permanently</span>
                  </button>
                </mat-menu>
              </div>
            </td>
          </ng-container>

          <!-- Header Row -->
          <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>

          <!-- Data Rows -->
          <tr
            mat-row
            *matRowDef="let element; columns: displayedColumns;"
            [class.selected-row]="element.isSelected"
            (click)="onRowClick(element.adminUsersId)"
            class="data-row"
          ></tr>
        </table>
      </div>

      <!-- Empty State -->
      <div *ngIf="!loading && (users || []).length === 0" class="empty-state">
        <mat-icon>people_outline</mat-icon>
        <p>No admin users found</p>
        <p class="empty-hint">Click "Add Admin User" to create a new administrator</p>
      </div>

      <!-- Paginator -->
      <mat-paginator
        [length]="totalCount"
        [pageSize]="pageSize"
        [pageSizeOptions]="pageSizeOptions"
        showFirstLastButtons
        (page)="onPageChange($event)"
      ></mat-paginator>
    </div>
  `,
  styles: [`
    .admin-user-list-container {
      display: flex;
      flex-direction: column;
      height: 100%;
      gap: 1rem;
    }

    .search-bar {
      display: flex;
      gap: 1rem;
      align-items: flex-end;

      @media (max-width: 768px) {
        flex-direction: column;
        align-items: stretch;
      }
    }

    .search-field {
      flex: 1;
      min-width: 300px;

      @media (max-width: 768px) {
        min-width: 100%;
      }
    }

    ::ng-deep .mat-mdc-form-field {
      width: 100%;
    }

    mat-progress-bar {
      ::ng-deep {
        height: 4px;
      }
    }

    .table-wrapper {
      flex: 1;
      overflow: auto;
      border: 1px solid var(--color-border, #e0e0e0);
      border-radius: 4px;
      background: white;

      @media (max-width: 768px) {
        border: none;
        border-radius: 0;
      }
    }

    .admin-user-table {
      width: 100%;
      border-collapse: collapse;

      th {
        background-color: var(--color-surface, #f5f5f5);
        font-weight: 500;
        padding: 1rem;
        text-align: left;
        border-bottom: 2px solid var(--color-border, #e0e0e0);

        /* Center align for Status and Actions columns */
        &:nth-child(4),
        &:last-child {
          text-align: center;
        }
      }

      td {
        padding: 1rem;
        border-bottom: 1px solid var(--color-border, #e0e0e0);
      }

      .data-row {
        cursor: pointer;
        transition: background-color 0.2s ease;

        &:hover {
          background-color: var(--color-hover, #f5f5f5);
        }

        &.selected-row {
          background-color: var(--color-selected, #e3f2fd);
        }
      }
    }

    .cell-content {
      display: flex;
      align-items: center;
      gap: 0.5rem;
    }

    /* Actions column - Tight fit for 4 icons */
    th:last-child,
    td:last-child {
      width: 7% !important;
      min-width: 100px !important;
      max-width: 120px !important;
      box-sizing: border-box;
    }

    .username-cell {
      font-weight: 500;
      color: var(--color-primary, #1976d2);
    }

    .email-cell a {
      color: var(--color-primary, #1976d2);
      text-decoration: none;

      &:hover {
        text-decoration: underline;
      }
    }

    .status-chip {
      display: inline-flex;
      padding: 0.25rem 0.75rem;
      border-radius: 16px;
      font-size: 0.75rem;
      font-weight: 500;
    }

    .role-badge {
      background-color: var(--color-surface, #f5f5f5);
      padding: 0.25rem 0.75rem;
      border-radius: 4px;
      font-size: 0.875rem;
    }

    .actions-cell {
      display: flex;
      gap: 4px;
      justify-content: flex-start;
      align-items: center;
      width: auto;
    }

    .actions-cell button[mat-icon-button] {
      width: 32px !important;
      height: 32px !important;
      padding: 0 !important;
      background-color: transparent !important;
      color: #666 !important;
      border-radius: 4px !important;
      transition: all 0.2s ease !important;
      display: inline-flex !important;
      align-items: center !important;
      justify-content: center !important;
      flex-shrink: 0;
    }

    /* Edit Button - Blue hover */
    .actions-cell button.edit-btn:hover {
      color: #1976d2 !important;
      background-color: rgba(25, 118, 210, 0.08) !important;
    }

    /* More Menu Button - Gray hover */
    .actions-cell button.more-btn:hover {
      color: #333 !important;
      background-color: rgba(0, 0, 0, 0.05) !important;
    }

    .actions-cell button[mat-icon-button]:active {
      transform: scale(0.95) !important;
    }

    .actions-cell button[mat-icon-button] mat-icon {
      color: inherit !important;
    }

    .actions-cell button[mat-icon-button]:disabled {
      background-color: transparent !important;
      color: #ccc !important;
      cursor: not-allowed !important;
    }

    .edit-btn,
    .more-btn {
      /* Styled via button[mat-icon-button] selector above */
    }

    .delete-action {
      color: #f44336;

      mat-icon {
        color: #f44336;
      }
    }

    .empty-state {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 3rem 1rem;
      color: var(--color-text-secondary, #757575);
      gap: 1rem;

      mat-icon {
        font-size: 48px;
        width: 48px;
        height: 48px;
        color: var(--color-text-secondary, #757575);
      }

      p {
        margin: 0;

        &:first-of-type {
          font-size: 1.125rem;
          font-weight: 500;
        }

        &.empty-hint {
          font-size: 0.875rem;
        }
      }
    }

    .rotating {
      animation: rotate 2s linear infinite;
    }

    @keyframes rotate {
      from {
        transform: rotate(0deg);
      }
      to {
        transform: rotate(360deg);
      }
    }

    ::ng-deep .mat-mdc-paginator {
      border-top: 1px solid var(--color-border, #e0e0e0);
    }
  `]
})
export class AdminUserListComponent implements AfterViewInit {

  @Input() users: AdminUserViewModel[] = [];
  @Input() loading = false;
  @Input() totalCount = 0;
  @Input() currentPage = 0;
  @Input() totalPages = 0;

  @Output() pageChanged = new EventEmitter<number>();
  @Output() pageSizeChanged = new EventEmitter<number>();
  @Output() rowClicked = new EventEmitter<string>();
  @Output() editClicked = new EventEmitter<string>();
  @Output() deleteClicked = new EventEmitter<string>();
  @Output() statusChanged = new EventEmitter<{ userId: string; newStatus: string }>();
  @Output() refreshClicked = new EventEmitter<void>();
  @Output() restoreClicked = new EventEmitter<string>();
  @Output() viewDetailsClicked = new EventEmitter<string>();
  @Output() changePasswordClicked = new EventEmitter<string>();
  @Output() manageRolesClicked = new EventEmitter<string>();
  @Output() viewAuditLogsClicked = new EventEmitter<string>();

  @ViewChild(MatPaginator) paginator: MatPaginator | null = null;
  @ViewChild(MatSort) sort: MatSort | null = null;

  displayedColumns = ADMIN_USER_TABLE_COLUMNS as unknown as string[];
  dataSource = new MatTableDataSource<AdminUserViewModel>([]);
  searchQuery = '';
  pageSize = ADMIN_USER_PAGINATION_DEFAULTS.pageSize;
  pageSizeOptions = ADMIN_USER_PAGINATION_DEFAULTS.pageSizeOptions;

  ngAfterViewInit(): void {
    if (this.paginator) {
      this.dataSource.paginator = this.paginator;
    }
    if (this.sort) {
      this.dataSource.sort = this.sort;
    }
  }

  ngOnChanges(): void {
    this.dataSource.data = this.users;
  }

  // ========================================
  // Search
  // ========================================

  onSearch(query: string): void {
    console.log('🔍 Component: Search:', query);
    // TODO: Call service search method
  }

  clearSearch(): void {
    this.searchQuery = '';
    this.onSearch('');
  }

  // ========================================
  // Pagination & Sorting
  // ========================================

  onPageChange(event: PageEvent): void {
    console.log('📄 Component: Page changed:', event.pageIndex);
    this.pageChanged.emit(event.pageIndex);
  }

  onSortChange(event: any): void {
    console.log('🔄 Component: Sort changed:', event.active, event.direction);
    // TODO: Call service sort method
  }

  // ========================================
  // Row & Action Handlers
  // ========================================

  onRowClick(userId: string): void {
    console.log('👤 Component: Row clicked:', userId);
    this.rowClicked.emit(userId);
  }

  onEdit(userId: string): void {
    console.log('✏️ Component: Edit clicked:', userId);
    this.editClicked.emit(userId);
  }

  onDelete(userId: string): void {
    console.log('🗑️ Component: Delete clicked:', userId);
    this.deleteClicked.emit(userId);
  }

  onRestore(userId: string): void {
    console.log('🔄 Component: Restore clicked:', userId);
    this.restoreClicked.emit(userId);
  }

  onViewDetails(userId: string): void {
    console.log('👁️ Component: View details clicked:', userId);
    this.viewDetailsClicked.emit(userId);
  }

  onChangePassword(userId: string): void {
    console.log('🔐 Component: Change password clicked:', userId);
    this.changePasswordClicked.emit(userId);
  }

  onManageRoles(userId: string): void {
    console.log('👮 Component: Manage roles clicked:', userId);
    this.manageRolesClicked.emit(userId);
  }

  onViewAuditLogs(userId: string): void {
    console.log('📋 Component: View audit logs clicked:', userId);
    this.viewAuditLogsClicked.emit(userId);
  }

  onStatusChange(user: AdminUserViewModel, action: string): void {
    console.log('🔄 Component: Status change:', action);
    this.statusChanged.emit({ userId: user.adminUsersId, newStatus: action });
  }

  onRefresh(): void {
    console.log('🔃 Component: Refresh clicked');
    this.refreshClicked.emit();
  }

  // ========================================
  // Display Helpers
  // ========================================

  getStatusLabel(status: string): string {
    return ADMIN_STATUS_LABELS[status as keyof typeof ADMIN_STATUS_LABELS] || status;
  }

  getStatusColor(status: string): string {
    return ADMIN_STATUS_COLORS[status as keyof typeof ADMIN_STATUS_COLORS] || '#9e9e9e';
  }

  getStatusTextColor(status: string): string {
    const colors = ADMIN_STATUS_COLORS;
    const statusColor = colors[status as keyof typeof colors];
    
    // Return white or dark text based on background brightness
    if (['#f44336', '#b71c1c'].includes(statusColor)) {
      return 'white';
    }
    return 'white';
  }
}
