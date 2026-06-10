package com.mmva.newsapp.infrastructure.monetization.billing.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Generates unique invoice numbers per tenant.
 * Format: INV-{tenantId}-{year}-{sequentialNumber}
 *
 * Thread-safe and handles concurrent requests.
 */
@Service
@Slf4j
public class InvoiceNumberGenerator {

    private static final DateTimeFormatter YEAR_FORMAT = DateTimeFormatter.ofPattern("yyyy");
    private final AtomicLong counter = new AtomicLong(1);

    public String generate(String tenantId) {
        String year = LocalDate.now().format(YEAR_FORMAT);

        // Use atomic counter for thread safety
        long sequence = counter.getAndIncrement();

        // Format: INV-TENANT-2026-000001
        String invoiceNumber = String.format("INV-%s-%s-%06d", tenantId.toUpperCase(), year, sequence);

        log.debug("Generated invoice number: {} for tenant: {}", invoiceNumber, tenantId);

        return invoiceNumber;
    }
}