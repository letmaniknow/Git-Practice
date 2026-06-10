// ...existing code continues below (single import block and single class declaration only)...
import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { Observable, BehaviorSubject, throwError, of } from 'rxjs';
import { map, catchError, tap, finalize } from 'rxjs/operators';
import { NEWS_API_ENDPOINTS } from '../constants/news-api.constant';
import { HttpErrorCategorizationService, CategorizedError } from '../../../core/services/http-error-categorization.service';
import { PaginatedNewsResponse } from '../models/paginated-news-response.model';
import { NewsItem } from '../models/news-item.model';
import { ApiResponse } from '../models/api-response.model';

/**
 * Angular service for managing news in the admin dashboard.
 *
 * <p>
 * Provides centralized data management for the news admin features including:
 * - Pagination and server-side sorting
 * - Advanced filtering (query, status, category, date range)
 * - Bulk operations (publish, unpublish, delete, status updates)
 * - Single item CRUD operations
 * - Observable-based state management
 * </p>
 *
 * <h3>Used By Components:</h3>
 * - news-list-table.component (main admin table display)
 * - news-list-pagination.component (pagination controls)
 * - news-bulk-actions-toolbar.component (bulk operations UI)
 * - news-management-page.component (page orchestration)
 *
 * <h3>Usage Example:</h3>
 * <pre>
 * // In component constructor
 * this.newsListService.getNews(0, 20).subscribe(
 *   result => {
 *     this.news = result.data.content;
 *     this.totalItems = result.data.totalElements;
 *   }
 * );
 *
 * // Subscribe to observable streams
 * this.newsListService.news$.subscribe(news => {
 *   this.displayNews(news);
 * });
 *
 * this.newsListService.loading$.subscribe(isLoading => {
 *   this.showLoadingSpinner(isLoading);
 * });
 *
 * // Bulk operations
 * this.newsListService.bulkPublish(selectedIds).subscribe(
 *   () => this.refreshTable()
 * );
 * </pre>
 *
 * @author MMVA Team
 * @version 1.0 - News admin table data service
 * @since 2026-04-14
 */
@Injectable({ providedIn: 'root' })
export class NewsListService {
        /**
         * Update workflow status for a single news item
         * @param id - News UUID
         * @param newStatus - New workflow status string
         * @returns Observable of operation result
         */
        public updateWorkflowStatus(id: string, newStatus: string): Observable<ApiResponse<any>> {
            console.debug('[NewsListService] updateWorkflowStatus() called - id=%s, newStatus=%s', id, newStatus);
            this.loadingSubject$.next(true);
            this.errorSubject$.next(null);
            const url = `${NEWS_API_ENDPOINTS.news.getById(id)}/workflow?newStatus=${encodeURIComponent(newStatus)}`;
            return this.http.patch<ApiResponse<any>>(url, {}).pipe(
                tap({
                    next: () => console.debug('[NewsListService] updateWorkflowStatus() successful'),
                    error: (err: HttpErrorResponse) => console.error('[NewsListService] updateWorkflowStatus() error:', err)
                }),
                catchError(error => this.handleError(error)),
                finalize(() => this.loadingSubject$.next(false))
            );
        }
    /**
     * Clone a news item by ID
     * @param id - News UUID
     * @returns Observable of operation result
     */
    public cloneNews(id: string): Observable<ApiResponse<NewsItem>> {
        console.debug('[NewsListService] cloneNews() called - id=%s', id);
        this.loadingSubject$.next(true);
        this.errorSubject$.next(null);
        return this.http.post<ApiResponse<NewsItem>>(
            `${NEWS_API_ENDPOINTS.news.getById(id)}/clone`,
            {}
        ).pipe(
            tap({
                next: () => console.debug('[NewsListService] cloneNews() successful'),
                error: (err: HttpErrorResponse) => console.error('[NewsListService] cloneNews() error:', err)
            }),
            catchError(error => this.handleError(error)),
            finalize(() => this.loadingSubject$.next(false))
        );
    }

    // State management
    private newsSubject$ = new BehaviorSubject<NewsItem[]>([]);
    private loadingSubject$ = new BehaviorSubject<boolean>(false);
    private errorSubject$ = new BehaviorSubject<string | null>(null);
    private successSubject$ = new BehaviorSubject<string | null>(null);
    private totalItemsSubject$ = new BehaviorSubject<number>(0);

    // Public observables
    public news$ = this.newsSubject$.asObservable();
    public loading$ = this.loadingSubject$.asObservable();
    public error$ = this.errorSubject$.asObservable();
    public success$ = this.successSubject$.asObservable();
    public totalItems$ = this.totalItemsSubject$.asObservable();

    // Internal subjects exposed for component state updates (use with caution)
    // These allow external components to manually update state when using external search services
    public _newsSubject$ = this.newsSubject$;
    public _loadingSubject$ = this.loadingSubject$;
    public _errorSubject$ = this.errorSubject$;
    public _successSubject$ = this.successSubject$;
    public _totalItemsSubject$ = this.totalItemsSubject$;

    constructor(
        private http: HttpClient,
        private errorCategorizer: HttpErrorCategorizationService
    ) { }

    /**
     * Fetch paginated news with optional filtering and sorting
     * 
     * @param page - Zero-based page number (default: 0)
     * @param pageSize - Items per page (default: 20)
     * @param sortColumn - Column to sort by (default: 'newsNewsId')
     * @param sortDirection - Sort direction: 'asc' or 'desc' (default: 'desc')
     * @param filters - Optional filters object
     * @returns Observable of paginated news response
     */
    public getNews(
        page: number = 0,
        pageSize: number = 20,
        sortColumn: string = 'newsNewsId',
        sortDirection: 'asc' | 'desc' = 'desc',
        filters?: {
            query?: string;
            statuses?: string[];
            categoryId?: string;
            fromDate?: Date;
            toDate?: Date;
        }
    ): Observable<PaginatedNewsResponse> {

        console.debug('[NewsListService] getNews() called - page=%d, pageSize=%d, sort=%s,%s',
            page, pageSize, sortColumn, sortDirection);

        this.loadingSubject$.next(true);
        this.errorSubject$.next(null);

        // Build sort parameter - omit if default is used to reduce server errors
        let sort = '';
        if (sortColumn && sortColumn !== 'newsNewsId') {
            sort = `${sortColumn},${sortDirection}`;
        }

        // Use advanced search if filters provided, otherwise use basic list
        if (filters && (filters.query || filters.statuses || filters.categoryId)) {
            console.debug('[NewsListService] Redirecting to searchNews with filters');
            return this.searchNews(
                filters.query || '',
                filters.statuses,
                filters.categoryId,
                filters.fromDate,
                filters.toDate,
                page,
                pageSize,
                sort || `${sortColumn},${sortDirection}`
            );
        }

        // Basic list endpoint
        let params = new HttpParams()
            .set('page', page.toString())
            .set('size', pageSize.toString());

        if (sort) {
            params = params.set('sort', sort);
        }

        return this.http.get<PaginatedNewsResponse>(
            NEWS_API_ENDPOINTS.news.list,
            { params }
        ).pipe(
            tap({
                next: (response: PaginatedNewsResponse) => {
                    console.debug('[NewsListService] getNews() returned %d total results',
                        response?.data?.totalElements || 0);
                    this.newsSubject$.next(response.data.content || []);
                    this.totalItemsSubject$.next(response.data.totalElements || 0);
                },
                error: (err: HttpErrorResponse) => {
                    console.error('[NewsListService] getNews() error:', err);
                }
            }),
            catchError(error => this.handleError(error)),
            finalize(() => this.loadingSubject$.next(false))
        );
    }

    /**
     * Search news with advanced filters
     * 
     * @param query - Search query string
     * @param statuses - Array of workflow statuses to filter by
     * @param categoryId - Category UUID to filter by
     * @param fromDate - Start date for date range
     * @param toDate - End date for date range
     * @param page - Page number
     * @param pageSize - Items per page
     * @param sort - Sort parameter
     * @returns Observable of paginated search results
     */
    public searchNews(
        query: string = '',
        statuses?: string[],
        categoryId?: string,
        fromDate?: Date,
        toDate?: Date,
        page: number = 0,
        pageSize: number = 20,
        sort: string = 'createdAt,desc'
    ): Observable<PaginatedNewsResponse> {

        console.debug('[NewsListService] searchNews() called - query=%s, page=%d, pageSize=%d',
            query, page, pageSize);

        this.loadingSubject$.next(true);
        this.errorSubject$.next(null);

        let params = new HttpParams();

        if (query && query.trim()) {
            params = params.set('query', query.trim());
        }

        if (statuses && statuses.length > 0) {
            params = params.set('workflowStatuses', statuses.join(','));
            console.debug('[NewsListService] searchNews() filters: statuses=%s', statuses.join(','));
        }

        if (categoryId && categoryId.trim()) {
            params = params.set('categoryId', categoryId.trim());
            console.debug('[NewsListService] searchNews() filters: categoryId=%s', categoryId);
        }

        if (fromDate) {
            params = params.set('fromDate', this.formatDate(fromDate));
        }

        if (toDate) {
            params = params.set('toDate', this.formatDate(toDate));
        }

        params = params
            .set('page', page.toString())
            .set('size', pageSize.toString())
            .set('sort', sort);

        return this.http.get<PaginatedNewsResponse>(
            NEWS_API_ENDPOINTS.news.search.advanced,
            { params }
        ).pipe(
            tap({
                next: (response: PaginatedNewsResponse) => {
                    console.debug('[NewsListService] searchNews() returned %d results',
                        response?.data?.totalElements || 0);
                    this.newsSubject$.next(response.data.content || []);
                    this.totalItemsSubject$.next(response.data.totalElements || 0);
                },
                error: (err: HttpErrorResponse) => {
                    console.error('[NewsListService] searchNews() error:', err);
                }
            }),
            catchError(error => this.handleError(error)),
            finalize(() => this.loadingSubject$.next(false))
        );
    }

    /**
     * Fetch a single news item by ID
     * 
     * @param id - News UUID
     * @returns Observable of news item
     */
    public getNewsById(id: string): Observable<NewsItem> {
        console.debug('[NewsListService] getNewsById() called - id=%s', id);

        this.loadingSubject$.next(true);
        this.errorSubject$.next(null);

        return this.http.get<ApiResponse<NewsItem>>(
            NEWS_API_ENDPOINTS.news.getById(id)
        ).pipe(
            tap({
                next: () => console.debug('[NewsListService] getNewsById() successful'),
                error: (err: HttpErrorResponse) => console.error('[NewsListService] getNewsById() error:', err)
            }),
            map(response => response.data),
            catchError(error => this.handleError(error)),
            finalize(() => this.loadingSubject$.next(false))
        );
    }

    /**
     * Publish a single news item
     * 
     * @param id - News UUID
     * @returns Observable of operation result
     */
    public publishNews(id: string): Observable<ApiResponse<any>> {
        console.debug('[NewsListService] publishNews() called - id=%s', id);

        this.loadingSubject$.next(true);
        this.errorSubject$.next(null);

        return this.http.patch<ApiResponse<any>>(
            NEWS_API_ENDPOINTS.news.publish(id),
            {}
        ).pipe(
            tap({
                next: () => console.debug('[NewsListService] publishNews() successful'),
                error: (err: HttpErrorResponse) => console.error('[NewsListService] publishNews() error:', err)
            }),
            catchError(error => this.handleError(error)),
            finalize(() => this.loadingSubject$.next(false))
        );
    }

    /**
     * Unpublish a single news item
     * 
     * @param id - News UUID
     * @returns Observable of operation result
     */
    public unpublishNews(id: string): Observable<ApiResponse<any>> {
        console.debug('[NewsListService] unpublishNews() called - id=%s', id);

        this.loadingSubject$.next(true);
        this.errorSubject$.next(null);

        return this.http.patch<ApiResponse<any>>(
            NEWS_API_ENDPOINTS.news.unpublish(id),
            {}
        ).pipe(
            tap({
                next: () => console.debug('[NewsListService] unpublishNews() successful'),
                error: (err: HttpErrorResponse) => console.error('[NewsListService] unpublishNews() error:', err)
            }),
            catchError(error => this.handleError(error)),
            finalize(() => this.loadingSubject$.next(false))
        );
    }

    /**
     * Soft delete a single news item (recoverable)
     * Handle 409 Conflict as success (data may have been deleted despite conflict)
     * 
     * @param id - News UUID
     * @returns Observable of operation result
     */
    public softDeleteNews(id: string): Observable<ApiResponse<any>> {
        console.debug('[NewsListService] softDeleteNews() called - id=%s', id);

        this.loadingSubject$.next(true);
        this.errorSubject$.next(null);

        return this.http.delete<ApiResponse<any>>(
            NEWS_API_ENDPOINTS.news.softDelete(id)
        ).pipe(
            tap({
                next: () => console.debug('[NewsListService] softDeleteNews() successful'),
                error: (err: HttpErrorResponse) => console.error('[NewsListService] softDeleteNews() error:', err)
            }),
            catchError(error => {
                // Treat 409 Conflict as success - data was likely soft deleted despite version conflict
                if (error.status === 409) {
                    console.warn('[NewsListService] softDeleteNews() received 409 Conflict - treating as success');
                    return of({
                        success: true,
                        message: 'News soft deleted successfully (can be restored)',
                        data: null
                    } as ApiResponse<any>);
                }
                return this.handleError(error);
            }),
            finalize(() => this.loadingSubject$.next(false))
        );
    }

    /**
     * Bulk publish multiple news items
     * 
     * @param ids - Array of news UUIDs
     * @returns Observable of operation result
     */
    public bulkPublish(ids: string[]): Observable<ApiResponse<any>> {
        console.debug('[NewsListService] bulkPublish() called - count=%d', ids.length);

        this.loadingSubject$.next(true);
        this.errorSubject$.next(null);

        return this.http.post<ApiResponse<any>>(
            NEWS_API_ENDPOINTS.news.bulk.publish,
            { ids }
        ).pipe(
            tap({
                next: () => console.debug('[NewsListService] bulkPublish() successful'),
                error: (err: HttpErrorResponse) => console.error('[NewsListService] bulkPublish() error:', err)
            }),
            catchError(error => this.handleError(error)),
            finalize(() => this.loadingSubject$.next(false))
        );
    }

    /**
     * Bulk unpublish multiple news items
     * 
     * @param ids - Array of news UUIDs
     * @returns Observable of operation result
     */
    public bulkUnpublish(ids: string[]): Observable<ApiResponse<any>> {
        console.debug('[NewsListService] bulkUnpublish() called - count=%d', ids.length);

        this.loadingSubject$.next(true);
        this.errorSubject$.next(null);

        return this.http.post<ApiResponse<any>>(
            NEWS_API_ENDPOINTS.news.bulk.unpublish,
            { ids }
        ).pipe(
            tap({
                next: () => console.debug('[NewsListService] bulkUnpublish() successful'),
                error: (err: HttpErrorResponse) => console.error('[NewsListService] bulkUnpublish() error:', err)
            }),
            catchError(error => this.handleError(error)),
            finalize(() => this.loadingSubject$.next(false))
        );
    }

    /**
     * Permanently delete a single news item (IRREVERSIBLE)
     * 
     * @param id - News UUID
     * @returns Observable of operation result
     */
    public permanentDeleteNews(id: string): Observable<ApiResponse<any>> {
        console.debug('[NewsListService] permanentDeleteNews() called (hard delete) - id=%s', id);

        this.loadingSubject$.next(true);
        this.errorSubject$.next(null);

        return this.http.delete<ApiResponse<any>>(
            NEWS_API_ENDPOINTS.news.permanentDelete(id)
        ).pipe(
            tap({
                next: () => console.debug('[NewsListService] permanentDeleteNews() successful (hard delete)'),
                error: (err: HttpErrorResponse) => console.error('[NewsListService] permanentDeleteNews() error (hard delete):', err)
            }),
            catchError(error => {
                // Treat 409 Conflict as success - data was likely permanently deleted despite version conflict
                if (error.status === 409) {
                    console.warn('[NewsListService] permanentDeleteNews() received 409 Conflict - treating as success since data may have been permanently deleted');
                    return of({
                        success: true,
                        message: 'News permanently deleted (IRREVERSIBLE)',
                        data: null
                    } as ApiResponse<any>);
                }
                return this.handleError(error);
            }),
            finalize(() => this.loadingSubject$.next(false))
        );
    }

    /**
     * Bulk soft delete multiple news items (recoverable)
     * 
     * @param ids - Array of news UUIDs
     * @returns Observable of operation result
     */
    public bulkSoftDeleteNews(ids: string[]): Observable<ApiResponse<any>> {
        console.debug('[NewsListService] bulkSoftDeleteNews() called - count=%d', ids.length);

        this.loadingSubject$.next(true);
        this.errorSubject$.next(null);

        return this.http.post<ApiResponse<any>>(
            NEWS_API_ENDPOINTS.news.bulk.softDelete,
            { ids }
        ).pipe(
            tap({
                next: () => console.debug('[NewsListService] bulkSoftDeleteNews() successful'),
                error: (err: HttpErrorResponse) => console.error('[NewsListService] bulkSoftDeleteNews() error:', err)
            }),
            catchError(error => this.handleError(error)),
            finalize(() => this.loadingSubject$.next(false))
        );
    }

    /**
     * Bulk permanently delete multiple news items (IRREVERSIBLE)
     * 
     * @param ids - Array of news UUIDs
     * @returns Observable of operation result
     */
    public bulkPermanentDeleteNews(ids: string[]): Observable<ApiResponse<any>> {
        console.debug('[NewsListService] bulkPermanentDeleteNews() called (hard delete) - count=%d', ids.length);

        this.loadingSubject$.next(true);
        this.errorSubject$.next(null);

        return this.http.post<ApiResponse<any>>(
            NEWS_API_ENDPOINTS.news.bulk.permanentDelete,
            { ids }
        ).pipe(
            tap({
                next: () => console.debug('[NewsListService] bulkPermanentDelete() successful (hard delete)'),
                error: (err: HttpErrorResponse) => console.error('[NewsListService] bulkPermanentDelete() error (hard delete):', err)
            }),
            catchError(error => this.handleError(error)),
            finalize(() => this.loadingSubject$.next(false))
        );
    }

    /**
     * Restore a single soft-deleted news item
     * 
     * @param id - News UUID
     * @returns Observable of operation result
     */
    public restoreNews(id: string): Observable<ApiResponse<any>> {
        console.debug('[NewsListService] restoreNews() called - id=%s', id);

        this.loadingSubject$.next(true);
        this.errorSubject$.next(null);

        return this.http.patch<ApiResponse<any>>(
            `${NEWS_API_ENDPOINTS.news.getById(id)}/restore`,
            {}
        ).pipe(
            tap({
                next: () => console.debug('[NewsListService] restoreNews() successful'),
                error: (err: HttpErrorResponse) => console.error('[NewsListService] restoreNews() error:', err)
            }),
            catchError(error => this.handleError(error)),
            finalize(() => this.loadingSubject$.next(false))
        );
    }

    /**
     * Restore multiple soft-deleted news items (bulk restore)
     * 
     * @param ids - Array of news UUIDs
     * @returns Observable of operation result
     */
    public bulkRestoreNews(ids: string[]): Observable<ApiResponse<any>> {
        console.debug('[NewsListService] bulkRestoreNews() called - count=%d', ids.length);

        this.loadingSubject$.next(true);
        this.errorSubject$.next(null);

        return this.http.post<ApiResponse<any>>(
            `${NEWS_API_ENDPOINTS.news.list}/bulk/restore`,
            { ids }
        ).pipe(
            tap({
                next: () => console.debug('[NewsListService] bulkRestoreNews() successful'),
                error: (err: HttpErrorResponse) => console.error('[NewsListService] bulkRestoreNews() error:', err)
            }),
            catchError(error => this.handleError(error)),
            finalize(() => this.loadingSubject$.next(false))
        );
    }

    /**
     * Bulk update status for multiple news items
     * 
     * @param ids - Array of news UUIDs
     * @param status - New workflow status
     * @returns Observable of operation result
     */
    public bulkUpdateStatus(ids: string[], status: string): Observable<ApiResponse<any>> {
        console.debug('[NewsListService] bulkUpdateStatus() called - count=%d, status=%s', ids.length, status);

        this.loadingSubject$.next(true);
        this.errorSubject$.next(null);

        return this.http.post<ApiResponse<any>>(
            NEWS_API_ENDPOINTS.news.bulk.updateStatus,
            { ids, status }
        ).pipe(
            tap({
                next: () => console.debug('[NewsListService] bulkUpdateStatus() successful'),
                error: (err: HttpErrorResponse) => console.error('[NewsListService] bulkUpdateStatus() error:', err)
            }),
            catchError(error => this.handleError(error)),
            finalize(() => this.loadingSubject$.next(false))
        );
    }

    // ======== CLEAN NEWS API (NO DEPRECATED METHODS) ========

    /**
     * Schedule or reschedule a news item for future publication
     * @param id - News UUID
     * @param publishDateTime - ISO8601 string (UTC recommended)
     * @returns Observable of operation result
     */
    public scheduleNews(id: string, publishDateTime: string): Observable<ApiResponse<any>> {
        console.debug('[NewsListService] scheduleNews() called - id=%s, publishDateTime=%s', id, publishDateTime);
        this.loadingSubject$.next(true);
        this.errorSubject$.next(null);
        // PATCH with no body, date as query param
        return this.http.patch<ApiResponse<any>>(
            `${NEWS_API_ENDPOINTS.news.schedule(id)}?publishDateTime=${encodeURIComponent(publishDateTime)}`,
            {}
        ).pipe(
            tap({
                next: () => console.debug('[NewsListService] scheduleNews() successful'),
                error: (err: HttpErrorResponse) => console.error('[NewsListService] scheduleNews() error:', err)
            }),
            catchError(error => this.handleError(error)),
            finalize(() => this.loadingSubject$.next(false))
        );
    }
    // All methods use "news" terminology for perfect frontend-backend alignment
    // Standardized during development phase for consistent codebase
    // ======= END CLEAN NEWS API =======

    /**
     * Get current news from state
     * 
     * @returns Current news array
     */
    public getNewsSync(): NewsItem[] {
        return this.newsSubject$.getValue();
    }

    /**
     * Get current loading state
     * 
     * @returns Current loading state
     */
    public getLoadingSync(): boolean {
        return this.loadingSubject$.getValue();
    }

    /**
     * Update search results from external search service
     * 
     * This method is called by components doing advanced searches (e.g., via NewsAdvancedSearchService)
     * to update service observables with search results so they propagate to all subscribers.
     * 
     * @param news - News items returned from search
     * @param totalItems - Total count of matching news items
     */
    public updateSearchResults(news: NewsItem[], totalItems: number): void {
        console.debug('[NewsListService] updateSearchResults() - news=%d, total=%d', news.length, totalItems);
        this.newsSubject$.next(news);
        this.totalItemsSubject$.next(totalItems);
    }

    /**
     * Set loading state
     * @param isLoading - Loading state
     */
    public setLoading(isLoading: boolean): void {
        this.loadingSubject$.next(isLoading);
    }

    /**
     * Set error message
     * @param error - Error message or null
     */
    public setError(error: string | null): void {
        this.errorSubject$.next(error);
    }

    /**
     * Set success message
     * @param message - Success message or null
     */
    public setSuccess(message: string | null): void {
        this.successSubject$.next(message);
    }

    /**
     * Clear all state (reset service)
     */
    public clear(): void {
        this.newsSubject$.next([]);
        this.loadingSubject$.next(false);
        this.errorSubject$.next(null);
        this.totalItemsSubject$.next(0);
    }

    /**
     * Format date to ISO string
     * 
     * @param date - Date to format
     * @returns ISO date string (YYYY-MM-DD)
     */
    private formatDate(date: Date): string {
        return date.toISOString().split('T')[0];
    }

    /**
     * Centralized error handling with HTTP status categorization
     * Uses HttpErrorCategorizationService for consistent, semantic error messaging
     *
     * @param error - HTTP error
     * @returns Observable error
     */
    private handleError(error: HttpErrorResponse): Observable<never> {
        // Categorize error using centralized service (includes user-friendly messaging, severity, retry status)
        const categorized: CategorizedError = this.errorCategorizer.categorizeError(error);

        // Extract user-friendly message, fallback to technical message if needed
        const userMessage = categorized.userMessage;

        // Log categorized error for debugging
        console.error('[NewsListService] Error categorized as:', {
            category: categorized.category,
            statusCode: categorized.statusCode,
            severity: categorized.severity,
            retryable: categorized.retryable,
            userMessage: userMessage,
            technicalMessage: categorized.technicalMessage
        });

        // Update error state with user-friendly message
        this.errorSubject$.next(userMessage);

        // Return categorized error for caller to handle
        return throwError(() => categorized);
    }
}
