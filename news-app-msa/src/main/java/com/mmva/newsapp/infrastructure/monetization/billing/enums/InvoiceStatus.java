package com.mmva.newsapp.infrastructure.monetization.billing.enums;

/**
 * Invoice Status Enum - Represents the lifecycle states of an invoice.
 */
public enum InvoiceStatus {
    DRAFT("Draft"),
    SENT("Sent"),
    PARTIALLY_PAID("Partially Paid"),
    PAID("Paid"),
    OVERDUE("Overdue"),
    CANCELLED("Cancelled"),
    REFUNDED("Refunded");

    private final String displayName;

    InvoiceStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}