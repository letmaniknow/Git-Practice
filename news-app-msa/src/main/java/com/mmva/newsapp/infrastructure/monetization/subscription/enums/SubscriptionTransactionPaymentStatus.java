package com.mmva.newsapp.infrastructure.monetization.subscription.enums;

/**
 * Enum representing the status of a subscription transaction payment.
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
public enum SubscriptionTransactionPaymentStatus {

    /**
     * Transaction initiated but not yet processed.
     */
    PENDING("Pending", false, false),

    /**
     * Payment processing in progress.
     */
    PROCESSING("Processing", false, false),

    /**
     * Payment successfully completed.
     */
    SUCCEEDED("Succeeded", true, false),

    /**
     * Payment failed (card declined, insufficient funds, etc.).
     */
    FAILED("Failed", false, true),

    /**
     * Payment cancelled before processing.
     */
    CANCELLED("Cancelled", false, true),

    /**
     * Payment refunded fully.
     */
    REFUNDED("Refunded", false, true),

    /**
     * Payment partially refunded.
     */
    PARTIALLY_REFUNDED("Partially Refunded", true, false),

    /**
     * Customer disputed the charge (chargeback).
     */
    DISPUTED("Disputed", false, false),

    /**
     * Requires additional authentication (3D Secure, etc.).
     */
    REQUIRES_ACTION("Requires Action", false, false);

    private final String displayName;
    private final boolean isSuccessful;
    private final boolean isTerminal;

    SubscriptionTransactionPaymentStatus(String displayName, boolean isSuccessful, boolean isTerminal) {
        this.displayName = displayName;
        this.isSuccessful = isSuccessful;
        this.isTerminal = isTerminal;
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
     * Checks if this status represents a successful payment.
     *
     * @return true if payment succeeded
     */
    public boolean isSuccessful() {
        return isSuccessful;
    }

    /**
     * Checks if this is a terminal/final status.
     *
     * @return true if no further processing expected
     */
    public boolean isTerminal() {
        return isTerminal || isSuccessful;
    }

    /**
     * Checks if payment can be retried.
     *
     * @return true if retry is possible
     */
    public boolean canRetry() {
        return this == FAILED || this == CANCELLED;
    }

    /**
     * Checks if payment is still in progress.
     *
     * @return true if pending or processing
     */
    public boolean isInProgress() {
        return this == PENDING || this == PROCESSING || this == REQUIRES_ACTION;
    }
}
