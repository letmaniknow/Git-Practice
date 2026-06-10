package com.mmva.newsapp.domain.newsletter.repository.core;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

/**
 * SQL Server implementation of NewsletterSubscriberRepositoryCustom.
 * Uses SQL Server-specific CLOB handling with CAST to VARCHAR(MAX).
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Repository
@Profile("sqlserver") // SQL Server fallback (usually not needed)
public class NewsletterSubscriberRepositorySqlServerImpl extends NewsletterSubscriberRepositoryImpl {

    @Override
    protected String buildInterestsQuery() {
        return "SELECT * FROM newsletter_subscriber s WHERE s.deleted_at IS NULL AND " +
                "LOWER(CAST(s.newsletter_subscriber_interests AS VARCHAR(MAX))) LIKE :keyword " +
                "ORDER BY s.created_at DESC";
    }

    @Override
    protected String buildInterestsCountQuery() {
        return "SELECT COUNT(*) FROM newsletter_subscriber s WHERE s.deleted_at IS NULL AND " +
                "LOWER(CAST(s.newsletter_subscriber_interests AS VARCHAR(MAX))) LIKE :keyword";
    }
}