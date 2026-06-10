// ✅ V1 ADMIN USER MANAGEMENT MODELS (matching backend API contract)

// ========================================
// Admin Status Enum
// ========================================
/**
 * Admin user account status.
 * Defines the lifecycle state of an admin account.
 */
export enum AdminStatus {
  /** Account is active and can login */
  ACTIVE = 'ACTIVE',

  /** Account is deactivated (voluntary or by admin) */
  INACTIVE = 'INACTIVE',

  /** Account is temporarily suspended */
  SUSPENDED = 'SUSPENDED',

  /** Account is pending email verification */
  PENDING = 'PENDING',

  /** Account is soft deleted */
  DELETED = 'DELETED',

  /** Account is permanently banned */
  BANNED = 'BANNED'
}

// ========================================
// Response DTO (from backend)
// ========================================
/**
 * AdminUserResponseDto - Complete admin user profile
 * 
 * Returned by all GET operations and state-changing operations.
 * Contains 48+ fields covering identity, security, profile, audit, and activity data.
 * 
 * Field Categories:
 * - Identity: adminUsersId, adminUsersUsername, adminUsersEmail
 * - Contact: adminUsersPhoneNumber, adminUsersPhoneVerified
 * - Profile: firstName, lastName, fullName, avatarUrl
 * - Status & Role: adminUsersStatus, adminUsersRoleId, adminUsersRoleName, accountLocked
 * - Verification: emailVerified, emailVerificationCode, emailVerificationExpiresAt
 * - Security: failedLoginAttempts, accountLockExpiresAt, mfaEnabled, lastLogin
 * - Audit: createdAt, updatedAt, deletedAt, createdBy, updatedBy, deletedBy
 * - Other: authProvider, notes
 */
export interface AdminUserResponseDto {
  // ========================================
  // Identity Fields
  // ========================================
  /** Unique identifier (UUID) for the admin user */
  adminUsersId: string;

  /** Unique username for login */
  adminUsersUsername: string;

  /** Primary email address */
  adminUsersEmail: string;

  // ========================================
  // Contact Information
  // ========================================
  /** Phone number (optional) */
  adminUsersPhoneNumber?: string;

  /** Email verified status */
  adminUsersEmailVerified: boolean;

  /** Phone verified status */
  adminUsersPhoneVerified?: boolean;

  // ========================================
  // Profile Information
  // ========================================
  /** First name */
  adminUsersFirstName?: string;

  /** Last name */
  adminUsersLastName?: string;

  /** Full name (computed or stored) */
  adminUsersFullName?: string;

  /** Avatar/profile picture URL */
  adminUsersAvatarUrl?: string;

  // ========================================
  // Status & Role
  // ========================================
  /** Current account status */
  adminUsersStatus: AdminStatus;

  /** Assigned role ID (UUID) */
  adminUsersRoleId: string;

  /** Assigned role name */
  adminUsersRoleName: string;

  /** Whether account is locked due to failed attempts */
  adminUsersAccountLocked: boolean;

  // ========================================
  // Verification (Email)
  // ========================================
  /** Verification code for email */
  adminUsersEmailVerificationCode?: string;

  /** Email verification code expiration time */
  adminUsersEmailVerificationExpiresAt?: string;

  // ========================================
  // Security Metrics
  // ========================================
  /** Count of failed login attempts */
  adminUsersFailedLoginAttempts: number;

  /** Time when account lock expires (if locked) */
  adminUsersAccountLockExpiresAt?: string;

  /** Multi-factor authentication enabled */
  adminUsersMfaEnabled: boolean;

  // ========================================
  // Activity Tracking
  // ========================================
  /** Last successful login timestamp */
  adminUsersLastLogin?: string;

  // ========================================
  // Audit Trail
  // ========================================
  /** Record creation timestamp (ISO 8601) */
  createdAt: string;

  /** Last update timestamp (ISO 8601) */
  updatedAt?: string;

  /** Soft delete timestamp (ISO 8601) */
  deletedAt?: string;

  /** User ID who created this record (UUID) */
  createdBy?: string;

  /** User ID who last updated this record (UUID) */
  updatedBy?: string;

  /** User ID who deleted this record (UUID) */
  deletedBy?: string;

  // ========================================
  // Authentication Provider
  // ========================================
  /** OAuth/Auth provider (if using federated identity) */
  adminUsersAuthProvider?: string;

  // ========================================
  // Additional Fields
  // ========================================
  /** Admin notes/comments */
  adminUsersNotes?: string;
}

// ========================================
// Create Request DTO (for POST /admin/staff)
// ========================================
/**
 * AdminUserCreateRequestDto - Request to create a new admin user
 * 
 * Validation Rules:
 * - username: @NotBlank, @Size(min=3, max=255)
 * - email: @NotBlank, @Email
 * - password: @NotBlank, @Size(min=8)
 * - firstName, lastName, fullName: @Size(max=100/255)
 * - phoneNumber: @Size(max=30)
 * - notes: @Size(max=1000)
 */
export interface AdminUserCreateRequestDto {
  // ========================================
  // Required Fields
  // ========================================
  /** Username (3-255 chars) - must be unique */
  adminUsersUsername: string;

  /** Email address - must be valid email format and unique */
  adminUsersEmail: string;

  /** Password (min 8 chars) - will be hashed before storage */
  adminUsersPassword: string;

  // ========================================
  // Optional Profile Fields
  // ========================================
  /** First name (max 100 chars) */
  adminUsersFirstName?: string;

  /** Last name (max 100 chars) */
  adminUsersLastName?: string;

  /** Full name (max 255 chars) */
  adminUsersFullName?: string;

  /** Avatar URL (max 255 chars) */
  adminUsersAvatarUrl?: string;

  /** Phone number (max 30 chars) */
  adminUsersPhoneNumber?: string;

  // ========================================
  // Role & Status
  // ========================================
  /** Role ID to assign (UUID) */
  adminUsersRoleId?: string;

  /** Role name to assign */
  adminUsersRoleName?: string;

  /** Initial account status (defaults to ACTIVE) */
  adminUsersStatus?: AdminStatus;

  // ========================================
  // Optional Fields
  // ========================================
  /** Whether phone is pre-verified */
  adminUsersPhoneVerified?: boolean;

  /** Authentication provider */
  adminUsersAuthProvider?: string;

  /** Whether account should be locked initially */
  adminUsersAccountLocked?: boolean;

  /** Admin notes (max 1000 chars) */
  adminUsersNotes?: string;
}

// ========================================
// Update Request DTO (for PUT /admin/staff/{id})
// ========================================
/**
 * AdminUserUpdateRequestDto - Request to update an existing admin user
 * 
 * All fields are optional (PATCH semantics).
 * Password cannot be updated via this endpoint - use dedicated password change endpoint.
 * 
 * Validation Rules (same as Create, but all optional):
 * - username: @Size(min=3, max=255) if provided
 * - email: @Email if provided
 * - other string fields: same max length constraints
 */
export interface AdminUserUpdateRequestDto {
  // ========================================
  // Optional Profile Fields
  // ========================================
  /** Username (3-255 chars) - must be unique */
  adminUsersUsername?: string;

  /** Email address - must be valid email format and unique */
  adminUsersEmail?: string;

  /** First name (max 100 chars) */
  adminUsersFirstName?: string;

  /** Last name (max 100 chars) */
  adminUsersLastName?: string;

  /** Full name (max 255 chars) */
  adminUsersFullName?: string;

  /** Avatar URL (max 255 chars) */
  adminUsersAvatarUrl?: string;

  /** Phone number (max 30 chars) */
  adminUsersPhoneNumber?: string;

  // ========================================
  // Role & Status
  // ========================================
  /** Role ID to assign (UUID) */
  adminUsersRoleId?: string;

  /** Role name to assign */
  adminUsersRoleName?: string;

  /** Account status */
  adminUsersStatus?: AdminStatus;

  // ========================================
  // Optional Fields
  // ========================================
  /** Whether phone is verified */
  adminUsersPhoneVerified?: boolean;

  /** Authentication provider */
  adminUsersAuthProvider?: string;

  /** Whether account is locked */
  adminUsersAccountLocked?: boolean;

  /** Admin notes (max 1000 chars) */
  adminUsersNotes?: string;
}

// ========================================
// Paginated Response Wrapper
// ========================================
/**
 * PaginatedAdminUserResponse - Spring Data paginated response
 * 
 * Wraps list of admin users with pagination metadata.
 * Matches Spring Data Page<T> response structure.
 */
export interface PaginatedAdminUserResponse {
  /** List of admin users in current page */
  content: AdminUserResponseDto[];

  /** Pagination metadata */
  pageable: {
    pageNumber: number;
    pageSize: number;
    sort?: {
      sorted: boolean;
      direction: string;
    };
  };

  /** Total number of records matching query */
  totalElements: number;

  /** Total number of pages available */
  totalPages: number;

  /** Whether this is the last page */
  last: boolean;

  /** Whether this is the first page */
  first: boolean;

  /** Whether content list is empty */
  empty: boolean;
}

// ========================================
// API Response Wrapper
// ========================================
/**
 * ApiResponseDtoWrapper<T> - Backend API response envelope
 * 
 * All backend endpoints wrap the actual data in a status envelope.
 * This matches the global ApiResponseDto structure from the backend.
 */
export interface ApiResponseDtoWrapper<T> {
  /** Operation status (success/error) */
  status: string;

  /** Human-readable message */
  message: string;

  /** Server timestamp */
  timestamp: string;

  /** Actual response data */
  data: T;
}

/**
 * AdminUserListApiResponse - Type-safe wrapper for admin users list endpoint
 * 
 * The backend returns: { status, message, timestamp, data: PaginatedAdminUserResponse }
 */
export type AdminUserListApiResponse = ApiResponseDtoWrapper<PaginatedAdminUserResponse>;

// ========================================
// Frontend View Models (for UI state management)
// ========================================
/**
 * AdminUserViewModel - Enhanced model for UI rendering
 * 
 * Extends AdminUserResponseDto with client-side state for UI components.
 * Used internally by components for display logic and interactions.
 */
export interface AdminUserViewModel extends AdminUserResponseDto {
  /** Row selection state (for bulk operations) */
  isSelected?: boolean;

  /** Row-level loading state (during update/delete) */
  isLoading?: boolean;

  /** Friendly display status (translated label) */
  displayStatus?: string;

  /** Computed display name (fullName or username) */
  displayName?: string;

  /** Whether row is in edit mode */
  isEditing?: boolean;

  /** Error message if operation failed */
  errorMessage?: string;
}

/**
 * AdminUserFilterOptions - Filter state for list operations
 * 
 * Used to track current filter/sort/search state in components.
 */
export interface AdminUserFilterOptions {
  /** Search query string */
  searchText?: string;

  /** Filter by status */
  status?: AdminStatus;

  /** Filter by role */
  roleId?: string;

  /** Only show active users */
  onlyActive?: boolean;

  /** Sort field */
  sortBy?: 'username' | 'email' | 'createdAt' | 'lastLogin';

  /** Sort direction */
  sortDirection?: 'asc' | 'desc';

  /** Current page number (0-indexed) */
  page?: number;

  /** Page size */
  pageSize?: number;
}

/**
 * AdminUserState - Component-level state snapshot
 * 
 * Represents the complete state of admin user management.
 * Used by components and services for state management.
 */
export interface AdminUserState {
  /** List of admin users in current view */
  users: AdminUserViewModel[];

  /** Total count of users matching filter */
  totalCount: number;

  /** Current page number */
  currentPage: number;

  /** Total pages available */
  totalPages: number;

  /** Whether data is currently loading */
  loading: boolean;

  /** Error message if operation failed */
  error: string | null;

  /** Last operation performed */
  lastOperation?: 'created' | 'updated' | 'deleted' | 'restored' | 'activated' | 'suspended';

  /** ID of last affected user */
  lastAffectedUserId?: string;

  /** Current filter/sort options */
  filterOptions: AdminUserFilterOptions;

  /** Selected user for detail view */
  selectedUser?: AdminUserViewModel;
}
