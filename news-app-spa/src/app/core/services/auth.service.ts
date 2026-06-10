import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap } from 'rxjs/operators';
import { Router } from '@angular/router';
import { AUTH_API_ENDPOINTS } from '@auth/constants/auth-api.constant';

export interface LoginRequest {
  adminUsersUsernameOrEmail: string;
  adminUsersPassword: string;
}

export interface LoginResponse {
  data: {
    accessToken: string;
    refreshToken: string;
    tokenType: string;
    expiresIn: number;
    userId: string;
    username: string;
    userType: string;
    roles: string[];
    permissions: string[];
  };
}

/**
 * Current authenticated user data
 * Exposes user information for role-based access control
 */
export interface CurrentUser {
  userId: string;
  username: string;
  userType: string;
  roles: string[];  // Array of role strings from JWT
  permissions: string[];
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  // Admin user keys (camelCase for clarity)
  private adminTokenKey = 'adminTokenKey';
  private adminRefreshTokenKey = 'adminRefreshTokenKey';
  private adminTokenTypeKey = 'adminTokenTypeKey';
  private adminExpiresInKey = 'adminExpiresInKey';
  private adminUserIdKey = 'adminUserIdKey';
  private adminUsernameKey = 'adminUsernameKey';
  private adminUserTypeKey = 'adminUserTypeKey';
  private adminRolesKey = 'adminRolesKey';
  private adminPermissionsKey = 'adminPermissionsKey';
  // For appuser, use: appuserTokenKey, appuserRefreshTokenKey, etc.
  private loggedIn$ = new BehaviorSubject<boolean>(this.hasToken());
  
  /**
   * Current authenticated user observable
   * Used for role-based access control in dashboards and components
   * Emits user data after successful login or token refresh
   * Emits null on logout
   * 
   * Initialized from localStorage on app startup to restore user data after page refresh
   */
  readonly currentUser$ = new BehaviorSubject<CurrentUser | null>(this.loadCurrentUserFromStorage());

  constructor(private http: HttpClient, private router: Router) {}

  login(credentials: LoginRequest): Observable<LoginResponse> {
    const url = AUTH_API_ENDPOINTS.login;
    return this.http.post<LoginResponse>(url, credentials).pipe(
      tap(response => {
        const d = response.data;
        if (d?.accessToken) {
          localStorage.setItem(this.adminTokenKey, d.accessToken);
          localStorage.setItem(this.adminRefreshTokenKey, d.refreshToken);
          localStorage.setItem(this.adminTokenTypeKey, d.tokenType || 'Bearer');
          localStorage.setItem(this.adminExpiresInKey, d.expiresIn?.toString() || '');
          localStorage.setItem(this.adminUserIdKey, d.userId || '');
          localStorage.setItem(this.adminUsernameKey, d.username || '');
          localStorage.setItem(this.adminUserTypeKey, d.userType || '');
          localStorage.setItem(this.adminRolesKey, JSON.stringify(d.roles || []));
          localStorage.setItem(this.adminPermissionsKey, JSON.stringify(d.permissions || []));
          this.loggedIn$.next(true);
          
          // Emit current user for role-based access control
          this.currentUser$.next({
            userId: d.userId || '',
            username: d.username || '',
            userType: d.userType || '',
            roles: d.roles || [],
            permissions: d.permissions || []
          });
        }
      })
    );
  }

  logout(): void {
    this.clearAuthStoragePrivate();
    this.loggedIn$.next(false);
    this.currentUser$.next(null);
    this.router.navigate(['/auth/login']);
  }

  /**
   * Future-proof async logout for best practice
   * Handles session cleanup, navigation, and error handling
   */
  async logoutAsync(): Promise<void> {
    try {
      const refreshToken = this.getRefreshToken();
      if (refreshToken) {
        // Call backend to invalidate refresh token
        const url = AUTH_API_ENDPOINTS.logout;
        try {
          await this.http.post(url, { refreshToken }).toPromise();
        } catch (apiError: any) {
          // Log but continue logout flow
          console.warn('Backend logout failed or already invalidated:', apiError?.error?.message);
        }
      }
      this.clearAuthStoragePrivate();
      this.loggedIn$.next(false);
      this.currentUser$.next(null);
      await Promise.resolve(this.router.navigate(['/auth/login']));
    } catch (error) {
      console.error('Async logout failed:', error);
      throw error;
    }
  }

  refreshToken(): Observable<LoginResponse> {
    const url = AUTH_API_ENDPOINTS.refreshToken;
    const refreshToken = this.getRefreshToken();
    return this.http.post<LoginResponse>(url, { refreshToken }).pipe(
      tap(response => {
        const d = response.data;
        if (d?.accessToken) {
          localStorage.setItem(this.adminTokenKey, d.accessToken);
          localStorage.setItem(this.adminTokenTypeKey, d.tokenType || 'Bearer');
          localStorage.setItem(this.adminExpiresInKey, d.expiresIn?.toString() || '');
          localStorage.setItem(this.adminUserIdKey, d.userId || '');
          localStorage.setItem(this.adminUsernameKey, d.username || '');
          localStorage.setItem(this.adminUserTypeKey, d.userType || '');
          localStorage.setItem(this.adminRolesKey, JSON.stringify(d.roles || []));
          localStorage.setItem(this.adminPermissionsKey, JSON.stringify(d.permissions || []));
          // Optionally update refreshToken if backend rotates it
          if (d.refreshToken) {
            localStorage.setItem(this.adminRefreshTokenKey, d.refreshToken);
          }
          this.loggedIn$.next(true);
          
          // Emit current user for role-based access control
          this.currentUser$.next({
            userId: d.userId || '',
            username: d.username || '',
            userType: d.userType || '',
            roles: d.roles || [],
            permissions: d.permissions || []
          });
        }
      })
    );
  }

  getToken(): string | null {
    return localStorage.getItem(this.adminTokenKey);
  }

  /**
   * Set token after refresh - used by interceptor on token refresh
   * @param token The new access token
   */
  setToken(token: string): void {
    localStorage.setItem(this.adminTokenKey, token);
  }

  getRefreshToken(): string | null {
    return localStorage.getItem(this.adminRefreshTokenKey);
  }

  getTokenType(): string | null {
    return localStorage.getItem(this.adminTokenTypeKey);
  }

  getUserId(): string | null {
    return localStorage.getItem(this.adminUserIdKey);
  }

  getUsername(): string | null {
    return localStorage.getItem(this.adminUsernameKey);
  }

  getUserType(): string | null {
    return localStorage.getItem(this.adminUserTypeKey);
  }

  getRoles(): string[] {
    const roles = localStorage.getItem(this.adminRolesKey);
    return roles ? JSON.parse(roles) : [];
  }

  getPermissions(): string[] {
    const permissions = localStorage.getItem(this.adminPermissionsKey);
    return permissions ? JSON.parse(permissions) : [];
  }

  hasRole(role: string): boolean {
    return this.getRoles().includes(role);
  }

  hasPermission(permission: string): boolean {
    return this.getPermissions().includes(permission);
  }

  isLoggedIn(): Observable<boolean> {
    return this.loggedIn$.asObservable();
  }

  private hasToken(): boolean {
    return !!localStorage.getItem(this.adminTokenKey);
  }

  /**
   * Load current user data from localStorage
   * Used to restore user session after page refresh
   * Called on AuthService initialization
   * 
   * @returns CurrentUser object if data exists in localStorage, null otherwise
   */
  private loadCurrentUserFromStorage(): CurrentUser | null {
    const userId = localStorage.getItem(this.adminUserIdKey);
    const username = localStorage.getItem(this.adminUsernameKey);
    const userType = localStorage.getItem(this.adminUserTypeKey);
    const rolesJson = localStorage.getItem(this.adminRolesKey);
    const permissionsJson = localStorage.getItem(this.adminPermissionsKey);

    // Only return user object if we have essential data
    if (userId && username && rolesJson) {
      try {
        return {
          userId,
          username,
          userType: userType || '',
          roles: JSON.parse(rolesJson) || [],
          permissions: permissionsJson ? JSON.parse(permissionsJson) : []
        };
      } catch (error) {
        console.warn('Failed to parse user data from localStorage:', error);
        return null;
      }
    }

    return null;
  }

  /**
   * Clear all authentication data from localStorage
   * Useful for logout and session expiration scenarios
   */
  clearAuthStorage(): void {
    localStorage.removeItem(this.adminTokenKey);
    localStorage.removeItem(this.adminRefreshTokenKey);
    localStorage.removeItem(this.adminTokenTypeKey);
    localStorage.removeItem(this.adminExpiresInKey);
    localStorage.removeItem(this.adminUserIdKey);
    localStorage.removeItem(this.adminUsernameKey);
    localStorage.removeItem(this.adminUserTypeKey);
    localStorage.removeItem(this.adminRolesKey);
    localStorage.removeItem(this.adminPermissionsKey);
  }

  /**
   * Clear all authentication data from localStorage (Private - for internal use)
   * Useful for logout and session expiration scenarios
   */
  private clearAuthStoragePrivate(): void {
    localStorage.removeItem(this.adminTokenKey);
    localStorage.removeItem(this.adminRefreshTokenKey);
    localStorage.removeItem(this.adminTokenTypeKey);
    localStorage.removeItem(this.adminExpiresInKey);
    localStorage.removeItem(this.adminUserIdKey);
    localStorage.removeItem(this.adminUsernameKey);
    localStorage.removeItem(this.adminUserTypeKey);
    localStorage.removeItem(this.adminRolesKey);
    localStorage.removeItem(this.adminPermissionsKey);
  }
}
