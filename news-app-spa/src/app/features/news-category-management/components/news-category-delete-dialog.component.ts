/**
 * News Category Delete Dialog Component - Soft-Delete Confirmation
 * Purpose: Confirm deletion of news category before sending delete request
 * Features: Display category details, cancel/confirm buttons
 * Pattern: Follows admin-user-delete-dialog.component.ts structure
 */

import { Component, ChangeDetectionStrategy, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatDialogModule, MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { NewsCategory, DialogResult } from '../models/news-category.model';
import { NEWS_CATEGORY_OPERATION_MESSAGES } from '../constants/news-category-api.constant';

/**
 * NewsCategoryDeleteDialogComponent - Confirm soft-delete
 * Injected data: category (NewsCategory object to delete)
 */
@Component({
  selector: 'app-news-category-delete-dialog',
  standalone: true,
  imports: [
    CommonModule,
    MatDialogModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './news-category-delete-dialog.component.html',
  styleUrls: ['./news-category-delete-dialog.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class NewsCategoryDeleteDialogComponent {
  /**
   * Loading state during deletion
   */
  isLoading = false;

  /**
   * Operation messages configuration
   */
  operationMessages = NEWS_CATEGORY_OPERATION_MESSAGES;

  constructor(
    private dialogRef: MatDialogRef<NewsCategoryDeleteDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public category: NewsCategory
  ) {}

  /**
   * Handle confirm delete button click
   * Emits deletion request and closes dialog
   */
  onConfirmDelete(): void {
    this.isLoading = true;

    // Prepare result
    const result: DialogResult = {
      mode: 'delete',
      success: true,
      data: this.category,
    };

    // Small delay to simulate network latency
    setTimeout(() => {
      this.dialogRef.close(result);
    }, 300);
  }

  /**
   * Handle cancel button click
   * Closes dialog without deleting
   */
  onCancel(): void {
    this.dialogRef.close({ mode: 'delete', success: false });
  }

  /**
   * Format date for display
   * @param dateString - ISO date string
   */
  formatDate(dateString: string): string {
    if (!dateString) return '-';
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  }

  /**
   * Truncate long description
   * @param text - Text to truncate
   * @param maxLength - Max length
   */
  truncateText(text: string | undefined, maxLength: number = 100): string {
    if (!text) return '-';
    return text.length > maxLength ? text.substring(0, maxLength) + '...' : text;
  }
}
