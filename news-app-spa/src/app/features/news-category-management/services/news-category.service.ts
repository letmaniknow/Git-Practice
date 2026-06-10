/**
 * News Category Service - HTTP Client & State Management
 * Purpose: Handle all HTTP communication with backend + reactive state management
 * Pattern: Follows admin-user.service.ts structure (BehaviorSubjects for state)
 * Cache Strategy: ADMIN_CACHE, ADMIN_BY_ID_CACHE, ADMIN_LIST_CACHE (per backend)
 */

import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, BehaviorSubject, throwError } from 'rxjs';
import { map, tap, catchError } from 'rxjs/operators';

import {
  NewsCategory,
  NewsCategoryRequestDto,
  NewsCategoryResponseDto,
  NewsCategoryViewModel,
  NewsCategoryPaginatedResponse,
  NewsCategoryPaginationParams,
  ApiResponse,
} from '../models/news-category.model';

import {
  NEWS_CATEGORY_API_ENDPOINTS,
  NEWS_CATEGORY_PAGINATION,
} from '../constants/news-category-api.constant';

/**
 * NewsCategoryService - Provides all news category operations
 * Manages HTTP requests and reactive state via BehaviorSubjects
 */
@Injectable({
  providedIn: 'root',
})
export class NewsCategoryService {
  /**
   * State Management - BehaviorSubjects for reactive updates
   */
  private categoriesSubject$ = new BehaviorSubject<NewsCategory[]>([]);
  private loadingSubject$ = new BehaviorSubject<boolean>(false);
  private errorSubject$ = new BehaviorSubject<string | null>(null);
  private totalCountSubject$ = new BehaviorSubject<number>(0);
  private currentPageSubject$ = new BehaviorSubject<number>(0);

  /**
   * Public Observables - Exposed to components via $ suffix convention
   */
  public categories$ = this.categoriesSubject$.asObservable();
  public loading$ = this.loadingSubject$.asObservable();
  public error$ = this.errorSubject$.asObservable();
  public totalCount$ = this.totalCountSubject$.asObservable();
  public currentPage$ = this.currentPageSubject$.asObservable();

  constructor(private http: HttpClient) {}

  /**
   * ========== PUBLIC STATE ACCESSORS ==========
   */

  /**
   * Get current categories from state (useful for quick access without subscription)
   */
  public getCategories(): NewsCategory[] {
    return this.categoriesSubject$.getValue();
  }

  /**
   * Get current loading state
   */
  public isLoading(): boolean {
    return this.loadingSubject$.getValue();
  }

  /**
   * Get current error message
   */
  public getError(): string | null {
    return this.errorSubject$.getValue();
  }

  /**
   * Get current total count
   */
  public getTotalCount(): number {
    return this.totalCountSubject$.getValue();
  }

  /**
   * Get current page number
   */
  public getCurrentPage(): number {
    return this.currentPageSubject$.getValue();
  }

  /**
   * ========== CRUD OPERATIONS ==========
   */

  /**
   * Load all categories with pagination from backend
   * Updates state: categories, totalCount, currentPage, loading
   * @param params - Pagination parameters (page, pageSize, sortBy, sortDirection, includeDeleted)
   * @param includeDeleted - If true, includes soft-deleted categories (default: false)
   */
  /**
   * loadCategoriesForManagement - Retrieves ACTIVE and INACTIVE categories (or ALL if includeDeleted=true)
   * Used for admin category management, not for public browsing
   * @param params pagination, sorting, and filter parameters
   * @returns Observable of paginated categories
   */
  public loadCategoriesForManagement(
    params?: Partial<NewsCategoryPaginationParams> & { includeDeleted?: boolean }
  ): Observable<NewsCategoryPaginatedResponse> {
    this.loadingSubject$.next(true);
    this.errorSubject$.next(null);

    // Merge defaults with provided params
    const paginationParams = {
      page: params?.page ?? NEWS_CATEGORY_PAGINATION.DEFAULT_PAGE,
      pageSize:
        params?.pageSize ?? NEWS_CATEGORY_PAGINATION.DEFAULT_PAGE_SIZE,
      sortBy: params?.sortBy ?? NEWS_CATEGORY_PAGINATION.DEFAULT_SORT_BY,
      sortDirection:
        params?.sortDirection ??
        NEWS_CATEGORY_PAGINATION.DEFAULT_SORT_DIRECTION,
      searchTerm: params?.searchTerm ?? '',
      includeDeleted: params?.includeDeleted ?? false,
    };

    // Build HTTP params - match backend parameter names
    const sortParam = `${paginationParams.sortBy},${paginationParams.sortDirection}`;
    const httpParams = new HttpParams()
      .set('page', paginationParams.page.toString())
      .set('size', paginationParams.pageSize.toString())
      .set('sort', sortParam)
      .set('statusFilter', 'ACTIVE_INACTIVE')
      .set('includeDeleted', paginationParams.includeDeleted.toString());

    return this.http
      .get<ApiResponse<NewsCategoryPaginatedResponse>>(
        NEWS_CATEGORY_API_ENDPOINTS.LIST,
        { params: httpParams }
      )
      .pipe(
        map((response) => response.data),
        tap((data) => {
          this.updateStateFromResponse(data, paginationParams.page);
        }),
        tap(() => this.loadingSubject$.next(false)),
        catchError((error) => this.handleError(error))
      );
  }

  /**
   * Get single category by ID
   * @param id - Category UUID
   * @returns Observable of NewsCategory
   */
  public getCategoryById(id: string): Observable<NewsCategory> {
    return this.http
      .get<ApiResponse<NewsCategoryResponseDto>>(
        NEWS_CATEGORY_API_ENDPOINTS.DETAIL(id)
      )
      .pipe(
        map((response) => this.mapResponseToModel(response.data)),
        catchError((error) => this.handleError(error))
      );
  }

  /**
   * Create new category
   * @param request - Category creation data
   * @returns Observable of created NewsCategory
   */
  public createCategory(
    request: NewsCategoryRequestDto
  ): Observable<NewsCategory> {
    this.loadingSubject$.next(true);
    this.errorSubject$.next(null);

    return this.http
      .post<ApiResponse<NewsCategoryResponseDto>>(
        NEWS_CATEGORY_API_ENDPOINTS.CREATE,
        request
      )
      .pipe(
        map((response) => this.mapResponseToModel(response.data)),
        tap(() => this.loadingSubject$.next(false)),
        catchError((error) => {
          this.loadingSubject$.next(false);
          return this.handleError(error);
        })
      );
  }

  /**
   * Update existing category
   * @param id - Category UUID
   * @param request - Updated category data
   * @returns Observable of updated NewsCategory
   */
  public updateCategory(
    id: string,
    request: NewsCategoryRequestDto
  ): Observable<NewsCategory> {
    this.loadingSubject$.next(true);
    this.errorSubject$.next(null);

    return this.http
      .put<ApiResponse<NewsCategoryResponseDto>>(
        NEWS_CATEGORY_API_ENDPOINTS.UPDATE(id),
        request
      )
      .pipe(
        map((response) => this.mapResponseToModel(response.data)),
        tap(() => this.loadingSubject$.next(false)),
        catchError((error) => {
          this.loadingSubject$.next(false);
          return this.handleError(error);
        })
      );
  }

  /**
   * Delete category (soft-delete via backend)
   * @param id - Category UUID
   * @returns Observable of void
   */
  public deleteCategory(id: string): Observable<void> {
    this.loadingSubject$.next(true);
    this.errorSubject$.next(null);

    return this.http
      .delete<ApiResponse<void>>(NEWS_CATEGORY_API_ENDPOINTS.DELETE(id))
      .pipe(
        map(() => undefined),
        tap(() => this.loadingSubject$.next(false)),
        catchError((error) => {
          this.loadingSubject$.next(false);
          return this.handleError(error);
        })
      );
  }

  /**
   * Activate category (change status to ACTIVE)
   * @param id - Category UUID
   * @returns Observable of updated NewsCategory
   */
  public activateCategory(id: string): Observable<NewsCategory> {
    this.loadingSubject$.next(true);
    this.errorSubject$.next(null);

    return this.http
      .patch<ApiResponse<NewsCategoryResponseDto>>(
        `${NEWS_CATEGORY_API_ENDPOINTS.BASE}/${id}/activate`,
        {}
      )
      .pipe(
        map((response) => this.mapResponseToModel(response.data)),
        tap(() => this.loadingSubject$.next(false)),
        catchError((error) => {
          this.loadingSubject$.next(false);
          return this.handleError(error);
        })
      );
  }

  /**
   * Deactivate category (change status to INACTIVE)
   * @param id - Category UUID
   * @returns Observable of updated NewsCategory
   */
  public deactivateCategory(id: string): Observable<NewsCategory> {
    this.loadingSubject$.next(true);
    this.errorSubject$.next(null);

    return this.http
      .patch<ApiResponse<NewsCategoryResponseDto>>(
        `${NEWS_CATEGORY_API_ENDPOINTS.BASE}/${id}/deactivate`,
        {}
      )
      .pipe(
        map((response) => this.mapResponseToModel(response.data)),
        tap(() => this.loadingSubject$.next(false)),
        catchError((error) => {
          this.loadingSubject$.next(false);
          return this.handleError(error);
        })
      );
  }

  /**
   * Restore deleted category
   * @param id - Category UUID
   * @returns Observable of restored NewsCategory
   */
  public restoreCategory(id: string): Observable<NewsCategory> {
    this.loadingSubject$.next(true);
    this.errorSubject$.next(null);

    return this.http
      .post<ApiResponse<NewsCategoryResponseDto>>(
        `${NEWS_CATEGORY_API_ENDPOINTS.BASE}/${id}/restore`,
        {}
      )
      .pipe(
        map((response) => this.mapResponseToModel(response.data)),
        tap(() => this.loadingSubject$.next(false)),
        catchError((error) => {
          this.loadingSubject$.next(false);
          return this.handleError(error);
        })
      );
  }

  /**
   * Get audit logs for category
   * @param id - Category UUID
   * @returns Observable of audit logs array
   */
  public getAuditLogs(id: string): Observable<any[]> {
    return this.http
      .get<ApiResponse<any[]>>(
        `${NEWS_CATEGORY_API_ENDPOINTS.BASE}/${id}/audit-logs`
      )
      .pipe(
        map((response) => response.data),
        catchError((error) => this.handleError(error))
      );
  }

  /**
   * Check if English name already exists (duplicate check for async validator)
   * @param name - Category name in English to check
   * @param excludeId - Optional: Category ID to exclude from check (for edit mode)
   * @returns Observable<boolean> - true if name exists (is a duplicate)
   */
  public checkDuplicateNameEn(name: string, excludeId?: string): Observable<boolean> {
    const categories = this.getCategories();
    const isDuplicate = categories.some(
      (cat) =>
        cat.newsCategoriesNameEn.toLowerCase() === name.toLowerCase() &&
        (!excludeId || cat.newsCategoriesId !== excludeId)
    );
    return new Observable((observer) => {
      observer.next(isDuplicate);
      observer.complete();
    });
  }

  /**
   * Check if Spanish name already exists (duplicate check for async validator)
   * @param name - Category name in Spanish to check
   * @param excludeId - Optional: Category ID to exclude from check (for edit mode)
   * @returns Observable<boolean> - true if name exists (is a duplicate)
   */
  public checkDuplicateNameEs(name: string, excludeId?: string): Observable<boolean> {
    const categories = this.getCategories();
    const isDuplicate = categories.some(
      (cat) =>
        cat.newsCategoriesNameEs.toLowerCase() === name.toLowerCase() &&
        (!excludeId || cat.newsCategoriesId !== excludeId)
    );
    return new Observable((observer) => {
      observer.next(isDuplicate);
      observer.complete();
    });
  }

  /**
   * ========== UTILITY METHODS ==========
   */

  /**
   * Clear all state (logout, reset, etc.)
   */
  public clearState(): void {
    this.categoriesSubject$.next([]);
    this.loadingSubject$.next(false);
    this.errorSubject$.next(null);
    this.totalCountSubject$.next(0);
    this.currentPageSubject$.next(0);
  }

  /**
   * Map backend response DTO to frontend model
   * No transformation needed - backend and frontend use same field names
   */
  private mapResponseToModel(dto: NewsCategoryResponseDto): NewsCategory {
    return dto as NewsCategory;
  }

  /**
   * Update internal state from paginated response
   * Called by loadCategories() after successful API call
   */
  private updateStateFromResponse(
    response: NewsCategoryPaginatedResponse,
    currentPage: number
  ): void {
    this.categoriesSubject$.next(response.content);
    this.totalCountSubject$.next(response.totalElements);
    this.currentPageSubject$.next(currentPage);
  }

  /**
   * Centralized error handling
   * Extracts error message from various error response formats
   */
  private handleError(error: any): Observable<never> {
    let errorMessage = 'An error occurred while processing your request';

    // Extract message from different error response structures
    if (error?.error?.message) {
      errorMessage = error.error.message;
    } else if (error?.message) {
      errorMessage = error.message;
    } else if (error?.statusText) {
      errorMessage = error.statusText;
    }

    this.errorSubject$.next(errorMessage);
    return throwError(() => new Error(errorMessage));
  }
}
