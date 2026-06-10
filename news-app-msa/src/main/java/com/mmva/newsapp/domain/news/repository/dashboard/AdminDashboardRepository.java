package com.mmva.newsapp.domain.news.repository.dashboard;

import com.mmva.newsapp.domain.news.model.core.NewsMasterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repository interface for Admin Dashboard queries
 * Provides methods for aggregating dashboard metrics
 * 
 * @author MMVA Team
 * @version 1.0
 * @since 2026-05-10
 */
@Repository
public interface AdminDashboardRepository extends JpaRepository<NewsMasterEntity, UUID> {

    /**
     * Count total active articles (not deleted)
     * 
     * @return count of active articles
     */
    @Query("SELECT COUNT(n) FROM NewsMasterEntity n WHERE n.deletedAt IS NULL")
    Long countActiveArticles();

    /**
     * Count draft articles
     * 
     * @return count of draft articles
     */
    @Query("SELECT COUNT(n) FROM NewsMasterEntity n " +
           "WHERE n.newsWorkflowStatus = 'DRAFT' " +
           "AND n.deletedAt IS NULL")
    Long countDraftArticles();

    /**
     * Count archived/deleted articles
     * 
     * @return count of archived articles
     */
    @Query("SELECT COUNT(n) FROM NewsMasterEntity n WHERE n.deletedAt IS NOT NULL")
    Long countArchivedArticles();

    /**
     * Sum total page views across all articles
     * 
     * @return total page views
     */
    @Query("SELECT COALESCE(SUM(n.newsViewCount), 0) FROM NewsMasterEntity n WHERE n.deletedAt IS NULL")
    Long sumTotalPageViews();

    /**
     * Sum total engagement (likes + shares + comments + bookmarks)
     * 
     * @return total engagement count
     */
    @Query("SELECT COALESCE(SUM(n.newsLikeCount + n.newsShareCount + n.newsCommentCount + n.newsBookmarkCount), 0) " +
           "FROM NewsMasterEntity n WHERE n.deletedAt IS NULL")
    Long sumTotalEngagement();
}
