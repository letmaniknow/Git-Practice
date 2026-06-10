package com.mmva.newsapp.infrastructure.monetization.billing.dto;

import com.mmva.newsapp.infrastructure.monetization.billing.enums.InvoiceStatus;
import lombok.Builder;
import lombok.Data;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/**
 * Invoice Update Request DTO - For updating existing invoices.
 */
@Data
@Builder
public class InvoiceUpdateRequestDto {

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes;

    @Size(max = 100, message = "PO number cannot exceed 100 characters")
    private String poNumber;

    @Size(max = 200, message = "Payment terms cannot exceed 200 characters")
    private String paymentTerms;

    private InvoiceStatus status;

    @DecimalMin(value = "0.00", message = "Discount amount cannot be negative")
    private BigDecimal discountAmount;

    @Size(max = 255, message = "Discount description cannot exceed 255 characters")
    private String discountDescription;
}