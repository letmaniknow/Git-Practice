package com.mmva.newsapp.infrastructure.rbac;

/**
 * Constants for RBAC permission names used throughout the application.
 * 
 * <p>
 * These permission names should match the values stored in the permissions
 * table.
 * They are used for permission-based authorization checks.
 * </p>
 * 
 * <h2>Naming Convention</h2>
 * <p>
 * Pattern: {@code {domain}.{action}}
 * </p>
 * <ul>
 * <li><b>domain:</b> The resource being accessed (staff, news, category,
 * etc.)</li>
 * <li><b>action:</b> The operation (create, read, update, delete, etc.)</li>
 * </ul>
 * 
 * <h2>Usage Example</h2>
 * 
 * <pre>{@code
 * adminValidationService.validateAdminWithPermission(adminId, RbacPermissionConstants.STAFF_CREATE);
 * }</pre>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
public final class RbacPermissionConstants {

    private RbacPermissionConstants() {
        // Prevent instantiation
    }

    // =========================
    // Staff Management Permissions
    // =========================
    public static final String STAFF_CREATE = "staff.create";
    public static final String STAFF_READ = "staff.read";
    public static final String STAFF_UPDATE = "staff.update";
    public static final String STAFF_DELETE = "staff.delete";
    public static final String STAFF_ACTIVATE = "staff.activate";
    public static final String STAFF_DEACTIVATE = "staff.deactivate";
    public static final String STAFF_RESTORE = "staff.restore";
    public static final String STAFF_ASSIGN_ROLE = "staff.assign_role";
    public static final String STAFF_REVOKE_ROLE = "staff.revoke_role";
    public static final String STAFF_VIEW_AUDIT = "staff.view_audit";
    public static final String STAFF_EXPORT = "staff.export";
    public static final String STAFF_MANAGE_PASSWORD = "staff.manage_password";

    // =========================
    // Public User Management Permissions
    // =========================
    public static final String USER_CREATE = "user.create";
    public static final String USER_READ = "user.read";
    public static final String USER_UPDATE = "user.update";
    public static final String USER_DELETE = "user.delete";
    public static final String USER_ACTIVATE = "user.activate";
    public static final String USER_DEACTIVATE = "user.deactivate";
    public static final String USER_UNLOCK = "user.unlock";
    public static final String USER_VIEW_AUDIT = "user.view_audit";
    public static final String USER_EXPORT = "user.export";
    public static final String USER_BULK_OPERATIONS = "user.bulk_operations";

    // =========================
    // News Management Permissions
    // =========================
    public static final String NEWS_CREATE = "news.create";
    public static final String NEWS_READ = "news.read";
    public static final String NEWS_UPDATE = "news.update";
    public static final String NEWS_DELETE = "news.delete";
    public static final String NEWS_PUBLISH = "news.publish";
    public static final String NEWS_UNPUBLISH = "news.unpublish";
    public static final String NEWS_ARCHIVE = "news.archive";
    public static final String NEWS_FEATURE = "news.feature";
    public static final String NEWS_VIEW_AUDIT = "news.view_audit";
    public static final String NEWS_MODERATE = "news.moderate";

    // =========================
    // Category Management Permissions
    // =========================
    public static final String CATEGORY_CREATE = "category.create";
    public static final String CATEGORY_READ = "category.read";
    public static final String CATEGORY_UPDATE = "category.update";
    public static final String CATEGORY_DELETE = "category.delete";
    public static final String CATEGORY_VIEW_AUDIT = "category.view_audit";

    // =========================
    // Comment Management Permissions
    // =========================
    public static final String COMMENT_READ = "comment.read";
    public static final String COMMENT_DELETE = "comment.delete";
    public static final String COMMENT_MODERATE = "comment.moderate";
    public static final String COMMENT_APPROVE = "comment.approve";
    public static final String COMMENT_REJECT = "comment.reject";

    // =========================
    // Role & Permission Management
    // =========================
    public static final String ROLE_CREATE = "role.create";
    public static final String ROLE_READ = "role.read";
    public static final String ROLE_UPDATE = "role.update";
    public static final String ROLE_DELETE = "role.delete";
    public static final String ROLE_ACTIVATE = "role.activate";
    public static final String ROLE_DEACTIVATE = "role.deactivate";
    public static final String ROLE_ASSIGN_PERMISSION = "role.assign_permission";
    public static final String ROLE_REVOKE_PERMISSION = "role.revoke_permission";
    public static final String ROLE_VIEW_AUDIT = "role.view_audit";

    public static final String PERMISSION_CREATE = "permission.create";
    public static final String PERMISSION_READ = "permission.read";
    public static final String PERMISSION_UPDATE = "permission.update";
    public static final String PERMISSION_DELETE = "permission.delete";
    public static final String PERMISSION_ENABLE = "permission.enable";
    public static final String PERMISSION_DISABLE = "permission.disable";
    public static final String PERMISSION_VIEW_AUDIT = "permission.view_audit";

    // =========================
    // Analytics Permissions
    // =========================
    public static final String ANALYTICS_VIEW = "analytics.view";
    public static final String ANALYTICS_EXPORT = "analytics.export";
    public static final String ANALYTICS_VIEW_DETAILED = "analytics.view_detailed";

    // =========================
    // System/Super Admin Permissions
    // =========================
    public static final String SYSTEM_ADMIN = "system.admin";
    public static final String SYSTEM_FULL_ACCESS = "system.full_access";
}
