package com.mmva.newsapp.infrastructure.monetization.billing.enums;

/**
 * Transaction Type Enum - Types of billing transactions.
 */
public enum TransactionType {
    SUBSCRIPTION_PAYMENT("Subscription Payment"),
    CAMPAIGN_PAYMENT("Campaign Payment"),
    AD_PLACEMENT_PAYMENT("Ad Placement Payment"),
    REFUND("Refund"),
    ADJUSTMENT("Adjustment");

    private final String displayName;

    TransactionType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}