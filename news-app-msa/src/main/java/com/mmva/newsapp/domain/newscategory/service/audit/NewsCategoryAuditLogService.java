package com.mmva.newsapp.domain.newscategory.service.audit;

import com.mmva.newsapp.domain.newscategory.dto.audit.NewsCategoryAuditLogDto;
import com.mmva.newsapp.domain.newscategory.enums.core.NewsCategoryAuditAction;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for managing news category audit logs.
 * Provides operations for logging and retrieving category change history.
 *
 * @author MMVA Team
 * @since 1.0.0
 */
public interface NewsCategoryAuditLogService {

    /**
     * Logs an action performed on a news category.
     *
     * @param categoryId the UUID of the news category
     * @param action     the action performed (from enum)
     * @param details    additional details about the action
     * @param createdBy  the UUID of the user who performed the action
     */
    void logAction(UUID categoryId, NewsCategoryAuditAction action, String details, UUID createdBy);

    /**
     * Retrieves all audit logs for a specific news category.
     *
     * @param categoryId the UUID of the news category
     * @return list of audit log DTOs for the category
     */
    List<NewsCategoryAuditLogDto> findByCategoryId(UUID categoryId);

    /**
     * Retrieves paginated audit logs for a specific news category.
     *
     * @param categoryId the UUID of the news category
     * @param pageable   pagination parameters
     * @return paginated audit log DTOs
     */
    Page<NewsCategoryAuditLogDto> findByCategoryId(UUID categoryId, Pageable pageable);
}
