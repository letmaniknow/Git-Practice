import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';
import { MatListModule } from '@angular/material/list';

export interface ValidationErrorData {
  errors: string[];
}

@Component({
  selector: 'app-validation-error-dialog',
  standalone: true,
  imports: [
    CommonModule,
    MatDialogModule,
    MatButtonModule,
    MatIconModule,
    MatDividerModule,
    MatListModule
  ],
  template: `
    <div class="validation-error-dialog">
      <!-- Header -->
      <div class="dialog-header">
        <div class="header-content">
          <mat-icon class="error-icon">error_outline</mat-icon>
          <h2 mat-dialog-title class="dialog-title">Validation Errors</h2>
        </div>
        <button 
          mat-icon-button 
          class="close-button"
          (click)="onClose()"
          aria-label="Close dialog">
          <mat-icon>close</mat-icon>
        </button>
      </div>

      <mat-divider></mat-divider>

      <!-- Content -->
      <mat-dialog-content class="dialog-content">
        <p class="error-message">
          Please fix the following fields before submitting:
        </p>
        
        <mat-list class="error-list">
          <mat-list-item *ngFor="let error of data.errors; let i = index" class="error-item">
            <mat-icon matListItemIcon class="item-icon">chevron_right</mat-icon>
            <div matListItemTitle class="item-text">{{ error }}</div>
          </mat-list-item>
        </mat-list>
      </mat-dialog-content>

      <mat-divider></mat-divider>

      <!-- Actions -->
      <mat-dialog-actions class="dialog-actions">
        <button 
          mat-raised-button 
          color="primary"
          (click)="onClose()"
          class="action-button">
          <mat-icon>check_circle</mat-icon>
          Got it, Fix Errors
        </button>
      </mat-dialog-actions>
    </div>
  `,
  styles: [`
    .validation-error-dialog {
      font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
    }

    .dialog-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 24px 24px 16px 24px;
      background: linear-gradient(135deg, #fef2f2 0%, #fff 100%);
    }

    .header-content {
      display: flex;
      align-items: center;
      gap: 16px;
    }

    .error-icon {
      font-size: 32px;
      width: 32px;
      height: 32px;
      color: #dc2626;
    }

    .dialog-title {
      margin: 0;
      font-size: 24px;
      font-weight: 600;
      color: #1f2937;
    }

    .close-button {
      color: #6b7280;
      
      &:hover {
        background-color: rgba(0, 0, 0, 0.05);
      }
    }

    .dialog-content {
      padding: 24px;
      max-height: 400px;
      overflow-y: auto;
    }

    .error-message {
      margin: 0 0 16px 0;
      font-size: 15px;
      color: #4b5563;
      line-height: 1.5;
    }

    .error-list {
      padding: 0;
      
      .error-item {
        padding: 12px 16px;
        margin-bottom: 8px;
        background: #fef2f2;
        border-left: 4px solid #dc2626;
        border-radius: 6px;
        transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
        
        &:hover {
          background: #fee2e2;
          transform: translateX(4px);
          box-shadow: 0 2px 4px rgba(220, 38, 38, 0.1);
        }
        
        &:last-child {
          margin-bottom: 0;
        }
      }

      .item-icon {
        color: #dc2626;
        font-size: 20px;
        width: 20px;
        height: 20px;
      }

      .item-text {
        font-size: 14px;
        font-weight: 500;
        color: #991b1b;
        line-height: 1.4;
      }
    }

    .dialog-actions {
      padding: 16px 24px;
      justify-content: center;
      background: #f9fafb;
    }

    .action-button {
      padding: 12px 32px !important;
      font-size: 15px !important;
      font-weight: 600 !important;
      border-radius: 8px !important;
      
      mat-icon {
        margin-right: 8px;
        font-size: 20px;
        width: 20px;
        height: 20px;
      }
    }

    /* Scrollbar styling */
    .dialog-content::-webkit-scrollbar {
      width: 8px;
    }

    .dialog-content::-webkit-scrollbar-track {
      background: #f3f4f6;
      border-radius: 4px;
    }

    .dialog-content::-webkit-scrollbar-thumb {
      background: #d1d5db;
      border-radius: 4px;
      
      &:hover {
        background: #9ca3af;
      }
    }

    /* Animation */
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

    .validation-error-dialog {
      animation: slideDown 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    }
  `]
})
export class ValidationErrorDialogComponent {
  constructor(
    public dialogRef: MatDialogRef<ValidationErrorDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: ValidationErrorData
  ) {}

  onClose(): void {
    this.dialogRef.close();
  }
}
