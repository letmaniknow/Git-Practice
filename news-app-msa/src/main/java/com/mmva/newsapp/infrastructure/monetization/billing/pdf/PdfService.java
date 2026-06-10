package com.mmva.newsapp.infrastructure.monetization.billing.pdf;

import com.mmva.newsapp.infrastructure.monetization.billing.model.Invoice;

/**
 * Service for generating PDF documents for billing operations.
 *
 * <p>
 * Supports generation of:
 * </p>
 * <ul>
 * <li>Professional invoices with company branding</li>
 * <li>Receipts for completed payments</li>
 * <li>Tax documents and statements</li>
 * </ul>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
public interface PdfService {

    /**
     * Generates a professional PDF invoice for the given invoice entity.
     *
     * @param invoice The invoice to generate PDF for
     * @return Byte array containing the PDF data
     */
    byte[] generateInvoicePdf(Invoice invoice);

    /**
     * Generates a receipt PDF for a completed payment.
     *
     * @param invoice The invoice that was paid
     * @return Byte array containing the PDF data
     */
    byte[] generateReceiptPdf(Invoice invoice);

    /**
     * Generates a tax invoice PDF with tax-specific formatting.
     *
     * @param invoice The invoice to generate tax document for
     * @return Byte array containing the PDF data
     */
    byte[] generateTaxInvoicePdf(Invoice invoice);
}