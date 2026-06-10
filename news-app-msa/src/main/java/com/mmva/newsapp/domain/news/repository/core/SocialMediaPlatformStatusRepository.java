package com.mmva.newsapp.domain.news.repository.core;

// ===============================
// Core Java Imports
// ===============================
import java.util.List;
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
import com.mmva.newsapp.domain.news.model.core.SocialMediaPlatformStatusEntity;

/**
 * Repository for Social Media Platform Status entities.
 * Provides database operations for individual platform status tracking.
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Repository
public interface SocialMediaPlatformStatusRepository extends JpaRepository<SocialMediaPlatformStatusEntity, UUID> {

    /**
     * Find all platform statuses for a sharing record.
     */
    List<SocialMediaPlatformStatusEntity> findBySharing_SharingId(UUID sharingId);

    /**
     * Find platform status by sharing ID and platform.
     */
    SocialMediaPlatformStatusEntity findBySharing_SharingIdAndPlatform(UUID sharingId, String platform);

    /**
     * Find all platform statuses for multiple sharing records.
     */
    @Query("SELECT p FROM SocialMediaPlatformStatusEntity p WHERE p.sharing.sharingId IN :sharingIds")
    List<SocialMediaPlatformStatusEntity> findBySharingIds(@Param("sharingIds") List<UUID> sharingIds);

    /**
     * Count completed platforms for a sharing record.
     */
    @Query("SELECT COUNT(p) FROM SocialMediaPlatformStatusEntity p WHERE p.sharing.sharingId = :sharingId AND p.status = 'COMPLETED'")
    long countCompletedBySharingId(@Param("sharingId") UUID sharingId);

    /**
     * Find platforms that are not completed for a sharing record.
     */
    @Query("SELECT p FROM SocialMediaPlatformStatusEntity p WHERE p.sharing.sharingId = :sharingId AND p.status != 'COMPLETED'")
    List<SocialMediaPlatformStatusEntity> findPendingBySharingId(@Param("sharingId") UUID sharingId);

    /**
     * Find all completed platform statuses.
     */
    @Query("SELECT p FROM SocialMediaPlatformStatusEntity p WHERE p.status = 'COMPLETED'")
    List<SocialMediaPlatformStatusEntity> findAllCompleted();

    /**
     * Find platform statuses by platform name.
     */
    List<SocialMediaPlatformStatusEntity> findByPlatform(String platform);

    /**
     * Find platform statuses by status value.
     */
    List<SocialMediaPlatformStatusEntity> findByStatus(String status);
}