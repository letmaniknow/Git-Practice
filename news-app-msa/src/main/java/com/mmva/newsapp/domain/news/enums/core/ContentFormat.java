package com.mmva.newsapp.domain.news.enums.core;

/**
 * Enum representing the format of news content for multi-platform delivery.
 *
 * <p>
 * Defines how content is stored and processed for different platforms.
 * Enables content reuse with platform-specific adaptations at runtime.
 * </p>
 *
 * <h3>Supported Formats:</h3>
 * <ul>
 * <li>{@code PLAIN_TEXT} - Basic text content (current format)</li>
 * <li>{@code HTML_BASIC} - Simple HTML with basic formatting</li>
 * <li>{@code HTML_RICH} - Full rich text with advanced formatting</li>
 * <li>{@code MARKDOWN} - Markdown format for flexible processing</li>
 * </ul>
 *
 * <h3>Platform Processing:</h3>
 * <ul>
 * <li><strong>Mobile App:</strong> All formats converted to native
 * components</li>
 * <li><strong>Web Browser:</strong> HTML formats used directly, others
 * converted</li>
 * <li><strong>AMP Pages:</strong> Optimized HTML with AMP-specific markup</li>
 * </ul>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
public enum ContentFormat {

    /**
     * Plain text content without formatting.
     * Current default format for existing content.
     * Suitable for basic text processing and mobile apps.
     */
    PLAIN_TEXT("Plain text without formatting"),

    /**
     * Basic HTML with simple formatting (paragraphs, links, basic tags).
     * Generated from plain text or created in basic HTML editors.
     * Safe for web display with minimal processing.
     */
    HTML_BASIC("Basic HTML formatting"),

    /**
     * Rich HTML with advanced formatting (tables, embedded media, styles).
     * Created in rich text editors with full formatting capabilities.
     * Requires sanitization for security.
     */
    HTML_RICH("Rich HTML with advanced formatting"),

    /**
     * Markdown format for flexible content creation.
     * Can be converted to HTML or other formats as needed.
     * Popular with technical writers and content creators.
     */
    MARKDOWN("Markdown format");

    private final String description;

    ContentFormat(String description) {
        this.description = description;
    }

    /**
     * Gets the human-readable description of the content format.
     *
     * @return description of the format
     */
    public String getDescription() {
        return description;
    }

    /**
     * Checks if this format contains HTML content.
     *
     * @return true if format is HTML-based
     */
    public boolean isHtmlFormat() {
        return this == HTML_BASIC || this == HTML_RICH;
    }

    /**
     * Checks if this format requires sanitization for web display.
     *
     * @return true if sanitization is recommended
     */
    public boolean requiresSanitization() {
        return this == HTML_BASIC || this == HTML_RICH;
    }

    /**
     * Gets the default format for new content.
     *
     * @return default content format
     */
    public static ContentFormat getDefault() {
        return PLAIN_TEXT;
    }
}