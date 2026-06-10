package com.mmva.newsapp.infrastructure.monetization.subscription.repository;

import com.mmva.newsapp.infrastructure.monetization.subscription.enums.SubscriptionPlanBillingCycle;
import com.mmva.newsapp.infrastructure.monetization.subscription.model.SubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for {@link SubscriptionPlan} entity operations.
 * 
 * <p>
 * Provides CRUD operations and custom queries for subscription plan management.
 * Supports multi-tenant queries via tenantId filtering.
 * </p>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Repository
public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, UUID> {

        // ========================================
        // Basic Finders
        // ========================================

        /**
         * Finds a plan by its unique plan code within a tenant.
         *
         * @param planCode the plan code
         * @param tenantId the tenant identifier
         * @return optional containing the plan if found
         */
        Optional<SubscriptionPlan> findBySubscriptionPlanCodeAndSubscriptionPlanTenantId(String planCode,
                        String tenantId);

        /**
         * Finds a plan by its unique plan code (default tenant).
         *
         * @param planCode the plan code
         * @return optional containing the plan if found
         */
        @Query("SELECT sp FROM SubscriptionPlan sp WHERE sp.subscriptionPlanCode = :planCode AND sp.subscriptionPlanTenantId = 'default'")
        Optional<SubscriptionPlan> findByPlanCode(@Param("planCode") String planCode);

        /**
         * Checks if a plan code exists within a tenant.
         *
         * @param planCode the plan code
         * @param tenantId the tenant identifier
         * @return true if exists
         */
        boolean existsBySubscriptionPlanCodeAndSubscriptionPlanTenantId(String planCode, String tenantId);

        // ========================================
        // Active Plans Queries
        // ========================================

        /**
         * Finds all active plans for a tenant, ordered by display order.
         *
         * @param tenantId the tenant identifier
         * @return list of active plans
         */
        @Query("""
                        SELECT sp FROM SubscriptionPlan sp
                        WHERE sp.subscriptionPlanTenantId = :tenantId
                        AND sp.subscriptionPlanIsActive = true
                        AND sp.deletedAt IS NULL
                        ORDER BY sp.subscriptionPlanDisplayOrder ASC, sp.subscriptionPlanPrice ASC
                        """)
        List<SubscriptionPlan> findActiveByTenantId(@Param("tenantId") String tenantId);

        /**
         * Finds all active plans (default tenant).
         *
         * @return list of active plans
         */
        @Query("""
                        SELECT sp FROM SubscriptionPlan sp
                        WHERE sp.subscriptionPlanTenantId = 'default'
                        AND sp.subscriptionPlanIsActive = true
                        AND sp.deletedAt IS NULL
                        ORDER BY sp.subscriptionPlanDisplayOrder ASC, sp.subscriptionPlanPrice ASC
                        """)
        List<SubscriptionPlan> findAllActive();

        /**
         * Finds all plans for a tenant (including inactive).
         *
         * @param tenantId the tenant identifier
         * @return list of all plans
         */
        @Query("""
                        SELECT sp FROM SubscriptionPlan sp
                        WHERE sp.subscriptionPlanTenantId = :tenantId
                        AND sp.deletedAt IS NULL
                        ORDER BY sp.subscriptionPlanDisplayOrder ASC
                        """)
        List<SubscriptionPlan> findAllByTenantId(@Param("tenantId") String tenantId);

        // ========================================
        // Filter Queries
        // ========================================

        /**
         * Finds active plans by tier code.
         *
         * @param tierCode the tier code (0=FREE, 1=BASIC, 2=PRO, 3=ENTERPRISE)
         * @param tenantId the tenant identifier
         * @return list of matching plans
         */
        @Query("""
                        SELECT sp FROM SubscriptionPlan sp
                        WHERE sp.subscriptionPlanTierCode = :tierCode
                        AND sp.subscriptionPlanTenantId = :tenantId
                        AND sp.subscriptionPlanIsActive = true
                        AND sp.deletedAt IS NULL
                        ORDER BY sp.subscriptionPlanPrice ASC
                        """)
        List<SubscriptionPlan> findActiveByTierCodeAndTenantId(
                        @Param("tierCode") Integer tierCode,
                        @Param("tenantId") String tenantId);

        /**
         * Finds active plans by billing cycle.
         *
         * @param billingCycle the billing cycle
         * @param tenantId     the tenant identifier
         * @return list of matching plans
         */
        @Query("""
                        SELECT sp FROM SubscriptionPlan sp
                        WHERE sp.subscriptionPlanBillingCycle = :billingCycle
                        AND sp.subscriptionPlanTenantId = :tenantId
                        AND sp.subscriptionPlanIsActive = true
                        AND sp.deletedAt IS NULL
                        ORDER BY sp.subscriptionPlanTierCode ASC, sp.subscriptionPlanPrice ASC
                        """)
        List<SubscriptionPlan> findActiveByBillingCycleAndTenantId(
                        @Param("billingCycle") SubscriptionPlanBillingCycle billingCycle,
                        @Param("tenantId") String tenantId);

        /**
         * Finds the recommended plan for a tenant.
         *
         * @param tenantId the tenant identifier
         * @return optional containing the recommended plan
         */
        @Query("""
                        SELECT sp FROM SubscriptionPlan sp
                        WHERE sp.subscriptionPlanIsRecommended = true
                        AND sp.subscriptionPlanTenantId = :tenantId
                        AND sp.subscriptionPlanIsActive = true
                        AND sp.deletedAt IS NULL
                        """)
        Optional<SubscriptionPlan> findRecommendedByTenantId(@Param("tenantId") String tenantId);

        // ========================================
        // External ID Queries (Payment Provider)
        // ========================================

        /**
         * Finds a plan by external price ID (Stripe, PayPal, etc.).
         *
         * @param externalPriceId the external price ID
         * @return optional containing the plan
         */
        Optional<SubscriptionPlan> findBySubscriptionPlanExternalPriceId(String externalPriceId);

        /**
         * Finds plans by external product ID.
         *
         * @param externalProductId the external product ID
         * @return list of plans with that product ID
         */
        List<SubscriptionPlan> findBySubscriptionPlanExternalProductId(String externalProductId);

        // ========================================
        // Statistics Queries
        // ========================================

        /**
         * Counts active plans by tenant.
         *
         * @param tenantId the tenant identifier
         * @return count of active plans
         */
        @Query("""
                        SELECT COUNT(sp) FROM SubscriptionPlan sp
                        WHERE sp.subscriptionPlanTenantId = :tenantId
                        AND sp.subscriptionPlanIsActive = true
                        AND sp.deletedAt IS NULL
                        """)
        long countActiveByTenantId(@Param("tenantId") String tenantId);

        /**
         * Finds distinct tier codes for active plans.
         *
         * @param tenantId the tenant identifier
         * @return list of tier codes
         */
        @Query("""
                        SELECT DISTINCT sp.subscriptionPlanTierCode FROM SubscriptionPlan sp
                        WHERE sp.subscriptionPlanTenantId = :tenantId
                        AND sp.subscriptionPlanIsActive = true
                        AND sp.deletedAt IS NULL
                        ORDER BY sp.subscriptionPlanTierCode ASC
                        """)
        List<Integer> findDistinctActiveTierCodes(@Param("tenantId") String tenantId);

        // ========================================
        // Legacy method wrappers for service compatibility
        // ========================================

        /**
         * Checks if a plan code exists within a tenant.
         * Wrapper for compatibility with service layer.
         */
        default boolean existsByPlanCodeAndTenantId(String planCode, String tenantId) {
                return existsBySubscriptionPlanCodeAndSubscriptionPlanTenantId(planCode, tenantId);
        }

        /**
         * Finds a plan by code and tenant.
         * Wrapper for compatibility with service layer.
         */
        default Optional<SubscriptionPlan> findByPlanCodeAndTenantId(String planCode, String tenantId) {
                return findBySubscriptionPlanCodeAndSubscriptionPlanTenantId(planCode, tenantId);
        }
}
