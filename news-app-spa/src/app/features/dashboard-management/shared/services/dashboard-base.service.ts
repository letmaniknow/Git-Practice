import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { PageResponse } from '../models/dashboard-base.model';

/**
 * Abstract Base Service for all Dashboard Features
 * 
 * Defines the contract that all dashboard feature services must implement
 * Ensures consistency across all phases (News, Admin-User, Category, Recycle-Bin, Settings)
 * 
 * Each phase service must extend this class and implement the required methods:
 * - getStats(): Returns dashboard statistics for the specific feature
 * - getRecentActivity(page, size): Returns paginated activity log for the feature
 * 
 * @abstract
 * @example
 * export class DashboardNewsService extends DashboardBaseService {
 *   getStats(): Observable<DashboardStats> { ... }
 *   getRecentActivity(page, size): Observable<AuditLog[]> { ... }
 * }
 */
@Injectable({ providedIn: 'root' })
export abstract class DashboardBaseService {
  /**
   * Retrieves dashboard statistics for the feature
   * @returns Observable of dashboard statistics with key metrics
   */
  abstract getStats(): Observable<any>;

  /**
   * Retrieves paginated recent activity/audit logs for the feature
   * @param page - Page number (0-indexed)
   * @param size - Number of records per page
   * @returns Observable of paginated activity logs
   */
  abstract getRecentActivity(page: number, size: number): Observable<any[]>;

  /**
   * Refreshes all dashboard data
   * Triggers fetching of fresh stats and activity logs
   */
  abstract onRefresh(): void;

  /**
   * Gets current page information
   * @returns Observable of PageResponse with pagination metadata
   */
  abstract getPageInfo(): Observable<PageResponse | null>;
}
