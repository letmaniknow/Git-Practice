package com.mmva.newsapp.infrastructure.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Local Filesystem Storage Provider
 * 
 * <p>
 * Implements StorageProvider for local filesystem storage.
 * Suitable for development and small-scale deployments.
 * 
 * <p>
 * Logging Pattern: [STORAGE-LOCAL] operation - key=value
 * 
 * @author TheNews Team
 * @since 1.0.0
 */
@Slf4j
@Component
public class LocalStorageProvider implements StorageProvider {

    private static final String LOG_PREFIX = "[STORAGE-LOCAL]";
    private static final String PROVIDER_TYPE = "local";
    private static final DateTimeFormatter DATE_PATH_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    private static final DateTimeFormatter ARCHIVE_TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private final String rootPath;
    private final String archivePath;
    private final String quarantinePath;

    public LocalStorageProvider(
            @Value("${media.root-path:./media}") String rootPath,
            @Value("${media.paths.archive:${media.root-path}/archive}") String archivePath,
            @Value("${media.paths.quarantine:${media.root-path}/quarantine}") String quarantinePath) {
        this.rootPath = rootPath;
        this.archivePath = archivePath;
        this.quarantinePath = quarantinePath;

        log.info("{} Initialized - rootPath={}", LOG_PREFIX, rootPath);
    }

    // =========================================================================
    // CORE OPERATIONS
    // =========================================================================

    @Override
    public String store(InputStream data, String path) {
        Path targetPath = resolveFull(path);

        try {
            ensureDirectoryExists(targetPath.getParent().toString());
            Files.copy(data, targetPath, StandardCopyOption.REPLACE_EXISTING);

            log.debug("{} STORE - path={}, size={}",
                    LOG_PREFIX, path, Files.size(targetPath));

            return targetPath.toString();
        } catch (IOException e) {
            log.error("{} STORE_FAILED - path={}, error={}",
                    LOG_PREFIX, path, e.getMessage());
            throw new StorageException("store", path, PROVIDER_TYPE,
                    "Failed to store file", e);
        }
    }

    @Override
    public String store(byte[] data, String path) {
        return store(new ByteArrayInputStream(data), path);
    }

    @Override
    public InputStream retrieve(String path) {
        Path sourcePath = resolveFull(path);

        if (!Files.exists(sourcePath)) {
            log.warn("{} RETRIEVE_NOT_FOUND - path={}", LOG_PREFIX, path);
            throw new StorageException("retrieve", path, PROVIDER_TYPE,
                    "File not found");
        }

        try {
            log.debug("{} RETRIEVE - path={}", LOG_PREFIX, path);
            return Files.newInputStream(sourcePath);
        } catch (IOException e) {
            log.error("{} RETRIEVE_FAILED - path={}, error={}",
                    LOG_PREFIX, path, e.getMessage());
            throw new StorageException("retrieve", path, PROVIDER_TYPE,
                    "Failed to retrieve file", e);
        }
    }

    @Override
    public byte[] retrieveBytes(String path) {
        Path sourcePath = resolveFull(path);

        if (!Files.exists(sourcePath)) {
            log.warn("{} RETRIEVE_NOT_FOUND - path={}", LOG_PREFIX, path);
            throw new StorageException("retrieve", path, PROVIDER_TYPE,
                    "File not found");
        }

        try {
            log.debug("{} RETRIEVE_BYTES - path={}", LOG_PREFIX, path);
            return Files.readAllBytes(sourcePath);
        } catch (IOException e) {
            log.error("{} RETRIEVE_FAILED - path={}, error={}",
                    LOG_PREFIX, path, e.getMessage());
            throw new StorageException("retrieve", path, PROVIDER_TYPE,
                    "Failed to retrieve file", e);
        }
    }

    @Override
    public boolean delete(String path) {
        Path targetPath = resolveFull(path);

        if (!Files.exists(targetPath)) {
            log.debug("{} DELETE_NOT_FOUND - path={}", LOG_PREFIX, path);
            return false;
        }

        try {
            Files.delete(targetPath);
            log.info("{} DELETE - path={}", LOG_PREFIX, path);
            return true;
        } catch (IOException e) {
            log.error("{} DELETE_FAILED - path={}, error={}",
                    LOG_PREFIX, path, e.getMessage());
            throw new StorageException("delete", path, PROVIDER_TYPE,
                    "Failed to delete file", e);
        }
    }

    @Override
    public String move(String sourcePath, String targetPath) {
        Path source = resolveFull(sourcePath);
        Path target = resolveFull(targetPath);

        if (!Files.exists(source)) {
            log.warn("{} MOVE_SOURCE_NOT_FOUND - source={}", LOG_PREFIX, sourcePath);
            throw new StorageException("move", sourcePath, PROVIDER_TYPE,
                    "Source file not found");
        }

        try {
            ensureDirectoryExists(target.getParent().toString());
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);

            log.info("{} MOVE - from={}, to={}", LOG_PREFIX, sourcePath, targetPath);
            return target.toString();
        } catch (IOException e) {
            log.error("{} MOVE_FAILED - from={}, to={}, error={}",
                    LOG_PREFIX, sourcePath, targetPath, e.getMessage());
            throw new StorageException("move", sourcePath, PROVIDER_TYPE,
                    "Failed to move file", e);
        }
    }

    @Override
    public String copy(String sourcePath, String targetPath) {
        Path source = resolveFull(sourcePath);
        Path target = resolveFull(targetPath);

        if (!Files.exists(source)) {
            log.warn("{} COPY_SOURCE_NOT_FOUND - source={}", LOG_PREFIX, sourcePath);
            throw new StorageException("copy", sourcePath, PROVIDER_TYPE,
                    "Source file not found");
        }

        try {
            ensureDirectoryExists(target.getParent().toString());
            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);

            log.debug("{} COPY - from={}, to={}", LOG_PREFIX, sourcePath, targetPath);
            return target.toString();
        } catch (IOException e) {
            log.error("{} COPY_FAILED - from={}, to={}, error={}",
                    LOG_PREFIX, sourcePath, targetPath, e.getMessage());
            throw new StorageException("copy", sourcePath, PROVIDER_TYPE,
                    "Failed to copy file", e);
        }
    }

    // =========================================================================
    // ARCHIVE OPERATIONS
    // =========================================================================

    @Override
    public String moveToArchive(String path, String entityType) {
        Path sourcePath = resolveFull(path);

        if (!Files.exists(sourcePath)) {
            log.debug("{} ARCHIVE_SOURCE_NOT_FOUND - path={}", LOG_PREFIX, path);
            return null;
        }

        String filename = sourcePath.getFileName().toString();
        String archivedFilename = generateArchivedFilename(filename);
        String archiveRelativePath = entityType + "/" + getDateBasedPath() + "/" + archivedFilename;
        Path archiveFullPath = Path.of(archivePath, archiveRelativePath);

        try {
            ensureDirectoryExists(archiveFullPath.getParent().toString());
            Files.move(sourcePath, archiveFullPath, StandardCopyOption.REPLACE_EXISTING);

            log.info("{} ARCHIVE - original={}, archived={}",
                    LOG_PREFIX, path, archiveRelativePath);
            return archiveFullPath.toString();
        } catch (IOException e) {
            log.error("{} ARCHIVE_FAILED - path={}, error={}",
                    LOG_PREFIX, path, e.getMessage());
            throw new StorageException("archive", path, PROVIDER_TYPE,
                    "Failed to archive file", e);
        }
    }

    @Override
    public String moveToQuarantine(String path, String entityType, String reason) {
        Path sourcePath = resolveFull(path);

        if (!Files.exists(sourcePath)) {
            log.debug("{} QUARANTINE_SOURCE_NOT_FOUND - path={}", LOG_PREFIX, path);
            return null;
        }

        String filename = sourcePath.getFileName().toString();
        String quarantineRelativePath = entityType + "/" + getDateBasedPath() + "/" + filename;
        Path quarantineFullPath = Path.of(quarantinePath, quarantineRelativePath);

        try {
            ensureDirectoryExists(quarantineFullPath.getParent().toString());
            Files.move(sourcePath, quarantineFullPath, StandardCopyOption.REPLACE_EXISTING);

            log.warn("{} QUARANTINE - path={}, destination={}, reason={}",
                    LOG_PREFIX, path, quarantineRelativePath, reason);
            return quarantineFullPath.toString();
        } catch (IOException e) {
            log.error("{} QUARANTINE_FAILED - path={}, error={}",
                    LOG_PREFIX, path, e.getMessage());
            throw new StorageException("quarantine", path, PROVIDER_TYPE,
                    "Failed to quarantine file", e);
        }
    }

    // =========================================================================
    // UTILITY OPERATIONS
    // =========================================================================

    @Override
    public boolean exists(String path) {
        return Files.exists(resolveFull(path));
    }

    @Override
    public long getFileSize(String path) {
        Path fullPath = resolveFull(path);
        if (!Files.exists(fullPath)) {
            return -1;
        }
        try {
            return Files.size(fullPath);
        } catch (IOException e) {
            log.warn("{} GET_SIZE_FAILED - path={}, error={}",
                    LOG_PREFIX, path, e.getMessage());
            return -1;
        }
    }

    @Override
    public String getContentType(String path) {
        try {
            return Files.probeContentType(resolveFull(path));
        } catch (IOException e) {
            log.warn("{} GET_CONTENT_TYPE_FAILED - path={}, error={}",
                    LOG_PREFIX, path, e.getMessage());
            return null;
        }
    }

    @Override
    public String resolvePath(String relativePath) {
        return resolveFull(relativePath).toString();
    }

    @Override
    public void ensureDirectoryExists(String directoryPath) {
        Path path = Path.of(directoryPath);
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path);
                log.debug("{} DIRECTORY_CREATED - path={}", LOG_PREFIX, directoryPath);
            } catch (IOException e) {
                log.error("{} DIRECTORY_CREATE_FAILED - path={}, error={}",
                        LOG_PREFIX, directoryPath, e.getMessage());
                throw new StorageException("ensureDirectory", directoryPath, PROVIDER_TYPE,
                        "Failed to create directory", e);
            }
        }
    }

    @Override
    public String getDateBasedPath() {
        return LocalDate.now().format(DATE_PATH_FORMAT);
    }

    @Override
    public String getProviderType() {
        return PROVIDER_TYPE;
    }

    // =========================================================================
    // PRIVATE HELPERS
    // =========================================================================

    private Path resolveFull(String relativePath) {
        // If already absolute, use as-is
        Path path = Path.of(relativePath);
        if (path.isAbsolute()) {
            return path;
        }
        // Otherwise resolve against root
        return Path.of(rootPath, relativePath);
    }

    private String generateArchivedFilename(String originalFilename) {
        String timestamp = LocalDateTime.now().format(ARCHIVE_TIMESTAMP_FORMAT);
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex > 0) {
            String name = originalFilename.substring(0, dotIndex);
            String ext = originalFilename.substring(dotIndex);
            return name + "_archived_" + timestamp + ext;
        }
        return originalFilename + "_archived_" + timestamp;
    }
}
