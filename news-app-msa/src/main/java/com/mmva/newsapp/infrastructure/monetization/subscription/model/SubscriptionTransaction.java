package com.mmva.newsapp.infrastructure.monetization.subscription.model;

import com.mmva.newsapp.infrastructure.common.audit.model.BaseAuditEntity;
import com.mmva.newsapp.infrastructure.monetization.subscription.enums.SubscriptionTransactionPaymentStatus;
import com.mmva.newsapp.infrastructure.monetization.subscription.enums.SubscriptionTransactionType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Entity representing a subscription-related financial transaction.
 * 
 * <p>
 * Tracks all financial events related to subscriptions including:
 * initial purchases, renewals, refunds, chargebacks, and credits.
 * </p>
 * 
 * <h3>Portability Note:</h3>
 * <p>
 * Uses {@code UUID subscriptionTransactionUserId} for portability. All external
 * payment provider IDs are stored as strings for flexibility across providers.
 * </p>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Entity
@Table(name = "monetization_subscription_transaction", indexes = {
        @Index(name = "idx_subscription_txn_user", columnList = "subscription_transaction_user_id"),
        @Index(name = "idx_subscription_txn_subscription", columnList = "subscription_transaction_subscription_id"),
        @Index(name = "idx_subscription_txn_status", columnList = "subscription_transaction_status"),
        @Index(name = "idx_subscription_txn_type", columnList = "subscription_transaction_type"),
        @Index(name = "idx_subscription_txn_date", columnList = "subscription_transaction_date"),
        @Index(name = "idx_subscription_txn_external", columnList = "subscription_transaction_external_transaction_id"),
        @Index(name = "idx_subscription_txn_tenant", columnList = "subscription_transaction_tenant_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class SubscriptionTransaction extends BaseAuditEntity {

    /**
     * Primary key - UUID for global uniqueness.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "subscription_transaction_id", updatable = false, nullable = false)
    private UUID subscriptionTransactionId;

    /**
     * User ID who made the payment.
     * Portable: Uses UUID instead of AppUser entity reference.
     */
    @Column(name = "subscription_transaction_user_id", nullable = false)
    private UUID subscriptionTransactionUserId;

    /**
     * Related subscription (may be null for failed initial purchases).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_transaction_subscription_id", foreignKey = @ForeignKey(name = "fk_subscription_txn_subscription"))
    private UserSubscription subscriptionTransactionSubscription;

    /**
     * Plan ID at time of transaction (denormalized for historical accuracy).
     */
    @Column(name = "subscription_transaction_plan_id")
    private UUID subscriptionTransactionPlanId;

    /**
     * Plan code at time of transaction.
     */
    @Column(name = "subscription_transaction_plan_code", length = 50)
    private String subscriptionTransactionPlanCode;

    /**
     * Type of transaction.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_transaction_type", nullable = false, length = 30)
    private SubscriptionTransactionType subscriptionTransactionType;

    /**
     * Transaction amount (positive for charges, negative for refunds/credits).
     */
    @Column(name = "subscription_transaction_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal subscriptionTransactionAmount;

    /**
     * ISO 4217 currency code.
     */
    @Column(name = "subscription_transaction_currency", nullable = false, length = 3)
    private String subscriptionTransactionCurrency;

    /**
     * Tax amount included in the transaction.
     */
    @Column(name = "subscription_transaction_tax_amount", precision = 10, scale = 2)
    private BigDecimal subscriptionTransactionTaxAmount;

    /**
     * Discount amount applied.
     */
    @Column(name = "subscription_transaction_discount_amount", precision = 10, scale = 2)
    private BigDecimal subscriptionTransactionDiscountAmount;

    /**
     * Net amount after tax and discounts.
     */
    @Column(name = "subscription_transaction_net_amount", precision = 10, scale = 2)
    private BigDecimal subscriptionTransactionNetAmount;

    /**
     * Payment status.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_transaction_status", nullable = false, length = 20)
    private SubscriptionTransactionPaymentStatus subscriptionTransactionStatus;

    /**
     * When the transaction occurred.
     */
    @Column(name = "subscription_transaction_date", nullable = false)
    private Instant subscriptionTransactionDate;

    /**
     * Payment method type (card, paypal, bank, etc.).
     */
    @Column(name = "subscription_transaction_payment_method", length = 50)
    private String subscriptionTransactionPaymentMethod;

    /**
     * Last 4 digits of card (for display only).
     */
    @Column(name = "subscription_transaction_card_last_four", length = 4)
    private String subscriptionTransactionCardLastFour;

    /**
     * Card brand (visa, mastercard, etc.).
     */
    @Column(name = "subscription_transaction_card_brand", length = 20)
    private String subscriptionTransactionCardBrand;

    /**
     * Promo code applied to this transaction.
     */
    @Column(name = "subscription_transaction_promo_code", length = 50)
    private String subscriptionTransactionPromoCode;

    /**
     * External transaction ID from payment provider.
     * Example: Stripe PaymentIntent ID.
     */
    @Column(name = "subscription_transaction_external_transaction_id", length = 100)
    private String subscriptionTransactionExternalTransactionId;

    /**
     * External invoice ID from payment provider.
     */
    @Column(name = "subscription_transaction_external_invoice_id", length = 100)
    private String subscriptionTransactionExternalInvoiceId;

    /**
     * External charge ID from payment provider.
     */
    @Column(name = "subscription_transaction_external_charge_id", length = 100)
    private String subscriptionTransactionExternalChargeId;

    /**
     * Failure code if transaction failed.
     */
    @Column(name = "subscription_transaction_failure_code", length = 50)
    private String subscriptionTransactionFailureCode;

    /**
     * Failure message if transaction failed.
     */
    @Column(name = "subscription_transaction_failure_message", length = 500)
    private String subscriptionTransactionFailureMessage;

    /**
     * Refund reason (if this is a refund transaction).
     */
    @Column(name = "subscription_transaction_refund_reason", length = 500)
    private String subscriptionTransactionRefundReason;

    /**
     * Original transaction ID if this is a refund.
     */
    @Column(name = "subscription_transaction_original_transaction_id")
    private UUID subscriptionTransactionOriginalTransactionId;

    /**
     * Billing period start this transaction covers.
     */
    @Column(name = "subscription_transaction_period_start")
    private Instant subscriptionTransactionPeriodStart;

    /**
     * Billing period end this transaction covers.
     */
    @Column(name = "subscription_transaction_period_end")
    private Instant subscriptionTransactionPeriodEnd;

    /**
     * Invoice number for accounting purposes.
     */
    @Column(name = "subscription_transaction_invoice_number", length = 50)
    private String subscriptionTransactionInvoiceNumber;

    /**
     * IP address of the user at transaction time.
     */
    @Column(name = "subscription_transaction_ip_address", length = 45)
    private String subscriptionTransactionIpAddress;

    /**
     * User agent string at transaction time.
     */
    @Column(name = "subscription_transaction_user_agent", length = 500)
    private String subscriptionTransactionUserAgent;

    /**
     * Tenant identifier for multi-app support.
     */
    @Column(name = "subscription_transaction_tenant_id", nullable = false, length = 50)
    private String subscriptionTransactionTenantId;

    /**
     * Metadata JSON for additional flexible data.
     */
    @Column(name = "subscription_transaction_metadata_json")
    private String subscriptionTransactionMetadataJson;

    // ========================================
    // Business Methods
    // ========================================

    /**
     * Checks if this transaction was successful.
     *
     * @return true if payment succeeded
     */
    public boolean isSuccessful() {
        return subscriptionTransactionStatus != null &&
                subscriptionTransactionStatus.isSuccessful();
    }

    /**
     * Checks if this transaction is a refund.
     *
     * @return true if refund type
     */
    public boolean isRefund() {
        return subscriptionTransactionType != null &&
                subscriptionTransactionType.isRefund();
    }

    /**
     * Checks if this is a charge (not credit/refund).
     *
     * @return true if charge type
     */
    public boolean isCharge() {
        return subscriptionTransactionType != null &&
                subscriptionTransactionType.isCharge();
    }

    /**
     * Calculates the net amount if not already set.
     *
     * @return calculated net amount
     */
    public BigDecimal calculateNetAmount() {
        if (subscriptionTransactionAmount == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal tax = subscriptionTransactionTaxAmount != null
                ? subscriptionTransactionTaxAmount
                : BigDecimal.ZERO;
        BigDecimal discount = subscriptionTransactionDiscountAmount != null
                ? subscriptionTransactionDiscountAmount
                : BigDecimal.ZERO;
        return subscriptionTransactionAmount.subtract(discount).add(tax);
    }

    /**
     * Gets a display-friendly transaction description.
     *
     * @return transaction description
     */
    public String getDescription() {
        if (subscriptionTransactionType == null) {
            return "Unknown Transaction";
        }
        String planInfo = subscriptionTransactionPlanCode != null
                ? " - " + subscriptionTransactionPlanCode
                : "";
        return subscriptionTransactionType.getDisplayName() + planInfo;
    }

    /**
     * Checks if transaction can be refunded.
     *
     * @return true if refund is possible
     */
    public boolean canBeRefunded() {
        return isSuccessful()
                && isCharge()
                && !isRefund()
                && subscriptionTransactionStatus != SubscriptionTransactionPaymentStatus.REFUNDED
                && subscriptionTransactionStatus != SubscriptionTransactionPaymentStatus.DISPUTED;
    }
}
