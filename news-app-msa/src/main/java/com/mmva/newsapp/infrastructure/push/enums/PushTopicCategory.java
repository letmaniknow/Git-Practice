package com.mmva.newsapp.infrastructure.push.enums;

/**
 * Enum representing the category of a push notification topic.
 * 
 * <p>
 * Used to group topics for UI display and management.
 * Topics follow naming pattern: {category}_{identifier}
 * </p>
 * 
 * <h3>Topic Naming Examples:</h3>
 * <ul>
 * <li>GLOBAL: all_news, breaking_news</li>
 * <li>CATEGORY: category_sports, category_tech</li>
 * <li>LANGUAGE: language_en, language_es</li>
 * <li>PLATFORM: platform_android, platform_ios</li>
 * <li>DIGEST: digest_daily, digest_weekly</li>
 * </ul>
 * 
 * <h3>Naming Convention:</h3>
 * <p>
 * Prefixed with {@code PushTopic} per PROJECT_PRINCIPLES.md §6.1
 * Feature-Contextual Naming to clearly indicate entity ownership.
 * </p>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
public enum PushTopicCategory {

    /**
     * Global topics (all_news, breaking_news).
     */
    GLOBAL,

    /**
     * Category-specific topics (category_sports, category_tech).
     */
    CATEGORY,

    /**
     * Language-specific topics (language_en, language_es).
     */
    LANGUAGE,

    /**
     * Platform-specific topics (platform_android, platform_ios).
     */
    PLATFORM,

    /**
     * Digest topics (digest_daily, digest_weekly).
     */
    DIGEST,

    /**
     * User-created custom topics.
     */
    CUSTOM
}
