/**
 * NEWS SCHEDULER JOB TRIGGER PANEL COMPONENT
 * 
 * Smart component for triggering new scheduler jobs.
 * Connected to NgRx store, displays form for job parameters.
 */

import { Component, OnInit, OnDestroy } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Store } from '@ngrx/store';
import { Subject, Observable } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

import { CommonModule } from '@angular/common';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';

import * as NewsSchedulerActions from '../../store/news-scheduler.actions';
import {
  selectTriggering,
  selectError,
} from '../../store/news-scheduler.selectors';
import {
  NewsSchedulerPriority,
  SchedulerJobTriggerRequest,
} from '../../models/news-scheduler.model';

/**
 * NewsSchedulerJobTriggerPanelComponent
 * 
 * Displays a form for creating and triggering new scheduler jobs.
 * Users can specify:
 * - News category/source
 * - Priority level
 * - Optional scheduling time
 * - Retry configuration
 */
@Component({
  selector: 'app-news-scheduler-job-trigger-panel',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatCardModule,
    MatProgressSpinnerModule,
    MatDatepickerModule,
    MatNativeDateModule,
  ],
  template: `
    <mat-card class="news-scheduler-trigger-panel">
      <mat-card-header>
        <mat-card-title>Trigger New Publishing Job</mat-card-title>
        <mat-card-subtitle>
          Schedule immediate or delayed news publishing
        </mat-card-subtitle>
      </mat-card-header>

      <mat-card-content>
        <form [formGroup]="triggerForm" (ngSubmit)="onTriggerJob()">
          <!-- Priority -->
          <mat-form-field appearance="outline" class="full-width">
            <mat-label>Priority</mat-label>
            <mat-select formControlName="priority">
              <mat-option *ngFor="let p of priorities" [value]="p">
                {{ p }}
              </mat-option>
            </mat-select>
            <mat-hint>
              HIGH: Immediate, MEDIUM: Within 1h, LOW: Background processing
            </mat-hint>
          </mat-form-field>

          <!-- Max Retries -->
          <mat-form-field appearance="outline" class="full-width">
            <mat-label>Max Retries</mat-label>
            <input
              matInput
              type="number"
              formControlName="maxRetries"
              min="0"
              max="10"
            />
            <mat-hint>Number of retry attempts if job fails</mat-hint>
          </mat-form-field>

          <!-- Error Message -->
          <div *ngIf="error$ | async as error" class="error-message">
            {{ error }}
          </div>
        </form>
      </mat-card-content>

      <mat-card-actions>
        <button mat-raised-button color="primary"
          (click)="onTriggerJob()"
          [disabled]="!triggerForm.valid || (triggering$ | async)"
        >
          <mat-spinner *ngIf="triggering$ | async" diameter="20"></mat-spinner>
          {{ (triggering$ | async) ? 'Triggering...' : 'Trigger Job' }}
        </button>
        <button mat-stroked-button (click)="onReset()">Reset Form</button>
      </mat-card-actions>
    </mat-card>
  `,
  styles: [`
    .news-scheduler-trigger-panel {
      max-width: 600px;
      margin: 1rem 0;

      mat-card-header {
        margin-bottom: 1rem;
      }

      form {
        display: flex;
        flex-direction: column;
        gap: 1rem;
      }

      .full-width {
        width: 100%;
      }

      .error-message {
        color: var(--error-color, #f44336);
        font-size: 0.875rem;
        padding: 0.5rem;
        background-color: var(--error-bg, rgba(244, 67, 54, 0.1));
        border-radius: 4px;
        margin-bottom: 1rem;
      }

      mat-card-actions {
        gap: 1rem;
        padding: 1rem;
      }

      button {
        display: flex;
        gap: 0.5rem;
        align-items: center;
      }

      mat-spinner {
        display: inline-block;
      }
    }

    @media (max-width: 768px) {
      .news-scheduler-trigger-panel {
        max-width: 100%;
      }
    }
  `],
})
export class NewsSchedulerJobTriggerPanelComponent implements OnInit, OnDestroy {
  triggerForm!: FormGroup;
  priorities = Object.values(NewsSchedulerPriority);
  readonly triggering$!: Observable<boolean>;
  readonly error$!: Observable<string | null>;

  private destroy$ = new Subject<void>();

  constructor(
    private fb: FormBuilder,
    private store: Store
  ) {
    this.triggering$ = this.store.select(selectTriggering);
    this.error$ = this.store.select(selectError);
  }

  ngOnInit(): void {
    this.initializeForm();
  }

  /**
   * Initialize trigger form with default values
   */
  private initializeForm(): void {
    this.triggerForm = this.fb.group({
      priority: [NewsSchedulerPriority.MEDIUM],
      maxRetries: [3, [Validators.required, Validators.min(0), Validators.max(10)]],
    });
  }

  /**
   * Trigger new scheduler job
   */
  onTriggerJob(): void {
    if (!this.triggerForm.valid) return;

    const request: SchedulerJobTriggerRequest = {
      priority: this.triggerForm.get('priority')?.value,
      maxRetries: this.triggerForm.get('maxRetries')?.value,
    };

    this.store.dispatch(
      NewsSchedulerActions.triggerSchedulerJob({ request })
    );
  }

  /**
   * Reset form to initial state
   */
  onReset(): void {
    this.triggerForm.reset({
      priority: NewsSchedulerPriority.MEDIUM,
      maxRetries: 3,
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
