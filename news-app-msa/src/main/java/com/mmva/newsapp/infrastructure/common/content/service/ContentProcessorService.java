package com.mmva.newsapp.infrastructure.common.content.service;

import com.mmva.newsapp.domain.news.enums.core.ContentFormat;

/**
 * Service interface for processing and transforming content across different
 * platforms.
 *
 * <p>
 * Provides content transformation capabilities for multi-platform content
 * delivery,
 * enabling content reuse between mobile apps and web browsers with
 * platform-specific
 * adaptations.
 * </p>
 *
 * <h3>Key Features:</h3>
 * <ul>
 * <li>Format conversion between content types</li>
 * <li>Platform-specific content optimization</li>
 * <li>HTML sanitization and security</li>
 * <li>Content validation and processing</li>
 * </ul>
 *
 * <h3>Supported Conversions:</h3>
 * <ul>
 * <li>Plain Text ↔ HTML (basic and rich)</li>
 * <li>Markdown ↔ HTML</li>
 * <li>Platform-specific adaptations (mobile vs web)</li>
 * </ul>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
public interface ContentProcessorService {

    /**
     * Converts content from one format to another.
     *
     * @param content      the source content
     * @param sourceFormat the source content format
     * @param targetFormat the target content format
     * @return converted content in target format
     * @throws IllegalArgumentException if conversion is not supported
     */
    String convertFormat(String content, ContentFormat sourceFormat, ContentFormat targetFormat);

    /**
     * Converts plain text content to basic HTML format.
     *
     * @param plainText the plain text content
     * @return HTML-formatted content
     */
    String convertPlainTextToHtml(String plainText);

    /**
     * Converts HTML content to plain text format.
     *
     * @param htmlContent the HTML content
     * @return plain text content
     */
    String convertHtmlToPlainText(String htmlContent);

    /**
     * Sanitizes HTML content for security.
     *
     * @param htmlContent the potentially unsafe HTML content
     * @return sanitized HTML content
     */
    String sanitizeHtml(String htmlContent);

    /**
     * Optimizes content for mobile app consumption.
     *
     * @param content      the source content
     * @param sourceFormat the source format
     * @return mobile-optimized content
     */
    String optimizeForMobile(String content, ContentFormat sourceFormat);

    /**
     * Optimizes content for web browser consumption.
     *
     * @param content      the source content
     * @param sourceFormat the source format
     * @return web-optimized content
     */
    String optimizeForWeb(String content, ContentFormat sourceFormat);

    /**
     * Validates content format and structure.
     *
     * @param content the content to validate
     * @param format  the content format
     * @return true if content is valid
     */
    boolean validateContent(String content, ContentFormat format);

    /**
     * Extracts plain text summary from any content format.
     *
     * @param content   the source content
     * @param format    the content format
     * @param maxLength maximum length of summary
     * @return plain text summary
     */
    String extractSummary(String content, ContentFormat format, int maxLength);

    /**
     * Gets the estimated reading time for content.
     *
     * @param content        the content
     * @param format         the content format
     * @param wordsPerMinute average reading speed
     * @return estimated reading time in minutes
     */
    int calculateReadingTime(String content, ContentFormat format, int wordsPerMinute);
}