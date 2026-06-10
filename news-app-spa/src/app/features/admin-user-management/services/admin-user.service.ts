import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, BehaviorSubject, Subject, throwError } from 'rxjs';
import { tap, map, catchError, finalize, debounceTime, distinctUntilChanged, switchMap } from 'rxjs/operators';

import {
  AdminUserResponseDto,
  AdminUserCreateRequestDto,
  AdminUserUpdateRequestDto,
  AdminUserViewModel,
  AdminUserFilterOptions,
  AdminUserState,
  PaginatedAdminUserResponse,
  AdminUserListApiResponse,
  ApiResponseDtoWrapper,
  AdminStatus
} from '../models/admin-user.model';

import {
  ADMIN_USER_API,
  ADMIN_USER_CONFIG,
  ADMIN_USER_PAGINATION_DEFAULTS
} from '../constants/admin-user-api.constant';

/**
 * Admin User Management Service
 * 
 * Manages all admin user CRUD operations and state management.
 * Provides reactive state through BehaviorSubjects and Observables.
 * 
 * Key Features:
 * - Full CRUD operations (Create, Read, Update, Delete)
 * - Advanced search and filtering capabilities
 * - Status management (activate, deactivate, suspend, restore)
 * - Reactive state management with BehaviorSubjects
 * - Comprehensive error handling
 * - Memory leak prevention with proper subscription management
 * - Request caching and debouncing
 * 
 * Used By:
 * - AdminUserListPageComponent (main page orchestrator)
 * - AdminUserListComponent (table display)
 * - AdminUserFormDialogComponent (create/edit)
 * - AdminUserDeleteDialogComponent (confirmation)
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Injectable({ providedIn: 'root' })
export class AdminUserService {

  // ========================================
  // Private State Subjects
  // ========================================
  private usersSubject$ = new BehaviorSubject<AdminUserViewModel[]>([]);
  private loadingSubject$ = new BehaviorSubject<boolean>(false);
  private errorSubject$ = new BehaviorSubject<string | null>(null);
  private totalCountSubject$ = new BehaviorSubject<number>(0);
  private currentPageSubject$ = new BehaviorSubject<number>(0);
  private totalPagesSubject$ = new BehaviorSubject<number>(0);
  private selectedUserSubject$ = new BehaviorSubject<AdminUserViewModel | null>(null);
  private filterOptionsSubject$ = new BehaviorSubject<AdminUserFilterOptions>({
    page: 0,
    pageSize: ADMIN_USER_PAGINATION_DEFAULTS.pageSize,
    sortBy: 'createdAt' as const,
    sortDirection: 'desc'
  });

  // Search subject for autocomplete
  private searchSubject$ = new Subject<string>();

  // ========================================
  // Public Observables (for component binding)
  // ========================================
  public users$ = this.usersSubject$.asObservable();
  public loading$ = this.loadingSubject$.asObservable();
  public error$ = this.errorSubject$.asObservable();
  public totalCount$ = this.totalCountSubject$.asObservable();
  public currentPage$ = this.currentPageSubject$.asObservable();
  public totalPages$ = this.totalPagesSubject$.asObservable();
  public selectedUser$ = this.selectedUserSubject$.asObservable();
  public filterOptions$ = this.filterOptionsSubject$.asObservable();

  // Combined observable for convenience
  public adminUserState$: Observable<AdminUserState> = this.users$.pipe(
    switchMap(users =>
      this.totalCount$.pipe(
        switchMap(totalCount =>
          this.currentPage$.pipe(
            switchMap(currentPage =>
              this.totalPages$.pipe(
                switchMap(totalPages =>
                  this.loading$.pipe(
                    switchMap(loading =>
                      this.error$.pipe(
                        switchMap(error =>
                          this.filterOptions$.pipe(
                            switchMap(filterOptions =>
                              this.selectedUser$.pipe(
                                map(selectedUser => ({
                                  users,
                                  totalCount,
                                  currentPage,
                                  totalPages,
                                  loading,
                                  error,
                                  filterOptions,
                                  selectedUser: selectedUser || undefined
                                }))
                              )
                            )
                          )
                        )
                      )
                    )
                  )
                )
              )
            )
          )
        )
      )
    )
  );

  constructor(private http: HttpClient) {
    this.initializeSearch();
  }

  // ========================================
  // Initialization
  // ========================================

  /**
   * Initialize search subject with debounce and distinctUntilChanged
   * for autocomplete functionality
   */
  private initializeSearch(): void {
    this.searchSubject$
      .pipe(
        debounceTime(ADMIN_USER_CONFIG.searchDebounceMs),
        distinctUntilChanged(),
        switchMap(query => this.searchActiveAdmins(query, 0, ADMIN_USER_PAGINATION_DEFAULTS.pageSize))
      )
      .subscribe({
        next: () => {
          // Results are handled by calling component
        },
        error: (error: any) => {
          console.error('❌ Service: Search error:', error);
          this.errorSubject$.next('Search failed');
        }
      });
  }

  // ========================================
  // Search Operations
  // ========================================

  /**
   * Trigger search for active admins
   * Automatically debounced to 300ms
   * 
   * @param query Search query string
   */
  public search(query: string): void {
    this.searchSubject$.next(query);
  }

  /**
   * Search active admin users by name, username, or email
   * 
   * @param query Search query string
   * @param page Page number (0-indexed)
   * @param pageSize Items per page
   * @returns Observable of paginated search results
   */
  private searchActiveAdmins(
    query: string,
    page: number = 0,
    pageSize: number = ADMIN_USER_PAGINATION_DEFAULTS.pageSize
  ): Observable<PaginatedAdminUserResponse> {
    let params = new HttpParams()
      .set('q', query)
      .set('page', page.toString())
      .set('pageSize', pageSize.toString());

    console.log('🔍 Service: Searching admins:', query);

    return this.http.get<PaginatedAdminUserResponse>(
      ADMIN_USER_API.SEARCH_ACTIVE,
      { params }
    ).pipe(
      tap(response => console.log('✅ Service: Search response:', response)),
      catchError((error: any) => {
        console.error('❌ Service: Search error:', error);
        return throwError(() => error);
      })
    );
  }

  // ========================================
  // List Operations
  // ========================================

  /**
   * Load admin users with current filter options
   * Updates all state subjects with response data
   * 
   * @returns Observable of paginated response
   */
  public loadAdminUsers(): Observable<PaginatedAdminUserResponse> {
    this.loadingSubject$.next(true);
    this.errorSubject$.next(null);

    const filters = this.filterOptionsSubject$.value;

    let params = new HttpParams()
      .set('page', filters.page?.toString() || '0')
      .set('size', filters.pageSize?.toString() || ADMIN_USER_PAGINATION_DEFAULTS.pageSize.toString());
      // Add cache buster to force fresh data from server
      //.set('_t', Date.now().toString());

    if (filters.sortBy) {
      params = params.set('sort', filters.sortBy);
      if (filters.sortDirection) {
        params = params.set('direction', filters.sortDirection);
      }
    }

    if (filters.status) {
      params = params.set('status', filters.status);
    }

    if (filters.searchText) {
      params = params.set('search', filters.searchText);
    }

    console.log('📋 Service: Loading admin users with filters:', filters);

    return this.http.get<AdminUserListApiResponse>(
      ADMIN_USER_API.LIST,
      { params }
    ).pipe(
      map(response => response.data),
      tap(response => {
        console.log('✅ Service: Admin users loaded:', response);
        this.updateStateFromResponse(response);
      }),
      catchError((error: any) => {
        console.error('❌ Service: Error loading admin users:', error);
        const errorMessage = error?.error?.message || 'Failed to load admin users';
        this.errorSubject$.next(errorMessage);
        return throwError(() => error);
      }),
      finalize(() => this.loadingSubject$.next(false))
    );
  }

  /**
   * Load only active admin users
   * 
   * @param page Page number (0-indexed)
   * @param pageSize Items per page
   * @returns Observable of paginated response
   */
  public loadActiveAdmins(
    page: number = 0,
    pageSize: number = ADMIN_USER_PAGINATION_DEFAULTS.pageSize
  ): Observable<PaginatedAdminUserResponse> {
    this.loadingSubject$.next(true);
    this.errorSubject$.next(null);

    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', pageSize.toString());

    return this.http.get<AdminUserListApiResponse>(
      ADMIN_USER_API.LIST_ACTIVE,
      { params }
    ).pipe(
      map(response => response.data),
      tap(response => {
        console.log('✅ Service: Active admins loaded:', response);
        this.updateStateFromResponse(response);
        this.currentPageSubject$.next(page);
      }),
      catchError((error: any) => {
        console.error('❌ Service: Error loading active admins:', error);
        this.errorSubject$.next('Failed to load active admin users');
        return throwError(() => error);
      }),
      finalize(() => this.loadingSubject$.next(false))
    );
  }

  /**
   * Load only deleted admin users
   * 
   * @param page Page number (0-indexed)
   * @param pageSize Items per page
   * @returns Observable of paginated response
   */
  public loadDeletedAdmins(
    page: number = 0,
    pageSize: number = ADMIN_USER_PAGINATION_DEFAULTS.pageSize
  ): Observable<PaginatedAdminUserResponse> {
    this.loadingSubject$.next(true);
    this.errorSubject$.next(null);

    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', pageSize.toString());

    return this.http.get<AdminUserListApiResponse>(
      ADMIN_USER_API.LIST_DELETED,
      { params }
    ).pipe(
      map(response => response.data),
      tap(response => {
        console.log('✅ Service: Deleted admins loaded:', response);
        this.updateStateFromResponse(response);
        this.currentPageSubject$.next(page);
      }),
      catchError((error: any) => {
        console.error('❌ Service: Error loading deleted admins:', error);
        this.errorSubject$.next('Failed to load deleted admin users');
        return throwError(() => error);
      }),
      finalize(() => this.loadingSubject$.next(false))
    );
  }

  // ========================================
  // Get Single Admin
  // ========================================

  /**
   * Get single admin user by ID
   * Updates selectedUser$ subject
   * 
   * @param id Admin user ID (UUID)
   * @returns Observable of admin user response
   */
  public getAdminUser(id: string): Observable<AdminUserResponseDto> {
    this.loadingSubject$.next(true);
    this.errorSubject$.next(null);

    console.log('👤 Service: Loading admin user:', id);

    return this.http.get<ApiResponseDtoWrapper<AdminUserResponseDto>>(
      ADMIN_USER_API.GET_BY_ID.replace('{id}', id)
    ).pipe(
      map(response => response.data),
      tap(response => {
        console.log('✅ Service: Admin user loaded:', response);
        const viewModel = this.convertToViewModel(response);
        this.selectedUserSubject$.next(viewModel);
      }),
      catchError((error: any) => {
        console.error('❌ Service: Error loading admin user:', error);
        this.errorSubject$.next('Failed to load admin user');
        return throwError(() => error);
      }),
      finalize(() => this.loadingSubject$.next(false))
    );
  }

  /**
   * Get admin user by username
   * 
   * @param username Admin username
   * @returns Observable of admin user response
   */
  public getAdminByUsername(username: string): Observable<AdminUserResponseDto> {
    return this.http.get<ApiResponseDtoWrapper<AdminUserResponseDto>>(
      ADMIN_USER_API.GET_BY_USERNAME.replace('{username}', username)
    ).pipe(
      map(response => response.data),
      catchError((error: any) => {
        console.error('❌ Service: Error loading admin by username:', error);
        return throwError(() => error);
      })
    );
  }

  /**
   * Get admin user by email
   * 
   * @param email Admin email address
   * @returns Observable of admin user response
   */
  public getAdminByEmail(email: string): Observable<AdminUserResponseDto> {
    return this.http.get<ApiResponseDtoWrapper<AdminUserResponseDto>>(
      ADMIN_USER_API.GET_BY_EMAIL.replace('{email}', email)
    ).pipe(
      map(response => response.data),
      catchError((error: any) => {
        console.error('❌ Service: Error loading admin by email:', error);
        return throwError(() => error);
      })
    );
  }

  // ========================================
  // Create Admin User
  // ========================================

  /**
   * Create new admin user
   * 
   * @param request Create request DTO
   * @returns Observable of created admin user response
   */
  public createAdminUser(request: AdminUserCreateRequestDto): Observable<AdminUserResponseDto> {
    this.loadingSubject$.next(true);
    this.errorSubject$.next(null);

    // Validate required fields
    if (!request.adminUsersUsername?.trim() || !request.adminUsersEmail?.trim() || !request.adminUsersPassword?.trim()) {
      const error = 'Username, email, and password are required';
      this.errorSubject$.next(error);
      this.loadingSubject$.next(false);
      return throwError(() => new Error(error));
    }

    console.log('➕ Service: Creating admin user:', request.adminUsersUsername);

    return this.http.post<AdminUserResponseDto>(
      ADMIN_USER_API.CREATE,
      request
    ).pipe(
      tap(response => {
        console.log('✅ Service: Admin user created:', response);
        this.errorSubject$.next(null);
      }),
      catchError((error: any) => {
        console.error('❌ Service: Error creating admin user:', error);
        const errorMessage = error?.error?.message || 'Failed to create admin user';
        this.errorSubject$.next(errorMessage);
        return throwError(() => error);
      }),
      finalize(() => this.loadingSubject$.next(false))
    );
  }

  // ========================================
  // Update Admin User
  // ========================================

  /**
   * Update existing admin user
   * 
   * @param id Admin user ID (UUID)
   * @param request Update request DTO (partial)
   * @returns Observable of updated admin user response
   */
  public updateAdminUser(id: string, request: AdminUserUpdateRequestDto): Observable<AdminUserResponseDto> {
    this.loadingSubject$.next(true);
    this.errorSubject$.next(null);

    console.log('✏️ Service: Updating admin user:', id);

    return this.http.put<AdminUserResponseDto>(
      ADMIN_USER_API.UPDATE.replace('{id}', id),
      request
    ).pipe(
      tap(response => {
        console.log('✅ Service: Admin user updated:', response);
        this.updateUserInList(response);
        if (this.selectedUserSubject$.value?.adminUsersId === id) {
          this.selectedUserSubject$.next(this.convertToViewModel(response));
        }
      }),
      catchError((error: any) => {
        console.error('❌ Service: Error updating admin user:', error);
        const errorMessage = error?.error?.message || 'Failed to update admin user';
        this.errorSubject$.next(errorMessage);
        return throwError(() => error);
      }),
      finalize(() => this.loadingSubject$.next(false))
    );
  }

  // ========================================
  // Delete Admin User
  // ========================================

  /**
   * Soft delete admin user
   * User can be restored later
   * 
   * @param id Admin user ID (UUID)
   * @returns Observable<void>
   */
  public deleteAdminUser(id: string): Observable<void> {
    this.loadingSubject$.next(true);
    this.errorSubject$.next(null);

    console.log('🗑️ Service: Deleting admin user:', id);

    return this.http.delete<void>(
      ADMIN_USER_API.DELETE.replace('{id}', id)
    ).pipe(
      tap(() => {
        console.log('✅ Service: Admin user deleted');
        this.removeUserFromList(id);
      }),
      catchError((error: any) => {
        console.error('❌ Service: Error deleting admin user:', error);
        this.errorSubject$.next('Failed to delete admin user');
        return throwError(() => error);
      }),
      finalize(() => this.loadingSubject$.next(false))
    );
  }

  /**
   * Restore soft-deleted admin user
   * 
   * @param id Admin user ID (UUID)
   * @returns Observable of restored admin user response
   */
  public restoreAdminUser(id: string): Observable<AdminUserResponseDto> {
    this.loadingSubject$.next(true);
    this.errorSubject$.next(null);

    console.log('🔄 Service: Restoring admin user:', id);

    return this.http.post<AdminUserResponseDto>(
      ADMIN_USER_API.RESTORE.replace('{id}', id),
      {}
    ).pipe(
      tap(response => {
        console.log('✅ Service: Admin user restored:', response);
        this.updateUserInList(response);
      }),
      catchError((error: any) => {
        console.error('❌ Service: Error restoring admin user:', error);
        this.errorSubject$.next('Failed to restore admin user');
        return throwError(() => error);
      }),
      finalize(() => this.loadingSubject$.next(false))
    );
  }

  // ========================================
  // Status Management
  // ========================================

  /**
   * Activate admin user (change status to ACTIVE)
   * 
   * @param id Admin user ID (UUID)
   * @returns Observable of updated admin user response
   */
  public activateAdminUser(id: string): Observable<AdminUserResponseDto> {
    this.loadingSubject$.next(true);
    this.errorSubject$.next(null);

    return this.http.patch<AdminUserResponseDto>(
      ADMIN_USER_API.ACTIVATE.replace('{id}', id),
      {}
    ).pipe(
      tap(response => {
        console.log('✅ Service: Admin user activated');
        this.updateUserInList(response);
      }),
      catchError((error: any) => {
        console.error('❌ Service: Error activating admin user:', error);
        this.errorSubject$.next('Failed to activate admin user');
        return throwError(() => error);
      }),
      finalize(() => this.loadingSubject$.next(false))
    );
  }

  /**
   * Deactivate admin user (change status to INACTIVE)
   * 
   * @param id Admin user ID (UUID)
   * @returns Observable of updated admin user response
   */
  public deactivateAdminUser(id: string): Observable<AdminUserResponseDto> {
    this.loadingSubject$.next(true);
    this.errorSubject$.next(null);

    return this.http.patch<AdminUserResponseDto>(
      ADMIN_USER_API.DEACTIVATE.replace('{id}', id),
      {}
    ).pipe(
      tap(response => {
        console.log('✅ Service: Admin user deactivated');
        this.updateUserInList(response);
      }),
      catchError((error: any) => {
        console.error('❌ Service: Error deactivating admin user:', error);
        this.errorSubject$.next('Failed to deactivate admin user');
        return throwError(() => error);
      }),
      finalize(() => this.loadingSubject$.next(false))
    );
  }

  /**
   * Suspend admin user (change status to SUSPENDED)
   * 
   * @param id Admin user ID (UUID)
   * @returns Observable of updated admin user response
   */
  public suspendAdminUser(id: string): Observable<AdminUserResponseDto> {
    this.loadingSubject$.next(true);
    this.errorSubject$.next(null);

    return this.http.post<AdminUserResponseDto>(
      ADMIN_USER_API.SUSPEND.replace('{id}', id),
      {}
    ).pipe(
      tap(response => {
        console.log('✅ Service: Admin user suspended');
        this.updateUserInList(response);
      }),
      catchError((error: any) => {
        console.error('❌ Service: Error suspending admin user:', error);
        this.errorSubject$.next('Failed to suspend admin user');
        return throwError(() => error);
      }),
      finalize(() => this.loadingSubject$.next(false))
    );
  }

  // ========================================
  // Filter & Sort Management
  // ========================================

  /**
   * Update filter options and reload list
   * 
   * @param options New filter options
   */
  public setFilterOptions(options: Partial<AdminUserFilterOptions>): void {
    const current = this.filterOptionsSubject$.value;
    const updated = { ...current, ...options, page: 0 }; // Reset to page 0 on filter change
    this.filterOptionsSubject$.next(updated);
  }

  /**
   * Update pagination
   * 
   * @param page Page number (0-indexed)
   * @param pageSize Items per page
   */
  public setPagination(page: number, pageSize: number): void {
    const current = this.filterOptionsSubject$.value;
    this.filterOptionsSubject$.next({ ...current, page, pageSize });
  }

  /**
   * Update sort
   * 
   * @param sortBy Field to sort by
   * @param sortDirection Sort direction ('asc' or 'desc')
   */
  public setSort(sortBy: string, sortDirection: 'asc' | 'desc'): void {
    const current = this.filterOptionsSubject$.value;
    this.filterOptionsSubject$.next({
      ...current,
      sortBy: sortBy as any,
      sortDirection,
      page: 0 // Reset to page 0 on sort change
    });
  }

  // ========================================
  // Helper Methods (Private)
  // ========================================

  /**
   * Update all state subjects from paginated response
   */
  private updateStateFromResponse(response: PaginatedAdminUserResponse): void {
    const viewModels = response.content.map(user => this.convertToViewModel(user));
    this.usersSubject$.next(viewModels);
    this.totalCountSubject$.next(response.totalElements);
    this.currentPageSubject$.next(response.pageable.pageNumber);
    this.totalPagesSubject$.next(response.totalPages);
  }

  /**
   * Convert response DTO to view model
   */
  private convertToViewModel(dto: AdminUserResponseDto): AdminUserViewModel {
    return {
      ...dto,
      displayStatus: this.getStatusLabel(dto.adminUsersStatus),
      displayName: dto.adminUsersFullName || dto.adminUsersUsername,
      isSelected: false,
      isLoading: false
    };
  }

  /**
   * Get friendly status label
   */
  private getStatusLabel(status: AdminStatus): string {
    const labels: Record<AdminStatus, string> = {
      [AdminStatus.ACTIVE]: 'Active',
      [AdminStatus.INACTIVE]: 'Inactive',
      [AdminStatus.SUSPENDED]: 'Suspended',
      [AdminStatus.PENDING]: 'Pending',
      [AdminStatus.DELETED]: 'Deleted',
      [AdminStatus.BANNED]: 'Banned'
    };
    return labels[status] || status;
  }

  /**
   * Update user in current list
   */
  private updateUserInList(updatedUser: AdminUserResponseDto): void {
    const current = this.usersSubject$.value;
    const index = current.findIndex(u => u.adminUsersId === updatedUser.adminUsersId);
    if (index !== -1) {
      const updated = [...current];
      updated[index] = this.convertToViewModel(updatedUser);
      this.usersSubject$.next(updated);
    }
  }

  /**
   * Remove user from current list
   */
  private removeUserFromList(id: string): void {
    const current = this.usersSubject$.value;
    const filtered = current.filter(u => u.adminUsersId !== id);
    this.usersSubject$.next(filtered);
  }

  /**
   * Clear all state (useful on logout or feature unmount)
   */
  public clearState(): void {
    this.usersSubject$.next([]);
    this.loadingSubject$.next(false);
    this.errorSubject$.next(null);
    this.totalCountSubject$.next(0);
    this.currentPageSubject$.next(0);
    this.totalPagesSubject$.next(0);
    this.selectedUserSubject$.next(null);
    this.filterOptionsSubject$.next({
      page: 0,
      pageSize: ADMIN_USER_PAGINATION_DEFAULTS.pageSize,
      sortBy: 'createdAt' as const,
      sortDirection: 'desc'
    });
  }

  // ========================================
  // Role & Permission Management
  // ========================================

  /**
   * Get all permissions for admin user
   * 
   * @param id Admin user ID (UUID)
   * @returns Observable of permissions list
   */
  public getAdminPermissions(id: string): Observable<any[]> {
    return this.http.get<any>(`${ADMIN_USER_API.LIST.replace(/\/\?.*/, '')}/${id}/permissions`)
      .pipe(
        map(response => response.data || []),
        catchError((error: any) => {
          console.error('❌ Service: Error loading admin permissions:', error);
          return throwError(() => error);
        })
      );
  }

  /**
   * Get all roles for admin user
   * 
   * @param id Admin user ID (UUID)
   * @returns Observable of roles list
   */
  public getAdminRoles(id: string): Observable<any[]> {
    return this.http.get<any>(`${ADMIN_USER_API.LIST.replace(/\/\?.*/, '')}/${id}/roles`)
      .pipe(
        map(response => response.data || []),
        catchError((error: any) => {
          console.error('❌ Service: Error loading admin roles:', error);
          return throwError(() => error);
        })
      );
  }

  /**
   * Check if admin has specific permission
   * 
   * @param id Admin user ID (UUID)
   * @param permissionName Permission name to check
   * @returns Observable of boolean
   */
  public hasPermission(id: string, permissionName: string): Observable<boolean> {
    const params = new HttpParams().set('permissionName', permissionName);
    return this.http.get<any>(`${ADMIN_USER_API.LIST.replace(/\/\?.*/, '')}/${id}/has-permission`, { params })
      .pipe(
        map(response => response.data || false),
        catchError((error: any) => {
          console.error('❌ Service: Error checking permission:', error);
          return throwError(() => error);
        })
      );
  }

  /**
   * Assign role to admin user
   * 
   * @param id Admin user ID (UUID)
   * @param roleId Role ID (UUID) to assign
   * @returns Observable<void>
   */
  public assignRole(id: string, roleId: string): Observable<void> {
    this.loadingSubject$.next(true);
    const params = new HttpParams().set('roleId', roleId);
    
    return this.http.patch<void>(
      `${ADMIN_USER_API.LIST.replace(/\/\?.*/, '')}/${id}/assign-role`,
      {},
      { params }
    ).pipe(
      tap(() => {
        console.log('✅ Service: Role assigned successfully');
        this.loadAdminUsers(); // Refresh list
      }),
      catchError((error: any) => {
        console.error('❌ Service: Error assigning role:', error);
        this.errorSubject$.next('Failed to assign role');
        return throwError(() => error);
      }),
      finalize(() => this.loadingSubject$.next(false))
    );
  }

  /**
   * Revoke role from admin user
   * 
   * @param id Admin user ID (UUID)
   * @param roleId Role ID (UUID) to revoke
   * @returns Observable<void>
   */
  public revokeRole(id: string, roleId: string): Observable<void> {
    this.loadingSubject$.next(true);
    const params = new HttpParams().set('roleId', roleId);
    
    return this.http.patch<void>(
      `${ADMIN_USER_API.LIST.replace(/\/\?.*/, '')}/${id}/revoke-role`,
      {},
      { params }
    ).pipe(
      tap(() => {
        console.log('✅ Service: Role revoked successfully');
        this.loadAdminUsers(); // Refresh list
      }),
      catchError((error: any) => {
        console.error('❌ Service: Error revoking role:', error);
        this.errorSubject$.next('Failed to revoke role');
        return throwError(() => error);
      }),
      finalize(() => this.loadingSubject$.next(false))
    );
  }

  // ========================================
  // Security & Authentication Management
  // ========================================

  /**
   * Change admin password
   * 
   * @param id Admin user ID (UUID)
   * @param newPassword New password
   * @param currentPassword Current password (optional, not needed when admin changes other admin's password)
   * @returns Observable<void>
   */
  public changePassword(id: string, newPassword: string, currentPassword: string): Observable<void> {
    this.loadingSubject$.next(true);
    const request: any = {
      adminUsersNewPassword: newPassword,
      adminUsersConfirmPassword: newPassword,
      adminUsersCurrentPassword: currentPassword
    };
    
    return this.http.patch<void>(
      `${ADMIN_USER_API.LIST.replace(/\/\?.*/, '')}/${id}/change-password`,
      request
    ).pipe(
      tap(() => {
        console.log('✅ Service: Password changed successfully');
        this.errorSubject$.next(null);
      }),
      catchError((error: any) => {
        console.error('❌ Service: Error changing password:', error);
        this.errorSubject$.next(error?.error?.message || 'Failed to change password');
        return throwError(() => error);
      }),
      finalize(() => this.loadingSubject$.next(false))
    );
  }

  /**
   * Send email verification code to admin
   * 
   * @param id Admin user ID (UUID)
   * @returns Observable<string> - verification code
   */
  public sendVerificationCode(id: string): Observable<string> {
    return this.http.post<any>(
      `${ADMIN_USER_API.LIST.replace(/\/\?.*/, '')}/${id}/send-verification-code`,
      {}
    ).pipe(
      tap(() => console.log('✅ Service: Verification code sent')),
      map(response => response.data),
      catchError((error: any) => {
        console.error('❌ Service: Error sending verification code:', error);
        return throwError(() => error);
      })
    );
  }

  /**
   * Verify admin email with verification code
   * 
   * @param id Admin user ID (UUID)
   * @param verificationCode Verification code
   * @returns Observable<void>
   */
  public verifyEmail(id: string, verificationCode: string): Observable<void> {
    const request = { verificationCode };
    
    return this.http.patch<void>(
      `${ADMIN_USER_API.LIST.replace(/\/\?.*/, '')}/${id}/verify-email`,
      request
    ).pipe(
      tap(() => console.log('✅ Service: Email verified successfully')),
      catchError((error: any) => {
        console.error('❌ Service: Error verifying email:', error);
        return throwError(() => error);
      })
    );
  }

  /**
   * Record admin login
   * 
   * @param id Admin user ID (UUID)
   * @returns Observable of updated admin user response
   */
  public recordLogin(id: string): Observable<AdminUserResponseDto> {
    return this.http.patch<any>(
      `${ADMIN_USER_API.LIST.replace(/\/\?.*/, '')}/${id}/login`,
      {}
    ).pipe(
      tap(response => console.log('✅ Service: Login recorded')),
      map(response => response.data),
      catchError((error: any) => {
        console.error('❌ Service: Error recording login:', error);
        return throwError(() => error);
      })
    );
  }

  /**
   * Request password reset
   * 
   * @param loginId Username or email
   * @returns Observable<void>
   */
  public requestPasswordReset(loginId: string): Observable<void> {
    const params = new HttpParams().set('loginId', loginId);
    
    return this.http.post<void>(
      `${ADMIN_USER_API.LIST.replace(/\/\?.*/, '')}/request-password-reset`,
      {},
      { params }
    ).pipe(
      tap(() => console.log('✅ Service: Password reset requested')),
      catchError((error: any) => {
        console.error('❌ Service: Error requesting password reset:', error);
        return throwError(() => error);
      })
    );
  }

  // ========================================
  // Data Export & Audit
  // ========================================

  /**
   * Export admin user data
   * 
   * @param id Admin user ID (UUID)
   * @returns Observable of exported data DTO
   */
  public exportAdminData(id: string): Observable<any> {
    return this.http.get<any>(
      `${ADMIN_USER_API.LIST.replace(/\/\?.*/, '')}/${id}/export`
    ).pipe(
      map(response => response.data),
      catchError((error: any) => {
        console.error('❌ Service: Error exporting admin data:', error);
        return throwError(() => error);
      })
    );
  }

  /**
   * Get audit logs for admin user
   * 
   * @param id Admin user ID (UUID)
   * @returns Observable of audit log entries
   */
  public getAuditLogs(id: string): Observable<any[]> {
    return this.http.get<any>(
      `${ADMIN_USER_API.LIST.replace(/\/\?.*/, '')}/${id}/audit-logs`
    ).pipe(
      map(response => response.data || []),
      catchError((error: any) => {
        console.error('❌ Service: Error loading audit logs:', error);
        return throwError(() => error);
      })
    );
  }

  /**
   * Request account deletion for admin user
   * 
   * @param id Admin user ID (UUID)
   * @returns Observable<void>
   */
  public requestAccountDeletion(id: string): Observable<void> {
    return this.http.patch<void>(
      `${ADMIN_USER_API.LIST.replace(/\/\?.*/, '')}/${id}/request-deletion`,
      {}
    ).pipe(
      tap(() => console.log('✅ Service: Account deletion requested')),
      catchError((error: any) => {
        console.error('❌ Service: Error requesting account deletion:', error);
        return throwError(() => error);
      })
    );
  }

  /**
   * Get admins by role
   * 
   * @param roleName Role name to filter by
   * @param page Page number (0-indexed)
   * @param size Page size
   * @returns Observable of paginated response
   */
  public getAdminsByRole(roleName: string, page: number = 0, size: number = 10): Observable<PaginatedAdminUserResponse> {
    const params = new HttpParams()
      .set('roleName', roleName)
      .set('page', page.toString())
      .set('size', size.toString());
    
    return this.http.get<AdminUserListApiResponse>(
      `${ADMIN_USER_API.LIST.replace(/\/\?.*/, '')}/by-role`,
      { params }
    ).pipe(
      map(response => response.data),
      catchError((error: any) => {
        console.error('❌ Service: Error loading admins by role:', error);
        return throwError(() => error);
      })
    );
  }

  /**
   * Autocomplete search for admin users
   * 
   * @param query Search query (name, username, or email)
   * @param page Page number (0-indexed)
   * @param size Page size
   * @returns Observable of paginated response
   */
  public searchAdminsAutocomplete(query: string, page: number = 0, size: number = 10): Observable<PaginatedAdminUserResponse> {
    const params = new HttpParams()
      .set('query', query)
      .set('page', page.toString())
      .set('size', size.toString());
    
    return this.http.get<AdminUserListApiResponse>(
      `${ADMIN_USER_API.LIST.replace(/\/\?.*/, '')}/autocomplete`,
      { params }
    ).pipe(
      map(response => response.data),
      catchError((error: any) => {
        console.error('❌ Service: Error searching admins:', error);
        return throwError(() => error);
      })
    );
  }

  /**
   * Get all available roles from RBAC service
   * Used for populating role dropdown in create/edit dialogs
   * 
   * @returns Observable of available roles array
   */
  public getAvailableRoles(): Observable<any[]> {
    return this.http.get<any>('/api/v1/admin/access/roles', {
      params: new HttpParams().set('page', '0').set('size', '100')
    }).pipe(
      map(response => {
        // Extract roles from paginated response
        if (response?.data?.content) {
          return response.data.content; // Page<RoleResponseDto> format
        }
        return [];
      }),
      catchError((error: any) => {
        console.error('❌ Service: Error loading available roles:', error);
        return throwError(() => error);
      })
    );
  }
}

