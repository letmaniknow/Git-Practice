package com.mmva.newsapp.infrastructure.push.enums;

/**
 * Enum representing the type/category of a push notification.
 * 
 * <p>
 * Used to categorize notifications for filtering, analytics,
 * and user preference management.
 * </p>
 * 
 * <h3>Naming Convention:</h3>
 * <p>
 * Prefixed with {@code PushNotification} per PROJECT_PRINCIPLES.md §6.1
 * Feature-Contextual Naming to clearly indicate entity ownership.
 * </p>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
public enum PushNotificationType {

    /**
     * Urgent breaking news alerts.
     * High priority, sent immediately.
     */
    BREAKING_NEWS,

    /**
     * Regular news article updates.
     */
    NEWS_UPDATE,

    /**
     * New content in a subscribed category.
     */
    CATEGORY_UPDATE,

    /**
     * Daily digest summary.
     */
    DAILY_DIGEST,

    /**
     * Weekly digest summary.
     */
    WEEKLY_DIGEST,

    /**
     * Marketing and promotional notifications.
     */
    PROMOTIONAL,

    /**
     * System announcements and maintenance notices.
     */
    SYSTEM,

    /**
     * AI-recommended personalized content.
     */
    PERSONALIZED,

    /**
     * Reply to user's comment on an article.
     */
    COMMENT_REPLY,

    /**
     * Trending news alert.
     */
    TRENDING
}
