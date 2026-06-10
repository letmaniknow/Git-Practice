package com.mmva.newsapp.domain.news.service.media;

import org.springframework.web.multipart.MultipartFile;

import com.mmva.newsapp.domain.news.dto.core.ImageProcessingResponseDto;

import java.io.IOException;
import java.util.Set;

/**
 * Service interface for news image processing operations.
 * Handles generation of configurable image sizes for optimal mobile/web
 * performance.
 *
 * <h3>Configurable Image Size Variants:</h3>
 * <ul>
 * <li><b>Thumbnail:</b> Configurable dimensions - For lists, previews, push
 * notifications</li>
 * <li><b>Small:</b> Configurable dimensions - For mobile list views, compact
 * displays</li>
 * <li><b>Medium:</b> Configurable dimensions - For web articles, standard
 * displays</li>
 * <li><b>Large:</b> Configurable dimensions - For high-res displays, zoom
 * capabilities</li>
 * </ul>
 *
 * <h3>Processing Strategy:</h3>
 * <ul>
 * <li>Backend processing ensures consistent quality across platforms</li>
 * <li>Configurable sizes reduce bandwidth and storage for specific use
 * cases</li>
 * <li>Preserve aspect ratio with optional smart cropping</li>
 * <li>Video processing generates frames for enabled sizes only</li>
 * </ul>
 *
 * <h3>Naming Convention:</h3>
 *
 * <pre>
 * Pattern: {sanitized-name}_{uuid}_{size}.jpg
 * Examples:
 *   breaking-news_a1b2c3d4_thumb.jpg
 *   breaking-news_a1b2c3d4_small.jpg
 *   breaking-news_a1b2c3d4_medium.jpg
 *   breaking-news_a1b2c3d4_large.jpg
 * </pre>
 *
 * @author MMVA Team
 * @version 2.0.0
 * @since 2026-02-11
 * @see NewsImageProcessingServiceImpl
 * @see NewsMediaStorageService
 */
public interface NewsImageProcessingService {

        // ========================================
        // Configuration Methods
        // ========================================

        /**
         * Gets the list of enabled image sizes for processing.
         * 
         * @return set of enabled size names (e.g., "thumbnail", "small", "medium",
         *         "large")
         */
        Set<String> getEnabledSizes();

        /**
         * Checks if a specific size is enabled for processing.
         * 
         * @param size the size name to check
         * @return true if the size is enabled, false otherwise
         */
        boolean isSizeEnabled(String size);

        /**
         * Gets the dimensions for a specific size.
         * 
         * @param size the size name
         * @return array with [width, height], or null if size not configured
         */
        int[] getDimensions(String size);

        /** Default video frame extraction time in seconds */
        double DEFAULT_FRAME_TIME_SECONDS = 2.0;

        // ========================================
        // Image Processing Methods
        // ========================================

        /**
         * Processes an image file to generate enabled size variants.
         * Only creates sizes that are enabled in configuration.
         *
         * @param imageFile        the source image file
         * @param originalFilename the original filename for naming consistency
         * @return response with enabled size variants and their details
         * @throws IOException if processing fails
         */
        ImageProcessingResponseDto processImage(MultipartFile imageFile, String originalFilename) throws IOException;

        /**
         * Processes image bytes to generate all size variants.
         * Creates thumbnail, small, medium, and large versions.
         *
         * @param imageBytes       the source image content as byte array
         * @param originalFilename the original filename for naming consistency
         * @param contentType      the MIME content type of the image
         * @return response with all size variants and their details
         * @throws IOException if processing fails
         */
        ImageProcessingResponseDto processImage(byte[] imageBytes, String originalFilename, String contentType)
                        throws IOException;

        /**
         * Processes an image file with custom dimensions for all sizes.
         * Allows overriding default size configurations.
         *
         * @param imageFile        the source image file
         * @param originalFilename the original filename for naming consistency
         * @param thumbnailWidth   custom thumbnail width
         * @param thumbnailHeight  custom thumbnail height
         * @param smallWidth       custom small width
         * @param smallHeight      custom small height
         * @param mediumWidth      custom medium width
         * @param mediumHeight     custom medium height
         * @param largeWidth       custom large width
         * @param largeHeight      custom large height
         * @return response with all size variants
         * @throws IOException if processing fails
         */
        ImageProcessingResponseDto processImageWithCustomSizes(
                        MultipartFile imageFile, String originalFilename,
                        int thumbnailWidth, int thumbnailHeight,
                        int cardWidth, int cardHeight,
                        int heroWidth, int heroHeight,
                        int largeWidth, int largeHeight) throws IOException;

        // ========================================
        // Video Processing Methods
        // ========================================

        /**
         * Processes a video file to generate image variants.
         * Extracts frames at default time (2 seconds) for all sizes.
         *
         * @param videoFile        the source video file
         * @param originalFilename the original filename for naming consistency
         * @return response with all size variants (frames from video)
         * @throws IOException if processing fails
         */
        ImageProcessingResponseDto processVideo(MultipartFile videoFile, String originalFilename) throws IOException;

        /**
         * Processes a video file with custom frame extraction time.
         *
         * @param videoFile        the source video file
         * @param originalFilename the original filename for naming consistency
         * @param frameTimeSeconds time position to extract frames (e.g., 2.5)
         * @return response with all size variants
         * @throws IOException if processing fails
         */
        ImageProcessingResponseDto processVideoAtTime(MultipartFile videoFile, String originalFilename,
                        double frameTimeSeconds) throws IOException;

        // ========================================
        // Individual Size Generation (for flexibility)
        // ========================================

        /**
         * Generates only a thumbnail from an image.
         * Convenience method for backward compatibility.
         *
         * @param imageFile        the source image file
         * @param originalFilename the original filename
         * @return thumbnail response (single size)
         * @throws IOException if processing fails
         */
        ImageProcessingResponseDto generateThumbnail(MultipartFile imageFile, String originalFilename)
                        throws IOException;

        /**
         * Generates only a small size image.
         *
         * @param imageFile        the source image file
         * @param originalFilename the original filename
         * @return small size response
         * @throws IOException if processing fails
         */
        ImageProcessingResponseDto generateSmall(MultipartFile imageFile, String originalFilename) throws IOException;

        /**
         * Generates only a medium size image.
         *
         * @param imageFile        the source image file
         * @param originalFilename the original filename
         * @return medium size response
         * @throws IOException if processing fails
         */
        ImageProcessingResponseDto generateMedium(MultipartFile imageFile, String originalFilename) throws IOException;

        // ========================================
        // Utility Methods
        // ========================================

        /**
         * Determines if a file is an image based on content type.
         *
         * @param contentType the MIME type
         * @return true if image
         */
        boolean isImage(String contentType);

        /**
         * Determines if a file is a video based on content type.
         *
         * @param contentType the MIME type
         * @return true if video
         */
        boolean isVideo(String contentType);

        /**
         * Sanitizes a filename for safe storage.
         * Removes special characters, converts to lowercase, replaces spaces with
         * hyphens.
         *
         * @param filename the original filename
         * @return sanitized filename
         */
        String sanitizeFilename(String filename);

        // ========================================
        // File Management Methods
        // ========================================

        /**
         * Gets the file path for an image by its filename and size.
         *
         * @param filename the image filename
         * @param size     the size variant (thumb, small, medium, large)
         * @return the full file system path
         */
        String getImagePath(String filename, String size);

        /**
         * Checks if an image file exists for the given size.
         *
         * @param filename the image filename
         * @param size     the size variant (thumb, small, medium, large)
         * @return true if file exists
         */
        boolean imageExists(String filename, String size);

        /**
         * Deletes image files for all sizes with the given base filename.
         *
         * @param baseFilename the base filename (without size suffix)
         * @return true if all files deleted successfully
         */
        boolean deleteImages(String baseFilename);

        /**
         * Deletes a specific image file.
         *
         * @param filename the image filename
         * @param size     the size variant (thumb, small, medium, large)
         * @return true if deleted successfully
         */
        boolean deleteImage(String filename, String size);
}