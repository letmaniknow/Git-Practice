package com.mmva.newsapp.domain.newscategory.repository.audit;

import com.mmva.newsapp.domain.newscategory.model.audit.NewsCategoryAuditLog;
import com.mmva.newsapp.infrastructure.common.audit.repository.UnifiedAuditLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repository for news category audit log operations.
 * Uses unified audit pattern with BaseAuditLogEntity for consistency.
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Repository
public interface NewsCategoryAuditLogRepository extends UnifiedAuditLogRepository<NewsCategoryAuditLog> {

        /**
         * Finds paginated audit logs for a category, ordered by creation date
         * descending.
         *
         * @param categoryId the UUID of the category
         * @param pageable   pagination parameters
         * @return page of audit logs
         */
        Page<NewsCategoryAuditLog> findByNewsCategoryIdOrderByCreatedAtDesc(UUID categoryId, Pageable pageable);
}
