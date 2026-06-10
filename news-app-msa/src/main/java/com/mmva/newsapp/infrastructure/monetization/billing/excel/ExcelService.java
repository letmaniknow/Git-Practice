package com.mmva.newsapp.infrastructure.monetization.billing.excel;

import java.io.IOException;
import java.time.LocalDate;

/**
 * Service interface for generating Excel reports for billing operations.
 *
 * <p>
 * Provides functionality to generate various Excel reports including:
 * - Invoice reports with detailed line items
 * - Billing summaries by client
 * - Revenue reports by date range
 * - Payment status reports
 * </p>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
public interface ExcelService {

    /**
     * Generates an Excel report containing all invoices within a date range.
     *
     * @param startDate the start date for the report (inclusive)
     * @param endDate   the end date for the report (inclusive)
     * @return byte array containing the Excel file data
     * @throws IOException if there's an error generating the Excel file
     */
    byte[] generateInvoiceReport(LocalDate startDate, LocalDate endDate) throws IOException;

    /**
     * Generates an Excel report summarizing billing by client.
     *
     * @param startDate the start date for the report (inclusive)
     * @param endDate   the end date for the report (inclusive)
     * @return byte array containing the Excel file data
     * @throws IOException if there's an error generating the Excel file
     */
    byte[] generateClientBillingReport(LocalDate startDate, LocalDate endDate) throws IOException;

    /**
     * Generates an Excel report of payment statuses.
     *
     * @param startDate the start date for the report (inclusive)
     * @param endDate   the end date for the report (inclusive)
     * @return byte array containing the Excel file data
     * @throws IOException if there's an error generating the Excel file
     */
    byte[] generatePaymentStatusReport(LocalDate startDate, LocalDate endDate) throws IOException;

    /**
     * Generates a detailed Excel report for a specific invoice.
     *
     * @param invoiceNumber the invoice number to generate the report for
     * @return byte array containing the Excel file data
     * @throws IOException if there's an error generating the Excel file
     */
    byte[] generateInvoiceDetailReport(String invoiceNumber) throws IOException;
}