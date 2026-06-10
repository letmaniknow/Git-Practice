package com.mmva.newsapp.infrastructure.monetization.ads.local.service;

/**
 * Service interface for building ad creative URLs.
 * Converts file paths or filenames to proper HTTP URLs for ad creatives.
 *
 * <h3>Usage Examples:</h3>
 *
 * <pre>
 * // From filename only
 * adsUrlService.buildCreativeUrl("abc123.png");
 * // Returns: http://localhost:8080/api/v1/admin/ads/creatives/files/abc123.png
 *
 * // From full file path
 * adsUrlService.buildCreativeUrl("tenant1/abc123.png");
 * // Returns: http://localhost:8080/api/v1/admin/ads/creatives/files/abc123.png
 * </pre>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
public interface AdsUrlService {

    /**
     * Builds a complete creative file URL from a filename or file path.
     * Uses the default ads creative files endpoint.
     *
     * @param filenameOrPath the filename or full file path
     * @return the complete HTTP URL, or null if input is null/empty
     */
    String buildCreativeUrl(String filenameOrPath);

    /**
     * Builds a complete thumbnail URL from a filename or file path.
     * Uses the thumbnail-specific endpoint with optimized caching.
     *
     * @param filenameOrPath the filename or full file path
     * @return the complete HTTP URL, or null if input is null/empty
     */
    String buildThumbnailUrl(String filenameOrPath);

    /**
     * Builds a complete URL from a custom endpoint and filename/path.
     *
     * @param endpoint       the API endpoint (e.g.,
     *                       "/api/v1/admin/ads/creatives/files")
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
     * Converts to creative URL only if not already an HTTP URL.
     * Safe to call on already-converted URLs.
     *
     * @param filenameOrPath the filename, path, or existing URL
     * @return the HTTP URL
     */
    String toCreativeUrlSafe(String filenameOrPath);
}