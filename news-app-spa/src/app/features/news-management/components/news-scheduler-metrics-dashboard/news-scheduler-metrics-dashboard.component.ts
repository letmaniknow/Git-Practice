/**
 * NEWS SCHEDULER METRICS DASHBOARD COMPONENT
 * 
 * Smart component displaying scheduler performance metrics.
 * Shows aggregated statistics, success rates, and trends.
 */

import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Store } from '@ngrx/store';
import { Subject, Observable } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatChipsModule } from '@angular/material/chips';
import { MatDividerModule } from '@angular/material/divider';
import { MatListModule } from '@angular/material/list';

import * as NewsSchedulerActions from '../../store/news-scheduler.actions';
import {
  selectMetrics,
  selectSuccessRate,
  selectArticleSuccessRate,
  selectAverageJobDuration,
} from '../../store/news-scheduler.selectors';
import { NewsSchedulerMetrics } from '../../models/news-scheduler.model';

interface MetricCard {
  title: string;
  value: string | number;
  unit?: string;
  icon: string;
  color: 'primary' | 'accent' | 'warn';
  trend?: number;
}

/**
 * NewsSchedulerMetricsDashboardComponent
 * 
 * Displays performance metrics for scheduler including:
 * - Total jobs, successful/failed counts
 * - Article processing statistics
 * - Success rates (jobs and articles)
 * - Average job duration
 * - Error breakdown by error code
 * - Time period selector (24h, 7d, 30d)
 */
@Component({
  selector: 'app-news-scheduler-metrics-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatProgressBarModule,
    MatSelectModule,
    MatFormFieldModule,
    MatChipsModule,
    MatDividerModule,
    MatListModule,
  ],
  template: `
    <div class="news-scheduler-metrics-dashboard">
      <!-- Header with time period selector -->
      <div class="dashboard-header">
        <h2>Scheduler Performance Metrics</h2>
        <mat-form-field appearance="outline">
          <mat-label>Time Period</mat-label>
          <mat-select (selectionChange)="onTimePeriodChange($event.value)">
            <mat-option value="24h">Last 24 Hours</mat-option>
            <mat-option value="7d">Last 7 Days</mat-option>
            <mat-option value="30d">Last 30 Days</mat-option>
          </mat-select>
        </mat-form-field>
        <button mat-icon-button (click)="onRefresh()" matTooltip="Refresh">
          <mat-icon>refresh</mat-icon>
        </button>
      </div>

      <!-- Metrics Grid -->
      <div class="metrics-grid">
        <!-- Total Jobs -->
        <mat-card class="metric-card">
          <div class="metric-icon accent">
            <mat-icon>scheduler</mat-icon>
          </div>
          <div class="metric-content">
            <div class="metric-title">Total Jobs</div>
            <div class="metric-value">{{ (metrics$ | async)?.totalJobs || 0 }}</div>
          </div>
        </mat-card>

        <!-- Successful Jobs -->
        <mat-card class="metric-card success">
          <div class="metric-icon accent">
            <mat-icon>check_circle</mat-icon>
          </div>
          <div class="metric-content">
            <div class="metric-title">Successful</div>
            <div class="metric-value success">{{ (metrics$ | async)?.successfulJobs || 0 }}</div>
          </div>
        </mat-card>

        <!-- Failed Jobs -->
        <mat-card class="metric-card error">
          <div class="metric-icon warn">
            <mat-icon>error_circle</mat-icon>
          </div>
          <div class="metric-content">
            <div class="metric-title">Failed</div>
            <div class="metric-value error">{{ (metrics$ | async)?.failedJobs || 0 }}</div>
          </div>
        </mat-card>

        <!-- Job Success Rate -->
        <mat-card class="metric-card">
          <div class="metric-icon primary">
            <mat-icon>trending_up</mat-icon>
          </div>
          <div class="metric-content">
            <div class="metric-title">Job Success Rate</div>
            <div class="metric-value">{{ (successRate$ | async) || 0 }}%</div>
          </div>
        </mat-card>
      </div>

      <!-- Article Statistics -->
      <mat-card class="stats-card">
        <mat-card-header>
          <mat-card-title>Article Processing Statistics</mat-card-title>
        </mat-card-header>

        <mat-card-content>
          <div class="stats-row">
            <div class="stat-item">
              <span class="stat-label">Total Articles</span>
              <span class="stat-value">{{ (metrics$ | async)?.totalArticlesProcessed || 0 }}</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">Successfully Published</span>
              <span class="stat-value success">{{ (metrics$ | async)?.successfullyPublished || 0 }}</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">Article Success Rate</span>
              <span class="stat-value">{{ (articleSuccessRate$ | async) || 0 }}%</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">Average Job Duration</span>
              <span class="stat-value">{{ formatDuration((averageDuration$ | async) || 0) }}</span>
            </div>
          </div>

          <mat-divider class="my-2"></mat-divider>

          <!-- Article Success Bar -->
          <div class="progress-section">
            <h4>Article Publication Rate</h4>
            <div class="progress-bars">
              <div class="progress-item">
                <span class="label">Published</span>
                <div class="progress-container">
                  <mat-progress-bar
                    mode="determinate"
                    [value]="calculatePublishedPercentage(metrics$ | async)"
                    class="rate-good"
                  ></mat-progress-bar>
                </div>
              </div>
              <div class="progress-item">
                <span class="label">Failed</span>
                <div class="progress-container">
                  <mat-progress-bar
                    mode="determinate"
                    [value]="calculateFailedPercentage(metrics$ | async)"
                    class="rate-poor"
                  ></mat-progress-bar>
                </div>
              </div>
            </div>
          </div>
        </mat-card-content>
      </mat-card>

      <!-- Error Distribution -->
      <mat-card class="stats-card">
        <mat-card-header>
          <mat-card-title>Error Distribution</mat-card-title>
        </mat-card-header>

        <mat-card-content>
          <div *ngIf="(metrics$ | async) as metrics">
            <ng-container *ngIf="metrics.errorsByCode && (metrics.errorsByCode | keyvalue).length > 0; else noErrors">
              <mat-list>
                <mat-list-item
                  *ngFor="let error of metrics.errorsByCode | keyvalue"
                >
                  <mat-icon matListItemIcon color="warn">error</mat-icon>
                  <div matListItemTitle>{{ error.key }}</div>
                  <div matListItemLine>
                    {{ error.value }} occurrences
                  </div>
                </mat-list-item>
              </mat-list>
            </ng-container>

            <ng-template #noErrors>
              <div class="empty-state">
                <mat-icon>check_circle</mat-icon>
                <p>No errors in this period</p>
              </div>
            </ng-template>
          </div>
        </mat-card-content>
      </mat-card>

      <!-- Summary Stats -->
      <mat-card class="summary-card">
        <mat-card-header>
          <mat-card-title>Summary</mat-card-title>
        </mat-card-header>

        <mat-card-content>
          <div class="summary-grid">
            <div class="summary-item">
              <span class="label">Average Job Duration</span>
              <span class="value">{{ formatDuration((averageDuration$ | async) || 0) }}</span>
            </div>

            <div class="summary-item">
              <span class="label">Time Period</span>
              <mat-chip size="small">
                {{ (metrics$ | async)?.periodLabel || '24h' }}
              </mat-chip>
            </div>

            <div class="summary-item">
              <span class="label">Data Collection Window</span>
              <span class="value">{{ (metrics$ | async)?.timePeriod || '24h' }}</span>
            </div>
          </div>
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .news-scheduler-metrics-dashboard {
      padding: 1rem 0;

      .dashboard-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 2rem;
        gap: 1rem;

        h2 {
          margin: 0;
          flex: 1;
        }

        mat-form-field {
          width: 150px;
        }
      }

      .metrics-grid {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
        gap: 1rem;
        margin-bottom: 2rem;
      }

      .metric-card {
        display: flex;
        align-items: center;
        gap: 1rem;
        padding: 1.5rem;
        transition: transform 0.2s, box-shadow 0.2s;

        &:hover {
          transform: translateY(-2px);
          box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
        }

        .metric-icon {
          width: 50px;
          height: 50px;
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          color: white;

          &.primary {
            background-color: #3f51b5;
          }

          &.accent {
            background-color: #ff4081;
          }

          &.warn {
            background-color: #ff9800;
          }

          mat-icon {
            font-size: 28px;
            width: 28px;
            height: 28px;
          }
        }

        .metric-content {
          flex: 1;

          .metric-title {
            font-size: 0.875rem;
            color: var(--text-secondary, #666);
            margin-bottom: 0.25rem;
          }

          .metric-value {
            font-size: 2rem;
            font-weight: 600;

            &.success {
              color: #4caf50;
            }

            &.error {
              color: #f44336;
            }
          }
        }
      }

      .stats-card {
        margin-bottom: 1.5rem;

        mat-card-content {
          padding: 1.5rem;
        }
      }

      .stats-row {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
        gap: 1.5rem;
        margin-bottom: 1rem;

        .stat-item {
          display: flex;
          flex-direction: column;
          gap: 0.5rem;

          .stat-label {
            font-size: 0.875rem;
            color: var(--text-secondary, #666);
          }

          .stat-value {
            font-size: 1.5rem;
            font-weight: 600;

            &.success {
              color: #4caf50;
            }
          }
        }
      }

      .progress-section {
        margin-top: 1rem;

        h4 {
          margin-top: 0;
          margin-bottom: 1rem;
        }

        .progress-bars {
          display: flex;
          flex-direction: column;
          gap: 1rem;

          .progress-item {
            display: flex;
            align-items: center;
            gap: 1rem;

            .label {
              min-width: 80px;
              font-weight: 500;
            }

            .progress-container {
              flex: 1;
              min-width: 200px;

              mat-progress-bar {
                &.rate-good {
                  --mdc-theme-primary: #4caf50;
                }

                &.rate-poor {
                  --mdc-theme-primary: #f44336;
                }
              }
            }
          }
        }
      }

      .empty-state {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        padding: 2rem;
        color: var(--text-secondary, #999);

        mat-icon {
          font-size: 48px;
          width: 48px;
          height: 48px;
          color: #4caf50;
          margin-bottom: 0.5rem;
        }

        p {
          margin: 0;
        }
      }

      .summary-card {
        background-color: var(--surface-variant, #f5f5f5);
      }

      .summary-grid {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
        gap: 1rem;

        .summary-item {
          display: flex;
          flex-direction: column;
          gap: 0.5rem;

          .label {
            font-size: 0.875rem;
            color: var(--text-secondary, #666);
            font-weight: 500;
          }

          .value {
            font-size: 1.125rem;
            font-weight: 500;
          }
        }
      }

      .my-2 {
        margin: 1rem 0;
      }
    }

    @media (max-width: 768px) {
      .news-scheduler-metrics-dashboard {
        .dashboard-header {
          flex-direction: column;
          align-items: flex-start;

          mat-form-field {
            width: 100%;
          }
        }

        .metrics-grid {
          grid-template-columns: 1fr;
        }

        .stats-row {
          grid-template-columns: repeat(2, 1fr);
        }
      }
    }
  `],
})
export class NewsSchedulerMetricsDashboardComponent implements OnInit, OnDestroy {
  readonly metrics$!: Observable<NewsSchedulerMetrics | null>;
  readonly successRate$!: Observable<number>;
  readonly articleSuccessRate$!: Observable<number>;
  readonly averageDuration$!: Observable<number>;

  private destroy$ = new Subject<void>();

  constructor(private store: Store) {
    this.metrics$ = this.store.select(selectMetrics);
    this.successRate$ = this.store.select(selectSuccessRate);
    this.articleSuccessRate$ = this.store.select(selectArticleSuccessRate);
    this.averageDuration$ = this.store.select(selectAverageJobDuration);
  }

  ngOnInit(): void {
    // Load metrics on component init
    this.store.dispatch(
      NewsSchedulerActions.loadSchedulerMetrics({ timePeriod: '24h' })
    );
  }

  /**
   * Handle time period change
   */
  onTimePeriodChange(period: '24h' | '7d' | '30d'): void {
    this.store.dispatch(
      NewsSchedulerActions.loadSchedulerMetrics({ timePeriod: period })
    );
  }

  /**
   * Manually refresh metrics
   */
  onRefresh(): void {
    this.store.dispatch(
      NewsSchedulerActions.loadSchedulerMetrics({})
    );
  }

  /**
   * Calculate published articles percentage
   */
  calculatePublishedPercentage(metrics: NewsSchedulerMetrics | null | undefined): number {
    if (!metrics || metrics.totalArticlesProcessed === 0) return 0;
    return Math.round((metrics.successfullyPublished / metrics.totalArticlesProcessed) * 100);
  }

  /**
   * Calculate failed articles percentage
   */
  calculateFailedPercentage(metrics: NewsSchedulerMetrics | null | undefined): number {
    if (!metrics || metrics.totalArticlesProcessed === 0) return 0;
    const failed = metrics.totalArticlesProcessed - metrics.successfullyPublished;
    return Math.round((failed / metrics.totalArticlesProcessed) * 100);
  }

  /**
   * Format duration in milliseconds
   */
  formatDuration(ms: number): string {
    if (ms === 0) return '—';
    const seconds = Math.floor(ms / 1000);
    if (seconds < 60) return `${seconds}s`;
    const minutes = Math.floor(seconds / 60);
    if (minutes < 60) return `${minutes}m ${seconds % 60}s`;
    const hours = Math.floor(minutes / 60);
    return `${hours}h ${minutes % 60}m`;
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
