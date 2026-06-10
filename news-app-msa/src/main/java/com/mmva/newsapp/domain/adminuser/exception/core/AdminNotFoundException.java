package com.mmva.newsapp.domain.adminuser.exception.core;

/**
 * Exception thrown when an admindashboard user is not found.
 */
public class AdminNotFoundException extends RuntimeException {

    public AdminNotFoundException(String message) {
        super(message);
    }

    public AdminNotFoundException(String field, String value) {
        super("Admin not found with " + field + ": " + value);
    }
}
