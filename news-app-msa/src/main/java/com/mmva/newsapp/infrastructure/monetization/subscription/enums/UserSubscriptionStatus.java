package com.mmva.newsapp.infrastructure.monetization.subscription.enums;

/**
 * Enum representing the lifecycle status of a user subscription.
 * 
 * <p>
 * Tracks the subscription through its complete lifecycle from trial
 * to expiration or cancellation. Used for access control decisions
 * and billing automation.
 * </p>
 * 
 * <h3>Status Flow:</h3>
 * 
 * <pre>
 * TRIAL ──────► ACTIVE ──────► EXPIRED
 *    │             │
 *    │             ▼
 *    │          PAST_DUE ────► CANCELLED
 *    │             │
 *    ▼             ▼
 * CANCELLED      PAUSED ──────► ACTIVE
 * </pre>
 * 
 * <h3>Naming Convention:</h3>
 * <p>
 * Prefixed with {@code UserSubscription} per PROJECT_PRINCIPLES.md §6.1
 * Feature-Contextual Naming to clearly indicate entity ownership.
 * </p>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
public enum UserSubscriptionStatus {

    /**
     * Trial period - user is trying the service.
     * Full access typically granted during trial.
     * Transitions to ACTIVE (payment) or CANCELLED (no conversion).
     */
    TRIAL("Trial", true, "User is in trial period"),

    /**
     * Active subscription - user is paying and has full access.
     * This is the normal state for a healthy subscription.
     */
    ACTIVE("Active", true, "Subscription is active and paid"),

    /**
     * Past due - payment failed but grace period is active.
     * User retains access for grace period (typically 3-7 days).
     * Transitions to ACTIVE (payment success) or CANCELLED (payment fails).
     */
    PAST_DUE("Past Due", true, "Payment failed, in grace period"),

    /**
     * Paused - user requested temporary pause.
     * No charges during pause, no access (or limited access).
     * Can resume to ACTIVE.
     */
    PAUSED("Paused", false, "Subscription temporarily paused"),

    /**
     * Cancelled - subscription terminated.
     * No access, no future charges. May retain access until period end.
     */
    CANCELLED("Cancelled", false, "Subscription cancelled"),

    /**
     * Expired - subscription period ended without renewal.
     * No access, no future charges. Different from cancelled (natural end).
     */
    EXPIRED("Expired", false, "Subscription expired");

    private final String displayName;
    private final boolean grantsAccess;
    private final String description;

    UserSubscriptionStatus(String displayName, boolean grantsAccess, String description) {
        this.displayName = displayName;
        this.grantsAccess = grantsAccess;
        this.description = description;
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
     * Checks if this status grants content access.
     * TRIAL, ACTIVE, and PAST_DUE grant access.
     *
     * @return true if user should have access
     */
    public boolean grantsAccess() {
        return grantsAccess;
    }

    /**
     * Returns the status description.
     *
     * @return description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Checks if this status is billable (charges can occur).
     *
     * @return true if billable
     */
    public boolean isBillable() {
        return this == ACTIVE || this == PAST_DUE;
    }

    /**
     * Checks if this status is terminal (no further transitions expected).
     *
     * @return true if terminal
     */
    public boolean isTerminal() {
        return this == CANCELLED || this == EXPIRED;
    }
}
