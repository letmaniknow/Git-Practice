package com.mmva.newsapp.infrastructure.storage.repository;

import com.mmva.newsapp.infrastructure.storage.model.MediaUpload;
import com.mmva.newsapp.infrastructure.storage.model.MediaUpload.EntityType;
import com.mmva.newsapp.infrastructure.storage.model.MediaUpload.MediaType;
import com.mmva.newsapp.infrastructure.storage.model.MediaUpload.UploadStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for MediaUpload entity.
 * 
 * <p>
 * Provides data access for media upload tracking and analytics.
 * 
 * @author TheNews Team
 * @since 1.0.0
 */
@Repository
public interface MediaUploadRepository extends JpaRepository<MediaUpload, UUID> {

    // =========================================================================
    // FIND BY ENTITY
    // =========================================================================

    /**
     * Find all uploads for a specific entity
     */
    List<MediaUpload> findByEntityTypeAndEntityId(EntityType entityType, String entityId);

    /**
     * Find the latest upload for an entity
     */
    Optional<MediaUpload> findTopByEntityTypeAndEntityIdOrderByUploadTimestampDesc(
            EntityType entityType, String entityId);

    /**
     * Find all uploads by entity type
     */
    Page<MediaUpload> findByEntityType(EntityType entityType, Pageable pageable);

    // =========================================================================
    // FIND BY STATUS
    // =========================================================================

    /**
     * Find uploads by status
     */
    List<MediaUpload> findByStatus(UploadStatus status);

    /**
     * Find uploads by status with pagination
     */
    Page<MediaUpload> findByStatus(UploadStatus status, Pageable pageable);

    /**
     * Find failed uploads for retry
     */
    @Query("SELECT m FROM MediaUpload m WHERE m.status = :status AND m.retryCount < :maxRetries")
    List<MediaUpload> findFailedForRetry(
            @Param("status") UploadStatus status,
            @Param("maxRetries") int maxRetries);

    /**
     * Find uploads pending processing
     */
    @Query("SELECT m FROM MediaUpload m WHERE m.status IN :statuses ORDER BY m.uploadTimestamp ASC")
    List<MediaUpload> findPendingProcessing(@Param("statuses") List<UploadStatus> statuses);

    // =========================================================================
    // ANALYTICS QUERIES
    // =========================================================================

    /**
     * Count uploads by entity type
     */
    long countByEntityType(EntityType entityType);

    /**
     * Count uploads by status
     */
    long countByStatus(UploadStatus status);

    /**
     * Count uploads in date range
     */
    @Query("SELECT COUNT(m) FROM MediaUpload m WHERE m.uploadTimestamp BETWEEN :start AND :end")
    long countByDateRange(@Param("start") Instant start, @Param("end") Instant end);

    /**
     * Get total storage used by entity type
     */
    @Query("SELECT COALESCE(SUM(m.originalSizeBytes), 0) FROM MediaUpload m WHERE m.entityType = :entityType")
    Long getTotalStorageByEntityType(@Param("entityType") EntityType entityType);

    /**
     * Get upload statistics by date
     */
    @Query("""
            SELECT DATE(m.uploadTimestamp) as date,
                   COUNT(m) as count,
                   SUM(m.originalSizeBytes) as totalBytes
            FROM MediaUpload m
            WHERE m.uploadTimestamp BETWEEN :start AND :end
            GROUP BY DATE(m.uploadTimestamp)
            ORDER BY DATE(m.uploadTimestamp) DESC
            """)
    List<Object[]> getUploadStatsByDate(
            @Param("start") Instant start,
            @Param("end") Instant end);

    /**
     * Get most common MIME types
     */
    @Query("""
            SELECT m.originalMimeType, COUNT(m) as count
            FROM MediaUpload m
            GROUP BY m.originalMimeType
            ORDER BY count DESC
            """)
    List<Object[]> getMimeTypeDistribution();

    /**
     * Get uploads by user
     */
    Page<MediaUpload> findByUploadedBy(String uploadedBy, Pageable pageable);

    // =========================================================================
    // CLEANUP QUERIES
    // =========================================================================

    /**
     * Find quarantined uploads older than specified date
     */
    @Query("SELECT m FROM MediaUpload m WHERE m.status = 'QUARANTINED' AND m.uploadTimestamp < :before")
    List<MediaUpload> findOldQuarantinedUploads(@Param("before") Instant before);

    /**
     * Find archived uploads older than specified date
     */
    @Query("SELECT m FROM MediaUpload m WHERE m.status = 'ARCHIVED' AND m.uploadTimestamp < :before")
    List<MediaUpload> findOldArchivedUploads(@Param("before") Instant before);

    // =========================================================================
    // UPDATE OPERATIONS
    // =========================================================================

    /**
     * Update status of an upload
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE MediaUpload m SET m.status = :status, m.updatedAt = CURRENT_TIMESTAMP WHERE m.uploadId = :uploadId")
    int updateStatus(@Param("uploadId") UUID uploadId, @Param("status") UploadStatus status);

    /**
     * Increment retry count
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE MediaUpload m SET m.retryCount = m.retryCount + 1, m.updatedAt = CURRENT_TIMESTAMP WHERE m.uploadId = :uploadId")
    int incrementRetryCount(@Param("uploadId") UUID uploadId);

    /**
     * Mark upload as published with processed file info
     */
    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE MediaUpload m SET
                m.status = 'PUBLISHED',
                m.processedFilename = :filename,
                m.processedPath = :path,
                m.processedSizeBytes = :size,
                m.publicUrl = :url,
                m.processedTimestamp = CURRENT_TIMESTAMP,
                m.updatedAt = CURRENT_TIMESTAMP
            WHERE m.uploadId = :uploadId
            """)
    int markAsPublished(
            @Param("uploadId") UUID uploadId,
            @Param("filename") String filename,
            @Param("path") String path,
            @Param("size") Long size,
            @Param("url") String url);

    /**
     * Mark upload as failed
     */
    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE MediaUpload m SET
                m.status = :status,
                m.failureReason = :reason,
                m.updatedAt = CURRENT_TIMESTAMP
            WHERE m.uploadId = :uploadId
            """)
    int markAsFailed(
            @Param("uploadId") UUID uploadId,
            @Param("status") UploadStatus status,
            @Param("reason") String reason);

    /**
     * Link upload to entity (direct update to avoid optimistic locking)
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE MediaUpload m SET m.entityId = :entityId, m.updatedAt = CURRENT_TIMESTAMP WHERE m.uploadId = :uploadId")
    int linkToEntity(@Param("uploadId") UUID uploadId, @Param("entityId") String entityId);

    // =========================================================================
    // SEARCH
    // =========================================================================

    /**
     * Find by original filename pattern
     */
    Page<MediaUpload> findByOriginalFilenameContainingIgnoreCase(
            String filename, Pageable pageable);

    /**
     * Find by correlation ID (for request tracing)
     */
    Optional<MediaUpload> findByCorrelationId(String correlationId);

    /**
     * Find by media type
     */
    Page<MediaUpload> findByMediaType(MediaType mediaType, Pageable pageable);
}
