import { Component, OnInit, OnDestroy, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { Subject, Observable, BehaviorSubject, of } from 'rxjs';
import { takeUntil, map, startWith, tap, catchError, switchMap, shareReplay } from 'rxjs/operators';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';

// Core
import { AppRole } from '../../../../core/models';
import { AuthService } from '../../../../core/services/auth.service';

// Shared barrel exports
import { DashboardStatsCardComponent, DashboardQuickActionsComponent } from '../../shared/components';
import { DashboardActionHandlerService } from '../../shared/services';
import { QuickAction } from '../../shared/models';

// News feature imports
import { DashboardNewsActivityComponent } from '../components/dashboard-news-activity/dashboard-news-activity.component';
import { DashboardStats, StatCard, NewsAuditLogDto, PageResponse } from '../models/dashboard-news.model';
import { DashboardNewsService } from '../services/dashboard-news.service';
import { DASHBOARD_QUICK_ACTIONS } from '../constants/dashboard-quick-actions.constant';

@Component({
  selector: 'app-dashboard-news-page',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    CommonModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatIconModule,
    MatTooltipModule,
    DashboardStatsCardComponent,
    DashboardQuickActionsComponent,
    DashboardNewsActivityComponent,
  ],
  template: `
    <div class="dashboard-container">
      <!-- Header -->
      <header class="dashboard-header">
        <h1>Admin Dashboard</h1>
        <div class="header-actions">
          <button class="create-news-btn" (click)="onCreateNews()" matTooltip="Create a new article">
            <mat-icon>add_circle</mat-icon>
            <span>Create News</span>
          </button>
          <button class="refresh-btn" (click)="onRefresh()" matTooltip="Refresh dashboard">
            <mat-icon>refresh</mat-icon>
            <span>Refresh</span>
          </button>
        </div>
      </header>

      <!-- Loading State -->
      <div *ngIf="loading$ | async" class="loading-container">
        <mat-spinner></mat-spinner>
        <p>Loading dashboard data...</p>
      </div>

      <!-- Error State -->
      <div *ngIf="error$ | async as error" class="error-container">
        <p>⚠️ Error loading dashboard: {{ error }}</p>
      </div>

      <!-- Dashboard Content -->
      <div *ngIf="!(loading$ | async) && !(error$ | async)" class="dashboard-content">
        
        <!-- Quick Actions (Filtered by User Role) -->
        <app-dashboard-quick-actions 
          [actions]="(actions$ | async) || []"
          (actionTriggered)="onActionTriggered($event)">
        </app-dashboard-quick-actions>

        <!-- Stats Cards Grid -->
        <div class="stats-grid">
          <app-dashboard-stats-card 
            *ngFor="let stat of (statCards$ | async)"
            [stat]="stat">
          </app-dashboard-stats-card>
        </div>

        <!-- Recent Activity Section -->
        <div class="activity-section">
          <app-dashboard-news-activity 
            [activities]="(recentActivity$ | async) ?? []">
          </app-dashboard-news-activity>
        </div>

        <!-- Health Section (Future) -->
        <div class="health-section">
          <h3>📊 System Health</h3>
          <p class="placeholder">System health monitoring coming soon...</p>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .dashboard-container {
      padding: 2rem;
      max-width: 1400px;
      margin: 0 auto;
      min-height: 100vh;
      background: var(--background-color, #f5f5f5);
    }

    .dashboard-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 2rem;

      h1 {
        margin: 0;
        font-size: 2rem;
        font-weight: 700;
        color: var(--text-primary, #222);
      }
    }

    .header-actions {
      display: flex;
      gap: 1rem;
      align-items: center;
    }

    .create-news-btn {
      padding: 0.75rem 1.5rem;
      background: var(--success-color, #4caf50);
      color: white;
      border: none;
      border-radius: 6px;
      font-size: 0.95rem;
      font-weight: 500;
      cursor: pointer;
      display: flex;
      align-items: center;
      gap: 0.5rem;
      transition: all 0.2s ease;

      &:hover {
        background: var(--success-dark, #45a049);
        transform: translateY(-1px);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
      }

      &:active {
        transform: translateY(0);
      }

      mat-icon {
        font-size: 1.25rem;
      }
    }

    .refresh-btn {
      padding: 0.75rem 1.5rem;
      background: var(--primary-color, #1976d2);
      color: white;
      border: none;
      border-radius: 6px;
      font-size: 0.95rem;
      font-weight: 500;
      cursor: pointer;
      display: flex;
      align-items: center;
      gap: 0.5rem;
      transition: all 0.2s ease;

      &:hover {
        background: var(--primary-dark, #1565c0);
        transform: translateY(-1px);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
      }

      &:active {
        transform: translateY(0);
      }

      mat-icon {
        font-size: 1.25rem;
      }
    }

    .loading-container {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      min-height: 400px;
      gap: 1rem;

      p {
        color: var(--text-secondary, #666);
        font-size: 1rem;
      }
    }

    .error-container {
      padding: 2rem;
      background: #ffebee;
      border: 1px solid #ef5350;
      border-radius: 8px;
      color: #c62828;
      text-align: center;
      font-weight: 500;
    }

    .dashboard-content {
      display: flex;
      flex-direction: column;
      gap: 2rem;
    }

    .stats-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
      gap: 1.5rem;
    }

    .activity-section {
      margin-top: 1rem;
    }

    .health-section {
      padding: 1.5rem;
      background: var(--surface-color, #fff);
      border-radius: 8px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

      h3 {
        margin: 0 0 1rem;
        font-size: 1.125rem;
        font-weight: 600;
        color: var(--text-primary, #222);
      }

      .placeholder {
        margin: 0;
        color: var(--text-secondary, #999);
        font-style: italic;
      }
    }

    @media (max-width: 768px) {
      .dashboard-container {
        padding: 1rem;
      }

      .dashboard-header {
        flex-direction: column;
        gap: 1rem;
        text-align: center;

        h1 {
          font-size: 1.5rem;
        }
      }

      .refresh-btn {
        width: 100%;
      }

      .stats-grid {
        grid-template-columns: 1fr;
        gap: 1rem;
      }
    }
  `]
})
export class DashboardNewsPageComponent implements OnInit, OnDestroy {
  // ✅ Quick actions - will be filtered by user role
  actions$!: Observable<QuickAction[]>;

  // ✅ Memory leak prevention
  private readonly destroy$ = new Subject<void>();
  private readonly refreshSubject$ = new Subject<void>();
  private readonly pageSubject$ = new BehaviorSubject<number>(0);
  private readonly pageSizeSubject$ = new BehaviorSubject<number>(10);

  // ✅ Observable streams for template binding (initialized in constructor)
  loading$!: Observable<boolean>;
  error$!: Observable<string | null>;
  stats$!: Observable<DashboardStats | null>;
  recentActivity$!: Observable<NewsAuditLogDto[]>;
  pageInfo$!: Observable<PageResponse | null>;
  totalActivities$!: Observable<number>;

  // ✅ Stat cards (derived from stats)
  statCards$!: Observable<StatCard[]>;

  constructor(
    private dashboardService: DashboardNewsService,
    private actionHandler: DashboardActionHandlerService,
    private router: Router,
    private snackBar: MatSnackBar,
    private authService: AuthService
  ) {
    // Initialize observables after service is available
    this.loading$ = this.dashboardService.loading$;
    this.error$ = this.dashboardService.error$;
    this.stats$ = this.dashboardService.stats$;
    this.recentActivity$ = this.dashboardService.activities$;
    this.pageInfo$ = this.dashboardService.pageInfo$;
    this.totalActivities$ = this.dashboardService.totalActivities$;

    // Get actions filtered by user role from AuthService
    // Converts string roles from JWT to AppRole enum and applies RBAC filtering
    this.actions$ = this.authService.currentUser$.pipe(
      switchMap(user => {
        if (!user || !user.roles || user.roles.length === 0) {
          // Fallback to EDITOR if no user role available
          return of(this.actionHandler.getFilteredActionsByRole(DASHBOARD_QUICK_ACTIONS, AppRole.EDITOR));
        }
        
        // Convert string roles to AppRole enum
        // Priority: SUPER_ADMIN > ADMIN > REVIEWER > EDITOR
        const userRole = this.convertStringRoleToAppRole(user.roles);
        return of(this.actionHandler.getFilteredActionsByRole(DASHBOARD_QUICK_ACTIONS, userRole));
      }),
      startWith([]),  // Start with empty array while loading user data
      shareReplay(1),
      takeUntil(this.destroy$)
    );

    // Transform stats to stat cards display format
    this.statCards$ = this.dashboardService.stats$.pipe(
      map(stats => this.transformStatsToCards(stats)),
      startWith([])
    );
  }

  /**
   * Convert string roles from JWT to AppRole enum
   * Uses priority hierarchy: SUPER_ADMIN > ADMIN > REVIEWER > EDITOR
   * 
   * @param roles String array of roles from JWT token
   * @returns Primary AppRole for RBAC filtering
   */
  private convertStringRoleToAppRole(roles: string[]): AppRole {
    // Check highest privilege first
    if (roles.includes('SUPER_ADMIN')) return AppRole.SUPER_ADMIN;
    if (roles.includes('ADMIN')) return AppRole.ADMIN;
    if (roles.includes('REVIEWER')) return AppRole.REVIEWER;
    if (roles.includes('EDITOR')) return AppRole.EDITOR;
    
    // Fallback to EDITOR if role not recognized
    return AppRole.EDITOR;
  }

  ngOnInit(): void {
    console.log('🚀 DashboardNewsPageComponent.ngOnInit()');

    // Load stats on refresh trigger
    this.refreshSubject$.pipe(
      tap(() => {
        console.log('🔄 Refresh triggered');
        this.dashboardService.clearError();
      }),
      switchMap(() => this.dashboardService.getStats()),
      catchError(error => {
        console.error('❌ Error loading stats:', error);
        this.snackBar.open('❌ Error loading dashboard', 'Close', { duration: 3000 });
        return of(null);
      }),
      takeUntil(this.destroy$)
    ).subscribe({
      next: () => console.log('✅ Stats loaded successfully'),
      error: (err) => console.error('❌ Stats subscription error:', err)
    });

    // Load recent activity on refresh trigger OR page change
    this.refreshSubject$.pipe(
      switchMap(() => {
        const page = this.pageSubject$.value;
        const pageSize = this.pageSizeSubject$.value;
        console.log('📡 Loading activities - page:', page, 'size:', pageSize);
        return this.dashboardService.getRecentActivity(page, pageSize);
      }),
      catchError(error => {
        console.error('❌ Error loading activities:', error);
        return of([]);
      }),
      tap(() => console.log('✅ Activities loaded successfully')),
      takeUntil(this.destroy$)
    ).subscribe();

    // Trigger initial load
    console.log('🎯 Triggering initial dashboard load');
    this.refreshSubject$.next();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  /**
   * Transform DashboardStats to StatCard[] for display
   * Following News feature transformation pattern
   */
  private transformStatsToCards(stats: DashboardStats | null): StatCard[] {
    if (!stats) {
      return [];
    }

    return [
      {
        title: 'Total Articles',
        value: stats.totalArticles,
        icon: 'article',
        change: 5,
        unit: 'articles'
      },
      {
        title: 'Published This Month',
        value: stats.publishedThisMonth,
        icon: 'publish',
        change: 12,
        unit: 'articles'
      },
      {
        title: 'Drafts',
        value: stats.draftCount,
        icon: 'description',
        change: -2,
        unit: 'articles'
      },
      {
        title: 'Scheduled',
        value: stats.scheduledCount,
        icon: 'schedule',
        change: 3,
        unit: 'articles'
      },
      {
        title: 'Archived',
        value: stats.archivedCount,
        icon: 'archive',
        change: 1,
        unit: 'articles'
      },
      {
        title: 'System Health',
        value: stats.systemHealth === 'healthy' ? '✓ Healthy' : stats.systemHealth === 'warning' ? '⚠ Warning' : '✗ Critical',
        icon: 'health_and_safety',
        unit: stats.systemHealth
      }
    ];
  }

  /**
   * Manual refresh - triggers fresh data load
   */
  onRefresh(): void {
    console.log('🔄 Manual refresh triggered');
    this.snackBar.open('✅ Dashboard refreshing...', 'Close', { duration: 1000 });
    this.pageSubject$.next(0);  // Reset to first page
    this.refreshSubject$.next();
  }

  /**
   * Create new news article
   */
  onCreateNews(): void {
    console.log('📝 Create News button clicked');
    this.onActionTriggered('create-news');
  }

  /**
   * Load specific page of activities
   */
  onPageChange(page: number): void {
    console.log('📄 Page changed to:', page);
    this.pageSubject$.next(page);
    this.refreshSubject$.next();
  }

  /**
   * Handle quick actions
   */
  onActionTriggered(actionId: string): void {
    const messages: Record<string, string> = {
      'create-news': '📝 Opening create news dialog...',
      'dashboard-news': '📰 You are already on the News Dashboard',
      'manage-users': '👥 Navigating to user management...',
      'manage-categories': '📂 Navigating to category management...',
      'recycle-bin': '🗑️ Navigating to recycle bin...',
      'settings': '⚙️ Navigating to settings...',
      'refresh': '🔄 Refreshing dashboard...',
    };

    console.log(`📌 Action triggered: ${actionId}`);

    switch (actionId) {
      case 'create-news':
        console.log('🚀 Navigating to create news page...');
        this.router.navigate(['/news/create']);
        break;

      case 'dashboard-news':
        // Already on news dashboard - just show message
        this.snackBar.open(messages[actionId], 'Close', { duration: 1500 });
        break;

      case 'refresh':
        this.onRefresh();
        break;

      case 'manage-users':
      case 'manage-categories':
      case 'recycle-bin':
      case 'settings':
        // Use DashboardActionHandlerService for centralized routing
        this.actionHandler.handleAction(actionId).then((success: boolean) => {
          if (success) {
            this.snackBar.open(messages[actionId], 'Close', { duration: 1500 });
          } else {
            this.snackBar.open(`⚠️ Navigation to ${actionId} not yet implemented`, 'Close', { duration: 2000 });
          }
        });
        break;

      default:
        console.warn(`⚠️ Unknown action: ${actionId}`);
    }
  }
}
