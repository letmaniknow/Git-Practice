package com.mmva.newsapp.infrastructure.monetization.subscription.service;

import com.mmva.newsapp.infrastructure.monetization.subscription.dto.SubscriptionTransactionRequestDto;
import com.mmva.newsapp.infrastructure.monetization.subscription.dto.SubscriptionTransactionResponseDto;
import com.mmva.newsapp.infrastructure.monetization.subscription.enums.SubscriptionTransactionPaymentStatus;
import com.mmva.newsapp.infrastructure.monetization.subscription.enums.SubscriptionTransactionType;
import com.mmva.newsapp.infrastructure.monetization.subscription.exception.SubscriptionTransactionNotFoundException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Service interface for Subscription Transaction management operations.
 * 
 * <p>
 * Provides business logic for recording, retrieving, and managing
 * subscription-related financial transactions. Supports payment processing
 * integration and transaction history queries.
 * </p>
 * 
 * <h3>Features:</h3>
 * <ul>
 * <li>Transaction recording (charges, refunds, credits)</li>
 * <li>Multi-tenant support via tenantId</li>
 * <li>User transaction history</li>
 * <li>Subscription-specific transaction history</li>
 * <li>Filtering by type, status, and date range</li>
 * </ul>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
public interface SubscriptionTransactionService {

    // =========================
    // Create Operations
    // =========================

    /**
     * Records a new subscription transaction.
     * 
     * <p>
     * Used for manual transaction recording (e.g., offline payments,
     * adjustments). Payment gateway integrations typically use internal
     * methods that handle subscription linkage automatically.
     * </p>
     *
     * @param dto the transaction request data
     * @return the recorded transaction response
     * @throws IllegalArgumentException if validation fails
     */
    SubscriptionTransactionResponseDto create(SubscriptionTransactionRequestDto dto);

    // =========================
    // Read Operations
    // =========================

    /**
     * Retrieves a transaction by its unique identifier.
     *
     * @param transactionId the transaction ID
     * @return the transaction response
     * @throws SubscriptionTransactionNotFoundException if not found
     */
    SubscriptionTransactionResponseDto getById(UUID transactionId);

    /**
     * Retrieves a transaction by external transaction ID.
     * 
     * <p>
     * Useful for webhook processing where only the payment provider's
     * transaction ID is available.
     * </p>
     *
     * @param externalTransactionId the external transaction ID
     * @param tenantId              the tenant identifier
     * @return the transaction response
     * @throws SubscriptionTransactionNotFoundException if not found
     */
    SubscriptionTransactionResponseDto getByExternalId(String externalTransactionId, String tenantId);

    /**
     * Retrieves all transactions for a user.
     *
     * @param userId   the user ID
     * @param tenantId the tenant identifier
     * @return list of transactions ordered by date descending
     */
    List<SubscriptionTransactionResponseDto> getByUserId(UUID userId, String tenantId);

    /**
     * Retrieves all transactions for a specific subscription.
     *
     * @param subscriptionId the subscription ID
     * @return list of transactions ordered by date descending
     */
    List<SubscriptionTransactionResponseDto> getBySubscriptionId(UUID subscriptionId);

    /**
     * Retrieves transactions within a date range.
     *
     * @param tenantId  the tenant identifier
     * @param startDate start of date range (inclusive)
     * @param endDate   end of date range (inclusive)
     * @return list of transactions ordered by date descending
     */
    List<SubscriptionTransactionResponseDto> getByDateRange(String tenantId, Instant startDate, Instant endDate);

    /**
     * Retrieves transactions filtered by status.
     *
     * @param tenantId the tenant identifier
     * @param status   the payment status to filter by
     * @return list of matching transactions
     */
    List<SubscriptionTransactionResponseDto> getByStatus(String tenantId, SubscriptionTransactionPaymentStatus status);

    /**
     * Retrieves transactions filtered by type.
     *
     * @param tenantId the tenant identifier
     * @param type     the transaction type to filter by
     * @return list of matching transactions
     */
    List<SubscriptionTransactionResponseDto> getByType(String tenantId, SubscriptionTransactionType type);

    // =========================
    // Update Operations
    // =========================

    /**
     * Updates the status of a transaction.
     * 
     * <p>
     * Used primarily by payment gateway webhooks to update transaction
     * status as payment processing progresses.
     * </p>
     *
     * @param transactionId  the transaction ID
     * @param newStatus      the new status
     * @param failureCode    optional failure code (for FAILED status)
     * @param failureMessage optional failure message (for FAILED status)
     * @return the updated transaction response
     * @throws SubscriptionTransactionNotFoundException if not found
     */
    SubscriptionTransactionResponseDto updateStatus(UUID transactionId,
            SubscriptionTransactionPaymentStatus newStatus,
            String failureCode,
            String failureMessage);

    // =========================
    // Refund Operations
    // =========================

    /**
     * Creates a refund transaction for an existing charge.
     *
     * @param originalTransactionId the original transaction to refund
     * @param refundReason          reason for the refund
     * @param isPartial             whether this is a partial refund
     * @param partialAmount         amount to refund (for partial refunds)
     * @return the refund transaction response
     * @throws SubscriptionTransactionNotFoundException if original not found
     * @throws IllegalArgumentException                 if original cannot be
     *                                                  refunded
     */
    SubscriptionTransactionResponseDto createRefund(UUID originalTransactionId,
            String refundReason,
            boolean isPartial,
            java.math.BigDecimal partialAmount);

    // =========================
    // Query Operations
    // =========================

    /**
     * Checks if a user has any successful transactions.
     *
     * @param userId   the user ID
     * @param tenantId the tenant identifier
     * @return true if user has at least one successful transaction
     */
    boolean hasSuccessfulTransactions(UUID userId, String tenantId);

    /**
     * Counts transactions by status for a tenant.
     *
     * @param tenantId the tenant identifier
     * @param status   the status to count
     * @return count of transactions with the given status
     */
    long countByStatus(String tenantId, SubscriptionTransactionPaymentStatus status);

    /**
     * Calculates total revenue for a date range.
     *
     * @param tenantId  the tenant identifier
     * @param startDate start of date range
     * @param endDate   end of date range
     * @return total revenue (successful charges minus refunds)
     */
    java.math.BigDecimal calculateRevenue(String tenantId, Instant startDate, Instant endDate);
}
