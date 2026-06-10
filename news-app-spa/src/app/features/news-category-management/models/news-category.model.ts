/**
 * News Category Management - Data Models & Interfaces
 * Purpose: Define all TypeScript interfaces for News Category feature
 * Pattern: Follows admin-user-management structure
 */

/**
 * NewsCategory - Main domain model from backend response
 * Uses exact backend field names for consistency
 */
export interface NewsCategory {
  newsCategoriesId: string; // UUID from backend
  newsCategoriesNameEn: string; // English name (2-100 chars)
  newsCategoriesNameEs: string; // Spanish name (2-100 chars)
  newsCategoriesSlug: string; // URL-friendly slug (auto-generated)
  newsCategoriesDescription?: string; // Optional description (max 500 chars)
  status?: 'ACTIVE' | 'INACTIVE' | 'DELETED'; // Category status
  createdAt: string; // ISO 8601 timestamp
  updatedAt: string; // ISO 8601 timestamp
  createdBy?: string; // Admin user ID who created
  updatedBy?: string; // Admin user ID who last updated
  deletedAt?: string; // ISO 8601 timestamp (null if not deleted)
}

/**
 * NewsCategoryRequestDto - Request payload for create/update operations
 * Sent TO backend when creating or modifying categories
 */
export interface NewsCategoryRequestDto {
  categoryNameEn: string; // REQUIRED: English name
  categoryNameEs: string; // REQUIRED: Spanish name
  slug?: string; // OPTIONAL: Will be auto-generated if not provided
  categoryDescription?: string; // OPTIONAL: Description text
}

/**
 * NewsCategoryResponseDto - Response payload from backend
 * Returned BY backend for all CRUD operations
 * Maps backend field names: newsCategoriesId → id, newsCategoriesNameEn → categoryNameEn, etc.
 */
export interface NewsCategoryResponseDto {
  newsCategoriesId: string;
  newsCategoriesNameEn: string;
  newsCategoriesNameEs: string;
  newsCategoriesSlug: string;
  newsCategoriesDescription?: string;
  status?: 'ACTIVE' | 'INACTIVE' | 'DELETED'; // Category status
  createdAt: string;
  updatedAt: string;
  createdBy?: string;
  updatedBy?: string;
  deletedAt?: string;
}

/**
 * NewsCategoryViewModel - Display/form model for UI rendering
 * Used in components for data binding and form state
 */
export interface NewsCategoryViewModel {
  id?: string; // Optional for create mode
  nameEn: string; // Form field: English name
  nameEs: string; // Form field: Spanish name
  slug?: string; // Form field: Slug (optional, auto-generated)
  description?: string; // Form field: Description
}

/**
 * NewsCategoryPaginatedResponse - Backend response for paginated list
 * Structure: { status, message, timestamp, data: { content, totalElements, totalPages, currentPage } }
 */
export interface NewsCategoryPaginatedResponse {
  content: NewsCategory[]; // Array of categories
  totalElements: number; // Total count of all categories
  totalPages: number; // Total number of pages
  currentPage: number; // Current page number (0-indexed)
  pageSize: number; // Items per page
}

/**
 * NewsCategoryPaginationParams - Query parameters for list endpoint
 * Used when calling backend paginated API
 */
export interface NewsCategoryPaginationParams {
  page: number; // Page number (0-indexed, default: 0)
  pageSize: number; // Items per page (default: 10)
  sortBy?: string; // Sort field (default: createdAt)
  sortDirection?: 'asc' | 'desc'; // Sort direction (default: desc)
  searchTerm?: string; // Optional: search by name
}

/**
 * NewsCategoryFilterOptions - Advanced filter criteria
 * Used for filtered queries and search functionality
 */
export interface NewsCategoryFilterOptions {
  searchTerm?: string; // Search by name or description
  sortBy?: 'name' | 'createdAt' | 'updatedAt'; // Sort field
  sortDirection?: 'asc' | 'desc'; // Sort order
  includeDeleted?: boolean; // Include soft-deleted (admin only)
}

/**
 * DialogResult - Generic dialog result format
 * Returned from dialogs (form, delete confirmation, etc.)
 */
export interface DialogResult {
  mode: 'create' | 'edit' | 'delete' | 'close';
  success: boolean;
  data?: NewsCategory | NewsCategoryViewModel;
  message?: string;
}

/**
 * ApiResponse - Generic API response wrapper
 * All backend responses follow this structure
 */
export interface ApiResponse<T> {
  status: 'success' | 'error';
  message: string;
  timestamp: string;
  data: T;
}
