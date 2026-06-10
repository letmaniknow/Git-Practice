package com.mmva.newsapp.domain.news.repository.core;

// ===============================
// Core Java Imports
// ===============================
import java.util.List;
import java.util.Optional;
import java.util.UUID;

// ===============================
// Spring Data JPA Imports
// ===============================
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

// ===============================
// Project Imports
// ===============================
import com.mmva.newsapp.domain.news.model.core.SocialMediaShareEntity;

/**
 * Repository for Social Media Share entities.
 * Provides database operations for social media sharing tracking.
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Repository
public interface SocialMediaShareRepository extends JpaRepository<SocialMediaShareEntity, UUID> {

    /**
     * Find sharing record by news ID.
     */
    Optional<SocialMediaShareEntity> findByNewsId(UUID newsId);

    /**
     * Find all sharing records that are enabled.
     * Fetches with platform statuses for complete information.
     */
    @Query("SELECT s FROM SocialMediaShareEntity s LEFT JOIN FETCH s.platformStatuses WHERE s.sharingEnabled = true")
    List<SocialMediaShareEntity> findEnabledSharingRecords();

    /**
     * Find sharing records by multiple news IDs.
     * Fetches with platform statuses.
     */
    @Query("SELECT s FROM SocialMediaShareEntity s LEFT JOIN FETCH s.platformStatuses WHERE s.newsId IN :newsIds")
    List<SocialMediaShareEntity> findByNewsIds(@Param("newsIds") List<UUID> newsIds);

    /**
     * Find sharing records that are not fully shared (have pending platforms).
     * Fetches with platform statuses.
     */
    @Query("""
                SELECT DISTINCT s FROM SocialMediaShareEntity s
                LEFT JOIN FETCH s.platformStatuses
                WHERE s.sharingEnabled = true
                AND EXISTS (
                    SELECT p FROM SocialMediaPlatformStatusEntity p
                    WHERE p.sharing = s
                    AND p.status != 'COMPLETED'
                )
            """)
    List<SocialMediaShareEntity> findIncompleteSharingRecords();

    /**
     * Check if a news article has a sharing record.
     */
    boolean existsByNewsId(UUID newsId);

    /**
     * Delete sharing record by news ID.
     */
    void deleteByNewsId(UUID newsId);
}