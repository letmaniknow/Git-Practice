package com.mmva.newsapp.infrastructure.monetization.billing.dto;

import com.mmva.newsapp.infrastructure.monetization.billing.enums.Currency;
import com.mmva.newsapp.infrastructure.monetization.billing.enums.InvoiceStatus;
import com.mmva.newsapp.infrastructure.monetization.billing.enums.PaymentMethod;
import com.mmva.newsapp.infrastructure.monetization.billing.enums.TransactionType;
import com.mmva.newsapp.infrastructure.monetization.billing.model.Address;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Invoice Response DTO - Complete invoice information for API responses.
 */
@Data
@Builder
public class InvoiceResponseDto {

    private UUID invoiceId;
    private String invoiceNumber;
    private String clientId;
    private String clientName;
    private String clientEmail;

    private TransactionType transactionType;
    private InvoiceStatus status;
    private Currency currency;

    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private String discountDescription;
    private BigDecimal taxRate;
    private BigDecimal taxAmount;
    private BigDecimal shippingAmount;
    private BigDecimal totalAmount;
    private BigDecimal amountPaid;
    private BigDecimal balance;

    private Address billingAddress;
    private Address shippingAddress;

    private String poNumber;
    private String paymentTerms;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private PaymentMethod paymentMethod;
    private LocalDate paymentDate;

    private String notes;
    private String termsAndConditions;
    private String footerText;
    private String receiptUrl;

    private String tenantId;
    private String referenceId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Related data
    private List<InvoiceLineItemDto> lineItems;
    private List<PaymentDto> payments;

    // Computed fields
    private boolean isPaid;
    private boolean isOverdue;
    private long daysOverdue;
}