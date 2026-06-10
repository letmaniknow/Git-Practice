package com.mmva.newsapp.domain.newsrss.enums.core;

/**
 * Enumeration of RSS feed types available in the system.
 * Used for feed generation and discovery endpoints.
 *
 * @author MMVA Team
 * @since 1.0.0
 */
public enum NewsRssFeedType {

    /**
     * Latest news feed - all categories combined.
     */
    LATEST("latest", "Latest News"),

    /**
     * Category-specific news feed.
     */
    CATEGORY("category", "Category News"),

    /**
     * Featured/highlighted news only.
     */
    FEATURED("featured", "Featured News");

    private final String code;
    private final String description;

    NewsRssFeedType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * Gets the feed type code for URLs.
     *
     * @return the code
     */
    public String getCode() {
        return code;
    }

    /**
     * Gets the human-readable description.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Finds feed type by code.
     *
     * @param code the code to search for
     * @return the matching feed type, or LATEST if not found
     */
    public static NewsRssFeedType fromCode(String code) {
        if (code == null) {
            return LATEST;
        }
        for (NewsRssFeedType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        return LATEST;
    }
}
