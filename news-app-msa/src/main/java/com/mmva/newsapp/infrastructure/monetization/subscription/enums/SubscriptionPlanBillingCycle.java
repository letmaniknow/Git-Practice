package com.mmva.newsapp.infrastructure.monetization.subscription.enums;

/**
 * Enum representing billing cycle options for subscription plans.
 * 
 * <p>
 * Defines the frequency at which subscription charges occur.
 * Each cycle has a corresponding number of days for period calculations.
 * </p>
 * 
 * <h3>Industry Standard Cycles:</h3>
 * <ul>
 * <li>{@code WEEKLY} - 7 days, common for trials</li>
 * <li>{@code MONTHLY} - 30 days, most common</li>
 * <li>{@code QUARTERLY} - 90 days, 3-month commitment</li>
 * <li>{@code SEMI_ANNUAL} - 180 days, 6-month commitment</li>
 * <li>{@code ANNUAL} - 365 days, typically discounted</li>
 * <li>{@code LIFETIME} - One-time payment, no renewal</li>
 * </ul>
 * 
 * <h3>Naming Convention:</h3>
 * <p>
 * Prefixed with {@code SubscriptionPlan} per PROJECT_PRINCIPLES.md §6.1
 * Feature-Contextual Naming to clearly indicate entity ownership.
 * </p>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
public enum SubscriptionPlanBillingCycle {

    /**
     * Weekly billing - 7 days.
     * Often used for trial periods or low-commitment subscriptions.
     */
    WEEKLY(7, "Weekly"),

    /**
     * Monthly billing - 30 days.
     * Most common subscription cycle, standard for SaaS products.
     */
    MONTHLY(30, "Monthly"),

    /**
     * Quarterly billing - 90 days (3 months).
     * Moderate commitment with slight discount typically offered.
     */
    QUARTERLY(90, "Quarterly"),

    /**
     * Semi-annual billing - 180 days (6 months).
     * Half-year commitment with moderate discount.
     */
    SEMI_ANNUAL(180, "Semi-Annual"),

    /**
     * Annual billing - 365 days.
     * Full year commitment, typically offers best discount (15-20% off).
     */
    ANNUAL(365, "Annual"),

    /**
     * Lifetime access - no renewal required.
     * One-time payment for permanent access.
     */
    LIFETIME(0, "Lifetime");

    private final int days;
    private final String displayName;

    SubscriptionPlanBillingCycle(int days, String displayName) {
        this.days = days;
        this.displayName = displayName;
    }

    /**
     * Returns the number of days in this billing cycle.
     * Returns 0 for LIFETIME (no renewal).
     *
     * @return number of days in the cycle
     */
    public int getDays() {
        return days;
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
     * Checks if this cycle requires recurring billing.
     *
     * @return true if recurring, false for LIFETIME
     */
    public boolean isRecurring() {
        return this != LIFETIME;
    }

    /**
     * Returns the number of cycles per year for revenue calculations.
     * Returns 1 for LIFETIME.
     *
     * @return cycles per year
     */
    public double getCyclesPerYear() {
        if (this == LIFETIME)
            return 1.0;
        return 365.0 / days;
    }
}
