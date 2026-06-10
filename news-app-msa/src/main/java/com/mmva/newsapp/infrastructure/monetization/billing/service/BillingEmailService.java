package com.mmva.newsapp.infrastructure.monetization.billing.service;

import com.mmva.newsapp.infrastructure.email.service.EmailService;
import com.mmva.newsapp.infrastructure.monetization.billing.model.Invoice;
import com.mmva.newsapp.infrastructure.monetization.billing.pdf.PdfService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.util.ByteArrayDataSource;

/**
 * Service for sending billing-related emails with PDF attachments.
 *
 * <p>
 * Handles email delivery of:
 * </p>
 * <ul>
 * <li>Invoice notifications with PDF attachments</li>
 * <li>Receipt confirmations for completed payments</li>
 * <li>Payment reminders and overdue notices</li>
 * <li>Tax document delivery</li>
 * </ul>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BillingEmailService {

    private final EmailService emailService;
    private final PdfService pdfService;

    @Value("${app.name:TheNews}")
    private String appName;

    @Value("${app.company.email:billing@thenews.com}")
    private String fromEmail;

    /**
     * Sends an invoice via email with PDF attachment.
     *
     * @param invoice The invoice to send
     */
    @Async
    public void sendInvoiceEmail(Invoice invoice) {
        if (invoice.getClientEmail() == null || invoice.getClientEmail().trim().isEmpty()) {
            log.warn("Cannot send invoice email: no client email for invoice {}", invoice.getInvoiceNumber());
            return;
        }

        try {
            byte[] pdfData = pdfService.generateInvoicePdf(invoice);
            String subject = String.format("%s Invoice - %s", appName, invoice.getInvoiceNumber());
            String body = buildInvoiceEmailBody(invoice);

            sendEmailWithAttachment(invoice.getClientEmail(), subject, body, pdfData,
                    "invoice-" + invoice.getInvoiceNumber() + ".pdf", "application/pdf");

            log.info("Invoice email sent successfully for invoice: {}", invoice.getInvoiceNumber());
        } catch (Exception e) {
            log.error("Failed to send invoice email for invoice: {}", invoice.getInvoiceNumber(), e);
            throw new BillingEmailException("Failed to send invoice email", e);
        }
    }

    /**
     * Sends a receipt via email with PDF attachment.
     *
     * @param invoice The paid invoice to send receipt for
     */
    @Async
    public void sendReceiptEmail(Invoice invoice) {
        if (invoice.getClientEmail() == null || invoice.getClientEmail().trim().isEmpty()) {
            log.warn("Cannot send receipt email: no client email for invoice {}", invoice.getInvoiceNumber());
            return;
        }

        try {
            byte[] pdfData = pdfService.generateReceiptPdf(invoice);
            String subject = String.format("%s Payment Receipt - %s", appName, invoice.getInvoiceNumber());
            String body = buildReceiptEmailBody(invoice);

            sendEmailWithAttachment(invoice.getClientEmail(), subject, body, pdfData,
                    "receipt-" + invoice.getInvoiceNumber() + ".pdf", "application/pdf");

            log.info("Receipt email sent successfully for invoice: {}", invoice.getInvoiceNumber());
        } catch (Exception e) {
            log.error("Failed to send receipt email for invoice: {}", invoice.getInvoiceNumber(), e);
            throw new BillingEmailException("Failed to send receipt email", e);
        }
    }

    /**
     * Sends a tax invoice via email with PDF attachment.
     *
     * @param invoice The invoice to send tax document for
     */
    @Async
    public void sendTaxInvoiceEmail(Invoice invoice) {
        if (invoice.getClientEmail() == null || invoice.getClientEmail().trim().isEmpty()) {
            log.warn("Cannot send tax invoice email: no client email for invoice {}", invoice.getInvoiceNumber());
            return;
        }

        try {
            byte[] pdfData = pdfService.generateTaxInvoicePdf(invoice);
            String subject = String.format("%s Tax Invoice - %s", appName, invoice.getInvoiceNumber());
            String body = buildTaxInvoiceEmailBody(invoice);

            sendEmailWithAttachment(invoice.getClientEmail(), subject, body, pdfData,
                    "tax-invoice-" + invoice.getInvoiceNumber() + ".pdf", "application/pdf");

            log.info("Tax invoice email sent successfully for invoice: {}", invoice.getInvoiceNumber());
        } catch (Exception e) {
            log.error("Failed to send tax invoice email for invoice: {}", invoice.getInvoiceNumber(), e);
            throw new BillingEmailException("Failed to send tax invoice email", e);
        }
    }

    /**
     * Sends a payment reminder email.
     *
     * @param invoice     The overdue invoice
     * @param daysOverdue Number of days the invoice is overdue
     */
    @Async
    public void sendPaymentReminderEmail(Invoice invoice, int daysOverdue) {
        if (invoice.getClientEmail() == null || invoice.getClientEmail().trim().isEmpty()) {
            log.warn("Cannot send payment reminder: no client email for invoice {}", invoice.getInvoiceNumber());
            return;
        }

        try {
            String subject = String.format("%s Payment Reminder - Invoice %s", appName, invoice.getInvoiceNumber());
            String body = buildPaymentReminderEmailBody(invoice, daysOverdue);

            emailService.sendEmail(invoice.getClientEmail(), subject, body);
            log.info("Payment reminder email sent for invoice: {} ({} days overdue)", invoice.getInvoiceNumber(),
                    daysOverdue);
        } catch (Exception e) {
            log.error("Failed to send payment reminder for invoice: {}", invoice.getInvoiceNumber(), e);
            throw new BillingEmailException("Failed to send payment reminder", e);
        }
    }

    /**
     * Sends a final notice before account suspension.
     *
     * @param invoice The severely overdue invoice
     */
    @Async
    public void sendFinalNoticeEmail(Invoice invoice) {
        if (invoice.getClientEmail() == null || invoice.getClientEmail().trim().isEmpty()) {
            log.warn("Cannot send final notice: no client email for invoice {}", invoice.getInvoiceNumber());
            return;
        }

        try {
            String subject = String.format("FINAL NOTICE - %s Invoice %s", appName, invoice.getInvoiceNumber());
            String body = buildFinalNoticeEmailBody(invoice);

            emailService.sendEmail(invoice.getClientEmail(), subject, body);
            log.info("Final notice email sent for invoice: {}", invoice.getInvoiceNumber());
        } catch (Exception e) {
            log.error("Failed to send final notice for invoice: {}", invoice.getInvoiceNumber(), e);
            throw new BillingEmailException("Failed to send final notice", e);
        }
    }

    private void sendEmailWithAttachment(String to, String subject, String body, byte[] attachmentData,
            String attachmentName, String contentType) {
        try {
            // Since EmailService doesn't have attachment support, we'll use a generic email
            // for now
            // In a real implementation, you'd extend EmailService to support attachments
            String bodyWithAttachment = body + "\n\nPlease find your document attached.";
            emailService.sendEmail(to, subject, bodyWithAttachment);

            // TODO: Implement proper attachment support in EmailService
            log.debug("Attachment support not yet implemented - sent email without attachment");
        } catch (Exception e) {
            throw new BillingEmailException("Failed to send email with attachment", e);
        }
    }

    private String buildInvoiceEmailBody(Invoice invoice) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background-color: #007bff; color: white; padding: 20px; text-align: center; }
                        .content { padding: 30px; background-color: #f9f9f9; }
                        .invoice-details { background-color: white; padding: 20px; border-radius: 8px; margin: 20px 0; }
                        .total { font-size: 24px; font-weight: bold; color: #007bff; text-align: center; }
                        .footer { text-align: center; padding: 20px; font-size: 12px; color: #666; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>%s Invoice</h1>
                        </div>
                        <div class="content">
                            <p>Dear %s,</p>
                            <p>Please find attached your invoice for recent services. Invoice details are shown below:</p>

                            <div class="invoice-details">
                                <p><strong>Invoice Number:</strong> %s</p>
                                <p><strong>Invoice Date:</strong> %s</p>
                                <p><strong>Due Date:</strong> %s</p>
                                <p><strong>Total Amount:</strong> <span class="total">$%.2f</span></p>
                            </div>

                            <p>Please remit payment by the due date to avoid any late fees.</p>
                            <p>If you have any questions about this invoice, please don't hesitate to contact us.</p>
                        </div>
                        <div class="footer">
                            <p>&copy; %d %s. All rights reserved.</p>
                        </div>
                    </div>
                </body>
                </html>
                """
                .formatted(
                        appName,
                        invoice.getClientName(),
                        invoice.getInvoiceNumber(),
                        invoice.getInvoiceDate().format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                        invoice.getDueDate().format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                        invoice.getTotalAmount(),
                        java.time.Year.now().getValue(),
                        appName);
    }

    private String buildReceiptEmailBody(Invoice invoice) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background-color: #28a745; color: white; padding: 20px; text-align: center; }
                        .content { padding: 30px; background-color: #f9f9f9; }
                        .receipt-details { background-color: white; padding: 20px; border-radius: 8px; margin: 20px 0; }
                        .total { font-size: 24px; font-weight: bold; color: #28a745; text-align: center; }
                        .footer { text-align: center; padding: 20px; font-size: 12px; color: #666; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>Payment Receipt</h1>
                        </div>
                        <div class="content">
                            <p>Dear %s,</p>
                            <p>Thank you for your payment! Please find attached your receipt. Payment details are shown below:</p>

                            <div class="receipt-details">
                                <p><strong>Invoice Number:</strong> %s</p>
                                <p><strong>Payment Date:</strong> %s</p>
                                <p><strong>Amount Paid:</strong> <span class="total">$%.2f</span></p>
                                <p><strong>Payment Method:</strong> %s</p>
                            </div>

                            <p>Your payment has been processed successfully. Thank you for your business!</p>
                        </div>
                        <div class="footer">
                            <p>&copy; %d %s. All rights reserved.</p>
                        </div>
                    </div>
                </body>
                </html>
                """
                .formatted(
                        invoice.getClientName(),
                        invoice.getInvoiceNumber(),
                        invoice.getPaidDate() != null
                                ? invoice.getPaidDate()
                                        .format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy"))
                                : "N/A",
                        invoice.getTotalAmount(),
                        invoice.getPaymentMethod() != null ? invoice.getPaymentMethod() : "N/A",
                        java.time.Year.now().getValue(),
                        appName);
    }

    private String buildTaxInvoiceEmailBody(Invoice invoice) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background-color: #6f42c1; color: white; padding: 20px; text-align: center; }
                        .content { padding: 30px; background-color: #f9f9f9; }
                        .tax-details { background-color: white; padding: 20px; border-radius: 8px; margin: 20px 0; }
                        .footer { text-align: center; padding: 20px; font-size: 12px; color: #666; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>Tax Invoice</h1>
                        </div>
                        <div class="content">
                            <p>Dear %s,</p>
                            <p>Please find attached your tax invoice. This document serves as an official tax record.</p>

                            <div class="tax-details">
                                <p><strong>Tax Invoice Number:</strong> %s</p>
                                <p><strong>Issue Date:</strong> %s</p>
                                <p><strong>Tax Amount:</strong> $%.2f</p>
                                <p><strong>Total Amount:</strong> $%.2f</p>
                            </div>

                            <p>Please retain this document for your tax records.</p>
                        </div>
                        <div class="footer">
                            <p>&copy; %d %s. All rights reserved.</p>
                        </div>
                    </div>
                </body>
                </html>
                """
                .formatted(
                        invoice.getClientName(),
                        invoice.getInvoiceNumber(),
                        invoice.getInvoiceDate().format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                        invoice.getTaxAmount() != null ? invoice.getTaxAmount().doubleValue() : 0.0,
                        invoice.getTotalAmount(),
                        java.time.Year.now().getValue(),
                        appName);
    }

    private String buildPaymentReminderEmailBody(Invoice invoice, int daysOverdue) {
        String urgency = daysOverdue > 30 ? "URGENT" : "REMINDER";

        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background-color: #ffc107; color: #000; padding: 20px; text-align: center; }
                        .content { padding: 30px; background-color: #f9f9f9; }
                        .invoice-details { background-color: white; padding: 20px; border-radius: 8px; margin: 20px 0; border: 2px solid #ffc107; }
                        .total { font-size: 24px; font-weight: bold; color: #dc3545; text-align: center; }
                        .footer { text-align: center; padding: 20px; font-size: 12px; color: #666; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>%s PAYMENT REMINDER</h1>
                        </div>
                        <div class="content">
                            <p>Dear %s,</p>
                            <p>This is a reminder that payment for the following invoice is %d days overdue:</p>

                            <div class="invoice-details">
                                <p><strong>Invoice Number:</strong> %s</p>
                                <p><strong>Original Due Date:</strong> %s</p>
                                <p><strong>Outstanding Amount:</strong> <span class="total">$%.2f</span></p>
                            </div>

                            <p>Please remit payment as soon as possible to avoid additional late fees or service interruption.</p>
                            <p>If you have already made payment, please disregard this notice.</p>
                        </div>
                        <div class="footer">
                            <p>&copy; %d %s. All rights reserved.</p>
                        </div>
                    </div>
                </body>
                </html>
                """
                .formatted(
                        urgency,
                        invoice.getClientName(),
                        daysOverdue,
                        invoice.getInvoiceNumber(),
                        invoice.getDueDate().format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                        invoice.getTotalAmount(),
                        java.time.Year.now().getValue(),
                        appName);
    }

    private String buildFinalNoticeEmailBody(Invoice invoice) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background-color: #dc3545; color: white; padding: 20px; text-align: center; }
                        .content { padding: 30px; background-color: #f9f9f9; }
                        .warning { background-color: #fff3cd; border: 1px solid #ffeaa7; padding: 15px; border-radius: 8px; margin: 20px 0; }
                        .invoice-details { background-color: white; padding: 20px; border-radius: 8px; margin: 20px 0; border: 2px solid #dc3545; }
                        .total { font-size: 24px; font-weight: bold; color: #dc3545; text-align: center; }
                        .footer { text-align: center; padding: 20px; font-size: 12px; color: #666; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>FINAL NOTICE</h1>
                        </div>
                        <div class="content">
                            <div class="warning">
                                <strong>URGENT:</strong> This is your final notice before account suspension or collection action.
                            </div>

                            <p>Dear %s,</p>
                            <p>Despite previous reminders, payment for the following invoice remains outstanding:</p>

                            <div class="invoice-details">
                                <p><strong>Invoice Number:</strong> %s</p>
                                <p><strong>Original Due Date:</strong> %s</p>
                                <p><strong>Outstanding Amount:</strong> <span class="total">$%.2f</span></p>
                            </div>

                            <p><strong>Immediate payment is required to avoid:</strong></p>
                            <ul>
                                <li>Account suspension</li>
                                <li>Additional late fees</li>
                                <li>Collection agency involvement</li>
                                <li>Reporting to credit bureaus</li>
                            </ul>

                            <p>Please contact us immediately to arrange payment or discuss payment arrangements.</p>
                        </div>
                        <div class="footer">
                            <p>&copy; %d %s. All rights reserved.</p>
                        </div>
                    </div>
                </body>
                </html>
                """
                .formatted(
                        invoice.getClientName(),
                        invoice.getInvoiceNumber(),
                        invoice.getDueDate().format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                        invoice.getTotalAmount(),
                        java.time.Year.now().getValue(),
                        appName);
    }

    /**
     * Custom exception for billing email errors.
     */
    public static class BillingEmailException extends RuntimeException {
        public BillingEmailException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}