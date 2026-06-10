package com.mmva.newsapp.infrastructure.monetization.billing.repository;

import com.mmva.newsapp.infrastructure.monetization.billing.enums.InvoiceStatus;
import com.mmva.newsapp.infrastructure.monetization.billing.enums.TransactionType;
import com.mmva.newsapp.infrastructure.monetization.billing.model.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Invoice entity operations.
 */
@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

    /**
     * Find invoice by invoice number
     */
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    /**
     * Find invoices by client ID
     */
    Page<Invoice> findByClientIdOrderByCreatedAtDesc(String clientId, Pageable pageable);

    /**
     * Find invoices by status
     */
    Page<Invoice> findByStatusOrderByCreatedAtDesc(InvoiceStatus status, Pageable pageable);

    /**
     * Find overdue invoices
     */
    @Query("SELECT i FROM Invoice i WHERE i.status != 'PAID' AND i.dueDate < :currentDate AND i.deletedAt IS NULL")
    List<Invoice> findOverdueInvoices(@Param("currentDate") LocalDate currentDate);

    /**
     * Find invoices by tenant
     */
    Page<Invoice> findByTenantIdOrderByCreatedAtDesc(String tenantId, Pageable pageable);

    /**
     * Find invoices by date range
     */
    List<Invoice> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find invoices by transaction type
     */
    Page<Invoice> findByTransactionTypeOrderByCreatedAtDesc(TransactionType transactionType, Pageable pageable);

    /**
     * Get total revenue for date range
     */
    @Query("SELECT SUM(i.totalAmount) FROM Invoice i WHERE i.status = 'PAID' AND i.paymentDate BETWEEN :startDate AND :endDate AND i.tenantId = :tenantId")
    Optional<Double> getTotalRevenueForPeriod(@Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("tenantId") String tenantId);

    /**
     * Count active invoices (not paid, not cancelled)
     */
    @Query("SELECT COUNT(i) FROM Invoice i WHERE i.status NOT IN ('PAID', 'CANCELLED') AND i.deletedAt IS NULL AND i.tenantId = :tenantId")
    long countActiveInvoices(@Param("tenantId") String tenantId);
}