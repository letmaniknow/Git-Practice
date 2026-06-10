package com.mmva.newsapp.domain.newsletter.exception.core;  
/**
 * Exception thrown when attempting to subscribe with an email that is already
 * subscribed.
 *
 * @author MMVA Team
 * @since 1.0.0
 */
public class NewsletterSubscriptionAlreadyExistsException extends RuntimeException {

    public NewsletterSubscriptionAlreadyExistsException(String message) {
        super(message);
    }

    public NewsletterSubscriptionAlreadyExistsException(String field, String value) {
        super("Newsletter subscription already exists with " + field + ": " + value);
    }
}
