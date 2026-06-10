/**
 * NEWS SCHEDULER JOB DETAILS COMPONENT
 * 
 * Smart component displaying detailed information about a scheduler job.
 * Shows execution logs, attempts, error details, and retry history.
 */

import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Store } from '@ngrx/store';
import { Subject, Observable } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

import { MatCardModule } from '@angular/material/card';
import { MatTabsModule } from '@angular/material/tabs';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatDividerModule } from '@angular/material/divider';
import { MatListModule } from '@angular/material/list';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatExpansionModule } from '@angular/material/expansion';

import * as NewsSchedulerActions from '../../store/news-scheduler.actions';
import {
  selectSelectedJobSummary,
  selectSelectedJobDetails,
  selectJobSuccessBadge,
} from '../../store/news-scheduler.selectors';
import {
  NewsSchedulerJob,
  NewsSchedulerJobStatus,
} from '../../models/news-scheduler.model';

/**
 * NewsSchedulerJobDetailsComponent
 * 
 * Displays detailed information about a scheduler job including:
 * - Job metadata (ID, status, priority, etc.)
 * - Execution summary (success rate, duration)
 * - Execution logs
 * - Retry attempts and history
 * - Error details with error codes
 */
@Component({
  selector: 'app-news-scheduler-job-details',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatTabsModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatDividerModule,
    MatListModule,
    MatProgressBarModule,
    MatTooltipModule,
    MatExpansionModule,
  ],
  template: `
    <div class="news-scheduler-job-details">
      <ng-container *ngIf="jobDetails$ | async as job; else noJob">
        <mat-card class="job-header">
          <mat-card-header>
            <mat-card-title>Job Details: {{ job.jobId | slice: 0:8 }}...</mat-card-title>
            <mat-card-subtitle>
              {{ job.status }} • Started {{ job.startedAt | date: 'short' }}
            </mat-card-subtitle>
          </mat-card-header>

          <mat-card-content>
            <!-- Summary Row -->
            <div class="summary-grid">
              <div class="summary-item">
                <span class="label">Status</span>
                <mat-chip [color]="getStatusColor(job.status)" selected>
                  {{ job.status }}
                </mat-chip>
              </div>

              <div class="summary-item">
                <span class="label">Priority</span>
                <span class="value">{{ job.priority }}</span>
              </div>

              <div class="summary-item">
                <span class="label">Duration</span>
                <span class="value">{{ formatDuration(job.durationMs) }}</span>
              </div>

              <div class="summary-item">
                <span class="label">Retry Attempt</span>
                <span class="value">{{ job.retryAttempt }}/{{ job.maxRetries }}</span>
              </div>
            </div>

            <mat-divider></mat-divider>

            <!-- Success Rate Progress -->
            <div class="success-section">
              <div class="success-header">
                <h3>Publishing Success Rate</h3>
                <span class="rate-label">
                  {{ (jobSuccessBadge$ | async)?.percentage || 0 }}%
                </span>
              </div>
              <mat-progress-bar
                mode="determinate"
                [value]="(jobSuccessBadge$ | async)?.percentage || 0"
                [ngClass]="'rate-' + ((jobSuccessBadge$ | async)?.color || 'error')"
              ></mat-progress-bar>
              <div class="article-breakdown">
                <span class="published">✓ Published: {{ job.publishedCount }}</span>
                <span class="failed">✗ Failed: {{ job.failedCount }}</span>
                <span class="total">/ {{ job.totalArticles }} total</span>
              </div>
            </div>

            <mat-divider></mat-divider>

            <!-- Tabs for detailed info -->
            <mat-tab-group>
              <!-- Metadata Tab -->
              <mat-tab label="Metadata">
                <div class="tab-content">
                  <mat-list>
                    <mat-list-item>
                      <strong matListItemTitle>Job ID</strong>
                      <code matListItemLine>{{ job.jobId }}</code>
                    </mat-list-item>

                    <mat-list-item>
                      <strong matListItemTitle>Correlation ID</strong>
                      <code matListItemLine>{{ job.correlationId }}</code>
                    </mat-list-item>

                    <mat-list-item>
                      <strong matListItemTitle>Idempotency Key</strong>
                      <code matListItemLine>{{ job.idempotencyKey || '—' }}</code>
                    </mat-list-item>

                    <mat-list-item>
                      <strong matListItemTitle>Source</strong>
                      <span matListItemLine>{{ job.source }}</span>
                    </mat-list-item>

                    <mat-list-item>
                      <strong matListItemTitle>Queue Name</strong>
                      <span matListItemLine>{{ job.queueName || '—' }}</span>
                    </mat-list-item>

                    <mat-list-item>
                      <strong matListItemTitle>Backoff Strategy</strong>
                      <span matListItemLine>{{ job.backoffStrategy }}</span>
                    </mat-list-item>
                  </mat-list>
                </div>
              </mat-tab>

              <!-- Execution Logs Tab -->
              <mat-tab label="Execution Logs">
                <div class="tab-content">
                  <mat-accordion>
                    <mat-expansion-panel
                      *ngFor="let log of getExecutionLogs(jobDetails$ | async)"
                      [class]="'log-level-' + log.level"
                    >
                      <mat-expansion-panel-header>
                        <mat-panel-title>
                          <span class="log-level-badge">{{ log.level }}</span>
                          {{ log.message }}
                        </mat-panel-title>
                        <mat-panel-description>
                          {{ log.timestamp | date: 'HH:mm:ss' }}
                        </mat-panel-description>
                      </mat-expansion-panel-header>
                      <div class="log-details">
                        <div *ngIf="log.context" class="log-context">
                          <strong>Context:</strong>
                          <pre>{{ log.context | json }}</pre>
                        </div>
                        <div *ngIf="log.stackTrace" class="log-stacktrace">
                          <strong>Stack Trace:</strong>
                          <pre>{{ log.stackTrace }}</pre>
                        </div>
                      </div>
                    </mat-expansion-panel>
                  </mat-accordion>
                </div>
              </mat-tab>

              <!-- Attempts Tab -->
              <mat-tab label="Retry Attempts">
                <div class="tab-content">
                  <mat-list>
                    <mat-list-item *ngFor="let attempt of getExecutionMetrics(jobDetails$ | async)?.attempts || []">
                      <mat-icon matListItemIcon [color]="attempt.status === 'SUCCESS' ? 'accent' : 'warn'">
                        {{ attempt.status === 'SUCCESS' ? 'check_circle' : 'error_circle' }}
                      </mat-icon>
                      <div matListItemTitle>
                        Attempt #{{ attempt.attemptNumber }}
                        <mat-chip size="small">{{ attempt.status }}</mat-chip>
                      </div>
                      <div matListItemLine>
                        {{ attempt.timestamp | date: 'short' }}
                        <span *ngIf="attempt.durationMs">• {{ attempt.durationMs }}ms</span>
                      </div>
                      <div matListItemLine *ngIf="attempt.errorMessage" class="error-text">
                        {{ attempt.errorMessage }}
                      </div>
                    </mat-list-item>

                    <div *ngIf="!getExecutionMetrics(jobDetails$ | async)?.attempts?.length" class="empty-state">
                      No retry attempts
                    </div>
                  </mat-list>
                </div>
              </mat-tab>

              <!-- Error Details Tab -->
              <mat-tab label="Error Details" *ngIf="job.errorMessage">
                <div class="tab-content">
                  <div class="error-section">
                    <h4>Error Message</h4>
                    <div class="error-box">{{ job.errorMessage }}</div>

                    <h4>Error Code</h4>
                    <mat-chip color="warn" selected>{{ (jobDetails$ | async)?.errorCode || 'UNKNOWN' }}</mat-chip>
                  </div>
                </div>
              </mat-tab>
            </mat-tab-group>
          </mat-card-content>

          <mat-card-actions>
            <button
              mat-raised-button
              color="primary"
              *ngIf="job.status === 'RUNNING'"
              (click)="onCancel(job.jobId)"
            >
              <mat-icon>stop_circle</mat-icon>
              Cancel Job
            </button>

            <button
              mat-raised-button
              color="accent"
              *ngIf="job.failedCount > 0"
              (click)="onRetryFailed()"
            >
              <mat-icon>replay</mat-icon>
              Retry Failed Articles
            </button>

            <button
              mat-stroked-button
              *ngIf="['SUCCESS', 'FAILED', 'CANCELLED'].includes(job.status)"
              (click)="onDelete(job.jobId)"
            >
              <mat-icon>delete</mat-icon>
              Delete Job
            </button>

            <button mat-stroked-button (click)="onClose()">
              <mat-icon>close</mat-icon>
              Close
            </button>
          </mat-card-actions>
        </mat-card>
      </ng-container>

      <ng-template #noJob>
        <mat-card class="empty-card">
          <mat-card-content>
            <p>No job selected. Select a job from the list to view details.</p>
          </mat-card-content>
        </mat-card>
      </ng-template>
    </div>
  `,
  styles: [`
    .news-scheduler-job-details {
      padding: 1rem 0;

      .job-header {
        margin-bottom: 1rem;
      }

      .summary-grid {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
        gap: 1rem;
        margin-bottom: 1rem;

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

      .success-section {
        margin: 1.5rem 0;

        .success-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 0.5rem;

          h3 {
            margin: 0;
            font-size: 1rem;
          }

          .rate-label {
            font-size: 1.25rem;
            font-weight: 600;
            color: var(--primary-color, #3f51b5);
          }
        }

        .article-breakdown {
          display: flex;
          gap: 1rem;
          margin-top: 0.5rem;
          font-size: 0.875rem;

          .published {
            color: #4caf50;
          }

          .failed {
            color: #f44336;
          }

          .total {
            color: var(--text-secondary, #666);
          }
        }
      }

      .tab-content {
        padding: 1rem;
        max-height: 600px;
        overflow-y: auto;
      }

      mat-list-item {
        margin-bottom: 0.5rem;
      }

      code {
        font-family: monospace;
        font-size: 0.875rem;
        background-color: var(--gray-bg, #f5f5f5);
        padding: 0.25rem 0.5rem;
        border-radius: 4px;
      }

      .log-level-badge {
        display: inline-block;
        padding: 0.25rem 0.5rem;
        border-radius: 4px;
        font-size: 0.75rem;
        font-weight: 600;
        margin-right: 0.5rem;

        &.ERROR {
          background-color: #ffebee;
          color: #c62828;
        }

        &.WARN {
          background-color: #fff3e0;
          color: #e65100;
        }

        &.INFO {
          background-color: #e3f2fd;
          color: #1565c0;
        }
      }

      .log-details {
        padding: 1rem;
        background-color: var(--gray-bg, #f5f5f5);

        pre {
          font-size: 0.75rem;
          overflow-x: auto;
          margin: 0.5rem 0 0 0;
        }
      }

      .error-section {
        padding: 1rem;

        h4 {
          margin-top: 1rem;
          margin-bottom: 0.5rem;
        }

        .error-box {
          background-color: #ffebee;
          border-left: 4px solid #f44336;
          padding: 0.75rem;
          border-radius: 4px;
          color: #c62828;
          font-family: monospace;
          font-size: 0.875rem;
          margin-bottom: 1rem;
        }
      }

      .empty-state {
        padding: 2rem;
        text-align: center;
        color: var(--text-secondary, #999);
      }

      .empty-card {
        text-align: center;
        padding: 3rem 1rem;
      }

      mat-card-actions {
        gap: 0.5rem;
        padding: 1rem;
        display: flex;
        flex-wrap: wrap;

        button {
          margin-right: 0.5rem;
        }
      }
    }

    @media (max-width: 768px) {
      .news-scheduler-job-details {
        .summary-grid {
          grid-template-columns: 1fr;
        }

        .tab-content {
          max-height: 400px;
        }
      }
    }
  `],
})
export class NewsSchedulerJobDetailsComponent implements OnInit, OnDestroy {
  readonly jobDetails$!: Observable<NewsSchedulerJob | null>;
  readonly jobSummary$!: Observable<any>;
  readonly jobSuccessBadge$!: Observable<any>;

  private destroy$ = new Subject<void>();

  constructor(private store: Store) {
    this.jobDetails$ = this.store.select(selectSelectedJobDetails);
    this.jobSummary$ = this.store.select(selectSelectedJobSummary);
    this.jobSuccessBadge$ = this.store.select(selectJobSuccessBadge);
  }

  ngOnInit(): void {
    // Component will subscribe to store selectors
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
  onRetryFailed(): void {
    this.jobDetails$
      .pipe(takeUntil(this.destroy$))
      .subscribe((job: NewsSchedulerJob | null) => {
        if (job) {
          this.store.dispatch(
            NewsSchedulerActions.retryFailedArticles({
              request: { jobId: job.jobId },
            })
          );
        }
      });
  }

  /**
   * Delete job
   */
  onDelete(jobId: string): void {
    if (confirm('Delete this job? This cannot be undone.')) {
      this.store.dispatch(
        NewsSchedulerActions.deleteSchedulerJob({ jobId })
      );
      this.onClose();
    }
  }

  /**
   * Close details panel
   */
  onClose(): void {
    this.store.dispatch(
      NewsSchedulerActions.toggleJobDetails({ show: false })
    );
  }

  /**
   * Format duration in milliseconds
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
   * Get status color
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

  /**
   * Parse execution logs from JSON string
   */
  getExecutionLogs(job: NewsSchedulerJob | null): any[] {
    if (!job?.executionLogs) return [];
    try {
      return typeof job.executionLogs === 'string'
        ? JSON.parse(job.executionLogs)
        : job.executionLogs;
    } catch {
      return [];
    }
  }

  /**
   * Parse execution metrics from JSON string
   */
  getExecutionMetrics(job: NewsSchedulerJob | null): any {
    if (!job?.executionMetrics) return null;
    try {
      return typeof job.executionMetrics === 'string'
        ? JSON.parse(job.executionMetrics)
        : job.executionMetrics;
    } catch {
      return null;
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
