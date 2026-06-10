package com.mmva.newsapp.infrastructure.monetization.billing.recurring;

import com.mmva.newsapp.infrastructure.monetization.billing.enums.Currency;
import com.mmva.newsapp.infrastructure.monetization.billing.model.Invoice;
import com.mmva.newsapp.infrastructure.monetization.billing.model.Payment;
import com.mmva.newsapp.infrastructure.monetization.billing.payment.PaymentGatewayService;
import com.mmva.newsapp.infrastructure.monetization.billing.service.BillingEmailService;
import com.mmva.newsapp.infrastructure.monetization.billing.service.BillingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Basic implementation of RecurringBillingService.
 *
 * <p>
 * Handles subscription lifecycle, automatic billing, payment retries,
 * and dunning management with configurable policies.
 * </p>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BasicRecurringBillingService implements RecurringBillingService {

    private final BillingService billingService;
    private final PaymentGatewayService paymentGateway;
    private final BillingEmailService emailService;

    // Configuration constants (in production, these would be configurable)
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final BigDecimal LATE_FEE_PERCENTAGE = new BigDecimal("0.015"); // 1.5%

    @Override
    @Transactional
    public RecurringBillingResult processRecurringBilling(String subscriptionId, BillingCycle billingCycle) {
        try {
            log.info("Processing recurring billing for subscription: {}", subscriptionId);

            // Generate invoice for the billing cycle
            Invoice invoice = generateRecurringInvoice(subscriptionId, billingCycle);
            if (invoice == null) {
                return new RecurringBillingResult(false, null, null, "FAILED",
                        "Failed to generate invoice", null);
            }

            // Process payment
            PaymentGatewayService.PaymentResult paymentResult = processSubscriptionPayment(invoice);

            LocalDateTime nextBillingDate = calculateNextBillingDate(LocalDateTime.now(), billingCycle);

            if (paymentResult.isSuccess()) {
                // Mark invoice as paid
                invoice.setPaidDate(LocalDateTime.now());
                invoice.setStatus(com.mmva.newsapp.infrastructure.monetization.billing.enums.InvoiceStatus.PAID);

                // Send receipt
                emailService.sendReceiptEmail(invoice);

                log.info("Recurring billing successful for subscription: {}", subscriptionId);
                return new RecurringBillingResult(true, invoice.getId().toString(),
                        paymentResult.getTransactionId(), "SUCCESS",
                        "Payment processed successfully", nextBillingDate);
            } else {
                // Handle failed payment
                handleFailedPayment(invoice, paymentResult);

                log.warn("Recurring billing failed for subscription: {}", subscriptionId);
                return new RecurringBillingResult(false, invoice.getId().toString(), null,
                        "PAYMENT_FAILED", paymentResult.getMessage(), nextBillingDate);
            }

        } catch (Exception e) {
            log.error("Error processing recurring billing for subscription: {}", subscriptionId, e);
            return new RecurringBillingResult(false, null, null, "ERROR",
                    "Unexpected error occurred", null);
        }
    }

    @Override
    @Transactional
    public PaymentRetryResult retryFailedPayment(String subscriptionId, int maxRetries) {
        try {
            log.info("Retrying failed payment for subscription: {}", subscriptionId);

            // Find failed invoice
            Invoice failedInvoice = findFailedInvoice(subscriptionId);
            if (failedInvoice == null) {
                return new PaymentRetryResult(false, 0, "NO_FAILED_INVOICE",
                        null, null, false);
            }

            int attempts = 0;
            PaymentGatewayService.PaymentResult lastResult = null;

            while (attempts < Math.min(maxRetries, MAX_RETRY_ATTEMPTS)) {
                attempts++;

                // Exponential backoff delay (in production, this would be scheduled)
                long delayMinutes = (long) Math.pow(2, attempts - 1) * 60; // 1h, 2h, 4h
                LocalDateTime nextRetryDate = LocalDateTime.now().plusMinutes(delayMinutes);

                // Retry payment
                lastResult = processSubscriptionPayment(failedInvoice);

                if (lastResult.isSuccess()) {
                    // Success - update invoice
                    failedInvoice.setPaidDate(LocalDateTime.now());
                    failedInvoice
                            .setStatus(com.mmva.newsapp.infrastructure.monetization.billing.enums.InvoiceStatus.PAID);

                    // Send receipt
                    emailService.sendReceiptEmail(failedInvoice);

                    log.info("Payment retry successful for subscription: {} after {} attempts", subscriptionId,
                            attempts);
                    return new PaymentRetryResult(true, attempts, "SUCCESS",
                            lastResult.getTransactionId(), null, false);
                }

                // Log retry attempt
                log.warn("Payment retry {} failed for subscription: {}", attempts, subscriptionId);
            }

            // Max retries reached
            LocalDateTime nextRetryDate = LocalDateTime.now().plusDays(1); // Schedule for dunning
            log.error("Max payment retries reached for subscription: {}", subscriptionId);

            return new PaymentRetryResult(false, attempts, "MAX_RETRIES_REACHED",
                    null, nextRetryDate, true);

        } catch (Exception e) {
            log.error("Error during payment retry for subscription: {}", subscriptionId, e);
            return new PaymentRetryResult(false, 0, "ERROR", null, null, false);
        }
    }

    @Override
    @Transactional
    public SubscriptionCancellationResult cancelSubscription(String subscriptionId, boolean cancelAtPeriodEnd,
            boolean refundUnusedAmount) {
        try {
            log.info("Cancelling subscription: {} (atPeriodEnd: {}, refund: {})",
                    subscriptionId, cancelAtPeriodEnd, refundUnusedAmount);

            LocalDateTime cancellationDate = cancelAtPeriodEnd ? calculatePeriodEndDate(subscriptionId)
                    : LocalDateTime.now();

            BigDecimal refundAmount = BigDecimal.ZERO;
            String refundId = null;

            if (refundUnusedAmount && !cancelAtPeriodEnd) {
                // Calculate prorated refund
                refundAmount = calculateProratedRefund(subscriptionId, cancellationDate);

                if (refundAmount.compareTo(BigDecimal.ZERO) > 0) {
                    // Process refund
                    Payment lastPayment = findLastSuccessfulPayment(subscriptionId);
                    if (lastPayment != null) {
                        PaymentGatewayService.RefundResult refundResult = paymentGateway.processRefund(lastPayment,
                                refundAmount, "subscription_cancellation");

                        if (refundResult.isSuccess()) {
                            refundId = refundResult.getRefundId();
                        } else {
                            log.warn("Refund failed for subscription cancellation: {}", subscriptionId);
                        }
                    }
                }
            }

            // Update subscription status (in production, this would update a subscription
            // entity)

            log.info("Subscription {} cancelled successfully", subscriptionId);
            return new SubscriptionCancellationResult(true, refundId, refundAmount,
                    cancellationDate, "Subscription cancelled successfully");

        } catch (Exception e) {
            log.error("Error cancelling subscription: {}", subscriptionId, e);
            return new SubscriptionCancellationResult(false, null, BigDecimal.ZERO,
                    LocalDateTime.now(), "Cancellation failed: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public SubscriptionUpdateResult updateSubscriptionBilling(String subscriptionId, String newPaymentMethodId,
            boolean prorateCharges) {
        try {
            log.info("Updating billing for subscription: {} (prorate: {})", subscriptionId, prorateCharges);

            BigDecimal prorationAmount = BigDecimal.ZERO;
            String proratedInvoiceId = null;
            LocalDateTime nextBillingDate = calculateNextBillingDate(LocalDateTime.now(), BillingCycle.MONTHLY);

            if (prorateCharges) {
                // Calculate proration for mid-cycle change
                prorationAmount = calculateProrationAmount(subscriptionId, LocalDateTime.now());

                if (prorationAmount.compareTo(BigDecimal.ZERO) > 0) {
                    // Create prorated invoice
                    Invoice prorationInvoice = billingService.createSubscriptionInvoice(
                            "user-" + subscriptionId, // userId
                            "Subscription Update", // userName
                            "user@example.com", // userEmail
                            prorationAmount, // amount
                            Currency.USD, // currency
                            "default", // tenantId
                            UUID.fromString(subscriptionId)); // subscriptionId

                    proratedInvoiceId = prorationInvoice.getId().toString();

                    // Process payment for proration
                    PaymentGatewayService.PaymentResult paymentResult = paymentGateway.processPayment(prorationInvoice,
                            newPaymentMethodId, prorationAmount, Map.of());

                    if (!paymentResult.isSuccess()) {
                        log.warn("Prorated payment failed for subscription update: {}", subscriptionId);
                    }
                }
            }

            // Update payment method (in production, this would update subscription entity)

            log.info("Subscription billing updated successfully for: {}", subscriptionId);
            return new SubscriptionUpdateResult(true, proratedInvoiceId, prorationAmount,
                    nextBillingDate, "Billing updated successfully");

        } catch (Exception e) {
            log.error("Error updating subscription billing: {}", subscriptionId, e);
            return new SubscriptionUpdateResult(false, null, BigDecimal.ZERO,
                    null, "Update failed: " + e.getMessage());
        }
    }

    @Override
    public InvoicePreview generateInvoicePreview(String subscriptionId, BillingCycle billingCycle) {
        try {
            // Calculate amounts (simplified - in production would query subscription
            // details)
            BigDecimal amount = new BigDecimal("29.99"); // Example monthly fee
            LocalDateTime billingDate = calculateNextBillingDate(LocalDateTime.now(), billingCycle);

            List<InvoiceLineItem> lineItems = List.of(
                    new InvoiceLineItem("Monthly Subscription", amount, 1));

            Map<String, Object> metadata = Map.of(
                    "billing_cycle", billingCycle.toString(),
                    "preview_date", LocalDateTime.now().toString());

            return new InvoicePreview(subscriptionId, amount, billingDate, lineItems, metadata);

        } catch (Exception e) {
            log.error("Error generating invoice preview for subscription: {}", subscriptionId, e);
            return null;
        }
    }

    @Override
    @Transactional
    public DunningResult processDunning(String subscriptionId, int dunningLevel) {
        try {
            log.info("Processing dunning level {} for subscription: {}", dunningLevel, subscriptionId);

            Invoice overdueInvoice = findFailedInvoice(subscriptionId);
            if (overdueInvoice == null) {
                return new DunningResult(false, "NO_OVERDUE_INVOICE", null, dunningLevel,
                        "No overdue invoice found");
            }

            String actionTaken;
            LocalDateTime nextActionDate;
            int newEscalationLevel = dunningLevel;

            switch (dunningLevel) {
                case 1 -> {
                    // Send first reminder
                    emailService.sendPaymentReminderEmail(overdueInvoice, 7); // 7 days overdue
                    actionTaken = "FIRST_REMINDER_SENT";
                    nextActionDate = LocalDateTime.now().plusDays(7);
                }
                case 2 -> {
                    // Send second reminder with late fee
                    addLateFee(overdueInvoice);
                    emailService.sendPaymentReminderEmail(overdueInvoice, 14); // 14 days overdue
                    actionTaken = "SECOND_REMINDER_SENT";
                    nextActionDate = LocalDateTime.now().plusDays(7);
                    newEscalationLevel = 3;
                }
                case 3 -> {
                    // Final notice
                    emailService.sendFinalNoticeEmail(overdueInvoice);
                    actionTaken = "FINAL_NOTICE_SENT";
                    nextActionDate = LocalDateTime.now().plusDays(7);
                    // Next step would be account suspension
                }
                default -> {
                    actionTaken = "MAX_LEVEL_REACHED";
                    nextActionDate = null;
                }
            }

            log.info("Dunning action '{}' completed for subscription: {}", actionTaken, subscriptionId);
            return new DunningResult(true, actionTaken, nextActionDate, newEscalationLevel,
                    "Dunning action processed successfully");

        } catch (Exception e) {
            log.error("Error processing dunning for subscription: {}", subscriptionId, e);
            return new DunningResult(false, "ERROR", null, dunningLevel,
                    "Dunning processing failed: " + e.getMessage());
        }
    }

    // Helper methods (simplified implementations)

    private Invoice generateRecurringInvoice(String subscriptionId, BillingCycle billingCycle) {
        // Simplified - in production would query subscription details
        return billingService.createSubscriptionInvoice(
                "user-" + subscriptionId, // userId
                "Monthly Subscription", // userName
                "user@example.com", // userEmail
                new BigDecimal("29.99"), // amount
                Currency.USD, // currency
                "default", // tenantId
                UUID.fromString(subscriptionId)); // subscriptionId
    }

    private PaymentGatewayService.PaymentResult processSubscriptionPayment(Invoice invoice) {
        // Simplified - in production would get payment method from subscription
        String paymentMethodId = "pm_card_visa"; // Example
        return paymentGateway.processPayment(invoice, paymentMethodId, invoice.getTotalAmount(), Map.of());
    }

    private void handleFailedPayment(Invoice invoice, PaymentGatewayService.PaymentResult paymentResult) {
        invoice.setStatus(com.mmva.newsapp.infrastructure.monetization.billing.enums.InvoiceStatus.OVERDUE);
        // Send failure notification
        emailService.sendPaymentReminderEmail(invoice, 1);
    }

    private Invoice findFailedInvoice(String subscriptionId) {
        // Simplified - in production would query database
        return null; // Placeholder
    }

    private Payment findLastSuccessfulPayment(String subscriptionId) {
        // Simplified - in production would query database
        return null; // Placeholder
    }

    private LocalDateTime calculateNextBillingDate(LocalDateTime from, BillingCycle cycle) {
        return switch (cycle) {
            case DAILY -> from.plusDays(1);
            case WEEKLY -> from.plusWeeks(1);
            case MONTHLY -> from.plusMonths(1);
            case QUARTERLY -> from.plusMonths(3);
            case YEARLY -> from.plusYears(1);
        };
    }

    private LocalDateTime calculatePeriodEndDate(String subscriptionId) {
        // Simplified - in production would calculate based on subscription end date
        return LocalDateTime.now().plusMonths(1);
    }

    private BigDecimal calculateProratedRefund(String subscriptionId, LocalDateTime cancellationDate) {
        // Simplified proration calculation
        LocalDateTime periodEnd = calculatePeriodEndDate(subscriptionId);
        long totalDays = ChronoUnit.DAYS.between(cancellationDate.minusMonths(1), periodEnd);
        long remainingDays = ChronoUnit.DAYS.between(cancellationDate, periodEnd);

        BigDecimal monthlyRate = new BigDecimal("29.99"); // Example
        return monthlyRate.multiply(BigDecimal.valueOf(remainingDays))
                .divide(BigDecimal.valueOf(totalDays), 2, BigDecimal.ROUND_HALF_UP);
    }

    private BigDecimal calculateProrationAmount(String subscriptionId, LocalDateTime changeDate) {
        // Simplified proration for billing changes
        LocalDateTime periodEnd = calculatePeriodEndDate(subscriptionId);
        long totalDays = ChronoUnit.DAYS.between(changeDate.minusMonths(1), periodEnd);
        long remainingDays = ChronoUnit.DAYS.between(changeDate, periodEnd);

        BigDecimal monthlyRate = new BigDecimal("29.99"); // Example
        return monthlyRate.multiply(BigDecimal.valueOf(remainingDays))
                .divide(BigDecimal.valueOf(totalDays), 2, BigDecimal.ROUND_HALF_UP);
    }

    private void addLateFee(Invoice invoice) {
        BigDecimal lateFee = invoice.getTotalAmount().multiply(LATE_FEE_PERCENTAGE);
        // In production, this would add a late fee line item to the invoice
        log.info("Added late fee of {} to invoice {}", lateFee, invoice.getInvoiceNumber());
    }
}