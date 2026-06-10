package com.mmva.newsapp.domain.newscategory.exception.core;

import java.util.UUID;

/**
 * Exception thrown when a news category is not found.
 * 
 * <p>
 * This is a domain-specific exception for the newscategory feature.
 * Use this instead of generic ResourceNotFoundException for better
 * error handling and debugging.
 * </p>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
public class NewsCategoryNotFoundException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "News category not found";

    /**
     * Constructs exception with default message.
     */
    public NewsCategoryNotFoundException() {
        super(DEFAULT_MESSAGE);
    }

    /**
     * Constructs exception with custom message.
     *
     * @param message the detail message
     */
    public NewsCategoryNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructs exception for a specific category ID.
     *
     * @param categoryId the UUID of the category that was not found
     */
    public NewsCategoryNotFoundException(UUID categoryId) {
        super(DEFAULT_MESSAGE + " with ID: " + categoryId);
    }

    /**
     * Constructs exception with message and cause.
     *
     * @param message the detail message
     * @param cause   the cause of the exception
     */
    public NewsCategoryNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
