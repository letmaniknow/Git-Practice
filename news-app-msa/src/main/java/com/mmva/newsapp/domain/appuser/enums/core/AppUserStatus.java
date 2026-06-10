package com.mmva.newsapp.domain.appuser.enums.core;

/**
 * Enumeration representing the status of a user account.
 * This is the single source of truth for user account status.
 * 
 * @author MMVA News Team
 * @since 1.0.0
 */
public enum AppUserStatus {
    /** User account is active and can log in */
    ACTIVE,

    /** User account is inactive (disabled by admindashboard or user) */
    INACTIVE,

    /** User account is temporarily suspended */
    SUSPENDED,

    /** User account is pending verification/approval */
    PENDING,

    /** User account has been soft-deleted */
    DELETED,

    /** User account is banned */
    BANNED
}
