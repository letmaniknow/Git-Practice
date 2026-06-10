package com.mmva.newsapp.infrastructure.monetization.revenue.model;

import com.mmva.newsapp.infrastructure.monetization.billing.enums.TransactionType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Represents revenue from different streams for aggregation and reporting.
 */
@Data
@Builder
public class RevenueStream {

    private LocalDate date;
    private TransactionType type;
    private String source; // "subscription", "campaign", "ads", "adprovider"
    private BigDecimal amount;
    private String currency;
    private String tenantId;
    private String clientId;
    private String referenceId; // subscription/campaign/ad ID

    // Aggregated fields
    private long transactionCount;
    private BigDecimal totalAmount;
    private BigDecimal averageAmount;
}