package com.mmva.newsapp.infrastructure.monetization.subscription.mapper;

import com.mmva.newsapp.infrastructure.monetization.subscription.enums.SubscriptionPlanBillingCycle;
import com.mmva.newsapp.infrastructure.monetization.subscription.dto.SubscriptionPlanRequestDto;
import com.mmva.newsapp.infrastructure.monetization.subscription.dto.SubscriptionPlanResponseDto;
import com.mmva.newsapp.infrastructure.monetization.subscription.model.SubscriptionPlan;
import org.mapstruct.*;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

/**
 * MapStruct mapper for SubscriptionPlan entity.
 * 
 * <p>
 * Handles conversion between SubscriptionPlan entity and DTOs.
 * Field names match between Entity and DTO for simplified auto-mapping.
 * </p>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Mapper(componentModel = "spring")
public interface SubscriptionPlanMapper {

    // ========================================
    // Entity Creation (DTO fields match entity fields - auto-maps)
    // ========================================

    /**
     * Maps request DTO to new entity.
     * Most fields auto-map due to matching names.
     */
    @Mappings({
            // Ignore auto-generated and audit fields
            @Mapping(target = "subscriptionPlanId", ignore = true),
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "createdBy", ignore = true),
            @Mapping(target = "updatedAt", ignore = true),
            @Mapping(target = "updatedBy", ignore = true),
            @Mapping(target = "deletedAt", ignore = true),
            @Mapping(target = "deletedBy", ignore = true)
            // All other fields auto-map: subscriptionPlanCode, subscriptionPlanName, etc.
    })
    SubscriptionPlan toEntity(SubscriptionPlanRequestDto dto);

    // ========================================
    // Entity Update
    // ========================================

    /**
     * Updates existing entity from DTO.
     */
    @Mappings({
            @Mapping(target = "subscriptionPlanId", ignore = true),
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "createdBy", ignore = true),
            @Mapping(target = "updatedAt", ignore = true),
            @Mapping(target = "updatedBy", ignore = true),
            @Mapping(target = "deletedAt", ignore = true),
            @Mapping(target = "deletedBy", ignore = true)
    })
    void updateEntityFromDto(SubscriptionPlanRequestDto dto, @MappingTarget SubscriptionPlan entity);

    // ========================================
    // Response DTO Mapping
    // ========================================

    /**
     * Maps entity to response DTO with computed fields.
     * Most fields auto-map due to matching names.
     */
    @Mappings({
            // Audit field mappings (different names)
            @Mapping(source = "createdAt", target = "subscriptionPlanCreatedAt"),
            @Mapping(source = "updatedAt", target = "subscriptionPlanUpdatedAt"),

            // Computed fields
            @Mapping(target = "subscriptionPlanTierDisplayName", expression = "java(getTierDisplayName(entity.getSubscriptionPlanTierCode()))"),
            @Mapping(target = "subscriptionPlanFormattedPrice", expression = "java(formatPrice(entity.getSubscriptionPlanPrice(), entity.getSubscriptionPlanCurrency()))"),
            @Mapping(target = "subscriptionPlanBillingCycleDisplayName", expression = "java(entity.getSubscriptionPlanBillingCycle() != null ? entity.getSubscriptionPlanBillingCycle().getDisplayName() : null)"),
            @Mapping(target = "subscriptionPlanHasTrialPeriod", expression = "java(entity.hasTrialPeriod())"),
            @Mapping(target = "subscriptionPlanIsAdFree", expression = "java(entity.isAdFree())"),
            @Mapping(target = "subscriptionPlanIsFree", expression = "java(entity.isFree())"),
            @Mapping(target = "subscriptionPlanIsRecurring", expression = "java(entity.isRecurring())"),
            @Mapping(target = "subscriptionPlanMonthlyEquivalent", expression = "java(entity.calculateMonthlyEquivalent())"),
            @Mapping(target = "subscriptionPlanYearlyCost", expression = "java(entity.calculateYearlyCost())"),
            @Mapping(target = "subscriptionPlanSavingsText", expression = "java(calculateSavingsText(entity))")
            // All other fields auto-map: subscriptionPlanId, subscriptionPlanCode, etc.
    })
    SubscriptionPlanResponseDto toResponseDto(SubscriptionPlan entity);

    /**
     * Maps list of entities to response DTOs.
     */
    List<SubscriptionPlanResponseDto> toResponseDtoList(List<SubscriptionPlan> entities);

    // ========================================
    // Helper Methods
    // ========================================

    /**
     * Gets display name for tier code.
     */
    default String getTierDisplayName(Integer tierCode) {
        if (tierCode == null)
            return "Unknown";
        return switch (tierCode) {
            case 0 -> "Free";
            case 1 -> "Basic";
            case 2 -> "Pro";
            case 3 -> "Enterprise";
            default -> "Tier " + tierCode;
        };
    }

    /**
     * Formats price with currency symbol.
     */
    default String formatPrice(BigDecimal price, String currencyCode) {
        if (price == null)
            return null;
        if (currencyCode == null)
            currencyCode = "USD";

        try {
            Currency currency = Currency.getInstance(currencyCode);
            NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.US);
            formatter.setCurrency(currency);
            return formatter.format(price);
        } catch (Exception e) {
            return currencyCode + " " + price.setScale(2, java.math.RoundingMode.HALF_UP);
        }
    }

    /**
     * Calculates savings text for annual plans.
     */
    default String calculateSavingsText(SubscriptionPlan entity) {
        if (entity == null || entity.getSubscriptionPlanBillingCycle() == null)
            return null;

        SubscriptionPlanBillingCycle cycle = entity.getSubscriptionPlanBillingCycle();
        if (cycle == SubscriptionPlanBillingCycle.MONTHLY || cycle == SubscriptionPlanBillingCycle.WEEKLY) {
            return null;
        }

        BigDecimal yearlyIfMonthly = entity.calculateMonthlyEquivalent()
                .multiply(BigDecimal.valueOf(12));
        BigDecimal actualYearly = entity.calculateYearlyCost();

        if (yearlyIfMonthly.compareTo(BigDecimal.ZERO) == 0)
            return null;

        BigDecimal savings = yearlyIfMonthly.subtract(actualYearly);
        if (savings.compareTo(BigDecimal.ZERO) <= 0)
            return null;

        BigDecimal savingsPercent = savings.divide(yearlyIfMonthly, 2, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

        int percent = savingsPercent.intValue();
        if (percent > 0) {
            return "Save " + percent + "%";
        }
        return null;
    }
}
