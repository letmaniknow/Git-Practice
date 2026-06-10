package com.mmva.newsapp.infrastructure.monetization.subscription.model;

import com.mmva.newsapp.infrastructure.common.audit.model.BaseAuditEntity;
import com.mmva.newsapp.infrastructure.monetization.subscription.enums.SubscriptionPlanBillingCycle;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Entity representing a subscription plan/tier available for purchase.
 * 
 * <p>
 * Subscription plans define the pricing, features, and billing terms
 * for premium access to content. Plans are created by admins and
 * purchased by users.
 * </p>
 * 
 * <h3>Portability Note:</h3>
 * <p>
 * This entity is designed to be portable across applications.
 * It uses {@code subscriptionPlanTenantId} for multi-app support and stores
 * tier as an integer code (not an enum reference) for flexibility.
 * </p>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Entity
@Table(name = "monetization_subscription_plan", uniqueConstraints = {
        @UniqueConstraint(name = "uk_subscription_plan_code_tenant", columnNames = { "subscription_plan_code",
                "subscription_plan_tenant_id" })
}, indexes = {
        @Index(name = "idx_subscription_plan_active", columnList = "subscription_plan_is_active"),
        @Index(name = "idx_subscription_plan_tier", columnList = "subscription_plan_tier_code"),
        @Index(name = "idx_subscription_plan_tenant", columnList = "subscription_plan_tenant_id")
})
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class SubscriptionPlan extends BaseAuditEntity {

    /**
     * Primary key - UUID for global uniqueness.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "subscription_plan_id", updatable = false, nullable = false)
    private UUID subscriptionPlanId;

    /**
     * Unique code for the plan (e.g., "BASIC_MONTHLY", "PRO_ANNUAL").
     * Used for programmatic reference and API calls.
     */
    @Column(name = "subscription_plan_code", nullable = false, length = 50)
    private String subscriptionPlanCode;

    /**
     * Display name of the plan (e.g., "Basic Plan", "Pro Plan").
     */
    @Column(name = "subscription_plan_name", nullable = false, length = 100)
    private String subscriptionPlanName;

    /**
     * Detailed description of plan features and benefits.
     */
    @Column(name = "subscription_plan_description", length = 1000)
    private String subscriptionPlanDescription;

    /**
     * Tier code representing access level (0=FREE, 1=BASIC, 2=PRO, 3=ENTERPRISE).
     * Uses integer code for portability - maps to PremiumTier enum in consuming
     * app.
     */
    @Column(name = "subscription_plan_tier_code", nullable = false)
    private Integer subscriptionPlanTierCode;

    /**
     * Base price of the plan.
     * Precision 10, scale 2 for monetary values up to 99,999,999.99.
     */
    @Column(name = "subscription_plan_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal subscriptionPlanPrice;

    /**
     * ISO 4217 currency code (e.g., "USD", "EUR", "GBP").
     */
    @Column(name = "subscription_plan_currency", nullable = false, length = 3)
    @Builder.Default
    private String subscriptionPlanCurrency = "USD";

    /**
     * Billing cycle for recurring payments.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_plan_billing_cycle", nullable = false, length = 20)
    private SubscriptionPlanBillingCycle subscriptionPlanBillingCycle;

    /**
     * Number of free trial days for new subscribers.
     * 0 = no trial period.
     */
    @Column(name = "subscription_plan_trial_days", nullable = false)
    @Builder.Default
    private Integer subscriptionPlanTrialDays = 0;

    /**
     * JSON string containing feature flags and limits.
     * Example: {"max_bookmarks": 100, "offline_reading": true, "ad_free": true}
     */
    @Column(name = "subscription_plan_features_json")
    private String subscriptionPlanFeaturesJson;

    /**
     * Maximum number of devices for this plan.
     * null = unlimited.
     */
    @Column(name = "subscription_plan_max_devices")
    private Integer subscriptionPlanMaxDevices;

    /**
     * Maximum concurrent sessions for this plan.
     * null = unlimited.
     */
    @Column(name = "subscription_plan_max_concurrent_sessions")
    private Integer subscriptionPlanMaxConcurrentSessions;

    /**
     * Whether ads are shown to subscribers of this plan.
     */
    @Column(name = "subscription_plan_show_ads", nullable = false)
    @Builder.Default
    private Boolean subscriptionPlanShowAds = true;

    /**
     * Display order for sorting plans in UI (lower = first).
     */
    @Column(name = "subscription_plan_display_order", nullable = false)
    @Builder.Default
    private Integer subscriptionPlanDisplayOrder = 0;

    /**
     * Whether this plan is currently active and available for purchase.
     */
    @Column(name = "subscription_plan_is_active", nullable = false)
    @Builder.Default
    private Boolean subscriptionPlanIsActive = true;

    /**
     * Whether this is the recommended/highlighted plan.
     */
    @Column(name = "subscription_plan_is_recommended", nullable = false)
    @Builder.Default
    private Boolean subscriptionPlanIsRecommended = false;

    /**
     * Tenant identifier for multi-app support.
     * Allows same plan structure to be used across different applications.
     */
    @Column(name = "subscription_plan_tenant_id", nullable = false, length = 50)
    @Builder.Default
    private String subscriptionPlanTenantId = "default";

    /**
     * External reference ID for integration with payment providers.
     * Example: Stripe Price ID, PayPal Plan ID.
     */
    @Column(name = "subscription_plan_external_price_id", length = 100)
    private String subscriptionPlanExternalPriceId;

    /**
     * External product ID for integration with payment providers.
     * Example: Stripe Product ID.
     */
    @Column(name = "subscription_plan_external_product_id", length = 100)
    private String subscriptionPlanExternalProductId;

    // ========================================
    // Business Methods
    // ========================================

    /**
     * Checks if this plan has a trial period.
     *
     * @return true if trial days > 0
     */
    public boolean hasTrialPeriod() {
        return subscriptionPlanTrialDays != null && subscriptionPlanTrialDays > 0;
    }

    /**
     * Checks if this is a free plan.
     *
     * @return true if price is zero or null
     */
    public boolean isFree() {
        return subscriptionPlanPrice == null ||
                subscriptionPlanPrice.compareTo(BigDecimal.ZERO) == 0;
    }

    /**
     * Checks if this is a recurring subscription (not lifetime).
     *
     * @return true if billing cycle is recurring
     */
    public boolean isRecurring() {
        return subscriptionPlanBillingCycle != null &&
                subscriptionPlanBillingCycle.isRecurring();
    }

    /**
     * Checks if this plan is ad-free.
     *
     * @return true if no ads shown
     */
    public boolean isAdFree() {
        return subscriptionPlanShowAds == null || !subscriptionPlanShowAds;
    }

    /**
     * Calculates yearly cost for this plan.
     *
     * @return yearly cost based on billing cycle
     */
    public BigDecimal calculateYearlyCost() {
        if (subscriptionPlanPrice == null || subscriptionPlanBillingCycle == null) {
            return BigDecimal.ZERO;
        }
        if (subscriptionPlanBillingCycle == SubscriptionPlanBillingCycle.LIFETIME) {
            return subscriptionPlanPrice;
        }
        double cyclesPerYear = subscriptionPlanBillingCycle.getCyclesPerYear();
        return subscriptionPlanPrice.multiply(BigDecimal.valueOf(cyclesPerYear));
    }

    /**
     * Calculates monthly equivalent cost (for comparison).
     *
     * @return monthly equivalent cost
     */
    public BigDecimal calculateMonthlyEquivalent() {
        BigDecimal yearlyCost = calculateYearlyCost();
        if (yearlyCost.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return yearlyCost.divide(BigDecimal.valueOf(12), 2, java.math.RoundingMode.HALF_UP);
    }
}
