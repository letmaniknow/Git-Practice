import { Component, OnInit, OnDestroy, ChangeDetectorRef, HostListener, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatDialog } from '@angular/material/dialog';

import { Subject, takeUntil, debounceTime, distinctUntilChanged, switchMap, catchError, merge, of, tap, Observable, filter } from 'rxjs';
import { NewsFormService } from '../../services/news-form.service';
import { NewsAdvancedSearchService } from '../../services/news-advanced-search.service';
import { AppMasterDataService, NewsCategory as MasterDataCategory } from '../../../../core/services/app-master-data.service';
import { VideoControlService } from '../../services/video-control.service';
import { ColumnPreferencesService } from '../../services/column-preferences.service';
import { ColumnCustomizationDialogComponent } from '../../components/column-customization-dialog.component';
import { NewsItem } from '../../models/news-item.model';
import { NewsCategory } from '../../models/news-form.model';
import { NewsCardComponent } from '../../components/news-card/news-card.component';
import { NewsSearchFiltersComponent, INewsSearchFiltersState, INewsSearchFiltersChangedEvent } from '../../components/news-search-filters/news-search-filters.component';
import { ErrorAlertComponent } from '../../../../shared/components/error-alert/error-alert.component';
import { LoadingComponent } from '../../../../shared/components/loading/loading.component';
import { EnumDisplayPipe } from '../../../../shared/pipes/enum-display.pipe';
import { ERROR_MESSAGES } from '../../../../shared/constants/error.constants';

/**
 * NEWS CARD LIST PAGE COMPONENT
 * 
 * Route: /news
 * Purpose: Public-facing news browsing interface
 * View Type: CARD/GRID layout
 * Audience: General Users
 * 
 * Features:
 * - Display news articles in card/grid format
 * - Advanced search and filtering
 * - Full-text search (title, content)
 * - Filter by status, category, date range
 * - Multi-language support (English/Spanish)
 * - Pagination
 * - Column customization for power users
 * - Create new article button
 * 
 * Architecture:
 * - Container (smart) component managing data and state
 * - Uses NewsCardComponent for individual card display
 * - Communicates with NewsFormService for data
 * - RxJS reactive patterns for search/filter pipelines
 * - OnPush change detection for performance
 * 
 * NOT Admin Features: No bulk operations, no moderation tools
 * 
 * @author MMVA Team
 * @since 2026-04-14
 */
@Component({
  selector: 'app-news-browse-page',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    CommonModule, 
    RouterModule, 
    FormsModule,
    MatIconModule,
    MatButtonModule,
    MatSelectModule,
    MatFormFieldModule,
    MatSlideToggleModule,
    MatTooltipModule,
    NewsCardComponent,
    NewsSearchFiltersComponent,
    ErrorAlertComponent,
    LoadingComponent
  ],
  templateUrl: './news-browse-page.component.html',
  styleUrls: ['./news-browse-page.component.css']
})
export class NewsBrowsePageComponent implements OnInit, OnDestroy {
  // ============================================================================
  // CONFIGURATION CONSTANTS - Single Source of Truth
  // ============================================================================
  private readonly DEFAULT_PAGE_SIZE = 10;
  private readonly PAGE_SIZE_OPTIONS = [5, 10, 20, 50, 100];
  private readonly MAX_VISIBLE_PAGINATION_PAGES = 5;
  private readonly EXCERPT_LENGTH_LIMIT = 200;
  private readonly SEARCH_CACHE_MAX_SIZE = 50;
  private readonly DEBOUNCE_TIME_MS = 150;
  private readonly SCROLL_TRIGGER_OFFSET = 200;

  private destroy$ = new Subject<void>();
  private mediaUrlCache = new Map<string, string>();
  private blobUrls = new Map<string, string>(); // Store blob URLs for cleanup
  private searchTerm$ = new Subject<string>(); // Debounced search subject
  private filterChange$ = new Subject<void>(); // Filter change subject for debouncing
  private lastExecutedFilterState: any = null; // Track finalized filter state for deduplication
  
  // Professional Search: Query Result Caching
  private searchRequestCache = new Map<string, any>(); // Cache query results
  private lastExecutedQuery: string | null = null; // Track last executed query
  
  // Expose Math to template
  Math = Math;
  
  newsList: NewsItem[] = [];
  filteredNewsList: NewsItem[] = [];
  isLoading = true;
  error: string | null = null;
  deletingNewsId: string | null = null; // Track which news item is being deleted
  togglingPublishId: string | null = null; // Track which news item is being published/unpublished
  
  // Pagination properties
  currentPage = 1;
  pageSize = this.DEFAULT_PAGE_SIZE;
  totalElements = 0;
  totalPages = 0;
  pageSizeOptions = this.PAGE_SIZE_OPTIONS;
  
  // Language toggle
  currentLanguage: 'en' | 'es' = 'en';
  get currentLanguageToggle(): boolean {
    return this.currentLanguage === 'es';
  }
  set currentLanguageToggle(value: boolean) {
    this.currentLanguage = value ? 'es' : 'en';
  }
  
  get langKey(): string {
    return this.currentLanguage === 'es' ? 'categoryName_es' : 'categoryName_en';
  }
  
  showFloatingToggle = false;
  cardLanguages: Map<string, 'en' | 'es'> = new Map();
  
  // ============================================================================
  // Categories & Statuses - Data Loading
  // ============================================================================
  categories: NewsCategory[] = [];
  categoriesMap: Map<string, NewsCategory> = new Map();
  workflowStatusOptions: string[] = [];
  
  // Loading & Error States (Industry Standard)
  isLoadingCategories = false;
  isLoadingWorkflowStatuses = false;
  categoriesError: string | null = null;
  workflowStatusesError: string | null = null;
  
  // ============================================================================
  // Filter State (maintained by NewsSearchFiltersComponent)
  // ============================================================================
  currentFiltersState: INewsSearchFiltersState = {
    searchTextInput: '',
    searchModeSelected: 'all',
    workflowStatusesSelected: [],
    categoryIdSelected: null,
    createdDateFrom: null,
    createdDateTo: null,
    createdByAdminUserIdSelected: null
  };

  // Admin users for search-filters component dropdown are managed internally
  // by NewsSearchFiltersComponent — no parent-level properties needed.

  // Backward compatibility getters (for existing code & template)
  get searchTerm(): string {
    return this.currentFiltersState.searchTextInput;
  }
  get searchMode(): 'all' | 'title' | 'content' {
    return this.currentFiltersState.searchModeSelected;
  }
  get filterByWorkflowStatus(): string[] {
    return this.currentFiltersState.workflowStatusesSelected;
  }
  get selectedCategoryId(): string | null {
    return this.currentFiltersState.categoryIdSelected;
  }
  get fromDate(): Date | null {
    return this.currentFiltersState.createdDateFrom;
  }
  get toDate(): Date | null {
    return this.currentFiltersState.createdDateTo;
  }
  get selectedCreatedBy(): string | null {
    return this.currentFiltersState.createdByAdminUserIdSelected;
  }
  
  get activeFilterCount(): number {
    let count = 0;
    if (this.currentFiltersState.workflowStatusesSelected.length > 0) count += this.currentFiltersState.workflowStatusesSelected.length;
    if (this.currentFiltersState.categoryIdSelected) count += 1;
    if (this.currentFiltersState.createdDateFrom) count += 1;
    if (this.currentFiltersState.createdDateTo) count += 1;
    if (this.currentFiltersState.createdByAdminUserIdSelected) count += 1;
    return count;
  }

  constructor(
    private newsService: NewsFormService,
    private adminSearchService: NewsAdvancedSearchService,
    private router: Router,
    private cdr: ChangeDetectorRef,
    private videoControlService: VideoControlService,
    private dialog: MatDialog,
    private columnPreferencesService: ColumnPreferencesService,
    private appMasterDataService: AppMasterDataService
  ) {}

  ngOnInit() {
    // Initialize data for search-filters component
    this.loadCategories();
    this.loadWorkflowStatuses();

    // Setup search pipeline that triggers on filter changes
    // The NewsSearchFiltersComponent emits changes, we listen and search
    this.filterChange$
      .pipe(
        debounceTime(this.DEBOUNCE_TIME_MS),
        tap(() => this.currentPage = 1),
        // Prevent duplicate query execution: Compare FINALIZED state values
        // (not raw currentFiltersState which may have empty arrays vs null)
        filter(() => {
          // FIX: Capture FINALIZED state (same values used for API calls)
          // This ensures the deduplication comparison matches actual query changes
          const finalizedState = {
            text: this.searchTerm.trim() || null,
            mode: this.searchMode,
            statuses: this.filterByWorkflowStatus.length > 0 ? [...this.filterByWorkflowStatus] : null,
            categoryId: this.selectedCategoryId || null,
            createdBy: this.selectedCreatedBy || null,
            fromDate: this.fromDate?.getTime() || null,
            toDate: this.toDate?.getTime() || null,
            page: this.currentPage
          };
          
          const stateKey = JSON.stringify(finalizedState);
          const lastKey = this.lastExecutedFilterState ? JSON.stringify(this.lastExecutedFilterState) : null;
          const isDifferent = stateKey !== lastKey;
          
          if (isDifferent) {
            this.lastExecutedFilterState = finalizedState;
            console.debug('[NewsCardListPageComponent] Filter state changed, executing search', finalizedState);
          } else {
            console.debug('[NewsCardListPageComponent] Filter state unchanged, skipping search', finalizedState);
          }
          return isDifferent;
        }),
        switchMap(() => this.performSearch()),
        catchError((error: any) => {
          console.error('[Search] Error occurred:', error);
          this.error = error?.message || 'Search failed';
          this.isLoading = false;
          this.cdr.markForCheck();
          return of(null);
        }),
        takeUntil(this.destroy$)
      )
      .subscribe();

    // Initial load
    this.loadNews();
  }

  /**
   * Called by NewsSearchFiltersComponent when any filter changes
   * Updates the filter state and triggers a new search
   */
  onFiltersChanged(event: INewsSearchFiltersChangedEvent): void {
    this.currentFiltersState = event.updatedFiltersState;
    // Reset to first page when filters change (getters automatically use currentFiltersState)
    this.currentPage = 1;
    console.debug('[NewsCardListPageComponent] onFiltersChanged() - Clearing search cache (size=%d)', 
      this.searchRequestCache.size);
    this.searchRequestCache.clear(); // Clear cache when filters change to ensure fresh API calls
    this.filterChange$.next();
  }

  /**
   * Reset all filters to empty state and reload news
   * Called from template's "Clear Filters" button in empty-state
   */
  resetAllFilters(): void {
    this.currentFiltersState = {
      searchTextInput: '',
      searchModeSelected: 'all',
      workflowStatusesSelected: [],
      categoryIdSelected: null,
      createdDateFrom: null,
      createdDateTo: null,
      createdByAdminUserIdSelected: null
    };
    this.currentPage = 1;
    this.searchRequestCache.clear();
    this.loadNews(); // Reload all news without filters
  }

  @HostListener('window:scroll')
  onWindowScroll(): void {
    const shouldShow = window.scrollY > this.SCROLL_TRIGGER_OFFSET;
    if (this.showFloatingToggle !== shouldShow) {
      this.showFloatingToggle = shouldShow;
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    
    // Clear URL cache and revoke blob URLs to prevent memory leaks
    this.mediaUrlCache.clear();
    this.blobUrls.forEach(url => URL.revokeObjectURL(url));
    this.blobUrls.clear();
  }

  loadCategories(): void {
    this.isLoadingCategories = true;
    this.categoriesError = null;
    
    // Load ALL categories (including soft-deleted) for filters
    // Users should be able to filter by historical categories
    this.appMasterDataService.getAllCategories().subscribe({
      next: (categories: MasterDataCategory[]) => {
        this.categories = categories as any;
        this.categoriesMap.clear();
        (categories as any[]).forEach((cat: any) => this.categoriesMap.set(cat.id, cat));
        this.categoriesError = null;
        this.isLoadingCategories = false;
        this.cdr.markForCheck();
      },
      error: (error: any) => {
        console.error('Error loading categories:', error);
        this.categories = [];
        this.categoriesMap.clear();
        this.categoriesError = ERROR_MESSAGES.NEWS_FILTERS.LOAD_CATEGORIES;
        this.isLoadingCategories = false;
        this.cdr.markForCheck();
      }
    });
  }

  loadWorkflowStatuses(): void {
    this.isLoadingWorkflowStatuses = true;
    this.workflowStatusesError = null;
    
    this.newsService.getWorkflowStatuses().subscribe({
      next: (statuses: string[]) => {
        this.workflowStatusOptions = statuses;
        this.workflowStatusesError = null;
        this.isLoadingWorkflowStatuses = false;
        this.cdr.markForCheck();
      },
      error: (error: any) => {
        console.error('Error loading workflow statuses:', error);
        this.workflowStatusOptions = [];
        this.workflowStatusesError = ERROR_MESSAGES.NEWS_FILTERS.LOAD_WORKFLOW_STATUSES;
        this.isLoadingWorkflowStatuses = false;
        this.cdr.markForCheck();
      }
    });
  }

  getCategoryName(categoryId: string): string {
    const category = this.categoriesMap.get(categoryId);
    if (!category) {
      return categoryId; // Fallback to ID if category not found
    }
    return this.currentLanguage === 'en' ? category.categoryNameEn : category.categoryNameEs;
  }

  loadNews() {
    this.isLoading = true;
    this.error = null;
    
    // Clear media URL cache when reloading news
    this.mediaUrlCache.clear();
    // Clear blob URLs
    this.blobUrls.forEach(url => URL.revokeObjectURL(url));
    this.blobUrls.clear();
    
    this.newsService.getAllNews(this.currentPage, this.pageSize).subscribe({
      next: (data: any) => {
        this.newsList = data.content || [];
        this.totalElements = data.totalElements;
        this.totalPages = data.totalPages;

        // Sort by latest news first (newest createdAt first)
        this.sortNewsByLatest();
        this.filteredNewsList = [...this.newsList];
        this.isLoading = false;
        this.cdr.markForCheck(); // Trigger change detection with OnPush

        // Trigger search pipeline if ANY filters/search are active
        if (this.hasActiveFilters()) {
          this.filterChange$.next();
        }
      },
      error: (error: any) => {
        console.error('Error loading news:', error);
        this.error = error.message;
        this.isLoading = false;
        this.cdr.markForCheck(); // Trigger change detection on error
      }
    });
  }

  // ========================================
  // Helper: Centralized Filter Detection
  // ========================================
  private hasActiveFilters(): boolean {
    const hasTextQuery = this.searchTerm?.trim().length > 0;
    const hasStatusFilter = this.filterByWorkflowStatus?.length > 0;
    const hasCategoryFilter = this.selectedCategoryId != null;
    const hasDateFilter = this.fromDate != null || this.toDate != null;
    const hasCreatedByFilter = this.selectedCreatedBy !== null;
    return hasTextQuery || hasStatusFilter || hasCategoryFilter || hasDateFilter || hasCreatedByFilter;
  }

  // Pagination methods
  onPageChange(page: number): void {
    if (page < 1 || page > this.totalPages) return;
    this.currentPage = page;
    
    // If ANY filters are active, trigger search pipeline
    if (this.hasActiveFilters()) {
      this.filterChange$.next();
    } else {
      this.loadNews();
    }
    
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  onPageSizeChange(event: any): void {
    // MatSelect emits MatSelectChange event with value property
    const selectedValue = event.value;
    this.pageSize = selectedValue;
    this.currentPage = 1; // Reset to first page
    // Use search pipeline to properly handle filters with new page size
    this.filterChange$.next();
  }

  getPageNumbers(): number[] {
    const pages: number[] = [];
    
    if (this.totalPages <= this.MAX_VISIBLE_PAGINATION_PAGES) {
      // Show all pages if total is less than max visible
      for (let i = 1; i <= this.totalPages; i++) {
        pages.push(i);
      }
    } else {
      // Show pages around current page
      const halfVisible = Math.floor(this.MAX_VISIBLE_PAGINATION_PAGES / 2);
      let startPage = Math.max(1, this.currentPage - halfVisible);
      let endPage = Math.min(this.totalPages, startPage + this.MAX_VISIBLE_PAGINATION_PAGES - 1);
      
      // Adjust start if we're near the end
      if (endPage - startPage < this.MAX_VISIBLE_PAGINATION_PAGES - 1) {
        startPage = Math.max(1, endPage - this.MAX_VISIBLE_PAGINATION_PAGES + 1);
      }
      
      for (let i = startPage; i <= endPage; i++) {
        pages.push(i);
      }
    }
    
    return pages;
  }

  goToFirstPage(): void {
    this.onPageChange(1);
  }

  goToLastPage(): void {
    this.onPageChange(this.totalPages);
  }

  goToPreviousPage(): void {
    this.onPageChange(this.currentPage - 1);
  }

  goToNextPage(): void {
    this.onPageChange(this.currentPage + 1);
  }

  // Language toggle methods
  toggleLanguage(): void {
    this.currentLanguage = this.currentLanguage === 'en' ? 'es' : 'en';
  }

  setLanguage(lang: 'en' | 'es'): void {
    this.currentLanguage = lang;
  }

  toggleCardLanguage(newsId: string): void {
    const current = this.cardLanguages.get(newsId) || this.currentLanguage;
    this.cardLanguages.set(newsId, current === 'en' ? 'es' : 'en');
  }

  setCardLanguage(newsId: string, lang: 'en' | 'es'): void {
    this.cardLanguages.set(newsId, lang);
  }

  getCardLanguage(newsId: string): 'en' | 'es' {
    return this.cardLanguages.get(newsId) || this.currentLanguage;
  }

  /**
   * Open column customization dialog
   * Allows users to show/hide columns and reorder them
   */
  openColumnCustomization(): void {
    const dialogRef = this.dialog.open(ColumnCustomizationDialogComponent, {
      width: '600px',
      maxHeight: '80vh',
      disableClose: false,
      data: {
        preferences: this.columnPreferencesService.getCurrentPreferences(),
      }
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result?.saved) {
        console.log('✅ Column preferences updated');
        this.cdr.markForCheck();
      }
    });
  }

  hasCustomLanguage(newsId: string): boolean {
    const cardLang = this.cardLanguages.get(newsId);
    return cardLang !== undefined && cardLang !== this.currentLanguage;
  }

  getTitle(news: NewsItem): string {
    const lang = this.getCardLanguage(news.newsNewsId);
    return lang === 'en' ? news.newsTitleEn : news.newsTitleEs;
  }

  getContent(news: NewsItem): string {
    const lang = this.getCardLanguage(news.newsNewsId);
    return lang === 'en' ? news.newsContentEn : news.newsContentEs;
  }

  viewNews(id: string) {
    this.router.navigate(['/news', id]);
  }

  editNews(id: string) {
    this.router.navigate(['/news', id, 'edit']);
  }

  createNews() {
    this.router.navigate(['/news/create']);
  }

  deleteNews(news: NewsItem) {
    const confirmed = confirm(
      `Delete News\n\nAre you sure you want to delete "${news.newsTitleEn}"?\n\nThis action cannot be undone and the news will be permanently removed.`
    );
    if (confirmed) {
      this.deletingNewsId = news.newsNewsId!;
      this.newsService.deleteNews(news.newsNewsId!).subscribe({
        next: () => {
          console.log('News deleted successfully');
          this.deletingNewsId = null;
          this.loadNews(); // Refresh the list
        },
        error: (error: any) => {
          console.error('Error deleting news:', error);
          this.deletingNewsId = null;
          alert('Failed to delete news\n\n' + error.message);
        }
      });
    }
  }

  // Card component event handlers
  onCardView(news: NewsItem) {
    this.viewNews(news.newsNewsId!);
  }

  onCardEdit(news: NewsItem) {
    this.editNews(news.newsNewsId!);
  }

  onCardDelete(news: NewsItem) {
    this.deleteNews(news);
  }

  onCardTogglePublish(news: NewsItem) {
    const isCurrentlyPublished = news.newsWorkflowStatus === 'PUBLISHED';
    const actionTitle = isCurrentlyPublished ? 'Unpublish' : 'Publish';
    const actionMessage = isCurrentlyPublished
      ? `Unpublish News\n\nAre you sure you want to unpublish "${news.newsTitleEn}"?\n\nThis news will be moved to DRAFT status.`
      : `Publish News\n\nAre you sure you want to publish "${news.newsTitleEn}"?\n\nThis news will be immediately available to readers.`;

    const confirmed = confirm(actionMessage);
    if (confirmed) {
      this.togglingPublishId = news.newsNewsId!;
      
      const publishOperation$ = isCurrentlyPublished
        ? this.newsService.unpublishNews(news.newsNewsId!)
        : this.newsService.publishNews(news.newsNewsId!);

      publishOperation$.subscribe({
        next: (updatedNews: NewsItem) => {
          console.log(`News ${actionTitle.toLowerCase()}ed successfully`);
          this.togglingPublishId = null;
          this.loadNews(); // Refresh the list to reflect status change
        },
        error: (error: any) => {
          console.error(`Error ${actionTitle.toLowerCase()}ing news:`, error);
          this.togglingPublishId = null;
          alert(`Failed to ${actionTitle.toLowerCase()} news\n\n${error.message}`);
        }
      });
    }
  }

  // Debug method to test video URL directly
  testVideoUrl(filename: string): void {
    const url = this.getMediaUrl(filename);
    console.log('Testing video URL:', url);
    window.open(url, '_blank');
  }

  // Check if browser supports video format
  canPlayVideo(filename: string): boolean {
    const video = document.createElement('video');
    const mimeType = this.getVideoMimeType(filename);
    return video.canPlayType(mimeType) !== '';
  }

  isNewsBeingDeleted(newsId: string): boolean {
    return this.deletingNewsId === newsId;
  }

  isNewsBeingToggled(newsId: string): boolean {
    return this.togglingPublishId === newsId;
  }

  getExcerpt(content: string): string {
    return content.length > this.EXCERPT_LENGTH_LIMIT 
      ? content.substring(0, this.EXCERPT_LENGTH_LIMIT) + '...' 
      : content;
  }

  formatDate(date: Date | string | undefined): string {
    if (!date) return 'Unknown date';
    
    try {
      const dateObj = typeof date === 'string' ? new Date(date) : date;
      
      // Check if the date is valid
      if (isNaN(dateObj.getTime())) {
        return 'Invalid date';
      }
      
      // For news list - show date and time in compact format
      return dateObj.toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
        hour12: true
      });
    } catch (error) {
      console.error('Error formatting date:', error);
      return 'Invalid date';
    }
  }

  getCurrentDate(): string {
    return this.formatDate(new Date());
  }

  // Helper method to show relative time (optional)
  getRelativeTime(date: Date | string | undefined): string {
    if (!date) return 'Unknown time';
    
    try {
      const dateObj = typeof date === 'string' ? new Date(date) : date;
      const now = new Date();
      const diffMs = now.getTime() - dateObj.getTime();
      const diffMinutes = Math.floor(diffMs / (1000 * 60));
      const diffHours = Math.floor(diffMs / (1000 * 60 * 60));
      const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));

      if (diffMinutes < 1) return 'Just now';
      if (diffMinutes < 60) return `${diffMinutes}m ago`;
      if (diffHours < 24) return `${diffHours}h ago`;
      if (diffDays < 7) return `${diffDays}d ago`;
      
      // For older news, show full date
      return this.formatDate(date);
    } catch (error) {
      return this.formatDate(date);
    }
  }

  // Sort news by latest first (newest createdAt timestamp first)
  private sortNewsByLatest(): void {
    this.newsList.sort((a, b) => {
      // Get timestamps for comparison
      const dateA = this.getTimestamp(a.newsCreatedAt);
      const dateB = this.getTimestamp(b.newsCreatedAt);
      
      // Sort in descending order (latest first)
      return dateB - dateA;
    });
  }

  // Helper method to get timestamp from date
  private getTimestamp(date: Date | string | undefined): number {
    if (!date) return 0; // Put items without dates at the end
    
    try {
      const dateObj = typeof date === 'string' ? new Date(date) : date;
      return dateObj.getTime();
    } catch (error) {
      return 0; // Put invalid dates at the end
    }
  }

  trackByNewsId(index: number, news: NewsItem): string {
    return news.newsNewsId || index.toString();
  }

  // Search functionality - debounced
  /**
   * OBSERVABLE-BASED SEARCH ARCHITECTURE (CORRECTED)
   * 
   * CRITICAL FIX: Return the actual search Observable from buildSearchQuery()
   * NOT an Observable that completes with null
   * 
   * This allows switchMap to properly wait for search results and cancel if needed.
   */
  private performSearch() {
    // Return the actual search Observable so switchMap can manage it
    return this.buildSearchQuery();
  }

  /**
   * Build the search query Observable with proper caching and error handling
   * 
   * CRITICAL FIXES:
   * 1. Capture state immutably FIRST (avoid race conditions)
   * 2. Include pageSize in cache key (different page sizes = different results)
   * 3. Validate empty query handling WITH other filters
   * 4. Return actual search Observable (not wrapped)
   * 5. Add logging for debugging inconsistent results
   */
  private buildSearchQuery() {
    // STEP 1: CAPTURE STATE IMMUTABLY (fixes race conditions)
    // All state is frozen at this exact moment before any async operations
    const query = this.searchTerm.trim() || null;
    const statuses = this.filterByWorkflowStatus.length > 0 
      ? [...this.filterByWorkflowStatus] // Snapshot array
      : null;
    const categoryId = this.selectedCategoryId || null;
    const fromDate = this.fromDate ? new Date(this.fromDate) : null;
    const toDate = this.toDate ? new Date(this.toDate) : null;
    const createdBy = this.selectedCreatedBy || null; // NEW: Admin user UUID filter
    const currentPage = this.currentPage;
    const pageSize = this.pageSize;
    const searchMode = this.searchMode;
    
    // STEP 2: CHECK IF ANY FILTERS ARE ACTIVE (not just query)
    // Filters include: text query, dates, status, category, created by
    const hasTextQuery = query && query.trim().length > 0;
    const hasDateFilter = fromDate || toDate;
    const hasStatusFilter = statuses && statuses.length > 0;
    const hasCategoryFilter = categoryId;
    const hasCreatedByFilter = createdBy; // NEW: Check if Created By filter active
    const hasAnyFilter = hasTextQuery || hasDateFilter || hasStatusFilter || hasCategoryFilter || hasCreatedByFilter;
    
    // SPECIAL CASE: No filters at all - load all news
    if (!hasAnyFilter) {
      this.isLoading = true;
      this.error = null;
      this.cdr.markForCheck();
      
      return new Observable(subscriber => {
        this.newsService.getAllNews(currentPage, pageSize).subscribe({
          next: (data) => {
            this.handleSearchSuccess(data);
            subscriber.next(data);
            subscriber.complete();
          },
          error: (error) => {
            this.handleSearchError(error);
            subscriber.error(error);
          }
        });
      });
    }
    
    // NORMAL CASE: Has at least one filter - use search API with filters
    // (even if query is null, date/status/category filters will be applied)
    
    // STEP 3: CREATE CACHE KEY WITH ALL RELEVANT FIELDS
    // Includes pageSize to avoid cache inconsistencies
    const queryKey = JSON.stringify({
      query, // Could be null or text
      statuses,
      categoryId,
      createdBy, // Added: Admin user UUID filter
      fromDate: fromDate?.getTime(),
      toDate: toDate?.getTime(),
      page: currentPage,
      pageSize: pageSize,
      searchMode: searchMode
    });
    
    // STEP 4: CHECK CACHE
    if (this.searchRequestCache.has(queryKey)) {
      const cachedData = this.searchRequestCache.get(queryKey);
      this.handleSearchSuccess(cachedData);
      return of(cachedData);
    }
    
    this.lastExecutedQuery = queryKey;
    this.isLoading = true;
    this.error = null;
    this.cdr.markForCheck();

    // STEP 5: SELECT SEARCH METHOD BASED ON MODE
    let searchObservable: any;
    
    switch (searchMode) {
      case 'title':
        searchObservable = this.adminSearchService.searchByTitleOnly(
          query, statuses, categoryId, createdBy, fromDate, toDate,
          currentPage - 1, pageSize, 'createdAt,desc'
        );
        break;
        
      case 'content':
        searchObservable = this.adminSearchService.searchByContentOnly(
          query, statuses, categoryId, createdBy, fromDate, toDate,
          currentPage - 1, pageSize, 'createdAt,desc'
        );
        break;
        
      default:
        searchObservable = this.adminSearchService.searchMultiField(
          query, statuses, categoryId, createdBy, fromDate, toDate,
          currentPage - 1, pageSize, 'createdAt,desc'
        );
    }

    // STEP 6: ADD CACHING AND SUCCESS HANDLING
    // Return the actual Observable (not wrapped in Promise)
    return searchObservable.pipe(
      tap((response: any) => {
        const data = response?.data || response;
        // Cache the result
        this.searchRequestCache.set(queryKey, data);
        
        // Limit cache size to prevent memory bloat
        if (this.searchRequestCache.size > this.SEARCH_CACHE_MAX_SIZE) {
          const firstKey = this.searchRequestCache.keys().next().value;
          if (firstKey) {
            this.searchRequestCache.delete(firstKey);
          }
        }
      }),
      tap((response: any) => {
        const data = response?.data || response;
        this.handleSearchSuccess(data);
      }),
      catchError((error: any) => {
        console.error('[Search] ✗ Error:', error);
        this.handleSearchError(error);
        throw error;
      })
    );
  }

  private handleSearchSuccess(data: any): void {
    this.newsList = data.content || [];
    this.totalElements = data.totalElements || 0;
    this.totalPages = data.totalPages || 0;
    this.filteredNewsList = [...this.newsList];
    this.isLoading = false;
    this.error = null;
    this.cdr.markForCheck();
  }

  private handleSearchError(error: any): void {
    console.error('[Search] Error occurred:', error);
    this.error = error?.message || 'Search failed';
    this.isLoading = false;
    this.newsList = [];
    this.filteredNewsList = [];
    this.cdr.markForCheck();
  }

  // Media handling methods
  // Media URL generation and management
  getMediaUrl(fileName: string): string {
    if (!fileName) return '';
    
    // Simply return the direct URL for all media files
    const url = this.newsService.getMediaFileUrl(fileName);
    console.log('Generated media URL for', fileName, ':', url);
    
    return url;
  }

  // Create streaming video URL using fetch to bypass download headers
  private createStreamingVideoUrl(fileName: string): void {
    console.log('Creating streaming URL for video:', fileName);
    
    const url = this.newsService.getMediaFileUrl(fileName);
    console.log('Fetching video from URL:', url);
    
    // First try a simple HEAD request to check if the endpoint is accessible
    fetch(url, { method: 'HEAD' })
      .then(response => {
        console.log('HEAD request status:', response.status);
        console.log('HEAD request headers:', response.headers);
        console.log('Content-Type:', response.headers.get('content-type'));
        console.log('Content-Disposition:', response.headers.get('content-disposition'));
        
        // If HEAD request fails, try direct URL
        if (!response.ok) {
          console.warn('HEAD request failed, trying direct URL');
          this.fallbackToDirectUrl(fileName);
          return;
        }
        
        // Try full fetch with range request
        return fetch(url, {
          method: 'GET',
          headers: {
            'Accept': 'video/*',
            'Range': 'bytes=0-'  // Request range to enable streaming
          }
        });
      })
      .then(response => {
        if (!response || !response.ok) {
          throw new Error(`HTTP ${response?.status}: ${response?.statusText}`);
        }
        console.log('GET request successful, getting blob...');
        return response.blob();
      })
      .then(blob => {
        console.log('Received video blob:', fileName, 'Size:', blob.size, 'Type:', blob.type);
        
        if (blob.size === 0) {
          console.error('Received empty blob for video:', fileName);
          this.fallbackToDirectUrl(fileName);
          return;
        }
        
        // Create a blob URL with video MIME type to force streaming
        const videoBlob = new Blob([blob], { type: this.getVideoMimeType(fileName) });
        const blobUrl = URL.createObjectURL(videoBlob);
        
        console.log('Created streaming URL for video:', fileName, blobUrl);
        
        // Update the cached URL
        this.mediaUrlCache.set(fileName, blobUrl);
        this.blobUrls.set(fileName, blobUrl);
        
        // Trigger change detection to update the view
        this.cdr.detectChanges();
      })
      .catch(error => {
        console.error('Failed to create streaming URL for video:', fileName, error);
        console.error('Error details:', {
          name: error.name,
          message: error.message,
          stack: error.stack
        });
        
        // Fallback to direct URL
        this.fallbackToDirectUrl(fileName);
      });
  }

  // Create blob URL for video files to enable proper streaming
  private createVideoBlobUrl(fileName: string): void {
    if (!fileName || !this.isVideo(fileName)) return;
    
    console.log('Creating blob URL for video:', fileName);
    
    this.newsService.getMediaFileBlob(fileName).subscribe({
      next: (blob: any) => {
        console.log('Received blob for video:', fileName, 'Size:', blob.size, 'Type:', blob.type);
        
        if (blob.size === 0) {
          console.error('Empty blob received for video:', fileName);
          this.fallbackToDirectUrl(fileName);
          return;
        }
        
        const blobUrl = URL.createObjectURL(blob);
        console.log('Created blob URL for video:', fileName, blobUrl);
        
        // Update the cached URL
        this.mediaUrlCache.set(fileName, blobUrl);
        this.blobUrls.set(fileName, blobUrl);
        
        // Trigger change detection to update the view
        this.cdr.detectChanges();
      },
      error: (error: any) => {
        console.error('Failed to create blob URL for video:', fileName, error);
        this.fallbackToDirectUrl(fileName);
      }
    });
  }

  // Fallback to direct URL when blob creation fails
  private fallbackToDirectUrl(fileName: string): void {
    console.log('Using direct URL fallback for:', fileName);
    const fallbackUrl = this.newsService.getMediaFileUrl(fileName);
    this.mediaUrlCache.set(fileName, fallbackUrl);
    this.cdr.detectChanges();
  }

  // Check if video URL is ready (check for blob URL or direct URL)
  isVideoReady(fileName: string): boolean {
    if (!fileName || !this.isVideo(fileName)) return false;
    
    const cachedUrl = this.mediaUrlCache.get(fileName);
    return cachedUrl !== undefined && cachedUrl !== 'data:video/mp4;base64,';
  }

  // Get direct video URL for fallback
  getDirectVideoUrl(fileName: string): string {
    if (!fileName) return '';
    return this.newsService.getMediaFileUrl(fileName);
  }

  // Force direct video URL when user clicks the fallback button
  forceDirectVideoUrl(fileName: string): void {
    console.log('Forcing direct URL for video:', fileName);
    this.fallbackToDirectUrl(fileName);
  }

  // Create blob URL for video files
  // Video and image detection methods
  isImage(filename: string): boolean {
    if (!filename) return false;
    const imageExtensions = ['.jpg', '.jpeg', '.png', '.gif', '.bmp', '.webp', '.svg'];
    const extension = filename.toLowerCase().substring(filename.lastIndexOf('.'));
    return imageExtensions.includes(extension);
  }

  isVideo(filename: string): boolean {
    if (!filename) return false;
    const videoExtensions = ['.mp4', '.avi', '.mov', '.wmv', '.flv', '.webm', '.mkv', '.m4v'];
    const extension = filename.toLowerCase().substring(filename.lastIndexOf('.'));
    return videoExtensions.includes(extension);
  }

  // Check if video format is supported by HTML5 video element
  isSupportedVideoFormat(filename: string): boolean {
    if (!filename) return false;
    const supportedExtensions = ['.mp4', '.webm', '.ogg'];
    const extension = filename.toLowerCase().substring(filename.lastIndexOf('.'));
    return supportedExtensions.includes(extension);
  }

  // Check if it's a video but not supported for streaming (like AVI)
  isUnsupportedVideoFormat(filename: string): boolean {
    return this.isVideo(filename) && !this.isSupportedVideoFormat(filename);
  }

  // Get file extension for display
  getFileExtension(filename: string): string {
    if (!filename) return '';
    return filename.substring(filename.lastIndexOf('.') + 1);
  }

  getVideoMimeType(filename: string): string {
    if (!filename) return 'video/mp4';
    
    const extension = filename.toLowerCase().substring(filename.lastIndexOf('.'));
    const mimeTypes: { [key: string]: string } = {
      '.mp4': 'video/mp4',
      '.avi': 'video/x-msvideo',
      '.mov': 'video/quicktime',
      '.wmv': 'video/x-ms-wmv',
      '.flv': 'video/x-flv',
      '.webm': 'video/webm',
      '.mkv': 'video/x-matroska',
      '.m4v': 'video/mp4'
    };
    
    return mimeTypes[extension] || 'video/mp4';
  }

  // Video playback control - ensures only one video plays at a time
  onVideoPlay(event: any): void {
    const currentVideo = event.target as HTMLVideoElement;
    console.log('Video started playing:', currentVideo.src);
    
    // Use the video control service to manage playback
    // This will automatically pause any other playing videos
    this.videoControlService.playVideo(currentVideo);
  }

  // Pause all videos except the currently playing one
  private pauseAllOtherVideos(currentVideo: HTMLVideoElement): void {
    // Since the service already handles pausing other videos in playVideo(),
    // this method is no longer needed as the logic is centralized
    // But we'll keep it for backward compatibility
    this.videoControlService.pauseAllVideos();
  }

  onMediaError(event: any): void {
    console.error('Media failed to load:', event);
    const target = event.target as HTMLElement;
    if (target) {
      // For videos, show a download link instead
      if (target.tagName.toLowerCase() === 'video') {
        const videoContainer = target.parentElement;
        if (videoContainer) {
          const filename = target.getAttribute('data-filename');
          videoContainer.innerHTML = `
            <div class="video-error">
              <p>Video cannot be played. <a href="${this.getMediaUrl(filename || '')}" target="_blank">Download video file</a></p>
            </div>
          `;
        }
      } else {
        target.style.display = 'none';
      }
    }
  }

  // Add a method to handle video loading
  onVideoLoad(event: any): void {
    console.log('Video loaded successfully:', event.target.src);
    console.log('Video duration:', event.target.duration);
    console.log('Video ready state:', event.target.readyState);
  }

  onVideoLoadStart(event: any): void {
    console.log('Video load started:', event.target.src);
  }

  onVideoCanPlay(event: any): void {
    console.log('Video can play:', event.target.src);
    console.log('Video buffered:', event.target.buffered.length);
  }

  onVideoError(event: any): void {
    console.error('Video failed to load:', event.target.src);
    console.error('Video error:', event.target.error);
    console.error('Network state:', event.target.networkState);
    console.error('Error code:', event.target.error?.code);
    console.error('Error message:', event.target.error?.message);
    
    // Try to test the URL directly
    this.testMediaUrl(event.target.src);
    
    // Create a direct link as fallback
    this.createDirectVideoLink(event.target);
  }

  // Create a direct link for video if the video element fails
  createDirectVideoLink(videoElement: HTMLVideoElement): void {
    const container = videoElement.parentElement;
    if (container) {
      const link = document.createElement('a');
      link.href = videoElement.querySelector('source')?.src || '';
      link.textContent = '🎬 Click to watch video';
      link.target = '_blank';
      link.style.display = 'block';
      link.style.padding = '1rem';
      link.style.textAlign = 'center';
      link.style.backgroundColor = '#f3f4f6';
      link.style.border = '1px solid #d1d5db';
      link.style.borderRadius = '8px';
      link.style.textDecoration = 'none';
      link.style.color = '#374151';
      
      // Replace the video element with the link
      container.replaceChild(link, videoElement);
    }
  }

  // Test if media URL is accessible
  testMediaUrl(url: string): void {
    fetch(url, { method: 'HEAD' })
      .then(response => {
        console.log('Media URL test - Status:', response.status);
        console.log('Media URL test - Headers:', response.headers);
        console.log('Media URL test - Content-Type:', response.headers.get('content-type'));
      })
      .catch(error => {
        console.error('Media URL test failed:', error);
      });
  }

  // Share video functionality
  shareVideo(news: any): void {
    const videoUrl = `${window.location.origin}${this.router.createUrlTree(['/news', news.id]).toString()}`;
    const shareData = {
      title: `${news.titleEn} - Video`,
      text: `Check out this video: ${news.titleEn}`,
      url: videoUrl
    };

    if (navigator.share) {
      navigator.share(shareData).catch(err => 
        console.log('Error sharing video:', err)
      );
    } else {
      // Fallback to copying URL to clipboard
      navigator.clipboard.writeText(videoUrl).then(() => {
        console.log('Video URL copied to clipboard');
        // You could add a toast notification here
        alert('Video link copied to clipboard!');
      }).catch(err => {
        console.error('Failed to copy to clipboard:', err);
        // Fallback to prompt
        prompt('Copy this URL to share the video:', videoUrl);
      });
    }
  }

  // Share news functionality
  shareNews(news: any): void {
    const newsUrl = `${window.location.origin}${this.router.createUrlTree(['/news', news.id]).toString()}`;
    const shareData = {
      title: news.titleEn,
      text: news.contentEn.substring(0, 200) + '...',
      url: newsUrl
    };

    if (navigator.share) {
      navigator.share(shareData).catch(err => 
        console.log('Error sharing news:', err)
      );
    } else {
      // Fallback to copying URL to clipboard
      navigator.clipboard.writeText(newsUrl).then(() => {
        console.log('News URL copied to clipboard');
        alert('News link copied to clipboard!');
      }).catch(err => {
        console.error('Failed to copy to clipboard:', err);
        // Fallback to prompt
        prompt('Copy this URL to share the news:', newsUrl);
      });
    }
  }

  /**
   * Dismiss categories error without retrying
   */
  onDismissCategoriesError(): void {
    this.categoriesError = null;
    this.cdr.markForCheck();
  }

  /**
   * Dismiss workflow statuses error without retrying
   */
  onDismissWorkflowStatusesError(): void {
    this.workflowStatusesError = null;
    this.cdr.markForCheck();
  }

  /**
   * Dismiss news loading error without retrying
   */
  onDismissNewsError(): void {
    this.error = null;
    this.cdr.markForCheck();
  }
}