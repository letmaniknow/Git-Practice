package com.mmva.newsapp.domain.news.service.media;

import org.springframework.web.multipart.MultipartFile;
import com.mmva.newsapp.infrastructure.storage.model.MediaUpload;
import java.io.File;
import java.util.UUID;

/**
 * Service interface for news media file storage operations.
 * ...existing code...
 */
public interface NewsMediaStorageService {
        /**
         * Moves a media variant (main, card, hero, thumbnail) to backup/archive.
         * @param fileName the filename
         * @param size the variant size (main, card, hero, thumbnail)
         */
        void moveMediaVariantToBackup(String fileName, String size);

        // ========================================
        // PRESERVE FIRST - Original File Storage
        // ========================================

        /**
         * STEP 1: Save original file IMMEDIATELY upon receipt.
         * 
         * <p>
         * This is the FIRST operation - before any validation or processing.
         * </p>
         * <p>
         * Creates a tracking record in the database with status=RECEIVED.
         * </p>
         * 
         * <h4>Path Pattern:</h4>
         * 
         * <pre>
         * originals/news/{YYYY}/{MM}/{DD}/{uploadId}_{originalName}
         * </pre>
         *
         * @param file          the uploaded multipart file
         * @param correlationId request correlation ID for tracing
         * @param uploadedBy    username of uploader
         * @return MediaUpload entity with tracking info and original path
         */
        MediaUpload preserveOriginal(MultipartFile file, String correlationId, String uploadedBy);

        /**
         * STEP 1 (Overload): Save original with additional context.
         *
         * @param file          the uploaded multipart file
         * @param entityId      the associated entity ID (if known)
         * @param correlationId request correlation ID for tracing
         * @param uploadedBy    username of uploader
         * @param ipAddress     uploader's IP address
         * @param userAgent     uploader's user agent
         * @return MediaUpload entity with tracking info
         */
        MediaUpload preserveOriginal(MultipartFile file, String entityId,
                        String correlationId, String uploadedBy,
                        String ipAddress, String userAgent);

        /**
         * STEP 1 (Byte Array): Save original file from byte array.
         *
         * @param content          the file content as byte array
         * @param originalFilename the original filename
         * @param contentType      the MIME content type
         * @param size             the file size in bytes
         * @param correlationId    request correlation ID for tracing
         * @param uploadedBy       username of uploader
         * @return MediaUpload entity with tracking info
         */
        MediaUpload preserveOriginal(byte[] content, String originalFilename, String contentType, long size,
                        String correlationId, String uploadedBy);

        // ========================================
        // Filename Generation
        // ========================================

        /**
         * Generates a unique filename for a media file upload.
         * 
         * <p>
         * Pattern: {@code {sanitized-name}_{short-uuid}.{ext}}
         * </p>
         * <p>
         * Example: {@code breaking-news-photo_a1b2c3d4.jpg}
         * </p>
         * 
         * <h4>Sanitization Rules:</h4>
         * <ul>
         * <li>Convert to lowercase</li>
         * <li>Replace special characters with hyphens</li>
         * <li>Limit to 50 characters</li>
         * <li>Append 8-character UUID for uniqueness</li>
         * </ul>
         *
         * @param file the uploaded multipart file
         * @return unique sanitized filename with extension
         */
        String generateMediaFilename(MultipartFile file);

        // ========================================
        // Path Resolution
        // ========================================

        /**
         * Resolves a filename to its full absolute path in processed/images folder.
         *
         * @param filename the filename to resolve
         * @return full absolute path to the file in processed/news/images/
         */
        String resolveFilePath(String filename);

        /**
         * Resolves a path within the originals folder.
         *
         * @param filename the filename
         * @return full path in originals/news/ folder
         */
        String resolveOriginalPath(String filename);

        /**
         * Resolves a path within the processed folder.
         *
         * @param filename the filename
         * @return full path in processed/news/ folder
         */
        String resolveProcessedPath(String filename);

        /**
         * Resolves a path within the processed images folder for a specific size.
         *
         * @param size     the image size (thumbnail, small, medium, large)
         * @param filename the filename
         * @return full path in processed/news/images/{size}/ folder
         */
        String resolveProcessedImagePath(String size, String filename);

        /**
         * Ensures the processed images folder exists for a specific size.
         *
         * @param size the image size
         * @return the path to the size-specific images folder
         */
        String ensureProcessedImagesFolderExists(String size);

        /**
         * Gets the configured media folder path (processed folder).
         *
         * @return the base media folder path
         */
        String getMediaFolderPath();

        /**
         * Gets the configured originals folder path.
         *
         * @return the originals folder path for news
         */
        String getOriginalsFolderPath();

        /**
         * Gets the configured backup/archive folder path.
         *
         * @return the backup folder path
         */
        String getBackupFolderPath();

        // ========================================
        // Folder Management
        // ========================================

        /**
         * Ensures the media folder exists, creating it if necessary.
         * 
         * @throws com.mmva.newsapp.infrastructure.common.exception.InvalidRequestException
         *                                                                                  if
         *                                                                                  folder
         *                                                                                  cannot
         *                                                                                  be
         *                                                                                  created
         */
        void ensureMediaFolderExists();

        /**
         * Ensures the originals folder exists with date-based subdirectory.
         *
         * @return the path to today's originals folder
         */
        String ensureOriginalsFolderExists();

        /**
         * Ensures the backup folder exists, creating it if necessary.
         * 
         * @throws java.io.IOException if folder cannot be created
         */
        void ensureBackupFolderExists() throws java.io.IOException;

        // ========================================
        // Processing Pipeline
        // ========================================

        /**
         * Mark upload as processing started.
         *
         * @param uploadId the upload ID to update
         */
        void markProcessingStarted(UUID uploadId);

        /**
         * Mark upload as successfully published.
         *
         * @param uploadId          the upload ID
         * @param processedFilename the generated filename
         * @param processedPath     the processed file path
         * @param publicUrl         the public access URL
         */
        void markPublished(UUID uploadId, String processedFilename,
                        String processedPath, String publicUrl);

        /**
         * Mark upload as failed.
         *
         * @param uploadId the upload ID
         * @param reason   the failure reason
         */
        void markFailed(UUID uploadId, String reason);

        // ========================================
        // Backup/Archive Operations
        // ========================================

        /**
         * Moves a media file to the archive folder.
         * 
         * <p>
         * Archive filename format: {@code {original}_{yyyyMMdd_HHmmss}.{ext}}
         * </p>
         *
         * @param fileName the media filename (not path)
         */
        void moveMediaToBackup(String fileName);

        /**
         * Moves a thumbnail file to the archive folder.
         *
         * @param thumbnailFilename the thumbnail filename (not URL)
         */
        void moveThumbnailToBackup(String thumbnailFilename);

        /**
         * Generic method to move any file to archive folder.
         *
         * @param filename   the original filename
         * @param sourcePath the full source path of the file
         * @param fileType   descriptive type for logging (e.g., "media", "thumbnail")
         */
        void moveFileToBackup(String filename, String sourcePath, String fileType);

        /**
         * Move a file to quarantine folder.
         *
         * @param uploadId the upload ID
         * @param reason   the quarantine reason
         */
        void moveToQuarantine(UUID uploadId, String reason);

        // ========================================
        // Cleanup Operations
        // ========================================

        /**
         * Cleans up (deletes) a file after a failed upload operation.
         * 
         * <p>
         * Used for rollback when news creation/update fails after file transfer.
         * </p>
         * <p>
         * NOTE: Only deletes from processed folder, never from originals.
         * </p>
         *
         * @param file the file to cleanup
         */
        void cleanupFile(File file);

        /**
         * Cleans up (deletes) a file by path after a failed upload operation.
         *
         * @param filePath the path to the file to cleanup
         */
        void cleanupFile(String filePath);

        // ========================================
        // Utility Methods
        // ========================================

        /**
         * Generates a backup filename with timestamp suffix.
         * 
         * <p>
         * Format: {@code {original}_{yyyyMMdd_HHmmss}.{ext}}
         * </p>
         *
         * @param originalFilename the original filename
         * @return filename with timestamp suffix
         */
        String generateBackupFilename(String originalFilename);

        /**
         * Checks if a file exists at the given filename.
         *
         * @param filename the filename to check
         * @return true if file exists, false otherwise
         */
        boolean fileExists(String filename);

        /**
         * Get the date-based subdirectory path.
         *
         * @return path in format YYYY/MM/DD
         */
        String getDateBasedPath();

        /**
         * Get upload tracking record by ID.
         *
         * @param uploadId the upload ID
         * @return the MediaUpload entity or null if not found
         */
        MediaUpload getUploadById(UUID uploadId);

        /**
         * Link an upload to its entity after entity creation.
         *
         * @param uploadId the upload ID
         * @param entityId the entity ID (news_id)
         */
        void linkToEntity(UUID uploadId, String entityId);
}
