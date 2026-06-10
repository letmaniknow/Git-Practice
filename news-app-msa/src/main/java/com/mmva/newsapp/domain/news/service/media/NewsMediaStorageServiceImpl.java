
package com.mmva.newsapp.domain.news.service.media;

import com.mmva.newsapp.infrastructure.common.exception.InvalidRequestException;
import com.mmva.newsapp.infrastructure.storage.StorageProvider;
import com.mmva.newsapp.infrastructure.storage.model.MediaUpload;
import com.mmva.newsapp.infrastructure.storage.model.MediaUpload.EntityType;
import com.mmva.newsapp.infrastructure.storage.model.MediaUpload.MediaType;
import com.mmva.newsapp.infrastructure.storage.model.MediaUpload.UploadStatus;
import com.mmva.newsapp.infrastructure.storage.repository.MediaUploadRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Implementation of NewsMediaStorageService with data-first approach.
 * 
 * <h3>Golden Rules Implementation:</h3>
 * <ol>
 * <li><b>PRESERVE FIRST</b> - Save original before any processing</li>
 * <li><b>NEVER DELETE ORIGINALS</b> - Move to archive instead</li>
 * <li><b>TRACK EVERYTHING</b> - Every upload logged to database</li>
 * <li><b>ENTITY SEPARATION</b> - Clear folder structure by type</li>
 * <li><b>DATE ORGANIZATION</b> - YYYY/MM/DD for easy archival</li>
 * </ol>
 * 
 * <h3>Storage Structure:</h3>
 * 
 * <pre>
 * media/
 * ├── originals/news/{YYYY}/{MM}/{DD}/    <- Raw uploads (SACRED)
 * ├── processed/news/images/              <- Optimized images
 * ├── processed/news/thumbnails/          <- Generated thumbnails
 * ├── quarantine/news/{YYYY}/{MM}/{DD}/   <- Failed/security issues
 * └── archive/news/{YYYY}/{MM}/{DD}/      <- Replaced files
 * </pre>
 * 
 * <h3>Logging Convention:</h3>
 * <p>
 * All logs use prefix {@code [MEDIA-STORAGE]} for easy filtering.
 * </p>
 * 
 * @author MMVA Team
 * @version 2.0.0
 * @since 2025-02-05
 * @see NewsMediaStorageService
 */
@Slf4j
@Service
public class NewsMediaStorageServiceImpl implements NewsMediaStorageService {

    // ========================================
    // Constants
    // ========================================

    private static final String LOG_PREFIX = "[MEDIA-STORAGE]";
    private static final int MAX_FILENAME_LENGTH = 50;
    private static final int SHORT_UUID_LENGTH = 8;
    private static final String DEFAULT_FILENAME = "media";
    private static final String BACKUP_TIMESTAMP_FORMAT = "yyyyMMdd_HHmmss";
    private static final DateTimeFormatter DATE_PATH_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    // ========================================
    // Dependencies
    // ========================================

    private final StorageProvider storageProvider;
    private final MediaUploadRepository mediaUploadRepository;
    private final MediaPathUtils mediaPathUtils;

    // ========================================
    // Configuration
    // ========================================

    @Value("${media.root-path:./media}")
    private String mediaRootPath;

    @Value("${media.entities.news.originals:${media.root-path}/originals/news}")
    private String originalsPath;

    @Value("${media.entities.news.processed:${media.root-path}/processed/news}")
    private String processedPath;

    @Value("${media.entities.news.archive:${media.root-path}/archive/news}")
    private String archivePath;

    @Value("${media.entities.news.quarantine:${media.root-path}/quarantine/news}")
    private String quarantinePath;

    // ========================================
    // Constructor
    // ========================================

    public NewsMediaStorageServiceImpl(
            StorageProvider storageProvider,
            MediaUploadRepository mediaUploadRepository,
            MediaPathUtils mediaPathUtils) {
        this.storageProvider = storageProvider;
        this.mediaUploadRepository = mediaUploadRepository;
        this.mediaPathUtils = mediaPathUtils;
    }

    // ========================================
    // PRESERVE FIRST - Original File Storage
    // ========================================

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public MediaUpload preserveOriginal(MultipartFile file, String correlationId, String uploadedBy) {
        return preserveOriginal(file, null, correlationId, uploadedBy, null, null);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public MediaUpload preserveOriginal(MultipartFile file, String entityId,
            String correlationId, String uploadedBy,
            String ipAddress, String userAgent) {
        long startTime = System.currentTimeMillis();
        String originalFilename = file.getOriginalFilename() != null
                ? file.getOriginalFilename()
                : DEFAULT_FILENAME;

        log.info("{} [{}] PRESERVE_START - filename='{}', size={}",
                LOG_PREFIX, correlationId, originalFilename, file.getSize());

        try {
            // Step 1: Generate path for original file (date-based)
            String datePath = getDateBasedPath();
            UUID uploadId = UUID.randomUUID();
            String preservedFilename = uploadId.toString() + "_" + sanitizeFilename(originalFilename);
            String relativePath = datePath + "/" + preservedFilename;
            String fullPath = Path.of(originalsPath, relativePath).toString();

            // Step 2: Save file to originals folder FIRST
            ensureOriginalsFolderExists();
            try (InputStream inputStream = file.getInputStream()) {
                storageProvider.store(inputStream, fullPath);
            }

            // Step 3: Create tracking record immediately
            MediaUpload upload = MediaUpload.builder()
                    .uploadId(uploadId)
                    .entityType(EntityType.NEWS)
                    .entityId(entityId)
                    .mediaType(determineMediaType(file.getContentType()))
                    .originalFilename(originalFilename)
                    .originalPath(fullPath)
                    .originalSizeBytes(file.getSize())
                    .originalMimeType(file.getContentType())
                    .uploadTimestamp(Instant.now())
                    .status(UploadStatus.RECEIVED)
                    .uploadedBy(uploadedBy)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .correlationId(correlationId)
                    .build();

            MediaUpload saved = mediaUploadRepository.save(upload);

            long duration = System.currentTimeMillis() - startTime;
            log.info("{} [{}] PRESERVE_COMPLETE - uploadId={}, path='{}', duration={}ms",
                    LOG_PREFIX, correlationId, uploadId, relativePath, duration);

            return saved;

        } catch (IOException e) {
            log.error("{} [{}] PRESERVE_FAILED - filename='{}', error={}",
                    LOG_PREFIX, correlationId, originalFilename, e.getMessage(), e);
            throw new InvalidRequestException("file",
                    "Failed to preserve original file: " + e.getMessage());
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public MediaUpload preserveOriginal(byte[] content, String originalFilename, String contentType, long size,
            String correlationId, String uploadedBy) {
        long startTime = System.currentTimeMillis();

        log.info("{} [{}] PRESERVE_START - filename='{}', size={}",
                LOG_PREFIX, correlationId, originalFilename, size);

        try {
            // Step 1: Generate path for original file (date-based)
            String datePath = getDateBasedPath();
            UUID uploadId = UUID.randomUUID();
            String preservedFilename = uploadId.toString() + "_" + sanitizeFilename(originalFilename);
            String relativePath = datePath + "/" + preservedFilename;
            String fullPath = Path.of(originalsPath, relativePath).toString();

            // Step 2: Save file to originals folder FIRST
            ensureOriginalsFolderExists();
            try (ByteArrayInputStream inputStream = new ByteArrayInputStream(content)) {
                storageProvider.store(inputStream, fullPath);
            }

            // Step 3: Create tracking record immediately
            MediaUpload upload = MediaUpload.builder()
                    .uploadId(uploadId)
                    .entityType(EntityType.NEWS)
                    .entityId(null)
                    .mediaType(determineMediaType(contentType))
                    .originalFilename(originalFilename)
                    .originalPath(fullPath)
                    .originalSizeBytes(size)
                    .originalMimeType(contentType)
                    .uploadTimestamp(Instant.now())
                    .status(UploadStatus.RECEIVED)
                    .uploadedBy(uploadedBy)
                    .correlationId(correlationId)
                    .build();

            MediaUpload saved = mediaUploadRepository.save(upload);

            long duration = System.currentTimeMillis() - startTime;
            log.info("{} [{}] PRESERVE_COMPLETE - uploadId={}, path='{}', duration={}ms",
                    LOG_PREFIX, correlationId, uploadId, relativePath, duration);

            return saved;

        } catch (Exception e) {
            log.error("{} [{}] PRESERVE_FAILED - filename='{}', error={}",
                    LOG_PREFIX, correlationId, originalFilename, e.getMessage(), e);
            throw new InvalidRequestException("file",
                    "Failed to preserve original file: " + e.getMessage());
        }
    }

    // ========================================
    // Filename Generation
    // ========================================

    @Override
    public String generateMediaFilename(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            originalFilename = DEFAULT_FILENAME;
        }

        // Extract extension
        int dotIndex = originalFilename.lastIndexOf('.');
        String extension = (dotIndex > 0) ? originalFilename.substring(dotIndex) : "";
        String nameWithoutExt = (dotIndex > 0) ? originalFilename.substring(0, dotIndex) : originalFilename;

        // Sanitize: lowercase, replace special chars with hyphens
        String sanitized = nameWithoutExt
                .toLowerCase()
                .replaceAll("[^a-z0-9\\-]", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");

        // Limit length
        if (sanitized.length() > MAX_FILENAME_LENGTH) {
            sanitized = sanitized.substring(0, MAX_FILENAME_LENGTH);
        }
        if (sanitized.isEmpty()) {
            sanitized = DEFAULT_FILENAME;
        }

        // Generate short UUID for uniqueness
        String shortUuid = UUID.randomUUID().toString().substring(0, SHORT_UUID_LENGTH);
        String generatedFilename = sanitized + "_" + shortUuid + extension;

        log.debug("{} Generated filename - original='{}', generated='{}'",
                LOG_PREFIX, originalFilename, generatedFilename);

        return generatedFilename;
    }

    // ========================================
    // Path Resolution
    // ========================================

    @Override
    public String resolveFilePath(String filename) {
        // Main images are stored in processed/news/images/main for consistency
        return Path.of(processedPath, "images", "main", filename).toString();
    }

    @Override
    public String resolveOriginalPath(String filename) {
        return Path.of(originalsPath, getDateBasedPath(), filename).toString();
    }

    @Override
    public String resolveProcessedPath(String filename) {
        // Main images are stored in processed/news/images/main for consistency
        return Path.of(processedPath, "images", "main", filename).toString();
    }

    @Override
    public String getMediaFolderPath() {
        // Main images folder path
        return Path.of(processedPath, "images", "main").toString();
    }

    @Override
    public String getOriginalsFolderPath() {
        return originalsPath;
    }

    @Override
    public String getBackupFolderPath() {
        return archivePath;
    }

    @Override
    public String resolveProcessedImagePath(String size, String filename) {
        return mediaPathUtils.resolveProcessedImagePath(processedPath, size, filename);
    }

    @Override
    public String ensureProcessedImagesFolderExists(String size) {
        String sizePath = Path.of(processedPath, "images", size).toString();
        File dir = new File(sizePath);
        if (!dir.exists()) {
            if (dir.mkdirs()) {
                log.info("{} Created processed images folder for size '{}' - path='{}'", LOG_PREFIX, size, sizePath);
            } else {
                log.error("{} Failed to create processed images folder for size '{}' - path='{}'", LOG_PREFIX, size,
                        sizePath);
                throw new InvalidRequestException("folder", "Cannot create processed images directory: " + sizePath);
            }
        }
        return sizePath;
    }

    // ========================================
    // Folder Management
    // ========================================

    @Override
    public void ensureMediaFolderExists() {
        // Ensure main images folder exists
        String mediaPath = Path.of(processedPath, "images", "main").toString();
        File dir = new File(mediaPath);
        if (!dir.exists()) {
            if (dir.mkdirs()) {
                log.info("{} Created media folder - path='{}'", LOG_PREFIX, mediaPath);
            } else {
                log.error("{} Failed to create media folder - path='{}'", LOG_PREFIX, mediaPath);
                throw new InvalidRequestException("folder", "Cannot create media directory: " + mediaPath);
            }
        }
    }

    @Override
    public String ensureOriginalsFolderExists() {
        String datePath = getDateBasedPath();
        String fullPath = Path.of(originalsPath, datePath).toString();

        File dir = new File(fullPath);
        if (!dir.exists()) {
            if (dir.mkdirs()) {
                log.info("{} Created originals folder - path='{}'", LOG_PREFIX, fullPath);
            } else {
                log.error("{} Failed to create originals folder - path='{}'", LOG_PREFIX, fullPath);
                throw new InvalidRequestException("folder", "Cannot create originals directory: " + fullPath);
            }
        }
        return fullPath;
    }

    @Override
    public void ensureBackupFolderExists() throws IOException {
        Path backupPath = Path.of(archivePath);
        if (!Files.exists(backupPath)) {
            Files.createDirectories(backupPath);
            log.info("{} Created archive folder - path='{}'", LOG_PREFIX, archivePath);
        }
    }

    // ========================================
    // Processing Pipeline
    // ========================================

    @Override
    @Transactional
    public void markProcessingStarted(UUID uploadId) {
        mediaUploadRepository.updateStatus(uploadId, UploadStatus.PROCESSING);
        log.debug("{} Status updated - uploadId={}, status=PROCESSING", LOG_PREFIX, uploadId);
    }

    @Override
    @Transactional
    public void markPublished(UUID uploadId, String processedFilename,
            String processedPath, String publicUrl) {
        long size = storageProvider.getFileSize(processedPath);
        mediaUploadRepository.markAsPublished(uploadId, processedFilename, processedPath, size, publicUrl);
        log.info("{} PUBLISHED - uploadId={}, filename='{}', url='{}'",
                LOG_PREFIX, uploadId, processedFilename, publicUrl);
    }

    @Override
    @Transactional
    public void markFailed(UUID uploadId, String reason) {
        mediaUploadRepository.markAsFailed(uploadId, UploadStatus.PROCESSING_FAILED, reason);
        mediaUploadRepository.incrementRetryCount(uploadId);
        log.warn("{} FAILED - uploadId={}, reason='{}'", LOG_PREFIX, uploadId, reason);
    }

    // ========================================
    // Backup/Archive Operations
    // ========================================

    /**
     * Moves a media variant (main, card, hero, thumbnail) to backup/archive.
     * 
     * @param fileName the filename
     * @param size     the variant size (main, card, hero, thumbnail)
     */
    public void moveMediaVariantToBackup(String fileName, String size) {
        if (fileName == null || fileName.isEmpty()) {
            log.debug("{} Skipping backup - fileName is null or empty (size={})", LOG_PREFIX, size);
            return;
        }

        // All media (images AND videos) follow same structure:
        // processed/news/images/{size}/filename
        String sourcePath = mediaPathUtils.resolveProcessedImagePath(processedPath, size, fileName);
        File sourceFile = new File(sourcePath);

        if (!sourceFile.exists()) {
            // For main media, try pattern matching for legacy DB records with incorrect
            // filenames
            if ("main".equals(size)) {
                File mainDir = new File(Path.of(processedPath, "images", "main").toString());
                File matchedFile = findMainImageByPattern(mainDir, fileName);
                if (matchedFile != null) {
                    log.info("{} Found main media by pattern match - DB filename: '{}', Actual file: '{}'",
                            LOG_PREFIX, fileName, matchedFile.getName());
                    moveFileToBackup(matchedFile.getName(), matchedFile.getAbsolutePath(), size);
                    return;
                }
            }
            log.warn("{} File not found for archival - size={}, filename='{}', expected path='{}'",
                    LOG_PREFIX, size, fileName, sourcePath);
            return;
        }

        // Standard archival for existing file (works for images, videos, and any media)
        log.info("{} Archiving {} media - filename: '{}', path: '{}'",
                LOG_PREFIX, size, fileName, sourcePath);
        moveFileToBackup(fileName, sourcePath, size);
    }

    /**
     * Find main image by pattern matching for legacy DB records with incorrect
     * filenames.
     * Handles case where DB has "original.png" but file is "original_uuid_main.jpg"
     * 
     * @param directory  The main images directory
     * @param dbFilename The filename from database (may be incorrect/legacy format)
     * @return Matched file or null if not found
     */
    private File findMainImageByPattern(File directory, String dbFilename) {
        if (dbFilename == null || !directory.exists() || !directory.isDirectory()) {
            return null;
        }

        // Extract base name without extension from DB filename
        final String baseName;
        int lastDot = dbFilename.lastIndexOf('.');
        if (lastDot > 0) {
            baseName = dbFilename.substring(0, lastDot);
        } else {
            baseName = dbFilename;
        }

        // Find files that match pattern: baseName_*_main.*
        File[] matchingFiles = directory.listFiles((dir, name) -> {
            // Must start with baseName, contain _main, and end with image extension
            return name.startsWith(baseName + "_")
                    && name.contains("_main.")
                    && (name.endsWith(".jpg") || name.endsWith(".png") || name.endsWith(".jpeg"));
        });

        if (matchingFiles != null && matchingFiles.length > 0) {
            if (matchingFiles.length > 1) {
                log.warn("{} Multiple main images found matching pattern '{}', using first: {}",
                        LOG_PREFIX, dbFilename, matchingFiles[0].getName());
            }
            return matchingFiles[0];
        }

        return null;
    }

    @Override
    public void moveMediaToBackup(String fileName) {
        moveMediaVariantToBackup(fileName, "main");
    }

    @Override
    public void moveThumbnailToBackup(String thumbnailFilename) {
        if (thumbnailFilename == null || thumbnailFilename.isEmpty()) {
            log.debug("{} Skipping thumbnail backup - filename is null or empty", LOG_PREFIX);
            return;
        }
        String sourcePath = mediaPathUtils.resolveProcessedImagePath(processedPath, "thumb", thumbnailFilename);
        moveFileToBackup(thumbnailFilename, sourcePath, "thumbnail");
    }

    @Override
    public void moveFileToBackup(String filename, String sourcePath, String fileType) {
        File sourceFile = new File(sourcePath);

        log.debug("{} Attempting to archive file - type={}, filename='{}', sourcePath='{}'",
                LOG_PREFIX, fileType, filename, sourcePath);

        if (!sourceFile.exists()) {
            log.warn("{} Source file does not exist, cannot archive - type={}, filename='{}', path='{}'",
                    LOG_PREFIX, fileType, filename, sourcePath);
            return;
        }

        log.info("{} File exists, proceeding with archival - type={}, size={}KB, path='{}'",
                LOG_PREFIX, fileType, sourceFile.length() / 1024, sourcePath);

        try {
            ensureBackupFolderExists();
            String datePath = getDateBasedPath();
            String archiveFilename = generateBackupFilename(filename);
            Path archiveFullPath = Path.of(archivePath, datePath, archiveFilename);

            // Ensure date-based subfolder exists
            Files.createDirectories(archiveFullPath.getParent());
            Files.move(sourceFile.toPath(), archiveFullPath, StandardCopyOption.REPLACE_EXISTING);

            log.info("{} ✓ File successfully archived - type={}, original='{}', archive='{}'",
                    LOG_PREFIX, fileType, filename, archiveFullPath);
        } catch (IOException e) {
            log.error("{} ✗ Failed to archive file - type={}, filename='{}', error={}",
                    LOG_PREFIX, fileType, filename, e.getMessage(), e);

            // Fallback: delete if archive fails to avoid orphaned files
            if (sourceFile.exists() && !sourceFile.delete()) {
                log.warn("{} Failed to delete file after archive failure - type={}, filename='{}'",
                        LOG_PREFIX, fileType, filename);
            }
        }
    }

    @Override
    @Transactional
    public void moveToQuarantine(UUID uploadId, String reason) {
        MediaUpload upload = mediaUploadRepository.findById(uploadId).orElse(null);
        if (upload == null) {
            log.warn("{} Upload not found for quarantine - uploadId={}", LOG_PREFIX, uploadId);
            return;
        }

        try {
            String datePath = getDateBasedPath();
            String filename = Path.of(upload.getOriginalPath()).getFileName().toString();
            Path quarantineFullPath = Path.of(quarantinePath, datePath, filename);

            Files.createDirectories(quarantineFullPath.getParent());
            Files.move(Path.of(upload.getOriginalPath()), quarantineFullPath,
                    StandardCopyOption.REPLACE_EXISTING);

            upload.setStatus(UploadStatus.QUARANTINED);
            upload.setQuarantinePath(quarantineFullPath.toString());
            upload.setFailureReason(reason);
            mediaUploadRepository.save(upload);

            log.warn("{} QUARANTINED - uploadId={}, path='{}', reason='{}'",
                    LOG_PREFIX, uploadId, quarantineFullPath, reason);
        } catch (IOException e) {
            log.error("{} Failed to quarantine - uploadId={}, error={}",
                    LOG_PREFIX, uploadId, e.getMessage());
        }
    }

    // ========================================
    // Cleanup Operations
    // ========================================

    @Override
    public void cleanupFile(File file) {
        if (file == null) {
            return;
        }
        // Safety check: Never delete from originals folder
        if (file.getAbsolutePath().contains("/originals/") ||
                file.getAbsolutePath().contains("\\originals\\")) {
            log.warn("{} BLOCKED cleanup of original file - path='{}'", LOG_PREFIX, file.getAbsolutePath());
            return;
        }

        if (file.exists() && !file.delete()) {
            log.warn("{} Failed to cleanup file - path='{}'", LOG_PREFIX, file.getAbsolutePath());
        } else if (file.exists()) {
            log.debug("{} File cleaned up - path='{}'", LOG_PREFIX, file.getAbsolutePath());
        }
    }

    @Override
    public void cleanupFile(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return;
        }
        cleanupFile(new File(filePath));
    }

    // ========================================
    // Utility Methods
    // ========================================

    @Override
    public String generateBackupFilename(String originalFilename) {
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern(BACKUP_TIMESTAMP_FORMAT));

        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex > 0) {
            String name = originalFilename.substring(0, dotIndex);
            String ext = originalFilename.substring(dotIndex);
            return name + "_archived_" + timestamp + ext;
        }
        return originalFilename + "_archived_" + timestamp;
    }

    @Override
    public boolean fileExists(String filename) {
        if (filename == null || filename.isEmpty()) {
            return false;
        }
        return new File(resolveFilePath(filename)).exists();
    }

    @Override
    public String getDateBasedPath() {
        return LocalDate.now().format(DATE_PATH_FORMAT);
    }

    @Override
    public MediaUpload getUploadById(UUID uploadId) {
        return mediaUploadRepository.findById(uploadId).orElse(null);
    }

    @Override
    @Transactional
    public void linkToEntity(UUID uploadId, String entityId) {
        int updated = mediaUploadRepository.linkToEntity(uploadId, entityId);
        if (updated > 0) {
            log.debug("{} Linked upload to entity - uploadId={}, entityId={}",
                    LOG_PREFIX, uploadId, entityId);
        } else {
            log.warn("{} Failed to link upload to entity - uploadId={}, entityId={}",
                    LOG_PREFIX, uploadId, entityId);
        }
    }

    // ========================================
    // Private Helpers
    // ========================================

    private String sanitizeFilename(String filename) {
        if (filename == null || filename.isEmpty()) {
            return DEFAULT_FILENAME;
        }

        // Remove directory path if present
        int lastSlash = Math.max(filename.lastIndexOf('/'), filename.lastIndexOf('\\'));
        if (lastSlash >= 0) {
            filename = filename.substring(lastSlash + 1);
        }

        // Replace unsafe characters
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private MediaType determineMediaType(String contentType) {
        if (contentType == null) {
            return MediaType.OTHER;
        }
        if (contentType.startsWith("image/")) {
            return MediaType.NEWS_IMAGE;
        }
        if (contentType.startsWith("video/")) {
            return MediaType.NEWS_VIDEO;
        }
        return MediaType.OTHER;
    }

}
