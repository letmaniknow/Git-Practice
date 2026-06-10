/**
 * News Category List Page Component - Main Container/Orchestration
 * Purpose: Main page for news category management (CRUD operations)
 * Features: Load list, open create/edit/delete dialogs, manage state
 * Pattern: Follows admin-user-list-page.component.ts structure
 */

import {
  Component,
  OnInit,
  OnDestroy,
  ChangeDetectionStrategy,
  ViewChild,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDialogModule, MatDialog, MatDialogRef } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatCardModule } from '@angular/material/card';
import { MatTooltipModule } from '@angular/material/tooltip';
import { PageEvent } from '@angular/material/paginator';
import { Sort } from '@angular/material/sort';

import { Subject, Observable } from 'rxjs';
import { takeUntil, tap } from 'rxjs/operators';

import { NewsCategoryService } from '../services/news-category.service';
import { NewsCategoryListComponent } from '../components/news-category-list.component';
import { NewsCategoryFormDialogComponent } from '../components/news-category-form-dialog.component';
import { NewsCategoryDeleteDialogComponent } from '../components/news-category-delete-dialog.component';

import {
  NewsCategory,
  NewsCategoryRequestDto,
  NewsCategoryViewModel,
  DialogResult,
  NewsCategoryPaginationParams,
} from '../models/news-category.model';

import {
  NEWS_CATEGORY_OPERATION_MESSAGES,
  NEWS_CATEGORY_SNACKBAR_CONFIG,
  NEWS_CATEGORY_DIALOG_CONFIG,
  NEWS_CATEGORY_PAGINATION,
} from '../constants/news-category-api.constant';

/**
 * NewsCategoryListPageComponent - Main page orchestrator
 * Manages all CRUD operations, dialogs, and state updates
 */
@Component({
  selector: 'app-news-category-list-page',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatIconModule,
    MatDialogModule,
    MatSnackBarModule,
    MatProgressSpinnerModule,
    MatCardModule,
    MatTooltipModule,
    NewsCategoryListComponent,
  ],
  templateUrl: './news-category-list-page.component.html',
  styleUrls: ['./news-category-list-page.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class NewsCategoryListPageComponent implements OnInit, OnDestroy {
  /**
   * Cleanup subject for unsubscribe pattern
   */
  private destroy$ = new Subject<void>();

  /**
   * Observable streams from service (exposed to template with $ suffix)
   * Initialized in ngOnInit() to avoid initialization order issues
   */
  categories$!: Observable<NewsCategory[]>;
  loading$!: Observable<boolean>;
  error$!: Observable<string | null>;
  totalCount$!: Observable<number>;
  currentPage$!: Observable<number>;

  /**
   * Toggle between active and deleted categories view
   */
  showDeletedCategories = false;

  /**
   * Current sort state (used to synchronize with MatSort in child component)
   */
  currentSortState: { field: string; direction: 'asc' | 'desc' } | null = null;

  /**
   * Current pagination state
   */
  private currentPaginationParams: NewsCategoryPaginationParams = {
    page: NEWS_CATEGORY_PAGINATION.DEFAULT_PAGE,
    pageSize: NEWS_CATEGORY_PAGINATION.DEFAULT_PAGE_SIZE,
    sortBy: NEWS_CATEGORY_PAGINATION.DEFAULT_SORT_BY,
    sortDirection: 'desc',
  };

  /**
   * Store current dialog reference for error handling
   */
  private currentDialogRef: MatDialogRef<NewsCategoryFormDialogComponent> | null = null;

  /**
   * Operation messages
   */
  operationMessages = NEWS_CATEGORY_OPERATION_MESSAGES;

  constructor(
    private service: NewsCategoryService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {}

  /**
   * Component initialization
   */
  ngOnInit(): void {
    // Initialize observables from service
    this.categories$ = this.service.categories$;
    this.loading$ = this.service.loading$;
    this.error$ = this.service.error$;
    this.totalCount$ = this.service.totalCount$;
    this.currentPage$ = this.service.currentPage$;

    // Initialize sort state from pagination params with fallback defaults
    this.currentSortState = {
      field: this.currentPaginationParams.sortBy ?? NEWS_CATEGORY_PAGINATION.DEFAULT_SORT_BY,
      direction: (this.currentPaginationParams.sortDirection ?? 'desc') as 'asc' | 'desc'
    };

    this.loadCategories();
  }

  /**
   * Component cleanup
   */
  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  /**
   * ========== LOAD/REFRESH OPERATIONS ==========
   */

  /**
   * Load categories with current pagination params for management view
   * Includes deleted categories in view if showDeletedCategories is true
   */
  private loadCategories(): void {
    this.service
      .loadCategoriesForManagement({
        ...this.currentPaginationParams,
        includeDeleted: this.showDeletedCategories,
      })
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          // Success - state updated automatically
        },
        error: (error) => {
          this.showErrorSnackbar(
            this.operationMessages.LOAD.ERROR || 'Failed to load categories'
          );
        },
      });
  }

  /**
   * Toggle between active and deleted categories view
   */
  toggleDeletedView(): void {
    this.showDeletedCategories = !this.showDeletedCategories;
    this.currentPaginationParams.page = NEWS_CATEGORY_PAGINATION.DEFAULT_PAGE; // Reset to first page
    this.loadCategories();
  }

  /**
   * Refresh categories after CRUD operation
   * Includes delay to allow backend cache eviction
   */
  private refreshCategoriesList(): void {
    setTimeout(() => {
      this.loadCategories();
    }, 500);
  }

  /**
   * ========== DIALOG OPERATIONS ==========
   */

  /**
   * Open create category dialog
   */
  openCreateCategoryDialog(): void {
    const dialogRef = this.dialog.open(NewsCategoryFormDialogComponent, {
      ...NEWS_CATEGORY_DIALOG_CONFIG.FORM_DIALOG,
      disableClose: false,
    });

    this.currentDialogRef = dialogRef;

    dialogRef
      .afterClosed()
      .pipe(takeUntil(this.destroy$))
      .subscribe((result: DialogResult) => {
        if (result && result.mode === 'create' && result.success) {
          this.handleCreateCategory(
            result.data as NewsCategoryViewModel,
            dialogRef
          );
        }
        this.currentDialogRef = null;
      });
  }

  /**
   * Open edit category dialog
   * @param category - Category to edit
   */
  openEditCategoryDialog(category: NewsCategory): void {
    const viewModel: NewsCategoryViewModel = {
      id: category.newsCategoriesId,
      nameEn: category.newsCategoriesNameEn,
      nameEs: category.newsCategoriesNameEs,
      slug: category.newsCategoriesSlug,
      description: category.newsCategoriesDescription,
    };

    const dialogRef = this.dialog.open(NewsCategoryFormDialogComponent, {
      ...NEWS_CATEGORY_DIALOG_CONFIG.FORM_DIALOG,
      data: viewModel,
      disableClose: false,
    });

    this.currentDialogRef = dialogRef;

    dialogRef
      .afterClosed()
      .pipe(takeUntil(this.destroy$))
      .subscribe((result: DialogResult) => {
        if (result && result.mode === 'edit' && result.success) {
          this.handleUpdateCategory(
            category.newsCategoriesId,
            result.data as NewsCategoryViewModel,
            dialogRef
          );
        }
        this.currentDialogRef = null;
      });
  }

  /**
   * Open delete confirmation dialog
   * @param category - Category to delete
   */
  openDeleteCategoryDialog(category: NewsCategory): void {
    const dialogRef = this.dialog.open(NewsCategoryDeleteDialogComponent, {
      ...NEWS_CATEGORY_DIALOG_CONFIG.DELETE_DIALOG,
      data: category,
    });

    dialogRef
      .afterClosed()
      .pipe(takeUntil(this.destroy$))
      .subscribe((result: DialogResult) => {
        if (result && result.mode === 'delete' && result.success) {
          this.handleDeleteCategory(category.newsCategoriesId);
        }
      });
  }

  /**
   * ========== CRUD HANDLERS ==========
   */

  /**
   * Handle create category submission
   * @param viewModel - Form data
   * @param dialogRef - Dialog reference for error handling
   */
  private handleCreateCategory(
    viewModel: NewsCategoryViewModel,
    dialogRef: MatDialogRef<NewsCategoryFormDialogComponent>
  ): void {
    const request: NewsCategoryRequestDto = {
      categoryNameEn: viewModel.nameEn,
      categoryNameEs: viewModel.nameEs,
      slug: viewModel.slug,
      categoryDescription: viewModel.description,
    };

    this.service
      .createCategory(request)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.showSuccessSnackbar(
            this.operationMessages.CREATE.SUCCESS
          );
          dialogRef.close();
          this.refreshCategoriesList();
        },
        error: (error) => {
          console.error('Failed to create category:', error);
          this.handleBackendDuplicateError(error, dialogRef);
        },
      });
  }

  /**
   * Handle update category submission
   * @param id - Category ID
   * @param viewModel - Updated form data
   * @param dialogRef - Dialog reference for error handling
   */
  private handleUpdateCategory(
    id: string,
    viewModel: NewsCategoryViewModel,
    dialogRef: MatDialogRef<NewsCategoryFormDialogComponent>
  ): void {
    const request: NewsCategoryRequestDto = {
      categoryNameEn: viewModel.nameEn,
      categoryNameEs: viewModel.nameEs,
      slug: viewModel.slug,
      categoryDescription: viewModel.description,
    };

    this.service
      .updateCategory(id, request)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.showSuccessSnackbar(
            this.operationMessages.EDIT.SUCCESS
          );
          dialogRef.close();
          this.refreshCategoriesList();
        },
        error: (error) => {
          console.error('Failed to update category:', error);
          this.handleBackendDuplicateError(error, dialogRef);
        },
      });
  }

  /**
   * Handle backend duplicate/constraint errors
   * Extracts field and error message, displays in form
   * @param error - Error response from backend
   * @param dialogRef - Dialog reference to update form
   */
  private handleBackendDuplicateError(
    error: any,
    dialogRef: MatDialogRef<NewsCategoryFormDialogComponent>
  ): void {
    let errorMessage = 'An error occurred while saving. Please try again.';
    let fieldName: string | null = null;

    // Check for constraint violation error
    if (error?.error?.message) {
      const msg = error.error.message.toLowerCase();

      // Extract field name from constraint error message
      if (msg.includes('newscategoriesnameen')) {
        fieldName = 'nameEn';
        errorMessage = 'This English name already exists. Please try another name.';
      } else if (msg.includes('newscategoriesnamese')) {
        fieldName = 'nameEs';
        errorMessage = 'This Spanish name already exists. Please try another name.';
      } else if (msg.includes('newscategoriesslug')) {
        fieldName = 'slug';
        errorMessage = 'This slug already exists. Please try another slug.';
      } else if (msg.includes('unique') || msg.includes('duplicate')) {
        // Generic duplicate error - try to infer field
        if (msg.includes('name')) {
          fieldName = 'nameEn';
          errorMessage = 'This category name already exists. Please try another name.';
        }
      }
    }

    // Get component instance and set error
    const componentInstance = dialogRef.componentInstance;
    if (fieldName && componentInstance) {
      componentInstance.setFieldBackendError(fieldName, errorMessage);
    } else {
      // Show general error if field not determined
      this.showErrorSnackbar(errorMessage);
    }
  }

  /**
   * Handle delete category submission
   * @param id - Category ID
   */
  private handleDeleteCategory(id: string): void {
    this.service
      .deleteCategory(id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.showSuccessSnackbar(
            this.operationMessages.DELETE.SUCCESS
          );
          this.refreshCategoriesList();
        },
        error: (error) => {
          console.error('Failed to delete category:', error);
          this.showErrorSnackbar(this.operationMessages.DELETE.ERROR);
        },
      });
  }

  /**
   * ========== TABLE EVENT HANDLERS ==========
   */

  /**
   * Handle pagination change
   * @param event - PageEvent from MatPaginator
   */
  onPageChanged(event: PageEvent): void {
    this.currentPaginationParams.page = event.pageIndex;
    this.currentPaginationParams.pageSize = event.pageSize;
    this.loadCategories();
  }

  /**
   * Handle sort change
   * @param event - Sort event from MatSort
   */
  onSortChanged(event: Sort): void {
    if (!event.direction) {
      return; // Don't reload if sort is cleared
    }

    // Update pagination params
    this.currentPaginationParams.sortBy = event.active;
    this.currentPaginationParams.sortDirection = event.direction as 'asc' | 'desc';

    // Update sort state for template binding
    this.currentSortState = {
      field: event.active,
      direction: event.direction as 'asc' | 'desc'
    };

    this.currentPaginationParams.page = 0; // Reset to first page
    this.loadCategories();
  }

  /**
   * Handle edit category event from list component
   * @param category - Category to edit
   */
  onEditCategory(category: NewsCategory): void {
    this.openEditCategoryDialog(category);
  }

  /**
   * Handle delete category event from list component
   * @param category - Category to delete
   */
  onDeleteCategory(category: NewsCategory): void {
    this.openDeleteCategoryDialog(category);
  }

  /**
   * Handle activate category event from list component
   * @param categoryId - Category ID to activate
   */
  onActivateCategory(categoryId: string): void {
    this.service
      .activateCategory(categoryId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.showSuccessSnackbar('Category activated successfully');
          this.refreshCategoriesList();
        },
        error: (error) => {
          console.error('Failed to activate category:', error);
          this.showErrorSnackbar('Failed to activate category');
        },
      });
  }

  /**
   * Handle deactivate category event from list component
   * @param categoryId - Category ID to deactivate
   */
  onDeactivateCategory(categoryId: string): void {
    this.service
      .deactivateCategory(categoryId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.showSuccessSnackbar('Category deactivated successfully');
          this.refreshCategoriesList();
        },
        error: (error) => {
          console.error('Failed to deactivate category:', error);
          this.showErrorSnackbar('Failed to deactivate category');
        },
      });
  }

  /**
   * Handle restore category event from list component
   * @param categoryId - Category ID to restore
   */
  onRestoreCategory(categoryId: string): void {
    this.service
      .restoreCategory(categoryId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.showSuccessSnackbar('Category restored successfully');
          this.refreshCategoriesList();
        },
        error: (error) => {
          console.error('Failed to restore category:', error);
          this.showErrorSnackbar('Failed to restore category');
        },
      });
  }

  /**
   * Handle view audit logs event from list component
   * @param categoryId - Category ID to view logs for
   */
  onViewAuditLogs(categoryId: string): void {
    // TODO: Open audit logs dialog or navigate to audit logs page
    console.log('View audit logs for category:', categoryId);
  }

  /**
   * ========== UI FEEDBACK ==========
   */

  /**
   * Show success snackbar message
   * @param message - Message to display
   */
  private showSuccessSnackbar(message: string): void {
    this.snackBar.open(message, 'Close', {
      ...NEWS_CATEGORY_SNACKBAR_CONFIG,
      panelClass: ['success-snackbar'],
    });
  }

  /**
   * Show error snackbar message
   * @param message - Message to display
   */
  private showErrorSnackbar(message: string): void {
    this.snackBar.open(message, 'Close', {
      ...NEWS_CATEGORY_SNACKBAR_CONFIG,
      panelClass: ['error-snackbar'],
    });
  }
}
