package com.mmva.newsapp.infrastructure.monetization.ads.local.service;

import com.mmva.newsapp.infrastructure.common.exception.InvalidRequestException;
import com.mmva.newsapp.infrastructure.common.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * File storage service for Ad Creative assets.
 *
 * <p>
 * Handles file upload, storage, and retrieval for creative files (images,
 * videos).
 * Follows the same pattern as NewsServiceImpl for consistency.
 * </p>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Service
@Slf4j
public class AdsFileStorageService {

    @Value("${media.entities.ads.banners:${media.root-path}/processed/ads/banners}")
    private String creativeFolderPath;

    /**
     * Stores a creative file and returns the stored file path.
     *
     * @param file         the uploaded file
     * @param creativeCode the creative code for filename generation
     * @param tenantId     the tenant identifier
     * @return the relative file path for storage in database
     */
    public String storeCreativeFile(MultipartFile file, String creativeCode, String tenantId) {
        log.debug("Storing creative file: {} for tenant: {}", creativeCode, tenantId);

        ensureCreativeFolderExists();

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        String uniqueFilename = generateUniqueFilename(creativeCode, fileExtension);

        // Create tenant-specific directory
        Path tenantDir = Paths.get(creativeFolderPath, tenantId);
        ensureDirectoryExists(tenantDir);

        // Full file path
        Path filePath = tenantDir.resolve(uniqueFilename);

        try {
            // Copy file to destination
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            log.info("Creative file stored successfully: {} -> {}", uniqueFilename, filePath);

            // Return relative path for database storage
            return tenantId + "/" + uniqueFilename;

        } catch (IOException e) {
            log.error("Failed to store creative file: {} for tenant: {}", creativeCode, tenantId, e);
            throw new InvalidRequestException("Failed to store creative file: " + e.getMessage());
        }
    }

    /**
     * Retrieves a creative file as a Resource.
     *
     * @param filePath the relative file path
     * @return the file resource
     */
    public Resource loadCreativeFile(String filePath) {
        log.debug("Loading creative file: {}", filePath);

        try {
            Path fullPath = resolveFilePath(filePath);
            Resource resource = new UrlResource(fullPath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                log.warn("Creative file not found or not readable: {}", filePath);
                throw new ResourceNotFoundException("Creative file not found: " + filePath);
            }

        } catch (Exception e) {
            log.error("Error loading creative file: {}", filePath, e);
            throw new ResourceNotFoundException("Creative file not found: " + filePath);
        }
    }

    /**
     * Deletes a creative file.
     *
     * @param filePath the relative file path
     * @return true if deleted successfully, false otherwise
     */
    public boolean deleteCreativeFile(String filePath) {
        log.debug("Deleting creative file: {}", filePath);

        try {
            Path fullPath = resolveFilePath(filePath);
            boolean deleted = Files.deleteIfExists(fullPath);

            if (deleted) {
                log.info("Creative file deleted successfully: {}", filePath);
            } else {
                log.warn("Creative file not found for deletion: {}", filePath);
            }

            return deleted;

        } catch (Exception e) {
            log.error("Error deleting creative file: {}", filePath, e);
            return false;
        }
    }

    /**
     * Gets the content type of a creative file.
     *
     * @param filePath the relative file path
     * @return the content type
     */
    public String getContentType(String filePath) {
        try {
            Path fullPath = resolveFilePath(filePath);
            return Files.probeContentType(fullPath);
        } catch (Exception e) {
            log.warn("Could not determine content type for: {}", filePath);
            return "application/octet-stream";
        }
    }

    /**
     * Checks if a creative file exists.
     *
     * @param filePath the relative file path
     * @return true if exists, false otherwise
     */
    public boolean creativeFileExists(String filePath) {
        try {
            Path fullPath = resolveFilePath(filePath);
            return Files.exists(fullPath) && Files.isReadable(fullPath);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Gets the full file path for a creative file.
     *
     * @param filename the filename (relative path like tenantId/filename)
     * @return the full file path as string
     */
    public String getCreativeFilePath(String filename) {
        return resolveFilePath(filename).toString();
    }

    /**
     * Gets the full file path for a creative thumbnail.
     * Thumbnails are stored in the same directory as creative files.
     *
     * @param filename the thumbnail filename (relative path like tenantId/filename)
     * @return the full thumbnail file path as string
     */
    public String getCreativeThumbnailPath(String filename) {
        return resolveFilePath(filename).toString();
    }

    /**
     * Resolves a relative file path to a full path.
     *
     * @param filePath the relative file path (e.g., "tenant1/filename.jpg")
     * @return the full path
     */
    private Path resolveFilePath(String filePath) {
        return Paths.get(creativeFolderPath, filePath);
    }

    /**
     * Ensures the creative folder exists.
     */
    private void ensureCreativeFolderExists() {
        File dir = new File(creativeFolderPath);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new InvalidRequestException("Cannot create creative directory: " + creativeFolderPath);
        }
    }

    /**
     * Ensures a directory exists.
     *
     * @param dir the directory path
     */
    private void ensureDirectoryExists(Path dir) {
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new InvalidRequestException("Cannot create directory: " + dir);
        }
    }

    /**
     * Generates a unique filename for the creative.
     *
     * @param creativeCode the creative code
     * @param extension    the file extension
     * @return the unique filename
     */
    private String generateUniqueFilename(String creativeCode, String extension) {
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return creativeCode + "_" + uuid + "." + extension;
    }

    /**
     * Gets the file extension from a filename.
     *
     * @param filename the filename
     * @return the extension (lowercase)
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}