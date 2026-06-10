// ✅ Admin User Management Feature - Barrel Exports

// ========================================
// Models
// ========================================
export type {
  AdminUserResponseDto,
  AdminUserCreateRequestDto,
  AdminUserUpdateRequestDto,
  AdminUserViewModel,
  AdminUserFilterOptions,
  AdminUserState,
  PaginatedAdminUserResponse,
  ApiResponseDtoWrapper,
  AdminUserListApiResponse
} from './models/admin-user.model';

export { AdminStatus } from './models/admin-user.model';

// ========================================
// Services
// ========================================
export { AdminUserService } from './services/admin-user.service';

// ========================================
// Constants
// ========================================
export {
  ADMIN_USER_API,
  ADMIN_STATUS_LABELS,
  ADMIN_STATUS_COLORS,
  ADMIN_STATUS_BADGE_COLORS,
  ADMIN_USER_TABLE_COLUMNS,
  ADMIN_USER_COLUMN_HEADERS,
  ADMIN_USER_COLUMN_WIDTHS,
  ADMIN_USER_FORM_VALIDATION_MESSAGES,
  ADMIN_USER_PAGINATION_DEFAULTS,
  ADMIN_USER_CONFIG,
  ADMIN_USER_OPERATION_MESSAGES,
  ADMIN_USER_PATTERNS
} from './constants/admin-user-api.constant';

// ========================================
// Components
// ========================================
export { AdminUserDetailComponent } from './components/admin-user-detail/admin-user-detail.component';
export { AdminUserChangePasswordDialogComponent } from './components/admin-user-change-password-dialog/admin-user-change-password-dialog.component';
export { AdminUserChangeRoleDialogComponent } from './components/admin-user-change-role-dialog/admin-user-change-role-dialog.component';
export { AdminUserManageRolesDialogComponent } from './components/admin-user-manage-roles-dialog/admin-user-manage-roles-dialog.component';
