/**
 * Error Messages Constants
 * Centralized error messages used across the entire application
 * Single source of truth for user-facing error messages
 *
 * PATTERN: Feature + Action = Key
 * Example: NEWS_LOAD_CATEGORIES, AUTH_LOGIN_FAILED
 *
 * BENEFITS:
 * - Consistency: Same error message across all features
 * - Translation: Easy to translate all messages at once
 * - Maintenance: Change message in one place
 * - Testing: Mock error messages easily
 */

export const ERROR_MESSAGES = {
  /**
   * ============================================
   * COMMON / GENERIC ERRORS
   * ============================================
   */
  GENERIC_ERROR: 'Something went wrong. Please try again.',
  NETWORK_ERROR: 'Network connection failed. Please check your internet and try again.',
  TIMEOUT_ERROR: 'Request timed out. Please try again.',
  UNAUTHORIZED: 'Your session has expired. Please log in again.',
  FORBIDDEN: 'You do not have permission to perform this action.',
  NOT_FOUND: 'The requested resource was not found.',
  SERVER_ERROR: 'Server error. Please try again later.',
  VALIDATION_ERROR: 'Invalid input. Please check your data and try again.',

  /**
   * ============================================
   * NEWS FEATURE ERRORS
   * ============================================
   */
  NEWS: {
    LOAD_LIST: 'Failed to load news articles. Please try again.',
    LOAD_DETAILS: 'Failed to load article details. Please try again.',
    CREATE: 'Failed to create article. Please try again.',
    UPDATE: 'Failed to update article. Please try again.',
    DELETE: 'Failed to delete article. Please try again.',
    PUBLISH: 'Failed to publish article. Please try again.',
    UNPUBLISH: 'Failed to unpublish article. Please try again.',
    SEARCH: 'Search failed. Please try again.',
    FILTER: 'Failed to apply filters. Please try again.',
  },

  /**
   * ============================================
   * NEWS FILTERS / SEARCH ERRORS
   * ============================================
   */
  NEWS_FILTERS: {
    LOAD_CATEGORIES: 'Failed to load categories. Please try again.',
    LOAD_WORKFLOW_STATUSES: 'Failed to load workflow statuses. Please try again.',
    LOAD_ADMIN_USERS: 'Failed to load admin users. Please try again.',
    LOAD_OPTIONS: 'Failed to load filter options. Please try again.',
  },

  /**
   * ============================================
   * AUTH FEATURE ERRORS
   * ============================================
   */
  AUTH: {
    LOGIN_FAILED: 'Login failed. Please check your credentials and try again.',
    LOGOUT_FAILED: 'Failed to log out. Please try again.',
    TOKEN_REFRESH_FAILED: 'Failed to refresh session. Please log in again.',
    SESSION_EXPIRED: 'Your session has expired. Please log in again.',
    INVALID_CREDENTIALS: 'Invalid email or password.',
    ACCOUNT_LOCKED: 'Your account has been locked. Please contact support.',
    SIGNUP_FAILED: 'Failed to create account. Please try again.',
  },

  /**
   * ============================================
   * UPLOAD / FILE ERRORS
   * ============================================
   */
  UPLOAD: {
    FAILED: 'File upload failed. Please try again.',
    FILE_TOO_LARGE: 'File size is too large. Maximum size is 50MB.',
    INVALID_FILE_TYPE: 'Invalid file type. Please upload a supported format.',
    NO_FILE_SELECTED: 'No file selected. Please choose a file to upload.',
    IMAGE_PROCESSING: 'Failed to process image. Please try again.',
  },

  /**
   * ============================================
   * DASHBOARD / ANALYTICS ERRORS
   * ============================================
   */
  DASHBOARD: {
    LOAD_STATS: 'Failed to load statistics. Please try again.',
    LOAD_CHARTS: 'Failed to load charts. Please try again.',
    EXPORT_FAILED: 'Failed to export data. Please try again.',
  },

  /**
   * ============================================
   * SETTINGS ERRORS
   * ============================================
   */
  SETTINGS: {
    LOAD_FAILED: 'Failed to load settings. Please try again.',
    UPDATE_FAILED: 'Failed to update settings. Please try again.',
    SAVE_FAILED: 'Failed to save changes. Please try again.',
  },

  /**
   * ============================================
   * USER MANAGEMENT ERRORS
   * ============================================
   */
  USERS: {
    LOAD_LIST: 'Failed to load users. Please try again.',
    LOAD_DETAILS: 'Failed to load user details. Please try again.',
    CREATE: 'Failed to create user. Please try again.',
    UPDATE: 'Failed to update user. Please try again.',
    DELETE: 'Failed to delete user. Please try again.',
    CHANGE_ROLE: 'Failed to change user role. Please try again.',
  },
};



/**
 * HTTP STATUS CODE MESSAGES
 * Fallback messages based on HTTP status codes
 * Used when no specific error message is available
 */
export const HTTP_STATUS_MESSAGES: Record<number, string> = {
  400: 'Bad request. Please check your input and try again.',
  401: 'Unauthorized. Please log in again.',
  403: 'Forbidden. You do not have permission.',
  404: 'Not found. The resource does not exist.',
  408: 'Request timeout. The server took too long to respond. Please try again.',
  409: 'Conflict. The data has been modified. Please refresh and try again.',
  413: 'Payload too large. The file or request is too large. Please try a smaller file.',
  422: 'Validation failed. Please check your input.',
  429: 'Too many requests. Please wait a moment before trying again.',
  500: 'Server error. Please try again later.',
  502: 'Bad gateway. Please try again later.',
  503: 'Service unavailable. Please try again later.',
  504: 'Gateway timeout. Please try again later.',
};


