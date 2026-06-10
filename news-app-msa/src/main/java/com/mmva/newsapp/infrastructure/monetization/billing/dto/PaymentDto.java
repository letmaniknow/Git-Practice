package com.mmva.newsapp.infrastructure.monetization.billing.dto;

import com.mmva.newsapp.infrastructure.monetization.billing.enums.Currency;
import com.mmva.newsapp.infrastructure.monetization.billing.enums.PaymentMethod;
import com.mmva.newsapp.infrastructure.monetization.billing.enums.TransactionType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Payment Response DTO
 */
@Data
@Builder
public class PaymentDto {

    private UUID paymentId;
    private UUID invoiceId;
    private String clientId;
    private BigDecimal amount;
    private Currency currency;
    private PaymentMethod paymentMethod;
    private TransactionType transactionType;

    private String gatewayTransactionId;
    private String gatewayStatus;
    private String gatewayResponseCode;
    private String gatewayResponseMessage;

    private String cardLastFour;
    private String cardType;
    private String bankReferenceNumber;

    private BigDecimal processingFee;
    private BigDecimal netAmount;
    private BigDecimal refundAmount;
    private String refundReason;
    private LocalDateTime refundDate;

    private String failureReason;
    private String payerIpAddress;
    private String notes;

    private String tenantId;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
}