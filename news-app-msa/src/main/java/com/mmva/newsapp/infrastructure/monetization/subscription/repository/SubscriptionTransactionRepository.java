package com.mmva.newsapp.infrastructure.monetization.subscription.repository;

import com.mmva.newsapp.infrastructure.monetization.subscription.enums.SubscriptionTransactionPaymentStatus;
import com.mmva.newsapp.infrastructure.monetization.subscription.enums.SubscriptionTransactionType;
import com.mmva.newsapp.infrastructure.monetization.subscription.model.SubscriptionTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for {@link SubscriptionTransaction} entity operations.
 * 
 * <p>
 * Provides CRUD operations and custom queries for transaction management.
 * Supports multi-tenant queries via tenantId filtering.
 * </p>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Repository
public interface SubscriptionTransactionRepository extends JpaRepository<SubscriptionTransaction, UUID> {

        // ========================================
        // User-based Queries
        // ========================================

        /**
         * Finds all transactions for a user.
         *
         * @param userId   the user ID
         * @param pageable pagination info
         * @return page of user's transactions
         */
        @Query("""
                        SELECT st FROM SubscriptionTransaction st
                        WHERE st.subscriptionTransactionUserId = :userId
                        AND st.deletedAt IS NULL
                        ORDER BY st.subscriptionTransactionDate DESC
                        """)
        Page<SubscriptionTransaction> findByUserId(
                        @Param("userId") UUID userId,
                        Pageable pageable);

        /**
         * Finds successful transactions for a user.
         *
         * @param userId   the user ID
         * @param tenantId the tenant identifier
         * @return list of successful transactions
         */
        @Query("""
                        SELECT st FROM SubscriptionTransaction st
                        WHERE st.subscriptionTransactionUserId = :userId
                        AND st.subscriptionTransactionTenantId = :tenantId
                        AND st.subscriptionTransactionStatus = 'SUCCEEDED'
                        AND st.deletedAt IS NULL
                        ORDER BY st.subscriptionTransactionDate DESC
                        """)
        List<SubscriptionTransaction> findSuccessfulByUserIdAndTenantId(
                        @Param("userId") UUID userId,
                        @Param("tenantId") String tenantId);

        // ========================================
        // Subscription-based Queries
        // ========================================

        /**
         * Finds all transactions for a subscription.
         *
         * @param subscriptionId the subscription ID
         * @return list of transactions
         */
        @Query("""
                        SELECT st FROM SubscriptionTransaction st
                        WHERE st.subscriptionTransactionSubscription.userSubscriptionId = :subscriptionId
                        AND st.deletedAt IS NULL
                        ORDER BY st.subscriptionTransactionDate DESC
                        """)
        List<SubscriptionTransaction> findBySubscriptionId(
                        @Param("subscriptionId") UUID subscriptionId);

        /**
         * Gets total amount paid for a subscription.
         *
         * @param subscriptionId the subscription ID
         * @return total amount
         */
        @Query("""
                        SELECT COALESCE(SUM(st.subscriptionTransactionAmount), 0)
                        FROM SubscriptionTransaction st
                        WHERE st.subscriptionTransactionSubscription.userSubscriptionId = :subscriptionId
                        AND st.subscriptionTransactionStatus = 'SUCCEEDED'
                        AND st.subscriptionTransactionType IN ('SUBSCRIPTION_CREATED', 'RENEWAL', 'UPGRADE')
                        AND st.deletedAt IS NULL
                        """)
        BigDecimal getTotalPaidForSubscription(@Param("subscriptionId") UUID subscriptionId);

        // ========================================
        // External ID Queries
        // ========================================

        /**
         * Finds transaction by external transaction ID.
         *
         * @param externalTransactionId the external ID
         * @return optional containing the transaction
         */
        Optional<SubscriptionTransaction> findBySubscriptionTransactionExternalTransactionId(
                        String externalTransactionId);

        /**
         * Finds transaction by external charge ID.
         *
         * @param externalChargeId the external charge ID
         * @return optional containing the transaction
         */
        Optional<SubscriptionTransaction> findBySubscriptionTransactionExternalChargeId(String externalChargeId);

        /**
         * Finds transactions by external invoice ID.
         *
         * @param externalInvoiceId the external invoice ID
         * @return list of transactions for that invoice
         */
        List<SubscriptionTransaction> findBySubscriptionTransactionExternalInvoiceId(String externalInvoiceId);

        // ========================================
        // Status and Type Queries
        // ========================================

        /**
         * Finds transactions by status.
         *
         * @param status   the payment status
         * @param tenantId the tenant identifier
         * @param pageable pagination info
         * @return page of transactions
         */
        @Query("""
                        SELECT st FROM SubscriptionTransaction st
                        WHERE st.subscriptionTransactionStatus = :status
                        AND st.subscriptionTransactionTenantId = :tenantId
                        AND st.deletedAt IS NULL
                        ORDER BY st.subscriptionTransactionDate DESC
                        """)
        Page<SubscriptionTransaction> findByStatusAndTenantId(
                        @Param("status") SubscriptionTransactionPaymentStatus status,
                        @Param("tenantId") String tenantId,
                        Pageable pageable);

        /**
         * Finds transactions by status and tenant ID (list version).
         *
         * @param tenantId the tenant identifier
         * @param status   the payment status
         * @return list of transactions
         */
        @Query("""
                        SELECT st FROM SubscriptionTransaction st
                        WHERE st.subscriptionTransactionTenantId = :tenantId
                        AND st.subscriptionTransactionStatus = :status
                        AND st.deletedAt IS NULL
                        ORDER BY st.subscriptionTransactionDate DESC
                        """)
        List<SubscriptionTransaction> findByTenantIdAndStatus(
                        @Param("tenantId") String tenantId,
                        @Param("status") SubscriptionTransactionPaymentStatus status);

        /**
         * Finds transactions by type and tenant ID.
         *
         * @param tenantId the tenant identifier
         * @param type     the transaction type
         * @return list of transactions
         */
        @Query("""
                        SELECT st FROM SubscriptionTransaction st
                        WHERE st.subscriptionTransactionTenantId = :tenantId
                        AND st.subscriptionTransactionType = :type
                        AND st.deletedAt IS NULL
                        ORDER BY st.subscriptionTransactionDate DESC
                        """)
        List<SubscriptionTransaction> findByTenantIdAndType(
                        @Param("tenantId") String tenantId,
                        @Param("type") SubscriptionTransactionType type);

        /**
         * Finds transactions by tenant ID and date range.
         *
         * @param tenantId  the tenant identifier
         * @param startDate start of date range
         * @param endDate   end of date range
         * @return list of transactions
         */
        @Query("""
                        SELECT st FROM SubscriptionTransaction st
                        WHERE st.subscriptionTransactionTenantId = :tenantId
                        AND st.subscriptionTransactionDate BETWEEN :startDate AND :endDate
                        AND st.deletedAt IS NULL
                        ORDER BY st.subscriptionTransactionDate DESC
                        """)
        List<SubscriptionTransaction> findByTenantIdAndDateRangeOrderByDateDesc(
                        @Param("tenantId") String tenantId,
                        @Param("startDate") Instant startDate,
                        @Param("endDate") Instant endDate);

        /**
         * Checks if user has any successful transactions.
         *
         * @param userId   the user ID
         * @param tenantId the tenant identifier
         * @return true if exists
         */
        @Query("""
                        SELECT CASE WHEN COUNT(st) > 0 THEN true ELSE false END
                        FROM SubscriptionTransaction st
                        WHERE st.subscriptionTransactionUserId = :userId
                        AND st.subscriptionTransactionTenantId = :tenantId
                        AND st.subscriptionTransactionStatus = 'SUCCEEDED'
                        AND st.deletedAt IS NULL
                        """)
        boolean existsSuccessfulByUserIdAndTenantId(
                        @Param("userId") UUID userId,
                        @Param("tenantId") String tenantId);

        /**
         * Counts transactions by tenant ID and status.
         *
         * @param tenantId the tenant identifier
         * @param status   the payment status
         * @return count of transactions
         */
        @Query("""
                        SELECT COUNT(st)
                        FROM SubscriptionTransaction st
                        WHERE st.subscriptionTransactionTenantId = :tenantId
                        AND st.subscriptionTransactionStatus = :status
                        AND st.deletedAt IS NULL
                        """)
        long countByTenantIdAndStatus(
                        @Param("tenantId") String tenantId,
                        @Param("status") SubscriptionTransactionPaymentStatus status);

        /**
         * Calculates revenue for a tenant and date range.
         *
         * @param tenantId  the tenant identifier
         * @param startDate start of date range
         * @param endDate   end of date range
         * @return total revenue
         */
        @Query("""
                        SELECT COALESCE(SUM(st.subscriptionTransactionAmount), 0)
                        FROM SubscriptionTransaction st
                        WHERE st.subscriptionTransactionTenantId = :tenantId
                        AND st.subscriptionTransactionDate BETWEEN :startDate AND :endDate
                        AND st.subscriptionTransactionStatus = 'SUCCEEDED'
                        AND st.subscriptionTransactionType IN ('SUBSCRIPTION_CREATED', 'RENEWAL', 'UPGRADE', 'ONE_TIME_CHARGE')
                        AND st.deletedAt IS NULL
                        """)
        BigDecimal calculateRevenueByTenantIdAndDateRange(
                        @Param("tenantId") String tenantId,
                        @Param("startDate") Instant startDate,
                        @Param("endDate") Instant endDate);

        /**
         * Finds transactions by type within date range.
         *
         * @param transactionType the transaction type
         * @param startDate       start of date range
         * @param endDate         end of date range
         * @param tenantId        the tenant identifier
         * @return list of transactions
         */
        @Query("""
                        SELECT st FROM SubscriptionTransaction st
                        WHERE st.subscriptionTransactionType = :transactionType
                        AND st.subscriptionTransactionDate BETWEEN :startDate AND :endDate
                        AND st.subscriptionTransactionTenantId = :tenantId
                        AND st.deletedAt IS NULL
                        ORDER BY st.subscriptionTransactionDate DESC
                        """)
        List<SubscriptionTransaction> findByTypeAndDateRange(
                        @Param("transactionType") SubscriptionTransactionType transactionType,
                        @Param("startDate") Instant startDate,
                        @Param("endDate") Instant endDate,
                        @Param("tenantId") String tenantId);

        /**
         * Finds pending transactions older than specified date.
         * Used for cleanup/retry of stuck transactions.
         *
         * @param olderThan cutoff date
         * @param tenantId  the tenant identifier
         * @return list of old pending transactions
         */
        @Query("""
                        SELECT st FROM SubscriptionTransaction st
                        WHERE st.subscriptionTransactionStatus = 'PENDING'
                        AND st.subscriptionTransactionDate < :olderThan
                        AND st.subscriptionTransactionTenantId = :tenantId
                        AND st.deletedAt IS NULL
                        """)
        List<SubscriptionTransaction> findStalePendingTransactions(
                        @Param("olderThan") Instant olderThan,
                        @Param("tenantId") String tenantId);

        // ========================================
        // Refund Queries
        // ========================================

        /**
         * Finds refund transactions for an original transaction.
         *
         * @param originalTransactionId the original transaction ID
         * @return list of refund transactions
         */
        List<SubscriptionTransaction> findBySubscriptionTransactionOriginalTransactionId(UUID originalTransactionId);

        /**
         * Gets total refunded amount for original transaction.
         *
         * @param originalTransactionId the original transaction ID
         * @return total refunded amount
         */
        @Query("""
                        SELECT COALESCE(SUM(ABS(st.subscriptionTransactionAmount)), 0)
                        FROM SubscriptionTransaction st
                        WHERE st.subscriptionTransactionOriginalTransactionId = :originalTransactionId
                        AND st.subscriptionTransactionType IN ('REFUND', 'PARTIAL_REFUND')
                        AND st.subscriptionTransactionStatus = 'SUCCEEDED'
                        AND st.deletedAt IS NULL
                        """)
        BigDecimal getTotalRefundedForTransaction(@Param("originalTransactionId") UUID originalTransactionId);

        // ========================================
        // Revenue Queries
        // ========================================

        /**
         * Calculates total revenue for a date range.
         *
         * @param startDate start of date range
         * @param endDate   end of date range
         * @param tenantId  the tenant identifier
         * @return total revenue
         */
        @Query("""
                        SELECT COALESCE(SUM(st.subscriptionTransactionAmount), 0)
                        FROM SubscriptionTransaction st
                        WHERE st.subscriptionTransactionDate BETWEEN :startDate AND :endDate
                        AND st.subscriptionTransactionStatus = 'SUCCEEDED'
                        AND st.subscriptionTransactionType IN ('SUBSCRIPTION_CREATED', 'RENEWAL', 'UPGRADE', 'ONE_TIME_CHARGE')
                        AND st.subscriptionTransactionTenantId = :tenantId
                        AND st.deletedAt IS NULL
                        """)
        BigDecimal calculateRevenueForPeriod(
                        @Param("startDate") Instant startDate,
                        @Param("endDate") Instant endDate,
                        @Param("tenantId") String tenantId);

        /**
         * Calculates total refunds for a date range.
         *
         * @param startDate start of date range
         * @param endDate   end of date range
         * @param tenantId  the tenant identifier
         * @return total refunds
         */
        @Query("""
                        SELECT COALESCE(SUM(ABS(st.subscriptionTransactionAmount)), 0)
                        FROM SubscriptionTransaction st
                        WHERE st.subscriptionTransactionDate BETWEEN :startDate AND :endDate
                        AND st.subscriptionTransactionStatus = 'SUCCEEDED'
                        AND st.subscriptionTransactionType IN ('REFUND', 'PARTIAL_REFUND', 'CHARGEBACK')
                        AND st.subscriptionTransactionTenantId = :tenantId
                        AND st.deletedAt IS NULL
                        """)
        BigDecimal calculateRefundsForPeriod(
                        @Param("startDate") Instant startDate,
                        @Param("endDate") Instant endDate,
                        @Param("tenantId") String tenantId);

        /**
         * Counts transactions by type for a date range.
         *
         * @param startDate start of date range
         * @param endDate   end of date range
         * @param tenantId  the tenant identifier
         * @return list of type counts
         */
        @Query("""
                        SELECT st.subscriptionTransactionType, COUNT(st), COALESCE(SUM(st.subscriptionTransactionAmount), 0)
                        FROM SubscriptionTransaction st
                        WHERE st.subscriptionTransactionDate BETWEEN :startDate AND :endDate
                        AND st.subscriptionTransactionStatus = 'SUCCEEDED'
                        AND st.subscriptionTransactionTenantId = :tenantId
                        AND st.deletedAt IS NULL
                        GROUP BY st.subscriptionTransactionType
                        """)
        List<Object[]> getTransactionSummaryByType(
                        @Param("startDate") Instant startDate,
                        @Param("endDate") Instant endDate,
                        @Param("tenantId") String tenantId);

        /**
         * Gets monthly revenue breakdown for a year.
         *
         * @param year     the year
         * @param tenantId the tenant identifier
         * @return list of monthly revenue
         */
        @Query(value = """
                        SELECT
                            MONTH(subscription_transaction_date) as month,
                            SUM(CASE WHEN subscription_transaction_type IN ('SUBSCRIPTION_CREATED', 'RENEWAL', 'UPGRADE') THEN subscription_transaction_amount ELSE 0 END) as revenue,
                            SUM(CASE WHEN subscription_transaction_type IN ('REFUND', 'PARTIAL_REFUND', 'CHARGEBACK') THEN ABS(subscription_transaction_amount) ELSE 0 END) as refunds,
                            COUNT(*) as transaction_count
                        FROM monetization_subscription_transaction
                        WHERE YEAR(subscription_transaction_date) = :year
                        AND subscription_transaction_status = 'SUCCEEDED'
                        AND subscription_transaction_tenant_id = :tenantId
                        AND deleted_at IS NULL
                        GROUP BY MONTH(subscription_transaction_date)
                        ORDER BY MONTH(subscription_transaction_date)
                        """, nativeQuery = true)
        List<Object[]> getMonthlyRevenueBreakdown(
                        @Param("year") int year,
                        @Param("tenantId") String tenantId);
}
