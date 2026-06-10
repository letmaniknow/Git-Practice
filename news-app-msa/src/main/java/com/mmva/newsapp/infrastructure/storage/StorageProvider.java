package com.mmva.newsapp.infrastructure.storage;

import java.io.InputStream;

/**
 * Storage Provider Interface - Cloud-Ready Abstraction
 * 
 * <p>
 * Golden Rules:
 * <ol>
 * <li>PRESERVE FIRST - Save original before any processing</li>
 * <li>NEVER DELETE ORIGINALS - Archive instead</li>
 * <li>TRACK EVERYTHING - Every operation logged</li>
 * <li>FAIL SAFE - On error, file goes to quarantine</li>
 * </ol>
 * 
 * <p>
 * Implementations:
 * <ul>
 * <li>{@code LocalStorageProvider} - Local filesystem (development/small
 * deployments)</li>
 * <li>{@code S3StorageProvider} - AWS S3 (future)</li>
 * <li>{@code AzureBlobStorageProvider} - Azure Blob Storage (future)</li>
 * <li>{@code GCSStorageProvider} - Google Cloud Storage (future)</li>
 * </ul>
 * 
 * @author TheNews Team
 * @since 1.0.0
 */
public interface StorageProvider {

    // =========================================================================
    // CORE OPERATIONS
    // =========================================================================

    /**
     * Store a file at the specified path.
     * Creates parent directories if they don't exist.
     *
     * @param data the file content as InputStream
     * @param path the relative path within storage root
     * @return the full path/URL where file was stored
     * @throws StorageException if storage fails
     */
    String store(InputStream data, String path);

    /**
     * Store a file with byte array content.
     *
     * @param data the file content as byte array
     * @param path the relative path within storage root
     * @return the full path/URL where file was stored
     * @throws StorageException if storage fails
     */
    String store(byte[] data, String path);

    /**
     * Retrieve a file from storage.
     *
     * @param path the relative path within storage root
     * @return the file content as InputStream
     * @throws StorageException if file not found or read fails
     */
    InputStream retrieve(String path);

    /**
     * Retrieve file as byte array.
     *
     * @param path the relative path within storage root
     * @return the file content as byte array
     * @throws StorageException if file not found or read fails
     */
    byte[] retrieveBytes(String path);

    /**
     * Delete a file from storage.
     * Note: For originals, use {@link #moveToArchive} instead.
     *
     * @param path the relative path within storage root
     * @return true if deleted, false if file didn't exist
     * @throws StorageException if deletion fails
     */
    boolean delete(String path);

    /**
     * Move a file from one location to another.
     *
     * @param sourcePath the current relative path
     * @param targetPath the new relative path
     * @return the new full path/URL
     * @throws StorageException if move fails
     */
    String move(String sourcePath, String targetPath);

    /**
     * Copy a file to a new location.
     *
     * @param sourcePath the current relative path
     * @param targetPath the new relative path
     * @return the new full path/URL
     * @throws StorageException if copy fails
     */
    String copy(String sourcePath, String targetPath);

    // =========================================================================
    // ARCHIVE OPERATIONS (For data preservation)
    // =========================================================================

    /**
     * Move a file to archive folder with timestamp.
     * Used for preserving replaced/deleted files.
     *
     * @param path       the current relative path
     * @param entityType the entity type (news, users, admin, ads)
     * @return the archive path where file was moved
     * @throws StorageException if archive fails
     */
    String moveToArchive(String path, String entityType);

    /**
     * Move a file to quarantine folder.
     * Used for files that failed processing or security validation.
     *
     * @param path       the current relative path
     * @param entityType the entity type (news, users, admin, ads)
     * @param reason     the reason for quarantine
     * @return the quarantine path where file was moved
     * @throws StorageException if quarantine fails
     */
    String moveToQuarantine(String path, String entityType, String reason);

    // =========================================================================
    // UTILITY OPERATIONS
    // =========================================================================

    /**
     * Check if a file exists at the specified path.
     *
     * @param path the relative path within storage root
     * @return true if file exists, false otherwise
     */
    boolean exists(String path);

    /**
     * Get file size in bytes.
     *
     * @param path the relative path within storage root
     * @return the file size in bytes, or -1 if file doesn't exist
     */
    long getFileSize(String path);

    /**
     * Get the content type (MIME type) of a file.
     *
     * @param path the relative path within storage root
     * @return the content type, or null if cannot be determined
     */
    String getContentType(String path);

    /**
     * Resolve a relative path to absolute path/URL.
     *
     * @param relativePath the relative path
     * @return the absolute path or full URL (for cloud storage)
     */
    String resolvePath(String relativePath);

    /**
     * Ensure a directory exists, creating it if necessary.
     *
     * @param directoryPath the directory path to ensure
     * @throws StorageException if directory cannot be created
     */
    void ensureDirectoryExists(String directoryPath);

    /**
     * Generate a date-based subdirectory path.
     * Format: YYYY/MM/DD
     *
     * @return the date-based path segment
     */
    String getDateBasedPath();

    /**
     * Get the storage provider type.
     *
     * @return the provider type (local, s3, azure, gcs)
     */
    String getProviderType();
}
