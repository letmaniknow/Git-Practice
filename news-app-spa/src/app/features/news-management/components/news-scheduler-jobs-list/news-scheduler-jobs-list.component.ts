/**
 * NEWS SCHEDULER JOBS LIST COMPONENT
 * 
 * Smart component displaying paginated list of scheduler jobs.
 * Shows job status, progress, and quick action buttons.
 */

import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Store } from '@ngrx/store';
import { Subject, Observable } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSortModule, Sort } from '@angular/material/sort';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatChipsModule } from '@angular/material/chips';
import { MatMenuModule } from '@angular/material/menu';
import { MatCardModule } from '@angular/material/card';
import { MatTooltipModule } from '@angular/material/tooltip';

import * as NewsSchedulerActions from '../../store/news-scheduler.actions';
import {
  selectSchedulerJobsSummary,
  selectPagination,
  selectSort,
  selectLoading,
  selectCancelling,
} from '../../store/news-scheduler.selectors';
import { NewsSchedulerJobStatus } from '../../models/news-scheduler.model';

interface JobRow {
  jobId: string;
  status: NewsSchedulerJobStatus;
  totalArticles: number;
  publishedCount: number;
  failedCount: number;
  startedAt: Date;
  durationMs?: number;
  successRate: number;
}

/**
 * NewsSchedulerJobsListComponent
 * 
 * Displays table of scheduler jobs with:
 * - Status badges (SUCCESS, FAILED, RUNNING, etc.)
 * - Progress bars showing success percentage
 * - Pagination controls
 * - Sortable columns
 * - Action buttons (view, cancel, retry, delete)
 */
@Component({
  selector: 'app-news-scheduler-jobs-list',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule,
    MatPaginatorModule,
    MatSortModule,
    MatButtonModule,
    MatIconModule,
    MatProgressBarModule,
    MatProgressSpinnerModule,
    MatChipsModule,
    MatMenuModule,
    MatCardModule,
    MatTooltipModule,
  ],
  template: `
    <mat-card class="news-scheduler-jobs-list">
      <mat-card-header>
        <mat-card-title>Publishing Jobs</mat-card-title>
        <mat-card-subtitle>
          {{ (pagination$ | async)?.total || 0 }} total jobs
        </mat-card-subtitle>
      </mat-card-header>

      <div class="table-container">
        <table
          mat-table
          [dataSource]="(jobs$ | async) || []"
          matSort
          (matSortChange)="onSortChange($event)"
        >
          <!-- Job ID Column -->
          <ng-container matColumnDef="jobId">
            <th mat-header-cell *matHeaderCellDef mat-sort-header>Job ID</th>
            <td mat-cell *matCellDef="let element">
              <code class="job-id">{{ element.jobId | slice: 0:8 }}...</code>
            </td>
          </ng-container>

          <!-- Status Column -->
          <ng-container matColumnDef="status">
            <th mat-header-cell *matHeaderCellDef mat-sort-header>Status</th>
            <td mat-cell *matCellDef="let element">
              <mat-chip
                [color]="getStatusColor(element.status)"
                selected
              >
                {{ element.status }}
              </mat-chip>
            </td>
          </ng-container>

          <!-- Progress Column -->
          <ng-container matColumnDef="progress">
            <th mat-header-cell *matHeaderCellDef>Progress</th>
            <td mat-cell *matCellDef="let element">
              <div class="progress-container">
                <mat-progress-bar
                  mode="determinate"
                  [value]="element.successRate"
                  [ngClass]="'rate-' + (element.successRate >= 75 ? 'good' : element.successRate >= 50 ? 'fair' : 'poor')"
                ></mat-progress-bar>
                <span class="progress-text">
                  {{ element.successRate }}% ({{ element.publishedCount }}/{{ element.totalArticles }})
                </span>
              </div>
            </td>
          </ng-container>

          <!-- Articles Column -->
          <ng-container matColumnDef="articles">
            <th mat-header-cell *matHeaderCellDef mat-sort-header>Articles</th>
            <td mat-cell *matCellDef="let element">
              <div class="article-stats">
                <span class="success">✓ {{ element.publishedCount }}</span>
                <span class="error" *ngIf="element.failedCount > 0">✗ {{ element.failedCount }}</span>
              </div>
            </td>
          </ng-container>

          <!-- Started Column -->
          <ng-container matColumnDef="startedAt">
            <th mat-header-cell *matHeaderCellDef mat-sort-header>Started</th>
            <td mat-cell *matCellDef="let element">
              {{ element.startedAt | date: 'short' }}
            </td>
          </ng-container>

          <!-- Duration Column -->
          <ng-container matColumnDef="duration">
            <th mat-header-cell *matHeaderCellDef mat-sort-header>Duration</th>
            <td mat-cell *matCellDef="let element">
              {{ formatDuration(element.durationMs) }}
            </td>
          </ng-container>

          <!-- Actions Column -->
          <ng-container matColumnDef="actions">
            <th mat-header-cell *matHeaderCellDef>Actions</th>
            <td mat-cell *matCellDef="let element">
              <button
                mat-icon-button
                matTooltip="View Details"
                (click)="onViewDetails(element.jobId)"
              >
                <mat-icon>visibility</mat-icon>
              </button>

              <button
                mat-icon-button
                matTooltip="Cancel"
                *ngIf="isRunning(element.status)"
                (click)="onCancel(element.jobId)"
                [disabled]="cancelling$ | async"
              >
                <mat-icon>stop_circle</mat-icon>
              </button>

              <button
                mat-icon-button
                [matMenuTriggerFor]="menu"
                matTooltip="More"
              >
                <mat-icon>more_vert</mat-icon>
              </button>

              <mat-menu #menu="matMenu">
                <button
                  mat-menu-item
                  *ngIf="element.failedCount > 0"
                  (click)="onRetry(element.jobId)"
                >
                  <mat-icon>replay</mat-icon>
                  <span>Retry Failed</span>
                </button>
                <button
                  mat-menu-item
                  *ngIf="isTerminal(element.status)"
                  (click)="onDelete(element.jobId)"
                >
                  <mat-icon>delete</mat-icon>
                  <span>Delete</span>
                </button>
              </mat-menu>
            </td>
          </ng-container>

          <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
          <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>
        </table>
      </div>

      <!-- Pagination -->
      <mat-paginator
        [length]="(pagination$ | async)?.total || 0"
        [pageSize]="(pagination$ | async)?.size || 20"
        [pageSizeOptions]="[10, 20, 50]"
        (page)="onPageChange($event)"
      ></mat-paginator>

      <!-- Loading State -->
      <div *ngIf="loading$ | async" class="loading-overlay">
        <mat-spinner diameter="50"></mat-spinner>
      </div>
    </mat-card>
  `,
  styles: [`
    .news-scheduler-jobs-list {
      margin: 1rem 0;

      .table-container {
        overflow-x: auto;
        position: relative;
      }

      table {
        width: 100%;
        min-width: 900px;
      }

      .job-id {
        font-family: monospace;
        font-size: 0.875rem;
        background-color: var(--gray-bg, #f5f5f5);
        padding: 0.25rem 0.5rem;
        border-radius: 4px;
      }

      .progress-container {
        display: flex;
        align-items: center;
        gap: 0.5rem;

        mat-progress-bar {
          flex: 1;
          min-width: 150px;
        }

        &.rate-good {
          --progress-color: #4caf50;
        }

        &.rate-fair {
          --progress-color: #ff9800;
        }

        &.rate-poor {
          --progress-color: #f44336;
        }
      }

      .progress-text {
        font-size: 0.875rem;
        color: var(--text-secondary, #666);
        min-width: 80px;
      }

      .article-stats {
        display: flex;
        gap: 1rem;

        .success {
          color: #4caf50;
          font-weight: 500;
        }

        .error {
          color: #f44336;
          font-weight: 500;
        }
      }

      mat-chip {
        margin: 0;
      }

      button[mat-icon-button] {
        margin: 0 0.25rem;
      }

      .loading-overlay {
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background-color: rgba(255, 255, 255, 0.8);
        display: flex;
        align-items: center;
        justify-content: center;
        z-index: 10;
      }
    }

    @media (max-width: 768px) {
      .news-scheduler-jobs-list {
        table {
          min-width: 600px;
        }
      }
    }
  `],
})
export class NewsSchedulerJobsListComponent implements OnInit, OnDestroy {
  displayedColumns: string[] = [
    'jobId',
    'status',
    'progress',
    'articles',
    'startedAt',
    'duration',
    'actions',
  ];

  readonly jobs$!: Observable<any[]>;
  readonly pagination$!: Observable<any>;
  readonly sort$!: Observable<any>;
  readonly loading$!: Observable<boolean>;
  readonly cancelling$!: Observable<boolean>;

  private destroy$ = new Subject<void>();

  constructor(private store: Store) {
    this.jobs$ = this.store.select(selectSchedulerJobsSummary);
    this.pagination$ = this.store.select(selectPagination);
    this.sort$ = this.store.select(selectSort);
    this.loading$ = this.store.select(selectLoading);
    this.cancelling$ = this.store.select(selectCancelling);
  }

  ngOnInit(): void {
    // Load jobs on component init
    this.store.dispatch(NewsSchedulerActions.loadSchedulerJobs({}));
  }

  /**
   * Handle sort change
   */
  onSortChange(sort: Sort): void {
    this.store.dispatch(
      NewsSchedulerActions.setSortScheduler({
        sortBy: sort.active,
        direction: (sort.direction || 'asc') as 'asc' | 'desc',
      })
    );
  }

  /**
   * Handle pagination change
   */
  onPageChange(event: PageEvent): void {
    this.store.dispatch(
      NewsSchedulerActions.setSchedulerPage({
        page: event.pageIndex,
      })
    );
    if (event.pageSize) {
      this.store.dispatch(
        NewsSchedulerActions.setSchedulerPageSize({
          size: event.pageSize,
        })
      );
    }
  }

  /**
   * View job details
   */
  onViewDetails(jobId: string): void {
    this.store.dispatch(
      NewsSchedulerActions.selectSchedulerJob({ jobId })
    );
  }

  /**
   * Cancel running job
   */
  onCancel(jobId: string): void {
    if (confirm('Cancel this job?')) {
      this.store.dispatch(
        NewsSchedulerActions.cancelSchedulerJob({ jobId })
      );
    }
  }

  /**
   * Retry failed articles
   */
  onRetry(jobId: string): void {
    this.store.dispatch(
      NewsSchedulerActions.retryFailedArticles({
        request: { jobId },
      })
    );
  }

  /**
   * Delete job
   */
  onDelete(jobId: string): void {
    if (confirm('Delete this job? This cannot be undone.')) {
      this.store.dispatch(
        NewsSchedulerActions.deleteSchedulerJob({ jobId })
      );
    }
  }

  /**
   * Format duration in milliseconds to readable string
   */
  formatDuration(ms?: number): string {
    if (!ms) return '—';
    const seconds = Math.floor(ms / 1000);
    if (seconds < 60) return `${seconds}s`;
    const minutes = Math.floor(seconds / 60);
    if (minutes < 60) return `${minutes}m ${seconds % 60}s`;
    const hours = Math.floor(minutes / 60);
    return `${hours}h ${minutes % 60}m`;
  }

  /**
   * Check if job is running
   */
  isRunning(status: NewsSchedulerJobStatus): boolean {
    return status === NewsSchedulerJobStatus.RUNNING;
  }

  /**
   * Check if job is in terminal state
   */
  isTerminal(status: NewsSchedulerJobStatus): boolean {
    return [
      NewsSchedulerJobStatus.SUCCESS,
      NewsSchedulerJobStatus.FAILED,
      NewsSchedulerJobStatus.CANCELLED,
    ].includes(status);
  }

  /**
   * Get color for status badge
   */
  getStatusColor(status: NewsSchedulerJobStatus): string {
    const colors: Record<NewsSchedulerJobStatus, string> = {
      [NewsSchedulerJobStatus.RUNNING]: 'primary',
      [NewsSchedulerJobStatus.SUCCESS]: 'accent',
      [NewsSchedulerJobStatus.FAILED]: 'warn',
      [NewsSchedulerJobStatus.PARTIAL_SUCCESS]: 'accent',
      [NewsSchedulerJobStatus.CANCELLED]: 'warn',
    };
    return colors[status] || 'primary';
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
