package com.mmva.newsapp.domain.newssourceagency.enums.core;

/**
 * Enumeration of audit actions for news source agency operations.
 * Used to track changes in the news_source_agencies_audit_log table.
 *
 * @author MMVA Team
 * @since 1.0.0
 */
public enum NewsSourceAgencyAuditAction {

    /**
     * Agency was created.
     */
    CREATE("Agency created"),

    /**
     * Agency was updated.
     */
    UPDATE("Agency updated"),

    /**
     * Agency was soft-deleted.
     */
    DELETE("Agency deleted"),

    /**
     * Agency was restored from soft-delete.
     */
    RESTORE("Agency restored"),

    /**
     * Agency trust status was changed.
     */
    TRUST_CHANGED("Agency trust status changed"),

    /**
     * Agency active status was changed.
     */
    STATUS_CHANGED("Agency active status changed");

    private final String description;

    NewsSourceAgencyAuditAction(String description) {
        this.description = description;
    }

    /**
     * Gets the human-readable description of the action.
     *
     * @return the action description
     */
    public String getDescription() {
        return description;
    }
}
