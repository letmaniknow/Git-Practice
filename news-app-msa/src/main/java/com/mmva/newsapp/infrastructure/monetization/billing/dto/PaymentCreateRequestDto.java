package com.mmva.newsapp.infrastructure.monetization.billing.dto;

import com.mmva.newsapp.infrastructure.monetization.billing.enums.PaymentMethod;
import lombok.Builder;
import lombok.Data;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Payment Create Request DTO - For processing payments.
 */
@Data
@Builder
public class PaymentCreateRequestDto {

    @NotNull(message = "Invoice ID is required")
    private UUID invoiceId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    @Size(max = 255, message = "Gateway transaction ID cannot exceed 255 characters")
    private String gatewayTransactionId;

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes;

    @NotBlank(message = "Tenant ID is required")
    private String tenantId;
}