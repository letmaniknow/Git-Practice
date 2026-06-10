import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, BehaviorSubject, throwError } from 'rxjs';
import { tap, map, catchError, finalize } from 'rxjs/operators';
import {
  DashboardStats,
  NewsAuditLogDto,
  PageResponse,
} from '../models/dashboard-news.model';
import { ADMIN_DASHBOARD_NEWS_API } from '../constants/dashboard-news-api.constant';
import { DashboardBaseService } from '../../shared/services/dashboard-base.service';

/**
 * Dashboard Service for News Feature (Phase 1)
 * 
 * Extends DashboardBaseService to implement the dashboard contract.
 * Manages all news dashboard data including statistics and audit logs.
 * 
 * Key Features:
 * - Extends abstract DashboardBaseService for consistency with other phases
 * - State management with BehaviorSubjects for reactive updates
 * - ApiResponseDto wrapper handling
 * - Pagination support (page, size, sort)
 * - Comprehensive error handling
 * - Memory leak prevention with observables
 * 
 * Used By:
 * - DashboardNewsPageComponent (main orchestrator)
 * - DashboardStatsCardComponent (stats display)
 * - DashboardNewsActivityComponent (activity feed with pagination)
 * 
 * @author MMVA Team
 * @since 1.0.0
 * @extends DashboardBaseService
 */
@Injectable({ providedIn: 'root' })
export class DashboardNewsService extends DashboardBaseService {

  // ========================================
  // State Management - Private BehaviorSubjects
  // ========================================
  private statsSubject$ = new BehaviorSubject<DashboardStats | null>(null);
  private activitiesSubject$ = new BehaviorSubject<NewsAuditLogDto[]>([]);
  private loadingSubject$ = new BehaviorSubject<boolean>(false);
  private errorSubject$ = new BehaviorSubject<string | null>(null);
  private totalActivitiesSubject$ = new BehaviorSubject<number>(0);
  private pageInfoSubject$ = new BehaviorSubject<PageResponse | null>(null);

  // ========================================
  // Public Observables (for component binding)
  // ========================================
  public stats$ = this.statsSubject$.asObservable();
  public activities$ = this.activitiesSubject$.asObservable();
  public loading$ = this.loadingSubject$.asObservable();
  public error$ = this.errorSubject$.asObservable();
  public totalActivities$ = this.totalActivitiesSubject$.asObservable();
  public pageInfo$ = this.pageInfoSubject$.asObservable();

  // ========================================
  // Internal subjects exposed for direct state updates (use with caution)
  // ========================================
  public _statsSubject$ = this.statsSubject$;
  public _activitiesSubject$ = this.activitiesSubject$;
  public _loadingSubject$ = this.loadingSubject$;
  public _errorSubject$ = this.errorSubject$;
  public _pageInfoSubject$ = this.pageInfoSubject$;

  constructor(private http: HttpClient) {
    super();
  }

  /**
   * Fetch dashboard statistics (aggregated metrics)
   * 
   * @returns Observable of dashboard stats
   */
  public getStats(): Observable<DashboardStats> {
    this.loadingSubject$.next(true);
    this.errorSubject$.next(null);

    console.log('📊 Service: Requesting stats from:', ADMIN_DASHBOARD_NEWS_API.STATS);

    return this.http.get<any>(ADMIN_DASHBOARD_NEWS_API.STATS).pipe(
      tap(response => console.log('✅ Service: Stats response:', response)),
      map(response => {
        // Unwrap ApiResponseDto wrapper
        const data = response.data || response;
        console.log('📥 Service: Mapped stats data:', data);
        this.statsSubject$.next(data as DashboardStats);
        return data as DashboardStats;
      }),
      catchError(error => {
        console.error('❌ Service: Error fetching stats:', error);
        this.errorSubject$.next('Failed to fetch dashboard stats');
        return throwError(() => error);
      }),
      finalize(() => this.loadingSubject$.next(false))
    );
  }

  /**
   * Fetch paginated recent activity (audit logs)
   * Follows Spring Data pagination: page (0-indexed), size, sort
   * 
   * @param page Zero-based page number (default: 0)
   * @param size Page size (default: 10)
   * @param sort Sort order (default: createdAt,desc)
   * @returns Observable of paginated activities
   */
  public getRecentActivity(
    page: number = 0,
    size: number = 10,
    sort: string = 'createdAt,desc'
  ): Observable<NewsAuditLogDto[]> {
    this.loadingSubject$.next(true);
    this.errorSubject$.next(null);

    console.log('📝 Service: Requesting activities - page:', page, 'size:', size, 'sort:', sort);

    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', sort);

    return this.http.get<any>(
      ADMIN_DASHBOARD_NEWS_API.RECENT_ACTIVITY,
      { params }
    ).pipe(
      tap(response => console.log('✅ Service: Activities response:', response)),
      map(response => {
        // Unwrap ApiResponseDto wrapper
        const pageData = response.data;
        console.log('📥 Service: Mapped activities data:', pageData);
        
        // Store page metadata
        this.pageInfoSubject$.next({
          page: pageData.number || 0,
          size: pageData.size || size,
          totalElements: pageData.totalElements || 0,
          totalPages: pageData.totalPages || 0,
          hasNext: pageData.hasNext || false,
          hasPrevious: pageData.hasPrevious || false,
        });

        this.totalActivitiesSubject$.next(pageData.totalElements || 0);
        
        // Extract content array from page response
        const activities = pageData.content || [];
        this.activitiesSubject$.next(activities as NewsAuditLogDto[]);
        return activities as NewsAuditLogDto[];
      }),
      catchError(error => {
        console.error('❌ Service: Error fetching activities:', error);
        this.errorSubject$.next('Failed to fetch recent activity');
        return throwError(() => error);
      }),
      finalize(() => this.loadingSubject$.next(false))
    );
  }

  /**
   * Get combined dashboard data (stats + activity in one call)
   * Optional: implement if backend supports combined endpoint
   */
  public getDashboardData(): Observable<{ stats: DashboardStats; activities: NewsAuditLogDto[] }> {
    this.loadingSubject$.next(true);
    this.errorSubject$.next(null);

    console.log('📊 Service: Requesting combined dashboard data');

    return this.http.get<any>(`${ADMIN_DASHBOARD_NEWS_API.STATS}?includeActivity=true`).pipe(
      tap(response => console.log('✅ Service: Dashboard response:', response)),
      map(response => {
        const data = response.data || response;
        return {
          stats: data.stats as DashboardStats,
          activities: data.activities as NewsAuditLogDto[],
        };
      }),
      catchError(error => {
        console.error('❌ Service: Error fetching dashboard data:', error);
        this.errorSubject$.next('Failed to fetch dashboard data');
        return throwError(() => error);
      }),
      finalize(() => this.loadingSubject$.next(false))
    );
  }

  /**
   * Implementation of DashboardBaseService.onRefresh()
   * Refresh all dashboard data when user clicks refresh button
   */
  public override onRefresh(): void {
    console.log('🔄 Service: Refreshing dashboard');
    this.getStats().subscribe();
    this.getRecentActivity(0, 10).subscribe();
  }

  /**
   * Implementation of DashboardBaseService.getPageInfo()
   * Get current pagination information
   */
  public override getPageInfo(): Observable<PageResponse | null> {
    return this.pageInfo$;
  }

  /**
   * Refresh all dashboard data
   * Legacy method - use onRefresh() for base service compatibility
   */
  public refreshDashboard(): void {
    this.onRefresh();
  }

  /**
   * Clear error state
   */
  public clearError(): void {
    this.errorSubject$.next(null);
  }
}
