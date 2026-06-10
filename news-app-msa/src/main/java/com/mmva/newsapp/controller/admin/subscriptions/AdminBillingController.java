package com.mmva.newsapp.controller.admin.subscriptions;

import com.mmva.newsapp.infrastructure.common.api.dto.ApiResponseDto;
import com.mmva.newsapp.infrastructure.monetization.billing.enums.Currency;
import com.mmva.newsapp.infrastructure.monetization.billing.enums.PaymentMethod;
import com.mmva.newsapp.infrastructure.monetization.billing.model.Invoice;
import com.mmva.newsapp.infrastructure.monetization.billing.model.Payment;
import com.mmva.newsapp.infrastructure.monetization.billing.service.BillingService;
import com.mmva.newsapp.infrastructure.monetization.billing.service.InvoiceService;
import com.mmva.newsapp.infrastructure.monetization.billing.service.PaymentService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Admin Billing Controller - Manage invoices, payments, and billing operations.
 * Handles both direct monetization billing and integrates with revenue
 * tracking.
 */
@RestController
@RequestMapping("/api/v1/admin/billing")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
public class AdminBillingController {

    private final BillingService billingService;
    private final InvoiceService invoiceService;
    private final PaymentService paymentService;

    /**
     * Get all invoices with pagination
     */
    @GetMapping("/invoices")
    public ResponseEntity<ApiResponseDto<Page<Invoice>>> getInvoices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String tenantId) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Invoice> invoices = invoiceService.findByTenantId(tenantId, pageable);

        return ResponseEntity.ok(ApiResponseDto.success("Invoices retrieved", invoices));
    }

    /**
     * Get invoice by ID
     */
    @GetMapping("/invoices/{invoiceId}")
    public ResponseEntity<ApiResponseDto<Invoice>> getInvoice(@PathVariable UUID invoiceId) {
        Invoice invoice = invoiceService.findById(invoiceId);
        if (invoice == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(ApiResponseDto.success("Invoice retrieved", invoice));
    }

    /**
     * Create subscription invoice
     */
    @PostMapping("/invoices/subscription")
    public ResponseEntity<ApiResponseDto<Invoice>> createSubscriptionInvoice(
            @RequestBody CreateSubscriptionInvoiceRequest request) {

        Invoice invoice = billingService.createSubscriptionInvoice(
                request.getUserId(),
                request.getUserName(),
                request.getUserEmail(),
                request.getAmount(),
                request.getCurrency(),
                request.getTenantId(),
                request.getSubscriptionId());

        return ResponseEntity.ok(ApiResponseDto.success("Subscription invoice created", invoice));
    }

    /**
     * Create campaign invoice
     */
    @PostMapping("/invoices/campaign")
    public ResponseEntity<ApiResponseDto<Invoice>> createCampaignInvoice(
            @RequestBody CreateCampaignInvoiceRequest request) {

        Invoice invoice = billingService.createCampaignInvoice(
                request.getClientId(),
                request.getClientName(),
                request.getClientEmail(),
                request.getAmount(),
                request.getCurrency(),
                request.getTenantId(),
                request.getCampaignId(),
                request.getNotes());

        return ResponseEntity.ok(ApiResponseDto.success("Campaign invoice created", invoice));
    }

    /**
     * Create ad placement invoice
     */
    @PostMapping("/invoices/ad-placement")
    public ResponseEntity<ApiResponseDto<Invoice>> createAdPlacementInvoice(
            @RequestBody CreateAdPlacementInvoiceRequest request) {

        Invoice invoice = billingService.createAdPlacementInvoice(
                request.getClientId(),
                request.getClientName(),
                request.getClientEmail(),
                request.getAmount(),
                request.getCurrency(),
                request.getTenantId(),
                request.getPlacementId(),
                request.getNotes());

        return ResponseEntity.ok(ApiResponseDto.success("Ad placement invoice created", invoice));
    }

    /**
     * Send invoice to client
     */
    @PostMapping("/invoices/{invoiceId}/send")
    public ResponseEntity<ApiResponseDto<Invoice>> sendInvoice(@PathVariable UUID invoiceId) {
        Invoice invoice = billingService.sendInvoice(invoiceId);
        return ResponseEntity.ok(ApiResponseDto.success("Invoice sent", invoice));
    }

    /**
     * Process payment for invoice
     */
    @PostMapping("/payments")
    public ResponseEntity<ApiResponseDto<Payment>> processPayment(
            @RequestBody ProcessPaymentRequest request) {

        Payment payment = billingService.processPayment(
                request.getInvoiceId(),
                request.getAmount(),
                request.getPaymentMethod(),
                request.getGatewayTransactionId(),
                request.getTenantId());

        return ResponseEntity.ok(ApiResponseDto.success("Payment processed", payment));
    }

    /**
     * Get payments for invoice
     */
    @GetMapping("/invoices/{invoiceId}/payments")
    public ResponseEntity<ApiResponseDto<List<Payment>>> getInvoicePayments(@PathVariable UUID invoiceId) {
        List<Payment> payments = paymentService.findByInvoiceId(invoiceId);
        return ResponseEntity.ok(ApiResponseDto.success("Payments retrieved", payments));
    }

    /**
     * Get overdue invoices
     */
    @GetMapping("/invoices/overdue")
    public ResponseEntity<ApiResponseDto<List<Invoice>>> getOverdueInvoices() {
        List<Invoice> overdueInvoices = invoiceService.findOverdueInvoices();
        return ResponseEntity.ok(ApiResponseDto.success("Overdue invoices retrieved", overdueInvoices));
    }

    // Request DTOs
    @Data
    public static class CreateSubscriptionInvoiceRequest {
        private String userId;
        private String userName;
        private String userEmail;
        private BigDecimal amount;
        private Currency currency;
        private String tenantId;
        private UUID subscriptionId;
        // getters/setters
    }

    @Data
    public static class CreateCampaignInvoiceRequest {
        private String clientId;
        private String clientName;
        private String clientEmail;
        private BigDecimal amount;
        private Currency currency;
        private String tenantId;
        private UUID campaignId;
        private String notes;
        // getters/setters
    }

    @Data
    public static class CreateAdPlacementInvoiceRequest {
        private String clientId;
        private String clientName;
        private String clientEmail;
        private BigDecimal amount;
        private Currency currency;
        private String tenantId;
        private String placementId;
        private String notes;
        // getters/setters
    }

    @Data
    public static class ProcessPaymentRequest {
        private UUID invoiceId;
        private BigDecimal amount;
        private PaymentMethod paymentMethod;
        private String gatewayTransactionId;
        private String tenantId;
        // getters/setters
    }
}