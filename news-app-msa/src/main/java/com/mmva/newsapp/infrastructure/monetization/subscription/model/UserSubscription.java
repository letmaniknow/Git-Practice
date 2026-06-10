package com.mmva.newsapp.infrastructure.monetization.subscription.model;

import com.mmva.newsapp.infrastructure.common.audit.model.BaseAuditEntity;
import com.mmva.newsapp.infrastructure.monetization.subscription.enums.UserSubscriptionStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Entity representing a user's subscription to a plan.
 * 
 * <p>
 * Tracks the lifecycle of a user's subscription including:
 * trial periods, billing dates, renewal status, and cancellation.
 * </p>
 * 
 * <h3>Portability Note:</h3>
 * <p>
 * Uses {@code UUID userSubscriptionUserId} instead of entity reference to
 * AppUser.
 * This allows the entity to be portable across applications.
 * </p>
 * 
 * <h3>Subscription Lifecycle:</h3>
 * 
 * <pre>
 * TRIAL -> ACTIVE -> PAST_DUE -> CANCELLED / EXPIRED -> PAUSED -> ACTIVE
 * </pre>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Entity
@Table(name = "monetization_user_subscription", indexes = {
        @Index(name = "idx_user_subscription_user", columnList = "user_subscription_user_id"),
        @Index(name = "idx_user_subscription_plan", columnList = "user_subscription_plan_id"),
        @Index(name = "idx_user_subscription_status", columnList = "user_subscription_status"),
        @Index(name = "idx_user_subscription_tenant", columnList = "user_subscription_tenant_id"),
        @Index(name = "idx_user_subscription_external", columnList = "user_subscription_external_subscription_id"),
        @Index(name = "idx_user_subscription_end_date", columnList = "user_subscription_current_period_end")
})
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class UserSubscription extends BaseAuditEntity {

    /**
     * Primary key - UUID for global uniqueness.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_subscription_id", updatable = false, nullable = false)
    private UUID userSubscriptionId;

    /**
     * User ID who owns this subscription.
     * Portable: Uses UUID instead of AppUser entity reference.
     */
    @Column(name = "user_subscription_user_id", nullable = false)
    private UUID userSubscriptionUserId;

    /**
     * The subscription plan for this subscription.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_subscription_plan_id", nullable = false, foreignKey = @ForeignKey(name = "fk_user_subscription_plan"))
    private SubscriptionPlan userSubscriptionPlan;

    /**
     * Current subscription status.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "user_subscription_status", nullable = false, length = 20)
    @Builder.Default
    private UserSubscriptionStatus userSubscriptionStatus = UserSubscriptionStatus.TRIAL;

    /**
     * When the subscription was initially started.
     */
    @Column(name = "user_subscription_started_at", nullable = false)
    private Instant userSubscriptionStartedAt;

    /**
     * Start of current billing period.
     */
    @Column(name = "user_subscription_current_period_start")
    private Instant userSubscriptionCurrentPeriodStart;

    /**
     * End of current billing period.
     * After this date, subscription needs renewal.
     */
    @Column(name = "user_subscription_current_period_end")
    private Instant userSubscriptionCurrentPeriodEnd;

    /**
     * When the trial period ends (if applicable).
     */
    @Column(name = "user_subscription_trial_end")
    private Instant userSubscriptionTrialEnd;

    /**
     * When the subscription was cancelled (user requested).
     */
    @Column(name = "user_subscription_cancelled_at")
    private Instant userSubscriptionCancelledAt;

    /**
     * Reason for cancellation (user feedback).
     */
    @Column(name = "user_subscription_cancellation_reason", length = 500)
    private String userSubscriptionCancellationReason;

    /**
     * Whether to cancel at end of current period.
     * If true, subscription won't renew but remains active until period end.
     */
    @Column(name = "user_subscription_cancel_at_period_end", nullable = false)
    @Builder.Default
    private Boolean userSubscriptionCancelAtPeriodEnd = false;

    /**
     * When the subscription was paused.
     */
    @Column(name = "user_subscription_paused_at")
    private Instant userSubscriptionPausedAt;

    /**
     * When a paused subscription should resume.
     */
    @Column(name = "user_subscription_resume_at")
    private Instant userSubscriptionResumeAt;

    /**
     * Current price locked in at subscription time.
     * May differ from plan price if grandfathered.
     */
    @Column(name = "user_subscription_current_price", precision = 10, scale = 2)
    private BigDecimal userSubscriptionCurrentPrice;

    /**
     * Currency for this subscription.
     */
    @Column(name = "user_subscription_currency", length = 3)
    @Builder.Default
    private String userSubscriptionCurrency = "USD";

    /**
     * Discount percentage applied (0-100).
     */
    @Column(name = "user_subscription_discount_percent")
    @Builder.Default
    private Integer userSubscriptionDiscountPercent = 0;

    /**
     * Promo/coupon code used for this subscription.
     */
    @Column(name = "user_subscription_promo_code", length = 50)
    private String userSubscriptionPromoCode;

    /**
     * Number of billing cycles completed.
     */
    @Column(name = "user_subscription_billing_cycles_completed", nullable = false)
    @Builder.Default
    private Integer userSubscriptionBillingCyclesCompleted = 0;

    /**
     * Number of failed payment attempts in current period.
     */
    @Column(name = "user_subscription_failed_payment_attempts", nullable = false)
    @Builder.Default
    private Integer userSubscriptionFailedPaymentAttempts = 0;

    /**
     * Last successful payment date.
     */
    @Column(name = "user_subscription_last_payment_at")
    private Instant userSubscriptionLastPaymentAt;

    /**
     * Next scheduled payment date.
     */
    @Column(name = "user_subscription_next_payment_at")
    private Instant userSubscriptionNextPaymentAt;

    /**
     * External subscription ID from payment provider.
     * Example: Stripe Subscription ID.
     */
    @Column(name = "user_subscription_external_subscription_id", length = 100)
    private String userSubscriptionExternalSubscriptionId;

    /**
     * External customer ID from payment provider.
     * Example: Stripe Customer ID.
     */
    @Column(name = "user_subscription_external_customer_id", length = 100)
    private String userSubscriptionExternalCustomerId;

    /**
     * Tenant identifier for multi-app support.
     */
    @Column(name = "user_subscription_tenant_id", nullable = false, length = 50)
    @Builder.Default
    private String userSubscriptionTenantId = "default";

    /**
     * Metadata JSON for additional flexible data.
     */
    @Column(name = "user_subscription_metadata_json")
    private String userSubscriptionMetadataJson;

    // ========================================
    // Business Methods
    // ========================================

    /**
     * Checks if subscription grants access to premium content.
     *
     * @return true if user has active access
     */
    public boolean grantsAccess() {
        return userSubscriptionStatus != null && userSubscriptionStatus.grantsAccess();
    }

    /**
     * Checks if subscription is in trial period.
     *
     * @return true if in trial
     */
    public boolean isInTrial() {
        return userSubscriptionStatus == UserSubscriptionStatus.TRIAL
                && userSubscriptionTrialEnd != null
                && Instant.now().isBefore(userSubscriptionTrialEnd);
    }

    /**
     * Checks if subscription is expired.
     *
     * @return true if expired
     */
    public boolean isExpired() {
        return userSubscriptionStatus == UserSubscriptionStatus.EXPIRED
                || (userSubscriptionCurrentPeriodEnd != null
                        && Instant.now().isAfter(userSubscriptionCurrentPeriodEnd)
                        && userSubscriptionStatus != UserSubscriptionStatus.ACTIVE);
    }

    /**
     * Checks if subscription will renew.
     *
     * @return true if set to renew
     */
    public boolean willRenew() {
        return !Boolean.TRUE.equals(userSubscriptionCancelAtPeriodEnd)
                && userSubscriptionStatus == UserSubscriptionStatus.ACTIVE;
    }

    /**
     * Checks if subscription needs payment retry.
     *
     * @return true if past due
     */
    public boolean needsPaymentRetry() {
        return userSubscriptionStatus == UserSubscriptionStatus.PAST_DUE
                && userSubscriptionFailedPaymentAttempts != null
                && userSubscriptionFailedPaymentAttempts < 4;
    }

    /**
     * Gets the effective price after discount.
     *
     * @return effective price
     */
    public BigDecimal getEffectivePrice() {
        if (userSubscriptionCurrentPrice == null) {
            return BigDecimal.ZERO;
        }
        if (userSubscriptionDiscountPercent == null || userSubscriptionDiscountPercent == 0) {
            return userSubscriptionCurrentPrice;
        }
        BigDecimal discountMultiplier = BigDecimal.valueOf(100 - userSubscriptionDiscountPercent)
                .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
        return userSubscriptionCurrentPrice.multiply(discountMultiplier);
    }

    /**
     * Gets remaining days in current period.
     *
     * @return days remaining, 0 if expired
     */
    public long getDaysRemaining() {
        if (userSubscriptionCurrentPeriodEnd == null) {
            return 0;
        }
        long seconds = userSubscriptionCurrentPeriodEnd.getEpochSecond()
                - Instant.now().getEpochSecond();
        return Math.max(0, seconds / 86400);
    }

    /**
     * Gets the tier code from the associated plan.
     *
     * @return tier code, or 0 if no plan
     */
    public Integer getTierCode() {
        return userSubscriptionPlan != null
                ? userSubscriptionPlan.getSubscriptionPlanTierCode()
                : 0;
    }
}
