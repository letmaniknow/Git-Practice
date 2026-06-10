package com.mmva.newsapp.controller.admin.newsletter;

import com.mmva.newsapp.domain.newsletter.dto.core.NewsletterSubscriberRequestDto;
import com.mmva.newsapp.domain.newsletter.dto.core.NewsletterSubscriberResponseDto;
import com.mmva.newsapp.domain.newsletter.enums.core.NewsletterSubscriptionStatus;
import com.mmva.newsapp.domain.newsletter.service.core.NewsletterSubscriberService;
import static com.mmva.newsapp.domain.newsletter.service.core.NewsletterSubscriberService.NewsletterSubscriberStatistics;
import static com.mmva.newsapp.domain.newsletter.service.core.NewsletterSubscriberService.SubscriberGrowthData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for newsletter subscriber operations.
 *
 * <p>
 * Provides endpoints for subscriber management including
 * subscription, confirmation, preferences, and analytics.
 * </p>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/newsletter/subscribers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Newsletter Subscribers API", description = "Newsletter subscriber management operations")
public class NewsletterSubscriberController {

    private final NewsletterSubscriberService subscriberService;

    // =========================
    // Subscription Management
    // =========================

    /**
     * Subscribes a new user to the newsletter.
     *
     * @param request the subscription request
     * @return ResponseEntity containing the created subscriber
     */
    @PostMapping("/subscribe")
    @Operation(summary = "Subscribe to newsletter", description = "Subscribes a new user to the newsletter with email confirmation")
    public ResponseEntity<NewsletterSubscriberResponseDto> subscribe(
            @Parameter(description = "Subscription request") @RequestBody NewsletterSubscriberRequestDto request) {

        log.info("Newsletter subscription request for email: {}", request.getNewsletterSubscriberEmail());

        NewsletterSubscriberResponseDto response = subscriberService.subscribe(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Confirms a subscriber's email address.
     *
     * @param token the confirmation token
     * @return ResponseEntity containing the confirmed subscriber
     */
    @PostMapping("/confirm")
    @Operation(summary = "Confirm subscription", description = "Confirms a subscriber's email address using the confirmation token")
    public ResponseEntity<NewsletterSubscriberResponseDto> confirmSubscription(
            @Parameter(description = "Confirmation token", example = "550e8400-e29b-41d4-a716-446655440000") @RequestParam UUID token) {

        log.info("Newsletter subscription confirmation request");

        NewsletterSubscriberResponseDto response = subscriberService.confirmSubscription(token);
        return ResponseEntity.ok(response);
    }

    /**
     * Unsubscribes a user from the newsletter.
     *
     * @param subscriberId the subscriber ID
     * @return ResponseEntity with no content
     */
    @PostMapping("/{subscriberId}/unsubscribe")
    @Operation(summary = "Unsubscribe from newsletter", description = "Unsubscribes a user from the newsletter")
    public ResponseEntity<Void> unsubscribe(
            @Parameter(description = "Subscriber ID", example = "1") @PathVariable Long subscriberId) {

        log.info("Newsletter unsubscribe request for subscriber ID: {}", subscriberId);

        subscriberService.unsubscribe(subscriberId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Updates subscriber preferences.
     *
     * @param subscriberId the subscriber ID
     * @param request      the update request
     * @return ResponseEntity containing the updated subscriber
     */
    @PutMapping("/{subscriberId}/preferences")
    @Operation(summary = "Update subscriber preferences", description = "Updates a subscriber's preferences and settings")
    public ResponseEntity<NewsletterSubscriberResponseDto> updatePreferences(
            @Parameter(description = "Subscriber ID", example = "1") @PathVariable Long subscriberId,
            @Parameter(description = "Update request") @RequestBody NewsletterSubscriberRequestDto request) {

        log.info("Newsletter preferences update request for subscriber ID: {}", subscriberId);

        NewsletterSubscriberResponseDto response = subscriberService.updatePreferences(subscriberId, request);
        return ResponseEntity.ok(response);
    }

    // =========================
    // Query Operations
    // =========================

    /**
     * Gets a subscriber by ID.
     *
     * @param subscriberId the subscriber ID
     * @return ResponseEntity containing the subscriber
     */
    @GetMapping("/{subscriberId}")
    @Operation(summary = "Get subscriber by ID", description = "Retrieves a subscriber by their ID")
    public ResponseEntity<NewsletterSubscriberResponseDto> getSubscriberById(
            @Parameter(description = "Subscriber ID", example = "1") @PathVariable Long subscriberId) {

        log.debug("Get subscriber by ID request: {}", subscriberId);

        NewsletterSubscriberResponseDto response = subscriberService.getSubscriberById(subscriberId);
        return ResponseEntity.ok(response);
    }

    /**
     * Gets a subscriber by email.
     *
     * @param email the subscriber email
     * @return ResponseEntity containing the subscriber
     */
    @GetMapping("/email/{email}")
    @Operation(summary = "Get subscriber by email", description = "Retrieves a subscriber by their email address")
    public ResponseEntity<NewsletterSubscriberResponseDto> getSubscriberByEmail(
            @Parameter(description = "Subscriber email", example = "user@example.com") @PathVariable String email) {

        log.debug("Get subscriber by email request: {}", email);

        NewsletterSubscriberResponseDto response = subscriberService.getSubscriberByEmail(email);
        return ResponseEntity.ok(response);
    }

    /**
     * Gets subscribers by status with pagination.
     *
     * @param status the subscription status
     * @param page   page number (0-based)
     * @param size   page size
     * @return ResponseEntity containing paginated subscribers
     */
    @GetMapping("/status/{status}")
    @Operation(summary = "Get subscribers by status", description = "Retrieves subscribers filtered by subscription status")
    public ResponseEntity<Page<NewsletterSubscriberResponseDto>> getSubscribersByStatus(
            @Parameter(description = "Subscription status", example = "ACTIVE") @PathVariable NewsletterSubscriptionStatus status,
            @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20") @RequestParam(defaultValue = "20") int size) {

        log.debug("Get subscribers by status request - status: {}, page: {}, size: {}", status, page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<NewsletterSubscriberResponseDto> response = subscriberService.getSubscribersByStatus(status, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Gets all subscribers with pagination.
     *
     * @param page page number (0-based)
     * @param size page size
     * @return ResponseEntity containing paginated subscribers
     */
    @GetMapping
    @Operation(summary = "Get all subscribers", description = "Retrieves all subscribers with pagination")
    public ResponseEntity<Page<NewsletterSubscriberResponseDto>> getAllSubscribers(
            @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20") @RequestParam(defaultValue = "20") int size) {

        log.debug("Get all subscribers request - page: {}, size: {}", page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<NewsletterSubscriberResponseDto> response = subscriberService.getAllSubscribers(pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Gets active subscribers with pagination.
     *
     * @param page page number (0-based)
     * @param size page size
     * @return ResponseEntity containing paginated active subscribers
     */
    @GetMapping("/active")
    @Operation(summary = "Get active subscribers", description = "Retrieves active (confirmed) subscribers")
    public ResponseEntity<Page<NewsletterSubscriberResponseDto>> getActiveSubscribers(
            @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20") @RequestParam(defaultValue = "20") int size) {

        log.debug("Get active subscribers request - page: {}, size: {}", page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<NewsletterSubscriberResponseDto> response = subscriberService.getActiveSubscribers(pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Gets unconfirmed subscribers with pagination.
     *
     * @param page page number (0-based)
     * @param size page size
     * @return ResponseEntity containing paginated unconfirmed subscribers
     */
    @GetMapping("/unconfirmed")
    @Operation(summary = "Get unconfirmed subscribers", description = "Retrieves subscribers who haven't confirmed their email")
    public ResponseEntity<Page<NewsletterSubscriberResponseDto>> getUnconfirmedSubscribers(
            @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20") @RequestParam(defaultValue = "20") int size) {

        log.debug("Get unconfirmed subscribers request - page: {}, size: {}", page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<NewsletterSubscriberResponseDto> response = subscriberService.getUnconfirmedSubscribers(pageable);
        return ResponseEntity.ok(response);
    }

    // =========================
    // Analytics & Statistics
    // =========================

    /**
     * Gets subscriber statistics.
     *
     * @return ResponseEntity containing subscriber statistics
     */
    @GetMapping("/statistics")
    @Operation(summary = "Get subscriber statistics", description = "Retrieves comprehensive subscriber statistics")
    public ResponseEntity<NewsletterSubscriberStatistics> getSubscriberStatistics() {

        log.debug("Get subscriber statistics request");

        NewsletterSubscriberStatistics response = subscriberService.getSubscriberStatistics();
        return ResponseEntity.ok(response);
    }

    /**
     * Gets subscriber growth data.
     *
     * @param days number of days to look back
     * @return ResponseEntity containing growth data
     */
    @GetMapping("/growth")
    @Operation(summary = "Get subscriber growth", description = "Retrieves subscriber growth data over time")
    public ResponseEntity<List<SubscriberGrowthData>> getSubscriberGrowth(
            @Parameter(description = "Number of days to look back", example = "30") @RequestParam(defaultValue = "30") int days) {

        log.debug("Get subscriber growth request - days: {}", days);

        List<SubscriberGrowthData> response = subscriberService.getSubscriberGrowth(days);
        return ResponseEntity.ok(response);
    }

    /**
     * Checks if an email is already subscribed.
     *
     * @param email the email to check
     * @return ResponseEntity containing subscription status
     */
    @GetMapping("/check")
    @Operation(summary = "Check subscription status", description = "Checks if an email address is already subscribed")
    public ResponseEntity<Boolean> isEmailSubscribed(
            @Parameter(description = "Email address to check", example = "user@example.com") @RequestParam String email) {

        log.debug("Check subscription status request for email: {}", email);

        boolean isSubscribed = subscriberService.isEmailSubscribed(email);
        return ResponseEntity.ok(isSubscribed);
    }
}