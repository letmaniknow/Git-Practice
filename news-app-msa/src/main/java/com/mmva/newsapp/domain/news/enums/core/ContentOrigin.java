package com.mmva.newsapp.domain.news.enums.core;

/**
 * Enum representing the origin/source of newsapp content.
 * 
 * <p>
 * Used to classify where the content originated from, enabling
 * proper attribution and analytics tracking.
 * </p>
 * 
 * <h3>Usage:</h3>
 * <ul>
 * <li>{@code ORIGINAL} - Created by internal editorial staff</li>
 * <li>{@code SYNDICATED} - From wire services (AP, Reuters, AFP)</li>
 * <li>{@code USER_SUBMITTED} - Community/reader contributions</li>
 * <li>{@code PARTNER} - Partner publication content</li>
 * </ul>
 * 
 * <p>
 * Note: Sponsored content is handled separately via {@code isSponsored} flag
 * in {@code NewsMasterEntity} as it relates to monetization, not origin.
 * </p>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
public enum ContentOrigin {

    /**
     * Content created by internal editorial staff.
     * Default value for new articles.
     */
    ORIGINAL("Original content created by internal staff"),

    /**
     * Content from wire services like Associated Press, Reuters, AFP.
     * Typically requires source agency attribution.
     */
    SYNDICATED("Syndicated from wire services"),

    /**
     * Content submitted by community members or readers.
     * May require additional moderation.
     */
    USER_SUBMITTED("Submitted by community/readers"),

    /**
     * Content from partner publications or media outlets.
     * Usually part of content sharing agreements.
     */
    PARTNER("Partner publication content");

    private final String description;

    ContentOrigin(String description) {
        this.description = description;
    }

    /**
     * Returns a human-readable description of the content origin.
     * 
     * @return the description
     */
    public String getDescription() {
        return description;
    }
}
