package com.mmva.newsapp.infrastructure.monetization.billing.payment;

import com.mmva.newsapp.infrastructure.monetization.billing.model.Invoice;
import com.mmva.newsapp.infrastructure.monetization.billing.model.Payment;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Interface for payment gateway integrations.
 *
 * <p>
 * Supports multiple payment providers:
 * </p>
 * <ul>
 * <li>Stripe for credit card processing</li>
 * <li>PayPal for digital wallet payments</li>
 * <li>Bank transfers for ACH/EFT</li>
 * <li>Digital wallets (Apple Pay, Google Pay)</li>
 * </ul>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
public interface PaymentGatewayService {

    /**
     * Processes a payment for an invoice.
     *
     * @param invoice         The invoice to process payment for
     * @param paymentMethodId The payment method identifier (token, card ID, etc.)
     * @param amount          The amount to charge
     * @param metadata        Additional metadata for the payment
     * @return Payment result with gateway response
     */
    PaymentResult processPayment(Invoice invoice, String paymentMethodId, BigDecimal amount,
            Map<String, String> metadata);

    /**
     * Processes a refund for a payment.
     *
     * @param payment The original payment to refund
     * @param amount  The amount to refund (partial or full)
     * @param reason  The reason for the refund
     * @return Refund result with gateway response
     */
    RefundResult processRefund(Payment payment, BigDecimal amount, String reason);

    /**
     * Creates a payment intent for frontend integration.
     *
     * @param invoice The invoice for payment intent
     * @param amount  The amount for the intent
     * @return Payment intent data for frontend
     */
    PaymentIntent createPaymentIntent(Invoice invoice, BigDecimal amount);

    /**
     * Retrieves payment status from gateway.
     *
     * @param gatewayTransactionId The gateway's transaction ID
     * @return Current payment status
     */
    PaymentStatus getPaymentStatus(String gatewayTransactionId);

    /**
     * Validates a payment method (card, bank account, etc.).
     *
     * @param paymentMethodData The payment method data to validate
     * @return Validation result
     */
    PaymentMethodValidation validatePaymentMethod(Map<String, Object> paymentMethodData);

    /**
     * Gets the gateway provider name.
     *
     * @return Provider name (e.g., "stripe", "paypal")
     */
    String getProviderName();

    /**
     * Result of a payment processing operation.
     */
    class PaymentResult {
        private final boolean success;
        private final String transactionId;
        private final String status;
        private final String message;
        private final Map<String, Object> metadata;

        public PaymentResult(boolean success, String transactionId, String status, String message,
                Map<String, Object> metadata) {
            this.success = success;
            this.transactionId = transactionId;
            this.status = status;
            this.message = message;
            this.metadata = metadata;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getTransactionId() {
            return transactionId;
        }

        public String getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }

        public Map<String, Object> getMetadata() {
            return metadata;
        }
    }

    /**
     * Result of a refund processing operation.
     */
    class RefundResult {
        private final boolean success;
        private final String refundId;
        private final String status;
        private final String message;
        private final BigDecimal refundedAmount;

        public RefundResult(boolean success, String refundId, String status, String message,
                BigDecimal refundedAmount) {
            this.success = success;
            this.refundId = refundId;
            this.status = status;
            this.message = message;
            this.refundedAmount = refundedAmount;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getRefundId() {
            return refundId;
        }

        public String getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }

        public BigDecimal getRefundedAmount() {
            return refundedAmount;
        }
    }

    /**
     * Payment intent for frontend integration.
     */
    class PaymentIntent {
        private final String intentId;
        private final String clientSecret;
        private final BigDecimal amount;
        private final String currency;
        private final Map<String, Object> metadata;

        public PaymentIntent(String intentId, String clientSecret, BigDecimal amount, String currency,
                Map<String, Object> metadata) {
            this.intentId = intentId;
            this.clientSecret = clientSecret;
            this.amount = amount;
            this.currency = currency;
            this.metadata = metadata;
        }

        public String getIntentId() {
            return intentId;
        }

        public String getClientSecret() {
            return clientSecret;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public String getCurrency() {
            return currency;
        }

        public Map<String, Object> getMetadata() {
            return metadata;
        }
    }

    /**
     * Payment status enumeration.
     */
    enum PaymentStatus {
        PENDING,
        PROCESSING,
        SUCCEEDED,
        FAILED,
        CANCELLED,
        REFUNDED,
        PARTIALLY_REFUNDED,
        DISPUTED,
        UNKNOWN
    }

    /**
     * Payment method validation result.
     */
    class PaymentMethodValidation {
        private final boolean valid;
        private final String message;
        private final Map<String, Object> details;

        public PaymentMethodValidation(boolean valid, String message, Map<String, Object> details) {
            this.valid = valid;
            this.message = message;
            this.details = details;
        }

        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }

        public Map<String, Object> getDetails() {
            return details;
        }
    }
}