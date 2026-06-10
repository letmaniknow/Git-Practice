import { environment } from '@environments/environment';

/**
 * Authentication API Endpoints
 * 
 * Centralized management of all auth-related API endpoints.
 * Feature-owned constants - part of auth feature module.
 * 
 * Usage in services:
 * import { AUTH_API_ENDPOINTS } from '@auth/constants/auth-api.constant';
 * 
 * Example:
 * this.http.post(AUTH_API_ENDPOINTS.login, credentials)
 */

const BASE_URL = environment.apiBaseUrl;

export const AUTH_API_ENDPOINTS = {
  login: `${BASE_URL}/api/v1/admin/auth/login`,
  logout: `${BASE_URL}/api/v1/admin/auth/logout`,
  refreshToken: `${BASE_URL}/api/v1/admin/auth/refresh-token`,
  verify: `${BASE_URL}/api/v1/admin/auth/verify`,
  profile: `${BASE_URL}/api/v1/admin/auth/profile`,
  changePassword: `${BASE_URL}/api/v1/admin/auth/change-password`,
};
