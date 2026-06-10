package com.mmva.newsapp.infrastructure.monetization.billing.enums;

/**
 * Payment Method Enum - Supported payment methods for transactions.
 */
public enum PaymentMethod {
    CREDIT_CARD("Credit Card"),
    DEBIT_CARD("Debit Card"),
    PAYPAL("PayPal"),
    BANK_TRANSFER("Bank Transfer"),
    CASH("Cash"),
    CHECK("Check"),
    STRIPE("Stripe"),
    OTHER("Other");

    private final String displayName;

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}