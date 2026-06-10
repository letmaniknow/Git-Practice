package com.mmva.newsapp.domain.newsletter.exception.core;  
/**
 * Exception thrown when a newsletter campaign is not found.
 *
 * @author MMVA Team
 * @since 1.0.0
 */
public class NewsletterCampaignNotFoundException extends RuntimeException {

    public NewsletterCampaignNotFoundException(String message) {
        super(message);
    }

    public NewsletterCampaignNotFoundException(Long id) {
        super("Newsletter campaign not found with id: " + id);
    }
}
