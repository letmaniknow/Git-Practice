package com.mmva.newsapp.infrastructure.monetization.billing.service;

import com.mmva.newsapp.infrastructure.monetization.billing.model.Payment;
import com.mmva.newsapp.infrastructure.monetization.billing.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for payment operations.
 */
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public Payment save(Payment payment) {
        return paymentRepository.save(payment);
    }

    public List<Payment> findByInvoiceId(UUID invoiceId) {
        return paymentRepository.findByInvoiceIdOrderByCreatedAtDesc(invoiceId);
    }

    public Page<Payment> findByClientId(String clientId, Pageable pageable) {
        return paymentRepository.findByClientIdOrderByCreatedAtDesc(clientId, pageable);
    }

    public Page<Payment> findByTenantId(String tenantId, Pageable pageable) {
        return paymentRepository.findByTenantIdOrderByCreatedAtDesc(tenantId, pageable);
    }

    public Optional<Double> getTotalPaymentsForPeriod(LocalDate startDate, LocalDate endDate, String tenantId) {
        return paymentRepository.getTotalPaymentsForPeriod(startDate, endDate, tenantId);
    }
}