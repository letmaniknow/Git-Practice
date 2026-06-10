package com.mmva.newsapp.domain.newsletter.repository.core;

import org.springframework.stereotype.Repository;

/**
 * PostgreSQL implementation of NewsletterSubscriberRepositoryCustom.
 * Uses PostgreSQL-specific TEXT field handling.
 * 
 * This is the standard implementation for all profiles (dev, prod).
 * SQL Server support removed - application is PostgreSQL-only.
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Repository
public class NewsletterSubscriberRepositoryPostgresqlImpl extends NewsletterSubscriberRepositoryImpl {

    @Override
    protected String buildInterestsQuery() {
        return "SELECT * FROM newsletter_subscriber s WHERE s.deleted_at IS NULL AND " +
                "LOWER(CAST(s.newsletter_subscriber_interests AS TEXT)) LIKE :keyword " +
                "ORDER BY s.created_at DESC";
    }

    @Override
    protected String buildInterestsCountQuery() {
        return "SELECT COUNT(*) FROM newsletter_subscriber s WHERE s.deleted_at IS NULL AND " +
                "LOWER(CAST(s.newsletter_subscriber_interests AS TEXT)) LIKE :keyword";
    }
}