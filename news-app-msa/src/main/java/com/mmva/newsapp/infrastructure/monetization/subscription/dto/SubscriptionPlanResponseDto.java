package com.mmva.newsapp.infrastructure.monetization.subscription.dto;

import com.mmva.newsapp.infrastructure.monetization.subscription.enums.SubscriptionPlanBillingCycle;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for subscription plan data.
 * 
 * <p>
 * Field naming follows entity pattern: {@code subscriptionPlan{FieldName}}
 * </p>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Subscription plan response data")
public class SubscriptionPlanResponseDto {

    @Schema(description = "Plan unique identifier")
    private UUID subscriptionPlanId;

    @Schema(description = "Unique plan code", example = "BASIC_MONTHLY")
    private String subscriptionPlanCode;

    @Schema(description = "Display name", example = "Basic Monthly")
    private String subscriptionPlanName;

    @Schema(description = "Description of plan", example = "Access to all basic features")
    private String subscriptionPlanDescription;

    @Schema(description = "Tier code (0=FREE, 1=BASIC, 2=PRO, 3=ENTERPRISE)", example = "1")
    private Integer subscriptionPlanTierCode;

    @Schema(description = "Tier display name", example = "Basic")
    private String subscriptionPlanTierDisplayName;

    @Schema(description = "Plan price", example = "9.99")
    private BigDecimal subscriptionPlanPrice;

    @Schema(description = "Currency code", example = "USD")
    private String subscriptionPlanCurrency;

    @Schema(description = "Formatted price with currency", example = "$9.99")
    private String subscriptionPlanFormattedPrice;

    @Schema(description = "Billing cycle", example = "MONTHLY")
    private SubscriptionPlanBillingCycle subscriptionPlanBillingCycle;

    @Schema(description = "Billing cycle display name", example = "Monthly")
    private String subscriptionPlanBillingCycleDisplayName;

    @Schema(description = "Trial period in days", example = "7")
    private Integer subscriptionPlanTrialDays;

    @Schema(description = "Whether plan has trial", example = "true")
    private Boolean subscriptionPlanHasTrialPeriod;

    @Schema(description = "Feature flags JSON")
    private String subscriptionPlanFeaturesJson;

    @Schema(description = "Maximum devices allowed")
    private Integer subscriptionPlanMaxDevices;

    @Schema(description = "Maximum concurrent sessions")
    private Integer subscriptionPlanMaxConcurrentSessions;

    @Schema(description = "Whether ads are shown", example = "false")
    private Boolean subscriptionPlanShowAds;

    @Schema(description = "Whether this is ad-free", example = "true")
    private Boolean subscriptionPlanIsAdFree;

    @Schema(description = "Display order", example = "1")
    private Integer subscriptionPlanDisplayOrder;

    @Schema(description = "Whether plan is active", example = "true")
    private Boolean subscriptionPlanIsActive;

    @Schema(description = "Whether this is recommended", example = "true")
    private Boolean subscriptionPlanIsRecommended;

    @Schema(description = "Whether this is a free plan", example = "false")
    private Boolean subscriptionPlanIsFree;

    @Schema(description = "Whether this is recurring", example = "true")
    private Boolean subscriptionPlanIsRecurring;

    @Schema(description = "Monthly equivalent cost", example = "9.99")
    private BigDecimal subscriptionPlanMonthlyEquivalent;

    @Schema(description = "Yearly cost", example = "119.88")
    private BigDecimal subscriptionPlanYearlyCost;

    @Schema(description = "Savings compared to monthly", example = "Save 20%")
    private String subscriptionPlanSavingsText;

    @Schema(description = "Tenant identifier")
    private String subscriptionPlanTenantId;

    @Schema(description = "External price ID")
    private String subscriptionPlanExternalPriceId;

    @Schema(description = "External product ID")
    private String subscriptionPlanExternalProductId;

    @Schema(description = "When the plan was created")
    private Instant subscriptionPlanCreatedAt;

    @Schema(description = "When the plan was last updated")
    private Instant subscriptionPlanUpdatedAt;
}
