import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { Router } from '@angular/router';

import { CommonModule } from '@angular/common';
@Component({
  selector: 'app-news-cloned-dialog',
  standalone: true,
  imports: [CommonModule, MatDialogModule, MatButtonModule, MatIconModule],
  template: `
    <div class="news-cloned-dialog-content">
      <mat-icon class="news-cloned-dialog-icon">check_circle</mat-icon>
      <h2 class="news-cloned-dialog-title">News Cloned!</h2>
      <div class="news-cloned-dialog-news-title">{{ data.newsTitle }}</div>
      <div class="news-cloned-dialog-message">
        The news item was cloned successfully.<br>Would you like to edit it now?
      </div>
      <div *ngIf="errorMessage" class="news-cloned-dialog-error">
        <mat-icon style="vertical-align: middle;">error</mat-icon>
        {{ errorMessage }}
      </div>
      <div class="news-cloned-dialog-actions">
        <button mat-flat-button color="warn" (click)="onClose()">Cancel</button>
        <button mat-flat-button color="primary" (click)="onEdit()" [disabled]="isNavigating">
          <mat-icon style="vertical-align: middle; margin-right: 4px;">edit</mat-icon>
          Edit Now
        </button>
      </div>
    </div>
  `
  ,
  styles: [`
    .news-cloned-dialog-content {
      text-align: center;
      padding: 32px 24px 20px 24px;
      max-width: 520px;
      min-width: 320px;
      box-sizing: border-box;
    }
    .news-cloned-dialog-icon {
      font-size: 56px;
      color: #43a047;
      margin-bottom: 12px;
    }
    .news-cloned-dialog-title {
      margin: 0 0 10px 0;
      font-weight: 700;
      color: #222;
      letter-spacing: 0.5px;
      font-size: 2rem;
      line-height: 1.2;
    }
    .news-cloned-dialog-news-title {
      color: #1976d2;
      font-size: 1.1rem;
      margin-bottom: 8px;
      font-weight: 500;
      word-break: break-word;
      white-space: normal;
      max-width: 100%;
      overflow-wrap: anywhere;
    }
    .news-cloned-dialog-message {
      color: #555;
      font-size: 15px;
      margin-bottom: 18px;
    }
    .news-cloned-dialog-error {
      color: #d32f2f;
      margin-bottom: 10px;
    }
    .news-cloned-dialog-actions {
      display: flex;
      justify-content: flex-end;
      gap: 16px;
      margin-top: 28px;
    }
    .news-cloned-dialog-actions button {
      min-width: 110px;
      font-weight: 600;
      font-size: 1rem;
    }
  `]
})
export class NewsClonedDialogComponent {
  isNavigating = false;
  errorMessage: string | null = null;

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: { newsId: string, newsTitle: string },
    private dialogRef: MatDialogRef<NewsClonedDialogComponent>,
    private router: Router
  ) {}

  onEdit() {
    this.isNavigating = true;
    this.errorMessage = null;
    this.router.navigate([`/news/${this.data.newsId}/edit`])
      .then(() => this.dialogRef.close())
      .catch(err => {
        this.errorMessage = 'Failed to navigate to edit page.';
        this.isNavigating = false;
      });
  }

  onClose() {
    this.dialogRef.close();
  }
}
