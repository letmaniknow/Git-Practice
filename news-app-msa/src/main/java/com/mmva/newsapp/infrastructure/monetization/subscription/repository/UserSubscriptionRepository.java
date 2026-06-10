package com.mmva.newsapp.infrastructure.monetization.subscription.repository;

import com.mmva.newsapp.infrastructure.monetization.subscription.enums.UserSubscriptionStatus;
import com.mmva.newsapp.infrastructure.monetization.subscription.model.UserSubscription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for {@link UserSubscription} entity operations.
 * 
 * <p>
 * Provides CRUD operations and custom queries for user subscription management.
 * Supports multi-tenant queries via tenantId filtering.
 * </p>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Repository
public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, UUID> {

        // ========================================
        // User-based Queries
        // ========================================

        /**
         * Finds all subscriptions for a user.
         *
         * @param userId the user ID
         * @return list of user's subscriptions
         */
        @Query("""
                        SELECT us FROM UserSubscription us
                        LEFT JOIN FETCH us.userSubscriptionPlan
                        WHERE us.userSubscriptionUserId = :userId
                        AND us.deletedAt IS NULL
                        ORDER BY us.createdAt DESC
                        """)
        List<UserSubscription> findByUserId(@Param("userId") UUID userId);

        /**
         * Finds active subscription for a user.
         * A user should typically have only one active subscription.
         *
         * @param userId   the user ID
         * @param tenantId the tenant identifier
         * @return optional containing active subscription
         */
        @Query("""
                        SELECT us FROM UserSubscription us
                        LEFT JOIN FETCH us.userSubscriptionPlan
                        WHERE us.userSubscriptionUserId = :userId
                        AND us.userSubscriptionTenantId = :tenantId
                        AND us.userSubscriptionStatus IN ('TRIAL', 'ACTIVE', 'PAST_DUE', 'PAUSED')
                        AND us.deletedAt IS NULL
                        ORDER BY us.createdAt DESC
                        """)
        Optional<UserSubscription> findActiveByUserIdAndTenantId(
                        @Param("userId") UUID userId,
                        @Param("tenantId") String tenantId);

        /**
         * Finds active subscription for a user (default tenant).
         *
         * @param userId the user ID
         * @return optional containing active subscription
         */
        @Query("""
                        SELECT us FROM UserSubscription us
                        LEFT JOIN FETCH us.userSubscriptionPlan
                        WHERE us.userSubscriptionUserId = :userId
                        AND us.userSubscriptionTenantId = 'default'
                        AND us.userSubscriptionStatus IN ('TRIAL', 'ACTIVE', 'PAST_DUE', 'PAUSED')
                        AND us.deletedAt IS NULL
                        """)
        Optional<UserSubscription> findActiveByUserId(@Param("userId") UUID userId);

        /**
         * Checks if user has any active subscription.
         *
         * @param userId   the user ID
         * @param tenantId the tenant identifier
         * @return true if has active subscription
         */
        @Query("""
                        SELECT COUNT(us) > 0 FROM UserSubscription us
                        WHERE us.userSubscriptionUserId = :userId
                        AND us.userSubscriptionTenantId = :tenantId
                        AND us.userSubscriptionStatus IN ('TRIAL', 'ACTIVE', 'PAST_DUE')
                        AND us.deletedAt IS NULL
                        """)
        boolean hasActiveSubscription(
                        @Param("userId") UUID userId,
                        @Param("tenantId") String tenantId);

        // ========================================
        // Status-based Queries
        // ========================================

        /**
         * Finds all subscriptions by status for a tenant.
         *
         * @param status   the subscription status
         * @param tenantId the tenant identifier
         * @param pageable pagination info
         * @return page of subscriptions
         */
        @Query("""
                        SELECT us FROM UserSubscription us
                        LEFT JOIN FETCH us.userSubscriptionPlan
                        WHERE us.userSubscriptionStatus = :status
                        AND us.userSubscriptionTenantId = :tenantId
                        AND us.deletedAt IS NULL
                        ORDER BY us.createdAt DESC
                        """)
        Page<UserSubscription> findByStatusAndTenantId(
                        @Param("status") UserSubscriptionStatus status,
                        @Param("tenantId") String tenantId,
                        Pageable pageable);

        /**
         * Finds subscriptions expiring within a date range.
         * Useful for sending renewal reminders.
         *
         * @param startDate start of date range
         * @param endDate   end of date range
         * @param tenantId  the tenant identifier
         * @return list of expiring subscriptions
         */
        @Query("""
                        SELECT us FROM UserSubscription us
                        LEFT JOIN FETCH us.userSubscriptionPlan
                        WHERE us.userSubscriptionCurrentPeriodEnd BETWEEN :startDate AND :endDate
                        AND us.userSubscriptionStatus = 'ACTIVE'
                        AND us.userSubscriptionCancelAtPeriodEnd = false
                        AND us.userSubscriptionTenantId = :tenantId
                        AND us.deletedAt IS NULL
                        ORDER BY us.userSubscriptionCurrentPeriodEnd ASC
                        """)
        List<UserSubscription> findExpiringBetween(
                        @Param("startDate") Instant startDate,
                        @Param("endDate") Instant endDate,
                        @Param("tenantId") String tenantId);

        /**
         * Finds subscriptions with ending trials.
         *
         * @param endDate  trial end before this date
         * @param tenantId the tenant identifier
         * @return list of subscriptions with ending trials
         */
        @Query("""
                        SELECT us FROM UserSubscription us
                        LEFT JOIN FETCH us.userSubscriptionPlan
                        WHERE us.userSubscriptionTrialEnd <= :endDate
                        AND us.userSubscriptionStatus = 'TRIAL'
                        AND us.userSubscriptionTenantId = :tenantId
                        AND us.deletedAt IS NULL
                        ORDER BY us.userSubscriptionTrialEnd ASC
                        """)
        List<UserSubscription> findTrialsEndingBefore(
                        @Param("endDate") Instant endDate,
                        @Param("tenantId") String tenantId);

        /**
         * Finds past due subscriptions needing retry.
         *
         * @param maxRetries maximum retry attempts
         * @param tenantId   the tenant identifier
         * @return list of past due subscriptions
         */
        @Query("""
                        SELECT us FROM UserSubscription us
                        LEFT JOIN FETCH us.userSubscriptionPlan
                        WHERE us.userSubscriptionStatus = 'PAST_DUE'
                        AND us.userSubscriptionFailedPaymentAttempts < :maxRetries
                        AND us.userSubscriptionTenantId = :tenantId
                        AND us.deletedAt IS NULL
                        ORDER BY us.userSubscriptionFailedPaymentAttempts ASC
                        """)
        List<UserSubscription> findPastDueNeedingRetry(
                        @Param("maxRetries") int maxRetries,
                        @Param("tenantId") String tenantId);

        // ========================================
        // External ID Queries (Payment Provider)
        // ========================================

        /**
         * Finds subscription by external subscription ID.
         *
         * @param externalSubscriptionId the external ID
         * @return optional containing the subscription
         */
        Optional<UserSubscription> findByUserSubscriptionExternalSubscriptionId(String externalSubscriptionId);

        /**
         * Finds subscriptions by external customer ID.
         *
         * @param externalCustomerId the external customer ID
         * @return list of customer's subscriptions
         */
        List<UserSubscription> findByUserSubscriptionExternalCustomerId(String externalCustomerId);

        // ========================================
        // Plan-based Queries
        // ========================================

        /**
         * Counts active subscriptions for a plan.
         *
         * @param planId the plan ID
         * @return count of active subscriptions
         */
        @Query("""
                        SELECT COUNT(us) FROM UserSubscription us
                        WHERE us.userSubscriptionPlan.subscriptionPlanId = :planId
                        AND us.userSubscriptionStatus IN ('TRIAL', 'ACTIVE', 'PAST_DUE')
                        AND us.deletedAt IS NULL
                        """)
        long countActiveByPlanId(@Param("planId") UUID planId);

        /**
         * Finds subscriptions by plan ID.
         *
         * @param planId   the plan ID
         * @param pageable pagination info
         * @return page of subscriptions
         */
        @Query("""
                        SELECT us FROM UserSubscription us
                        WHERE us.userSubscriptionPlan.subscriptionPlanId = :planId
                        AND us.deletedAt IS NULL
                        ORDER BY us.createdAt DESC
                        """)
        Page<UserSubscription> findByPlanId(
                        @Param("planId") UUID planId,
                        Pageable pageable);

        // ========================================
        // Bulk Update Operations
        // ========================================

        /**
         * Expires all subscriptions past their period end.
         *
         * @param now      current timestamp
         * @param tenantId the tenant identifier
         * @return count of expired subscriptions
         */
        @Modifying
        @Query("""
                        UPDATE UserSubscription us
                        SET us.userSubscriptionStatus = 'EXPIRED', us.updatedAt = :now
                        WHERE us.userSubscriptionCurrentPeriodEnd < :now
                        AND us.userSubscriptionStatus = 'ACTIVE'
                        AND us.userSubscriptionCancelAtPeriodEnd = true
                        AND us.userSubscriptionTenantId = :tenantId
                        AND us.deletedAt IS NULL
                        """)
        int expireCancelledSubscriptions(
                        @Param("now") Instant now,
                        @Param("tenantId") String tenantId);

        /**
         * Converts expired trials to cancelled.
         *
         * @param now      current timestamp
         * @param tenantId the tenant identifier
         * @return count of converted trials
         */
        @Modifying
        @Query("""
                        UPDATE UserSubscription us
                        SET us.userSubscriptionStatus = 'EXPIRED', us.updatedAt = :now
                        WHERE us.userSubscriptionTrialEnd < :now
                        AND us.userSubscriptionStatus = 'TRIAL'
                        AND us.userSubscriptionTenantId = :tenantId
                        AND us.deletedAt IS NULL
                        """)
        int expireTrialSubscriptions(
                        @Param("now") Instant now,
                        @Param("tenantId") String tenantId);

        // ========================================
        // Statistics Queries
        // ========================================

        /**
         * Counts subscriptions by status for a tenant.
         *
         * @param tenantId the tenant identifier
         * @return list of status counts
         */
        @Query("""
                        SELECT us.userSubscriptionStatus, COUNT(us)
                        FROM UserSubscription us
                        WHERE us.userSubscriptionTenantId = :tenantId
                        AND us.deletedAt IS NULL
                        GROUP BY us.userSubscriptionStatus
                        """)
        List<Object[]> countByStatusGrouped(@Param("tenantId") String tenantId);

        /**
         * Gets total active subscribers count.
         *
         * @param tenantId the tenant identifier
         * @return count of active subscribers
         */
        @Query("""
                        SELECT COUNT(DISTINCT us.userSubscriptionUserId)
                        FROM UserSubscription us
                        WHERE us.userSubscriptionTenantId = :tenantId
                        AND us.userSubscriptionStatus IN ('TRIAL', 'ACTIVE', 'PAST_DUE')
                        AND us.deletedAt IS NULL
                        """)
        long countActiveSubscribers(@Param("tenantId") String tenantId);
}
