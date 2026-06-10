package com.mmva.newsapp.domain.newssourceagency.exception.core;

import java.util.UUID;

/**
 * Exception thrown when a news source agency is not found.
 * 
 * <p>
 * This is a domain-specific exception for the newssourceagency feature.
 * Use this instead of generic ResourceNotFoundException for better
 * error handling and debugging.
 * </p>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
public class NewsSourceAgencyNotFoundException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "News source agency not found";

    /**
     * Constructs exception with default message.
     */
    public NewsSourceAgencyNotFoundException() {
        super(DEFAULT_MESSAGE);
    }

    /**
     * Constructs exception with custom message.
     *
     * @param message the detail message
     */
    public NewsSourceAgencyNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructs exception for a specific agency ID.
     *
     * @param agencyId the UUID of the agency that was not found
     */
    public NewsSourceAgencyNotFoundException(UUID agencyId) {
        super(DEFAULT_MESSAGE + " with ID: " + agencyId);
    }

    /**
     * Constructs exception for a specific agency code.
     *
     * @param agencyCode the code of the agency that was not found
     * @param isCode     flag to differentiate from UUID constructor
     */
    public NewsSourceAgencyNotFoundException(String agencyCode, boolean isCode) {
        super(DEFAULT_MESSAGE + " with code: " + agencyCode);
    }

    /**
     * Constructs exception with message and cause.
     *
     * @param message the detail message
     * @param cause   the cause of the exception
     */
    public NewsSourceAgencyNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
