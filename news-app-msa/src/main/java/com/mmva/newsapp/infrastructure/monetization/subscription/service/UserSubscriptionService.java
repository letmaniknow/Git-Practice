package com.mmva.newsapp.infrastructure.monetization.subscription.service;

import com.mmva.newsapp.infrastructure.common.exception.ResourceNotFoundException;
import com.mmva.newsapp.infrastructure.monetization.subscription.enums.UserSubscriptionStatus;
import com.mmva.newsapp.infrastructure.monetization.subscription.dto.UserSubscriptionCancelRequestDto;
import com.mmva.newsapp.infrastructure.monetization.subscription.dto.UserSubscriptionRequestDto;
import com.mmva.newsapp.infrastructure.monetization.subscription.dto.UserSubscriptionResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service interface for User Subscription management operations.
 * 
 * <p>
 * Provides business logic for subscription lifecycle including:
 * creation, cancellation, renewal, and status management.
 * </p>
 * 
 * <h3>Features:</h3>
 * <ul>
 * <li>Subscribe users to plans</li>
 * <li>Manage subscription lifecycle</li>
 * <li>Handle cancellations and refunds</li>
 * <li>Multi-tenant support</li>
 * </ul>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
public interface UserSubscriptionService {

    // =========================
    // Subscribe Operations
    // =========================

    /**
     * Creates a new subscription for a user.
     *
     * @param userId the user ID
     * @param dto    the subscription request
     * @return the created subscription response
     * @throws IllegalStateException if user already has active subscription
     */
    UserSubscriptionResponseDto subscribe(UUID userId, UserSubscriptionRequestDto dto);

    // =========================
    // Read Operations
    // =========================

    /**
     * Retrieves a subscription by its unique identifier.
     *
     * @param subscriptionId the subscription ID
     * @return the subscription response
     * @throws ResourceNotFoundException if not found
     */
    UserSubscriptionResponseDto getById(UUID subscriptionId);

    /**
     * Gets the active subscription for a user.
     *
     * @param userId   the user ID
     * @param tenantId the tenant identifier
     * @return optional containing active subscription if exists
     */
    Optional<UserSubscriptionResponseDto> getActiveSubscription(UUID userId, String tenantId);

    /**
     * Gets all subscriptions for a user.
     *
     * @param userId the user ID
     * @return list of user's subscriptions
     */
    List<UserSubscriptionResponseDto> getUserSubscriptions(UUID userId);

    /**
     * Checks if user has an active subscription.
     *
     * @param userId   the user ID
     * @param tenantId the tenant identifier
     * @return true if has active subscription
     */
    boolean hasActiveSubscription(UUID userId, String tenantId);

    /**
     * Gets the tier code for user's active subscription.
     *
     * @param userId   the user ID
     * @param tenantId the tenant identifier
     * @return tier code (0-3), or 0 if no active subscription
     */
    int getUserTierCode(UUID userId, String tenantId);

    /**
     * Checks if user can access a tier level.
     *
     * @param userId       the user ID
     * @param requiredTier the required tier code
     * @param tenantId     the tenant identifier
     * @return true if user's tier >= required tier
     */
    boolean canAccessTier(UUID userId, int requiredTier, String tenantId);

    // =========================
    // Admin Query Operations
    // =========================

    /**
     * Gets subscriptions by status with pagination.
     *
     * @param status   the subscription status
     * @param tenantId the tenant identifier
     * @param pageable pagination info
     * @return page of subscriptions
     */
    Page<UserSubscriptionResponseDto> getByStatus(
            UserSubscriptionStatus status,
            String tenantId,
            Pageable pageable);

    /**
     * Gets subscriptions for a specific plan.
     *
     * @param planId   the plan ID
     * @param pageable pagination info
     * @return page of subscriptions
     */
    Page<UserSubscriptionResponseDto> getByPlan(UUID planId, Pageable pageable);

    // =========================
    // Lifecycle Operations
    // =========================

    /**
     * Cancels a subscription.
     *
     * @param subscriptionId the subscription ID
     * @param dto            the cancellation request
     * @return the updated subscription response
     */
    UserSubscriptionResponseDto cancel(UUID subscriptionId, UserSubscriptionCancelRequestDto dto);

    /**
     * Pauses a subscription (temporarily suspends billing).
     *
     * @param subscriptionId the subscription ID
     * @return the updated subscription response
     */
    UserSubscriptionResponseDto pause(UUID subscriptionId);

    /**
     * Resumes a paused subscription.
     *
     * @param subscriptionId the subscription ID
     * @return the updated subscription response
     */
    UserSubscriptionResponseDto resume(UUID subscriptionId);

    /**
     * Changes a user's subscription to a different plan.
     *
     * @param subscriptionId the current subscription ID
     * @param newPlanId      the new plan ID
     * @return the updated subscription response
     */
    UserSubscriptionResponseDto changePlan(UUID subscriptionId, UUID newPlanId);

    // =========================
    // Payment Integration
    // =========================

    /**
     * Records a successful payment for a subscription.
     *
     * @param subscriptionId        the subscription ID
     * @param externalTransactionId external payment ID
     * @return the updated subscription response
     */
    UserSubscriptionResponseDto recordPayment(UUID subscriptionId, String externalTransactionId);

    /**
     * Records a failed payment attempt.
     *
     * @param subscriptionId the subscription ID
     * @param failureReason  the failure reason
     * @return the updated subscription response
     */
    UserSubscriptionResponseDto recordPaymentFailure(UUID subscriptionId, String failureReason);

    // =========================
    // Background Operations
    // =========================

    /**
     * Processes expired trial subscriptions.
     * Called by scheduled job.
     *
     * @param tenantId the tenant identifier
     * @return count of processed subscriptions
     */
    int processExpiredTrials(String tenantId);

    /**
     * Processes subscriptions cancelled at period end.
     * Called by scheduled job.
     *
     * @param tenantId the tenant identifier
     * @return count of processed subscriptions
     */
    int processExpiredSubscriptions(String tenantId);
}
