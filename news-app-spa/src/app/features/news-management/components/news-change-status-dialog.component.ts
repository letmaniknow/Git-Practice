import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatOptionModule } from '@angular/material/core';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-news-change-status-dialog',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatSelectModule,
    MatButtonModule,
    MatOptionModule,
    MatIconModule
  ],
  template: `
    <div class="change-status-dialog-content">
      <mat-icon class="change-status-dialog-icon">sync_alt</mat-icon>
      <h2 class="change-status-dialog-title">Change News Status</h2>
      <div class="change-status-dialog-message">Select a new workflow status for this news item.</div>
      <mat-form-field appearance="fill" style="width: 100%; margin-bottom: 24px;">
        <mat-label>Select new status</mat-label>
        <mat-select [(ngModel)]="selectedStatus">
          <mat-option *ngFor="let status of data.statuses" [value]="status">
            {{ status }}
          </mat-option>
        </mat-select>
      </mat-form-field>
      <div class="change-status-dialog-actions">
        <button mat-stroked-button color="basic" (click)="onCancel()">Cancel</button>
        <button mat-flat-button color="primary" [disabled]="!selectedStatus" (click)="onConfirm()">Change Status</button>
      </div>
    </div>
  `,
  styles: [`
    .change-status-dialog-content {
      text-align: center;
      padding: 32px 24px 20px 24px;
      max-width: 420px;
      min-width: 320px;
      box-sizing: border-box;
      overflow: hidden;
      background: #fff;
      border-radius: 12px;
      box-shadow: 0 8px 32px rgba(60,60,60,0.18);
    }
    .change-status-dialog-icon {
      font-size: 64px;
      margin-bottom: 24px;
      color: #1976d2;
      background: none;
      border-radius: 50%;
    }
    .change-status-dialog-title {
      margin: 0 0 10px 0;
      font-weight: 700;
      color: #222;
      font-size: 2rem;
      line-height: 1.2;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
    .change-status-dialog-message {
      color: #555;
      font-size: 1.08rem;
      margin-bottom: 18px;
      word-break: break-word;
      overflow-x: auto;
      max-width: 100%;
    }
    .change-status-dialog-actions {
      display: flex;
      justify-content: center;
      gap: 16px;
      margin-top: 24px;
    }
    .change-status-dialog-actions button {
      min-width: 110px;
      font-weight: 600;
    }
  `]
})
export class NewsChangeStatusDialogComponent {
  selectedStatus: string;

  constructor(
    public dialogRef: MatDialogRef<NewsChangeStatusDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { statuses: string; currentStatus: string }
  ) {
    this.selectedStatus = data.currentStatus;
  }

  onCancel(): void {
    this.dialogRef.close();
  }

  onConfirm(): void {
    this.dialogRef.close(this.selectedStatus);
  }
}
