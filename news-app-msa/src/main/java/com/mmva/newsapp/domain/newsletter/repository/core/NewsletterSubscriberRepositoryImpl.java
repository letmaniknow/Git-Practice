package com.mmva.newsapp.domain.newsletter.repository.core;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import com.mmva.newsapp.domain.newsletter.model.core.NewsletterSubscriber;

import java.util.List;

/**
 * Base implementation for NewsletterSubscriberRepositoryCustom.
 * Provides database-agnostic functionality with database-specific query
 * implementations.
 *
 * @author MMVA Team
 * @since 1.0.0
 */
public abstract class NewsletterSubscriberRepositoryImpl implements NewsletterSubscriberRepositoryCustom {

    @PersistenceContext
    protected EntityManager entityManager;

    @Override
    public Page<NewsletterSubscriber> findByNewsletterSubscriberInterestsContaining(String keyword, Pageable pageable) {
        String sql = buildInterestsQuery();
        String countSql = buildInterestsCountQuery();

        // Create queries
        Query query = entityManager.createNativeQuery(sql, NewsletterSubscriber.class);
        Query countQuery = entityManager.createNativeQuery(countSql);

        // Set parameters
        String searchPattern = "%" + keyword.toLowerCase() + "%";
        query.setParameter("keyword", searchPattern);
        countQuery.setParameter("keyword", searchPattern);

        // Apply pagination
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        // Execute queries
        @SuppressWarnings("unchecked")
        List<NewsletterSubscriber> results = query.getResultList();
        Long total = ((Number) countQuery.getSingleResult()).longValue();

        return new PageImpl<>(results, pageable, total);
    }

    /**
     * Builds the database-specific query for finding subscribers by interests.
     * Must be implemented by database-specific subclasses.
     *
     * @return the native SQL query string
     */
    protected abstract String buildInterestsQuery();

    /**
     * Builds the database-specific count query for finding subscribers by
     * interests.
     * Must be implemented by database-specific subclasses.
     *
     * @return the native SQL count query string
     */
    protected abstract String buildInterestsCountQuery();
}