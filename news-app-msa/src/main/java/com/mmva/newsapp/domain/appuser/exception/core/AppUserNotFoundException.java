package com.mmva.newsapp.domain.appuser.exception.core;

/**
 * Exception thrown when a user profile is not found.
 */
public class AppUserNotFoundException extends RuntimeException {

    public AppUserNotFoundException(String message) {
        super(message);
    }

    public AppUserNotFoundException(String field, String value) {
        super("User not found with " + field + ": " + value);
    }
}
