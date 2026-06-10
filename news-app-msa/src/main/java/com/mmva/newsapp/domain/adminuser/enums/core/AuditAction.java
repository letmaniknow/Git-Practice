package com.mmva.newsapp.domain.adminuser.enums.core;

/**
 * Enum representing audit action types for admin user operations.
 * Using an enum instead of magic strings ensures type safety and consistency.
 * 
 * @author MMVA News Team
 * @since 1.0.0
 */
public enum AuditAction {
    // Account Lifecycle
    CREATE("Account created"),
    UPDATE("Account updated"),
    DELETE("Account deleted"),
    SOFT_DELETE("Account soft deleted"),
    RESTORE("Account restored"),

    // Authentication
    LOGIN("User logged in"),
    LOGOUT("User logged out"),
    LOGIN_FAILED("Login attempt failed"),

    // Status Changes
    ACTIVATE("Account activated"),
    DEACTIVATE("Account deactivated"),
    LOCKED("Account locked"),
    UNLOCKED("Account unlocked"),
    SUSPENDED("Account suspended"),

    // Password Management
    CHANGE_PASSWORD("Password changed"),
    PASSWORD_RESET_REQUEST("Password reset requested"),
    PASSWORD_RESET("Password reset completed"),

    // Email Verification
    EMAIL_VERIFICATION_SENT("Email verification sent"),
    VERIFY_EMAIL("Email verified"),

    // Two-Factor Authentication
    TWO_FACTOR_ENABLED("Two-factor authentication enabled"),
    TWO_FACTOR_DISABLED("Two-factor authentication disabled"),
    TWO_FACTOR_VERIFIED("Two-factor authentication verified"),

    // Role Management
    ASSIGN_ROLE("Role assigned"),
    REVOKE_ROLE("Role revoked"),

    // Profile Updates
    PROFILE_UPDATED("Profile updated"),
    UPDATE_PROFILE_PICTURE("Avatar updated"),

    // Data Operations
    DATA_EXPORTED("Data exported"),
    REQUEST_ACCOUNT_DELETION("Account deletion requested");

    private final String description;

    AuditAction(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return name();
    }
}
