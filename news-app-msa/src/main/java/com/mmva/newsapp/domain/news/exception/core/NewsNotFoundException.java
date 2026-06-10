package com.mmva.newsapp.domain.news.exception.core;

import java.util.UUID;

/**
 * Exception thrown when a newsapp newsapp is not found.
 */
public class NewsNotFoundException extends RuntimeException {

    public NewsNotFoundException(String message) {
        super(message);
    }

    public NewsNotFoundException(UUID id) {
        super("News not found with id: " + id);
    }

    public NewsNotFoundException(String field, String value) {
        super("News not found with " + field + ": " + value);
    }
}
