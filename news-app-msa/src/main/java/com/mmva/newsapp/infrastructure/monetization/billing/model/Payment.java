package com.mmva.newsapp.infrastructure.monetization.billing.model;

import com.mmva.newsapp.infrastructure.monetization.billing.enums.Currency;
import com.mmva.newsapp.infrastructure.monetization.billing.enums.PaymentMethod;
import com.mmva.newsapp.infrastructure.monetization.billing.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Payment entity - Records individual payment transactions.
 * Links to invoices and tracks payment processing.
 */
@Entity
@Table(name = "billing_payments", indexes = {
        @Index(name = "idx_payment_invoice", columnList = "invoiceId"),
        @Index(name = "idx_payment_client", columnList = "clientId"),
        @Index(name = "idx_payment_tenant", columnList = "tenantId")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "payment_id")
    private UUID paymentId;

    /**
     * Reference to the invoice being paid
     */
    @Column(name = "invoice_id", nullable = false)
    private UUID invoiceId;

    /**
     * Client/User ID
     */
    @Column(name = "client_id", nullable = false)
    private String clientId;

    /**
     * Payment amount
     */
    @Column(name = "amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal amount;

    /**
     * Currency
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "currency", nullable = false)
    private Currency currency;

    /**
     * Payment method used
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    /**
     * Transaction type
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;

    /**
     * Payment gateway transaction ID
     */
    @Column(name = "gateway_transaction_id")
    private String gatewayTransactionId;

    /**
     * Payment status from gateway
     */
    @Column(name = "gateway_status")
    private String gatewayStatus;

    /**
     * Gateway response code
     */
    @Column(name = "gateway_response_code")
    private String gatewayResponseCode;

    /**
     * Gateway response message
     */
    @Column(name = "gateway_response_message", length = 500)
    private String gatewayResponseMessage;

    /**
     * Last 4 digits of credit card (for credit card payments)
     */
    @Column(name = "card_last_four", length = 4)
    private String cardLastFour;

    /**
     * Card type (Visa, MasterCard, etc.)
     */
    @Column(name = "card_type", length = 50)
    private String cardType;

    /**
     * Bank reference number
     */
    @Column(name = "bank_reference_number")
    private String bankReferenceNumber;

    /**
     * Processing fee charged by payment gateway
     */
    @Column(name = "processing_fee", precision = 10, scale = 2)
    private BigDecimal processingFee;

    /**
     * Net amount received (amount - processing_fee)
     */
    @Column(name = "net_amount", precision = 10, scale = 2)
    private BigDecimal netAmount;

    /**
     * Refund amount (if any)
     */
    @Column(name = "refund_amount", precision = 10, scale = 2)
    private BigDecimal refundAmount;

    /**
     * Refund reason
     */
    @Column(name = "refund_reason", length = 255)
    private String refundReason;

    /**
     * Refund date
     */
    @Column(name = "refund_date")
    private LocalDateTime refundDate;

    /**
     * Payment failure reason (if failed)
     */
    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    /**
     * IP address of the payer
     */
    @Column(name = "payer_ip_address")
    private String payerIpAddress;

    /**
     * Payment notes
     */
    @Column(name = "notes", length = 500)
    private String notes;

    /**
     * Tenant ID
     */
    @Column(name = "tenant_id")
    private String tenantId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Processed timestamp
     */
    @Column(name = "processed_at")
    private LocalDateTime processedAt;
}