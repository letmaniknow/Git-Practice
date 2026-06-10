package com.mmva.newsapp.domain.news.service.media;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

/**
 * Utility class for media path resolution.
 * Provides centralized path construction logic to avoid circular dependencies.
 *
 * @author MMVA Team
 * @version 1.0.0
 * @since 2026-02-10
 */
@Slf4j
@Component
public class MediaPathUtils {

    /**
     * Resolves the processed image path for a given size and filename.
     *
     * @param processedPath Base processed images path (e.g.,
     *                      "./media/processed/news")
     * @param size          Image size (thumb, small, medium, large)
     * @param filename      Image filename
     * @return Full path to the processed image
     */
    public String resolveProcessedImagePath(String processedPath, String size, String filename) {
        return Path.of(processedPath, "images", size, filename).toString();
    }

    /**
     * Resolves the original media path for a given filename.
     *
     * @param originalsPath Base originals path (e.g., "./media/originals/news")
     * @param filename      Media filename
     * @return Full path to the original media
     */
    public String resolveOriginalMediaPath(String originalsPath, String filename) {
        return Path.of(originalsPath, filename).toString();
    }

    /**
     * Resolves the backup/archive path for a given filename and date path.
     *
     * @param archivePath Base archive path (e.g., "./media/archive/news")
     * @param datePath    Date path (e.g., "2026/02/10")
     * @param filename    Media filename
     * @return Full path to the archived media
     */
    public String resolveArchivePath(String archivePath, String datePath, String filename) {
        return Path.of(archivePath, datePath, filename).toString();
    }
}