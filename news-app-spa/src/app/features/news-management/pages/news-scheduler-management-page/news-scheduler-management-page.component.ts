/**
 * NEWS SCHEDULER MANAGEMENT PAGE
 * 
 * Page container component for the scheduler feature.
 * Orchestrates all scheduler sub-components and manages layout.
 */

import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Store } from '@ngrx/store';
import { Observable, Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTabsModule } from '@angular/material/tabs';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDividerModule } from '@angular/material/divider';

import { NewsSchedulerJobTriggerPanelComponent } from '../../components/news-scheduler-job-trigger-panel/news-scheduler-job-trigger-panel.component';
import { NewsSchedulerJobsListComponent } from '../../components/news-scheduler-jobs-list/news-scheduler-jobs-list.component';
import { NewsSchedulerJobDetailsComponent } from '../../components/news-scheduler-job-details/news-scheduler-job-details.component';
import { NewsSchedulerMetricsDashboardComponent } from '../../components/news-scheduler-metrics-dashboard/news-scheduler-metrics-dashboard.component';
import { NewsSchedulerFailedArticlesComponent } from '../../components/news-scheduler-failed-articles/news-scheduler-failed-articles.component';

import * as NewsSchedulerActions from '../../store/news-scheduler.actions';
import {
  selectShowTriggerForm,
  selectShowJobDetails,
  selectShowMetrics,
  selectError,
  selectAnyOperationInProgress,
} from '../../store/news-scheduler.selectors';

/**
 * NewsSchedulerManagementPageComponent
 * 
 * Main page for news scheduler management.
 * Features:
 * - Job triggering form (collapsible)
 * - Jobs list with pagination and filtering
 * - Job details panel (side-by-side or modal)
 * - Performance metrics dashboard
 * - Error handling and notifications
 */
@Component({
  selector: 'app-news-scheduler-management-page',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatIconModule,
    MatTabsModule,
    MatSidenavModule,
    MatProgressBarModule,
    MatSnackBarModule,
    MatDividerModule,
    NewsSchedulerJobTriggerPanelComponent,
    NewsSchedulerJobsListComponent,
    NewsSchedulerJobDetailsComponent,
    NewsSchedulerMetricsDashboardComponent,
    NewsSchedulerFailedArticlesComponent,
  ],
  template: `
    <div class="news-scheduler-management-page">
      <!-- Page Header -->
      <header class="page-header">
        <div class="header-content">
          <h1>News Publisher Scheduler</h1>
          <p class="subtitle">
            Manage and monitor automated news publishing jobs
          </p>
        </div>

        <div class="header-actions">
          <button
            mat-raised-button
            color="primary"
            (click)="toggleTriggerForm()"
            [disabled]="(operationInProgress$ | async)"
          >
            <mat-icon>add</mat-icon>
            New Job
          </button>

          <button
            mat-icon-button
            (click)="toggleMetrics()"
            matTooltip="Toggle Metrics"
          >
            <mat-icon>assessment</mat-icon>
          </button>

          <button
            mat-icon-button
            (click)="onRefresh()"
            matTooltip="Refresh"
            [disabled]="(operationInProgress$ | async)"
          >
            <mat-icon>refresh</mat-icon>
          </button>
        </div>
      </header>

      <!-- Loading Bar -->
      <mat-progress-bar
        *ngIf="operationInProgress$ | async"
        mode="indeterminate"
      ></mat-progress-bar>

      <!-- Main Content with Sidebar -->
      <mat-sidenav-container class="sidenav-container">
        <!-- Main Content Area -->
        <mat-sidenav-content class="main-content">
          <!-- Tabs for different views -->
          <mat-tab-group>
            <!-- Jobs Tab -->
            <mat-tab label="Publishing Jobs">
              <ng-template mat-tab-label>
                <mat-icon class="tab-icon">list</mat-icon>
                Publishing Jobs
              </ng-template>

              <!-- Trigger Form (Collapsible) -->
              <div
                class="trigger-form-container"
                *ngIf="showTriggerForm$ | async"
              >
                <app-news-scheduler-job-trigger-panel></app-news-scheduler-job-trigger-panel>
              </div>

              <!-- Jobs List -->
              <app-news-scheduler-jobs-list></app-news-scheduler-jobs-list>
            </mat-tab>

            <!-- Metrics Tab -->
            <mat-tab label="Performance Metrics">
              <ng-template mat-tab-label>
                <mat-icon class="tab-icon">assessment</mat-icon>
                Performance Metrics
              </ng-template>

              <app-news-scheduler-metrics-dashboard></app-news-scheduler-metrics-dashboard>
            </mat-tab>

            <!-- Failed Articles Tab -->
            <mat-tab label="Failed Articles">
              <ng-template mat-tab-label>
                <mat-icon class="tab-icon">error</mat-icon>
                Failed Articles
              </ng-template>

              <app-news-scheduler-failed-articles></app-news-scheduler-failed-articles>
            </mat-tab>

            <!-- Help Tab -->
            <mat-tab label="Help">
              <ng-template mat-tab-label>
                <mat-icon class="tab-icon">help</mat-icon>
                Help
              </ng-template>

              <div class="help-content">
                <h3>Scheduler Guide</h3>

                <section class="help-section">
                  <h4>What is the News Scheduler?</h4>
                  <p>
                    The News Scheduler allows you to create and monitor automated
                    publishing jobs. You can publish news articles to multiple
                    channels simultaneously with configurable retry logic and
                    error handling.
                  </p>
                </section>

                <section class="help-section">
                  <h4>Creating a New Job</h4>
                  <ol>
                    <li>Click the "New Job" button</li>
                    <li>Fill in the job details (source, priority, etc.)</li>
                    <li>Configure retry settings</li>
                    <li>Click "Trigger Job" to start publishing</li>
                  </ol>
                </section>

                <section class="help-section">
                  <h4>Job Status Meanings</h4>
                  <ul>
                    <li><strong>RUNNING:</strong> Job is currently executing</li>
                    <li><strong>SUCCESS:</strong> All articles published successfully</li>
                    <li><strong>PARTIAL_SUCCESS:</strong> Some articles failed</li>
                    <li><strong>FAILED:</strong> Most articles failed to publish</li>
                    <li><strong>CANCELLED:</strong> Job was manually cancelled</li>
                  </ul>
                </section>

                <section class="help-section">
                  <h4>Understanding Metrics</h4>
                  <p>
                    The Performance Metrics tab shows aggregated statistics including:
                  </p>
                  <ul>
                    <li>Total jobs executed</li>
                    <li>Job and article success rates</li>
                    <li>Error distribution</li>
                    <li>Average job duration</li>
                  </ul>
                </section>

                <section class="help-section">
                  <h4>Troubleshooting</h4>
                  <p>
                    If a job fails, check the error details in the job details panel.
                    Common errors include:
                  </p>
                  <ul>
                    <li><strong>TIMEOUT:</strong> Job took too long</li>
                    <li><strong>AUTH_FAILED:</strong> Authentication error</li>
                    <li><strong>NETWORK_ERROR:</strong> Network connectivity issue</li>
                    <li><strong>VALIDATION_ERROR:</strong> Invalid article data</li>
                  </ul>
                </section>
              </div>
            </mat-tab>
          </mat-tab-group>
        </mat-sidenav-content>

        <!-- Details Sidebar -->
        <mat-sidenav
          #detailsSidenav
          position="end"
          mode="side"
          [opened]="showJobDetails$ | async"
          class="details-sidenav"
        >
          <div class="sidenav-header">
            <h2>Job Details</h2>
            <button
              mat-icon-button
              (click)="toggleJobDetails(false)"
              class="close-button"
            >
              <mat-icon>close</mat-icon>
            </button>
          </div>
          <mat-divider></mat-divider>
          <app-news-scheduler-job-details></app-news-scheduler-job-details>
        </mat-sidenav>
      </mat-sidenav-container>
    </div>
  `,
  styles: [`
    .news-scheduler-management-page {
      height: 100%;
      display: flex;
      flex-direction: column;
      background-color: var(--background-color, #fafafa);

      .page-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 1.5rem 2rem;
        background-color: white;
        border-bottom: 1px solid var(--divider-color, #e0e0e0);
        box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);

        .header-content {
          flex: 1;

          h1 {
            margin: 0 0 0.25rem 0;
            font-size: 2rem;
            font-weight: 500;
          }

          .subtitle {
            margin: 0;
            font-size: 0.875rem;
            color: var(--text-secondary, #666);
          }
        }

        .header-actions {
          display: flex;
          gap: 0.5rem;
          align-items: center;
        }
      }

      mat-progress-bar {
        height: 2px;
      }

      .sidenav-container {
        flex: 1;
        overflow: hidden;
      }

      .main-content {
        padding: 0;
        overflow-y: auto;

        mat-tab-group {
          height: 100%;

          ::ng-deep {
            .mat-mdc-tab-labels {
              background-color: white;
              border-bottom: 1px solid var(--divider-color, #e0e0e0);
            }

            .mat-mdc-tab-body-content {
              padding: 1.5rem 2rem;
            }
          }
        }

        .tab-icon {
          margin-right: 0.5rem;
        }

        .trigger-form-container {
          margin-bottom: 1rem;
          animation: slideDown 0.3s ease-out;
        }

        @keyframes slideDown {
          from {
            opacity: 0;
            transform: translateY(-20px);
          }
          to {
            opacity: 1;
            transform: translateY(0);
          }
        }

        .help-content {
          max-width: 800px;
          line-height: 1.6;

          h3 {
            margin-top: 0;
            font-size: 1.5rem;
            color: var(--primary-color, #3f51b5);
          }

          .help-section {
            margin-bottom: 2rem;
            padding-bottom: 1rem;
            border-bottom: 1px solid var(--divider-color, #e0e0e0);

            h4 {
              margin-top: 0;
              font-size: 1.125rem;
              color: var(--primary-color, #3f51b5);
            }

            ul, ol {
              margin: 0.5rem 0;
              padding-left: 1.5rem;
            }

            li {
              margin-bottom: 0.25rem;
            }
          }
        }
      }

      .details-sidenav {
        width: 400px;
        background-color: white;
        border-left: 1px solid var(--divider-color, #e0e0e0);
        box-shadow: -2px 0 8px rgba(0, 0, 0, 0.1);

        .sidenav-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          padding: 1rem;

          h2 {
            margin: 0;
            font-size: 1.25rem;
            flex: 1;
          }

          .close-button {
            margin-right: -0.5rem;
          }
        }

        ::ng-deep {
          .mat-divider {
            margin: 0;
          }
        }

        overflow-y: auto;
        padding: 1rem;
      }
    }

    @media (max-width: 1200px) {
      .news-scheduler-management-page {
        .details-sidenav {
          width: 350px;
        }
      }
    }

    @media (max-width: 768px) {
      .news-scheduler-management-page {
        .page-header {
          flex-direction: column;
          align-items: flex-start;
          gap: 1rem;

          .header-actions {
            width: 100%;
            justify-content: flex-end;
          }
        }

        .main-content {
          ::ng-deep .mat-mdc-tab-body-content {
            padding: 1rem;
          }
        }

        .details-sidenav {
          width: 100% !important;
          max-width: 100vw;
          position: fixed;
          right: 0;
          top: 0;
          bottom: 0;
          z-index: 1000;
          box-shadow: -2px 0 16px rgba(0, 0, 0, 0.2);
        }
      }
    }
  `],
})
export class NewsSchedulerManagementPageComponent implements OnInit, OnDestroy {
  readonly showTriggerForm$!: Observable<boolean>;
  readonly showJobDetails$!: Observable<boolean>;
  readonly showMetrics$!: Observable<boolean>;
  readonly error$!: Observable<string | null>;
  readonly operationInProgress$!: Observable<boolean>;

  private destroy$ = new Subject<void>();
  private errorUnsubscribe$ = new Subject<void>();

  constructor(
    private store: Store,
    private snackBar: MatSnackBar
  ) {
    this.showTriggerForm$ = this.store.select(selectShowTriggerForm);
    this.showJobDetails$ = this.store.select(selectShowJobDetails);
    this.showMetrics$ = this.store.select(selectShowMetrics);
    this.error$ = this.store.select(selectError);
    this.operationInProgress$ = this.store.select(selectAnyOperationInProgress);
  }

  ngOnInit(): void {
    // Subscribe to errors and show them as notifications
    this.error$
      .pipe(takeUntil(this.errorUnsubscribe$))
      .subscribe((error) => {
        if (error) {
          this.snackBar.open(error as string, 'Dismiss', {
            duration: 5000,
            panelClass: ['error-snackbar'],
            horizontalPosition: 'end',
            verticalPosition: 'top',
          });

          // Clear error after showing
          this.store.dispatch(NewsSchedulerActions.clearSchedulerError());
        }
      });
  }

  /**
   * Toggle trigger form visibility
   */
  toggleTriggerForm(): void {
    this.store.dispatch(
      NewsSchedulerActions.toggleTriggerForm({ show: true })
    );
  }

  /**
   * Toggle job details visibility
   */
  toggleJobDetails(show: boolean): void {
    this.store.dispatch(
      NewsSchedulerActions.toggleJobDetails({ show })
    );
  }

  /**
   * Toggle metrics visibility
   */
  toggleMetrics(): void {
    this.store.dispatch(
      NewsSchedulerActions.toggleMetricsDashboard({ show: true })
    );
  }

  /**
   * Refresh all data
   */
  onRefresh(): void {
    this.store.dispatch(NewsSchedulerActions.loadSchedulerJobs({}));
    this.store.dispatch(NewsSchedulerActions.loadSchedulerMetrics({}));
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    this.errorUnsubscribe$.next();
    this.errorUnsubscribe$.complete();
  }
}
