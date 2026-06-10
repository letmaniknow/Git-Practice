package com.mmva.newsapp.domain.newscategory.enums;

/**
 * Enum representing the status of a news category.
 * 
 * Statuses:
 * - ACTIVE: Category is active and visible
 * - INACTIVE: Category is inactive and hidden
 * - DELETED: Category is soft-deleted (logically deleted)
 */
public enum NewsCategoryStatus {
    ACTIVE("Active"),
    INACTIVE("Inactive"),
    DELETED("Deleted");

    private final String displayName;

    NewsCategoryStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
