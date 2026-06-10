package com.mmva.newsapp.infrastructure.common.exception;

import java.util.Map;

/**
 * Exception thrown when a request contains invalid data or parameters.
 * Supports both generic error messages and field-level validation errors (RFC
 * 7807).
 */
public class InvalidRequestException extends RuntimeException {
    private final Map<String, String> fieldErrors;

    public InvalidRequestException(String message) {
        super(message);
        this.fieldErrors = null;
    }

    public InvalidRequestException(String fieldName, String reason) {
        super("Invalid value for field '" + fieldName + "': " + reason);
        this.fieldErrors = Map.of(fieldName, reason);
    }

    public InvalidRequestException(String message, String fieldName, String fieldValue) {
        super(message + " Field: " + fieldName + ", Value: " + fieldValue);
        this.fieldErrors = null;
    }

    /**
     * Constructor that accepts field-level validation errors.
     * Supports RFC 7807 Problem Details pattern.
     * 
     * @param message     The main error message
     * @param fieldErrors Map of field names to error messages
     */
    public InvalidRequestException(String message, Map<String, String> fieldErrors) {
        super(message);
        this.fieldErrors = fieldErrors;
    }

    /**
     * Get field-level errors if present.
     * 
     * @return Map of field names to error messages, or null if no field errors
     */
    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }
}
