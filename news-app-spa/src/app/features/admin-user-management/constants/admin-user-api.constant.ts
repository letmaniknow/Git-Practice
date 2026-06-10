// ✅ V1 ADMIN USER MANAGEMENT API ENDPOINTS & CONSTANTS

import { AdminStatus } from '../models/admin-user.model';

// ========================================
// API Endpoints
// ========================================
/**
 * Admin User API Endpoints
 * 
 * Defines all available REST API endpoints for admin user management.
 * Base path: /api/v1/admin/staff (internal admin staff management)
 */
export const ADMIN_USER_API = {
  // ========================================
  // CRUD Operations
  // ========================================
  /** GET - Retrieve paginated list of all admin staff users (including deleted) */
  LIST: '/api/v1/admin/staff',

  /** GET - Retrieve single admin staff user by ID */
  GET_BY_ID: '/api/v1/admin/staff/{id}',

  /** POST - Create new admin staff user */
  CREATE: '/api/v1/admin/staff',

  /** PUT - Update existing admin staff user (partial update) */
  UPDATE: '/api/v1/admin/staff/{id}',

  /** DELETE - Soft delete admin staff user */
  DELETE: '/api/v1/admin/staff/{id}',

  // ========================================
  // Search & Filter Operations
  // ========================================
  /** GET - Search active admin staff users by name/username/email with autocomplete */
  SEARCH_ACTIVE: '/api/v1/admin/staff/search',

  /** GET - Retrieve only active (non-deleted) admin staff users with pagination */
  LIST_ACTIVE: '/api/v1/admin/staff/active',

  /** GET - Retrieve only deleted admin staff users with pagination */
  LIST_DELETED: '/api/v1/admin/staff/deleted',

  // ========================================
  // Lookup by Unique Fields
  // ========================================
  /** GET - Retrieve admin staff user by username */
  GET_BY_USERNAME: '/api/v1/admin/staff/username/{username}',

  /** GET - Retrieve admin staff user by email */
  GET_BY_EMAIL: '/api/v1/admin/staff/email/{email}',

  // ========================================
  // Status Operations
  // ========================================
  /** POST - Activate admin staff user (change status to ACTIVE) */
  ACTIVATE: '/api/v1/admin/staff/{id}/activate',

  /** POST - Deactivate admin staff user (change status to INACTIVE) */
  DEACTIVATE: '/api/v1/admin/staff/{id}/deactivate',

  /** POST - Suspend admin staff user (change status to SUSPENDED) */
  SUSPEND: '/api/v1/admin/staff/{id}/suspend',

  /** POST - Restore soft-deleted admin staff user */
  RESTORE: '/api/v1/admin/staff/{id}/restore'
};

// ========================================
// Admin Status Display Configuration
// ========================================
/**
 * User-friendly labels for admin status values
 * Used in dropdowns, filters, and status displays
 */
export const ADMIN_STATUS_LABELS: Record<AdminStatus, string> = {
  [AdminStatus.ACTIVE]: 'Active',
  [AdminStatus.INACTIVE]: 'Inactive',
  [AdminStatus.SUSPENDED]: 'Suspended',
  [AdminStatus.PENDING]: 'Pending Verification',
  [AdminStatus.DELETED]: 'Deleted',
  [AdminStatus.BANNED]: 'Banned'
};

/**
 * Material Design color codes for admin status badges
 * Used in tables, chips, and status indicators
 */
export const ADMIN_STATUS_COLORS: Record<AdminStatus, string> = {
  [AdminStatus.ACTIVE]: '#4caf50',        // Green - Success
  [AdminStatus.INACTIVE]: '#2196f3',      // Blue - Info (Deactivated)
  [AdminStatus.SUSPENDED]: '#ff9800',    // Orange - Warning
  [AdminStatus.PENDING]: '#2196f3',      // Blue - Info
  [AdminStatus.DELETED]: '#f44336',      // Red - Danger
  [AdminStatus.BANNED]: '#b71c1c'        // Dark Red - Critical
};

/**
 * Material Design background colors for status chips
 * Lighter versions for better contrast with text
 */
export const ADMIN_STATUS_BADGE_COLORS: Record<AdminStatus, { bg: string; text: string }> = {
  [AdminStatus.ACTIVE]: { bg: '#e8f5e9', text: '#2e7d32' },        // Light Green
  [AdminStatus.INACTIVE]: { bg: '#e3f2fd', text: '#1565c0' },      // Light Blue (Deactivated)
  [AdminStatus.SUSPENDED]: { bg: '#fff3e0', text: '#e65100' },     // Light Orange
  [AdminStatus.PENDING]: { bg: '#e3f2fd', text: '#1565c0' },       // Light Blue
  [AdminStatus.DELETED]: { bg: '#ffebee', text: '#c62828' },       // Light Red
  [AdminStatus.BANNED]: { bg: '#f3e5f5', text: '#6a1b9a' }         // Light Purple
};

// ========================================
// Table Display Configuration
// ========================================
/**
 * Columns to display in admin user list table
 * Order determines display sequence
 */
export const ADMIN_USER_TABLE_COLUMNS = [
  'username',
  'email',
  'fullName',
  'status',
  'role',
  'lastLogin',
  'createdAt',
  'actions'
] as const;

/**
 * Column header labels (human-readable)
 */
export const ADMIN_USER_COLUMN_HEADERS: Record<typeof ADMIN_USER_TABLE_COLUMNS[number], string> = {
  username: 'Username',
  email: 'Email',
  fullName: 'Full Name',
  status: 'Status',
  role: 'Role',
  lastLogin: 'Last Login',
  createdAt: 'Created',
  actions: 'Actions'
};

/**
 * Column widths for material table (flex basis)
 */
export const ADMIN_USER_COLUMN_WIDTHS: Record<typeof ADMIN_USER_TABLE_COLUMNS[number], string> = {
  username: '15%',
  email: '20%',
  fullName: '15%',
  status: '12%',
  role: '12%',
  lastLogin: '15%',
  createdAt: '11%',
  actions: '10%'
};

// ========================================
// Form Validation Messages
// ========================================
/**
 * Error and info messages for form validation
 * Used in form components and validation feedback
 */
export const ADMIN_USER_FORM_VALIDATION_MESSAGES = {
  // ========================================
  // Username Validation
  // ========================================
  username: {
    required: 'Username is required',
    minlength: 'Username must be at least 3 characters long',
    maxlength: 'Username must not exceed 255 characters',
    pattern: 'Username can only contain letters, numbers, and underscores',
    taken: 'This username is already in use'
  },

  // ========================================
  // Email Validation
  // ========================================
  email: {
    required: 'Email is required',
    email: 'Please enter a valid email address',
    maxlength: 'Email must not exceed 255 characters',
    taken: 'This email is already in use'
  },

  // ========================================
  // Password Validation (create only)
  // ========================================
  password: {
    required: 'Password is required',
    minlength: 'Password must be at least 8 characters long',
    pattern: 'Password must include uppercase, lowercase, number, and special character'
  },

  // ========================================
  // Profile Fields
  // ========================================
  firstName: {
    maxlength: 'First name must not exceed 100 characters'
  },

  lastName: {
    maxlength: 'Last name must not exceed 100 characters'
  },

  fullName: {
    maxlength: 'Full name must not exceed 255 characters'
  },

  phone: {
    maxlength: 'Phone number must not exceed 30 characters',
    pattern: 'Please enter a valid phone number'
  },

  notes: {
    maxlength: 'Notes must not exceed 1000 characters'
  },

  // ========================================
  // Role Validation
  // ========================================
  role: {
    required: 'Role selection is required'
  }
};

// ========================================
// Pagination Defaults
// ========================================
/**
 * Default pagination parameters for list operations
 */
export const ADMIN_USER_PAGINATION_DEFAULTS = {
  /** Default page size for admin user lists */
  pageSize: 10,

  /** Available page size options for dropdown */
  pageSizeOptions: [5, 10, 25, 50, 100],

  /** Default sort field */
  defaultSort: 'createdAt',

  /** Default sort direction */
  defaultDirection: 'desc' as const
};

// ========================================
// Feature Feature Flags & Limits
// ========================================
/**
 * Feature configuration for admin user management
 */
export const ADMIN_USER_CONFIG = {
  /** Maximum concurrent admin operations */
  maxConcurrentOperations: 10,

  /** Enable bulk delete operations */
  enableBulkDelete: true,

  /** Enable bulk status change operations */
  enableBulkStatusChange: true,

  /** Enable export to CSV */
  enableExport: true,

  /** Enable import from CSV */
  enableImport: false,

  /** Maximum file size for import (MB) */
  maxImportFileSizeMB: 10,

  /** Cache duration for user list (milliseconds) */
  listCacheDurationMs: 5 * 60 * 1000, // 5 minutes

  /** Debounce delay for search input (milliseconds) */
  searchDebounceMs: 300,

  /** Auto-refresh interval for active list (milliseconds) */
  autoRefreshIntervalMs: 10 * 60 * 1000 // 10 minutes
};

// ========================================
// Operation Messages
// ========================================
/**
 * User-facing messages for operations
 */
export const ADMIN_USER_OPERATION_MESSAGES = {
  // ========================================
  // Success Messages
  // ========================================
  createSuccess: 'Admin user created successfully',
  updateSuccess: 'Admin user updated successfully',
  deleteSuccess: 'Admin user deleted successfully',
  restoreSuccess: 'Admin user restored successfully',
  activateSuccess: 'Admin user activated successfully',
  deactivateSuccess: 'Admin user deactivated successfully',
  suspendSuccess: 'Admin user suspended successfully',
  bulkDeleteSuccess: (count: number) => `${count} admin user(s) deleted successfully`,

  // ========================================
  // Error Messages
  // ========================================
  createError: 'Failed to create admin user',
  updateError: 'Failed to update admin user',
  deleteError: 'Failed to delete admin user',
  restoreError: 'Failed to restore admin user',
  activateError: 'Failed to activate admin user',
  deactivateError: 'Failed to deactivate admin user',
  suspendError: 'Failed to suspend admin user',
  loadError: 'Failed to load admin users',
  notFoundError: 'Admin user not found',
  duplicateError: 'Username or email already exists',

  // ========================================
  // Confirmation Messages
  // ========================================
  deleteConfirmation: (username: string) => `Are you sure you want to delete ${username}? This action can be undone.`,
  restoreConfirmation: (username: string) => `Restore ${username} to active list?`,
  statusChangeConfirmation: (username: string, newStatus: string) =>
    `Change ${username}'s status to ${newStatus}?`,
  bulkDeleteConfirmation: (count: number) =>
    `Are you sure you want to delete ${count} admin user(s)? This action can be undone.`
};

// ========================================
// Regex Patterns for Validation
// ========================================
/**
 * Regular expressions for field validation
 */
export const ADMIN_USER_PATTERNS = {
  /** Username: alphanumeric and underscore only */
  username: /^[a-zA-Z0-9_]+$/,

  /** Phone: basic international format */
  phone: /^[+]?[(]?[0-9]{1,4}[)]?[-\s.]?[(]?[0-9]{1,4}[)]?[-\s.]?[0-9]{1,9}$/,

  /** URL validation for avatar */
  url: /^(https?:\/\/)?([\da-z\.-]+)\.([a-z\.]{2,6})([\/\w \.-]*)*\/?$/
};
