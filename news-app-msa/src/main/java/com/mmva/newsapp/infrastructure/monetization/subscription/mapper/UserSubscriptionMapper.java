package com.mmva.newsapp.infrastructure.monetization.subscription.mapper;

import com.mmva.newsapp.infrastructure.monetization.subscription.dto.UserSubscriptionResponseDto;
import com.mmva.newsapp.infrastructure.monetization.subscription.model.SubscriptionPlan;
import com.mmva.newsapp.infrastructure.monetization.subscription.model.UserSubscription;
import org.mapstruct.*;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.Instant;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

/**
 * MapStruct mapper for UserSubscription entity.
 * 
 * <p>
 * Handles conversion between UserSubscription entity and DTOs.
 * Field names match between Entity and DTO for simplified auto-mapping.
 * </p>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Mapper(componentModel = "spring")
public interface UserSubscriptionMapper {

    // ========================================
    // Response DTO Mapping
    // ========================================

    /**
     * Maps entity to response DTO with computed fields.
     * Most fields auto-map due to matching names.
     */
    @Mappings({
            // Audit field mappings (different names)
            @Mapping(source = "createdAt", target = "userSubscriptionCreatedAt"),
            @Mapping(source = "updatedAt", target = "userSubscriptionUpdatedAt"),

            // Plan info mappings (nested object -> flat fields)
            @Mapping(source = "userSubscriptionPlan.subscriptionPlanId", target = "userSubscriptionPlanId"),
            @Mapping(source = "userSubscriptionPlan.subscriptionPlanCode", target = "userSubscriptionPlanCode"),
            @Mapping(source = "userSubscriptionPlan.subscriptionPlanName", target = "userSubscriptionPlanName"),
            @Mapping(source = "userSubscriptionPlan.subscriptionPlanTierCode", target = "userSubscriptionTierCode"),
            @Mapping(target = "userSubscriptionTierDisplayName", expression = "java(getTierDisplayName(entity.getUserSubscriptionPlan()))"),

            // Computed status fields
            @Mapping(target = "userSubscriptionStatusDisplayName", expression = "java(entity.getUserSubscriptionStatus() != null ? entity.getUserSubscriptionStatus().name() : null)"),
            @Mapping(target = "userSubscriptionGrantsAccess", expression = "java(entity.grantsAccess())"),
            @Mapping(target = "userSubscriptionFormattedPrice", expression = "java(formatSubscriptionPrice(entity))"),
            @Mapping(target = "userSubscriptionEffectivePrice", expression = "java(entity.getEffectivePrice())"),
            @Mapping(target = "userSubscriptionIsInTrial", expression = "java(entity.isInTrial())"),
            @Mapping(target = "userSubscriptionTrialDaysRemaining", expression = "java(calculateTrialDaysRemaining(entity))"),
            @Mapping(target = "userSubscriptionWillRenew", expression = "java(entity.willRenew())"),
            @Mapping(target = "userSubscriptionDaysRemaining", expression = "java(entity.getDaysRemaining())"),
            @Mapping(target = "userSubscriptionIsExpired", expression = "java(entity.isExpired())"),
            @Mapping(target = "userSubscriptionIsAdFree", expression = "java(entity.getUserSubscriptionPlan() != null ? entity.getUserSubscriptionPlan().isAdFree() : false)")
            // All other fields auto-map: userSubscriptionId, userSubscriptionUserId,
            // userSubscriptionStatus, etc.
    })
    UserSubscriptionResponseDto toResponseDto(UserSubscription entity);

    /**
     * Maps list of entities to response DTOs.
     */
    List<UserSubscriptionResponseDto> toResponseDtoList(List<UserSubscription> entities);

    // ========================================
    // Helper Methods
    // ========================================

    /**
     * Gets display name for tier from plan.
     */
    default String getTierDisplayName(SubscriptionPlan plan) {
        if (plan == null || plan.getSubscriptionPlanTierCode() == null)
            return "Unknown";
        return switch (plan.getSubscriptionPlanTierCode()) {
            case 0 -> "Free";
            case 1 -> "Basic";
            case 2 -> "Pro";
            case 3 -> "Enterprise";
            default -> "Tier " + plan.getSubscriptionPlanTierCode();
        };
    }

    /**
     * Formats subscription price with billing cycle.
     */
    default String formatSubscriptionPrice(UserSubscription entity) {
        if (entity == null || entity.getUserSubscriptionCurrentPrice() == null)
            return null;

        String currencyCode = entity.getUserSubscriptionCurrency() != null
                ? entity.getUserSubscriptionCurrency()
                : "USD";
        BigDecimal price = entity.getEffectivePrice();

        try {
            Currency currency = Currency.getInstance(currencyCode);
            NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.US);
            formatter.setCurrency(currency);
            String formattedAmount = formatter.format(price);

            // Add billing cycle suffix
            if (entity.getUserSubscriptionPlan() != null &&
                    entity.getUserSubscriptionPlan().getSubscriptionPlanBillingCycle() != null) {
                String cycleSuffix = switch (entity.getUserSubscriptionPlan()
                        .getSubscriptionPlanBillingCycle()) {
                    case WEEKLY -> "/week";
                    case MONTHLY -> "/month";
                    case QUARTERLY -> "/quarter";
                    case SEMI_ANNUAL -> "/6 months";
                    case ANNUAL -> "/year";
                    case LIFETIME -> " (one-time)";
                };
                return formattedAmount + cycleSuffix;
            }
            return formattedAmount;
        } catch (Exception e) {
            return currencyCode + " " + price.setScale(2, java.math.RoundingMode.HALF_UP);
        }
    }

    /**
     * Calculates remaining trial days.
     */
    default Long calculateTrialDaysRemaining(UserSubscription entity) {
        if (entity == null || !entity.isInTrial() ||
                entity.getUserSubscriptionTrialEnd() == null) {
            return null;
        }
        long seconds = entity.getUserSubscriptionTrialEnd().getEpochSecond()
                - Instant.now().getEpochSecond();
        return Math.max(0, seconds / 86400);
    }
}
