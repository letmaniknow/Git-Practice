
  // ...existing imports...

import { UnifiedDatetimePickerDialogComponent } from 'src/app/shared/components/unified-datetime-picker/unified-datetime-picker-dialog.component';


import { Component, Input, OnInit, OnDestroy, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { GridSortEvent } from './news-list-table-grid.component';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { trigger, transition, style, animate } from '@angular/animations';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatMenuModule } from '@angular/material/menu';
import { MatDividerModule } from '@angular/material/divider';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { NewsClonedDialogComponent } from '../news-cloned-dialog/news-cloned-dialog.component';
import { SuccessErrorDialogComponent } from 'src/app/shared/components/success-error-dialog/success-error-dialog.component';
import { ErrorAlertComponent } from '../../../../shared/components/error-alert/error-alert.component';
import { LoadingComponent } from '../../../../shared/components/loading/loading.component';
import { Subject, Observable, debounceTime, switchMap, catchError, tap, of, timeout, distinctUntilChanged, finalize, filter, interval, take, map } from 'rxjs';
import { PageEvent } from '@angular/material/paginator';
import { takeUntil } from 'rxjs/operators';
import { NewsListService } from '../../services/news-list.service';
import { NewsAdvancedSearchService } from '../../services/news-advanced-search.service';
import { NewsWorkflowStatusService, WorkflowStatusConfig } from '../../services/news-workflow-status.service';
import { NewsFormService } from '../../services/news-form.service';
import { AppMasterDataService, AdminUser, NewsCategory as MasterDataCategory } from '../../../../core/services/app-master-data.service';
import { ColumnPreferencesService } from '../../services/column-preferences.service';
import { HttpErrorCategorizationService } from '../../../../core/services/http-error-categorization.service';
import { ThemeService } from '../../../../core/services/theme.service';
import { NewsItem } from '../../models/news-item.model';
import { PaginatedNewsResponse } from '../../models/paginated-news-response.model';
import { NewsCategory } from '../../models/news-form.model';
import { DEFAULT_COLUMN_DEFINITIONS, ColumnDefinition } from '../../models/column-config.model';
import { ConfirmationDialogComponent } from '../confirmation-dialog/confirmation-dialog.component';
import { NewsListTableGridComponent } from './news-list-table-grid.component';
import { ColumnCustomizationDialogComponent } from '../column-customization-dialog.component';
import { NewsSearchFiltersComponent, INewsSearchFiltersState, INewsSearchFiltersChangedEvent } from '../news-search-filters/news-search-filters.component';
import { TABLE_CONFIG, SPINNER_CONFIG, DIALOG_CONFIG, SORT_CONFIG } from './table.constants';
import { LOADING_STRINGS, EMPTY_STATE_STRINGS, ERROR_STATE_STRINGS, TOOLBAR_STRINGS, BULK_ACTIONS_STRINGS, DATE_STRINGS, ACCESSIBILITY_STRINGS, DEBUG_STRINGS } from './table-strings';

@Component({
  selector: 'app-news-list-table',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  animations: [
    trigger('slideInOut', [
      transition(':enter', [
        style({ opacity: 0, transform: 'translateY(-10px)', maxHeight: '0px' }),
        animate('300ms ease-out', style({ opacity: 1, transform: 'translateY(0)', maxHeight: '200px' }))
      ]),
      transition(':leave', [
        animate('200ms ease-in', style({ opacity: 0, transform: 'translateY(-10px)', maxHeight: '0px' }))
      ])
    ])
  ],
  imports: [
    CommonModule,
    FormsModule,
    RouterModule,
    MatTableModule,
    MatIconModule,
    MatButtonModule,
    MatMenuModule,
    MatDividerModule,
    MatCheckboxModule,
    MatProgressSpinnerModule,
    MatTooltipModule,
    MatSelectModule,
    MatFormFieldModule,
    MatDialogModule,
    MatSnackBarModule,
    NewsListTableGridComponent,
    MatPaginatorModule,
    ErrorAlertComponent,
    LoadingComponent,
  ],
  templateUrl: './news-list-table.component.html',
  styleUrl: './news-list-table.component.css'
})
export class NewsListTableComponent implements OnInit, OnDestroy {

  /**
   * Handles refresh requests from the grid after backend-altering actions (soft delete, restore, status change, etc.)
   * Always triggers a table refresh, bypassing deduplication logic.
   */
  onRefreshRequested(): void {
    this.currentPage = 1;
    if (this.hasActiveFilters()) {
      this.filterChange$.next();
    } else {
      this.loadArticles();
    }
  }

    /**
     * Handle schedule/reschedule news action from the table grid
     * Opens datetime picker, calls service, blocks UI, shows notification, and refreshes table
     */
    isScheduling = false;
    onScheduleNews(newsItem: NewsItem): void {
      const dialogRef = this.dialog.open(UnifiedDatetimePickerDialogComponent, {
        data: {
          selectedDateTime: newsItem.newsScheduledPublishAt ? new Date(newsItem.newsScheduledPublishAt) : null,
          min: new Date(),
        },
        width: '400px',
        disableClose: true,
      });

      dialogRef.afterClosed().subscribe((result: Date | null) => {
        if (!result) return;
        // Block UI during API call
        this.isScheduling = true;
        const isoString = result.toISOString();
        this.newsListService.scheduleNews(newsItem.newsNewsId, isoString)
          .pipe(
            takeUntil(this.destroy$),
            finalize(() => this.isScheduling = false)
          )
          .subscribe({
            next: () => {
              const successDialog = this.dialog.open(SuccessErrorDialogComponent, {
                data: {
                  type: 'success',
                  title: newsItem.newsScheduledPublishAt ? 'Rescheduled!' : 'Scheduled!',
                  message: `The news item has been ${newsItem.newsScheduledPublishAt ? 'rescheduled' : 'scheduled'} for publication at ${result.toLocaleString()}.\nWould you like to reschedule again?`,
                  showCancel: true,
                  okText: 'Reschedule',
                  cancelText: 'Cancel',
                },
                width: '400px',
                disableClose: true,
              });
              successDialog.afterClosed().subscribe((res: 'ok' | 'cancel' | undefined) => {
                if (res === 'ok') {
                  // Always reload the latest entity before rescheduling again
                  this.newsListService.getNewsById(newsItem.newsNewsId).subscribe((freshNews) => {
                    this.onScheduleNews(freshNews);
                  });
                } else {
                  if (this.hasActiveFilters()) {
                    this.filterChange$.next();
                  } else {
                    this.loadArticles();
                  }
                }
              });
            },
            error: (err) => {
              let message = err?.userMessage || 'Failed to schedule news. Please try again.';
              // Show user-friendly message for 409 Conflict
              if (err?.status === 409) {
                message = 'This news item was updated by another user or in another tab. Please reload the list and try again.';
              }
              this.dialog.open(SuccessErrorDialogComponent, {
                data: {
                  type: 'error',
                  title: 'Schedule Failed',
                  message,
                },
                width: '400px',
              });
              // Always refresh list after error to avoid stale data
              if (this.hasActiveFilters()) {
                this.filterChange$.next();
              } else {
                this.loadArticles();
              }
            },
          });
      });
    }
      /**
       * Handle unpublish news action from the table grid
       * Calls service to unpublish news item, shows notification, and refreshes table
       */
      onUnpublishNews(newsItem: NewsItem): void {
        const title = this.currentLanguage === 'en' ? newsItem.newsTitleEn : newsItem.newsTitleEs;
        this.newsListService.unpublishNews(newsItem.newsNewsId)
          .pipe(takeUntil(this.destroy$))
          .subscribe({
            next: () => {
              this.dialog.open(SuccessErrorDialogComponent, {
                data: {
                  type: 'success',
                  title: 'News Unpublished!',
                  message: `The news item "${title}" was unpublished successfully.`
                },
                width: '400px'
              });
              if (this.hasActiveFilters()) {
                this.filterChange$.next();
              } else {
                this.loadArticles();
              }
            },
            error: (error: any) => {
              this.dialog.open(SuccessErrorDialogComponent, {
                data: {
                  type: 'error',
                  title: 'Unpublish Failed',
                  message: `Failed to unpublish news: ${title}`
                },
                width: '400px'
              });
              console.error('[NewsListTableComponent] unpublishNews() failed:', error);
            }
          });
      }
  // ...existing properties...

  onPublishNews(newsItem: NewsItem): void {
    const title = this.currentLanguage === 'en' ? newsItem.newsTitleEn : newsItem.newsTitleEs;
    this.newsListService.publishNews(newsItem.newsNewsId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.dialog.open(SuccessErrorDialogComponent, {
            data: {
              type: 'success',
              title: 'News Published!',
              message: `The news item "${title}" was published successfully.`
            },
            width: '400px'
          });
          if (this.hasActiveFilters()) {
            this.filterChange$.next();
          } else {
            this.loadArticles();
          }
        },
        error: (error: any) => {
          this.dialog.open(SuccessErrorDialogComponent, {
            data: {
              type: 'error',
              title: 'Publish Failed',
              message: `Failed to publish news: ${title}`
            },
            width: '400px'
          });
          console.error('[NewsListTableComponent] publishArticle() failed:', error);
        }
      });
  }

  /**
   * Handle row action (Clone News)
   * Calls service to clone news item, shows notification, and refreshes table
   */
  onCloneNews(newsItem: NewsItem): void {
    const title = this.currentLanguage === 'en' ? newsItem.newsTitleEn : newsItem.newsTitleEs;
    console.debug('[NewsListTableComponent] onCloneNews() - id=%s, title=%s', newsItem.newsNewsId, title);
    this.newsListService.cloneNews(newsItem.newsNewsId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response: any) => {
          // Open modal dialog with Edit option
          const newNewsId = response?.data?.newsNewsId;
          this.dialog.open(NewsClonedDialogComponent, {
            data: { newsId: newNewsId, newsTitle: title },
            width: '520px',
            maxWidth: '95vw',
            panelClass: 'modern-news-cloned-dialog'
          });
          // Refresh table respecting active filters
          this.currentPage = 1;
          if (this.hasActiveFilters()) {
            this.filterChange$.next();
          } else {
            this.loadArticles();
          }
        },
        error: (error: any) => {
          this.dialog.open(SuccessErrorDialogComponent, {
            data: {
              type: 'error',
              title: 'Clone Failed',
              message: `Failed to clone news: ${title}`
            },
            width: '400px'
          });
          console.error('[NewsListTableComponent] cloneNews() failed:', error);
        }
      });
  }

// Remove duplicate/unclosed @Component and class blocks below this line
// (The rest of the file should only contain the class body and its methods/properties)
  columnDefinitions = DEFAULT_COLUMN_DEFINITIONS;
  // Column visibility is always managed by ColumnPreferencesService
  visibleColumnIds: string[] = [];
  lookupsLoaded = false;
  private categoriesLoaded = false;
  private authorsLoaded = false;

  /**
   * Handle sort change event from grid
   * Updates sort state and triggers search/filter pipeline
   */
  onSortChange(event: GridSortEvent): void {
    if (!event || !event.columnId) return;
    this.sortColumn = event.columnId; // This is now always the correct propertyPath
    this.sortDirection = event.direction;
    this.currentPage = 1;
    window.scrollTo({ top: 0, behavior: 'smooth' });
    this.cardLanguages.clear();
    this.filterChange$.next();
  }

    /**
     * Returns a thumbnail URL string (never undefined) for the given item.
     * Used for template binding to satisfy strict type requirements.
     */
    getThumbnailUrlSafe(item: any): string {
      return this.getThumbnailUrl(item) || '';
    }
  @Input() currentLanguage: 'en' | 'es' = 'en';



  // ============================================================================
  // CONFIGURATION CONSTANTS - Single Source of Truth (imported from table.constants.ts)
  // ============================================================================
  private readonly DEFAULT_PAGE_SIZE = TABLE_CONFIG.DEFAULT_PAGE_SIZE;
  private readonly PAGE_SIZE_OPTIONS = TABLE_CONFIG.PAGE_SIZE_OPTIONS;
  private readonly SEARCH_CACHE_MAX_SIZE = TABLE_CONFIG.SEARCH_CACHE_MAX_SIZE;
  private readonly DEBOUNCE_TIME_MS = TABLE_CONFIG.DEBOUNCE_TIME_MS;
  private readonly REQUEST_TIMEOUT_MS = TABLE_CONFIG.REQUEST_TIMEOUT_MS;
  private readonly MAX_SEARCH_LENGTH = TABLE_CONFIG.MAX_SEARCH_LENGTH;
  private readonly MAX_CATEGORY_ID_LENGTH = TABLE_CONFIG.MAX_CATEGORY_ID_LENGTH;



  // Lifecycle management
  private destroy$ = new Subject<void>();
  private filterChange$ = new Subject<void>(); // RxJS-based filter change orchestration
  
  // Error handling for master data
  categoriesLoadError: string | null = null;
  adminUsersLoadError: string | null = null;
  isLoadingCategories = false;
  isLoadingAdminUsers = false;

  // News loading state (from service)
  isLoadingNews = false;
  newsLoadError: string | null = null;

  // Observable streams from service
  public news$: Observable<NewsItem[]>;
  public loading$: Observable<boolean>;
  public error$: Observable<string | null>;
  public success$: Observable<string | null>;
  public totalItems$: Observable<number>;
  public errorLiveRegion$: Observable<'polite' | 'assertive'>; // WCAG aria-live politeness level

  // Current news snapshot for bulk selection (synced from news$ observable)
  private currentNews: NewsItem[] = [];

  // Professional Search: Query Result Caching
  private searchRequestCache = new Map<string, any>();
  private lastExecutedQuery: string | null = null;
  private lastExecutedFilterState: any = null; // Track finalized filter state for deduplication

  // Workflow status cache
  private statusConfigCache: Map<string, WorkflowStatusConfig> = new Map();
  public statusesLoaded = false; // Track if statuses have been loaded from service

  // Language support - per-row overrides (like card view)
  cardLanguages: Map<string, 'en' | 'es'> = new Map();

  // Bulk selection - track selected news IDs
  private selectedNewsIds: Set<string> = new Set();
  get selectedNewsCount(): number {
    return this.selectedNewsIds.size;
  }
  get isAllSelected(): boolean {
    return this.currentNews.length > 0 && this.selectedNewsIds.size === this.currentNews.length;
  }

  // Pagination parameters (local UI state only, not synced to service)
  currentPage: number = 1;
  pageSize: number = this.DEFAULT_PAGE_SIZE;
  pageSizeOptions: number[] = [...this.PAGE_SIZE_OPTIONS]; // Expose to template, mutable copy
  totalItems: number = 0;  // Display-only: updated from totalItems$ observable
  totalPages: number = 0;

  // Sorting parameters (local UI state only, not synced to service)
  sortColumn: string = SORT_CONFIG.DEFAULT_COLUMN;
  sortDirection: 'asc' | 'desc' = SORT_CONFIG.DEFAULT_DIRECTION;

  // Track visible columns (updated reactively from preferences)
  // (Removed duplicate declaration)

  // Delete confirmation dialog state
  deleteDialogOpen = false;
  deleteDialogLoading = false;
  deleteNewsId: string | null = null;
  deleteNewsTitle: string | null = null;

  currentFiltersState: INewsSearchFiltersState = {
    searchTextInput: '',
    searchModeSelected: 'all',
    workflowStatusesSelected: [],
    categoryIdSelected: null,
    createdDateFrom: null,
    createdDateTo: null,
    createdByAdminUserIdSelected: null
  };

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

  /**
   * Spinner diameter - exposed to template from configuration constant
   */
  get spinnerDiameter(): number {
    return SPINNER_CONFIG.DIAMETER;
  }


  // ==========================================================================
  // STRING CONSTANTS - Exposed to Template (Professional, i18n-ready)
  // ==========================================================================
  get customizeColumnsTooltip(): string {
    return TOOLBAR_STRINGS.CUSTOMIZE_COLUMNS_TOOLTIP;
  }

  get customizeColumnsAriaLabel(): string {
    return TOOLBAR_STRINGS.CUSTOMIZE_COLUMNS_ARIA_LABEL;
  }

  get loadingMessage(): string {
    return LOADING_STRINGS.LOADING_MESSAGE;
  }

  get emptyStateHeading(): string {
    return EMPTY_STATE_STRINGS.EMPTY_HEADING;
  }

  get emptyStateDescription(): string {
    return EMPTY_STATE_STRINGS.EMPTY_DESCRIPTION;
  }

  get emptyStateIcon(): string {
    return EMPTY_STATE_STRINGS.EMPTY_ICON;
  }

  get notPublishedText(): string {
    return DATE_STRINGS.NOT_PUBLISHED;
  }

  get bulkActionsPublishLabel(): string {
    return BULK_ACTIONS_STRINGS.PUBLISH_BUTTON;
  }

  get bulkActionsPublishAriaLabel(): string {
    return BULK_ACTIONS_STRINGS.PUBLISH_ARIA_LABEL;
  }

  get bulkActionsDeleteLabel(): string {
    return BULK_ACTIONS_STRINGS.DELETE_BUTTON;
  }

  get bulkActionsDeleteAriaLabel(): string {
    return BULK_ACTIONS_STRINGS.DELETE_ARIA_LABEL;
  }

  get bulkActionsClearLabel(): string {
    return BULK_ACTIONS_STRINGS.CLEAR_BUTTON;
  }

  get bulkActionsClearAriaLabel(): string {
    return BULK_ACTIONS_STRINGS.CLEAR_ARIA_LABEL;
  }

  // Pagination event handler
  onPageEvent(event: PageEvent): void {
    if (event.pageSize !== this.pageSize) {
      this.pageSize = event.pageSize;
      this.currentPage = 1; // Reset to first page on page size change
    } else {
      this.currentPage = event.pageIndex + 1; // MatPaginator is 0-based, backend is 1-based
    }
    window.scrollTo({ top: 0, behavior: 'smooth' });
    this.filterChange$.next();
  }

  constructor(
    private newsListService: NewsListService,
    private adminSearchService: NewsAdvancedSearchService,
    private workflowStatusService: NewsWorkflowStatusService,
    private newsFormService: NewsFormService,
    private columnPreferencesService: ColumnPreferencesService,
    private errorCategorizer: HttpErrorCategorizationService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
    private cdr: ChangeDetectorRef,
    private router: Router,
    private themeService: ThemeService,
    private appMasterDataService: AppMasterDataService
  ) {
    console.debug('[NewsListTableComponent] constructor called');

    // Initialize observable streams from service
    this.news$ = this.newsListService.news$;
    this.loading$ = this.newsListService.loading$;
    this.error$ = this.newsListService.error$;
    this.success$ = this.newsListService.success$;
    this.totalItems$ = this.newsListService.totalItems$;

    // Map error to appropriate aria-live politeness level based on severity
    this.errorLiveRegion$ = this.error$.pipe(
      map(error => {
        if (!error) return 'polite';
        // For critical errors, use assertive for screen reader urgency
        const isConnectionError = error.includes('Connection') || error.includes('Failed');
        const isServerError = error.includes('500') || error.includes('503');
        return (isConnectionError || isServerError) ? 'assertive' : 'polite';
      })
    );

    // Pre-bind methods for template (performance optimization for OnPush)
    // Created once in constructor, reused forever - no new functions per change detection
    this.getThumbnailUrlBound = this.getThumbnailUrlSafe.bind(this);
    this.getCategoryNameByIdBound = this.getCategoryNameById.bind(this);
    this.getAdminUserNameByIdBound = this.getAdminUserNameById.bind(this);
    this.trackByNewsIdBound = this.trackByNewsId.bind(this);
    this.getTitleBound = this.getTitle.bind(this);
    this.getStatusConfigBound = this.getStatusConfig.bind(this);
    this.onThumbnailErrorBound = this.onThumbnailError.bind(this);
    this.isRowSelectedBound = this.isRowSelected.bind(this);
  }

  // Search & Filter State
  availableCategories: NewsCategory[] = [];
  availableWorkflowStatuses: string[] = [];
  availableAdminUsers: AdminUser[] = [];

  // Pre-bound methods for template (avoid .bind(this) in template for performance)
  // These are created once in constructor and reused - no new functions per change detection
  readonly getThumbnailUrlBound: (newsId: string) => string;
  readonly getCategoryNameByIdBound: (categoryId: string | null) => string;
  readonly getAdminUserNameByIdBound: (userId: string | null) => string;
  readonly trackByNewsIdBound: (index: number, item: NewsItem) => string;
  readonly getTitleBound: (article: NewsItem) => string;
  readonly getStatusConfigBound: (status: string) => WorkflowStatusConfig;
  readonly onThumbnailErrorBound: (event: Event) => void;
  readonly isRowSelectedBound: (newsId: string) => boolean;

  ngOnInit(): void {
    console.debug(DEBUG_STRINGS.COMPONENT_INIT);

    // Initialize visible columns from preferences
    this.visibleColumnIds = this.columnPreferencesService.getCurrentPreferences().visibleColumnIds;



    // Subscribe to unknown statuses and show dismissible warning
    this.workflowStatusService.unknownStatuses$
      .pipe(
        takeUntil(this.destroy$),
        filter((unknownStatuses: string[]) => unknownStatuses.length > 0)
      )
      .subscribe((unknownStatuses: string[]) => {
        console.warn('[NewsListTableComponent] 🚨 Unknown statuses detected:', unknownStatuses);
        const statusList = unknownStatuses.join(', ');
        const message = `⚠️ New status(es) detected: ${statusList}. Please update the styling in the frontend code.`;
        this.snackBar.open(message, 'Dismiss', {
          duration: 0,
          horizontalPosition: 'center',
          verticalPosition: 'top',
          panelClass: ['warning-snackbar']
        });
      });

    // Load filter data (categories and admin users)
    this.loadFilterData();

    // Subscribe to news loading state
    this.loading$
      .pipe(takeUntil(this.destroy$))
      .subscribe(isLoading => {
        this.isLoadingNews = isLoading;
        this.cdr.markForCheck();
      });

    // Subscribe to news loading errors
    this.error$
      .pipe(takeUntil(this.destroy$))
      .subscribe(error => {
        this.newsLoadError = error;
        this.cdr.markForCheck();
      });

    // Subscribe to column preference changes and trigger change detection
    this.columnPreferencesService.preferences$
      .pipe(takeUntil(this.destroy$))
      .subscribe((prefs) => {
        this.visibleColumnIds = prefs.visibleColumnIds;
        this.cdr.markForCheck();
      });

    // Calculate totalPages when totalItems or pageSize changes
    this.totalItems$
      .pipe(takeUntil(this.destroy$))
      .subscribe(totalItems => {
        this.totalItems = totalItems;
        this.totalPages = Math.ceil(totalItems / this.pageSize);
      });

    // Sync current news for bulk selection
    this.news$
      .pipe(takeUntil(this.destroy$))
      .subscribe((articles: any) => {
        this.currentNews = articles;
        // Clear selections when news change (happens during pagination/filtering)
        this.clearSelection();
      });

    // Auto-dismiss success message after 4 seconds
    this.success$
      .pipe(
        takeUntil(this.destroy$),
        filter((message): message is string => message !== null),
        switchMap(message =>
          interval(TABLE_CONFIG.SUCCESS_MESSAGE_AUTO_DISMISS_MS).pipe(
            take(1),
            tap(() => this.newsListService.setSuccess(null))
          )
        )
      )
      .subscribe();

    // ========================================================================
    // PROFESSIONAL SEARCH PIPELINE (RxJS-DRIVEN) - MATCHES CARD VIEW PATTERN
    // ========================================================================
    this.filterChange$
      .pipe(
        debounceTime(this.DEBOUNCE_TIME_MS),
        tap(() => console.debug('[NewsListTableComponent] After debounce, executing buildSearchQuery()')),
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
            page: this.currentPage,
            pageSize: this.pageSize,  // CRITICAL: Include pageSize for page size change detection
            sortColumn: this.sortColumn,  // CRITICAL: Include sort column for sort change detection
            sortDirection: this.sortDirection  // CRITICAL: Include sort direction for sort change detection
          };

          const stateKey = JSON.stringify(finalizedState);
          const lastKey = this.lastExecutedFilterState ? JSON.stringify(this.lastExecutedFilterState) : null;
          const isDifferent = stateKey !== lastKey;

          if (isDifferent) {
            this.lastExecutedFilterState = finalizedState;
            console.debug('[NewsListTableComponent] Filter state changed, executing search', finalizedState);
          } else {
            console.debug('[NewsListTableComponent] Filter state unchanged, skipping search', finalizedState);
          }
          return isDifferent;
        }),
        switchMap(() => {
          console.debug('[NewsListTableComponent] switchMap executing buildSearchQuery()');
          return this.buildSearchQuery().pipe(
            tap((result) => console.debug('[NewsListTableComponent] buildSearchQuery() returned result:', result))
          );
        }),
        takeUntil(this.destroy$)
      )
      .subscribe({
        next: (result) => {
          console.debug('[NewsListTableComponent] Pipeline subscription - next() handler called with result:', result);

          // Handle different response formats
          let articles: NewsItem[] = [];
          let totalItems: number = 0;

          if (result === null) {
            console.warn('[NewsListTableComponent] Result is null, no data to display');
            articles = [];
            totalItems = 0;
          } else if (result?.data?.content) {
            articles = result.data.content;
            totalItems = result.data.totalElements || 0;
            console.debug('[NewsListTableComponent] Result format: data.content, articles=%d', articles.length);
          } else if (result?.content) {
            articles = result.content;
            totalItems = result.totalElements || 0;
            console.debug('[NewsListTableComponent] Result format: direct content, articles=%d', articles.length);
          } else if (Array.isArray(result)) {
            articles = result;
            console.debug('[NewsListTableComponent] Result format: direct array, articles=%d', articles.length);
          } else {
            console.warn('[NewsListTableComponent] Unrecognized result format:', result);
          }

          // Update service with results (even if empty)
          console.debug('[NewsListTableComponent] Calling updateSearchResults with %d articles, %d total items', articles.length, totalItems);
          this.newsListService.updateSearchResults(articles, totalItems);

          this.cdr.markForCheck();
        },
        error: (error) => {
          console.error('[NewsListTableComponent] Pipeline subscription - error() handler called:', error);
          this.cdr.markForCheck();
        },
        complete: () => {
          console.debug('[NewsListTableComponent] Pipeline subscription - complete() called');
        }
      });

    // Load initial data
    // FIX #5: Only call loadArticles() if NO active filters are present
    // If filters are somehow active at init time, skip loading - let search pipeline handle it
    if (!this.hasActiveFilters()) {
      console.debug('[NewsListTableComponent] ngOnInit - No active filters, loading all articles');
      this.loadArticles();
    } else {
      console.debug('[NewsListTableComponent] ngOnInit - Active filters detected at init, skipping loadArticles()');
    }
  }

  /**
   * Get display name for category by ID
   * Maps newsNewsCategoryId to human-readable category name
   * Used in template to display category name instead of ID
   */
  getCategoryNameById(categoryId: string | null): string {
    if (!categoryId) return '—';
    const category = this.availableCategories.find((c) => c.id === categoryId);
    return category ? category.categoryNameEn : categoryId;
  }

  /**
   * GENERIC METHOD: Get admin user display name by ID
   * 
   * Single unified method for all admin user name resolution.
   * Replaces: getAuthorNameForId, getPublisherNameForId, getUpdaterNameForId, 
   *           getSchedulerNameForId, getDeleterNameForId
   * 
   * Industry Standard: DRY (Don't Repeat Yourself) principle
   * Benefits:
   * - Single source of truth
   * - Consistent behavior across all admin user columns
   * - Easy to maintain and extend
   * - Reduces code duplication
   * 
   * @param userId - Admin user ID (can be null)
   * @returns User display name or '—' if not found
   */
  public getAdminUserNameById(userId: string | null): string {
    if (!userId) return '—';
    const user = this.availableAdminUsers.find((u: AdminUser) => u.id === userId);
    return user ? user.name : userId;
  }

  ngOnDestroy(): void {
    console.debug('[NewsListTableComponent] ngOnDestroy - cleaning up subscriptions');
    this.destroy$.next();
    this.destroy$.complete();
  }

  /**
   * Open column customization dialog
   */
  openColumnCustomization(): void {
    console.log('🔧 Opening column customization dialog');
    const currentPreferences = this.columnPreferencesService.getCurrentPreferences();

    const dialogRef = this.dialog.open(ColumnCustomizationDialogComponent, {
      width: DIALOG_CONFIG.COLUMN_CUSTOMIZATION.WIDTH,
      maxWidth: DIALOG_CONFIG.COLUMN_CUSTOMIZATION.MAX_WIDTH,
      minWidth: DIALOG_CONFIG.COLUMN_CUSTOMIZATION.MIN_WIDTH,
      maxHeight: DIALOG_CONFIG.COLUMN_CUSTOMIZATION.MAX_HEIGHT,
      data: { preferences: currentPreferences },
      disableClose: DIALOG_CONFIG.COLUMN_CUSTOMIZATION.DISABLE_CLOSE,
      panelClass: DIALOG_CONFIG.COLUMN_CUSTOMIZATION.PANEL_CLASS,
      hasBackdrop: DIALOG_CONFIG.COLUMN_CUSTOMIZATION.HAS_BACKDROP,
      backdropClass: DIALOG_CONFIG.COLUMN_CUSTOMIZATION.BACKDROP_CLASS,
      autoFocus: false,
      restoreFocus: true
    });

    // Listen for dialog close and refresh if changes were saved
    dialogRef.afterClosed().subscribe((result) => {
      console.log('📋 Dialog closed with result:', result);
      if (result && result.saved) {
        console.log('✅ Column preferences updated, triggering change detection');
        // Preference changes already trigger subscription and markForCheck()
        // But let's also manually trigger for immediate visual feedback
        this.cdr.markForCheck();
      } else {
        console.log('ℹ️ Dialog closed without saving');
      }
    });
  }

  /**
   * Helper: Check if a column should be visible by its ID
   * Used in template with *ngIf to conditionally show columns
   */
  isColumnVisible(columnId: string): boolean {
    return this.visibleColumnIds.includes(columnId);
  }

  /**
   * Load filter data (categories, workflow statuses, admin users)
   * 
   * Called once during component initialization.
   * Loads FRESH data from backend - no caching, always up-to-date.
   * Stores results locally in component for lookups.
   * 
   * ARCHITECTURE:
   * - Each component loads its own fresh data when it needs it
   * - No hidden cache logic or refresh calls needed
   * - Simple, predictable, easy to debug
   * 
   * ERROR HANDLING (Industry Standard):
   * - Categories: IMPORTANT - Show snackbar with retry if fails
   * - Admin Users: NICE-TO-HAVE - Show snackbar, degrade to UUIDs
   * - News: CRITICAL - Block feature with error message
   */
  private loadFilterData(): void {
    console.debug('[NewsListTableComponent] loadFilterData() - loading FRESH master data from backend');

    this.loadCategories();
    this.loadWorkflowStatuses();
    this.loadAdminUsers();
  }

  /**
   * Load categories with error handling and inline error alert
   * 
   * Uses getAllCategories() because:
   * - News list may display articles with soft-deleted categories
   * - Filters/search need ALL categories including inactive ones
   * - Historical data should show category names even if now deleted
   */
  loadCategories(): void {
    console.log('[NewsListTableComponent] 🔵 loadCategories() called');
    this.isLoadingCategories = true;
    this.categoriesLoadError = null;
    
    this.appMasterDataService.getAllCategories()
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => {
          this.isLoadingCategories = false;
          this.categoriesLoaded = true;
          
          if (this.categoriesLoaded && this.authorsLoaded) {
            this.lookupsLoaded = true;
          }
          this.cdr.markForCheck();
        }),
        catchError((error: any) => {
          console.error('[NewsListTableComponent] 🔴 Categories load FAILED:', error);
          this.categoriesLoadError = 'Categories failed to load. Filters may be limited.';
          return of([]);
        })
      )
      .subscribe({
        next: (categories: MasterDataCategory[]) => {
          console.debug('[NewsListTableComponent] ✅ Fresh categories loaded: %d items', categories.length);
          this.availableCategories = categories;
          this.categoriesLoadError = null;
        }
      });
  }

  /**
   * Load workflow statuses  
   */
  private loadWorkflowStatuses(): void {
    this.newsFormService.getWorkflowStatuses()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (statuses: string[]) => {
          console.debug('[NewsListTableComponent] Workflow statuses loaded: %d items', statuses.length);
          this.availableWorkflowStatuses = statuses;
          this.cdr.markForCheck();
        },
        error: (error) => {
          console.error('[NewsListTableComponent] Error loading workflow statuses:', error);
          this.availableWorkflowStatuses = [];
          this.cdr.markForCheck();
        }
      });
  }

  /**
   * Load admin users with error handling and inline error alert
   */
  loadAdminUsers(): void {
    console.log('[NewsListTableComponent] 🔵 loadAdminUsers() called');
    this.isLoadingAdminUsers = true;
    this.adminUsersLoadError = null;
    
    this.appMasterDataService.getAdminUsers()
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => {
          this.isLoadingAdminUsers = false;
          this.authorsLoaded = true;
          
          if (this.categoriesLoaded && this.authorsLoaded) {
            this.lookupsLoaded = true;
          }
          this.cdr.markForCheck();
        }),
        catchError((error: any) => {
          console.error('[NewsListTableComponent] 🔴 Admin users load FAILED:', error);
          this.adminUsersLoadError = 'User names unavailable. IDs will be shown instead.';
          return of([]);
        })
      )
      .subscribe({
        next: (adminUsers: AdminUser[]) => {
          console.debug('[NewsListTableComponent] ✅ Fresh admin users loaded: %d items', adminUsers.length);
          this.availableAdminUsers = adminUsers;
          this.adminUsersLoadError = null;
        }
      });
  }

  /**
   * Dismiss categories error without retrying
   */
  onDismissCategoriesError(): void {
    this.categoriesLoadError = null;
    this.cdr.markForCheck();
  }

  /**
   * Dismiss admin users error without retrying
   */
  onDismissAdminUsersError(): void {
    this.adminUsersLoadError = null;
    this.cdr.markForCheck();
  }

  /**
   * Dismiss news loading error without retrying
   */
  onDismissNewsError(): void {
    this.newsLoadError = null;
    this.newsListService.setError(null);
    this.cdr.markForCheck();
  }

  /**
   * Retry loading news after error
   */
  onRetryLoadNews(): void {
    if (this.hasActiveFilters()) {
      this.filterChange$.next();
    } else {
      this.loadArticles();
    }
  }

  /**
   * Handle filter changes from NewsSearchFiltersComponent
   * 
   * Called when user changes any search or filter criteria.
   * Updates current filter state and performs search with new filters.
   * 
   * SPECIAL HANDLING: When 'clearAll' action is detected,
   * skips search pipeline and calls clearAllFilters() directly
   * for simpler, more reliable behavior.
   * 
   * FIX #4: Scrolls viewport to top when filters change for better UX
   * 
   * @param event - Filter change event containing updated filter state and action type
   */
  /**
   * Handle filter changes from NewsSearchFiltersComponent
   * 
   * Called when user changes any search or filter criteria.
   * Updates current filter state and triggers the RxJS pipeline.
   * 
   * MATCHES CARD VIEW PATTERN:
   * - Simple state update
   * - Emit to Subject
   * - Let pipeline handle execution with debounce/deduplication
   * 
   * @param event - Filter change event containing updated filter state and action type
   */
  onFiltersChanged(event: INewsSearchFiltersChangedEvent): void {
    console.debug('[NewsListTableComponent] onFiltersChanged() - changedFilterType=%s',
      event.changedFilterType);

    // SPECIAL CASE: Clear All action
    if (event.changedFilterType === 'clearAll') {
      console.debug('[NewsListTableComponent] onFiltersChanged() - Detected clearAll action');
      this.clearAllFilters();
      return;
    }

    // NORMAL CASE: Update filter state and trigger pipeline
    console.debug('[NewsListTableComponent] onFiltersChanged() - changedFilterType=%s, Updated state:',
      event.changedFilterType, event.updatedFiltersState);
    this.currentFiltersState = event.updatedFiltersState;

    // Reset to first page when filters change (CRITICAL: must happen before pipeline)
    this.currentPage = 1;
    console.debug('[NewsListTableComponent] onFiltersChanged() - Reset to page 1');

    // Clear search cache when filters change to ensure fresh API calls
    console.debug('[NewsListTableComponent] onFiltersChanged() - Clearing search cache (size=%d)',
      this.searchRequestCache.size);
    this.searchRequestCache.clear();

    // Clear language cache to prevent memory leaks
    this.cardLanguages.clear();

    // Scroll to top for better UX
    window.scrollTo({ top: 0, behavior: 'smooth' });

    // Emit to Subject (RxJS pipeline handles debounce, deduplication, search)
    console.debug('[NewsListTableComponent] onFiltersChanged() - Emitting filterChange$ event');
    this.filterChange$.next();
  }

  /**
   * Helper: Centralized Filter Detection
   * 
   * Returns true if ANY filters/search are active.
   * Used to decide between search API and getAllNews() API.
   * 
   * MATCHES CARD VIEW: Simple logic, no complex state tracking
   */
  private hasActiveFilters(): boolean {
    const hasTextQuery = this.searchTerm?.trim().length > 0;
    const hasStatusFilter = this.filterByWorkflowStatus?.length > 0;
    const hasCategoryFilter = this.selectedCategoryId != null;
    const hasDateFilter = this.fromDate != null || this.toDate != null;
    const hasCreatedByFilter = this.selectedCreatedBy !== null;

    return hasTextQuery || hasStatusFilter || hasCategoryFilter || hasDateFilter || hasCreatedByFilter;
  }

  /**
   * Build and execute search query with caching and error handling.
   * 
   * This method follows the card view best practices:
   * 1. Immutable state capture (prevents race conditions)
   * 2. Cache key creation with all relevant fields
   * 3. Check cache before API call
   * 4. Support for multiple search modes (title, content, multi-field)
   * 5. Return Observable for RxJS pipeline (switchMap will manage it)
   * 
   * @returns Observable that completes after search finishes
   */
  private buildSearchQuery(): Observable<any> {
    // STEP 0: VALIDATE INPUT DATA (Security & Performance)
    this.validateSearchInput();

    // Set loading state BEFORE starting search
    this.newsListService.setLoading(true);
    this.newsListService.setError(null);

    // STEP 1: CAPTURE STATE IMMUTABLY (fixes race conditions)
    const query = this.searchTerm.trim() || null;
    const statuses = this.filterByWorkflowStatus.length > 0
      ? [...this.filterByWorkflowStatus]
      : null;
    const categoryId = this.selectedCategoryId || null;
    const fromDate = this.fromDate ? new Date(this.fromDate) : null;
    const toDate = this.toDate ? new Date(this.toDate) : null;
    const createdBy = this.selectedCreatedBy || null;

    // STEP 2: CHECK IF ANY FILTERS ARE ACTIVE
    const hasAnyFilter = this.hasActiveFilters();

    console.debug('[NewsListTableComponent] buildSearchQuery() - hasAnyFilter=%s, statuses=%s, categoryId=%s',
      hasAnyFilter, statuses, categoryId);

    // SPECIAL CASE: No filters - load all articles
    if (!hasAnyFilter) {
      console.debug('[NewsListTableComponent] buildSearchQuery() - no filters, loading all articles');
      return this.loadArticlesObservable();
    }

    // STEP 3: CREATE CACHE KEY WITH ALL RELEVANT FIELDS
    const queryKey = JSON.stringify({
      query,
      searchMode: this.searchMode, // Include search mode in cache key
      statuses,
      categoryId,
      createdBy,
      fromDate: fromDate?.getTime(),
      toDate: toDate?.getTime(),
      page: this.currentPage,
      pageSize: this.pageSize,
      sortColumn: this.sortColumn,
      sortDirection: this.sortDirection
    });

    // STEP 4: CHECK CACHE
    if (this.searchRequestCache.has(queryKey)) {
      console.debug('[NewsListTableComponent] buildSearchQuery() - returning cached results');
      const cachedData = this.searchRequestCache.get(queryKey);
      return of(cachedData);
    }

    this.lastExecutedQuery = queryKey;

    // STEP 5: SELECT SEARCH METHOD BASED ON SEARCH MODE
    const zeroBasedPage = this.currentPage - 1;
    const searchSortParam = `${this.sortColumn},${this.sortDirection}`;

    console.debug('[NewsListTableComponent] buildSearchQuery() - calling search with mode=%s, query=%s, statuses=%s, categoryId=%s, page=%d, size=%d',
      this.searchMode, query, statuses, categoryId, zeroBasedPage, this.pageSize);

    // Select the correct search endpoint based on search mode
    let searchObservable: Observable<any>;

    if (this.searchMode === 'title' && query) {
      // Search by title only
      searchObservable = this.adminSearchService.searchByTitleOnly(
        query,
        statuses,
        categoryId,
        createdBy,
        fromDate,
        toDate,
        zeroBasedPage,
        this.pageSize,
        searchSortParam
      );
    } else if (this.searchMode === 'content' && query) {
      // Search by content only
      searchObservable = this.adminSearchService.searchByContentOnly(
        query,
        statuses,
        categoryId,
        createdBy,
        fromDate,
        toDate,
        zeroBasedPage,
        this.pageSize,
        searchSortParam
      );
    } else {
      // Default: multi-field search (searches all fields)
      searchObservable = this.adminSearchService.searchMultiField(
        query,
        statuses,
        categoryId,
        createdBy,
        fromDate,
        toDate,
        zeroBasedPage,
        this.pageSize,
        searchSortParam
      );
    }

    // STEP 6: ADD TIMEOUT, CACHING AND SUCCESS HANDLING
    return searchObservable.pipe(
      timeout(this.REQUEST_TIMEOUT_MS),
      tap((response: any) => {
        // Cache the FULL response (with wrapper), not just data
        // This ensures cached data matches the same format as fresh API responses
        this.searchRequestCache.set(queryKey, response);

        // Limit cache size
        if (this.searchRequestCache.size > this.SEARCH_CACHE_MAX_SIZE) {
          const firstKey = this.searchRequestCache.keys().next().value;
          if (firstKey) {
            this.searchRequestCache.delete(firstKey);
          }
        }
      }),
      catchError((error: any) => {
        console.error('[NewsListTableComponent] buildSearchQuery() error:', error);
        const errorMessage = error?.message || 'Search failed';
        this.newsListService.setError(errorMessage);
        return of(null);
      }),
      finalize(() => {
        this.newsListService.setLoading(false);
      })
    );
  }

  /**
   * Validate search input to prevent XSS, DoS, and performance issues
   * Enforces max length constraints and format validation
   */
  private validateSearchInput(): void {
    // Validate search term length (prevent XSS/DoS)
    const searchTerm = this.currentFiltersState.searchTextInput;
    if (searchTerm && searchTerm.trim().length > this.MAX_SEARCH_LENGTH) {
      console.warn('[NewsListTableComponent] Search term exceeds max length (' + this.MAX_SEARCH_LENGTH + 'chars). Truncating.');
      this.currentFiltersState.searchTextInput = searchTerm.substring(0, this.MAX_SEARCH_LENGTH);
    }

    // Validate category ID format (UUID)
    const categoryId = this.currentFiltersState.categoryIdSelected;
    if (categoryId && (categoryId.length > this.MAX_CATEGORY_ID_LENGTH || !/^[a-f0-9\-]*$/.test(categoryId.toLowerCase()))) {
      console.warn('[NewsListTableComponent] Invalid category ID format. Clearing filter.');
      this.currentFiltersState.categoryIdSelected = null;
    }

    // Validate workflow statuses are valid strings
    const statuses = this.currentFiltersState.workflowStatusesSelected;
    if (statuses && !Array.isArray(statuses)) {
      console.warn('[NewsListTableComponent] Invalid statuses format. Clearing filter.');
      this.currentFiltersState.workflowStatusesSelected = [];
    }
  }

  private loadArticlesObservable(): Observable<any> {
    // Return the actual service Observable so the RxJS pipeline waits for it to complete
    const zeroBasedPage = this.currentPage - 1;
    const sortParam = `${this.sortColumn},${this.sortDirection}`;

    console.debug('[NewsListTableComponent] loadArticlesObservable() - loading all articles, page=%d, sortParam=%s', zeroBasedPage, sortParam);

    return this.newsListService.getNews(
      zeroBasedPage,
      this.pageSize,
      this.sortColumn,
      this.sortDirection
    ).pipe(
      tap((response) => console.debug('[NewsListTableComponent] loadArticlesObservable() - response received:', response)),
      catchError((error: any) => {
        console.error('[NewsListTableComponent] loadArticlesObservable() error:', error);
        return of(null);
      })
    );
  }

  // Note: State management is now handled by the service observables.
  // buildSearchQuery() directly returns the service observable,
  // which the RxJS pipeline manages. No manual state updates needed.

  /**
   * Perform search with current filter state (LEGACY - now handled via RxJS buildSearchQuery)
   * 
   * This method remains for backward compatibility but is now replaced by
   * the RxJS-driven buildSearchQuery() which provides:
   * - Debouncing
   * - Deduplication (distinctUntilChanged)
   * - Request cancellation (switchMap)
   * - Result caching
   * - Better error handling
   */
  private performSearch(): void {
    // Now handled by buildSearchQuery() through RxJS pipeline
    this.filterChange$.next();
  }

  /**
   * Clear all filters and reload articles
   * 
   * Resets filter state and loads all articles directly (bypasses search pipeline).
   * Follows the card view pattern for simplicity and reliability.
   * Can be called from search-filters component "Clear Filters" button.
   * 
   * FIX #4: Scrolls viewport to top when filters are cleared for better UX
   */
  clearAllFilters(): void {
    console.debug('[NewsListTableComponent] clearAllFilters() - resetting all filters and reloading');
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
    this.sortColumn = 'newsNewsId'; // Reset sort to default
    this.sortDirection = 'desc';
    this.searchRequestCache.clear(); // Clear cache when filters cleared
    this.cardLanguages.clear(); // MEMORY LEAK FIX: Clear language overrides

    // FIX #4: Scroll to top when filters cleared for better UX
    window.scrollTo({ top: 0, behavior: 'smooth' });

    // Load articles directly (like card view does)
    // Bypass search pipeline for unfiltered loads - simpler and more reliable
    this.loadArticles();
  }

  /**
   * Handle pagination changes with filter awareness
   * 
   * If ANY filters are active, executes search directly
   * Otherwise, calls loadArticles() for default load
   * 
   * UNIFIED HANDLER: All pagination methods (goToFirstPage, goToNextPage, etc)
   * delegate to this method to ensure consistent behavior and filter awareness
   * 
   * FIX #2: Scrolls viewport to top when page changes for better UX
   * FIX #8: Explicit zero-check for totalPages edge case protection
   */
  onPageChange(page: number): void {
    if (this.totalPages === 0 || page < 1 || page > this.totalPages) return;
    this.currentPage = page;

    window.scrollTo({ top: 0, behavior: 'smooth' });

    // Emit to Subject pipeline
    this.filterChange$.next();
  }

  /**
   * Handle page size changes
   * 
   * Resets to first page and triggers appropriate search/load with new page size
   * Clears cache since different page sizes affect result counts
   */
  onPageSizeChange(event: any): void {
    const selectedValue = event.value;

    console.debug('[NewsListTableComponent] onPageSizeChange() - old pageSize=%d, new pageSize=%d',
      this.pageSize, selectedValue);

    this.pageSize = selectedValue;
    this.currentPage = 1;
    this.searchRequestCache.clear();

    // Scroll to top for better UX when page size changes
    window.scrollTo({ top: 0, behavior: 'smooth' });

    // Emit to Subject pipeline
    this.filterChange$.next();
  }

  /**
   * Retry loading articles after an error
   * 
   * Clears error message and reloads from first page
   */
  retryLoadNews(): void {
    console.debug('[NewsListTableComponent] retryLoadNews() - retrying load');
    this.currentPage = 1;
    this.loadArticles();
  }

  /**
   * Dismiss error message without retrying
   * 
   * Called from ErrorAlertComponent when user clicks dismiss.
   * Error state is managed by service observable - will clear on next successful load.
   * 
   * This behavior ensures users are aware that errors persist until resolved,
   * encouraging them to either retry or investigate what went wrong.
   */
  onDismissError(): void {
    console.debug('[NewsListTableComponent] onDismissError() - error notification dismissed by user');
    // Error message will persist in error$ stream until service clears it
    // (on successful load or manual clearance by service)
    // This is intentional design - users can inspect error and retry as needed
  }

  /**
   * Dismiss success message
   */
  onDismissSuccess(): void {
    console.debug('[NewsListTableComponent] onDismissSuccess() - success notification dismissed by user');
    this.newsListService.setSuccess(null);
  }

  /**
   * Load news articles from service with current pagination and sorting
   * 
   * Calls NewsListService.getArticles() with current page, size, sort parameters.
   * Service handles HTTP request and updates observable streams.
   * Component subscribes to streams via ngOnInit lifecycle.
   */
  loadArticles(): void {
    console.debug('[NewsListTableComponent] loadArticles() called - page=%d, pageSize=%d, sort=%s,%s',
      this.currentPage - 1, this.pageSize, this.sortColumn, this.sortDirection);

    // Call service (page is 0-based in backend, UI uses 1-based)
    const zeroBasedPage = this.currentPage - 1;

    this.newsListService.getNews(
      zeroBasedPage,
      this.pageSize,
      this.sortColumn,
      this.sortDirection
    )
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response: PaginatedNewsResponse) => {
          console.debug('[NewsListTableComponent] loadArticles() success - received %d items',
            response?.data?.content?.length || 0);

          // CRITICAL FIX: Actually process the response and update component
          const articles = response?.data?.content || [];
          const totalItems = response?.data?.totalElements || 0;

          // Update service state
          console.debug('[NewsListTableComponent] loadArticles() - updating with %d articles, %d total',
            articles.length, totalItems);
          this.newsListService.updateSearchResults(articles, totalItems);
          this.cdr.markForCheck();
        },
        error: (error: any) => {
          console.error('[NewsListTableComponent] loadArticles() failed:', error);
          // Try again without sort parameter (backend compatibility)
          if (this.sortColumn !== 'newsNewsId') {
            console.debug('[NewsListTableComponent] Retrying without sort parameter');
            this.sortColumn = 'newsNewsId';
            this.sortDirection = 'desc';
            // Retry respecting active filters
            if (this.hasActiveFilters()) {
              this.filterChange$.next(); // Retry with search pipeline
            } else {
              this.loadArticles(); // Retry with direct load
            }
          }
        }
      });
  }

  /**
   * Sort table by column
   * 
   * Toggles sort direction if clicking same column,
   * otherwise sets new column and reset to desc direction
   * 
   * Respects active filters - if filters exist, triggers search pipeline,
   * otherwise loads all articles normally.
   * 
   * @param column - Column name to sort by
   */
  sortBy(column: string): void {
    console.debug('[NewsListTableComponent] sortBy() called - column=%s', column);

    if (this.sortColumn === column) {
      this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortColumn = column;
      this.sortDirection = 'desc';
    }

    this.currentPage = 1;
    window.scrollTo({ top: 0, behavior: 'smooth' });
    this.cardLanguages.clear();

    // Emit to Subject pipeline
    this.filterChange$.next();
  }

  /**
   * Handle row action (Edit)
   * 
   * Navigates to edit page using router with article ID as path parameter.
   * Route: /news/:id/edit
   * 
   * @param article - Article to edit
   */
  onEditArticle(article: NewsItem): void {
    const title = this.currentLanguage === 'en' ? article.newsTitleEn : article.newsTitleEs;
    console.debug('[NewsListTableComponent] onEditArticle() - id=%s, title=%s',
      article.newsNewsId, title);

    this.router.navigate(['/news', article.newsNewsId, 'edit']).catch((error) => {
      console.error('[NewsListTableComponent] Navigation to edit page failed:', error);
    });
  }

  /**
   * Handle row action (Delete)
   * 
   * Shows confirmation dialog before soft deleting (recoverable).
   * On confirmation, calls service to soft delete news item and refreshes table.
   * 
   * @param news - News item to soft delete (recoverable)
   */
  onSoftDeleteNews(news: NewsItem): void {
    const title = this.currentLanguage === 'en' ? news.newsTitleEn : news.newsTitleEs;
    console.debug('[NewsListTableComponent] onSoftDeleteNews() - id=%s, title=%s',
      news.newsNewsId, title);

    this.deleteNewsId = news.newsNewsId;
    this.deleteNewsTitle = title;
    this.deleteDialogOpen = true;
  }

  /**
   * Handle soft delete confirmation
   * 
   * Called when user confirms soft deletion in dialog.
   * Calls service to soft delete article (recoverable), then refreshes table.
   * Respects active filters - if filters exist, triggers search pipeline,
   * otherwise loads all articles normally.
   */
  onConfirmSoftDelete(): void {
    if (!this.deleteNewsId) return;

    console.debug('[NewsListTableComponent] onConfirmSoftDelete() - id=%s', this.deleteNewsId);
    this.deleteDialogLoading = true;

    this.newsListService.softDeleteNews(this.deleteNewsId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          console.debug('[NewsListTableComponent] softDeleteNews() success');
          this.deleteDialogOpen = false;
          this.deleteDialogLoading = false;
          this.deleteNewsId = null;
          this.deleteNewsTitle = null;

          // Refresh table respecting active filters
          this.currentPage = 1; // Reset to first page after delete
          if (this.hasActiveFilters()) {
            this.filterChange$.next(); // Refresh search results
          } else {
            this.loadArticles(); // Refresh all articles
          }
        },
        error: (error) => {
          console.error('[NewsListTableComponent] softDeleteNews() failed:', error);
          this.deleteDialogLoading = false;
          // Dialog stays open - user can retry or cancel
        }
      });
  }

  /**
   * Handle delete dialog cancel
   * 
   * Called when user cancels or dismisses deletion dialog.
   */
  onCancelDelete(): void {
    console.debug('[NewsListTableComponent] onCancelDelete()');
    this.deleteDialogOpen = false;
    this.deleteDialogLoading = false;
    this.deleteNewsId = null;
    this.deleteNewsTitle = null;
  }

  /**
   * Handle row action (Publish)
   * 
   * Changes article status to PUBLISHED.
   * Immediate action without confirmation (publish is safe/reversible).
   * Calls service and refreshes table on success.
   * Respects active filters - if filters exist, triggers search pipeline,
   * otherwise loads all articles normally.
   * 
   * @param article - Article to publish
   */
// (removed duplicate onPublishNews)

  /**
   * Get workflow status configuration for a status value
   * 
   * Looks up status configuration from service cache.
   * If status not found, returns a default fallback config from service.
   * 
   * Used by template to access status badge styling and metadata
   * 
   * @param status - Workflow status value
   * @returns Status configuration object (from cache or fallback)
   */
  getStatusConfig(status: string): WorkflowStatusConfig {
    // First check cache (loaded from service in ngOnInit)
    if (this.statusConfigCache.has(status)) {
      return this.statusConfigCache.get(status)!;
    }

    // Fallback to service (handles unknown statuses gracefully)
    const config = this.workflowStatusService.getStatusConfig(status);

    // Log for debugging badge rendering
    console.debug('[NewsListTableComponent] getStatusConfig(%s):', status, {
      backgroundColor: config.backgroundColor,
      textColor: config.textColor,
      icon: config.icon,
      label: config.label
    });

    return config;
  }

  // ============================================================================
  // THUMBNAIL & MEDIA METHODS
  // ============================================================================

  /**
   * Get thumbnail URL for article with fallback to image card URL
   * 
   * Safely extracts thumbnail from NewsItem with fallback chain:
   * 1. newsThumbnailUrl (primary thumbnail)
   * 2. newsImageCardUrl (card view image fallback)
   * 3. newsImageHeroUrl (hero image fallback)
   * 4. undefined (no image available)
   * 
   * @param newsItem - Article item
   * @returns Thumbnail URL or undefined
   */
  getThumbnailUrl(newsItem: NewsItem): string | undefined {
    return newsItem?.newsThumbnailUrl ||
      newsItem?.newsImageCardUrl ||
      newsItem?.newsImageHeroUrl;
  }

  /**
   * Get readability level class based on score
   * 
   * Readability scores typically range from 0-100:
   * - 0-30: Very difficult (red)
   * - 31-50: Difficult (orange) 
   * - 51-70: Fairly difficult (yellow)
   * - 71-100: Easy (green)
   * 
   * @param score - Readability score (0-100)
   * @returns CSS class name for styling
   */
  getReadabilityLevel(score: number): string {
    if (score <= 30) return 'very-difficult';
    if (score <= 50) return 'difficult';
    if (score <= 70) return 'fairly-difficult';
    return 'easy';
  }

  /**
   * Handle thumbnail image load error
   * 
   * Called when image fails to load. Could be extended to:
   * - Log error metrics
   * - Show placeholder
   * - Retry with fallback URL
   * 
   * @param event - Image error event
   */
  onThumbnailError(event: Event): void {
    console.warn('[NewsListTableComponent] Thumbnail image failed to load', event);
    // Future enhancement: Could implement retry logic or fallback image here
  }

  /**
   * Get sponsor information for display
   * 
   * Extracts and formats sponsor data from NewsItem:
   * - Sponsor name (brand name)
   * - Sponsor logo URL (brand branding)
   * - Sponsor website URL (brand link)
   * 
   * @param newsItem - Article item
   * @returns Sponsor information object or null if not sponsored
   */
  getSponsorInfo(newsItem: NewsItem): { name: string; logo?: string; website?: string } | null {
    if (!newsItem?.newsIsSponsored) {
      return null;
    }
    return {
      name: newsItem.newsSponsorName || 'Sponsored',
      logo: newsItem.newsSponsorLogoUrl,
      website: newsItem.newsSponsorWebsiteUrl
    };
  }

  /**
   * Get premium information for display
   * 
   * Extracts and formats premium content data from NewsItem:
   * - Premium flag (is premium)
   * - Premium tier (SILVER, GOLD, PLATINUM, etc.)
   * 
   * @param newsItem - Article item
   * @returns Premium information object or null if not premium
   */
  getPremiumInfo(newsItem: NewsItem): { tier: string } | null {
    if (!newsItem?.newsIsPremium) {
      return null;
    }
    return {
      tier: newsItem.newsPremiumTier || 'Premium'
    };
  }

  // ============================================================================
  // LANGUAGE SUPPORT - Per-Row Override (matches card view pattern)
  // ============================================================================

  /**
   * Toggle language for a specific article row
   * 
   * Switches between English and Spanish for the given article
   * 
   * @param articleId - Article ID to toggle language for
   */
  toggleRowLanguage(articleId: string): void {
    const current = this.cardLanguages.get(articleId) || this.currentLanguage;
    this.cardLanguages.set(articleId, current === 'en' ? 'es' : 'en');
  }

  /**
   * Set language for a specific article row
   * 
   * @param articleId - Article ID to set language for
   * @param lang - Language to set ('en' or 'es')
   */
  setRowLanguage(articleId: string, lang: 'en' | 'es'): void {
    this.cardLanguages.set(articleId, lang);
  }

  /**
   * Get language for a specific article row
   * 
   * Returns the language for the article, or global default if not set
   * 
   * @param articleId - Article ID to get language for
   * @returns Language ('en' or 'es')
   */
  getRowLanguage(articleId: string): 'en' | 'es' {
    return this.cardLanguages.get(articleId) || this.currentLanguage;
  }

  /**
   * Check if an article row has a custom language override
   * 
   * @param articleId - Article ID to check
   * @returns True if row has custom language override
   */
  hasCustomRowLanguage(articleId: string): boolean {
    const rowLang = this.cardLanguages.get(articleId);
    return rowLang !== undefined && rowLang !== this.currentLanguage;
  }

  /**
   * Get title in the appropriate language for a specific article
   * 
   * Respects per-row language override
   * 
   * @param article - Article to get title for
   * @returns Title in selected language
   */
  getTitle(article: NewsItem): string {
    const lang = this.getRowLanguage(article.newsNewsId);
    return lang === 'en' ? article.newsTitleEn : article.newsTitleEs;
  }

  /**
   * Get ARIA sort direction for a specific column
   * 
   * Returns aria-sort value for accessibility:
   * - 'ascending' - sorted ascending
   * - 'descending' - sorted descending
   * - 'none' - not sorted
   * 
   * @param column - Column name to check
   * @returns ARIA sort value
   */
  getSortDirection(column: string): 'ascending' | 'descending' | 'none' {
    if (this.sortColumn !== column) {
      return 'none';
    }
    return this.sortDirection === 'asc' ? 'ascending' : 'descending';
  }

  /**
   * Get delete dialog message
   * 
   * @returns Formatted delete confirmation message
   */
  getDeleteMessage(): string {
    return `Are you sure you want to delete "${this.deleteNewsTitle}"? This action cannot be undone.`;
  }

  /**
   * Pagination: Go to first page
   * Delegates to unified onPageChange() handler
   */
  goToFirstPage(): void {
    if (this.currentPage === 1) return;
    console.debug('[NewsListTableComponent] goToFirstPage()');
    this.onPageChange(1);
  }

  /**
   * Pagination: Go to previous page
   * Delegates to unified onPageChange() handler
   */
  goToPreviousPage(): void {
    if (this.currentPage <= 1) return;
    console.debug('[NewsListTableComponent] goToPreviousPage() - current=%d', this.currentPage);
    this.onPageChange(this.currentPage - 1);
  }

  /**
   * Pagination: Go to next page
   * Delegates to unified onPageChange() handler
   */
  goToNextPage(): void {
    if (this.currentPage >= this.totalPages) return;
    console.debug('[NewsListTableComponent] goToNextPage() - current=%d, total=%d', this.currentPage, this.totalPages);
    this.onPageChange(this.currentPage + 1);
  }

  /**
   * Pagination: Go to last page
   * Delegates to unified onPageChange() handler
   */
  goToLastPage(): void {
    if (this.currentPage === this.totalPages) return;
    console.debug('[NewsListTableComponent] goToLastPage() - jumping to %d', this.totalPages);
    this.onPageChange(this.totalPages);
  }

  /**
   * Pagination: Go to specific page
   * Delegates to unified onPageChange() handler
   * 
   * @param page - 1-based page number
   */
  goToPage(page: number): void {
    if (page < 1 || page > this.totalPages || page === this.currentPage) return;
    console.debug('[NewsListTableComponent] goToPage() - page=%d', page);
    this.onPageChange(page);
  }

  /**
   * Pagination helper: Check if on first page
   * 
   * @returns true if currentPage === 1
   */
  isFirstPage(): boolean {
    return this.currentPage === 1;
  }

  /**
   * Pagination helper: Check if on last page
   * 
   * @returns true if currentPage === totalPages
   */
  isLastPage(): boolean {
    return this.currentPage >= this.totalPages;
  }

  /**
   * Pagination helper: Get visible page range for pagination controls
   * 
   * Shows max 5 page numbers. If current page is near start/end,
   * extends in other direction.
   * 
   * @returns Array of page numbers to display
   */
  getVisiblePageRange(): number[] {
    const maxPagesShown = 5;

    if (this.totalPages <= maxPagesShown) {
      return Array.from({ length: this.totalPages }, (_, i) => i + 1);
    }

    let startPage = Math.max(1, this.currentPage - 2);
    let endPage = Math.min(this.totalPages, startPage + maxPagesShown - 1);

    // Adjust start if we're at the end
    if (endPage - startPage + 1 < maxPagesShown) {
      startPage = Math.max(1, endPage - maxPagesShown + 1);
    }

    return Array.from({ length: endPage - startPage + 1 }, (_, i) => startPage + i);
  }

  /**
   * Pagination helper: Get result count display text
   * 
   * @returns Text like "Showing 1-20 of 150"
   */
  getResultsText(): string {
    if (this.totalItems === 0) return 'No results';

    const startItem = (this.currentPage - 1) * this.pageSize + 1;
    const endItem = Math.min(this.currentPage * this.pageSize, this.totalItems);

    return `Showing ${startItem}-${endItem} of ${this.totalItems}`;
  }

  /**
   * Track by article ID for ngFor performance optimization
   * 
   * Prevents Angular from destroying/recreating rows unnecessarily
   * 
   * @param index - Row index
   * @param article - Article item
   * @returns Article ID for tracking
   */
  trackByNewsId(index: number, article: NewsItem): string {
    return article.newsNewsId;
  }

  // ============================================================================
  // BULK SELECTION METHODS
  // ============================================================================

  /**
   * Toggle selection of a single article
   * @param articleId - Article ID to toggle
   */
  toggleRowSelection(articleId: string): void {
    if (this.selectedNewsIds.has(articleId)) {
      this.selectedNewsIds.delete(articleId);
      console.debug('[NewsListTableComponent] Deselected article: %s', articleId);
    } else {
      this.selectedNewsIds.add(articleId);
      console.debug('[NewsListTableComponent] Selected article: %s', articleId);
    }
    this.cdr.markForCheck();
  }

  /**
   * Check if a specific article is selected
   * @param articleId - Article ID to check
   * @returns True if article is selected
   */
  isRowSelected(articleId: string): boolean {
    return this.selectedNewsIds.has(articleId);
  }

  /**
   * Toggle select all articles on current page
   */
  toggleSelectAll(): void {
    if (this.isAllSelected) {
      // Deselect all
      this.selectedNewsIds.clear();
      console.debug('[NewsListTableComponent] Deselected all articles');
    } else {
      // Select all
      this.currentNews.forEach((article: NewsItem) => {
        this.selectedNewsIds.add(article.newsNewsId);
      });
      console.debug('[NewsListTableComponent] Selected all %d articles', this.currentNews.length);
    }
    this.cdr.markForCheck();
  }

  /**
   * Clear all selections
   */
  clearSelection(): void {
    this.selectedNewsIds.clear();
    console.debug('[NewsListTableComponent] Cleared all selections');
    this.cdr.markForCheck();
  }

  /**
   * Get array of selected news IDs for bulk operations
   */
  getSelectedNewsIds(): string[] {
    return Array.from(this.selectedNewsIds);
  }

  // ============================================================================
  // BULK ACTIONS
  // ============================================================================

  /**
   * Publish all selected news items
   */
  onBulkPublish(): void {
    const selectedIds = this.getSelectedNewsIds();
    if (selectedIds.length === 0) return;

    console.debug('[NewsListTableComponent] Bulk publish: %d news items', selectedIds.length);
    console.debug('[NewsListTableComponent] Selected news IDs:', selectedIds);

    // Log full article details for verification
    selectedIds.forEach(id => {
      const article = this.currentNews.find(a => a.newsNewsId === id);
      if (article) {
        console.debug('[NewsListTableComponent] Publishing article:', {
          id: article.newsNewsId,
          title: article.newsTitleEn,
          status: article.newsWorkflowStatus,
          exists: true
        });
      } else {
        console.warn('[NewsListTableComponent] Article not found in currentNews:', id);
      }
    });

    let completedCount = 0;
    let successCount = 0;
    const failedIds: string[] = [];

    // Execute publish for each selected article
    selectedIds.forEach(id => {
      console.debug('[NewsListTableComponent] Publishing article with ID: %s', id);
      this.newsListService.publishNews(id)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: () => {
            console.debug('[NewsListTableComponent] Article published: %s', id);
            successCount++;
            completedCount++;
            this.selectedNewsIds.delete(id);

            // After all publishes complete, refresh the table
            if (completedCount === selectedIds.length) {
              console.debug('[NewsListTableComponent] Bulk publish completed: %d success, %d failed', successCount, failedIds.length);
              this.newsListService.setError(null);
              if (failedIds.length > 0) {
                this.newsListService.setSuccess(`✓ Published ${successCount} article${successCount !== 1 ? 's' : ''}`);
              } else {
                this.newsListService.setSuccess(`✓ Published ${successCount} article${successCount !== 1 ? 's' : ''}`);
              }
              this.refreshArticles();
            }
          },
          error: (error: any) => {
            console.error('[NewsListTableComponent] Failed to publish article %s:', id, error);
            completedCount++;
            failedIds.push(id);

            // After all publishes complete (even with errors), refresh the table
            if (completedCount === selectedIds.length) {
              console.debug('[NewsListTableComponent] Bulk publish completed: %d success, %d failed', successCount, failedIds.length);
              if (failedIds.length > 0) {
                this.newsListService.setError(`Failed to publish ${failedIds.length} article(s)`);
              } else {
                this.newsListService.setError(null);
              }
              this.refreshArticles();
            }
          }
        });
    });

    this.cdr.markForCheck();
  }

  /**
   * Soft delete all selected news items with confirmation
   */
  onBulkSoftDeleteNews(): void {
    const selectedIds = this.getSelectedNewsIds();
    if (selectedIds.length === 0) return;

    const confirmMessage = `Soft delete ${selectedIds.length} news item${selectedIds.length !== 1 ? 's' : ''}? This action can be undone.`;
    if (!confirm(confirmMessage)) {
      return;
    }

    console.debug('[NewsListTableComponent] Bulk delete: %d articles', selectedIds.length);
    let completedCount = 0;
    let successCount = 0;
    const failedIds: string[] = [];

    // Execute delete for each selected article
    selectedIds.forEach(id => {
      this.newsListService.softDeleteNews(id)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: () => {
            console.debug('[NewsListTableComponent] Article deleted: %s', id);
            successCount++;
            completedCount++;
            this.selectedNewsIds.delete(id);

            // After all deletes complete, refresh the table
            if (completedCount === selectedIds.length) {
              console.debug('[NewsListTableComponent] Bulk delete completed: %d success, %d failed', successCount, failedIds.length);
              this.newsListService.setError(null);
              if (failedIds.length > 0) {
                this.newsListService.setSuccess(`✓ Deleted ${successCount} article${successCount !== 1 ? 's' : ''}. ${failedIds.length} failed.`);
              } else {
                this.newsListService.setSuccess(`✓ Deleted ${successCount} article${successCount !== 1 ? 's' : ''}`);
              }
              this.refreshArticles();
            }
          },
          error: (error: any) => {
            console.error('[NewsListTableComponent] Failed to delete article %s:', id, error);
            completedCount++;
            failedIds.push(id);

            // After all deletes complete (even with errors), refresh the table
            if (completedCount === selectedIds.length) {
              console.debug('[NewsListTableComponent] Bulk delete completed: %d success, %d failed', successCount, failedIds.length);
              this.newsListService.setError(null);
              if (failedIds.length > 0) {
                this.newsListService.setSuccess(`✓ Deleted ${successCount} article${successCount !== 1 ? 's' : ''}. ${failedIds.length} failed.`);
              } else {
                this.newsListService.setSuccess(`✓ Deleted ${successCount} article${successCount !== 1 ? 's' : ''}`);
              }
              this.refreshArticles();
            }
          }
        });
    });

    this.cdr.markForCheck();
  }

  /**
   * Refresh articles from service based on current filter state
   * Called after bulk operations (publish/delete) to update the table
   */
  private refreshArticles(): void {
    console.debug('[NewsListTableComponent] refreshArticles() - reloading data');
    this.currentPage = 1; // Reset to first page after bulk operation

    // Clear any previous errors before refresh
    this.newsListService.setError(null);

    if (this.hasActiveFilters()) {
      console.debug('[NewsListTableComponent] refreshArticles() - using search pipeline');
      this.filterChange$.next(); // Refresh via search pipeline if filters active
    } else {
      console.debug('[NewsListTableComponent] refreshArticles() - loading all articles');
      this.loadArticles(); // Refresh via standard load if no filters
    }
  }
}
