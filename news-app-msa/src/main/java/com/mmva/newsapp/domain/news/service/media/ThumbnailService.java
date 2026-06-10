package com.mmva.newsapp.domain.news.service.media;

import org.springframework.web.multipart.MultipartFile;

import com.mmva.newsapp.domain.news.dto.core.ThumbnailResponseDto;

import java.io.IOException;

/**
 * Service interface for thumbnail operations.
 * 
 * <h3>Thumbnail Generation Strategy:</h3>
 * <ul>
 * <li><b>Custom upload:</b> User provides a specific thumbnail image</li>
 * <li><b>Image resize:</b> Auto-generate thumbnail from main image</li>
 * <li><b>Video frame:</b> Extract a frame from video at specified time</li>
 * </ul>
 * 
 * <h3>Naming Convention:</h3>
 * 
 * <pre>
 * Pattern: {sanitized-name}_{uuid}_thumb.jpg
 * Example: breaking-newsapp-story_a1b2c3d4_thumb.jpg
 * </pre>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
public interface ThumbnailService {

    // ========================================
    // Configuration Constants
    // ========================================

    /**
     * Default thumbnail width in pixels.
     * Industry standard for push notifications: 1024px works on both Android and
     * iOS.
     * Android recommends 800px, iOS recommends 1024px - using 1024 for universal
     * compatibility.
     */
    int DEFAULT_WIDTH = 1024;

    /**
     * Default thumbnail height in pixels.
     * Using 2:1 aspect ratio (1024x512) which is the industry standard for:
     * - Android Big Picture notifications
     * - iOS Rich notifications
     * - Apps like CNN, BBC, Inshorts use this ratio
     */
    int DEFAULT_HEIGHT = 512;

    /**
     * Push notification optimized thumbnail width.
     * Same as default - universal size for Android and iOS.
     */
    int PUSH_NOTIFICATION_WIDTH = 1024;

    /**
     * Push notification optimized thumbnail height.
     * 2:1 aspect ratio for rich notifications.
     */
    int PUSH_NOTIFICATION_HEIGHT = 512;

    /** Default video frame extraction time in seconds */
    double DEFAULT_FRAME_TIME_SECONDS = 2.0;

    // ========================================
    // Thumbnail Generation Methods
    // ========================================

    /**
     * Saves a custom thumbnail uploaded by the user.
     * 
     * @param file              the thumbnail file (image)
     * @param originalMediaName the original media filename for naming consistency
     * @return response with thumbnail details and URL
     * @throws IOException if file cannot be saved
     */
    ThumbnailResponseDto saveCustomThumbnail(MultipartFile file, String originalMediaName) throws IOException;

    /**
     * Generates a thumbnail by resizing an image file.
     * 
     * @param imageFile the source image file
     * @return response with thumbnail details and URL
     * @throws IOException if processing fails
     */
    ThumbnailResponseDto generateFromImage(MultipartFile imageFile) throws IOException;

    /**
     * Generates a thumbnail by resizing an image file with custom dimensions.
     * 
     * @param imageFile the source image file
     * @param width     target width in pixels
     * @param height    target height in pixels
     * @return response with thumbnail details and URL
     * @throws IOException if processing fails
     */
    ThumbnailResponseDto generateFromImage(MultipartFile imageFile, int width, int height) throws IOException;

    /**
     * Generates a push notification optimized thumbnail (1024x512, 2:1 ratio).
     * This size works perfectly on both Android and iOS.
     * 
     * <h4>Industry Standard Sizes:</h4>
     * <ul>
     * <li>Android Big Picture: 800x400 (2:1) - our 1024x512 scales down
     * perfectly</li>
     * <li>iOS Rich Notification: 1024x512 (2:1) - exact match</li>
     * </ul>
     * 
     * @param imageFile the source image file
     * @return response with thumbnail details and URL
     * @throws IOException if processing fails
     */
    ThumbnailResponseDto generatePushNotificationThumbnail(MultipartFile imageFile) throws IOException;

    /**
     * Extracts a frame from a video file to use as thumbnail.
     * Uses the default frame time (2 seconds).
     * 
     * @param videoFile the source video file
     * @return response with thumbnail details and URL
     * @throws IOException if processing fails
     */
    ThumbnailResponseDto generateFromVideo(MultipartFile videoFile) throws IOException;

    /**
     * Extracts a frame from a video file at a specific time.
     * 
     * @param videoFile     the source video file
     * @param timeInSeconds time position to extract frame (e.g., 2.5)
     * @return response with thumbnail details and URL
     * @throws IOException if processing fails
     */
    ThumbnailResponseDto generateFromVideo(MultipartFile videoFile, double timeInSeconds) throws IOException;

    /**
     * Generates a thumbnail from an already-saved media file path.
     * Auto-detects if image or video and processes accordingly.
     * 
     * @param mediaFilePath    the path to the saved media file
     * @param originalFilename the original filename for naming
     * @return response with thumbnail details and URL
     * @throws IOException if processing fails
     */
    ThumbnailResponseDto generateFromSavedMedia(String mediaFilePath, String originalFilename) throws IOException;

    // ========================================
    // Thumbnail Retrieval Methods
    // ========================================

    /**
     * Gets the file path for a thumbnail by its filename.
     * 
     * @param filename the thumbnail filename
     * @return the full file system path
     */
    String getThumbnailPath(String filename);

    /**
     * Checks if a thumbnail file exists.
     * 
     * @param filename the thumbnail filename
     * @return true if file exists
     */
    boolean thumbnailExists(String filename);

    /**
     * Deletes a thumbnail file.
     * 
     * @param filename the thumbnail filename
     * @return true if deleted successfully
     */
    boolean deleteThumbnail(String filename);

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

    /**
     * Generates a unique thumbnail filename following the naming convention.
     * 
     * @param originalFilename the original media filename
     * @return formatted thumbnail filename: {sanitized-name}_{uuid}_thumb.jpg
     */
    String generateThumbnailFilename(String originalFilename);
}
