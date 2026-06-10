package com.mmva.newsapp.infrastructure.monetization.billing.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Basic Invoice DTO for general use.
 */
@Data
@Builder
public class InvoiceDto {

    private UUID invoiceId;
    private String invoiceNumber;
    private String clientId;
    private String clientName;
    private String clientEmail;
    private BigDecimal totalAmount;
    private BigDecimal amountPaid;
    private BigDecimal balance;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private String status;
    private String tenantId;
}