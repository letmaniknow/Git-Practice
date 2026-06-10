/**
 * Application-Wide Role Enumeration
 * 
 * Defines all available roles in the system.
 * Used across all features for role-based access control.
 * 
 * Roles:
 * - EDITOR: Can create and edit content (limited access)
 * - REVIEWER: Can review and approve content (review-level access)
 * - ADMIN: Full administrative access to features
 * - SUPER_ADMIN: Complete system access, highest privilege level
 * 
 * @usage import { AppRole } from '@core/models';
 */
export enum AppRole {
  /** Editor role - can create/edit content */
  EDITOR = 'EDITOR',

  /** Reviewer role - can review/approve content */
  REVIEWER = 'REVIEWER',

  /** Admin role - full administrative access */
  ADMIN = 'ADMIN',

  /** Super Admin role - complete system access */
  SUPER_ADMIN = 'SUPER_ADMIN'
}
