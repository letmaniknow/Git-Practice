package com.mmva.newsapp.infrastructure.monetization.subscription.dto;

import com.mmva.newsapp.infrastructure.monetization.subscription.enums.SubscriptionTransactionPaymentStatus;
import com.mmva.newsapp.infrastructure.monetization.subscription.enums.SubscriptionTransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for subscription transaction data.
 * 
 * <p>
 * Field naming follows entity pattern:
 * {@code subscriptionTransaction{FieldName}}
 * per PROJECT_PRINCIPLES.md §6.1 Feature-Contextual Naming.
 * </p>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Subscription transaction response data")
public class SubscriptionTransactionResponseDto {

    // ========================================
    // Identity Fields
    // ========================================

    @Schema(description = "Transaction unique identifier")
    private UUID subscriptionTransactionId;

    @Schema(description = "User ID who made the transaction")
    private UUID subscriptionTransactionUserId;

    @Schema(description = "Related subscription ID")
    private UUID subscriptionTransactionSubscriptionId;

    // ========================================
    // Plan Reference Fields
    // ========================================

    @Schema(description = "Plan ID at time of transaction")
    private UUID subscriptionTransactionPlanId;

    @Schema(description = "Plan code at time of transaction", example = "PRO_MONTHLY")
    private String subscriptionTransactionPlanCode;

    // ========================================
    // Transaction Type & Status
    // ========================================

    @Schema(description = "Type of transaction", example = "RENEWAL")
    private SubscriptionTransactionType subscriptionTransactionType;

    @Schema(description = "Type display name", example = "Renewal")
    private String subscriptionTransactionTypeDisplayName;

    @Schema(description = "Payment status", example = "SUCCEEDED")
    private SubscriptionTransactionPaymentStatus subscriptionTransactionStatus;

    @Schema(description = "Status display name", example = "Succeeded")
    private String subscriptionTransactionStatusDisplayName;

    // ========================================
    // Amount Fields
    // ========================================

    @Schema(description = "Transaction amount", example = "9.99")
    private BigDecimal subscriptionTransactionAmount;

    @Schema(description = "Currency code", example = "USD")
    private String subscriptionTransactionCurrency;

    @Schema(description = "Formatted amount with currency", example = "$9.99")
    private String subscriptionTransactionFormattedAmount;

    @Schema(description = "Tax amount", example = "0.99")
    private BigDecimal subscriptionTransactionTaxAmount;

    @Schema(description = "Discount amount", example = "2.00")
    private BigDecimal subscriptionTransactionDiscountAmount;

    @Schema(description = "Net amount after tax and discounts", example = "8.98")
    private BigDecimal subscriptionTransactionNetAmount;

    @Schema(description = "Formatted net amount", example = "$8.98")
    private String subscriptionTransactionFormattedNetAmount;

    // ========================================
    // Date Fields
    // ========================================

    @Schema(description = "When the transaction occurred")
    private Instant subscriptionTransactionDate;

    @Schema(description = "Billing period start")
    private Instant subscriptionTransactionPeriodStart;

    @Schema(description = "Billing period end")
    private Instant subscriptionTransactionPeriodEnd;

    // ========================================
    // Payment Method Fields
    // ========================================

    @Schema(description = "Payment method type", example = "card")
    private String subscriptionTransactionPaymentMethod;

    @Schema(description = "Last 4 digits of card", example = "4242")
    private String subscriptionTransactionCardLastFour;

    @Schema(description = "Card brand", example = "visa")
    private String subscriptionTransactionCardBrand;

    @Schema(description = "Display-friendly payment method", example = "Visa •••• 4242")
    private String subscriptionTransactionPaymentMethodDisplay;

    // ========================================
    // Promo & Invoice Fields
    // ========================================

    @Schema(description = "Promo code applied", example = "SAVE20")
    private String subscriptionTransactionPromoCode;

    @Schema(description = "Invoice number", example = "INV-2024-001")
    private String subscriptionTransactionInvoiceNumber;

    // ========================================
    // External References
    // ========================================

    @Schema(description = "External transaction ID from payment provider", example = "pi_abc123")
    private String subscriptionTransactionExternalTransactionId;

    @Schema(description = "External invoice ID", example = "inv_abc123")
    private String subscriptionTransactionExternalInvoiceId;

    @Schema(description = "External charge ID", example = "ch_abc123")
    private String subscriptionTransactionExternalChargeId;

    // ========================================
    // Failure Fields
    // ========================================

    @Schema(description = "Failure code if transaction failed", example = "card_declined")
    private String subscriptionTransactionFailureCode;

    @Schema(description = "Failure message", example = "Your card was declined")
    private String subscriptionTransactionFailureMessage;

    // ========================================
    // Refund Fields
    // ========================================

    @Schema(description = "Refund reason (if refund)")
    private String subscriptionTransactionRefundReason;

    @Schema(description = "Original transaction ID (if refund)")
    private UUID subscriptionTransactionOriginalTransactionId;

    // ========================================
    // Computed Flags
    // ========================================

    @Schema(description = "Whether transaction was successful", example = "true")
    private Boolean subscriptionTransactionIsSuccessful;

    @Schema(description = "Whether this is a refund transaction", example = "false")
    private Boolean subscriptionTransactionIsRefund;

    @Schema(description = "Whether this is a charge", example = "true")
    private Boolean subscriptionTransactionIsCharge;

    @Schema(description = "Whether this can be refunded", example = "true")
    private Boolean subscriptionTransactionCanBeRefunded;

    @Schema(description = "Transaction description", example = "Renewal - PRO_MONTHLY")
    private String subscriptionTransactionDescription;

    // ========================================
    // Tenant & Audit Fields
    // ========================================

    @Schema(description = "Tenant identifier")
    private String subscriptionTransactionTenantId;

    @Schema(description = "When the record was created")
    private Instant subscriptionTransactionCreatedAt;

    @Schema(description = "When the record was last updated")
    private Instant subscriptionTransactionUpdatedAt;
}
