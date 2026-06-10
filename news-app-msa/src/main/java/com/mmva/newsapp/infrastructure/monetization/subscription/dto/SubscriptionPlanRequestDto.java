package com.mmva.newsapp.infrastructure.monetization.subscription.dto;

import com.mmva.newsapp.infrastructure.monetization.subscription.enums.SubscriptionPlanBillingCycle;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for creating or updating a subscription plan.
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
@Schema(description = "Request for creating or updating a subscription plan")
public class SubscriptionPlanRequestDto {

    @Schema(description = "Unique plan code (e.g., BASIC_MONTHLY, PRO_ANNUAL)", example = "BASIC_MONTHLY", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Plan code is required")
    @Size(min = 3, max = 50, message = "Plan code must be between 3 and 50 characters")
    @Pattern(regexp = "^[A-Z0-9_]+$", message = "Plan code must be uppercase alphanumeric with underscores only")
    private String subscriptionPlanCode;

    @Schema(description = "Display name of the plan", example = "Basic Monthly", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Plan name is required")
    @Size(min = 2, max = 100, message = "Plan name must be between 2 and 100 characters")
    private String subscriptionPlanName;

    @Schema(description = "Description of plan features and benefits", example = "Access to all basic features with monthly billing")
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String subscriptionPlanDescription;

    @Schema(description = "Tier code (0=FREE, 1=BASIC, 2=PRO, 3=ENTERPRISE)", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Tier code is required")
    @Min(value = 0, message = "Tier code must be 0 or greater")
    @Max(value = 3, message = "Tier code must be 3 or less")
    private Integer subscriptionPlanTierCode;

    @Schema(description = "Price of the plan", example = "9.99", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.00", message = "Price cannot be negative")
    @DecimalMax(value = "99999999.99", message = "Price exceeds maximum allowed")
    private BigDecimal subscriptionPlanPrice;

    @Schema(description = "ISO 4217 currency code", example = "USD", defaultValue = "USD")
    @Size(min = 3, max = 3, message = "Currency code must be exactly 3 characters")
    @Builder.Default
    private String subscriptionPlanCurrency = "USD";

    @Schema(description = "Billing cycle for recurring payments", example = "MONTHLY", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Billing cycle is required")
    private SubscriptionPlanBillingCycle subscriptionPlanBillingCycle;

    @Schema(description = "Number of free trial days (0 = no trial)", example = "7", defaultValue = "0")
    @Min(value = 0, message = "Trial days cannot be negative")
    @Max(value = 365, message = "Trial days cannot exceed 365")
    @Builder.Default
    private Integer subscriptionPlanTrialDays = 0;

    @Schema(description = "JSON string containing feature flags", example = "{\"max_bookmarks\": 100, \"offline_reading\": true}")
    @Size(max = 4000, message = "Features JSON must not exceed 4000 characters")
    private String subscriptionPlanFeaturesJson;

    @Schema(description = "Maximum devices for this plan (null = unlimited)", example = "3")
    @Min(value = 1, message = "Max devices must be at least 1")
    private Integer subscriptionPlanMaxDevices;

    @Schema(description = "Maximum concurrent sessions (null = unlimited)", example = "2")
    @Min(value = 1, message = "Max concurrent sessions must be at least 1")
    private Integer subscriptionPlanMaxConcurrentSessions;

    @Schema(description = "Whether ads are shown to subscribers", example = "false", defaultValue = "true")
    @Builder.Default
    private Boolean subscriptionPlanShowAds = true;

    @Schema(description = "Display order (lower = first)", example = "1", defaultValue = "0")
    @Min(value = 0, message = "Display order cannot be negative")
    @Builder.Default
    private Integer subscriptionPlanDisplayOrder = 0;

    @Schema(description = "Whether this plan is active", example = "true", defaultValue = "true")
    @Builder.Default
    private Boolean subscriptionPlanIsActive = true;

    @Schema(description = "Whether this is the recommended plan", example = "true", defaultValue = "false")
    @Builder.Default
    private Boolean subscriptionPlanIsRecommended = false;

    @Schema(description = "Tenant identifier for multi-app support", example = "default", defaultValue = "default")
    @Size(max = 50, message = "Tenant ID must not exceed 50 characters")
    @Builder.Default
    private String subscriptionPlanTenantId = "default";

    @Schema(description = "External price ID from payment provider (e.g., Stripe Price ID)", example = "price_1ABC123DEF456")
    @Size(max = 100, message = "External price ID must not exceed 100 characters")
    private String subscriptionPlanExternalPriceId;

    @Schema(description = "External product ID from payment provider", example = "prod_ABC123")
    @Size(max = 100, message = "External product ID must not exceed 100 characters")
    private String subscriptionPlanExternalProductId;
}
