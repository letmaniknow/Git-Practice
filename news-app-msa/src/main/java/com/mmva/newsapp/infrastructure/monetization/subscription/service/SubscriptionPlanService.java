package com.mmva.newsapp.infrastructure.monetization.subscription.service;

import com.mmva.newsapp.infrastructure.common.exception.ResourceNotFoundException;
import com.mmva.newsapp.infrastructure.monetization.subscription.enums.SubscriptionPlanBillingCycle;
import com.mmva.newsapp.infrastructure.monetization.subscription.dto.SubscriptionPlanRequestDto;
import com.mmva.newsapp.infrastructure.monetization.subscription.dto.SubscriptionPlanResponseDto;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for Subscription Plan management operations.
 * 
 * <p>
 * Provides business logic for creating, retrieving, updating, and deleting
 * subscription plans. Plans define the pricing, features, and billing terms.
 * </p>
 * 
 * <h3>Features:</h3>
 * <ul>
 * <li>Full CRUD operations for plans</li>
 * <li>Multi-tenant support via tenantId</li>
 * <li>Filtering by tier, billing cycle, status</li>
 * </ul>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
public interface SubscriptionPlanService {

    // =========================
    // Create Operations
    // =========================

    /**
     * Creates a new subscription plan.
     *
     * @param dto the plan creation request
     * @return the created plan response
     * @throws IllegalArgumentException if plan code already exists
     */
    SubscriptionPlanResponseDto create(SubscriptionPlanRequestDto dto);

    // =========================
    // Read Operations
    // =========================

    /**
     * Retrieves a plan by its unique identifier.
     *
     * @param planId the plan ID
     * @return the plan response
     * @throws ResourceNotFoundException if not found
     */
    SubscriptionPlanResponseDto getById(UUID planId);

    /**
     * Retrieves a plan by its unique code and tenant.
     *
     * @param planCode the plan code
     * @param tenantId the tenant identifier
     * @return the plan response
     * @throws ResourceNotFoundException if not found
     */
    SubscriptionPlanResponseDto getByCode(String planCode, String tenantId);

    /**
     * Retrieves all active plans for a tenant.
     *
     * @param tenantId the tenant identifier
     * @return list of active plans ordered by display order
     */
    List<SubscriptionPlanResponseDto> getActivePlans(String tenantId);

    /**
     * Retrieves all plans for a tenant (including inactive).
     *
     * @param tenantId the tenant identifier
     * @return list of all plans
     */
    List<SubscriptionPlanResponseDto> getAllPlans(String tenantId);

    /**
     * Retrieves active plans filtered by tier code.
     *
     * @param tierCode the tier code (0-3)
     * @param tenantId the tenant identifier
     * @return list of matching plans
     */
    List<SubscriptionPlanResponseDto> getByTier(Integer tierCode, String tenantId);

    /**
     * Retrieves active plans filtered by billing cycle.
     *
     * @param billingCycle the billing cycle
     * @param tenantId     the tenant identifier
     * @return list of matching plans
     */
    List<SubscriptionPlanResponseDto> getByBillingCycle(SubscriptionPlanBillingCycle billingCycle, String tenantId);

    /**
     * Retrieves the recommended plan for a tenant.
     *
     * @param tenantId the tenant identifier
     * @return the recommended plan, or null if none set
     */
    SubscriptionPlanResponseDto getRecommendedPlan(String tenantId);

    // =========================
    // Update Operations
    // =========================

    /**
     * Updates an existing plan.
     *
     * @param planId the plan ID
     * @param dto    the update request
     * @return the updated plan response
     * @throws ResourceNotFoundException if not found
     */
    SubscriptionPlanResponseDto update(UUID planId, SubscriptionPlanRequestDto dto);

    /**
     * Activates a plan (makes it available for purchase).
     *
     * @param planId the plan ID
     * @return the updated plan response
     */
    SubscriptionPlanResponseDto activate(UUID planId);

    /**
     * Deactivates a plan (hides from purchase, existing subscriptions continue).
     *
     * @param planId the plan ID
     * @return the updated plan response
     */
    SubscriptionPlanResponseDto deactivate(UUID planId);

    /**
     * Sets a plan as the recommended plan (unsets previous recommended).
     *
     * @param planId the plan ID
     * @return the updated plan response
     */
    SubscriptionPlanResponseDto setAsRecommended(UUID planId);

    // =========================
    // Delete Operations
    // =========================

    /**
     * Soft deletes a plan.
     * Plan cannot be deleted if it has active subscriptions.
     *
     * @param planId the plan ID
     * @throws IllegalStateException if plan has active subscriptions
     */
    void delete(UUID planId);

    // =========================
    // Validation Operations
    // =========================

    /**
     * Checks if a plan code exists for a tenant.
     *
     * @param planCode the plan code
     * @param tenantId the tenant identifier
     * @return true if exists
     */
    boolean existsByCode(String planCode, String tenantId);

    /**
     * Counts active subscribers for a plan.
     *
     * @param planId the plan ID
     * @return count of active subscriptions
     */
    long countActiveSubscribers(UUID planId);
}
