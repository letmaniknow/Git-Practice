package com.mmva.newsapp.infrastructure.monetization.subscription.mapper;

import com.mmva.newsapp.infrastructure.monetization.subscription.dto.SubscriptionTransactionRequestDto;
import com.mmva.newsapp.infrastructure.monetization.subscription.dto.SubscriptionTransactionResponseDto;
import com.mmva.newsapp.infrastructure.monetization.subscription.model.SubscriptionTransaction;
import org.mapstruct.*;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

/**
 * MapStruct mapper for SubscriptionTransaction entity.
 * 
 * <p>
 * Handles conversion between SubscriptionTransaction entity and DTOs.
 * Field names match between Entity and DTO for simplified auto-mapping.
 * </p>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Mapper(componentModel = "spring")
public interface SubscriptionTransactionMapper {

    // ========================================
    // Entity Creation (DTO fields match entity fields - auto-maps)
    // ========================================

    /**
     * Maps request DTO to new entity.
     * Most fields auto-map due to matching names.
     */
    @Mappings({
            // Ignore auto-generated and audit fields
            @Mapping(target = "subscriptionTransactionId", ignore = true),
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "createdBy", ignore = true),
            @Mapping(target = "updatedAt", ignore = true),
            @Mapping(target = "updatedBy", ignore = true),
            @Mapping(target = "deletedAt", ignore = true),
            @Mapping(target = "deletedBy", ignore = true),
            // Computed fields
            @Mapping(target = "subscriptionTransactionNetAmount", expression = "java(calculateNetAmount(dto))"),
            @Mapping(target = "subscriptionTransactionDate", expression = "java(dto.getSubscriptionTransactionDate() != null ? dto.getSubscriptionTransactionDate() : java.time.Instant.now())"),
            // Relationship - handled by service layer
            @Mapping(target = "subscriptionTransactionSubscription", ignore = true),
            // Fields not in request DTO
            @Mapping(target = "subscriptionTransactionExternalChargeId", ignore = true),
            @Mapping(target = "subscriptionTransactionFailureCode", ignore = true),
            @Mapping(target = "subscriptionTransactionFailureMessage", ignore = true),
            @Mapping(target = "subscriptionTransactionIpAddress", ignore = true),
            @Mapping(target = "subscriptionTransactionUserAgent", ignore = true),
            // Period dates are set by service layer based on subscription billing cycle
            @Mapping(target = "subscriptionTransactionPeriodStart", ignore = true),
            @Mapping(target = "subscriptionTransactionPeriodEnd", ignore = true)
            // All other fields auto-map
    })
    SubscriptionTransaction toEntity(SubscriptionTransactionRequestDto dto);

    // ========================================
    // Response DTO Mapping
    // ========================================

    /**
     * Maps entity to response DTO with computed fields.
     * Most fields auto-map due to matching names.
     */
    @Mappings({
            // Relationship mapping
            @Mapping(source = "subscriptionTransactionSubscription.userSubscriptionId", target = "subscriptionTransactionSubscriptionId"),

            // Audit field mappings (different names)
            @Mapping(source = "createdAt", target = "subscriptionTransactionCreatedAt"),
            @Mapping(source = "updatedAt", target = "subscriptionTransactionUpdatedAt"),

            // Computed fields
            @Mapping(target = "subscriptionTransactionTypeDisplayName", expression = "java(entity.getSubscriptionTransactionType() != null ? entity.getSubscriptionTransactionType().getDisplayName() : null)"),
            @Mapping(target = "subscriptionTransactionStatusDisplayName", expression = "java(entity.getSubscriptionTransactionStatus() != null ? entity.getSubscriptionTransactionStatus().getDisplayName() : null)"),
            @Mapping(target = "subscriptionTransactionFormattedAmount", expression = "java(formatAmount(entity.getSubscriptionTransactionAmount(), entity.getSubscriptionTransactionCurrency()))"),
            @Mapping(target = "subscriptionTransactionFormattedNetAmount", expression = "java(formatAmount(entity.getSubscriptionTransactionNetAmount(), entity.getSubscriptionTransactionCurrency()))"),
            @Mapping(target = "subscriptionTransactionPaymentMethodDisplay", expression = "java(buildPaymentMethodDisplay(entity))"),
            @Mapping(target = "subscriptionTransactionIsSuccessful", expression = "java(entity.isSuccessful())"),
            @Mapping(target = "subscriptionTransactionIsRefund", expression = "java(entity.isRefund())"),
            @Mapping(target = "subscriptionTransactionIsCharge", expression = "java(entity.isCharge())"),
            @Mapping(target = "subscriptionTransactionCanBeRefunded", expression = "java(entity.canBeRefunded())"),
            @Mapping(target = "subscriptionTransactionDescription", expression = "java(entity.getDescription())")
            // All other fields auto-map: subscriptionTransactionId,
            // subscriptionTransactionUserId, etc.
    })
    SubscriptionTransactionResponseDto toResponseDto(SubscriptionTransaction entity);

    /**
     * Maps list of entities to response DTOs.
     */
    List<SubscriptionTransactionResponseDto> toResponseDtoList(List<SubscriptionTransaction> entities);

    // ========================================
    // Helper Methods
    // ========================================

    /**
     * Calculates net amount from request DTO fields.
     *
     * @param dto the request DTO
     * @return calculated net amount
     */
    default BigDecimal calculateNetAmount(SubscriptionTransactionRequestDto dto) {
        if (dto.getSubscriptionTransactionAmount() == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal amount = dto.getSubscriptionTransactionAmount();
        BigDecimal tax = dto.getSubscriptionTransactionTaxAmount() != null
                ? dto.getSubscriptionTransactionTaxAmount()
                : BigDecimal.ZERO;
        BigDecimal discount = dto.getSubscriptionTransactionDiscountAmount() != null
                ? dto.getSubscriptionTransactionDiscountAmount()
                : BigDecimal.ZERO;
        return amount.subtract(discount).add(tax);
    }

    /**
     * Formats an amount with currency symbol.
     *
     * @param amount   the amount
     * @param currency the currency code
     * @return formatted amount string
     */
    default String formatAmount(BigDecimal amount, String currency) {
        if (amount == null) {
            return null;
        }
        try {
            NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.US);
            if (currency != null) {
                formatter.setCurrency(Currency.getInstance(currency));
            }
            return formatter.format(amount);
        } catch (Exception e) {
            return (currency != null ? currency + " " : "$") + amount.toPlainString();
        }
    }

    /**
     * Builds a display-friendly payment method string.
     *
     * @param entity the transaction entity
     * @return formatted payment method display
     */
    default String buildPaymentMethodDisplay(SubscriptionTransaction entity) {
        if (entity.getSubscriptionTransactionPaymentMethod() == null) {
            return null;
        }

        String method = entity.getSubscriptionTransactionPaymentMethod();

        // For card payments, format as "Brand •••• 1234"
        if ("card".equalsIgnoreCase(method)) {
            String brand = entity.getSubscriptionTransactionCardBrand() != null
                    ? capitalize(entity.getSubscriptionTransactionCardBrand())
                    : "Card";
            String lastFour = entity.getSubscriptionTransactionCardLastFour() != null
                    ? entity.getSubscriptionTransactionCardLastFour()
                    : "****";
            return brand + " •••• " + lastFour;
        }

        // For other methods, just capitalize
        return capitalize(method);
    }

    /**
     * Capitalizes the first letter of a string.
     *
     * @param str the string
     * @return capitalized string
     */
    default String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}
