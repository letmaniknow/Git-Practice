/**
 * News Category Management - API Constants & UI Configuration
 * Purpose: Centralized constants for API endpoints, table config, and UI messages
 * Pattern: Follows admin-user-api.constant.ts structure
 */

import { MatSnackBarConfig } from '@angular/material/snack-bar';

/**
 * API_ENDPOINTS - Backend REST API endpoints
 */
export const NEWS_CATEGORY_API_ENDPOINTS = {
  BASE: '/api/v1/admin/news-categories',
  LIST: '/api/v1/admin/news-categories', // GET with pagination
  DETAIL: (id: string) => `/api/v1/admin/news-categories/${id}`,
  CREATE: '/api/v1/admin/news-categories',
  UPDATE: (id: string) => `/api/v1/admin/news-categories/${id}`,
  DELETE: (id: string) => `/api/v1/admin/news-categories/${id}`,
  AUDIT_LOGS: (id: string) => `/api/v1/admin/news-categories/${id}/audit-logs`,
} as const;

/**
 * TABLE_COLUMNS - Material table column definitions
 * Order: column order in table | Display name | Sort field
 */
export const NEWS_CATEGORY_TABLE_COLUMNS = {
  COLUMNS: [
    { key: 'nameEn', label: 'Category (English)', sortable: true },
    { key: 'nameEs', label: 'Categoría (Español)', sortable: true },
    { key: 'slug', label: 'Slug', sortable: true },
    { key: 'description', label: 'Description', sortable: false },
    { key: 'status', label: 'Status', sortable: true },
    { key: 'createdAt', label: 'Created Date', sortable: true },
    { key: 'actions', label: 'Actions', sortable: false },
  ],
  DISPLAY_COLUMNS: ['nameEn', 'nameEs', 'slug', 'description', 'status', 'createdAt', 'actions'],
} as const;

/**
 * PAGINATION - Default pagination settings
 */
export const NEWS_CATEGORY_PAGINATION = {
  DEFAULT_PAGE_SIZE: 10,
  PAGE_SIZE_OPTIONS: [5, 10, 25, 50],
  DEFAULT_PAGE: 0,
  DEFAULT_SORT_BY: 'createdAt',
  DEFAULT_SORT_DIRECTION: 'desc',
} as const;

/**
 * FORM_VALIDATION - Validation rules for form fields
 */
export const NEWS_CATEGORY_FORM_VALIDATION = {
  CATEGORY_NAME_EN: {
    REQUIRED_ERROR: 'English category name is required',
    MIN_LENGTH: 2,
    MAX_LENGTH: 100,
    MIN_ERROR: 'Must be at least 2 characters',
    MAX_ERROR: 'Cannot exceed 100 characters',
    PATTERN: /^[a-zA-Z0-9\s\-&áéíóúñ]+$/,
    PATTERN_ERROR: 'Only alphanumeric, spaces, hyphens, and accents allowed',
  },
  CATEGORY_NAME_ES: {
    REQUIRED_ERROR: 'Spanish category name is required',
    MIN_LENGTH: 2,
    MAX_LENGTH: 100,
    MIN_ERROR: 'Must be at least 2 characters',
    MAX_ERROR: 'Cannot exceed 100 characters',
    PATTERN: /^[a-zA-Z0-9\s\-&áéíóúñ]+$/,
    PATTERN_ERROR: 'Only alphanumeric, spaces, hyphens, and accents allowed',
  },
  SLUG: {
    OPTIONAL: true,
    PATTERN: /^[a-z0-9\-]+$/,
    PATTERN_ERROR: 'Slug must be lowercase alphanumeric with hyphens only',
    HINT: '(Auto-generated if left blank)',
  },
  DESCRIPTION: {
    OPTIONAL: true,
    MAX_LENGTH: 500,
    MAX_ERROR: 'Cannot exceed 500 characters',
  },
} as const;

/**
 * OPERATION_MESSAGES - User feedback messages for operations
 */
export const NEWS_CATEGORY_OPERATION_MESSAGES = {
  CREATE: {
    TITLE: 'Create New Category',
    SUCCESS: 'Category created successfully',
    ERROR: 'Failed to create category',
    CONFIRM_BTN: 'Create',
  },
  EDIT: {
    TITLE: 'Edit Category',
    SUCCESS: 'Category updated successfully',
    ERROR: 'Failed to update category',
    CONFIRM_BTN: 'Update',
  },
  DELETE: {
    TITLE: 'Delete Category',
    CONFIRM_MESSAGE: 'Are you sure you want to delete this category?',
    SUCCESS: 'Category deleted successfully',
    ERROR: 'Failed to delete category',
    CONFIRM_BTN: 'Delete',
  },
  LOAD: {
    ERROR: 'Failed to load categories',
  },
  GENERAL: {
    CANCEL_BTN: 'Cancel',
    CLOSE_BTN: 'Close',
  },
} as const;

/**
 * SNACKBAR_CONFIG - Material snackbar settings
 */
export const NEWS_CATEGORY_SNACKBAR_CONFIG: MatSnackBarConfig = {
  duration: 4000,
  horizontalPosition: 'end',
  verticalPosition: 'bottom',
  panelClass: ['app-snackbar'],
} as const;

/**
 * SNACKBAR_ACTIONS - Snackbar action buttons
 */
export const NEWS_CATEGORY_SNACKBAR_ACTIONS = {
  SUCCESS: { message: '✓', cssClass: 'success-snackbar' },
  ERROR: { message: '✕', cssClass: 'error-snackbar' },
  WARNING: { message: '⚠', cssClass: 'warning-snackbar' },
} as const;

/**
 * CACHE_CONFIG - Cache management keys
 */
export const NEWS_CATEGORY_CACHE = {
  CATEGORIES_CACHE: 'NEWS_CATEGORIES_CACHE',
  CATEGORIES_BY_ID_CACHE: 'NEWS_CATEGORIES_BY_ID_CACHE',
  CATEGORY_LIST_CACHE: 'NEWS_CATEGORY_LIST_CACHE',
  CACHE_TTL_MS: 5 * 60 * 1000, // 5 minutes
} as const;

/**
 * EMPTY_STATES - UI messages for empty states
 */
export const NEWS_CATEGORY_EMPTY_STATES = {
  NO_CATEGORIES: {
    TITLE: 'No Categories Found',
    MESSAGE: 'Create your first category to get started',
    ACTION: 'Create Category',
  },
  NO_RESULTS: {
    TITLE: 'No Results',
    MESSAGE: 'No categories match your search criteria',
    ACTION: 'Clear Filters',
  },
} as const;

/**
 * DIALOG_CONFIG - Material dialog settings
 */
export const NEWS_CATEGORY_DIALOG_CONFIG = {
  FORM_DIALOG: {
    width: '600px',
    maxWidth: '90vw',
    disableClose: false,
    autoFocus: true,
  },
  DELETE_DIALOG: {
    width: '400px',
    maxWidth: '90vw',
    disableClose: false,
  },
} as const;

/**
 * RESPONSIVE - Responsive design breakpoints
 */
export const NEWS_CATEGORY_RESPONSIVE = {
  SMALL_SCREEN: 600,
  MEDIUM_SCREEN: 960,
  LARGE_SCREEN: 1280,
} as const;
