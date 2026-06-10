package com.mmva.newsapp.infrastructure.monetization.ads.local.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Implementation of {@link AdsUrlService}.
 * Centralized service for building ad creative URLs.
 * Converts file paths or filenames to proper HTTP URLs.
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Slf4j
@Service
public class AdsUrlServiceImpl implements AdsUrlService {

    private static final String DEFAULT_CREATIVE_ENDPOINT = "/api/v1/admin/ads/creatives/files";
    private static final String DEFAULT_THUMBNAIL_ENDPOINT = "/api/v1/admin/ads/creatives/thumbnails";

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Override
    public String buildCreativeUrl(String filenameOrPath) {
        return buildUrl(DEFAULT_CREATIVE_ENDPOINT, filenameOrPath);
    }

    @Override
    public String buildThumbnailUrl(String filenameOrPath) {
        return buildUrl(DEFAULT_THUMBNAIL_ENDPOINT, filenameOrPath);
    }

    @Override
    public String buildUrl(String endpoint, String filenameOrPath) {
        if (filenameOrPath == null || filenameOrPath.isEmpty()) {
            return null;
        }

        String filename = extractFilename(filenameOrPath);
        if (filename == null || filename.isEmpty()) {
            log.warn("AdsUrlService: Could not extract filename from: {}", filenameOrPath);
            return null;
        }

        String url = baseUrl + endpoint + "/" + filename;
        log.trace("AdsUrlService: Built URL: {} from: {}", url, filenameOrPath);
        return url;
    }

    @Override
    public String extractFilename(String path) {
        if (path == null || path.isEmpty()) {
            return null;
        }

        // Handle both Windows and Unix separators
        int lastSeparatorIndex = Math.max(path.lastIndexOf('\\'), path.lastIndexOf('/'));

        if (lastSeparatorIndex >= 0 && lastSeparatorIndex < path.length() - 1) {
            return path.substring(lastSeparatorIndex + 1);
        }

        // No separator found, return as-is (already a filename)
        return path;
    }

    @Override
    public String getBaseUrl() {
        return baseUrl;
    }

    @Override
    public boolean isFilePath(String path) {
        if (path == null) {
            return false;
        }
        return path.contains("\\") || path.contains("/");
    }

    @Override
    public boolean isHttpUrl(String url) {
        if (url == null) {
            return false;
        }
        return url.startsWith("http://") || url.startsWith("https://");
    }

    @Override
    public String toCreativeUrlSafe(String filenameOrPath) {
        if (isHttpUrl(filenameOrPath)) {
            return filenameOrPath; // Already a URL
        }
        return buildCreativeUrl(filenameOrPath);
    }
}