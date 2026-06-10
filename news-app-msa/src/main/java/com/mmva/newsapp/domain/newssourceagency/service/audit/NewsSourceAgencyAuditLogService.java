package com.mmva.newsapp.domain.newssourceagency.service.audit;

import com.mmva.newsapp.domain.newssourceagency.dto.audit.NewsSourceAgencyAuditLogDto;
import com.mmva.newsapp.domain.newssourceagency.enums.core.NewsSourceAgencyAuditAction;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for managing news source agency audit logs.
 * Provides operations for logging and retrieving agency change history.
 *
 * @author MMVA Team
 * @since 1.0.0
 */
public interface NewsSourceAgencyAuditLogService {

    /**
     * Logs an action performed on a news source agency.
     *
     * @param agencyId  the UUID of the agency
     * @param action    the action performed (from enum)
     * @param details   additional details about the action
     * @param createdBy the UUID of the user who performed the action
     */
    void logAction(UUID agencyId, NewsSourceAgencyAuditAction action, String details, UUID createdBy);

    /**
     * Retrieves all audit logs for a specific agency.
     *
     * @param agencyId the UUID of the agency
     * @return list of audit log DTOs for the agency
     */
    List<NewsSourceAgencyAuditLogDto> findByAgencyId(UUID agencyId);

    /**
     * Retrieves paginated audit logs for a specific agency.
     *
     * @param agencyId the UUID of the agency
     * @param pageable pagination parameters
     * @return paginated audit log DTOs
     */
    Page<NewsSourceAgencyAuditLogDto> findByAgencyId(UUID agencyId, Pageable pageable);
}
