package com.mmva.newsapp.infrastructure.monetization.billing.repository;

import com.mmva.newsapp.infrastructure.monetization.billing.model.InvoiceLineItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for InvoiceLineItem entity operations.
 */
@Repository
public interface InvoiceLineItemRepository extends JpaRepository<InvoiceLineItem, UUID> {

    /**
     * Find all line items for an invoice
     */
    List<InvoiceLineItem> findByInvoiceIdOrderByLineNumber(UUID invoiceId);

    /**
     * Find line items by tenant
     */
    List<InvoiceLineItem> findByTenantIdOrderByCreatedAtDesc(String tenantId);

    /**
     * Find line items by reference ID (e.g., subscription ID, campaign ID)
     */
    List<InvoiceLineItem> findByReferenceId(String referenceId);

    /**
     * Delete all line items for an invoice
     */
    void deleteByInvoiceId(UUID invoiceId);

    /**
     * Count line items for an invoice
     */
    long countByInvoiceId(UUID invoiceId);

    /**
     * Get total amount for invoice line items
     */
    @Query("SELECT SUM(i.finalAmount) FROM InvoiceLineItem i WHERE i.invoiceId = :invoiceId AND i.deletedAt IS NULL")
    Double getTotalAmountForInvoice(@Param("invoiceId") UUID invoiceId);
}