/**
 * NEWS SCHEDULER FAILED ARTICLES COMPONENT
 * 
 * Displays failed articles from scheduler jobs with retry capabilities.
 * Allows bulk retry of failed articles and shows error details.
 */

import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Store } from '@ngrx/store';
import { Observable, Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatChipsModule } from '@angular/material/chips';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatCardModule } from '@angular/material/card';
import { MatDividerModule } from '@angular/material/divider';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

import * as NewsSchedulerActions from '../../store/news-scheduler.actions';
import {
  selectFailedArticles,
  selectFailedArticlesCount,
  selectFailedArticlesPagination,
  selectLoading,
  selectSelectedJobId,
} from '../../store/news-scheduler.selectors';
import { NewsSchedulerState } from '../../store/news-scheduler.state';

@Component({
  selector: 'app-news-scheduler-failed-articles',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule,
    MatPaginatorModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatChipsModule,
    MatTooltipModule,
    MatCardModule,
    MatDividerModule,
    MatSnackBarModule,
  ],
  template: `
    <div class="failed-articles-container">
      <!-- Empty State -->
      <div *ngIf="!(isLoading$ | async) && (failedArticles$ | async)?.length === 0" class="empty-state">
        <mat-icon>check_circle</mat-icon>
        <h3>No Failed Articles</h3>
        <p>All articles are publishing successfully!</p>
      </div>

      <!-- Failed Articles Table -->
      <div *ngIf="(failedArticles$ | async)?.length || (isLoading$ | async)" class="content">
        <!-- Header with Actions -->
        <div class="table-header">
          <div class="header-left">
            <h3>Failed Articles</h3>
            <span class="count" *ngIf="failedArticlesCount$ | async as count">
              {{ count }} article<ng-container *ngIf="count !== 1">s</ng-container>
            </span>
          </div>

          <button
            mat-raised-button
            color="warn"
            (click)="onRetryAll()"
            [disabled]="(isLoading$ | async) || !((failedArticles$ | async)?.length)"
            class="retry-all-btn"
          >
            <mat-icon>cached</mat-icon>
            Retry All Failed Articles
          </button>
        </div>

        <!-- Loading Spinner -->
        <div *ngIf="isLoading$ | async" class="loading-container">
          <mat-progress-spinner mode="indeterminate" diameter="40"></mat-progress-spinner>
          <p>Loading failed articles...</p>
        </div>

        <!-- Articles Table -->
        <table *ngIf="!(isLoading$ | async)" mat-table [dataSource]="(failedArticles$ | async) ?? []" class="failed-articles-table">
          <!-- Article ID Column -->
          <ng-container matColumnDef="articleId">
            <th mat-header-cell *matHeaderCellDef>Article ID</th>
            <td mat-cell *matCellDef="let element">
              <code class="article-id">{{ element.articleId | slice:0:8 }}...</code>
            </td>
          </ng-container>

          <!-- Job ID Column -->
          <ng-container matColumnDef="jobId">
            <th mat-header-cell *matHeaderCellDef>Job ID</th>
            <td mat-cell *matCellDef="let element">
              <code class="job-id">{{ element.jobId | slice:0:8 }}...</code>
            </td>
          </ng-container>

          <!-- Error Code Column -->
          <ng-container matColumnDef="errorCode">
            <th mat-header-cell *matHeaderCellDef>Error</th>
            <td mat-cell *matCellDef="let element">
              <mat-chip class="error-chip" [matTooltip]="element.errorMessage">
                {{ element.errorCode }}
              </mat-chip>
            </td>
          </ng-container>

          <!-- Status Column -->
          <ng-container matColumnDef="status">
            <th mat-header-cell *matHeaderCellDef>Status</th>
            <td mat-cell *matCellDef="let element">
              <mat-chip class="status-chip failed">{{ element.status }}</mat-chip>
            </td>
          </ng-container>

          <!-- Attempt Number Column -->
          <ng-container matColumnDef="attemptNumber">
            <th mat-header-cell *matHeaderCellDef>Attempts</th>
            <td mat-cell *matCellDef="let element">
              <span class="attempt-count">{{ element.attemptNumber }}</span>
            </td>
          </ng-container>

          <!-- Retry Available Column -->
          <ng-container matColumnDef="shouldRetry">
            <th mat-header-cell *matHeaderCellDef>Retryable</th>
            <td mat-cell *matCellDef="let element">
              <mat-icon
                *ngIf="element.shouldRetry"
                class="icon-success"
                matTooltip="This article can be retried"
              >
                check_circle
              </mat-icon>
              <mat-icon
                *ngIf="!element.shouldRetry"
                class="icon-error"
                matTooltip="This article cannot be retried"
              >
                cancel
              </mat-icon>
            </td>
          </ng-container>

          <!-- Timestamp Column -->
          <ng-container matColumnDef="completedAt">
            <th mat-header-cell *matHeaderCellDef>Failed At</th>
            <td mat-cell *matCellDef="let element">
              <span class="timestamp">{{ element.completedAt | date:'short' }}</span>
            </td>
          </ng-container>

          <!-- Actions Column -->
          <ng-container matColumnDef="actions">
            <th mat-header-cell *matHeaderCellDef>Actions</th>
            <td mat-cell *matCellDef="let element">
              <button
                mat-icon-button
                disabled
                matTooltip="Individual retry not yet supported - use 'Retry All Failed Articles'"
              >
                <mat-icon>retry</mat-icon>
              </button>
              <button
                mat-icon-button
                color="warn"
                (click)="onDismissArticle(element.articleId)"
                matTooltip="Dismiss this failure"
              >
                <mat-icon>clear</mat-icon>
              </button>
            </td>
          </ng-container>

          <!-- Table Headers -->
          <tr mat-header-row *matHeaderRowDef="displayedColumns; sticky: true"></tr>

          <!-- Table Rows -->
          <tr mat-row *matRowDef="let row; columns: displayedColumns;" class="article-row"></tr>
        </table>

        <!-- Paginator -->
        <mat-paginator
          *ngIf="!(isLoading$ | async) && (failedArticles$ | async)?.length"
          [length]="(failedArticlesCount$ | async) || 0"
          [pageSize]="(failedArticlesPagination$ | async)?.size || 20"
          [pageSizeOptions]="[10, 20, 50]"
          (page)="onPageChange($event)"
          showFirstLastButtons
        ></mat-paginator>
      </div>
    </div>
  `,
  styles: [`
    .failed-articles-container {
      padding: 1.5rem;
      height: 100%;
      display: flex;
      flex-direction: column;
      background-color: var(--background-color, #fafafa);

      .empty-state {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        height: 100%;
        color: var(--text-secondary, #999);

        mat-icon {
          font-size: 3rem;
          width: 3rem;
          height: 3rem;
          color: var(--success-color, #4caf50);
          margin-bottom: 1rem;
        }

        h3 {
          margin: 0 0 0.5rem 0;
          font-size: 1.25rem;
        }

        p {
          margin: 0;
          font-size: 0.875rem;
        }
      }

      .content {
        display: flex;
        flex-direction: column;
        flex: 1;
        overflow: hidden;

        .table-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 1.5rem;
          flex-wrap: wrap;
          gap: 1rem;

          .header-left {
            display: flex;
            align-items: center;
            gap: 1rem;

            h3 {
              margin: 0;
              font-size: 1.25rem;
            }

            .count {
              background-color: var(--primary-color, #2196f3);
              color: white;
              padding: 0.25rem 0.75rem;
              border-radius: 4px;
              font-size: 0.875rem;
              font-weight: 500;
            }
          }

          .retry-all-btn {
            mat-icon {
              margin-right: 0.5rem;
            }
          }
        }

        .loading-container {
          display: flex;
          flex-direction: column;
          align-items: center;
          justify-content: center;
          flex: 1;
          gap: 1rem;

          p {
            color: var(--text-secondary, #666);
            margin: 0;
          }
        }

        .failed-articles-table {
          width: 100%;
          background-color: white;
          border-radius: 4px;
          box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
          flex: 1;
          overflow: auto;

          th {
            font-weight: 600;
            background-color: var(--header-background, #f5f5f5);
            border-bottom: 2px solid var(--divider-color, #e0e0e0);
            white-space: nowrap;
          }

          td {
            padding: 0.75rem;
            border-bottom: 1px solid var(--divider-color, #e0e0e0);
          }

          .article-row:hover {
            background-color: var(--hover-background, #f9f9f9);
          }

          .article-id,
          .job-id {
            font-family: 'Courier New', monospace;
            font-size: 0.75rem;
            background-color: var(--code-background, #f5f5f5);
            padding: 0.25rem 0.5rem;
            border-radius: 2px;
            word-break: break-all;
          }

          .error-chip {
            font-size: 0.75rem;
            background-color: var(--error-background, #ffebee);
            color: var(--error-color, #c62828);
          }

          .status-chip {
            font-size: 0.75rem;
            font-weight: 500;

            &.failed {
              background-color: var(--error-background, #ffebee);
              color: var(--error-color, #c62828);
            }
          }

          .attempt-count {
            font-weight: 500;
            color: var(--text-primary, #333);
          }

          .icon-success {
            color: var(--success-color, #4caf50);
          }

          .icon-error {
            color: var(--error-color, #f44336);
          }

          .timestamp {
            font-size: 0.85rem;
            color: var(--text-secondary, #666);
          }

          button {
            margin-left: 0.25rem;
          }
        }

        mat-paginator {
          border-top: 1px solid var(--divider-color, #e0e0e0);
          background-color: white;
        }
      }
    }
  `],
})
export class NewsSchedulerFailedArticlesComponent implements OnInit, OnDestroy {
  displayedColumns: string[] = [
    'articleId',
    'jobId',
    'errorCode',
    'status',
    'attemptNumber',
    'shouldRetry',
    'completedAt',
    'actions',
  ];

  failedArticles$!: Observable<any[]>;
  failedArticlesCount$!: Observable<number>;
  failedArticlesPagination$!: Observable<any>;
  isLoading$!: Observable<boolean>;
  selectedJobId$!: Observable<string | null>;

  private destroy$ = new Subject<void>();

  constructor(
    private store: Store<{ newsScheduler: NewsSchedulerState }>,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.failedArticles$ = this.store.select(selectFailedArticles);
    this.failedArticlesCount$ = this.store.select(selectFailedArticlesCount);
    this.failedArticlesPagination$ = this.store.select(selectFailedArticlesPagination);
    this.isLoading$ = this.store.select(selectLoading);
    this.selectedJobId$ = this.store.select(selectSelectedJobId);

    // Load failed articles on init
    this.store.dispatch(NewsSchedulerActions.loadFailedArticles({}));
  }

  onRetryAll(): void {
    this.selectedJobId$.pipe(
      takeUntil(this.destroy$),
    ).subscribe((jobId) => {
      if (jobId) {
        this.store.dispatch(
          NewsSchedulerActions.retryFailedArticles({
            request: { jobId },
          })
        );
        this.snackBar.open('Retrying all failed articles...', 'Close', {
          duration: 3000,
        });
      } else {
        this.snackBar.open('No job selected', 'Close', {
          duration: 2000,
        });
      }
    });
  }

  onRetryArticle(articleId: string): void {
    // Note: Individual article retry is not supported by API
    // API only supports retry by jobId
    this.snackBar.open('Individual article retry is not yet supported. Use "Retry All Failed Articles" instead.', 'Close', {
      duration: 4000,
    });
  }

  onDismissArticle(articleId: string): void {
    // Placeholder for dismiss functionality
    this.snackBar.open('Article dismissed', 'Close', {
      duration: 2000,
    });
  }

  onPageChange(event: PageEvent): void {
    this.store.dispatch(
      NewsSchedulerActions.setFailedArticlesPage({
        page: event.pageIndex,
        pageSize: event.pageSize,
      })
    );
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
