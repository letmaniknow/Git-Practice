package com.mmva.newsapp.infrastructure.monetization.billing.service;

import com.mmva.newsapp.infrastructure.monetization.billing.model.Invoice;
import com.mmva.newsapp.infrastructure.monetization.billing.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for invoice operations.
 */
@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;

    public Invoice save(Invoice invoice) {
        return invoiceRepository.save(invoice);
    }

    public Invoice findById(UUID id) {
        return invoiceRepository.findById(id).orElse(null);
    }

    public Optional<Invoice> findByInvoiceNumber(String invoiceNumber) {
        return invoiceRepository.findByInvoiceNumber(invoiceNumber);
    }

    public Page<Invoice> findByClientId(String clientId, Pageable pageable) {
        return invoiceRepository.findByClientIdOrderByCreatedAtDesc(clientId, pageable);
    }

    public Page<Invoice> findByTenantId(String tenantId, Pageable pageable) {
        return invoiceRepository.findByTenantIdOrderByCreatedAtDesc(tenantId, pageable);
    }

    public List<Invoice> findOverdueInvoices() {
        return invoiceRepository.findOverdueInvoices(LocalDate.now());
    }

    public long countActiveInvoices(String tenantId) {
        return invoiceRepository.countActiveInvoices(tenantId);
    }
}