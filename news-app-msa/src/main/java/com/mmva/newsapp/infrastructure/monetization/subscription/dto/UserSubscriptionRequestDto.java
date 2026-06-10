package com.mmva.newsapp.infrastructure.monetization.subscription.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request DTO for subscribing a user to a plan.
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
@Schema(description = "Request for subscribing a user to a plan")
public class UserSubscriptionRequestDto {

    @Schema(description = "User ID to subscribe (for admindashboard operations)", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID userSubscriptionUserId;

    @Schema(description = "Plan ID to subscribe to", example = "123e4567-e89b-12d3-a456-426614174001", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Plan ID is required")
    private UUID userSubscriptionPlanId;

    @Schema(description = "Promo/coupon code to apply", example = "SAVE20")
    @Size(max = 50, message = "Promo code must not exceed 50 characters")
    private String userSubscriptionPromoCode;

    @Schema(description = "External payment method ID (e.g., Stripe PaymentMethod ID)", example = "pm_1ABC123")
    @Size(max = 100, message = "Payment method ID must not exceed 100 characters")
    private String userSubscriptionPaymentMethodId;

    @Schema(description = "Whether to start with trial period if available", example = "true", defaultValue = "true")
    @Builder.Default
    private Boolean userSubscriptionStartWithTrial = true;

    @Schema(description = "Tenant identifier", example = "default", defaultValue = "default")
    @Size(max = 50, message = "Tenant ID must not exceed 50 characters")
    @Builder.Default
    private String userSubscriptionTenantId = "default";

    @Schema(description = "Metadata JSON for custom data", example = "{\"source\": \"mobile_app\"}")
    @Size(max = 4000, message = "Metadata must not exceed 4000 characters")
    private String userSubscriptionMetadataJson;
}
