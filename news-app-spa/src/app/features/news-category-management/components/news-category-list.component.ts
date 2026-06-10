/**
 * News Category List Component - Material Table Display
 * Purpose: Display paginated list of news categories in Material table
 * Features: Sorting, pagination, action buttons (edit, delete)
 * Pattern: Follows admin-user-list.component.ts structure
 */

import {
  Component,
  Input,
  Output,
  EventEmitter,
  OnInit,
  OnChanges,
  AfterViewInit,
  SimpleChanges,
  ChangeDetectionStrategy,
  ViewChild,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSortModule, Sort, MatSort } from '@angular/material/sort';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatMenuModule } from '@angular/material/menu';
import { MatDividerModule } from '@angular/material/divider';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { NewsCategory } from '../models/news-category.model';
import {
  NEWS_CATEGORY_TABLE_COLUMNS,
  NEWS_CATEGORY_PAGINATION,
} from '../constants/news-category-api.constant';

/**
 * NewsCategoryListComponent - Table display for categories
 * Input: categories, loading, totalCount, pageSize
 * Output: edit, delete, pageChange, sortChange events
 */
@Component({
  selector: 'app-news-category-list',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule,
    MatPaginatorModule,
    MatSortModule,
    MatButtonModule,
    MatIconModule,
    MatTooltipModule,
    MatMenuModule,
    MatDividerModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './news-category-list.component.html',
  styleUrls: ['./news-category-list.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class NewsCategoryListComponent implements OnInit, AfterViewInit, OnChanges {
  /**
   * Input properties from parent component
   */
  @Input() categories: NewsCategory[] = [];
  @Input() isLoading = false;
  @Input() totalCount = 0;
  @Input() pageSize = NEWS_CATEGORY_PAGINATION.DEFAULT_PAGE_SIZE;
  @Input() currentPage = 0;
  @Input() currentSort: { field: string; direction: 'asc' | 'desc' } | null = null;

  /**
   * MatSort reference for sorting
   */
  @ViewChild(MatSort) sort!: MatSort;

  /**
   * MatTableDataSource for proper Material table integration
   */
  dataSource = new MatTableDataSource<NewsCategory>();

  /**
   * Output events to parent component
   */
  @Output() editCategory = new EventEmitter<NewsCategory>();
  @Output() deleteCategory = new EventEmitter<NewsCategory>();
  @Output() activateCategory = new EventEmitter<string>();
  @Output() deactivateCategory = new EventEmitter<string>();
  @Output() restoreCategory = new EventEmitter<string>();
  @Output() viewAuditLogs = new EventEmitter<string>();
  @Output() pageChanged = new EventEmitter<PageEvent>();
  @Output() sortChanged = new EventEmitter<Sort>();

  /**
   * Table configuration from constants
   */
  displayedColumns = NEWS_CATEGORY_TABLE_COLUMNS.DISPLAY_COLUMNS;
  pageSizeOptions = NEWS_CATEGORY_PAGINATION.PAGE_SIZE_OPTIONS;

  ngOnInit(): void {
    // Component initialized
  }

  ngAfterViewInit(): void {
    // Server-side sorting - MatSort manages its own state via user clicks
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['categories']) {
      this.dataSource.data = this.categories;
    }
  }

  /**
   * Handle pagination change event
   * @param event - MatPaginator PageEvent
   */
  onPageChange(event: PageEvent): void {
    this.pageChanged.emit(event);
  }

  /**
   * Handle sort change event
   * @param event - MatSort Sort event
   */
  onSortChange(event: Sort): void {
    this.sortChanged.emit(event);
  }

  /**
   * Emit edit event when edit button clicked
   * @param category - Category to edit
   */
  onEdit(category: NewsCategory): void {
    this.editCategory.emit(category);
  }

  /**
   * Emit delete event when delete button clicked
   * @param category - Category to delete
   */
  onDelete(category: NewsCategory): void {
    this.deleteCategory.emit(category);
  }

  /**
   * Emit activate event when activate button clicked
   * @param categoryId - Category ID to activate
   */
  onActivate(categoryId: string): void {
    this.activateCategory.emit(categoryId);
  }

  /**
   * Emit deactivate event when deactivate button clicked
   * @param categoryId - Category ID to deactivate
   */
  onDeactivate(categoryId: string): void {
    this.deactivateCategory.emit(categoryId);
  }

  /**
   * Emit restore event when restore button clicked
   * @param categoryId - Category ID to restore
   */
  onRestore(categoryId: string): void {
    this.restoreCategory.emit(categoryId);
  }

  /**
   * Emit viewAuditLogs event when audit logs button clicked
   * @param categoryId - Category ID to view logs for
   */
  onViewAuditLogs(categoryId: string): void {
    this.viewAuditLogs.emit(categoryId);
  }

  /**
   * Get display name for category (English or Spanish based on locale)
   * For now, shows English. Can be extended for i18n
   */
  getCategoryName(category: NewsCategory): string {
    return category.newsCategoriesNameEn;
  }

  /**
   * Truncate long description text
   * @param text - Text to truncate
   * @param maxLength - Max length before truncation
   */
  truncateText(text: string | undefined, maxLength: number = 50): string {
    if (!text) return '-';
    return text.length > maxLength ? text.substring(0, maxLength) + '...' : text;
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
    });
  }
}
