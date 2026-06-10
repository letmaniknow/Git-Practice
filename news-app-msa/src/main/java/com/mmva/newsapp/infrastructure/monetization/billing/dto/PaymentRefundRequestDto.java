package com.mmva.newsapp.infrastructure.monetization.billing.dto;

import lombok.Builder;
import lombok.Data;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Payment Refund Request DTO - For processing refunds.
 */
@Data
@Builder
public class PaymentRefundRequestDto {

    @NotNull(message = "Payment ID is required")
    private UUID paymentId;

    @NotNull(message = "Refund amount is required")
    @DecimalMin(value = "0.01", message = "Refund amount must be greater than 0")
    private BigDecimal refundAmount;

    @Size(max = 500, message = "Reason cannot exceed 500 characters")
    private String reason;

    @Size(max = 255, message = "Gateway refund ID cannot exceed 255 characters")
    private String gatewayRefundId;

    @NotBlank(message = "Tenant ID is required")
    private String tenantId;
}