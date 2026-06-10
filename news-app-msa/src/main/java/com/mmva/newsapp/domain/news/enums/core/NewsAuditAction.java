package com.mmva.newsapp.domain.news.enums.core;

/**
 * Enum representing audit actions for news article operations.
 * 
 * @author MMVA News Team
 * @since 1.0.0
 */
public enum NewsAuditAction {
    // Content Lifecycle
    CREATE("News article created"),
    UPDATE("News article updated"),
    DELETE("News article deleted (soft)"),
    PERMANENT_DELETE("News article permanently deleted"),
    RESTORE("News article restored"),

    // Draft Operations
    DRAFT_CREATE("Draft news article created"),
    DRAFT_UPDATE("Draft news article updated"),

    // Workflow Management
    WORKFLOW_UPDATE("Workflow status changed"),
    SCHEDULE_PUBLISH("News scheduled for publication"),
    SCHEDULED_PUBLISH("Scheduled news published automatically"),
    PUBLISH("News article published"),
    UNPUBLISH("News article unpublished"),

    // Archive Operations
    ARCHIVE("News article archived"),
    UNARCHIVE("News article unarchived"),

    // Featured/Pin Operations
    PIN("News article pinned as featured"),
    UNPIN("News article unpinned"),

    // Bulk Operations
    BULK_PUBLISH("Bulk news articles published"),
    BULK_DELETE("Bulk news articles deleted"),
    BULK_ARCHIVE("Bulk news articles archived"),

    // Media Operations
    MEDIA_UPLOAD("Media file uploaded"),
    MEDIA_DELETE("Media file deleted"),
    THUMBNAIL_UPLOAD("Thumbnail uploaded"),
    THUMBNAIL_DELETE("Thumbnail deleted"),

    // Version Control
    VERSION_CREATE("New version created"),
    VERSION_RESTORE("Previous version restored");

    private final String description;

    NewsAuditAction(String description) {
        this.description = description;
    }

    /**
     * Gets the human-readable description of the audit action.
     *
     * @return the description of the audit action
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets the enum value as a lowercase string for database storage.
     *
     * @return the enum name in lowercase
     */
    public String getValue() {
        return this.name().toLowerCase();
    }
}
