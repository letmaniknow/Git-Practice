/**
 * Dashboard Quick Actions Role-Based Permissions Configuration
 * 
 * Defines which roles can access which dashboard features.
 * Controls visibility of quick action buttons on the dashboard.
 * 
 * Each role maps to an array of action IDs they are permitted to access.
 * Actions not in the role's array will be disabled (grayed out) in the UI.
 * 
 * @constant DASHBOARD_ROLE_PERMISSIONS
 * @type {Record<AppRole, string[]>}
 * 
 * ROLES:
 * - EDITOR: Can access News Dashboard and Category Management only
 * - REVIEWER: Can access News, Categories, and Recycle Bin
 * - ADMIN: Full access to all dashboard features
 * - SUPER_ADMIN: Full access to all dashboard features
 */

import { AppRole } from '../../../../core/models';

/**
 * Role-based permission matrix for dashboard quick actions
 * 
 * @example
 * EDITOR role has access to:
 *   ✅ dashboard-news (News Dashboard)
 *   ✅ manage-categories (Category Management)
 *   ❌ manage-users (User Management) - grayed out
 *   ❌ recycle-bin (Recycle Bin) - grayed out
 *   ❌ settings (Settings) - grayed out
 */
export const DASHBOARD_ROLE_PERMISSIONS: Record<AppRole, string[]> = {
  /**
   * EDITOR: Limited access to content-related features
   */
  [AppRole.EDITOR]: [
    'dashboard-news',      // ✅ View news dashboard
    'manage-categories'    // ✅ Manage categories
  ],

  /**
   * REVIEWER: Can review and access content features + recycle bin
   */
  [AppRole.REVIEWER]: [
    'dashboard-news',      // ✅ View news dashboard
    'manage-categories',   // ✅ Manage categories
    'recycle-bin'          // ✅ View recycle bin
  ],

  /**
   * ADMIN: Full administrative access
   */
  [AppRole.ADMIN]: [
    'dashboard-news',      // ✅ News Dashboard
    'manage-users',        // ✅ User Management
    'manage-categories',   // ✅ Category Management
    'recycle-bin',         // ✅ Recycle Bin
    'settings'             // ✅ Settings
  ],

  /**
   * SUPER_ADMIN: Complete system access
   */
  [AppRole.SUPER_ADMIN]: [
    'dashboard-news',      // ✅ News Dashboard
    'manage-users',        // ✅ User Management
    'manage-categories',   // ✅ Category Management
    'recycle-bin',         // ✅ Recycle Bin
    'settings'             // ✅ Settings
  ]
};

/**
 * Helper function to check if a role has access to an action
 * 
 * @param role - The user's role
 * @param actionId - The action to check access for
 * @returns true if role has access, false otherwise
 * 
 * @example
 * hasRolePermission(AppRole.EDITOR, 'dashboard-news') // true
 * hasRolePermission(AppRole.EDITOR, 'manage-users')   // false
 */
export function hasRolePermission(role: AppRole, actionId: string): boolean {
  const permissions = DASHBOARD_ROLE_PERMISSIONS[role];
  return permissions ? permissions.includes(actionId) : false;
}
