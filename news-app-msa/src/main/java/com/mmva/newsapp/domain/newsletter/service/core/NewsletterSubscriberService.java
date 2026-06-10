package com.mmva.newsapp.domain.newsletter.service.core;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.mmva.newsapp.domain.newsletter.dto.core.NewsletterSubscriberRequestDto;
import com.mmva.newsapp.domain.newsletter.dto.core.NewsletterSubscriberResponseDto;
import com.mmva.newsapp.domain.newsletter.enums.core.NewsletterSubscriptionStatus;
import com.mmva.newsapp.domain.newsletter.exception.core.NewsletterSubscriberNotFoundException;
import com.mmva.newsapp.domain.newsletter.exception.core.NewsletterSubscriptionAlreadyExistsException;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for newsletter subscriber management.
 *
 * <p>
 * Provides business logic for subscriber operations including
 * registration, confirmation, status management, and analytics.
 * </p>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
public interface NewsletterSubscriberService {

    // =========================
    // Subscriber Management
    // =========================

    /**
     * Subscribes a new user to the newsletter.
     *
     * @param request the subscription request
     * @return the created subscriber response
     * @throws SubscriberAlreadyExistsException if email already exists
     */
    NewsletterSubscriberResponseDto subscribe(NewsletterSubscriberRequestDto request);

    /**
     * Confirms a subscriber's email address.
     *
     * @param confirmationToken the confirmation token
     * @return the confirmed subscriber response
     * @throws NewsletterSubscriberNotFoundException if subscriber not found
     */
    NewsletterSubscriberResponseDto confirmSubscription(UUID confirmationToken);

    /**
     * Unsubscribes a user from the newsletter.
     *
     * @param subscriberId the subscriber ID
     * @throws NewsletterSubscriberNotFoundException if subscriber not found
     */
    void unsubscribe(Long subscriberId);

    /**
     * Updates subscriber preferences.
     *
     * @param subscriberId the subscriber ID
     * @param request      the update request
     * @return the updated subscriber response
     * @throws NewsletterSubscriberNotFoundException if subscriber not found
     */
    NewsletterSubscriberResponseDto updatePreferences(Long subscriberId, NewsletterSubscriberRequestDto request);

    // =========================
    // Query Operations
    // =========================

    /**
     * Gets a subscriber by ID.
     *
     * @param subscriberId the subscriber ID
     * @return the subscriber response
     * @throws NewsletterSubscriberNotFoundException if subscriber not found
     */
    NewsletterSubscriberResponseDto getSubscriberById(Long subscriberId);

    /**
     * Gets a subscriber by email.
     *
     * @param email the subscriber email
     * @return the subscriber response
     * @throws NewsletterSubscriberNotFoundException if subscriber not found
     */
    NewsletterSubscriberResponseDto getSubscriberByEmail(String email);

    /**
     * Gets subscribers by status with pagination.
     *
     * @param status   the subscription status
     * @param pageable pagination information
     * @return page of subscriber responses
     */
    Page<NewsletterSubscriberResponseDto> getSubscribersByStatus(NewsletterSubscriptionStatus status,
            Pageable pageable);

    /**
     * Gets all subscribers with pagination.
     *
     * @param pageable pagination information
     * @return page of subscriber responses
     */
    Page<NewsletterSubscriberResponseDto> getAllSubscribers(Pageable pageable);

    /**
     * Gets active subscribers (confirmed and not unsubscribed).
     *
     * @param pageable pagination information
     * @return page of active subscriber responses
     */
    Page<NewsletterSubscriberResponseDto> getActiveSubscribers(Pageable pageable);

    /**
     * Gets unconfirmed subscribers.
     *
     * @param pageable pagination information
     * @return page of unconfirmed subscriber responses
     */
    Page<NewsletterSubscriberResponseDto> getUnconfirmedSubscribers(Pageable pageable);

    // =========================
    // Analytics & Statistics
    // =========================

    /**
     * Gets subscriber statistics.
     *
     * @return subscriber statistics
     */
    NewsletterSubscriberStatistics getSubscriberStatistics();

    /**
     * Gets subscriber growth over time.
     *
     * @param days number of days to look back
     * @return list of daily subscriber counts
     */
    List<SubscriberGrowthData> getSubscriberGrowth(int days);

    /**
     * Checks if an email is already subscribed.
     *
     * @param email the email to check
     * @return true if subscribed, false otherwise
     */
    boolean isEmailSubscribed(String email);

    /**
     * Gets total subscriber count.
     *
     * @return total number of subscribers
     */
    long getTotalSubscriberCount();

    /**
     * Gets active subscriber count.
     *
     * @return number of active subscribers
     */
    long getActiveSubscriberCount();

    // =========================
    // Administrative Operations
    // =========================

    /**
     * Updates subscriber status.
     *
     * @param subscriberId the subscriber ID
     * @param status       the new status
     * @return the updated subscriber response
     * @throws NewsletterSubscriberNotFoundException if subscriber not found
     */
    NewsletterSubscriberResponseDto updateSubscriberStatus(Long subscriberId, NewsletterSubscriptionStatus status);

    /**
     * Deletes a subscriber permanently.
     *
     * @param subscriberId the subscriber ID
     * @throws NewsletterSubscriberNotFoundException if subscriber not found
     */
    void deleteSubscriber(Long subscriberId);

    /**
     * Bulk unsubscribes subscribers.
     *
     * @param subscriberIds list of subscriber IDs to unsubscribe
     */
    void bulkUnsubscribe(List<Long> subscriberIds);

    // =========================
    // Data Transfer Objects
    // =========================

    /**
     * Subscriber statistics data.
     */
    interface NewsletterSubscriberStatistics {
        long getTotalSubscribers();

        long getActiveSubscribers();

        long getUnconfirmedSubscribers();

        long getUnsubscribedCount();

        double getConfirmationRate();
    }

    /**
     * Subscriber growth data point.
     */
    interface SubscriberGrowthData {
        String getDate();

        long getNewSubscribers();

        long getCumulativeTotal();
    }
}