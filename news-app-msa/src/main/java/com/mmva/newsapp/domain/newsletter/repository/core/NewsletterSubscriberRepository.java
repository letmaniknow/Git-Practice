package com.mmva.newsapp.domain.newsletter.repository.core;

import com.mmva.newsapp.domain.newsletter.enums.core.NewsletterSubscriptionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mmva.newsapp.domain.newsletter.model.core.NewsletterSubscriber;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for NewsletterSubscriber entity operations.
 *
 * <p>
 * Provides data access methods for newsletter subscriber management
 * with support for soft deletes and various query operations.
 * </p>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Repository
public interface NewsletterSubscriberRepository
                extends JpaRepository<NewsletterSubscriber, Long>, JpaSpecificationExecutor<NewsletterSubscriber>,
                NewsletterSubscriberRepositoryCustom {

        /**
         * Finds a subscriber by email address (case-insensitive).
         *
         * @param email the subscriber email
         * @return optional subscriber
         */
        Optional<NewsletterSubscriber> findByNewsletterSubscriberEmailIgnoreCase(String email);

        /**
         * Finds a subscriber by confirmation token.
         *
         * @param confirmationToken the confirmation token
         * @return optional subscriber
         */
        Optional<NewsletterSubscriber> findByNewsletterSubscriberConfirmationToken(UUID confirmationToken);

        /**
         * Finds subscribers by subscription status.
         *
         * @param status   the subscription status
         * @param pageable pagination information
         * @return page of subscribers
         */
        Page<NewsletterSubscriber> findByNewsletterSubscriberSubscriptionStatus(NewsletterSubscriptionStatus status,
                        Pageable pageable);

        /**
         * Finds subscribers by preferred language.
         *
         * @param language the preferred language code
         * @param pageable pagination information
         * @return page of subscribers
         */
        Page<NewsletterSubscriber> findByNewsletterSubscriberPreferredLanguage(String language, Pageable pageable);

        /**
         * Finds subscribers linked to a specific user.
         *
         * @param userId the user ID
         * @return list of subscribers for the user
         */
        List<NewsletterSubscriber> findByUserId(UUID userId);

        /**
         * Checks if a subscriber exists with the given email.
         *
         * @param email the subscriber email
         * @return true if subscriber exists
         */
        boolean existsByNewsletterSubscriberEmailIgnoreCase(String email);

        /**
         * Counts subscribers by subscription status.
         *
         * @param status the subscription status
         * @return count of subscribers with the status
         */
        long countByNewsletterSubscriberSubscriptionStatus(NewsletterSubscriptionStatus status);

        /**
         * Finds subscribers by interests containing a specific keyword.
         *
         * @param keyword  the keyword to search in interests
         * @param pageable pagination information
         * @return page of subscribers
         */
        Page<NewsletterSubscriber> findByNewsletterSubscriberInterestsContaining(String keyword, Pageable pageable);

        /**
         * Finds subscribers who have not confirmed their subscription within a certain
         * period.
         *
         * @param cutoffTime the cutoff timestamp for confirmation
         * @return list of unconfirmed subscribers
         */
        @Query("SELECT s FROM NewsletterSubscriber s WHERE s.deletedAt IS NULL AND " +
                        "s.newsletterSubscriberSubscriptionStatus = 'PENDING' AND " +
                        "s.createdAt < :cutoffTime")
        List<NewsletterSubscriber> findUnconfirmedSubscribers(@Param("cutoffTime") java.time.Instant cutoffTime);
}