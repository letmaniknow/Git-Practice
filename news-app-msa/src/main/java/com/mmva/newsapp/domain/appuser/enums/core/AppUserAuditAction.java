package com.mmva.newsapp.domain.appuser.enums.core;

/**
 * Enum representing audit actions for user profile operations.
 * 
 * @author MMVA News Team
 * @since 1.0.0
 */
public enum AppUserAuditAction {
    // Account Lifecycle
    CREATE("User profile created"),
    UPDATE("User profile updated"),
    DELETE("User profile deleted"),
    DEACTIVATE("User profile deactivated"),
    ACTIVATE("User profile activated"),

    // Authentication
    LOGIN("User logged in"),
    LOGIN_FAILED("Login attempt failed"),
    LOGOUT("User logged out"),

    // Password Management
    CHANGE_PASSWORD("Password changed"),
    PASSWORD_RESET_REQUESTED("Password reset requested"),
    PASSWORD_RESET_COMPLETED("Password reset completed"),

    // Email Verification
    EMAIL_VERIFICATION_SENT("Email verification code sent"),
    VERIFY_EMAIL("Email verified"),
    EMAIL_VERIFICATION_FAILED("Email verification failed"),

    // Phone Verification
    PHONE_VERIFICATION_SENT("Phone verification code sent"),
    VERIFY_PHONE("Phone verified"),
    PHONE_VERIFICATION_FAILED("Phone verification failed"),

    // Profile Updates
    UPDATE_PROFILE_PICTURE("Profile picture updated"),
    UPDATE_PREFERENCES("Preferences updated"),

    // GDPR & Data
    EXPORT_DATA("User data exported"),
    REQUEST_DELETION("Account deletion requested"),

    // Security
    ACCOUNT_LOCKED("Account locked due to failed attempts"),
    ACCOUNT_UNLOCKED("Account unlocked"),
    MFA_ENABLED("Multi-factor authentication enabled"),
    MFA_DISABLED("Multi-factor authentication disabled");

    private final String description;

    AppUserAuditAction(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
