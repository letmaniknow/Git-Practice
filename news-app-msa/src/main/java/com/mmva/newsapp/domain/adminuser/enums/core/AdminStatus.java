package com.mmva.newsapp.domain.adminuser.enums.core;

/**
 * Enum representing the status of an admin user account.
 * 
 * @author MMVA News Team
 * @since 1.0.0
 */
public enum AdminStatus {

    /**
     * Account is active and can login
     */
    ACTIVE,

    /**
     * Account is deactivated (voluntary or by admin)
     */
    INACTIVE,

    /**
     * Account is temporarily suspended
     */
    SUSPENDED,

    /**
     * Account is pending email verification
     */
    PENDING,

    /**
     * Account is soft deleted
     */
    DELETED,

    /**
     * Account is permanently banned
     */
    BANNED
}
