package com.mmva.newsapp.domain.news.service.media;

/**
 * Service interface for building media URLs.
 * Converts file paths or filenames to proper HTTP URLs.
 * 
 * <h3>Usage Examples:</h3>
 * 
 * <pre>
 * // From filename only
 * mediaUrlService.buildMediaUrl("abc123.png");
 * // Returns: http://localhost:8080/api/v1/public/news/media/abc123.png
 * 
 * // From full file path
 * mediaUrlService.buildMediaUrl("C:\\path\\to\\abc123.png");
 * // Returns: http://localhost:8080/api/v1/public/news/media/abc123.png
 * 
 * // Custom endpoint
 * mediaUrlService.buildUrl("/api/v1/public/images", "photo.jpg");
 * // Returns: http://localhost:8080/api/v1/public/images/photo.jpg
 * </pre>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
public interface MediaUrlService {

    /**
     * Builds a complete media URL from a filename or file path.
     * Uses the default newsapp media endpoint.
     * 
     * @param filenameOrPath the filename or full file path
     * @return the complete HTTP URL, or null if input is null/empty
     */
    String buildMediaUrl(String filenameOrPath);

    /**
     * Builds a complete thumbnail URL from a filename or file path.
     * Uses the thumbnail-specific endpoint with optimized caching.
     * 
     * @param filenameOrPath the filename or full file path
     * @return the complete HTTP URL, or null if input is null/empty
     */
    String buildThumbnailUrl(String filenameOrPath);

    /**
     * Builds a complete image URL for a specific size variant.
     * Uses size-specific endpoints for optimized delivery.
     * 
     * @param size           the image size (thumbnail, small, medium, large)
     * @param filenameOrPath the filename or full file path
     * @return the complete HTTP URL, or null if input is null/empty
     */
    String buildImageUrl(String size, String filenameOrPath);

    /**
     * Builds a complete URL from a custom endpoint and filename/path.
     * 
     * @param endpoint       the API endpoint (e.g., "/api/v1/public/images")
     * @param filenameOrPath the filename or full file path
     * @return the complete HTTP URL, or null if input is null/empty
     */
    String buildUrl(String endpoint, String filenameOrPath);

    /**
     * Extracts the filename from a file path.
     * Handles both Windows (\) and Unix (/) path separators.
     * 
     * @param path the file path or filename
     * @return the extracted filename
     */
    String extractFilename(String path);

    /**
     * Gets the configured base URL.
     * 
     * @return the base URL (e.g., "http://localhost:8080")
     */
    String getBaseUrl();

    /**
     * Checks if a string looks like a full file path (not just a filename).
     * 
     * @param path the string to check
     * @return true if it contains path separators
     */
    boolean isFilePath(String path);

    /**
     * Checks if a string is already an HTTP URL.
     * 
     * @param url the string to check
     * @return true if it starts with http:// or https://
     */
    boolean isHttpUrl(String url);

    /**
     * Converts to media URL only if not already an HTTP URL.
     * Safe to call on already-converted URLs.
     * 
     * @param filenameOrPath the filename, path, or existing URL
     * @return the HTTP URL
     */
    String toMediaUrlSafe(String filenameOrPath);
}
