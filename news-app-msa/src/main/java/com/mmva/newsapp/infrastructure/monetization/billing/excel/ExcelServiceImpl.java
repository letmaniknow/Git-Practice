package com.mmva.newsapp.infrastructure.monetization.billing.excel;

import com.mmva.newsapp.infrastructure.monetization.billing.enums.InvoiceStatus;
import com.mmva.newsapp.infrastructure.monetization.billing.model.Invoice;
import com.mmva.newsapp.infrastructure.monetization.billing.model.InvoiceLineItem;
import com.mmva.newsapp.infrastructure.monetization.billing.repository.InvoiceLineItemRepository;
import com.mmva.newsapp.infrastructure.monetization.billing.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Production-ready implementation of ExcelService using Apache POI.
 *
 * <p>
 * Generates professional Excel reports for billing operations including
 * invoice reports, client summaries, and payment status tracking.
 * Uses Apache POI for reliable Excel generation.
 * </p>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExcelServiceImpl implements ExcelService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceLineItemRepository invoiceLineItemRepository;

    @Value("${app.name:TheNews}")
    private String companyName;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public byte[] generateInvoiceReport(LocalDate startDate, LocalDate endDate) throws IOException {
        log.info("Generating invoice report from {} to {}", startDate, endDate);

        List<Invoice> invoices = invoiceRepository.findByCreatedAtBetween(
                startDate.atStartOfDay(), endDate.atTime(23, 59, 59));

        try (Workbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Invoice Report");
            createInvoiceReportHeader(workbook, sheet);

            int rowNum = 1;
            for (Invoice invoice : invoices) {
                Row row = sheet.createRow(rowNum++);
                populateInvoiceRow(row, invoice);
            }

            // Auto-size columns
            for (int i = 0; i < 10; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);
            log.info("Successfully generated invoice report with {} invoices", invoices.size());
            return outputStream.toByteArray();
        }
    }

    @Override
    public byte[] generateClientBillingReport(LocalDate startDate, LocalDate endDate) throws IOException {
        log.info("Generating client billing report from {} to {}", startDate, endDate);

        List<Invoice> invoices = invoiceRepository.findByCreatedAtBetween(
                startDate.atStartOfDay(), endDate.atTime(23, 59, 59));

        // Group by client
        Map<String, List<Invoice>> invoicesByClient = invoices.stream()
                .collect(Collectors.groupingBy(Invoice::getClientId));

        try (Workbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Client Billing Summary");
            createClientReportHeader(workbook, sheet);

            int rowNum = 1;
            for (Map.Entry<String, List<Invoice>> entry : invoicesByClient.entrySet()) {
                String clientId = entry.getKey();
                List<Invoice> clientInvoices = entry.getValue();

                BigDecimal totalAmount = clientInvoices.stream()
                        .map(Invoice::getTotalAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal paidAmount = clientInvoices.stream()
                        .filter(invoice -> invoice.getStatus() == InvoiceStatus.PAID)
                        .map(Invoice::getTotalAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal outstandingAmount = totalAmount.subtract(paidAmount);

                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(clientId);
                row.createCell(1).setCellValue(clientInvoices.get(0).getClientName());
                row.createCell(2).setCellValue(clientInvoices.size());
                row.createCell(3).setCellValue(totalAmount.doubleValue());
                row.createCell(4).setCellValue(paidAmount.doubleValue());
                row.createCell(5).setCellValue(outstandingAmount.doubleValue());
            }

            // Auto-size columns
            for (int i = 0; i < 6; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);
            log.info("Successfully generated client billing report for {} clients", invoicesByClient.size());
            return outputStream.toByteArray();
        }
    }

    @Override
    public byte[] generatePaymentStatusReport(LocalDate startDate, LocalDate endDate) throws IOException {
        log.info("Generating payment status report from {} to {}", startDate, endDate);

        List<Invoice> invoices = invoiceRepository.findByCreatedAtBetween(
                startDate.atStartOfDay(), endDate.atTime(23, 59, 59));

        try (Workbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Payment Status Report");
            createPaymentStatusHeader(workbook, sheet);

            int rowNum = 1;
            for (Invoice invoice : invoices) {
                Row row = sheet.createRow(rowNum++);
                populatePaymentStatusRow(row, invoice);
            }

            // Auto-size columns
            for (int i = 0; i < 8; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);
            log.info("Successfully generated payment status report with {} invoices", invoices.size());
            return outputStream.toByteArray();
        }
    }

    @Override
    public byte[] generateInvoiceDetailReport(String invoiceNumber) throws IOException {
        log.info("Generating detailed report for invoice: {}", invoiceNumber);

        Invoice invoice = invoiceRepository.findByInvoiceNumber(invoiceNumber)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + invoiceNumber));

        List<InvoiceLineItem> lineItems = invoiceLineItemRepository
                .findByInvoiceIdOrderByLineNumber(invoice.getInvoiceId());

        try (Workbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            // Invoice summary sheet
            Sheet summarySheet = workbook.createSheet("Invoice Summary");
            createInvoiceSummaryHeader(workbook, summarySheet);
            populateInvoiceSummary(summarySheet, invoice);

            // Line items sheet
            Sheet lineItemsSheet = workbook.createSheet("Line Items");
            createLineItemsHeader(workbook, lineItemsSheet);

            int rowNum = 1;
            for (InvoiceLineItem item : lineItems) {
                Row row = lineItemsSheet.createRow(rowNum++);
                populateLineItemRow(row, item);
            }

            // Auto-size columns
            for (int i = 0; i < 6; i++) {
                lineItemsSheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);
            log.info("Successfully generated detailed report for invoice: {}", invoiceNumber);
            return outputStream.toByteArray();
        }
    }

    private void createInvoiceReportHeader(Workbook workbook, Sheet sheet) {
        Row headerRow = sheet.createRow(0);
        CellStyle headerStyle = createHeaderStyle(workbook);

        String[] headers = { "Invoice Number", "Client ID", "Client Name", "Created Date",
                "Due Date", "Subtotal", "Tax Amount", "Total Amount", "Payment Status", "Paid Date" };

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    private void createClientReportHeader(Workbook workbook, Sheet sheet) {
        Row headerRow = sheet.createRow(0);
        CellStyle headerStyle = createHeaderStyle(workbook);

        String[] headers = { "Client ID", "Client Name", "Invoice Count", "Total Amount", "Paid Amount",
                "Outstanding Amount" };

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    private void createPaymentStatusHeader(Workbook workbook, Sheet sheet) {
        Row headerRow = sheet.createRow(0);
        CellStyle headerStyle = createHeaderStyle(workbook);

        String[] headers = { "Invoice Number", "Client Name", "Total Amount", "Payment Status",
                "Due Date", "Days Overdue", "Last Payment Date", "Notes" };

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    private void createInvoiceSummaryHeader(Workbook workbook, Sheet sheet) {
        Row headerRow = sheet.createRow(0);
        CellStyle headerStyle = createHeaderStyle(workbook);

        String[] headers = { "Field", "Value" };

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    private void createLineItemsHeader(Workbook workbook, Sheet sheet) {
        Row headerRow = sheet.createRow(0);
        CellStyle headerStyle = createHeaderStyle(workbook);

        String[] headers = { "Line Number", "Description", "Quantity", "Unit Price", "Line Total", "Taxable" };

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    private void populateInvoiceRow(Row row, Invoice invoice) {
        row.createCell(0).setCellValue(invoice.getInvoiceNumber());
        row.createCell(1).setCellValue(invoice.getClientId());
        row.createCell(2).setCellValue(invoice.getClientName());
        row.createCell(3).setCellValue(invoice.getCreatedAt().format(DATE_FORMATTER));
        row.createCell(4).setCellValue(invoice.getDueDate().format(DATE_FORMATTER));
        row.createCell(5).setCellValue(invoice.getSubtotal().doubleValue());
        row.createCell(6).setCellValue(invoice.getTaxAmount() != null ? invoice.getTaxAmount().doubleValue() : 0.0);
        row.createCell(7).setCellValue(invoice.getTotalAmount().doubleValue());
        row.createCell(8).setCellValue(invoice.getStatus().toString());
        row.createCell(9)
                .setCellValue(invoice.getPaymentDate() != null ? invoice.getPaymentDate().format(DATE_FORMATTER) : "");
    }

    private void populatePaymentStatusRow(Row row, Invoice invoice) {
        row.createCell(0).setCellValue(invoice.getInvoiceNumber());
        row.createCell(1).setCellValue(invoice.getClientName());
        row.createCell(2).setCellValue(invoice.getTotalAmount().doubleValue());
        row.createCell(3).setCellValue(invoice.getStatus().toString());
        row.createCell(4).setCellValue(invoice.getDueDate().format(DATE_FORMATTER));

        long daysOverdue = LocalDate.now().isAfter(invoice.getDueDate())
                ? java.time.temporal.ChronoUnit.DAYS.between(invoice.getDueDate(), LocalDate.now())
                : 0;
        row.createCell(5).setCellValue(daysOverdue);

        row.createCell(6)
                .setCellValue(invoice.getPaymentDate() != null ? invoice.getPaymentDate().format(DATE_FORMATTER) : "");
        row.createCell(7).setCellValue(invoice.getNotes() != null ? invoice.getNotes() : "");
    }

    private void populateInvoiceSummary(Sheet sheet, Invoice invoice) {
        String[][] data = {
                { "Invoice Number", invoice.getInvoiceNumber() },
                { "Client ID", invoice.getClientId() },
                { "Client Name", invoice.getClientName() },
                { "Created Date", invoice.getCreatedAt().format(DATE_FORMATTER) },
                { "Due Date", invoice.getDueDate().format(DATE_FORMATTER) },
                { "Subtotal", "$" + invoice.getSubtotal().toString() },
                { "Tax Amount", invoice.getTaxAmount() != null ? "$" + invoice.getTaxAmount().toString() : "$0.00" },
                { "Total Amount", "$" + invoice.getTotalAmount().toString() },
                { "Payment Status", invoice.getStatus().toString() },
                { "Paid Date",
                        invoice.getPaymentDate() != null ? invoice.getPaymentDate().format(DATE_FORMATTER) : "N/A" },
                { "Notes", invoice.getNotes() != null ? invoice.getNotes() : "" }
        };

        for (int i = 0; i < data.length; i++) {
            Row row = sheet.createRow(i + 1);
            row.createCell(0).setCellValue(data[i][0]);
            row.createCell(1).setCellValue(data[i][1]);
        }

        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    private void populateLineItemRow(Row row, InvoiceLineItem item) {
        row.createCell(0).setCellValue(item.getLineNumber());
        row.createCell(1).setCellValue(item.getDescription());
        row.createCell(2).setCellValue(item.getQuantity().doubleValue());
        row.createCell(3).setCellValue(item.getUnitPrice().doubleValue());
        row.createCell(4).setCellValue(item.getLineTotal().doubleValue());
        row.createCell(5).setCellValue(
                item.getTaxRate() != null && item.getTaxRate().compareTo(BigDecimal.ZERO) > 0 ? "Yes" : "No");
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        return style;
    }
}