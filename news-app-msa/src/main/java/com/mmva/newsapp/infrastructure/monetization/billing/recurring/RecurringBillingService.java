package com.mmva.newsapp.infrastructure.monetization.billing.recurring;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Service for handling recurring billing operations.
 *
 * <p>
 * Manages subscription auto-renewal including:
 * </p>
 * <ul>
 * <li>Automatic invoice generation for recurring charges</li>
 * <li>Payment retry logic with exponential backoff</li>
 * <li>Failed payment handling and notifications</li>
 * <li>Subscription lifecycle management</li>
 * <li>Dunning management for overdue payments</li>
 * </ul>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
public interface RecurringBillingService {

    /**
     * Processes recurring billing for a subscription.
     *
     * @param subscriptionId The subscription identifier
     * @param billingCycle   The billing cycle (MONTHLY, YEARLY, etc.)
     * @return Processing result
     */
    RecurringBillingResult processRecurringBilling(String subscriptionId, BillingCycle billingCycle);

    /**
     * Retries failed payment for a subscription.
     *
     * @param subscriptionId The subscription identifier
     * @param maxRetries     Maximum number of retry attempts
     * @return Retry result
     */
    PaymentRetryResult retryFailedPayment(String subscriptionId, int maxRetries);

    /**
     * Handles subscription cancellation with proration.
     *
     * @param subscriptionId     The subscription to cancel
     * @param cancelAtPeriodEnd  Whether to cancel at period end or immediately
     * @param refundUnusedAmount Whether to refund unused amount
     * @return Cancellation result
     */
    SubscriptionCancellationResult cancelSubscription(String subscriptionId, boolean cancelAtPeriodEnd,
            boolean refundUnusedAmount);

    /**
     * Updates subscription billing details.
     *
     * @param subscriptionId     The subscription identifier
     * @param newPaymentMethodId New payment method identifier
     * @param prorateCharges     Whether to prorate charges for mid-cycle changes
     * @return Update result
     */
    SubscriptionUpdateResult updateSubscriptionBilling(String subscriptionId, String newPaymentMethodId,
            boolean prorateCharges);

    /**
     * Generates upcoming invoice preview for a subscription.
     *
     * @param subscriptionId The subscription identifier
     * @param billingCycle   The billing cycle to preview
     * @return Invoice preview
     */
    InvoicePreview generateInvoicePreview(String subscriptionId, BillingCycle billingCycle);

    /**
     * Processes dunning for overdue subscriptions.
     *
     * @param subscriptionId The subscription identifier
     * @param dunningLevel   Current dunning level (1, 2, 3, etc.)
     * @return Dunning result
     */
    DunningResult processDunning(String subscriptionId, int dunningLevel);

    /**
     * Billing cycle enumeration.
     */
    enum BillingCycle {
        DAILY,
        WEEKLY,
        MONTHLY,
        QUARTERLY,
        YEARLY
    }

    /**
     * Result of recurring billing processing.
     */
    class RecurringBillingResult {
        private final boolean success;
        private final String invoiceId;
        private final String paymentId;
        private final String status;
        private final String message;
        private final LocalDateTime nextBillingDate;

        public RecurringBillingResult(boolean success, String invoiceId, String paymentId,
                String status, String message, LocalDateTime nextBillingDate) {
            this.success = success;
            this.invoiceId = invoiceId;
            this.paymentId = paymentId;
            this.status = status;
            this.message = message;
            this.nextBillingDate = nextBillingDate;
        }

        // Getters
        public boolean isSuccess() {
            return success;
        }

        public String getInvoiceId() {
            return invoiceId;
        }

        public String getPaymentId() {
            return paymentId;
        }

        public String getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }

        public LocalDateTime getNextBillingDate() {
            return nextBillingDate;
        }
    }

    /**
     * Result of payment retry operation.
     */
    class PaymentRetryResult {
        private final boolean success;
        private final int attemptsMade;
        private final String finalStatus;
        private final String paymentId;
        private final LocalDateTime nextRetryDate;
        private final boolean maxRetriesReached;

        public PaymentRetryResult(boolean success, int attemptsMade, String finalStatus,
                String paymentId, LocalDateTime nextRetryDate, boolean maxRetriesReached) {
            this.success = success;
            this.attemptsMade = attemptsMade;
            this.finalStatus = finalStatus;
            this.paymentId = paymentId;
            this.nextRetryDate = nextRetryDate;
            this.maxRetriesReached = maxRetriesReached;
        }

        // Getters
        public boolean isSuccess() {
            return success;
        }

        public int getAttemptsMade() {
            return attemptsMade;
        }

        public String getFinalStatus() {
            return finalStatus;
        }

        public String getPaymentId() {
            return paymentId;
        }

        public LocalDateTime getNextRetryDate() {
            return nextRetryDate;
        }

        public boolean isMaxRetriesReached() {
            return maxRetriesReached;
        }
    }

    /**
     * Result of subscription cancellation.
     */
    class SubscriptionCancellationResult {
        private final boolean success;
        private final String refundId;
        private final BigDecimal refundAmount;
        private final LocalDateTime cancellationDate;
        private final String message;

        public SubscriptionCancellationResult(boolean success, String refundId, BigDecimal refundAmount,
                LocalDateTime cancellationDate, String message) {
            this.success = success;
            this.refundId = refundId;
            this.refundAmount = refundAmount;
            this.cancellationDate = cancellationDate;
            this.message = message;
        }

        // Getters
        public boolean isSuccess() {
            return success;
        }

        public String getRefundId() {
            return refundId;
        }

        public BigDecimal getRefundAmount() {
            return refundAmount;
        }

        public LocalDateTime getCancellationDate() {
            return cancellationDate;
        }

        public String getMessage() {
            return message;
        }
    }

    /**
     * Result of subscription update.
     */
    class SubscriptionUpdateResult {
        private final boolean success;
        private final String proratedInvoiceId;
        private final BigDecimal prorationAmount;
        private final LocalDateTime nextBillingDate;
        private final String message;

        public SubscriptionUpdateResult(boolean success, String proratedInvoiceId, BigDecimal prorationAmount,
                LocalDateTime nextBillingDate, String message) {
            this.success = success;
            this.proratedInvoiceId = proratedInvoiceId;
            this.prorationAmount = prorationAmount;
            this.nextBillingDate = nextBillingDate;
            this.message = message;
        }

        // Getters
        public boolean isSuccess() {
            return success;
        }

        public String getProratedInvoiceId() {
            return proratedInvoiceId;
        }

        public BigDecimal getProrationAmount() {
            return prorationAmount;
        }

        public LocalDateTime getNextBillingDate() {
            return nextBillingDate;
        }

        public String getMessage() {
            return message;
        }
    }

    /**
     * Preview of upcoming invoice.
     */
    class InvoicePreview {
        private final String subscriptionId;
        private final BigDecimal amount;
        private final LocalDateTime billingDate;
        private final List<InvoiceLineItem> lineItems;
        private final Map<String, Object> metadata;

        public InvoicePreview(String subscriptionId, BigDecimal amount, LocalDateTime billingDate,
                List<InvoiceLineItem> lineItems, Map<String, Object> metadata) {
            this.subscriptionId = subscriptionId;
            this.amount = amount;
            this.billingDate = billingDate;
            this.lineItems = lineItems;
            this.metadata = metadata;
        }

        // Getters
        public String getSubscriptionId() {
            return subscriptionId;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public LocalDateTime getBillingDate() {
            return billingDate;
        }

        public List<InvoiceLineItem> getLineItems() {
            return lineItems;
        }

        public Map<String, Object> getMetadata() {
            return metadata;
        }
    }

    /**
     * Result of dunning process.
     */
    class DunningResult {
        private final boolean success;
        private final String actionTaken;
        private final LocalDateTime nextActionDate;
        private final int escalationLevel;
        private final String message;

        public DunningResult(boolean success, String actionTaken, LocalDateTime nextActionDate,
                int escalationLevel, String message) {
            this.success = success;
            this.actionTaken = actionTaken;
            this.nextActionDate = nextActionDate;
            this.escalationLevel = escalationLevel;
            this.message = message;
        }

        // Getters
        public boolean isSuccess() {
            return success;
        }

        public String getActionTaken() {
            return actionTaken;
        }

        public LocalDateTime getNextActionDate() {
            return nextActionDate;
        }

        public int getEscalationLevel() {
            return escalationLevel;
        }

        public String getMessage() {
            return message;
        }
    }

    /**
     * Simplified invoice line item for previews.
     */
    class InvoiceLineItem {
        private final String description;
        private final BigDecimal amount;
        private final int quantity;

        public InvoiceLineItem(String description, BigDecimal amount, int quantity) {
            this.description = description;
            this.amount = amount;
            this.quantity = quantity;
        }

        // Getters
        public String getDescription() {
            return description;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public int getQuantity() {
            return quantity;
        }
    }
}