package com.mmva.newsapp.infrastructure.monetization.billing.dto;

import com.mmva.newsapp.infrastructure.monetization.billing.enums.Currency;
import com.mmva.newsapp.infrastructure.monetization.billing.enums.TransactionType;
import lombok.Builder;
import lombok.Data;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Invoice Create Request DTO - For creating new invoices.
 */
@Data
@Builder
public class InvoiceCreateRequestDto {

    @NotBlank(message = "Client ID is required")
    private String clientId;

    @NotBlank(message = "Client name is required")
    private String clientName;

    @NotBlank(message = "Client email is required")
    @Email(message = "Invalid email format")
    private String clientEmail;

    @NotNull(message = "Transaction type is required")
    private TransactionType transactionType;

    @NotNull(message = "Currency is required")
    private Currency currency;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes;

    private UUID referenceId;

    @NotBlank(message = "Tenant ID is required")
    private String tenantId;
}