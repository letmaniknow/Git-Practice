package com.mmva.newsapp.infrastructure.rbac.audit.constants;

/**
 * String constants for all RBAC audit actions.
 * 
 * This class serves as the single source of truth for audit action names
 * across the entire RBAC module. Instead of using hardcoded strings scattered
 * throughout the codebase, always reference these constants.
 * 
 * <h2>Benefits</h2>
 * ✅ Type-safe: Compiler catches typos (not runtime)
 * ✅ Searchable: Find all usages of an action
 * ✅ Reusable: Constants across services
 * ✅ Maintainable: Change action name in one place
 * ✅ Self-documenting: Clear what actions exist
 * 
 * <h2>Action Semantics</h2>
 * 
 * <h3>Role Actions</h3>
 * - ROLE_CREATED: Admin created a new role (with initial permissions)
 * - ROLE_UPDATED: Admin modified role details (name, description)
 * - ROLE_DELETED: Admin soft-deleted a role
 * - ROLE_RESTORED: Admin restored a soft-deleted role
 * - ROLE_ACTIVATED: Admin activated a deactivated role
 * - ROLE_DEACTIVATED: Admin deactivated an active role
 * 
 * <h3>Permission Actions</h3>
 * - PERMISSION_CREATED: Admin created a new permission
 * - PERMISSION_UPDATED: Admin modified permission details
 * - PERMISSION_DELETED: Admin soft-deleted a permission
 * - PERMISSION_ENABLED: Admin enabled a disabled permission
 * - PERMISSION_DISABLED: Admin disabled an enabled permission
 * - PERMISSION_RESTORED: Admin restored a soft-deleted permission
 * 
 * <h3>Assignment Actions</h3>
 * - PERMISSION_GRANTED: Admin assigned a permission to a role
 * - PERMISSION_REVOKED: Admin removed a permission from a role
 * - ROLE_ASSIGNED_TO_USER: Admin assigned a role to an admin user
 * - ROLE_REMOVED_FROM_USER: Admin removed a role from an admin user
 * 
 * <h2>Usage Examples</h2>
 * 
 * <pre>
 * // Instead of this (error-prone):
 * auditingUtility.audit("RBAC", actorId, "ROLE_CREATED", roleId, ...);
 * auditingUtility.audit("RBAC", actorId, "RoleCreated", roleId, ...);  // Oops, typo
 * 
 * // Use this (type-safe):
 * auditingUtility.audit("RBAC", actorId, RbacAuditActions.ROLE_CREATED, roleId, ...);
 * </pre>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
public final class RbacAuditActions {

    // Prevent instantiation
    private RbacAuditActions() {
        throw new AssertionError("Cannot instantiate RbacAuditActions");
    }

    // ========================================
    // Role Actions
    // ========================================

    /** Admin created a new role */
    public static final String ROLE_CREATED = "ROLE_CREATED";

    /** Admin updated role details (name, description, etc.) */
    public static final String ROLE_UPDATED = "ROLE_UPDATED";

    /** Admin soft-deleted a role */
    public static final String ROLE_DELETED = "ROLE_DELETED";

    /** Admin restored a soft-deleted role */
    public static final String ROLE_RESTORED = "ROLE_RESTORED";

    /** Admin activated a deactivated role */
    public static final String ROLE_ACTIVATED = "ROLE_ACTIVATED";

    /** Admin deactivated an active role */
    public static final String ROLE_DEACTIVATED = "ROLE_DEACTIVATED";

    // ========================================
    // Permission Actions
    // ========================================

    /** Admin created a new permission */
    public static final String PERMISSION_CREATED = "PERMISSION_CREATED";

    /** Admin updated permission details (name, description, resource, etc.) */
    public static final String PERMISSION_UPDATED = "PERMISSION_UPDATED";

    /** Admin soft-deleted a permission */
    public static final String PERMISSION_DELETED = "PERMISSION_DELETED";

    /** Admin enabled a disabled permission */
    public static final String PERMISSION_ENABLED = "PERMISSION_ENABLED";

    /** Admin disabled an enabled permission */
    public static final String PERMISSION_DISABLED = "PERMISSION_DISABLED";

    /** Admin restored a soft-deleted permission */
    public static final String PERMISSION_RESTORED = "PERMISSION_RESTORED";

    // ========================================
    // Permission-Role Assignment Actions
    // ========================================

    /** Admin granted a permission to a role */
    public static final String PERMISSION_GRANTED = "PERMISSION_GRANTED";

    /** Admin revoked a permission from a role */
    public static final String PERMISSION_REVOKED = "PERMISSION_REVOKED";

    // ========================================
    // Role-User Assignment Actions
    // ========================================

    /** Admin assigned a role to an admin user */
    public static final String ROLE_ASSIGNED_TO_USER = "ROLE_ASSIGNED_TO_USER";

    /** Admin removed a role from an admin user */
    public static final String ROLE_REMOVED_FROM_USER = "ROLE_REMOVED_FROM_USER";
}
