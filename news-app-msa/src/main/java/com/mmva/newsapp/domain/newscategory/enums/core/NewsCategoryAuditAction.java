package com.mmva.newsapp.domain.newscategory.enums.core;

/**
 * Enumeration of audit actions for news category operations.
 * Used to track changes in the news_categories_audit_log table.
 *
 * @author MMVA Team
 * @since 1.0.0
 */
public enum NewsCategoryAuditAction {

    /**
     * Category was created.
     */
    CREATE("Category created"),

    /**
     * Category was updated.
     */
    UPDATE("Category updated"),

    /**
     * Category was soft-deleted.
     */
    DELETE("Category deleted"),

    /**
     * Category was restored from soft-delete.
     */
    RESTORE("Category restored"),

    /**
     * Category was activated.
     */
    ACTIVATE("Category activated"),

    /**
     * Category was deactivated.
     */
    DEACTIVATE("Category deactivated");

    private final String description;

    NewsCategoryAuditAction(String description) {
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
