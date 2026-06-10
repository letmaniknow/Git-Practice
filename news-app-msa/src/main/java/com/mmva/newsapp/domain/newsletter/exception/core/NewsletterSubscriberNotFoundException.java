package com.mmva.newsapp.domain.newsletter.exception.core;

/**
 * Exception thrown when a newsletter subscriber is not found.
 *
 * @author MMVA Team
 * @since 1.0.0
 */
public class NewsletterSubscriberNotFoundException extends RuntimeException {

    public NewsletterSubscriberNotFoundException(String message) {
        super(message);
    }

    public NewsletterSubscriberNotFoundException(Long id) {
        super("Newsletter subscriber not found with id: " + id);
    }

    public NewsletterSubscriberNotFoundException(String field, String value) {
        super("Newsletter subscriber not found with " + field + ": " + value);
    }
}
