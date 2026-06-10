/**
 * Dashboard Action Handler Service
 * Centralized navigation handler for all dashboard quick actions
 * 
 * Manages routing logic for all dashboard phases:
 * - Phase 1: News (dashboard-news)
 * - Phase 2: Admin User Management (manage-users)
 * - Phase 3: Category Management (manage-categories)
 * - Phase 4: Recycle Bin (recycle-bin)
 * - Phase 5: Settings (settings)
 * 
 * Also handles role-based access control for quick actions.
 */

import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { AppRole } from '../../../../core/models';
import { QuickAction } from '../models/dashboard-quick-actions.model';
import { DASHBOARD_ROLE_PERMISSIONS, hasRolePermission } from '../constants/dashboard-role-permissions.constant';

interface ActionRoute {
  [actionId: string]: string;
}

@Injectable({ providedIn: 'root' })
export class DashboardActionHandlerService {
  /**
   * Maps quick action IDs to their corresponding routes
   * Update this map as new phases are implemented
   */
  private readonly actionRouteMap: ActionRoute = {
    'dashboard-news': '/admin/dashboard',
    'manage-users': '/admin/users',                            // Phase 2 - Admin User Management
    'manage-categories': '/features/dashboard-management/category',   // Phase 3
    'recycle-bin': '/features/dashboard-management/recycle-bin',      // Phase 4
    'settings': '/features/dashboard-management/settings',            // Phase 5
  };

  constructor(private router: Router) {}

  /**
   * Handles dashboard action click
   * Routes to the appropriate feature page based on action ID
   * 
   * @param actionId - ID of the action to handle
   * @returns Promise<boolean> - Navigation success/failure
   * 
   * @example
   * this.actionHandler.handleAction('dashboard-news'); // Routes to news dashboard
   */
  async handleAction(actionId: string): Promise<boolean> {
    const route = this.actionRouteMap[actionId];
    
    if (!route) {
      console.warn(`Unknown action: ${actionId}`);
      return false;
    }

    return this.router.navigate([route]);
  }

  /**
   * Gets the route for a specific action
   * Useful for checking if action exists or getting the route programmatically
   * 
   * @param actionId - ID of the action
   * @returns Route string or null if action not found
   */
  getRoute(actionId: string): string | null {
    return this.actionRouteMap[actionId] || null;
  }

  /**
   * Checks if an action is available/implemented
   * 
   * @param actionId - ID of the action to check
   * @returns boolean - True if action is implemented
   */
  isActionAvailable(actionId: string): boolean {
    return !!this.actionRouteMap[actionId];
  }

  /**
   * Gets all available actions
   * Useful for dynamic UI rendering or admin features
   * 
   * @returns Array of available action IDs
   */
  getAvailableActions(): string[] {
    return Object.keys(this.actionRouteMap);
  }

  /**
   * Gets quick actions filtered by user role
   * Adds disabled flag for actions user cannot access
   * 
   * @param actions - Array of all quick actions
   * @param userRole - User's role from JWT token
   * @returns Array of quick actions with disabled flag set based on role
   * 
   * @example
   * const actions = [{id: 'dashboard-news'}, {id: 'manage-users'}];
   * const filtered = this.getFilteredActionsByRole(actions, AppRole.EDITOR);
   * // Returns: [{id: 'dashboard-news', disabled: false}, {id: 'manage-users', disabled: true}]
   */
  getFilteredActionsByRole(actions: QuickAction[], userRole: AppRole): QuickAction[] {
    return actions.map(action => ({
      ...action,
      disabled: !hasRolePermission(userRole, action.id)
    }));
  }

  /**
   * Checks if user has permission to access a specific action
   * 
   * @param actionId - The action ID to check
   * @param userRole - User's role
   * @returns true if user can access the action, false otherwise
   * 
   * @example
   * this.hasActionPermission('manage-users', AppRole.EDITOR) // false
   * this.hasActionPermission('manage-users', AppRole.ADMIN)  // true
   */
  hasActionPermission(actionId: string, userRole: AppRole): boolean {
    return hasRolePermission(userRole, actionId);
  }

  /**
   * Gets the role permission configuration
   * Useful for admin features or debugging
   * 
   * @param userRole - Optional: get permissions for specific role
   * @returns Permission configuration for all roles or specific role
   */
  getRolePermissions(userRole?: AppRole): Record<AppRole, string[]> | string[] {
    if (userRole) {
      return DASHBOARD_ROLE_PERMISSIONS[userRole] || [];
    }
    return DASHBOARD_ROLE_PERMISSIONS;
  }
}
