package com.mmva.newsapp.infrastructure.storage.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.UUID;

import com.mmva.newsapp.infrastructure.common.audit.model.BaseAuditEntity;

/**
 * Media Upload Entity - Tracks all media uploads for analytics.
 * 
 * <p>
 * Golden Rules Implementation:
 * <ol>
 * <li>Every upload is tracked IMMEDIATELY upon receipt</li>
 * <li>Original file path is ALWAYS preserved</li>
 * <li>Status transitions are logged for audit trail</li>
 * <li>Analytics-ready with full metadata capture</li>
 * </ol>
 * 
 * <p>
 * Status Flow:
 * 
 * <pre>
 * RECEIVED → VALIDATING → PROCESSING → PUBLISHED
 *                ↓              ↓
 *         VALIDATION_FAILED  PROCESSING_FAILED
 *                ↓              ↓
 *           QUARANTINED    QUARANTINED
 * </pre>
 * 
 * @author TheNews Team
 * @since 1.0.0
 */
@Data
@SuperBuilder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table(name = "media_uploads", indexes = {
        @Index(name = "idx_media_entity_type", columnList = "entity_type"),
        @Index(name = "idx_media_entity_id", columnList = "entity_id"),
        @Index(name = "idx_media_status", columnList = "status"),
        @Index(name = "idx_media_upload_date", columnList = "upload_timestamp"),
        @Index(name = "idx_media_mime_type", columnList = "original_mime_type"),
        @Index(name = "idx_media_uploaded_by", columnList = "uploaded_by")
})
public class MediaUpload extends BaseAuditEntity {

    // =========================================================================
    // PRIMARY IDENTIFIER
    // =========================================================================

    /**
     * Manually generated UUID - set before save.
     * Not using @GeneratedValue because we need the ID for filename generation
     * before persisting to database.
     */
    @Id
    @Column(name = "upload_id", updatable = false, nullable = false)
    private UUID uploadId;

    // =========================================================================
    // ENTITY ASSOCIATION
    // =========================================================================

    /**
     * Entity type: NEWS, USER, ADMIN, AD
     */
    @Column(name = "entity_type", length = 50, nullable = false)
    @Enumerated(EnumType.STRING)
    private EntityType entityType;

    /**
     * The ID of the associated entity (news_id, user_id, etc.)
     * Null until entity is created (for pre-upload scenarios)
     */
    @Column(name = "entity_id", length = 100)
    private String entityId;

    /**
     * Sub-type for more granular categorization
     * e.g., THUMBNAIL, AVATAR, BANNER, VIDEO
     */
    @Column(name = "media_type", length = 50)
    @Enumerated(EnumType.STRING)
    private MediaType mediaType;

    // =========================================================================
    // ORIGINAL FILE INFO (PRESERVED - NEVER MODIFIED)
    // =========================================================================

    /**
     * Original filename as uploaded by user
     */
    @Column(name = "original_filename", length = 500, nullable = false)
    private String originalFilename;

    /**
     * Full path to the original file in storage
     */
    @Column(name = "original_path", length = 1000, nullable = false)
    private String originalPath;

    /**
     * File size in bytes
     */
    @Column(name = "original_size_bytes", nullable = false)
    private Long originalSizeBytes;

    /**
     * MIME type (e.g., image/jpeg, video/mp4)
     */
    @Column(name = "original_mime_type", length = 100, nullable = false)
    private String originalMimeType;

    /**
     * File hash for deduplication and integrity
     */
    @Column(name = "file_hash", length = 64)
    private String fileHash;

    // =========================================================================
    // PROCESSED FILE INFO
    // =========================================================================

    /**
     * Generated filename after processing
     */
    @Column(name = "processed_filename", length = 500)
    private String processedFilename;

    /**
     * Full path to the processed file
     */
    @Column(name = "processed_path", length = 1000)
    private String processedPath;

    /**
     * Processed file size in bytes
     */
    @Column(name = "processed_size_bytes")
    private Long processedSizeBytes;

    /**
     * Public URL for accessing the processed file
     */
    @Column(name = "public_url", length = 1000)
    private String publicUrl;

    // =========================================================================
    // TRACKING & STATUS
    // =========================================================================

    /**
     * Timestamp when file was received
     */
    @Column(name = "upload_timestamp", nullable = false)
    private Instant uploadTimestamp;

    /**
     * Timestamp when processing completed
     */
    @Column(name = "processed_timestamp")
    private Instant processedTimestamp;

    /**
     * Current status of the upload
     */
    @Column(name = "status", length = 50, nullable = false)
    @Enumerated(EnumType.STRING)
    private UploadStatus status;

    /**
     * Reason for failure (if status is failed)
     */
    @Column(name = "failure_reason")
    private String failureReason;

    /**
     * Number of processing retry attempts
     */
    @Column(name = "retry_count")
    @Builder.Default
    private Integer retryCount = 0;

    /**
     * Path if file was moved to quarantine
     */
    @Column(name = "quarantine_path", length = 1000)
    private String quarantinePath;

    // =========================================================================
    // ANALYTICS & CONTEXT
    // =========================================================================

    /**
     * User who uploaded the file
     */
    @Column(name = "uploaded_by", length = 100)
    private String uploadedBy;

    /**
     * IP address of uploader
     */
    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    /**
     * User agent string
     */
    @Column(name = "user_agent", length = 500)
    private String userAgent;

    /**
     * Correlation ID for request tracing
     */
    @Column(name = "correlation_id", length = 50)
    private String correlationId;

    // =========================================================================
    // IMAGE METADATA (For analytics)
    // =========================================================================

    /**
     * Original image width (for images only)
     */
    @Column(name = "image_width")
    private Integer imageWidth;

    /**
     * Original image height (for images only)
     */
    @Column(name = "image_height")
    private Integer imageHeight;

    /**
     * Duration in seconds (for videos only)
     */
    @Column(name = "video_duration_seconds")
    private Integer videoDurationSeconds;

    // =========================================================================
    // ENUMS
    // =========================================================================

    public enum EntityType {
        NEWS,
        USER,
        ADMIN,
        AD
    }

    public enum MediaType {
        // News related
        NEWS_IMAGE,
        NEWS_VIDEO,
        NEWS_THUMBNAIL,

        // User related
        USER_AVATAR,

        // Admin related
        ADMIN_AVATAR,

        // Ads related
        AD_BANNER,
        AD_VIDEO,

        // Generic
        DOCUMENT,
        OTHER
    }

    public enum UploadStatus {
        /**
         * File received and saved to originals folder
         */
        RECEIVED,

        /**
         * Validation in progress (type, size, virus scan)
         */
        VALIDATING,

        /**
         * Validation failed - file moved to quarantine
         */
        VALIDATION_FAILED,

        /**
         * Processing in progress (resize, optimize, transcode)
         */
        PROCESSING,

        /**
         * Processing failed - will be retried
         */
        PROCESSING_FAILED,

        /**
         * Successfully processed and available for use
         */
        PUBLISHED,

        /**
         * Moved to quarantine (security or repeated failures)
         */
        QUARANTINED,

        /**
         * Soft deleted (entity was deleted)
         */
        ARCHIVED
    }

    // =========================================================================
    // LIFECYCLE CALLBACKS
    // =========================================================================

    @PrePersist
    protected void onCreate() {
        if (uploadTimestamp == null) {
            uploadTimestamp = Instant.now();
        }
        if (status == null) {
            status = UploadStatus.RECEIVED;
        }
        if (retryCount == null) {
            retryCount = 0;
        }
    }
}
