package com.mmva.newsapp.infrastructure.monetization.subscription.dto;

import com.mmva.newsapp.infrastructure.monetization.subscription.enums.UserSubscriptionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for user subscription data.
 * 
 * <p>
 * Field naming follows entity pattern: {@code userSubscription{FieldName}}
 * </p>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User subscription response data")
public class UserSubscriptionResponseDto {

    @Schema(description = "Subscription unique identifier")
    private UUID userSubscriptionId;

    @Schema(description = "User ID")
    private UUID userSubscriptionUserId;

    @Schema(description = "Current subscription status")
    private UserSubscriptionStatus userSubscriptionStatus;

    @Schema(description = "Status display name", example = "Active")
    private String userSubscriptionStatusDisplayName;

    @Schema(description = "Whether user has access", example = "true")
    private Boolean userSubscriptionGrantsAccess;

    // ========== Plan Info (Embedded) ==========

    @Schema(description = "Plan ID")
    private UUID userSubscriptionPlanId;

    @Schema(description = "Plan code", example = "BASIC_MONTHLY")
    private String userSubscriptionPlanCode;

    @Schema(description = "Plan name", example = "Basic Monthly")
    private String userSubscriptionPlanName;

    @Schema(description = "Tier code", example = "1")
    private Integer userSubscriptionTierCode;

    @Schema(description = "Tier display name", example = "Basic")
    private String userSubscriptionTierDisplayName;

    // ========== Billing Info ==========

    @Schema(description = "Current price", example = "9.99")
    private BigDecimal userSubscriptionCurrentPrice;

    @Schema(description = "Currency", example = "USD")
    private String userSubscriptionCurrency;

    @Schema(description = "Formatted current price", example = "$9.99/month")
    private String userSubscriptionFormattedPrice;

    @Schema(description = "Effective price after discount", example = "7.99")
    private BigDecimal userSubscriptionEffectivePrice;

    @Schema(description = "Discount percentage applied", example = "20")
    private Integer userSubscriptionDiscountPercent;

    @Schema(description = "Promo code applied")
    private String userSubscriptionPromoCode;

    // ========== Dates ==========

    @Schema(description = "When subscription started")
    private Instant userSubscriptionStartedAt;

    @Schema(description = "Current billing period start")
    private Instant userSubscriptionCurrentPeriodStart;

    @Schema(description = "Current billing period end")
    private Instant userSubscriptionCurrentPeriodEnd;

    @Schema(description = "Trial end date (if in trial)")
    private Instant userSubscriptionTrialEnd;

    @Schema(description = "Next payment date")
    private Instant userSubscriptionNextPaymentAt;

    @Schema(description = "Cancellation date (if cancelled)")
    private Instant userSubscriptionCancelledAt;

    @Schema(description = "Cancellation reason")
    private String userSubscriptionCancellationReason;

    // ========== Status Flags ==========

    @Schema(description = "Whether currently in trial", example = "true")
    private Boolean userSubscriptionIsInTrial;

    @Schema(description = "Days remaining in trial", example = "5")
    private Long userSubscriptionTrialDaysRemaining;

    @Schema(description = "Whether subscription will renew", example = "true")
    private Boolean userSubscriptionWillRenew;

    @Schema(description = "Whether cancellation is pending at period end", example = "false")
    private Boolean userSubscriptionCancelAtPeriodEnd;

    @Schema(description = "Days remaining in current period", example = "15")
    private Long userSubscriptionDaysRemaining;

    @Schema(description = "Whether subscription is expired", example = "false")
    private Boolean userSubscriptionIsExpired;

    @Schema(description = "Whether this is ad-free", example = "true")
    private Boolean userSubscriptionIsAdFree;

    // ========== Usage Stats ==========

    @Schema(description = "Billing cycles completed", example = "3")
    private Integer userSubscriptionBillingCyclesCompleted;

    @Schema(description = "Failed payment attempts", example = "0")
    private Integer userSubscriptionFailedPaymentAttempts;

    @Schema(description = "Last successful payment date")
    private Instant userSubscriptionLastPaymentAt;

    // ========== External IDs ==========

    @Schema(description = "External subscription ID")
    private String userSubscriptionExternalSubscriptionId;

    @Schema(description = "External customer ID")
    private String userSubscriptionExternalCustomerId;

    // ========== Meta ==========

    @Schema(description = "Tenant identifier")
    private String userSubscriptionTenantId;

    @Schema(description = "Custom metadata")
    private String userSubscriptionMetadataJson;

    @Schema(description = "When record was created")
    private Instant userSubscriptionCreatedAt;

    @Schema(description = "When record was last updated")
    private Instant userSubscriptionUpdatedAt;
}
