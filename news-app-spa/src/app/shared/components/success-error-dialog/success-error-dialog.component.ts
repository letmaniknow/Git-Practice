import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-success-error-dialog',
  standalone: true,
  imports: [CommonModule, MatDialogModule, MatButtonModule, MatIconModule],
  template: `
    <div class="success-error-dialog-content">
      <mat-icon class="success-error-dialog-icon" [ngClass]="{ 'success': data.type === 'success', 'error': data.type === 'error', 'warning': data.type === 'warning' }">
        {{ data.type === 'success' ? 'check_circle' : (data.type === 'warning' ? 'warning' : 'error') }}
      </mat-icon>
      <h2 class="success-error-dialog-title">{{ data.title }}</h2>
      <div class="success-error-dialog-message" [innerHTML]="data.message"></div>
      <div class="success-error-dialog-actions">
        <button *ngIf="data.showCancel" mat-stroked-button (click)="onCancel()">{{ data.cancelText || 'Cancel' }}</button>
        <button *ngIf="data.type === 'success'" mat-flat-button color="primary" [disabled]="data.loading" (click)="onOk()">{{ data.okText || 'OK' }}</button>
        <button *ngIf="data.type === 'error' || data.type === 'warning'" mat-flat-button color="warn" [disabled]="data.loading" (click)="onOk()">{{ data.okText || 'OK' }}</button>
      </div>
    </div>
  `,
  styles: [`
    .success-error-dialog-content {
      text-align: center;
      padding: 32px 24px 20px 24px;
      max-width: 420px;
      min-width: 320px;
      box-sizing: border-box;
      overflow: hidden;
    }
    .success-error-dialog-icon {
      font-size: 96px;
      margin-bottom: 40px;
      transition: font-size 0.2s;
    }
    .success-error-dialog-icon.success {
      color: #43a047 !important;
      text-shadow: 0 2px 8px #e8f5e9;
      background: none;
      border-radius: 50%;
    }
    .success-error-dialog-icon.error {
      color: #d32f2f;
    }
    .success-error-dialog-icon.warning {
      color: #ffa000;
    }
    .success-error-dialog-title {
      margin: 0 0 10px 0;
      font-weight: 700;
      color: #222;
      font-size: 2rem;
      line-height: 1.2;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
    .success-error-dialog-message {
      color: #555;
      font-size: 1.08rem;
      margin-bottom: 18px;
      word-break: break-word;
      overflow-x: auto;
      max-width: 100%;
    }
    .success-error-dialog-actions {
      display: flex;
      justify-content: center;
      margin-top: 24px;
      gap: 16px;
    }
    .success-error-dialog-actions .cancel-btn {
      min-width: 110px;
      font-weight: 600;
      border-color: #bdbdbd;
      color: #333 !important;
      background: #f5f5f5 !important;
      border-width: 1px;
    }
    .success-error-dialog-actions .ok-btn {
      min-width: 110px;
      font-weight: 600;
    }
  `]
})
export class SuccessErrorDialogComponent {
    getButtonColor() {
      if (this.data.type === 'success') return 'primary';
      if (this.data.type === 'error') return 'warn';
      if (this.data.type === 'warning') return 'warn';
      return 'primary';
    }
  constructor(
    @Inject(MAT_DIALOG_DATA) public data: { type: 'success' | 'error' | 'warning', title: string, message: string, showCancel?: boolean, okText?: string, cancelText?: string, loading?: boolean },
    private dialogRef: MatDialogRef<SuccessErrorDialogComponent>
  ) {}

  onOk() {
    this.dialogRef.close(true);
  }

  onCancel() {
    this.dialogRef.close(false);
  }
}
