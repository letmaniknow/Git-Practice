package com.mmva.newsapp.infrastructure.monetization.billing.dto;

import lombok.Builder;
import lombok.Data;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/**
 * Invoice Line Item Create Request DTO - For creating invoice line items.
 */
@Data
@Builder
public class InvoiceLineItemCreateDto {

    @NotBlank(message = "Description is required")
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.01", message = "Quantity must be greater than 0")
    private BigDecimal quantity;

    @NotNull(message = "Unit price is required")
    @DecimalMin(value = "0.00", message = "Unit price cannot be negative")
    private BigDecimal unitPrice;

    @DecimalMin(value = "0.00", message = "Discount amount cannot be negative")
    private BigDecimal discountAmount;

    @DecimalMin(value = "0.00", message = "Tax rate cannot be negative")
    @DecimalMax(value = "100.00", message = "Tax rate cannot exceed 100%")
    private BigDecimal taxRate;

    @Size(max = 100, message = "Product code cannot exceed 100 characters")
    private String productCode;

    @NotBlank(message = "Tenant ID is required")
    private String tenantId;
}