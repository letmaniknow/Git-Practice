import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-news-soft-delete-dialog',
  standalone: true,
  imports: [CommonModule, MatDialogModule, MatButtonModule, MatIconModule],
  template: `
    <div class="soft-delete-dialog-content">
      <mat-icon class="soft-delete-dialog-icon" color="warn">delete_forever</mat-icon>
      <h2 class="soft-delete-dialog-title">Confirm Soft Delete</h2>
      <div class="soft-delete-dialog-message">
        Are you sure you want to <b>soft delete</b> this news item?<br>
        This action can be undone by restoring the item later.
      </div>
      <div class="soft-delete-dialog-actions">
        <button mat-stroked-button color="basic" class="cancel-btn" (click)="onCancel()">Cancel</button>
        <button mat-flat-button color="warn" class="ok-btn" (click)="onConfirm()">Delete</button>
      </div>
    </div>
  `,
  styles: [`
    .soft-delete-dialog-content {
      text-align: center;
      padding: 32px 24px 20px 24px;
      max-width: 420px;
      min-width: 320px;
      box-sizing: border-box;
      overflow: hidden;
    }
    .soft-delete-dialog-icon {
      font-size: 96px;
      margin-bottom: 40px;
      color: #e53935 !important;
      text-shadow: 0 2px 8px #ffebee;
    }
    .soft-delete-dialog-title {
      font-size: 1.5rem;
      margin-bottom: 16px;
      font-weight: 600;
    }
    .soft-delete-dialog-message {
      font-size: 1.1rem;
      margin-bottom: 32px;
      color: #444;
    }
    .soft-delete-dialog-actions {
      display: flex;
      justify-content: center;
      gap: 16px;
      margin-top: 24px;
    }
    .soft-delete-dialog-actions .cancel-btn {
      min-width: 110px;
      font-weight: 600;
      border-color: #bdbdbd;
      color: #333;
    }
    .soft-delete-dialog-actions .ok-btn {
      min-width: 110px;
      font-weight: 600;
    }
  `]
})
export class NewsSoftDeleteDialogComponent {
  constructor(
    public dialogRef: MatDialogRef<NewsSoftDeleteDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) {}

  onCancel(): void {
    this.dialogRef.close(false);
  }

  onConfirm(): void {
    this.dialogRef.close(true);
  }
}
