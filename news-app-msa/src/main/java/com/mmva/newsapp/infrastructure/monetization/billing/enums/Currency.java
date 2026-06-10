package com.mmva.newsapp.infrastructure.monetization.billing.enums;

/**
 * Currency Enum - Supported currencies for billing operations.
 */
public enum Currency {
    USD("US Dollar", "$"),
    EUR("Euro", "€"),
    GBP("British Pound", "£"),
    JPY("Japanese Yen", "¥"),
    CAD("Canadian Dollar", "C$"),
    AUD("Australian Dollar", "A$"),
    CHF("Swiss Franc", "CHF"),
    CNY("Chinese Yuan", "¥"),
    INR("Indian Rupee", "₹");

    private final String displayName;
    private final String symbol;

    Currency(String displayName, String symbol) {
        this.displayName = displayName;
        this.symbol = symbol;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getSymbol() {
        return symbol;
    }
}