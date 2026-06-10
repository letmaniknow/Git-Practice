package com.mmva.newsapp.domain.newsletter.service.core;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mmva.newsapp.domain.newsletter.dto.core.NewsletterSubscriberRequestDto;
import com.mmva.newsapp.domain.newsletter.dto.core.NewsletterSubscriberResponseDto;
import com.mmva.newsapp.domain.newsletter.enums.core.NewsletterSubscriptionStatus;
import com.mmva.newsapp.domain.newsletter.exception.core.NewsletterSubscriberNotFoundException;
import com.mmva.newsapp.domain.newsletter.exception.core.NewsletterSubscriptionAlreadyExistsException;
import com.mmva.newsapp.domain.newsletter.mapper.core.NewsletterMapper;
import com.mmva.newsapp.domain.newsletter.model.core.NewsletterSubscriber;
import com.mmva.newsapp.domain.newsletter.repository.core.NewsletterSubscriberRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of NewsletterSubscriberService.
 *
 * <p>
 * Provides business logic for newsletter subscriber management
 * with transaction support and validation.
 * </p>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NewsletterSubscriberServiceImpl implements NewsletterSubscriberService {

    private final NewsletterSubscriberRepository subscriberRepository;
    private final NewsletterMapper mapper;

    // =========================
    // Subscriber Management
    // =========================

    @Override
    @Transactional
    public NewsletterSubscriberResponseDto subscribe(NewsletterSubscriberRequestDto request) {
        log.info("Processing newsletter subscription for email: {}", request.getNewsletterSubscriberEmail());

        // Check if email already exists
        if (subscriberRepository.findByNewsletterSubscriberEmailIgnoreCase(request.getNewsletterSubscriberEmail())
                .isPresent()) {
            throw new NewsletterSubscriptionAlreadyExistsException(
                    "Email already subscribed: " + request.getNewsletterSubscriberEmail());
        }

        // Create new subscriber
        NewsletterSubscriber subscriber = mapper.toEntity(request);
        subscriber.setNewsletterSubscriberConfirmationToken(UUID.randomUUID());

        NewsletterSubscriber savedSubscriber = subscriberRepository.save(subscriber);
        log.info("Successfully subscribed user with ID: {}", savedSubscriber.getNewsletterSubscriberId());

        return mapper.toResponseDto(savedSubscriber);
    }

    @Override
    @Transactional
    public NewsletterSubscriberResponseDto confirmSubscription(UUID confirmationToken) {
        log.info("Confirming subscription with token: {}", confirmationToken);

        NewsletterSubscriber subscriber = subscriberRepository
                .findByNewsletterSubscriberConfirmationToken(confirmationToken)
                .orElseThrow(() -> new NewsletterSubscriberNotFoundException(
                        "Invalid confirmation token: " + confirmationToken));

        subscriber.setNewsletterSubscriberSubscriptionStatus(NewsletterSubscriptionStatus.ACTIVE);
        subscriber.setNewsletterSubscriberConfirmedAt(Instant.now());

        NewsletterSubscriber confirmedSubscriber = subscriberRepository.save(subscriber);
        log.info("Successfully confirmed subscription for user ID: {}",
                confirmedSubscriber.getNewsletterSubscriberId());

        return mapper.toResponseDto(confirmedSubscriber);
    }

    @Override
    @Transactional
    public void unsubscribe(Long subscriberId) {
        log.info("Unsubscribing user with ID: {}", subscriberId);

        NewsletterSubscriber subscriber = subscriberRepository.findById(subscriberId)
                .orElseThrow(() -> new NewsletterSubscriberNotFoundException(
                        "Subscriber not found with ID: " + subscriberId));

        subscriber.setNewsletterSubscriberSubscriptionStatus(NewsletterSubscriptionStatus.UNSUBSCRIBED);
        subscriber.setNewsletterSubscriberUnsubscribedAt(Instant.now());

        subscriberRepository.save(subscriber);
        log.info("Successfully unsubscribed user with ID: {}", subscriberId);
    }

    @Override
    @Transactional
    public NewsletterSubscriberResponseDto updatePreferences(Long subscriberId,
            NewsletterSubscriberRequestDto request) {
        log.info("Updating preferences for subscriber ID: {}", subscriberId);

        NewsletterSubscriber subscriber = subscriberRepository.findById(subscriberId)
                .orElseThrow(() -> new NewsletterSubscriberNotFoundException(
                        "Subscriber not found with ID: " + subscriberId));

        // Update preferences (only non-null fields)
        if (request.getNewsletterSubscriberPreferredLanguage() != null) {
            subscriber.setNewsletterSubscriberPreferredLanguage(request.getNewsletterSubscriberPreferredLanguage());
        }
        if (request.getNewsletterSubscriberFirstName() != null) {
            subscriber.setNewsletterSubscriberFirstName(request.getNewsletterSubscriberFirstName());
        }
        if (request.getNewsletterSubscriberLastName() != null) {
            subscriber.setNewsletterSubscriberLastName(request.getNewsletterSubscriberLastName());
        }

        NewsletterSubscriber updatedSubscriber = subscriberRepository.save(subscriber);
        log.info("Successfully updated preferences for subscriber ID: {}", subscriberId);

        return mapper.toResponseDto(updatedSubscriber);
    }

    // =========================
    // Query Operations
    // =========================

    @Override
    public NewsletterSubscriberResponseDto getSubscriberById(Long subscriberId) {
        log.debug("Fetching subscriber by ID: {}", subscriberId);

        NewsletterSubscriber subscriber = subscriberRepository.findById(subscriberId)
                .orElseThrow(() -> new NewsletterSubscriberNotFoundException(
                        "Subscriber not found with ID: " + subscriberId));

        return mapper.toResponseDto(subscriber);
    }

    @Override
    public NewsletterSubscriberResponseDto getSubscriberByEmail(String email) {
        log.debug("Fetching subscriber by email: {}", email);

        NewsletterSubscriber subscriber = subscriberRepository.findByNewsletterSubscriberEmailIgnoreCase(email)
                .orElseThrow(() -> new NewsletterSubscriberNotFoundException(
                        "Subscriber not found with email: " + email));

        return mapper.toResponseDto(subscriber);
    }

    @Override
    public Page<NewsletterSubscriberResponseDto> getSubscribersByStatus(NewsletterSubscriptionStatus status,
            Pageable pageable) {
        log.debug("Fetching subscribers by status: {} with pageable: {}", status, pageable);

        Page<NewsletterSubscriber> subscribers = subscriberRepository
                .findByNewsletterSubscriberSubscriptionStatus(status, pageable);
        return subscribers.map(mapper::toResponseDto);
    }

    @Override
    public Page<NewsletterSubscriberResponseDto> getAllSubscribers(Pageable pageable) {
        log.debug("Fetching all subscribers with pageable: {}", pageable);

        Page<NewsletterSubscriber> subscribers = subscriberRepository.findAll(pageable);
        return subscribers.map(mapper::toResponseDto);
    }

    @Override
    public Page<NewsletterSubscriberResponseDto> getActiveSubscribers(Pageable pageable) {
        log.debug("Fetching active subscribers with pageable: {}", pageable);

        Page<NewsletterSubscriber> subscribers = subscriberRepository.findByNewsletterSubscriberSubscriptionStatus(
                NewsletterSubscriptionStatus.ACTIVE, pageable);
        return subscribers.map(mapper::toResponseDto);
    }

    @Override
    public Page<NewsletterSubscriberResponseDto> getUnconfirmedSubscribers(Pageable pageable) {
        log.debug("Fetching unconfirmed subscribers with pageable: {}", pageable);

        Page<NewsletterSubscriber> subscribers = subscriberRepository.findByNewsletterSubscriberSubscriptionStatus(
                NewsletterSubscriptionStatus.PENDING, pageable);
        return subscribers.map(mapper::toResponseDto);
    }

    // =========================
    // Analytics & Statistics
    // =========================

    @Override
    public NewsletterSubscriberStatistics getSubscriberStatistics() {
        log.debug("Calculating subscriber statistics");

        long totalSubscribers = subscriberRepository.count();
        long activeSubscribers = subscriberRepository
                .countByNewsletterSubscriberSubscriptionStatus(NewsletterSubscriptionStatus.ACTIVE);
        long unconfirmedSubscribers = subscriberRepository
                .countByNewsletterSubscriberSubscriptionStatus(NewsletterSubscriptionStatus.PENDING);
        long unsubscribedCount = subscriberRepository
                .countByNewsletterSubscriberSubscriptionStatus(NewsletterSubscriptionStatus.UNSUBSCRIBED);

        double confirmationRate = totalSubscribers > 0 ? (double) activeSubscribers / totalSubscribers * 100 : 0;

        return new NewsletterSubscriberStatistics() {
            @Override
            public long getTotalSubscribers() {
                return totalSubscribers;
            }

            @Override
            public long getActiveSubscribers() {
                return activeSubscribers;
            }

            @Override
            public long getUnconfirmedSubscribers() {
                return unconfirmedSubscribers;
            }

            @Override
            public long getUnsubscribedCount() {
                return unsubscribedCount;
            }

            @Override
            public double getConfirmationRate() {
                return confirmationRate;
            }
        };
    }

    @Override
    public List<SubscriberGrowthData> getSubscriberGrowth(int days) {
        log.debug("Calculating subscriber growth for last {} days", days);

        // This would require a custom query to get daily growth
        // For now, return empty list - would need to implement in repository
        return List.of();
    }

    @Override
    public boolean isEmailSubscribed(String email) {
        return subscriberRepository.findByNewsletterSubscriberEmailIgnoreCase(email).isPresent();
    }

    @Override
    public long getTotalSubscriberCount() {
        return subscriberRepository.count();
    }

    @Override
    public long getActiveSubscriberCount() {
        return subscriberRepository.countByNewsletterSubscriberSubscriptionStatus(NewsletterSubscriptionStatus.ACTIVE);
    }

    // =========================
    // Administrative Operations
    // =========================

    @Override
    @Transactional
    public NewsletterSubscriberResponseDto updateSubscriberStatus(Long subscriberId,
            NewsletterSubscriptionStatus status) {
        log.info("Updating subscriber status for ID: {} to {}", subscriberId, status);

        NewsletterSubscriber subscriber = subscriberRepository.findById(subscriberId)
                .orElseThrow(() -> new NewsletterSubscriberNotFoundException(
                        "Subscriber not found with ID: " + subscriberId));

        subscriber.setNewsletterSubscriberSubscriptionStatus(status);

        if (status == NewsletterSubscriptionStatus.ACTIVE && subscriber.getNewsletterSubscriberConfirmedAt() == null) {
            subscriber.setNewsletterSubscriberConfirmedAt(Instant.now());
        } else if (status == NewsletterSubscriptionStatus.UNSUBSCRIBED
                && subscriber.getNewsletterSubscriberUnsubscribedAt() == null) {
            subscriber.setNewsletterSubscriberUnsubscribedAt(Instant.now());
        }

        NewsletterSubscriber updatedSubscriber = subscriberRepository.save(subscriber);
        return mapper.toResponseDto(updatedSubscriber);
    }

    @Override
    @Transactional
    public void deleteSubscriber(Long subscriberId) {
        log.info("Deleting subscriber with ID: {}", subscriberId);

        if (!subscriberRepository.existsById(subscriberId)) {
            throw new NewsletterSubscriberNotFoundException("Subscriber not found with ID: " + subscriberId);
        }

        subscriberRepository.deleteById(subscriberId);
        log.info("Successfully deleted subscriber with ID: {}", subscriberId);
    }

    @Override
    @Transactional
    public void bulkUnsubscribe(List<Long> subscriberIds) {
        log.info("Bulk unsubscribing {} subscribers", subscriberIds.size());

        List<NewsletterSubscriber> subscribers = subscriberRepository.findAllById(subscriberIds);
        Instant now = Instant.now();

        subscribers.forEach(subscriber -> {
            subscriber.setNewsletterSubscriberSubscriptionStatus(NewsletterSubscriptionStatus.UNSUBSCRIBED);
            subscriber.setNewsletterSubscriberUnsubscribedAt(now);
        });

        subscriberRepository.saveAll(subscribers);
        log.info("Successfully bulk unsubscribed {} subscribers", subscribers.size());
    }
}