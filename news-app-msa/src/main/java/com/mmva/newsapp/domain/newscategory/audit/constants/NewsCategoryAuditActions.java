package com.mmva.newsapp.domain.newscategory.audit.constants;

/**
 * Type-safe constants for news category audit actions.
 * Replaces magic strings with compile-time constants for audit logging.
 *
 * @author MMVA Team
 * @since 1.0.0
 */
public final class NewsCategoryAuditActions {

    private NewsCategoryAuditActions() {
        // Prevent instantiation
    }

    /**
     * Category was created.
     */
    public static final String CATEGORY_CREATED = "CATEGORY_CREATED";

    /**
     * Category was updated.
     */
    public static final String CATEGORY_UPDATED = "CATEGORY_UPDATED";

    /**
     * Category was deleted (soft delete).
     */
    public static final String CATEGORY_DELETED = "CATEGORY_DELETED";

    /**
     * Category was restored from soft delete.
     */
    public static final String CATEGORY_RESTORED = "CATEGORY_RESTORED";
}
