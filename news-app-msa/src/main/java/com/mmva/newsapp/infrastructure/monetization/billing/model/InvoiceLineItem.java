package com.mmva.newsapp.infrastructure.monetization.billing.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Invoice line item - Detailed breakdown of what is being billed.
 * Each invoice can have multiple line items.
 */
@Entity
@Table(name = "billing_invoice_line_items", indexes = {
        @Index(name = "idx_line_item_invoice", columnList = "invoiceId"),
        @Index(name = "idx_line_item_tenant", columnList = "tenantId")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceLineItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "line_item_id")
    private UUID lineItemId;

    /**
     * Reference to the parent invoice
     */
    @Column(name = "invoice_id", nullable = false)
    private UUID invoiceId;

    /**
     * Line item number (1, 2, 3...) within the invoice
     */
    @Column(name = "line_number", nullable = false)
    private Integer lineNumber;

    /**
     * Description of the item being billed
     */
    @Column(name = "description", length = 500, nullable = false)
    private String description;

    /**
     * Quantity of items
     */
    @Column(name = "quantity", precision = 10, scale = 2, nullable = false)
    private BigDecimal quantity;

    /**
     * Unit price
     */
    @Column(name = "unit_price", precision = 10, scale = 2, nullable = false)
    private BigDecimal unitPrice;

    /**
     * Line total before discount (quantity * unit_price)
     */
    @Column(name = "line_total", precision = 10, scale = 2, nullable = false)
    private BigDecimal lineTotal;

    /**
     * Discount amount for this line item
     */
    @Column(name = "discount_amount", precision = 10, scale = 2)
    private BigDecimal discountAmount;

    /**
     * Discount description
     */
    @Column(name = "discount_description", length = 255)
    private String discountDescription;

    /**
     * Tax rate for this line item (percentage)
     */
    @Column(name = "tax_rate", precision = 5, scale = 2)
    private BigDecimal taxRate;

    /**
     * Tax amount for this line item
     */
    @Column(name = "tax_amount", precision = 10, scale = 2)
    private BigDecimal taxAmount;

    /**
     * Final amount after discount and tax
     */
    @Column(name = "final_amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal finalAmount;

    /**
     * Reference to the source (subscription ID, campaign ID, etc.)
     */
    @Column(name = "reference_id")
    private String referenceId;

    /**
     * Product/service code for accounting
     */
    @Column(name = "product_code", length = 50)
    private String productCode;

    /**
     * SKU or item code
     */
    @Column(name = "sku", length = 100)
    private String sku;

    /**
     * Tenant ID
     */
    @Column(name = "tenant_id")
    private String tenantId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Soft delete flag
     */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // Business methods
    public void calculateLineTotal() {
        this.lineTotal = quantity.multiply(unitPrice);
        BigDecimal discountedTotal = lineTotal.subtract(discountAmount != null ? discountAmount : BigDecimal.ZERO);
        this.taxAmount = taxRate != null ? discountedTotal.multiply(taxRate.divide(BigDecimal.valueOf(100)))
                : BigDecimal.ZERO;
        this.finalAmount = discountedTotal.add(taxAmount);
    }

    // Additional getter method for compatibility
    public BigDecimal getTotalAmount() {
        return lineTotal != null ? lineTotal : BigDecimal.ZERO;
    }
}