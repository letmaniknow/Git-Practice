package com.mmva.newsapp.domain.newsengagement.comments.repository;

import com.mmva.newsapp.domain.newsengagement.comments.enums.NewsCommentStatus;
import com.mmva.newsapp.domain.newsengagement.comments.model.NewsComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Repository for NewsComment entity.
 * 
 * <p>
 * Table: news_comments
 * </p>
 * <p>
 * Naming convention: {tableName}_{fieldName}
 * </p>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Repository
public interface NewsCommentRepository extends JpaRepository<NewsComment, UUID>, JpaSpecificationExecutor<NewsComment> {
        List<NewsComment> findByNewsCommentsParentIdOrderByNewsCommentsCommentedAtAsc(UUID parentId);

        List<NewsComment> findByNewsCommentsNewsId(UUID newsId);

        List<NewsComment> findByNewsCommentsUserId(UUID userId);

        @Query("SELECT COUNT(nc) FROM NewsComment nc WHERE nc.deletedAt IS NULL AND nc.newsCommentsNewsId = :newsId")
        Long countCommentsByNewsCommentsNewsId(@Param("newsId") UUID newsId);

        @Query("SELECT nc FROM NewsComment nc WHERE nc.deletedAt IS NULL AND nc.newsCommentsNewsId = :newsId ORDER BY nc.newsCommentsCommentedAt DESC")
        List<NewsComment> findByNewsCommentsNewsIdOrderByNewsCommentsCommentedAtDesc(@Param("newsId") UUID newsId);

        @Query("SELECT nc FROM NewsComment nc WHERE nc.deletedAt IS NULL AND nc.newsCommentsNewsId = :newsId ORDER BY nc.newsCommentsCommentedAt DESC")
        Page<NewsComment> findByNewsCommentsNewsIdOrderByNewsCommentsCommentedAtDesc(@Param("newsId") UUID newsId,
                        Pageable pageable);

        // =============================================
        // PUBLIC API - Only APPROVED comments
        // =============================================

        @Query("SELECT nc FROM NewsComment nc WHERE nc.deletedAt IS NULL AND nc.newsCommentsNewsId = :newsId AND nc.newsCommentsStatus = 'APPROVED' ORDER BY nc.newsCommentsCommentedAt DESC")
        List<NewsComment> findApprovedByNewsCommentsNewsId(@Param("newsId") UUID newsId);

        @Query("SELECT nc FROM NewsComment nc WHERE nc.deletedAt IS NULL AND nc.newsCommentsNewsId = :newsId AND nc.newsCommentsStatus = 'APPROVED' ORDER BY nc.newsCommentsCommentedAt DESC")
        Page<NewsComment> findApprovedByNewsCommentsNewsId(@Param("newsId") UUID newsId, Pageable pageable);

        @Query("SELECT COUNT(nc) FROM NewsComment nc WHERE nc.deletedAt IS NULL AND nc.newsCommentsNewsId = :newsId AND nc.newsCommentsStatus = 'APPROVED'")
        Long countApprovedByNewsCommentsNewsId(@Param("newsId") UUID newsId);

        @Query("SELECT nc FROM NewsComment nc WHERE nc.deletedAt IS NULL AND nc.newsCommentsParentId = :parentId AND nc.newsCommentsStatus = 'APPROVED' ORDER BY nc.newsCommentsCommentedAt ASC")
        List<NewsComment> findApprovedReplies(@Param("parentId") UUID parentId);

        // =============================================
        // USER API - User's own comments (excludes DELETED)
        // =============================================

        @Query("SELECT nc FROM NewsComment nc WHERE nc.deletedAt IS NULL AND nc.newsCommentsUserId = :userId AND nc.newsCommentsStatus <> 'DELETED' ORDER BY nc.createdAt DESC")
        List<NewsComment> findByNewsCommentsUserIdOrderByCreatedAtDesc(@Param("userId") UUID userId);

        @Query("SELECT nc FROM NewsComment nc WHERE nc.deletedAt IS NULL AND nc.newsCommentsUserId = :userId AND nc.newsCommentsStatus <> 'DELETED' ORDER BY nc.createdAt DESC")
        Page<NewsComment> findByNewsCommentsUserIdOrderByCreatedAtDesc(@Param("userId") UUID userId, Pageable pageable);

        // =============================================
        // ADMIN API - Filter by status, newsId, userId
        // =============================================

        @Query("SELECT nc FROM NewsComment nc WHERE nc.deletedAt IS NULL AND nc.newsCommentsStatus = :status ORDER BY nc.createdAt DESC")
        Page<NewsComment> findByNewsCommentsStatus(@Param("status") NewsCommentStatus status, Pageable pageable);

        @Query("SELECT nc FROM NewsComment nc WHERE nc.deletedAt IS NULL AND nc.newsCommentsNewsId = :newsId AND nc.newsCommentsStatus = :status ORDER BY nc.createdAt DESC")
        Page<NewsComment> findByNewsCommentsNewsIdAndNewsCommentsStatus(@Param("newsId") UUID newsId,
                        @Param("status") NewsCommentStatus status,
                        Pageable pageable);

        @Query("SELECT nc FROM NewsComment nc WHERE nc.deletedAt IS NULL AND nc.newsCommentsUserId = :userId AND nc.newsCommentsStatus = :status ORDER BY nc.createdAt DESC")
        Page<NewsComment> findByNewsCommentsUserIdAndNewsCommentsStatus(@Param("userId") UUID userId,
                        @Param("status") NewsCommentStatus status,
                        Pageable pageable);

        @Query("SELECT nc FROM NewsComment nc WHERE nc.deletedAt IS NULL AND " +
                        "(:status IS NULL OR nc.newsCommentsStatus = :status) AND " +
                        "(:newsId IS NULL OR nc.newsCommentsNewsId = :newsId) AND " +
                        "(:userId IS NULL OR nc.newsCommentsUserId = :userId) " +
                        "ORDER BY nc.createdAt DESC")
        Page<NewsComment> findWithFilters(
                        @Param("status") NewsCommentStatus status,
                        @Param("newsId") UUID newsId,
                        @Param("userId") UUID userId,
                        Pageable pageable);

        @Query("SELECT COUNT(nc) FROM NewsComment nc WHERE nc.deletedAt IS NULL AND nc.newsCommentsCommentedAt BETWEEN :start AND :end")
        Long countCommentsBetween(@Param("start") Instant start, @Param("end") Instant end);
}
