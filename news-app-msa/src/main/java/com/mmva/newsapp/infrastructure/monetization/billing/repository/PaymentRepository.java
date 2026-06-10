package com.mmva.newsapp.infrastructure.monetization.billing.repository;

import com.mmva.newsapp.infrastructure.monetization.billing.model.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Payment entity operations.
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    /**
     * Find payments by invoice ID
     */
    List<Payment> findByInvoiceIdOrderByCreatedAtDesc(UUID invoiceId);

    /**
     * Find payments by client ID
     */
    Page<Payment> findByClientIdOrderByCreatedAtDesc(String clientId, Pageable pageable);

    /**
     * Find payments by tenant
     */
    Page<Payment> findByTenantIdOrderByCreatedAtDesc(String tenantId, Pageable pageable);

    /**
     * Get total payments for date range
     */
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.processedAt BETWEEN :startDate AND :endDate AND p.tenantId = :tenantId")
    Optional<Double> getTotalPaymentsForPeriod(@Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("tenantId") String tenantId);

    /**
     * Find payments by gateway transaction ID
     */
    Optional<Payment> findByGatewayTransactionId(String gatewayTransactionId);
}