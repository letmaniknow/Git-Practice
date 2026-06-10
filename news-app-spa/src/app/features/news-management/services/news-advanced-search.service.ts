import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { map, catchError, finalize, tap } from 'rxjs/operators';
import { NEWS_API_ENDPOINTS } from '../constants/news-api.constant';
import { PaginatedNewsResponse } from '../models/paginated-news-response.model';
import { ApiResponse } from '../models/api-response.model';

/**
 * Represents an admin user as returned by the autocomplete endpoint.
 * Used in the Created By filter for news search.
 */
export interface AdminUser {
  id: string;
  name: string;
  email: string;
  username?: string;
}

/**
 * Angular service for advanced admin news search with server-side filtering.
 *
 * <p>
 * Provides HTTP client abstractions for calling the backend /search-advanced endpoint
 * with support for:
 * - Full-text search (query parameter)
 * - Multiple workflow status filtering (workflowStatuses parameter)
 * - Category filtering (categoryId parameter)
 * - Date range filtering (fromDate, toDate parameters)
 * - Pagination and sorting
 * - Complete news data mapping including media files
 * </p>
 *
 * <h3>Complete Data Mapping (matches NewsItem model):</h3>
 * <pre>
 * Response includes ALL news properties:
 * - Basic: newsNewsId, newsTitleEn/Es, newsContentEn/Es, newsSlug
 * - Media: newsMediaFileName, newsMediaFileType, newsMediaFileSize, newsMediaFileUrl, newsMediaType
 * - Images: newsThumbnailUrl, newsImageCardUrl, newsImageHeroUrl
 * - Metadata: newsNewsCategoryId, newsWorkflowStatus, newsIsActive, newsFeatured, newsIsBreaking
 * - Engagement: newsViewCount, newsShareCount, newsLikeCount, newsCommentCount, newsBookmarkCount
 * - Publishing: newsCreatedAt, newsPublishedAt, newsScheduledPublishAt, newsCreatedBy
 * - SEO: newsMetaDescription, newsKeywords, newsContentFormat
 * </pre>
 *
 * <h3>Workflow Statuses Supported:</h3>
 * DRAFT, SUBMITTED, REVIEWED, APPROVED, SCHEDULED, PUBLISHED, ARCHIVED
 *
 * <h3>Usage Example:</h3>
 * <pre>
 * // Search for published news with pagination
 * this.adminSearchService.searchAdvanced(
 *   'breaking',
 *   ['PUBLISHED', 'SCHEDULED'],
 *   null,
 *   new Date('2026-03-01'),
 *   new Date('2026-03-31'),
 *   0,
 *   10,
 *   'createdAt,desc'
 * ).subscribe(
 *   (results) => {
 *     // Results include complete news items with all media properties
 *     const newsItems = results.data.content;
 *     // Access media: newsItems[0].newsMediaFileName, newsItems[0].newsMediaFileUrl
 *   },
 *   (error) => console.error('Search error:', error)
 * );
 * </pre>
 *
 * @author MMVA Team
 * @version 1.1 - Complete data mapping
 * @since 2026-03-04
 */
@Injectable({ providedIn: 'root' })
export class NewsAdvancedSearchService {

  /**
   * Constructor.
   *
   * @param http Angular HttpClient for HTTP operations
   */
  constructor(private http: HttpClient) {}

  /**
   * Performs advanced search with all available filter criteria.
   *
   * <p>
   * This is the primary method for admin news search combining multiple filter types.
   * All filter criteria are optional (null-safe).
   * </p>
   *
   * @param query              optional free-text search query
   * @param workflowStatuses   optional array of workflow status values
   * @param categoryId         optional category UUID
   * @param createdBy          optional admin user UUID who created the news
   * @param fromDate           optional start date for creation date range
   * @param toDate             optional end date for creation date range
   * @param page               zero-based page number
   * @param size               page size
   * @param sort               sort order (e.g., 'createdAt,desc')
   * @return observable of paginated search results
   */
  searchAdvanced(
    query: string | null,
    workflowStatuses: string[] | null,
    categoryId: string | null,
    createdBy: string | null,
    fromDate: Date | null,
    toDate: Date | null,
    page: number = 0,
    size: number = 10,
    sort: string = 'createdAt,desc'
  ): Observable<PaginatedNewsResponse> {

    // Build query parameters
    let params = new HttpParams();

    if (query && query.trim()) {
      params = params.set('query', query.trim());
    }

    if (workflowStatuses && workflowStatuses.length > 0) {
      // Convert array to comma-separated string for query parameter
      params = params.set('workflowStatuses', workflowStatuses.join(','));
    }

    if (categoryId && categoryId.trim()) {
      params = params.set('categoryId', categoryId.trim());
    }

    if (createdBy && createdBy.trim()) {
      params = params.set('createdBy', createdBy.trim());
    }

    if (fromDate) {
      const fromDateStr = this.formatDateToISO(fromDate);
      params = params.set('fromDate', fromDateStr);
    }

    if (toDate) {
      const toDateStr = this.formatDateToISO(toDate);
      params = params.set('toDate', toDateStr);
    }

    params = params.set('page', page.toString());
    params = params.set('size', size.toString());
    params = params.set('sort', sort);

    console.debug(
      'NewsAdvancedSearchService: Advanced search - query=%s, statuses=%s, categoryId=%s, createdBy=%s, from=%s, to=%s, page=%d, size=%d',
      query,
      workflowStatuses?.join(','),
      categoryId,
      createdBy,
      fromDate?.toISOString(),
      toDate?.toISOString(),
      page,
      size
    );

    return this.http.get<any>(
      NEWS_API_ENDPOINTS.news.search.advanced,
      { params }
    ).pipe(
      tap({
        next: (response: any) => console.debug(
          'AdminNewsSearchService: Search returned %d results',
          response?.data?.totalElements || 0
        ),
        error: (err: HttpErrorResponse) => console.error(
          'AdminNewsSearchService: Search error',
          err
        )
      }),
      map(response => this.extractPaginatedData(response)),
      catchError(error => this.handleError(error))
    );
  }

  /**
   * Multi-field search across all searchable fields (All Fields mode).
   *
   * <p>
   * Searches across multiple content fields simultaneously:
   * - Titles (English and Spanish)
   * - Content (English and Spanish)
   * - Keywords, metadata, tags, slug
   * </p>
   *
   * <p>
   * Uses ES-first/DB-fallback architecture:
   * 1. Attempts Elasticsearch if enabled
   * 2. Falls back to database LIKE search if ES unavailable
   * </p>
   *
   * @param query            optional text search query (searches multiple fields)
   * @param workflowStatuses optional array of workflow status values
   * @param categoryId       optional category UUID
   * @param createdBy        optional admin user UUID who created the news
   * @param fromDate         optional start date for creation date range
   * @param toDate           optional end date for creation date range
   * @param page             zero-based page number
   * @param size             page size
   * @param sort             sort order (e.g., 'createdAt,desc')
   * @return observable of paginated search results across all fields
   */
  searchMultiField(
    query: string | null,
    workflowStatuses: string[] | null,
    categoryId: string | null,
    createdBy: string | null,
    fromDate: Date | null,
    toDate: Date | null,
    page: number = 0,
    size: number = 10,
    sort: string = 'createdAt,desc'
  ): Observable<PaginatedNewsResponse> {

    // Build query parameters
    let params = new HttpParams();

    if (query && query.trim()) {
      params = params.set('query', query.trim());
    }

    if (workflowStatuses && workflowStatuses.length > 0) {
      params = params.set('workflowStatuses', workflowStatuses.join(','));
    }

    if (categoryId && categoryId.trim()) {
      params = params.set('categoryId', categoryId.trim());
    }

    if (createdBy && createdBy.trim()) {
      params = params.set('createdBy', createdBy.trim());
    }

    if (fromDate) {
      const fromDateStr = this.formatDateToISO(fromDate);
      params = params.set('fromDate', fromDateStr);
    }

    if (toDate) {
      const toDateStr = this.formatDateToISO(toDate);
      params = params.set('toDate', toDateStr);
    }

    params = params.set('page', page.toString());
    params = params.set('size', size.toString());
    params = params.set('sort', sort);

    console.debug(
      'AdminNewsSearchService: Multi-field search - query=%s, statuses=%s, categoryId=%s, createdBy=%s, from=%s, to=%s, page=%d, size=%d',
      query,
      workflowStatuses?.join(','),
      categoryId,
      createdBy,
      fromDate?.toISOString(),
      toDate?.toISOString(),
      page,
      size
    );

    return this.http.get<any>(
      NEWS_API_ENDPOINTS.news.search.multiField,
      { params }
    ).pipe(
      tap({
        next: (response: any) => console.debug(
          'AdminNewsSearchService: Multi-field search returned %d results',
          response?.data?.totalElements || 0
        ),
        error: (err: HttpErrorResponse) => console.error(
          'AdminNewsSearchService: Multi-field search error',
          err
        )
      }),
      map(response => this.extractPaginatedData(response)),
      catchError(error => this.handleError(error))
    );
  }

  /**
   * Search by workflow status only.
   *
   * <p>
   * Convenience method for status-only filtering.
   * </p>
   *
   * @param statuses workflow status values to filter by
   * @param page     page number
   * @param size     page size
   * @param sort     sort order
   * @return observable of search results
   */
  searchByStatuses(
    statuses: string[],
    page: number = 0,
    size: number = 10,
    sort: string = 'createdAt,desc'
  ): Observable<PaginatedNewsResponse> {
    return this.searchAdvanced(null, statuses, null, null, null, null, page, size, sort);
  }

  /**
   * Search by query text.
   *
   * <p>
   * Convenience method for text-based search across title and content.
   * </p>
   *
   * @param query free-text search query
   * @param page  page number
   * @param size  page size
   * @param sort  sort order
   * @return observable of search results
   */
  searchByQuery(
    query: string,
    page: number = 0,
    size: number = 10,
    sort: string = 'createdAt,desc'
  ): Observable<PaginatedNewsResponse> {
    return this.searchAdvanced(query, null, null, null, null, null, page, size, sort);
  }

  /**
   * Search by category.
   *
   * <p>
   * Convenience method for category-filtered search.
   * </p>
   *
   * @param categoryId category UUID
   * @param page       page number
   * @param size       page size
   * @param sort       sort order
   * @return observable of search results
   */
  searchByCategory(
    categoryId: string,
    page: number = 0,
    size: number = 10,
    sort: string = 'createdAt,desc'
  ): Observable<PaginatedNewsResponse> {
    return this.searchAdvanced(null, null, categoryId, null, null, null, page, size, sort);
  }

  /**
   * Search by date range.
   *
   * <p>
   * Convenience method for date-range filtered search.
   * </p>
   *
   * @param fromDate creation date range start (inclusive)
   * @param toDate   creation date range end (inclusive)
   * @param page     page number
   * @param size     page size
   * @param sort     sort order
   * @return observable of search results
   */
  searchByDateRange(
    fromDate: Date,
    toDate: Date,
    page: number = 0,
    size: number = 10,
    sort: string = 'createdAt,desc'
  ): Observable<PaginatedNewsResponse> {
    return this.searchAdvanced(null, null, null, null, fromDate, toDate, page, size, sort);
  }

  /**
   * Search by title only (exact news lookup).
   *
   * <p>
   * Searches ONLY title fields for precise news matching.
   * Admin can find ONE specific news item to edit/delete by title.
   * </p>
   *
   * @param query            text search query (title match)
   * @param workflowStatuses optional workflow statuses to filter by
   * @param categoryId       optional category ID to filter by
   * @param createdBy        optional admin user UUID who created the news
   * @param fromDate         optional start date
   * @param toDate           optional end date
   * @param page             page number
   * @param size             page size
   * @param sort             sort order
   * @return observable of search results
   */
  searchByTitleOnly(
    query: string | null,
    workflowStatuses: string[] | null = null,
    categoryId: string | null = null,
    createdBy: string | null = null,
    fromDate: Date | null = null,
    toDate: Date | null = null,
    page: number = 0,
    size: number = 10,
    sort: string = 'createdAt,desc'
  ): Observable<PaginatedNewsResponse> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', sort);

    if (query) params = params.set('query', query);
    if (workflowStatuses && workflowStatuses.length > 0) {
      workflowStatuses.forEach(status => {
        params = params.set('workflowStatuses', status);
      });
    }
    if (categoryId) params = params.set('categoryId', categoryId);
    if (createdBy && createdBy.trim()) {
      params = params.set('createdBy', createdBy.trim());
    }
    if (fromDate) params = params.set('fromDate', this.formatDateToISO(fromDate));
    if (toDate) params = params.set('toDate', this.formatDateToISO(toDate));

    return this.http.get<any>(NEWS_API_ENDPOINTS.news.search.byTitle, { params })
      .pipe(
        tap((response) =>
          console.debug(
            'AdminNewsSearchService: Title-only search returned %d results',
            response?.data?.totalElements || 0
          )
        ),
        map(response => this.extractPaginatedData(response)),
        catchError(error => this.handleError(error))
      );
  }

  /**
   * Search by content/metadata only (topic discovery).
   *
   * <p>
   * Searches content, metadata, keywords, tags (everything except title).
   * Admin can find related news by topic without exact title matches.
   * </p>
   *
   * @param query            text search query (content/topic match)
   * @param workflowStatuses optional workflow statuses to filter by
   * @param categoryId       optional category ID to filter by
   * @param createdBy        optional admin user UUID who created the news
   * @param fromDate         optional start date
   * @param toDate           optional end date
   * @param page             page number
   * @param size             page size
   * @param sort             sort order
   * @return observable of search results
   */
  searchByContentOnly(
    query: string | null,
    workflowStatuses: string[] | null = null,
    categoryId: string | null = null,
    createdBy: string | null = null,
    fromDate: Date | null = null,
    toDate: Date | null = null,
    page: number = 0,
    size: number = 10,
    sort: string = 'createdAt,desc'
  ): Observable<PaginatedNewsResponse> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', sort);

    if (query) params = params.set('query', query);
    if (workflowStatuses && workflowStatuses.length > 0) {
      workflowStatuses.forEach(status => {
        params = params.set('workflowStatuses', status);
      });
    }
    if (categoryId) params = params.set('categoryId', categoryId);
    if (createdBy && createdBy.trim()) {
      params = params.set('createdBy', createdBy.trim());
    }
    if (fromDate) params = params.set('fromDate', this.formatDateToISO(fromDate));
    if (toDate) params = params.set('toDate', this.formatDateToISO(toDate));

    return this.http.get<any>(NEWS_API_ENDPOINTS.news.search.byContent, { params })
      .pipe(
        tap((response) =>
          console.debug(
            'AdminNewsSearchService: Content-only search returned %d results',
            response?.data?.totalElements || 0
          )
        ),
        map(response => this.extractPaginatedData(response)),
        catchError(error => this.handleError(error))
      );
  }

  /**
   * Extracts paginated data from API response.
   *
   * <p>
   * Handles different response formats and normalizes to PaginatedNewsResponse.
   * </p>
   *
   * @param response raw HTTP response
   * @return normalized PaginatedNewsResponse
   */
  private extractPaginatedData(response: any): PaginatedNewsResponse {
    // The response should have a 'data' property with pagination info
    const paginationData = response?.data || response;

    return {
      status: response?.status || 'success',
      message: response?.message || '',
      timestamp: response?.timestamp || new Date().toISOString(),
      data: {
        content: paginationData?.content || paginationData?.news || [],
        pageable: paginationData?.pageable || {},
        totalElements: paginationData?.totalElements || 0,
        totalPages: paginationData?.totalPages || 0,
        last: paginationData?.last || false,
        size: paginationData?.pageSize || paginationData?.size || 10,
        number: paginationData?.currentPage || paginationData?.number || 0,
        sort: paginationData?.sort || {},
        numberOfElements: (paginationData?.content || []).length,
        first: (paginationData?.currentPage || paginationData?.number || 0) === 0,
        empty: (paginationData?.content || []).length === 0
      }
    };
  }

  /**
   * Formats a Date object to ISO 8601 date string (YYYY-MM-DD).
   *
   * @param date date to format
   * @return ISO 8601 date string
   */
  private formatDateToISO(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  /**
   * Handles HTTP errors.
   *
   * @param error the HTTP error
   * @return observable error
   */
  private handleError(error: HttpErrorResponse) {
    console.error('AdminNewsSearchService: HTTP error', error);

    let errorMessage = 'An error occurred during search';
    if (error.error instanceof ErrorEvent) {
      // Client-side error
      errorMessage = `Error: ${error.error.message}`;
    } else {
      // Server-side error
      errorMessage = `Error Code: ${error.status}\nMessage: ${error.message}`;
    }

    return throwError(() => new Error(errorMessage));
  }

  /**
   * Fetch active admin users for autocomplete dropdown.
   *
   * <p>
   * Retrieves list of active admin users from adminuser table.
   * Used for Created By filter autocomplete in search UI.
   * </p>
   *
   * @param query optional search query (searches name, username, email)
   * @return observable of admin user array
   */
  getAdminUsers(query: string = ''): Observable<any[]> {
    let params = new HttpParams();
    
    // Always include query parameter - empty string for "match all", or search term
    params = params.set('query', query);
    
    // Add pagination parameters for consistency
    params = params.set('page', '0');
    params = params.set('size', '50'); // Reasonable default for autocomplete

    console.debug('AdminNewsSearchService: Fetching admin users', query ? `with query: "${query}"` : '(all) with empty query');

    return this.http.get<any>(
      NEWS_API_ENDPOINTS.staff.autocomplete,
      { params }
    ).pipe(
      map((response: any) => {
        // Backend returns ApiResponseDto<Page<AdminUserResponseDto>>
        // Extract the content array from the paginated response
        if (Array.isArray(response)) {
          return response;
        }
        // Handle paginated response from backend
        const pageData = response?.data || response;
        const content = pageData?.content || [];
        
        // Map backend field names to frontend expected names
        return content.map((admin: any) => ({
          id: admin.adminUsersId,
          adminId: admin.adminUsersId,
          name: admin.adminUsersFullName || admin.adminUsersUsername,
          fullName: admin.adminUsersFullName,
          email: admin.adminUsersEmail,
          username: admin.adminUsersUsername,
          phone: admin.adminUsersPhoneNumber,
          avatarUrl: admin.adminUsersAvatarUrl,
          status: admin.adminUsersStatus,
          // Keep original data for reference
          ...admin
        }));
      }),
      tap((users: any[]) => {
        console.debug('AdminNewsSearchService: Fetched', users.length, 'admin users');
      }),
      catchError((error: HttpErrorResponse) => {
        console.error('AdminNewsSearchService: Error fetching admin users', error);
        return throwError(() => new Error('Failed to fetch admin users'));
      })
    );
  }
}
