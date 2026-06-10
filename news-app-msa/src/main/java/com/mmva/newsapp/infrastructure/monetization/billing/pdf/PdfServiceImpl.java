package com.mmva.newsapp.infrastructure.monetization.billing.pdf;

import com.mmva.newsapp.infrastructure.monetization.billing.model.Invoice;
import com.mmva.newsapp.infrastructure.monetization.billing.model.InvoiceLineItem;
import com.mmva.newsapp.infrastructure.monetization.billing.repository.InvoiceLineItemRepository;
import com.mmva.newsapp.infrastructure.monetization.billing.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Production-ready implementation of PdfService using Apache PDFBox.
 *
 * <p>
 * Generates professional PDF invoices with proper formatting, headers,
 * line items, and totals. Uses Apache PDFBox for reliable, secure PDF
 * generation without licensing concerns.
 * </p>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PdfServiceImpl implements PdfService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceLineItemRepository invoiceLineItemRepository;

    @Value("${app.name:TheNews}")
    private String companyName;

    @Value("${app.company.address:123 News Street, Media City, MC 12345}")
    private String companyAddress;

    @Value("${app.company.phone:+1 (555) 123-4567}")
    private String companyPhone;

    @Value("${app.company.email:billing@thenews.com}")
    private String companyEmail;

    @Value("${app.company.website:www.thenews.com}")
    private String companyWebsite;

    @Value("${app.company.tax-id:TAX123456789}")
    private String companyTaxId;

    private static final float MARGIN = 50;
    private static final float LINE_HEIGHT = 15;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    @Override
    public byte[] generateInvoicePdf(Invoice invoice) {
        log.info("Generating PDF for invoice: {}", invoice.getInvoiceNumber());

        try {
            // Fetch line items
            List<InvoiceLineItem> lineItems = invoiceLineItemRepository
                    .findByInvoiceIdOrderByLineNumber(invoice.getInvoiceId());

            try (PDDocument document = new PDDocument();
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

                PDPage page = new PDPage(PDRectangle.A4);
                document.addPage(page);

                try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                    float yPosition = page.getMediaBox().getHeight() - MARGIN;

                    // Company Header
                    yPosition = drawCompanyHeader(contentStream, yPosition);

                    // Invoice Header
                    yPosition = drawInvoiceHeader(contentStream, invoice, yPosition);

                    // Bill To Section
                    yPosition = drawBillToSection(contentStream, invoice, yPosition);

                    // Line Items Table
                    yPosition = drawLineItemsTable(contentStream, lineItems, yPosition);

                    // Totals Section
                    yPosition = drawTotalsSection(contentStream, invoice, yPosition);

                    // Footer
                    drawFooter(contentStream, invoice);
                }

                document.save(outputStream);
                log.info("Successfully generated PDF for invoice: {}", invoice.getInvoiceNumber());
                return outputStream.toByteArray();
            }
        } catch (IOException e) {
            log.error("Failed to generate PDF for invoice: {}", invoice.getInvoiceNumber(), e);
            throw new RuntimeException("Failed to generate invoice PDF", e);
        }
    }

    @Override
    public byte[] generateReceiptPdf(Invoice invoice) {
        // For now, receipt is the same as invoice but with "RECEIPT" title
        // In a real implementation, this would have receipt-specific formatting
        try (PDDocument document = new PDDocument();
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                float yPosition = PDRectangle.A4.getHeight() - MARGIN;

                // Company header
                yPosition = drawCompanyHeader(contentStream, yPosition);

                // Receipt title
                PDType1Font boldFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
                contentStream.setFont(boldFont, 24);
                contentStream.beginText();
                contentStream.newLineAtOffset(MARGIN, yPosition);
                contentStream.showText("RECEIPT");
                contentStream.endText();

                yPosition -= LINE_HEIGHT * 3;

                // Receipt details
                contentStream.setFont(boldFont, 12);
                contentStream.beginText();
                contentStream.newLineAtOffset(MARGIN, yPosition);
                contentStream.showText("Receipt #: " + invoice.getInvoiceNumber());
                contentStream.endText();

                yPosition -= LINE_HEIGHT;
                PDType1Font normalFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
                contentStream.setFont(normalFont, 10);
                contentStream.beginText();
                contentStream.newLineAtOffset(MARGIN, yPosition);
                contentStream.showText("Payment Date: "
                        + (invoice.getPaymentDate() != null ? invoice.getPaymentDate().format(DATE_FORMATTER)
                                : LocalDate.now().format(DATE_FORMATTER)));
                contentStream.endText();

                yPosition -= LINE_HEIGHT * 2;

                // Bill to section
                yPosition = drawBillToSection(contentStream, invoice, yPosition);

                // Simple payment summary
                contentStream.setFont(boldFont, 12);
                contentStream.beginText();
                contentStream.newLineAtOffset(MARGIN, yPosition);
                contentStream.showText("Payment Summary");
                contentStream.endText();

                yPosition -= LINE_HEIGHT;
                contentStream.setFont(normalFont, 10);
                NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);

                contentStream.beginText();
                contentStream.newLineAtOffset(MARGIN, yPosition);
                contentStream.showText("Total Paid: " + currencyFormat.format(invoice.getTotalAmount()));
                contentStream.endText();

                // Footer
                drawFooter(contentStream, invoice);
            }

            document.save(outputStream);
            log.info("Successfully generated receipt PDF for invoice: {}", invoice.getInvoiceNumber());
            return outputStream.toByteArray();
        } catch (IOException e) {
            log.error("Failed to generate receipt PDF for invoice: {}", invoice.getInvoiceNumber(), e);
            throw new RuntimeException("Failed to generate receipt PDF", e);
        }
    }

    @Override
    public byte[] generateTaxInvoicePdf(Invoice invoice) {
        // For now, tax invoice is the same as regular invoice
        // In a real implementation, this would have tax-specific formatting
        return generateInvoicePdf(invoice);
    }

    private float drawCompanyHeader(PDPageContentStream contentStream, float yPosition) throws IOException {
        PDType1Font boldFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        PDType1Font normalFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

        contentStream.setFont(boldFont, 18);
        contentStream.beginText();
        contentStream.newLineAtOffset(MARGIN, yPosition);
        contentStream.showText(companyName);
        contentStream.endText();

        yPosition -= LINE_HEIGHT * 2;

        contentStream.setFont(normalFont, 10);
        contentStream.beginText();
        contentStream.newLineAtOffset(MARGIN, yPosition);
        contentStream.showText(companyAddress);
        contentStream.endText();

        yPosition -= LINE_HEIGHT;
        contentStream.beginText();
        contentStream.newLineAtOffset(MARGIN, yPosition);
        contentStream.showText(companyPhone + " | " + companyEmail);
        contentStream.endText();

        yPosition -= LINE_HEIGHT;
        contentStream.beginText();
        contentStream.newLineAtOffset(MARGIN, yPosition);
        contentStream.showText(companyWebsite + " | Tax ID: " + companyTaxId);
        contentStream.endText();

        return yPosition - LINE_HEIGHT * 2;
    }

    private float drawInvoiceHeader(PDPageContentStream contentStream, Invoice invoice, float yPosition)
            throws IOException {
        PDType1Font boldFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        PDType1Font normalFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

        // Invoice title
        contentStream.setFont(boldFont, 24);
        contentStream.beginText();
        contentStream.newLineAtOffset(MARGIN, yPosition);
        contentStream.showText("INVOICE");
        contentStream.endText();

        // Invoice number and date (right aligned)
        float rightX = PDRectangle.A4.getWidth() - MARGIN - 150;
        contentStream.setFont(boldFont, 12);
        contentStream.beginText();
        contentStream.newLineAtOffset(rightX, yPosition);
        contentStream.showText("Invoice #: " + invoice.getInvoiceNumber());
        contentStream.endText();

        yPosition -= LINE_HEIGHT;
        contentStream.setFont(normalFont, 10);
        contentStream.beginText();
        contentStream.newLineAtOffset(rightX, yPosition);
        contentStream.showText("Date: " + invoice.getCreatedAt().format(DATE_FORMATTER));
        contentStream.endText();

        yPosition -= LINE_HEIGHT;
        contentStream.beginText();
        contentStream.newLineAtOffset(rightX, yPosition);
        contentStream.showText("Due Date: " + invoice.getDueDate().format(DATE_FORMATTER));
        contentStream.endText();

        return yPosition - LINE_HEIGHT * 2;
    }

    private float drawBillToSection(PDPageContentStream contentStream, Invoice invoice, float yPosition)
            throws IOException {
        PDType1Font boldFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        PDType1Font normalFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

        contentStream.setFont(boldFont, 12);
        contentStream.beginText();
        contentStream.newLineAtOffset(MARGIN, yPosition);
        contentStream.showText("Bill To:");
        contentStream.endText();

        yPosition -= LINE_HEIGHT * 1.5f;
        contentStream.setFont(normalFont, 10);
        contentStream.beginText();
        contentStream.newLineAtOffset(MARGIN, yPosition);
        contentStream.showText(invoice.getClientName());
        contentStream.endText();

        yPosition -= LINE_HEIGHT;
        contentStream.beginText();
        contentStream.newLineAtOffset(MARGIN, yPosition);
        contentStream.showText("Client ID: " + invoice.getClientId());
        contentStream.endText();

        return yPosition - LINE_HEIGHT * 2;
    }

    private float drawLineItemsTable(PDPageContentStream contentStream, List<InvoiceLineItem> lineItems,
            float yPosition) throws IOException {
        PDType1Font boldFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        PDType1Font normalFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

        // Table headers
        contentStream.setFont(boldFont, 10);
        float[] columnWidths = { 300, 60, 80, 80 };
        float[] xPositions = { MARGIN, MARGIN + columnWidths[0], MARGIN + columnWidths[0] + columnWidths[1],
                MARGIN + columnWidths[0] + columnWidths[1] + columnWidths[2] };

        contentStream.beginText();
        contentStream.newLineAtOffset(xPositions[0], yPosition);
        contentStream.showText("Description");
        contentStream.endText();

        contentStream.beginText();
        contentStream.newLineAtOffset(xPositions[1], yPosition);
        contentStream.showText("Qty");
        contentStream.endText();

        contentStream.beginText();
        contentStream.newLineAtOffset(xPositions[2], yPosition);
        contentStream.showText("Unit Price");
        contentStream.endText();

        contentStream.beginText();
        contentStream.newLineAtOffset(xPositions[3], yPosition);
        contentStream.showText("Amount");
        contentStream.endText();

        // Header underline
        yPosition -= 5;
        contentStream.moveTo(MARGIN, yPosition);
        contentStream.lineTo(PDRectangle.A4.getWidth() - MARGIN, yPosition);
        contentStream.stroke();

        yPosition -= LINE_HEIGHT;

        // Line items
        contentStream.setFont(normalFont, 9);
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);

        for (InvoiceLineItem item : lineItems) {
            contentStream.beginText();
            contentStream.newLineAtOffset(xPositions[0], yPosition);
            contentStream.showText(item.getDescription());
            contentStream.endText();

            contentStream.beginText();
            contentStream.newLineAtOffset(xPositions[1], yPosition);
            contentStream.showText(item.getQuantity().toString());
            contentStream.endText();

            contentStream.beginText();
            contentStream.newLineAtOffset(xPositions[2], yPosition);
            contentStream.showText(currencyFormat.format(item.getUnitPrice()));
            contentStream.endText();

            contentStream.beginText();
            contentStream.newLineAtOffset(xPositions[3], yPosition);
            contentStream.showText(currencyFormat.format(item.getLineTotal()));
            contentStream.endText();

            yPosition -= LINE_HEIGHT;
        }

        return yPosition - LINE_HEIGHT;
    }

    private float drawTotalsSection(PDPageContentStream contentStream, Invoice invoice, float yPosition)
            throws IOException {
        PDType1Font normalFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        PDType1Font boldFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

        float rightX = PDRectangle.A4.getWidth() - MARGIN - 150;
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);

        contentStream.setFont(normalFont, 10);

        // Subtotal
        contentStream.beginText();
        contentStream.newLineAtOffset(rightX, yPosition);
        contentStream.showText("Subtotal: " + currencyFormat.format(invoice.getSubtotal()));
        contentStream.endText();

        yPosition -= LINE_HEIGHT;

        // Tax
        if (invoice.getTaxAmount() != null && invoice.getTaxAmount().compareTo(BigDecimal.ZERO) > 0) {
            contentStream.beginText();
            contentStream.newLineAtOffset(rightX, yPosition);
            contentStream.showText("Tax: " + currencyFormat.format(invoice.getTaxAmount()));
            contentStream.endText();
            yPosition -= LINE_HEIGHT;
        }

        // Total
        contentStream.setFont(boldFont, 12);
        yPosition -= 5;
        contentStream.moveTo(rightX - 20, yPosition);
        contentStream.lineTo(PDRectangle.A4.getWidth() - MARGIN, yPosition);
        contentStream.stroke();

        yPosition -= LINE_HEIGHT;
        contentStream.beginText();
        contentStream.newLineAtOffset(rightX, yPosition);
        contentStream.showText("Total: " + currencyFormat.format(invoice.getTotalAmount()));
        contentStream.endText();

        return yPosition - LINE_HEIGHT * 2;
    }

    private void drawFooter(PDPageContentStream contentStream, Invoice invoice) throws IOException {
        PDType1Font normalFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

        float yPosition = MARGIN + LINE_HEIGHT * 2;

        contentStream.setFont(normalFont, 8);
        contentStream.beginText();
        contentStream.newLineAtOffset(MARGIN, yPosition);
        contentStream.showText("Thank you for your business! Payment is due within " +
                java.time.temporal.ChronoUnit.DAYS.between(invoice.getCreatedAt().toLocalDate(), invoice.getDueDate()) +
                " days. Please include invoice number on all payments.");
        contentStream.endText();
    }

    private Invoice getInvoiceByNumber(String invoiceNumber) {
        return invoiceRepository.findByInvoiceNumber(invoiceNumber)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + invoiceNumber));
    }
}