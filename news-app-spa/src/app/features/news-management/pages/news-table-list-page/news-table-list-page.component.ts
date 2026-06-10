import { Component, OnInit, OnDestroy, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatTooltipModule } from '@angular/material/tooltip';
import { FormsModule } from '@angular/forms';
import { Subject, takeUntil } from 'rxjs';
import { NewsListTableComponent } from '../../components/news-list-table/index';
import { NewsSearchFiltersComponent, INewsSearchFiltersState, INewsSearchFiltersChangedEvent } from '../../components/news-search-filters/news-search-filters.component';
import { NewsFormService } from '../../services/news-form.service';
import { NewsCategory } from '../../models/news-form.model';
import { AppMasterDataService, NewsCategory as MasterDataCategory } from '../../../../core/services/app-master-data.service';

/**
 * NEWS MANAGEMENT LIST PAGE COMPONENT
 * 
 * Route: /news/admin
 * Purpose: Admin-only news management and moderation interface
 * View Type: TABLE layout (optimized for admin operations)
 * Audience: Administrators/Moderators Only
 * 
 * Features:
 * - Display news in efficient table format
 * - Column customization and reordering
 * - Sortable columns (click headers)
 * - Pagination for faster data rendering
 * - Bulk operations toolbar:
 *   • Select All / Clear Selection
 *   • Soft Delete (recoverable)
 *   • Publish/Unpublish
 *   • Change Status
 * - Multi-language support (English/Spanish)
 * - Quick actions menu per row (Edit, Publish, Delete)
 * - Real-time filtering and search
 * 
 * Architecture:
 * - Container (smart) component orchestrating admin interface
 * - Delegates table display to NewsListTableComponent
 * - Passes language preference to child components
 * - Uses reactive patterns with RxJS
 * - OnPush change detection for performance
 * 
 * Admin Features Only: Bulk operations, moderation, status changes
 * NO Public Browsing: This is NOT for readers, only for administrators
 * 
 * @author MMVA Team
 * @version 1.1 - Data binding integration
 * @since 2026-04-14
 */
@Component({
  selector: 'app-news-table-list-page',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    RouterModule,
    MatIconModule,
    MatButtonModule,
    MatSlideToggleModule,
    MatTooltipModule,
    NewsListTableComponent,
    NewsSearchFiltersComponent
  ],
  templateUrl: './news-table-list-page.component.html',
  styleUrls: ['./news-table-list-page.component.css']
})
export class NewsTableListPageComponent implements OnInit, OnDestroy {
  
  @ViewChild(NewsListTableComponent) newsListTableComponent?: NewsListTableComponent;
  
  private destroy$ = new Subject<void>();

  // Language state
  currentLanguage: 'en' | 'es' = 'en';
  currentLanguageToggle = false;

  // Search & Filter State
  currentFiltersState: INewsSearchFiltersState = {
    searchTextInput: '',
    searchModeSelected: 'all',
    workflowStatusesSelected: [],
    categoryIdSelected: null,
    createdDateFrom: null,
    createdDateTo: null,
    createdByAdminUserIdSelected: null
  };

  availableCategories: NewsCategory[] = [];
  availableWorkflowStatuses: string[] = [];

  constructor(
    private newsFormService: NewsFormService,
    private appMasterDataService: AppMasterDataService
  ) {
    console.debug('[NewsTableListPageComponent] constructor called');
  }

  ngOnInit(): void {
    console.debug('[NewsTableListPageComponent] ngOnInit - initializing page');
    
    // Load workflow statuses and categories for search filters
    this.loadFilterData();
    
    // Load workflow statuses from backend API (only after user is authenticated)
    // This runs AFTER login, so no 401 errors
    // Uses newsFormService which has caching to prevent duplicate API calls
    this.newsFormService.getWorkflowStatuses()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (statuses) => {
          console.debug('[NewsTableListPageComponent] Workflow statuses loaded: %d items', statuses.length);
        },
        error: (error) => {
          console.warn('[NewsTableListPageComponent] Failed to load workflow statuses:', error);
          // Component continues to work even if statuses fail to load
          // (fallback handling is in the service)
        }
      });
  }

  ngOnDestroy(): void {
    console.debug('[NewsTableListPageComponent] ngOnDestroy - cleaning up');
    this.destroy$.next();
    this.destroy$.complete();
  }

  /**
   * Toggle between English and Spanish display language
   * 
   * Updates currentLanguage state which propagates to all child components
   * via Input bindings for content translation.
   */
  toggleLanguage(): void {
    this.currentLanguage = this.currentLanguage === 'en' ? 'es' : 'en';
    this.currentLanguageToggle = !this.currentLanguageToggle;
    console.debug('[NewsTableListPageComponent] Language switched to: %s', this.currentLanguage);
  }

  /**
   * Open column customization dialog
   * Delegates to the table component which manages column preferences
   */
  openColumnCustomization(): void {
    if (this.newsListTableComponent) {
      this.newsListTableComponent.openColumnCustomization();
    }
  }

  /**
   * Load filter data (categories and workflow statuses)
   * 
   * Called once during component initialization.
   * Stores results in availableCategories and availableWorkflowStatuses
   * for the NewsSearchFiltersComponent to populate dropdowns.
   * 
   * Uses getAllCategories() to include ALL categories (active + soft-deleted)
   * so users can filter by historical categories that may no longer be active.
   */
  private loadFilterData(): void {
    console.debug('[NewsTableListPageComponent] loadFilterData() - loading categories and workflow statuses');

    // Load ALL categories (including soft-deleted) for filters
    this.appMasterDataService.getAllCategories()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (categories: MasterDataCategory[]) => {
          console.debug('[NewsTableListPageComponent] Categories loaded: %d items', categories.length);
          this.availableCategories = categories as any;
        },
        error: (error) => {
          console.error('[NewsTableListPageComponent] Error loading categories:', error);
          this.availableCategories = [];
        }
      });

    // Load workflow statuses
    this.newsFormService.getWorkflowStatuses()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (statuses: string[]) => {
          console.debug('[NewsTableListPageComponent] Workflow statuses loaded: %d items', statuses.length);
          this.availableWorkflowStatuses = statuses;
        },
        error: (error) => {
          console.error('[NewsTableListPageComponent] Error loading workflow statuses:', error);
          this.availableWorkflowStatuses = [];
        }
      });
  }

  /**
   * Handle filter changes from NewsSearchFiltersComponent
   * 
   * Called when user changes any search or filter criteria.
   * Updates current filter state and passes it to the table component.
   * 
   * @param event - Filter change event containing updated filter state and action type
   */
  onFiltersChanged(event: INewsSearchFiltersChangedEvent): void {
    console.debug('[NewsTableListPageComponent] onFiltersChanged() - changedFilterType=%s',
      event.changedFilterType);

    // Update filter state
    this.currentFiltersState = event.updatedFiltersState;

    // Delegate to table component to handle the filter change
    if (this.newsListTableComponent) {
      this.newsListTableComponent.onFiltersChanged(event);
    }
  }
}
