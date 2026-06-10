package com.mmva.newsapp.infrastructure.monetization.billing.service;

import com.mmva.newsapp.infrastructure.monetization.billing.enums.Currency;
import com.mmva.newsapp.infrastructure.monetization.billing.enums.InvoiceStatus;
import com.mmva.newsapp.infrastructure.monetization.billing.enums.PaymentMethod;
import com.mmva.newsapp.infrastructure.monetization.billing.enums.TransactionType;
import com.mmva.newsapp.infrastructure.monetization.billing.model.Invoice;
import com.mmva.newsapp.infrastructure.monetization.billing.model.InvoiceLineItem;
import com.mmva.newsapp.infrastructure.monetization.billing.model.Payment;
import com.mmva.newsapp.infrastructure.monetization.billing.repository.InvoiceLineItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Core billing service - Orchestrates invoice creation, payment processing, and
 * revenue tracking.
 * This is the main entry point for all billing operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BillingService {

    private final InvoiceService invoiceService;
    private final PaymentService paymentService;
    private final InvoiceLineItemRepository invoiceLineItemRepository;
    private final InvoiceNumberGenerator invoiceNumberGenerator;

    /**
     * Create invoice for user subscription
     */
    @Transactional
    public Invoice createSubscriptionInvoice(String userId, String userName, String userEmail,
            BigDecimal amount, Currency currency, String tenantId,
            UUID subscriptionId) {

        log.info("Creating subscription invoice for user: {} amount: {}", userId, amount);

        // Create line item for subscription
        InvoiceLineItem lineItem = InvoiceLineItem.builder()
                .lineNumber(1)
                .description("Monthly Subscription")
                .quantity(BigDecimal.ONE)
                .unitPrice(amount)
                .lineTotal(amount)
                .discountAmount(BigDecimal.ZERO)
                .taxRate(BigDecimal.valueOf(10.0)) // 10% tax
                .referenceId(subscriptionId.toString())
                .productCode("SUBSCRIPTION")
                .tenantId(tenantId)
                .build();

        lineItem.calculateLineTotal();

        Invoice invoice = Invoice.builder()
                .clientId(userId)
                .clientName(userName)
                .clientEmail(userEmail)
                .transactionType(TransactionType.SUBSCRIPTION_PAYMENT)
                .currency(currency)
                .subtotal(amount)
                .discountAmount(BigDecimal.ZERO)
                .taxRate(BigDecimal.valueOf(10.0))
                .taxAmount(calculateTax(amount, BigDecimal.valueOf(10.0)))
                .shippingAmount(BigDecimal.ZERO)
                .totalAmount(amount.add(calculateTax(amount, BigDecimal.valueOf(10.0))))
                .balance(amount.add(calculateTax(amount, BigDecimal.valueOf(10.0))))
                .amountPaid(BigDecimal.ZERO)
                .issueDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(30)) // 30 days payment terms
                .status(InvoiceStatus.DRAFT)
                .paymentTerms("Net 30")
                .tenantId(tenantId)
                .referenceId(subscriptionId.toString())
                .build();

        // Generate invoice number
        invoice.setInvoiceNumber(invoiceNumberGenerator.generate(tenantId));

        Invoice savedInvoice = invoiceService.save(invoice);

        // Save line item with invoice ID
        lineItem.setInvoiceId(savedInvoice.getInvoiceId());
        invoiceLineItemRepository.save(lineItem);

        return savedInvoice;
    }

    /**
     * Create invoice for client campaign
     */
    @Transactional
    public Invoice createCampaignInvoice(String clientId, String clientName, String clientEmail,
            BigDecimal amount, Currency currency, String tenantId,
            UUID campaignId, String notes) {

        log.info("Creating campaign invoice for client: {} amount: {}", clientId, amount);

        // Create line item for campaign
        InvoiceLineItem lineItem = InvoiceLineItem.builder()
                .lineNumber(1)
                .description("Advertising Campaign Services")
                .quantity(BigDecimal.ONE)
                .unitPrice(amount)
                .lineTotal(amount)
                .discountAmount(BigDecimal.ZERO)
                .taxRate(BigDecimal.valueOf(10.0))
                .referenceId(campaignId.toString())
                .productCode("CAMPAIGN")
                .tenantId(tenantId)
                .build();

        lineItem.calculateLineTotal();

        Invoice invoice = Invoice.builder()
                .clientId(clientId)
                .clientName(clientName)
                .clientEmail(clientEmail)
                .transactionType(TransactionType.CAMPAIGN_PAYMENT)
                .currency(currency)
                .subtotal(amount)
                .discountAmount(BigDecimal.ZERO)
                .taxRate(BigDecimal.valueOf(10.0))
                .taxAmount(calculateTax(amount, BigDecimal.valueOf(10.0)))
                .shippingAmount(BigDecimal.ZERO)
                .totalAmount(amount.add(calculateTax(amount, BigDecimal.valueOf(10.0))))
                .balance(amount.add(calculateTax(amount, BigDecimal.valueOf(10.0))))
                .amountPaid(BigDecimal.ZERO)
                .issueDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(15)) // 15 days for campaigns
                .status(InvoiceStatus.DRAFT)
                .paymentTerms("Net 15")
                .notes(notes)
                .tenantId(tenantId)
                .referenceId(campaignId.toString())
                .build();

        invoice.setInvoiceNumber(invoiceNumberGenerator.generate(tenantId));

        Invoice savedInvoice = invoiceService.save(invoice);

        // Save line item
        lineItem.setInvoiceId(savedInvoice.getInvoiceId());
        invoiceLineItemRepository.save(lineItem);

        return savedInvoice;
    }

    /**
     * Create invoice for local business ad placement
     */
    @Transactional
    public Invoice createAdPlacementInvoice(String clientId, String clientName, String clientEmail,
            BigDecimal amount, Currency currency, String tenantId,
            String placementId, String notes) {

        log.info("Creating ad placement invoice for client: {} amount: {}", clientId, amount);

        // Create line item for ad placement
        InvoiceLineItem lineItem = InvoiceLineItem.builder()
                .lineNumber(1)
                .description("Display Advertising Placement")
                .quantity(BigDecimal.ONE)
                .unitPrice(amount)
                .lineTotal(amount)
                .discountAmount(BigDecimal.ZERO)
                .taxRate(BigDecimal.valueOf(10.0))
                .referenceId(placementId)
                .productCode("AD_PLACEMENT")
                .tenantId(tenantId)
                .build();

        lineItem.calculateLineTotal();

        Invoice invoice = Invoice.builder()
                .clientId(clientId)
                .clientName(clientName)
                .clientEmail(clientEmail)
                .transactionType(TransactionType.AD_PLACEMENT_PAYMENT)
                .currency(currency)
                .subtotal(amount)
                .discountAmount(BigDecimal.ZERO)
                .taxRate(BigDecimal.valueOf(10.0))
                .taxAmount(calculateTax(amount, BigDecimal.valueOf(10.0)))
                .shippingAmount(BigDecimal.ZERO)
                .totalAmount(amount.add(calculateTax(amount, BigDecimal.valueOf(10.0))))
                .balance(amount.add(calculateTax(amount, BigDecimal.valueOf(10.0))))
                .amountPaid(BigDecimal.ZERO)
                .issueDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(7)) // 7 days for ad placements
                .status(InvoiceStatus.DRAFT)
                .paymentTerms("Net 7")
                .notes(notes)
                .tenantId(tenantId)
                .referenceId(placementId)
                .build();

        invoice.setInvoiceNumber(invoiceNumberGenerator.generate(tenantId));

        Invoice savedInvoice = invoiceService.save(invoice);

        // Save line item
        lineItem.setInvoiceId(savedInvoice.getInvoiceId());
        invoiceLineItemRepository.save(lineItem);

        return savedInvoice;
    }

    /**
     * Process payment for an invoice
     */
    @Transactional
    public Payment processPayment(UUID invoiceId, BigDecimal amount, PaymentMethod method,
            String gatewayTransactionId, String tenantId) {

        log.info("Processing payment for invoice: {} amount: {}", invoiceId, amount);

        Invoice invoice = invoiceService.findById(invoiceId);
        if (invoice == null) {
            throw new IllegalArgumentException("Invoice not found: " + invoiceId);
        }

        // Create payment record
        Payment payment = Payment.builder()
                .invoiceId(invoiceId)
                .clientId(invoice.getClientId())
                .amount(amount)
                .currency(invoice.getCurrency())
                .paymentMethod(method)
                .transactionType(invoice.getTransactionType())
                .gatewayTransactionId(gatewayTransactionId)
                .gatewayStatus("COMPLETED")
                .processingFee(calculateProcessingFee(amount, method))
                .netAmount(amount.subtract(calculateProcessingFee(amount, method)))
                .tenantId(tenantId)
                .processedAt(java.time.LocalDateTime.now())
                .build();

        paymentService.save(payment);

        // Update invoice
        invoice.markAsPaid(amount, method);
        invoiceService.save(invoice);

        return payment;
    }

    /**
     * Send invoice to client (mark as SENT)
     */
    @Transactional
    public Invoice sendInvoice(UUID invoiceId) {
        Invoice invoice = invoiceService.findById(invoiceId);
        if (invoice.getStatus() == InvoiceStatus.DRAFT) {
            invoice.setStatus(InvoiceStatus.SENT);
            return invoiceService.save(invoice);
        }
        return invoice;
    }

    /**
     * Calculate tax based on amount and rate
     */
    private BigDecimal calculateTax(BigDecimal amount, BigDecimal taxRate) {
        return amount.multiply(taxRate.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
    }

    /**
     * Calculate processing fee based on payment method
     */
    private BigDecimal calculateProcessingFee(BigDecimal amount, PaymentMethod method) {
        // Simplified processing fees - customize based on your payment provider
        switch (method) {
            case CREDIT_CARD:
                return amount.multiply(BigDecimal.valueOf(0.029)).add(BigDecimal.valueOf(0.30));
            case PAYPAL:
                return amount.multiply(BigDecimal.valueOf(0.024)).add(BigDecimal.valueOf(0.49));
            default:
                return BigDecimal.ZERO;
        }
    }

}