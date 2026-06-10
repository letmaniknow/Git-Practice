package com.mmva.newsapp.domain.newsrss.exception.core;

import java.util.UUID;

/**
 * Exception thrown when RSS feed generation fails.
 * 
 * <p>
 * This is a domain-specific exception for the newsrss feature.
 * Use this for feed generation errors, invalid parameters, or
 * missing data scenarios.
 * </p>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
public class NewsRssFeedException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "Failed to generate RSS feed";

    /**
     * Constructs exception with default message.
     */
    public NewsRssFeedException() {
        super(DEFAULT_MESSAGE);
    }

    /**
     * Constructs exception with custom message.
     *
     * @param message the detail message
     */
    public NewsRssFeedException(String message) {
        super(message);
    }

    /**
     * Constructs exception for a specific category not found.
     *
     * @param categoryId the UUID of the category that was not found
     */
    public NewsRssFeedException(UUID categoryId) {
        super(DEFAULT_MESSAGE + " - category not found: " + categoryId);
    }

    /**
     * Constructs exception for invalid feed type.
     *
     * @param feedType      the invalid feed type
     * @param isInvalidType flag to differentiate constructors
     */
    public NewsRssFeedException(String feedType, boolean isInvalidType) {
        super(DEFAULT_MESSAGE + " - invalid feed type: " + feedType);
    }

    /**
     * Constructs exception with message and cause.
     *
     * @param message the detail message
     * @param cause   the cause of the exception
     */
    public NewsRssFeedException(String message, Throwable cause) {
        super(message, cause);
    }
}
