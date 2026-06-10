package com.mmva.newsapp.infrastructure.monetization.billing.model;

import com.mmva.newsapp.infrastructure.monetization.billing.enums.Currency;
import com.mmva.newsapp.infrastructure.monetization.billing.enums.InvoiceStatus;
import com.mmva.newsapp.infrastructure.monetization.billing.enums.PaymentMethod;
import com.mmva.newsapp.infrastructure.monetization.billing.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Invoice entity - Core billing document.
 * Represents a bill sent to clients or users for monetization services.
 */
@Entity
@Table(name = "billing_invoices", indexes = {
        @Index(name = "idx_invoice_client", columnList = "clientId"),
        @Index(name = "idx_invoice_status", columnList = "status"),
        @Index(name = "idx_invoice_due_date", columnList = "dueDate"),
        @Index(name = "idx_invoice_tenant", columnList = "tenantId")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "invoice_id")
    private UUID invoiceId;

    /**
     * Client/User ID - can be user UUID or client company ID
     */
    @Column(name = "client_id", nullable = false)
    private String clientId;

    /**
     * Client name for display purposes
     */
    @Column(name = "client_name", nullable = false)
    private String clientName;

    /**
     * Client email for invoice delivery
     */
    @Column(name = "client_email")
    private String clientEmail;

    /**
     * Invoice number - auto-generated, unique per tenant
     * Format: INV-{tenantId}-{year}-{sequential}
     */
    @Column(name = "invoice_number", unique = true, nullable = false)
    private String invoiceNumber;

    /**
     * Type of transaction this invoice represents
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;

    /**
     * Invoice status
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private InvoiceStatus status;

    /**
     * Currency for the invoice
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "currency", nullable = false)
    private Currency currency;

    /**
     * Subtotal before tax
     */
    @Column(name = "subtotal", precision = 10, scale = 2, nullable = false)
    private BigDecimal subtotal;

    /**
     * Discount amount
     */
    @Column(name = "discount_amount", precision = 10, scale = 2)
    private BigDecimal discountAmount;

    /**
     * Discount description
     */
    @Column(name = "discount_description", length = 255)
    private String discountDescription;

    /**
     * Tax rate (percentage)
     */
    @Column(name = "tax_rate", precision = 5, scale = 2)
    private BigDecimal taxRate;

    /**
     * Tax amount
     */
    @Column(name = "tax_amount", precision = 10, scale = 2)
    private BigDecimal taxAmount;

    /**
     * Shipping amount
     */
    @Column(name = "shipping_amount", precision = 10, scale = 2)
    private BigDecimal shippingAmount;

    /**
     * Total amount including tax and shipping
     */
    @Column(name = "total_amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal totalAmount;

    /**
     * Amount paid so far
     */
    @Column(name = "amount_paid", precision = 10, scale = 2)
    private BigDecimal amountPaid;

    /**
     * Outstanding balance
     */
    @Column(name = "balance", precision = 10, scale = 2, nullable = false)
    private BigDecimal balance;

    /**
     * Billing address
     */
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "streetAddress", column = @Column(name = "billing_street_address")),
            @AttributeOverride(name = "city", column = @Column(name = "billing_city")),
            @AttributeOverride(name = "stateProvince", column = @Column(name = "billing_state_province")),
            @AttributeOverride(name = "postalCode", column = @Column(name = "billing_postal_code")),
            @AttributeOverride(name = "country", column = @Column(name = "billing_country")),
            @AttributeOverride(name = "countryCode", column = @Column(name = "billing_country_code"))
    })
    private Address billingAddress;

    /**
     * Shipping address (if different from billing)
     */
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "streetAddress", column = @Column(name = "shipping_street_address")),
            @AttributeOverride(name = "city", column = @Column(name = "shipping_city")),
            @AttributeOverride(name = "stateProvince", column = @Column(name = "shipping_state_province")),
            @AttributeOverride(name = "postalCode", column = @Column(name = "shipping_postal_code")),
            @AttributeOverride(name = "country", column = @Column(name = "shipping_country")),
            @AttributeOverride(name = "countryCode", column = @Column(name = "shipping_country_code"))
    })
    private Address shippingAddress;

    /**
     * Invoice line items
     */
    @OneToMany(mappedBy = "invoiceId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<InvoiceLineItem> lineItems = new ArrayList<>();

    /**
     * Purchase Order number
     */
    @Column(name = "po_number", length = 100)
    private String poNumber;

    /**
     * Payment terms (e.g., "Net 30", "Due on receipt")
     */
    @Column(name = "payment_terms", length = 100)
    private String paymentTerms;

    /**
     * Invoice issue date
     */
    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    /**
     * Payment due date
     */
    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    /**
     * Payment method used (if paid)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    private PaymentMethod paymentMethod;

    /**
     * Payment date (if paid)
     */
    @Column(name = "payment_date")
    private LocalDate paymentDate;

    /**
     * Invoice notes/description
     */
    @Column(name = "notes", length = 1000)
    private String notes;

    /**
     * Terms and conditions
     */
    @Column(name = "terms_and_conditions", length = 2000)
    private String termsAndConditions;

    /**
     * Footer text for invoice
     */
    @Column(name = "footer_text", length = 500)
    private String footerText;

    /**
     * PDF receipt URL (generated after payment)
     */
    @Column(name = "receipt_url")
    private String receiptUrl;

    /**
     * Tenant ID for multi-tenant support
     */
    @Column(name = "tenant_id")
    private String tenantId;

    /**
     * Reference to related entity (subscription ID, campaign ID, etc.)
     */
    @Column(name = "reference_id")
    private String referenceId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Soft delete flag
     */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // Business methods
    public boolean isPaid() {
        return status == InvoiceStatus.PAID;
    }

    public boolean isOverdue() {
        return status == InvoiceStatus.OVERDUE ||
                (status != InvoiceStatus.PAID && LocalDate.now().isAfter(dueDate));
    }

    public void markAsPaid(BigDecimal paidAmount, PaymentMethod method) {
        this.amountPaid = paidAmount;
        this.balance = totalAmount.subtract(paidAmount);
        this.paymentMethod = method;
        this.paymentDate = LocalDate.now();
        this.status = balance.compareTo(BigDecimal.ZERO) == 0 ? InvoiceStatus.PAID : InvoiceStatus.SENT;
    }

    // Additional getter methods for compatibility
    public LocalDate getInvoiceDate() {
        return issueDate;
    }

    public LocalDate getPaidDate() {
        return paymentDate;
    }

    public void setPaidDate(LocalDateTime paidDate) {
        this.paymentDate = paidDate != null ? paidDate.toLocalDate() : null;
    }

    public UUID getId() {
        return invoiceId;
    }
}