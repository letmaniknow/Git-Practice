package com.mmva.newsapp.domain.news.audit.constants;

/**
 * Type-safe action constants for news article audit logging.
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
public class NewsAuditActions {

    public static final String NEWS_CREATED = "NEWS_CREATED";
    public static final String NEWS_PUBLISHED = "NEWS_PUBLISHED";
    public static final String NEWS_UPDATED = "NEWS_UPDATED";
    public static final String NEWS_ARCHIVED = "NEWS_ARCHIVED";
    public static final String NEWS_RESTORED = "NEWS_RESTORED";
    public static final String NEWS_FEATURED = "NEWS_FEATURED";
    public static final String NEWS_UNFEATURED = "NEWS_UNFEATURED";
    public static final String NEWS_MEDIA_UPLOADED = "NEWS_MEDIA_UPLOADED";
    public static final String NEWS_DELETED = "NEWS_DELETED";
    public static final String NEWS_PERMANENT_DELETED = "NEWS_PERMANENT_DELETED";
    public static final String NEWS_BULK_PUBLISHED = "NEWS_BULK_PUBLISHED";
    public static final String NEWS_DRAFT_CREATED = "NEWS_DRAFT_CREATED";
    public static final String NEWS_SCHEDULED_PUBLISHED = "NEWS_SCHEDULED_PUBLISHED";
    public static final String NEWS_CATEGORY_ASSIGNED = "NEWS_CATEGORY_ASSIGNED";
    public static final String NEWS_CATEGORY_REMOVED = "NEWS_CATEGORY_REMOVED";

    private NewsAuditActions() {
        // Utility class - no instantiation
    }
}
