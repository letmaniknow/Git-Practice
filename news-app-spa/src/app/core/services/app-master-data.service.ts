import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '@environments/environment';

/**
 * AppMasterDataService
 * 
 * Centralized service for application-wide master/reference data endpoints.
 * Simple HTTP utility service - NO caching, NO hidden logic.
 * 
 * ARCHITECTURE:
 * - Simple HTTP methods that return fresh Observables
 * - Each component loads data when it needs it (ngOnInit)
 * - Each component stores data locally for lookups
 * - Always fresh data - no stale cache issues
 * - No complex cache management or refresh logic needed
 * 
 * SCOPE:
 * - Categories (all + active for forms)
 * - Admin Users (for audit fields, assignments, etc.)
 * 
 * USED BY:
 * - News Management (list, form, audit trail)
 * - Category Management (admin user lookups)
 * - Ads Management (category, user lookups)
 * - Analytics (category, user names)
 * - Any feature needing categories or admin users
 * 
 * BENEFITS:
 * - ✅ Simple and predictable - no hidden logic
 * - ✅ Always fresh data from backend
 * - ✅ Easy to debug - explicit data loads in components
 * - ✅ No "forgot to refresh cache" bugs
 * - ✅ Self-contained components
 * - ✅ Centralized endpoint management
 * 
 * USAGE PATTERN:
 * ```typescript
 * export class MyComponent implements OnInit {
 *   availableCategories: NewsCategory[] = [];
 *   availableAdminUsers: AdminUser[] = [];
 * 
 *   constructor(private masterData: AppMasterDataService) {}
 * 
 *   ngOnInit() {
 *     // Load fresh data when component initializes
 *     this.masterData.getAllCategories()
 *       .pipe(takeUntil(this.destroy$))
 *       .subscribe(cats => {
 *         this.availableCategories = cats;
 *       });
 * 
 *     this.masterData.getAdminUsers()
 *       .pipe(takeUntil(this.destroy$))
 *       .subscribe(users => {
 *         this.availableAdminUsers = users;
 *       });
 *   }
 * 
 *   // Use local data for lookups
 *   getCategoryName(id: string): string {
 *     const cat = this.availableCategories.find(c => c.id === id);
 *     return cat?.categoryNameEn || '—';
 *   }
 * }
 * ```
 * 
 * WHY NO CACHE:
 * - Master data changes infrequently (categories, users)
 * - HTTP calls are fast (< 100ms)
 * - Data is small (~10KB for users, ~10KB for categories)
 * - Component lifecycle handles freshness automatically
 * - Avoids complexity and "stale data" confusion
 * 
 * @author System Architect
 * @since 2026-06-03
 */
@Injectable({ providedIn: 'root' })
export class AppMasterDataService {
  
  private readonly BASE_URL = environment.apiBaseUrl;
  
  constructor(private http: HttpClient) {}
  
  // ======================== CATEGORY METHODS ========================
  
  /**
   * Get ALL categories (ACTIVE + INACTIVE)
   * 
   * Used for:
   * - Display in grids (show names for old news with inactive categories)
   * - Filters (allow filtering by inactive categories)
   * - Audit trails (show historical category names)
   * 
   * @returns Observable of all categories (fresh from backend)
   */
  getAllCategories(): Observable<NewsCategory[]> {
    return this.http.get<any>(`${this.BASE_URL}/api/v1/admin/news-categories`, {
      params: new HttpParams()
        .set('page', '0')
        .set('size', '100')
        // NO statusFilter → returns ALL categories (active + inactive)
    }).pipe(
      map(response => {
        const categories = response?.data?.content || [];
        return categories.map((cat: any) => ({
          id: cat.newsCategoriesId,
          categoryNameEn: cat.newsCategoriesNameEn,
          categoryNameEs: cat.newsCategoriesNameEs,
          slug: cat.newsCategoriesSlug,
          description: cat.newsCategoriesDescription,
          status: cat.newsCategoriesStatus
        }));
      })
    );
  }
  
  /**
   * Get ACTIVE categories only for news creation/update forms
   * 
   * Used for:
   * - News creation/edit forms (category dropdown)
   * - Any form where only active categories should be selectable
   * 
   * Backend applies statusFilter=ACTIVE to return ACTIVE categories only
   * 
   * @returns Observable of active categories (fresh from backend)
   */
  getActiveCategoriesForNewsCreate(): Observable<NewsCategory[]> {
    return this.http.get<any>(`${this.BASE_URL}/api/v1/admin/news-categories`, {
      params: new HttpParams()
        .set('page', '0')
        .set('size', '100')
        .set('statusFilter', 'ACTIVE') // Only ACTIVE categories
    }).pipe(
      map(response => {
        const categories = response?.data?.content || [];
        return categories.map((cat: any) => ({
          id: cat.newsCategoriesId,
          categoryNameEn: cat.newsCategoriesNameEn,
          categoryNameEs: cat.newsCategoriesNameEs,
          slug: cat.newsCategoriesSlug,
          description: cat.newsCategoriesDescription,
          status: cat.newsCategoriesStatus
        }));
      })
    );
  }
  
  // ======================== ADMIN USER METHODS ========================
  
  /**
   * Get all admin users
   * 
   * Used for:
   * - User name lookups in audit fields (createdBy, updatedBy, publishedBy, etc.)
   * - Assignment dropdowns
   * - User filters
   * 
   * CORRECT ENDPOINT: /api/v1/admin/staff/autocomplete
   * (NOT /api/v1/admin/admin-users - that endpoint doesn't exist)
   * 
   * @returns Observable of admin users (fresh from backend)
   */
  getAdminUsers(): Observable<AdminUser[]> {
    const params = new HttpParams()
      .set('query', '')
      .set('page', '0')
      .set('size', '100');
    
    return this.http.get<any>(`${this.BASE_URL}/api/v1/admin/staff/autocomplete`, { params }).pipe(
      map(response => {
        // Backend returns ApiResponseDto<Page<AdminUserResponseDto>>
        // Extract the content array from the paginated response
        const pageData = response?.data || response;
        const users = pageData?.content || [];
        
        return users.map((user: any) => ({
          id: user.adminUsersId,
          name: user.adminUsersFullName || 
                `${user.adminUsersFirstName || ''} ${user.adminUsersLastName || ''}`.trim() ||
                user.adminUsersUsername,
          email: user.adminUsersEmail,
          username: user.adminUsersUsername,
          firstName: user.adminUsersFirstName,
          lastName: user.adminUsersLastName
        }));
      })
    );
  }
}

// ======================== INTERFACES ========================

/**
 * News Category model
 */
export interface NewsCategory {
  id: string;
  categoryNameEn: string;
  categoryNameEs: string;
  slug: string;
  description?: string;
  status?: string;
}

/**
 * Admin User model
 */
export interface AdminUser {
  id: string;
  name: string;
  email: string;
  username: string;
  firstName?: string;
  lastName?: string;
}
