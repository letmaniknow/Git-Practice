package com.mmva.newsapp.infrastructure.monetization.subscription.dto;

import com.mmva.newsapp.infrastructure.monetization.subscription.enums.SubscriptionTransactionPaymentStatus;
import com.mmva.newsapp.infrastructure.monetization.subscription.enums.SubscriptionTransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Request DTO for creating a subscription transaction.
 * 
 * <p>
 * Used primarily for manual transaction recording (e.g., offline payments,
 * adjustments). Most transactions are created automatically by the payment
 * processing flow.
 * </p>
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
@Schema(description = "Request for creating a subscription transaction")
public class SubscriptionTransactionRequestDto {

    @Schema(description = "User ID for the transaction", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "User ID is required")
    private UUID subscriptionTransactionUserId;

    @Schema(description = "Subscription ID (optional for failed initial purchases)")
    private UUID subscriptionTransactionSubscriptionId;

    @Schema(description = "Plan ID at time of transaction")
    private UUID subscriptionTransactionPlanId;

    @Schema(description = "Plan code at time of transaction", example = "PRO_MONTHLY")
    @Size(max = 50, message = "Plan code must not exceed 50 characters")
    private String subscriptionTransactionPlanCode;

    @Schema(description = "Type of transaction", example = "RENEWAL", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Transaction type is required")
    private SubscriptionTransactionType subscriptionTransactionType;

    @Schema(description = "Transaction amount", example = "9.99", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Transaction amount is required")
    @DecimalMin(value = "-99999999.99", message = "Amount exceeds minimum allowed")
    @DecimalMax(value = "99999999.99", message = "Amount exceeds maximum allowed")
    private BigDecimal subscriptionTransactionAmount;

    @Schema(description = "ISO 4217 currency code", example = "USD", defaultValue = "USD")
    @Size(min = 3, max = 3, message = "Currency code must be exactly 3 characters")
    @Builder.Default
    private String subscriptionTransactionCurrency = "USD";

    @Schema(description = "Tax amount", example = "0.99")
    @DecimalMin(value = "0.00", message = "Tax amount cannot be negative")
    @Builder.Default
    private BigDecimal subscriptionTransactionTaxAmount = BigDecimal.ZERO;

    @Schema(description = "Discount amount applied", example = "2.00")
    @DecimalMin(value = "0.00", message = "Discount amount cannot be negative")
    @Builder.Default
    private BigDecimal subscriptionTransactionDiscountAmount = BigDecimal.ZERO;

    @Schema(description = "Payment status", example = "PENDING", defaultValue = "PENDING")
    @Builder.Default
    private SubscriptionTransactionPaymentStatus subscriptionTransactionStatus = SubscriptionTransactionPaymentStatus.PENDING;

    @Schema(description = "Transaction date (defaults to now)")
    private Instant subscriptionTransactionDate;

    @Schema(description = "Payment method type", example = "card")
    @Size(max = 50, message = "Payment method must not exceed 50 characters")
    private String subscriptionTransactionPaymentMethod;

    @Schema(description = "Last 4 digits of card", example = "4242")
    @Size(min = 4, max = 4, message = "Card last four must be exactly 4 characters")
    private String subscriptionTransactionCardLastFour;

    @Schema(description = "Card brand", example = "visa")
    @Size(max = 20, message = "Card brand must not exceed 20 characters")
    private String subscriptionTransactionCardBrand;

    @Schema(description = "Promo code applied", example = "SAVE20")
    @Size(max = 50, message = "Promo code must not exceed 50 characters")
    private String subscriptionTransactionPromoCode;

    @Schema(description = "External transaction ID from payment provider", example = "pi_abc123")
    @Size(max = 100, message = "External transaction ID must not exceed 100 characters")
    private String subscriptionTransactionExternalTransactionId;

    @Schema(description = "External invoice ID", example = "inv_abc123")
    @Size(max = 100, message = "External invoice ID must not exceed 100 characters")
    private String subscriptionTransactionExternalInvoiceId;

    @Schema(description = "Invoice number for accounting", example = "INV-2024-001")
    @Size(max = 50, message = "Invoice number must not exceed 50 characters")
    private String subscriptionTransactionInvoiceNumber;

    @Schema(description = "Original transaction ID (for refunds)")
    private UUID subscriptionTransactionOriginalTransactionId;

    @Schema(description = "Refund reason (if refund transaction)")
    @Size(max = 500, message = "Refund reason must not exceed 500 characters")
    private String subscriptionTransactionRefundReason;

    @Schema(description = "Tenant identifier", defaultValue = "default")
    @Size(max = 50, message = "Tenant ID must not exceed 50 characters")
    @Builder.Default
    private String subscriptionTransactionTenantId = "default";

    @Schema(description = "Additional metadata JSON")
    @Size(max = 4000, message = "Metadata JSON must not exceed 4000 characters")
    private String subscriptionTransactionMetadataJson;
}
