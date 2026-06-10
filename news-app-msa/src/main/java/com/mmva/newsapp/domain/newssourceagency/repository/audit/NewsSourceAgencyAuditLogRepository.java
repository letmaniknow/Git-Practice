package com.mmva.newsapp.domain.newssourceagency.repository.audit;

import com.mmva.newsapp.domain.newssourceagency.model.audit.NewsSourceAgencyAuditLog;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for news source agency audit log operations.
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Repository
public interface NewsSourceAgencyAuditLogRepository extends JpaRepository<NewsSourceAgencyAuditLog, Long> {

    /**
     * Finds all audit logs for an agency, ordered by creation date descending.
     *
     * @param agencyId the UUID of the agency
     * @return list of audit logs
     */
    List<NewsSourceAgencyAuditLog> findByAgencyIdOrderByCreatedAtDesc(UUID agencyId);

    /**
     * Finds paginated audit logs for an agency, ordered by creation date
     * descending.
     *
     * @param agencyId the UUID of the agency
     * @param pageable pagination parameters
     * @return page of audit logs
     */
    Page<NewsSourceAgencyAuditLog> findByAgencyIdOrderByCreatedAtDesc(UUID agencyId, Pageable pageable);
}
