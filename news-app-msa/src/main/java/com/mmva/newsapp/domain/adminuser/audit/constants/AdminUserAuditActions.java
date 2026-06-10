package com.mmva.newsapp.domain.adminuser.audit.constants;

/**
 * Type-safe constants for admin user audit actions.
 * Replaces magic strings with constants, ensuring consistency across the
 * codebase.
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
public final class AdminUserAuditActions {

    // Account Lifecycle
    public static final String ADMIN_CREATED = "ADMIN_CREATED";
    public static final String ADMIN_UPDATED = "ADMIN_UPDATED";
    public static final String ADMIN_DELETED = "ADMIN_DELETED";
    public static final String ADMIN_RESTORED = "ADMIN_RESTORED";

    // Status Management
    public static final String ADMIN_ACTIVATED = "ADMIN_ACTIVATED";
    public static final String ADMIN_DEACTIVATED = "ADMIN_DEACTIVATED";

    // Authentication & Security
    public static final String ADMIN_LOGIN = "ADMIN_LOGIN";
    public static final String ADMIN_PASSWORD_CHANGED = "ADMIN_PASSWORD_CHANGED";
    public static final String ADMIN_PASSWORD_RESET = "ADMIN_PASSWORD_RESET";

    // Email & Verification
    public static final String ADMIN_EMAIL_VERIFIED = "ADMIN_EMAIL_VERIFIED";
    public static final String ADMIN_EMAIL_VERIFICATION_CODE_SENT = "ADMIN_EMAIL_VERIFICATION_CODE_SENT";

    // Role Management
    public static final String ADMIN_ROLE_ASSIGNED = "ADMIN_ROLE_ASSIGNED";
    public static final String ADMIN_ROLE_REVOKED = "ADMIN_ROLE_REVOKED";

    // Profile Updates
    public static final String ADMIN_AVATAR_UPDATED = "ADMIN_AVATAR_UPDATED";
    public static final String ADMIN_DATA_EXPORTED = "ADMIN_DATA_EXPORTED";

    private AdminUserAuditActions() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
}
