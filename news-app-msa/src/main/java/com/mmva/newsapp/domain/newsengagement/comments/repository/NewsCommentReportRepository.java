package com.mmva.newsapp.domain.newsengagement.comments.repository;

import com.mmva.newsapp.domain.newsengagement.comments.model.NewsCommentReport;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

/**
 * Repository for {@link NewsCommentReport} entity operations.
 *
 * <p>
 * Follows PROJECT_PRINCIPLES.md field naming convention using
 * entity field names with domain prefix.
 * </p>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
public interface NewsCommentReportRepository extends JpaRepository<NewsCommentReport, UUID> {

    long countByNewsCommentReportsCommentId(UUID commentId);
}
