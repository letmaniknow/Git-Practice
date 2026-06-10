import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { Observable, throwError, shareReplay } from 'rxjs';
import { tap, catchError, map } from 'rxjs/operators';
import { NEWS_API_ENDPOINTS } from '../constants/news-api.constant';
import { NewsCategory } from '../models/news-form.model';
import { PaginatedNewsResponse } from '../models/paginated-news-response.model';
import { NewsItem } from '../models/news-item.model';
import { ApiResponse } from '../models/api-response.model';
import { NewsMapper } from './news.mapper';
import { NewsWorkflowStatusService } from './news-workflow-status.service';
import { NewsAuditLogDto } from '../../dashboard-management/news/models/dashboard-news.model';

@Injectable({ providedIn: 'root' })
export class NewsFormService {
  private workflowStatusesCache$: Observable<string[]> | null = null;

  constructor(
    private http: HttpClient,
    private workflowStatusService: NewsWorkflowStatusService
  ) {}

  /**
   * getActiveCategoriesForNewsCreation - Retrieves only ACTIVE categories for news creation forms
   * Backend applies statusFilter=ACTIVE to return ACTIVE categories only
   * @returns Observable of ACTIVE categories only
   */
  getActiveCategoriesForNewsCreation(): Observable<NewsCategory[]> {
    return this.http.get<any>(NEWS_API_ENDPOINTS.categories.list, {
      params: new HttpParams()
        .set('page', '0')
        .set('size', '100')  // Max allowed by backend validation
        .set('statusFilter', 'ACTIVE')
    })
      .pipe(
        tap({
          next: (data: any) => console.log('ACTIVE categories loaded for news creation:', data),
          error: (err: any) => console.error('Error loading ACTIVE categories:', err)
        }),
        map(response => {
          let categories = response?.data?.content || [];
          return categories.map((cat: any) => ({
            id: cat.newsCategoriesId,
            categoryNameEn: cat.newsCategoriesNameEn,
            categoryNameEs: cat.newsCategoriesNameEs,
            slug: cat.newsCategoriesSlug,
            description: cat.newsCategoriesDescription
          }));
        })
      );
  }

  getWorkflowStatuses(): Observable<string[]> {
    // Use cached Observable to prevent duplicate API calls
    if (!this.workflowStatusesCache$) {
      console.debug('[NewsFormService] Loading workflow statuses from API (first time)');
      this.workflowStatusesCache$ = this.http
        .get<ApiResponse<any>>(NEWS_API_ENDPOINTS.news.workflowStatuses)
        .pipe(
          tap(response => {
            console.debug('[NewsFormService] API response received:', response);
          }),
          map(response => {
            // Extract status strings from the response
            // Handle both possible response formats: array directly or nested in data.content
            const statuses = Array.isArray(response?.data)
              ? response.data.map((item: any) => item.status || item)
              : Array.isArray(response?.data?.content)
              ? response.data.content.map((item: any) => item.status || item)
              : [];
            console.debug('[NewsFormService] Extracted statuses:', statuses);
            return statuses;
          }),
          catchError(err => {
            console.warn('[NewsFormService] Failed to load workflow statuses:', err);
            this.workflowStatusesCache$ = null; // Clear cache on error to retry next time
            return throwError(() => err);
          }),
          shareReplay(1) // Cache the result, multiple subscribers share same response
        );
    }
    return this.workflowStatusesCache$;
  }

  createNews(formData: FormData): Observable<NewsItem> {
    return this.http.post<any>(NEWS_API_ENDPOINTS.news.create, formData).pipe(
      map(response => NewsMapper.fromDto(response.data ? response.data : response)),
      tap(news => console.log('[createNews] News created:', news)),
      catchError((error) => this.handleError(error))
    );
  }

  // CRUD stubs for future implementation
 // Test backend connectivity
  testConnection(): Observable<boolean> {
    return this.http.get<any>(NEWS_API_ENDPOINTS.news.list).pipe(
      map(() => true),
      catchError(() => {
        console.error('Backend connectivity test failed');
        return throwError(() => new Error('Cannot connect to backend'));
      })
    );
  }

  // Get all news with pagination
  getAllNews(page: number = 1, size: number = 10): Observable<PaginatedNewsResponse> {
  const pageIndex = page - 1;
  const params = { page: pageIndex, size };
  return this.http.get<any>(NEWS_API_ENDPOINTS.news.list, { params })
    .pipe(
      map(response => response.data)
    );
}

  // Get single news by ID
  getNewsById(id: string): Observable<NewsItem> {
    return this.http.get<any>(NEWS_API_ENDPOINTS.news.getById(id)).pipe(
      map(response => {
        console.log('[getNewsById] Response received:', response);
        const dto = response?.data ? response.data : response;
        return NewsMapper.fromDto(dto);
      }),
      tap(news => console.log('[getNewsById] News mapped:', news)),
      catchError((error) => this.handleError(error))
    );
  }

  
  //

  // Update existing news - Backend only supports POST method for updates
  updateNews(id: string, formData: FormData): Observable<NewsItem> {
    return this.http.put<any>(NEWS_API_ENDPOINTS.news.update(id), formData).pipe(
      map(response => {
        console.log('[updateNews] Response received:', response);
        const dto = response?.data ? response.data : response;
        return NewsMapper.fromDto(dto);
      }),
      tap(news => console.log('[updateNews] News updated:', news)),
      catchError((error) => this.handleError(error))
    );
  }

  // Get audit logs for a news article
  getNewsAuditLogs(newsId: string): Observable<NewsAuditLogDto[]> {
    return this.http.get<ApiResponse<NewsAuditLogDto[]>>(NEWS_API_ENDPOINTS.news.auditLogs(newsId)).pipe(
      map(response => response.data || []),
      tap(logs => console.log('[getNewsAuditLogs] Audit logs retrieved:', logs)),
      catchError((error) => this.handleError(error))
    );
  }

  // Delete news - Backend expects GET method for delete operations
  deleteNews(id: string): Observable<boolean> {
    return this.http.delete<any>(NEWS_API_ENDPOINTS.news.delete(id)).pipe(
      map(response => {
        console.log('Delete news response:', response);
        
        if (response && typeof response.success === 'boolean') {
          return response.success;
        } else if (response && response.message) {
          // Some APIs return success in message
          return true;
        } else {
          // If no specific success indicator, assume success if no error
          return true;
        }
      }),
      catchError((error) => this.handleError(error))
    );
  }

  // Get media file by filename
  getMediaFile(filename: string): Observable<Blob> {
    return this.http.get(NEWS_API_ENDPOINTS.news.media, {
      params: { filename },
      responseType: 'blob'
    }).pipe(
      catchError((error) => this.handleError(error))
    );
  }

  // Get media file URL for direct access with inline display
  getMediaFileUrl(filename: string): string {
    return `${NEWS_API_ENDPOINTS.news.media}/${encodeURIComponent(filename)}`;
  }

  // Get media file as blob for video streaming
  getMediaFileBlob(filename: string): Observable<Blob> {
    return this.http.get(NEWS_API_ENDPOINTS.news.mediaFile(filename), {
      params: { 
        filename,
        inline: 'true'  // Request inline display
      },
      responseType: 'blob',
      headers: {
        'Accept': 'video/*,image/*,*/*',
        'Cache-Control': 'no-cache'
      }
    }).pipe(
      catchError((error) => this.handleError(error))
    );
  }

  // Publish news
  publishNews(id: string): Observable<NewsItem> {
    return this.http.patch<any>(NEWS_API_ENDPOINTS.news.publish(id), {}).pipe(
      map(response => NewsMapper.fromDto(response.data ? response.data : response)),
      tap(news => console.log('[publishNews] News published:', news)),
      catchError((error) => this.handleError(error))
    );
  }

  // Unpublish news
  unpublishNews(id: string): Observable<NewsItem> {
    return this.http.patch<any>(NEWS_API_ENDPOINTS.news.unpublish(id), {}).pipe(
      map(response => NewsMapper.fromDto(response.data ? response.data : response)),
      tap(news => console.log('[unpublishNews] News unpublished:', news)),
      catchError((error) => this.handleError(error))
    );
  }

  // Error handler - Preserves RFC 7807 response with fieldErrors
  private handleError(error: any): Observable<never> {
    let errorMessage = 'An unknown error occurred';
    let fieldErrors: { [key: string]: string } | null = null;
    
    console.error('=== HTTP ERROR DETAILS ===');
    console.error('Status:', error?.status || error?.statusCode);
    console.error('Status Text:', error?.statusText);
    console.error('URL:', error?.url);
    console.error('Error Object:', error?.error);
    console.error('Full Error:', error);
    console.error('=== END ERROR DETAILS ===');
    
    // Detailed logging for fieldErrors extraction
    console.log('🔍 Checking for fieldErrors...');
    console.log('error.error exists:', !!error?.error);
    console.log('error.error.data exists:', !!error?.error?.data);
    console.log('error.error.data.fieldErrors exists:', !!error?.error?.data?.fieldErrors);
    
    // Check if this is a transformed error from GlobalErrorInterceptor
    console.log('Checking if error is IApiError from GlobalErrorInterceptor...');
    const isApiError = error && (error.statusCode !== undefined) && error.originalError;
    console.log('Is IApiError:', !!isApiError);
    
    if (isApiError) {
      console.log('✅ Error from GlobalErrorInterceptor detected');
      console.log('originalError:', (error as any).originalError);
      console.log('originalError.error:', (error as any).originalError?.error);
      const originalHttpError = (error as any).originalError as HttpErrorResponse;
      
      console.log('🔍 DEBUG: Checking wrapped error.error:', originalHttpError?.error);
      console.log('🔍 DEBUG: Checking wrapped error.error.data:', originalHttpError?.error?.data);
      console.log('🔍 DEBUG: Checking wrapped error.error.data.fieldErrors:', originalHttpError?.error?.data?.fieldErrors);
      
      // INDUSTRY STANDARD: Extract backend's message from wrapped response
      // Priority: fieldErrors message > backend message > wrapper message
      if (originalHttpError?.error?.data?.message) {
        // Backend sent specific message - use it
        errorMessage = originalHttpError.error.data.message;
        console.log('✅ Backend message extracted from wrapped error:', errorMessage);
        
        // Also check for fieldErrors
        if (originalHttpError?.error?.data?.fieldErrors) {
          fieldErrors = originalHttpError.error.data.fieldErrors;
          console.log('✅ fieldErrors extracted from wrapped error:', fieldErrors);
        }
      }
      // Fallback: Use wrapper's message if available
      else if ((error as any).message) {
        errorMessage = (error as any).message;
        console.log('⚠️ Using error.message from IApiError wrapper:', errorMessage);
      }
    }
    // Direct HttpErrorResponse from service call
    else if (error?.error instanceof ErrorEvent) {
      errorMessage = `Client Error: ${error.error.message}`;
      console.log('✅ Client-side error detected');
    } 
    // Network error
    else if (error?.error instanceof ProgressEvent) {
      errorMessage = 'Network error: Unable to connect to server. Please check if the backend is running on http://localhost:8080 and CORS is properly configured.';
      console.log('✅ Network error detected');
    } 
    // Server-side error - try to extract RFC 7807 response
    else if (error?.status && error?.status >= 400) {
      console.log('✅ Server error detected, attempting to extract backend message...');
      console.log('🔍 DEBUG: Checking error.error:', error.error);
      console.log('🔍 DEBUG: Checking error.error.data:', error.error?.data);
      console.log('🔍 DEBUG: Checking error.error.data.fieldErrors:', error.error?.data?.fieldErrors);
      
      // INDUSTRY STANDARD: Always try to extract backend's RFC 7807 message first
      if (error.error?.data?.message) {
        // Backend sent specific message - use it
        errorMessage = error.error.data.message;
        console.log('✅ Backend message extracted:', errorMessage);
        
        // Also check for fieldErrors
        if (error.error?.data?.fieldErrors) {
          fieldErrors = error.error.data.fieldErrors;
          console.log('✅ fieldErrors extracted from data.fieldErrors:', fieldErrors);
        }
      }
      // Fallback: Check if error has direct message (for wrapped errors)
      else if (error.error?.message) {
        errorMessage = error.error.message;
        console.log('⚠️ Using error.error.message:', errorMessage);
      }
      // Fallback: Generate generic message from HTTP status
      else {
        console.log('⚠️ No backend message found, generating from HTTP status');
        if (error.status === 0) {
          errorMessage = 'Unable to connect to server. This could be due to:\n' +
                       '1. Backend not running on http://localhost:8080\n' +
                       '2. CORS configuration issue\n' +
                       '3. Network connectivity problem\n' +
                       'Please check the browser Network tab for more details.';
        } else if (error.status === 400) {
          errorMessage = 'Bad Request: Please check your input and try again.';
        } else if (error.status === 404) {
          errorMessage = `Endpoint not found: ${error.url}. Please check if the API endpoint is correct.`;
        } else if (error.status === 405) {
          errorMessage = `Method not allowed for ${error.url}. The server may not support this HTTP method.`;
        } else if (error.status === 409) {
          errorMessage = 'Conflict: The resource could not be created due to a constraint violation. Please check your input.';
        } else if (error.status >= 500) {
          errorMessage = `Server Error: ${error.status} - ${error.statusText}`;
        } else {
          errorMessage = `HTTP Error: ${error.status} - ${error.statusText}`;
        }
      }
    }
    // No status code - network connectivity issue
    else {
      errorMessage = 'Unable to connect to server. Please check if the backend is running.';
      console.log('⚠️ No status code detected');
    }
    
    console.error('Final Error Message:', errorMessage);
    console.error('Field Errors Extracted:', fieldErrors);
    
    // Create error object that preserves fieldErrors and original error response
    const enhancedError: any = new Error(errorMessage);
    enhancedError.fieldErrors = fieldErrors;
    enhancedError.originalError = error;
    enhancedError.status = error?.status || (error as any)?.statusCode;
    enhancedError.errorResponse = error?.error;
    
    console.error('🔴 SERVICE THROWING ENHANCED ERROR:');
    console.error('Enhanced Error:', enhancedError);
    console.error('Enhanced Error.fieldErrors:', enhancedError.fieldErrors);
    console.error('About to call throwError...');
    
    return throwError(() => enhancedError);
  }
}
