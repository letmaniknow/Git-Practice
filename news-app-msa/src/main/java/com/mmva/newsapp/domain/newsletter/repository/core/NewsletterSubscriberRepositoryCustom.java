package com.mmva.newsapp.domain.newsletter.repository.core;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.mmva.newsapp.domain.newsletter.model.core.NewsletterSubscriber;

/**
 * Custom repository interface for NewsletterSubscriber with database-specific
 * queries.
 *
 * @author MMVA Team
 * @since 1.0.0
 */
public interface NewsletterSubscriberRepositoryCustom {

    /**
     * Finds subscribers by interests containing a specific keyword.
     * Implementation varies by database type for CLOB handling.
     *
     * @param keyword  the keyword to search in interests
     * @param pageable pagination information
     * @return page of subscribers
     */
    Page<NewsletterSubscriber> findByNewsletterSubscriberInterestsContaining(String keyword, Pageable pageable);
}