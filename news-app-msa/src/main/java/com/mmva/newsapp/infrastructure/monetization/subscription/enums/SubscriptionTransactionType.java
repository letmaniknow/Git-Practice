package com.mmva.newsapp.infrastructure.monetization.subscription.enums;

/**
 * Enum representing the type of subscription transaction.
 * 
 * <p>
 * Categorizes financial transactions related to subscriptions
 * for accurate accounting and reporting.
 * </p>
 * 
 * <h3>Naming Convention:</h3>
 * <p>
 * Prefixed with {@code SubscriptionTransaction} per PROJECT_PRINCIPLES.md §6.1
 * Feature-Contextual Naming to clearly indicate entity ownership.
 * </p>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
public enum SubscriptionTransactionType {

    /**
     * Initial subscription purchase.
     */
    SUBSCRIPTION_CREATED("Subscription Created", true, false),

    /**
     * Recurring subscription renewal.
     */
    RENEWAL("Renewal", true, false),

    /**
     * Upgrade from lower tier plan.
     * May include prorated charge or credit.
     */
    UPGRADE("Upgrade", true, false),

    /**
     * Downgrade to lower tier plan.
     * May result in credit for next billing cycle.
     */
    DOWNGRADE("Downgrade", false, true),

    /**
     * Full refund of a payment.
     */
    REFUND("Refund", false, true),

    /**
     * Partial refund.
     */
    PARTIAL_REFUND("Partial Refund", false, true),

    /**
     * Promo/discount credit applied.
     */
    CREDIT("Credit Applied", false, true),

    /**
     * One-time charge (addon, overage, etc.).
     */
    ONE_TIME_CHARGE("One-Time Charge", true, false),

    /**
     * Payment failed (recorded for audit).
     */
    PAYMENT_FAILED("Payment Failed", false, false),

    /**
     * Chargeback from customer dispute.
     */
    CHARGEBACK("Chargeback", false, true),

    /**
     * Trial conversion to paid.
     */
    TRIAL_CONVERSION("Trial Conversion", true, false),

    /**
     * Plan change (neither upgrade nor downgrade).
     */
    PLAN_CHANGE("Plan Change", false, false);

    private final String displayName;
    private final boolean isCharge;
    private final boolean isCredit;

    SubscriptionTransactionType(String displayName, boolean isCharge, boolean isCredit) {
        this.displayName = displayName;
        this.isCharge = isCharge;
        this.isCredit = isCredit;
    }

    /**
     * Returns a human-readable display name.
     *
     * @return the display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Checks if this transaction type represents a charge to customer.
     *
     * @return true if charge
     */
    public boolean isCharge() {
        return isCharge;
    }

    /**
     * Checks if this transaction type represents a credit to customer.
     *
     * @return true if credit
     */
    public boolean isCredit() {
        return isCredit;
    }

    /**
     * Checks if this is a refund-type transaction.
     *
     * @return true if refund or chargeback
     */
    public boolean isRefund() {
        return this == REFUND || this == PARTIAL_REFUND || this == CHARGEBACK;
    }

    /**
     * Gets the sign multiplier for amount calculations.
     * Charges are positive, credits are negative.
     *
     * @return 1 for charges, -1 for credits, 0 for neutral
     */
    public int getAmountSign() {
        if (isCharge)
            return 1;
        if (isCredit)
            return -1;
        return 0;
    }
}
