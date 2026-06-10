package com.mmva.newsapp.infrastructure.monetization.billing.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Invoice Line Item DTOs
 */
@Data
@Builder
public class InvoiceLineItemDto {

    private UUID lineItemId;
    private UUID invoiceId;
    private Integer lineNumber;
    private String description;

    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;
    private BigDecimal discountAmount;
    private String discountDescription;
    private BigDecimal taxRate;
    private BigDecimal taxAmount;
    private BigDecimal finalAmount;

    private String referenceId;
    private String productCode;
    private String sku;
    private String tenantId;
    private LocalDateTime createdAt;
}