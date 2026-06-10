package com.mmva.newsapp.infrastructure.common.content.service;

import com.mmva.newsapp.domain.news.enums.core.ContentFormat;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

/**
 * Implementation of ContentProcessorService for multi-platform content
 * processing.
 *
 * <p>
 * Provides format conversion, sanitization, and platform-specific optimizations
 * for content reuse across mobile and web platforms.
 * </p>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
/**
 * @deprecated This service is deprecated. Use NewsContentProcessingServiceImpl
 *             for all content processing logic.
 *             This class will be removed in a future release.
 */
@Deprecated
@Service
@Slf4j
public class ContentProcessorServiceImpl implements ContentProcessorService {

    private static final int DEFAULT_WORDS_PER_MINUTE = 200;
    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<[^>]+>");

    @Override
    public String convertFormat(String content, ContentFormat sourceFormat, ContentFormat targetFormat) {
        if (content == null || content.trim().isEmpty()) {
            return content;
        }

        log.debug("Converting content from {} to {}", sourceFormat, targetFormat);

        // Same format, return as-is
        if (sourceFormat == targetFormat) {
            return content;
        }

        // Convert based on source and target formats
        switch (sourceFormat) {
            case PLAIN_TEXT:
                return convertFromPlainText(content, targetFormat);
            case HTML_BASIC:
            case HTML_RICH:
                return convertFromHtml(content, targetFormat);
            case MARKDOWN:
                return convertFromMarkdown(content, targetFormat);
            default:
                throw new IllegalArgumentException("Unsupported source format: " + sourceFormat);
        }
    }

    @Override
    public String convertPlainTextToHtml(String plainText) {
        if (plainText == null) {
            return null;
        }

        log.debug("Converting plain text to HTML, length: {}", plainText.length());

        return plainText
                // Convert line breaks to paragraphs
                .replaceAll("\\n\\n+", "</p><p>")
                .replaceAll("\\n", "<br>")
                // Convert URLs to links
                .replaceAll("(https?://\\S+)", "<a href=\"$1\" rel=\"nofollow\" target=\"_blank\">$1</a>")
                // Wrap in paragraph tags if not already wrapped
                .replaceAll("^(.+)$", "<p>$1</p>")
                // Clean up empty paragraphs
                .replaceAll("<p>\\s*</p>", "");
    }

    @Override
    public String convertHtmlToPlainText(String htmlContent) {
        if (htmlContent == null) {
            return null;
        }

        log.debug("Converting HTML to plain text, length: {}", htmlContent.length());

        // Use Jsoup to extract text content
        return Jsoup.parse(htmlContent).text();
    }

    @Override
    public String sanitizeHtml(String htmlContent) {
        if (htmlContent == null) {
            return null;
        }

        log.debug("Sanitizing HTML content, length: {}", htmlContent.length());

        // Use Jsoup to sanitize HTML, allowing only safe tags
        return Jsoup.clean(htmlContent, Safelist.relaxed()
                .addTags("h1", "h2", "h3", "h4", "h5", "h6")
                .addAttributes("a", "href", "rel", "target")
                .addAttributes("img", "src", "alt", "width", "height")
                .addAttributes("table", "th", "td", "tr", "thead", "tbody")
                .addProtocols("a", "href", "http", "https", "mailto")
                .addProtocols("img", "src", "http", "https", "data"));
    }

    @Override
    public String optimizeForMobile(String content, ContentFormat sourceFormat) {
        if (content == null) {
            return null;
        }

        log.debug("Optimizing content for mobile, format: {}", sourceFormat);

        String processedContent = content;

        // Convert to appropriate format for mobile
        if (sourceFormat.isHtmlFormat()) {
            // For mobile, we might want to keep some HTML but simplify it
            processedContent = simplifyHtmlForMobile(content);
        } else if (sourceFormat == ContentFormat.PLAIN_TEXT) {
            // Keep as plain text for mobile native components
            processedContent = content;
        }

        return processedContent;
    }

    @Override
    public String optimizeForWeb(String content, ContentFormat sourceFormat) {
        if (content == null) {
            return null;
        }

        log.debug("Optimizing content for web, format: {}", sourceFormat);

        String processedContent = content;

        // Ensure web content is in HTML format
        if (sourceFormat == ContentFormat.PLAIN_TEXT) {
            processedContent = convertPlainTextToHtml(content);
        } else if (sourceFormat.isHtmlFormat()) {
            // Sanitize and optimize existing HTML
            processedContent = sanitizeHtml(content);
            processedContent = optimizeHtmlForWeb(processedContent);
        }

        return processedContent;
    }

    @Override
    public boolean validateContent(String content, ContentFormat format) {
        if (content == null || content.trim().isEmpty()) {
            return false;
        }

        // Basic validation based on format
        switch (format) {
            case PLAIN_TEXT:
                return content.length() > 0;
            case HTML_BASIC:
            case HTML_RICH:
                // Check for basic HTML structure
                return content.contains("<") && content.contains(">");
            case MARKDOWN:
                // Basic markdown validation
                return content.length() > 0;
            default:
                return false;
        }
    }

    @Override
    public String extractSummary(String content, ContentFormat format, int maxLength) {
        if (content == null) {
            return null;
        }

        // Convert to plain text first
        String plainText = (format.isHtmlFormat()) ? convertHtmlToPlainText(content) : content;

        // Extract summary
        if (plainText.length() <= maxLength) {
            return plainText;
        }

        // Find word boundary
        String summary = plainText.substring(0, maxLength);
        int lastSpace = summary.lastIndexOf(' ');

        if (lastSpace > 0) {
            summary = summary.substring(0, lastSpace);
        }

        return summary + "...";
    }

    @Override
    public int calculateReadingTime(String content, ContentFormat format, int wordsPerMinute) {
        if (content == null || content.trim().isEmpty()) {
            return 0;
        }

        // Convert to plain text for word counting
        String plainText = (format.isHtmlFormat()) ? convertHtmlToPlainText(content) : content;

        // Count words (simple approximation)
        String[] words = plainText.split("\\s+");
        int wordCount = words.length;

        // Calculate reading time
        int minutes = (int) Math.ceil((double) wordCount / wordsPerMinute);

        return Math.max(1, minutes); // At least 1 minute
    }

    // Private helper methods

    private String convertFromPlainText(String content, ContentFormat targetFormat) {
        switch (targetFormat) {
            case HTML_BASIC:
            case HTML_RICH:
                return convertPlainTextToHtml(content);
            case MARKDOWN:
                // Basic conversion to markdown
                return content.replaceAll("\\n\\n", "\n\n")
                        .replaceAll("\\n", "  \n");
            default:
                throw new IllegalArgumentException("Cannot convert plain text to " + targetFormat);
        }
    }

    private String convertFromHtml(String content, ContentFormat targetFormat) {
        switch (targetFormat) {
            case PLAIN_TEXT:
                return convertHtmlToPlainText(content);
            case MARKDOWN:
                // Basic HTML to markdown conversion
                return convertHtmlToMarkdown(content);
            default:
                throw new IllegalArgumentException("Cannot convert HTML to " + targetFormat);
        }
    }

    private String convertFromMarkdown(String content, ContentFormat targetFormat) {
        switch (targetFormat) {
            case PLAIN_TEXT:
                // Remove markdown syntax
                return content.replaceAll("[*_`~]", "");
            case HTML_BASIC:
            case HTML_RICH:
                // Basic markdown to HTML (in production, use a proper library)
                return convertMarkdownToHtml(content);
            default:
                throw new IllegalArgumentException("Cannot convert markdown to " + targetFormat);
        }
    }

    private String simplifyHtmlForMobile(String htmlContent) {
        // Remove complex elements that don't work well on mobile
        return Jsoup.parse(htmlContent)
                .select("table, iframe, embed, object")
                .remove()
                .html();
    }

    private String optimizeHtmlForWeb(String htmlContent) {
        // Add web-specific optimizations
        // - Ensure proper heading hierarchy
        // - Add responsive image classes
        // - Optimize link attributes
        return htmlContent
                .replaceAll("<img([^>]+)>", "<img$1 loading=\"lazy\" class=\"responsive-image\">")
                .replaceAll("<a([^>]+)>", "<a$1 rel=\"noopener noreferrer\">");
    }

    private String convertHtmlToMarkdown(String htmlContent) {
        // Basic HTML to markdown conversion
        // In production, use a proper library like flexmark
        return htmlContent
                .replaceAll("<strong>(.*?)</strong>", "**$1**")
                .replaceAll("<em>(.*?)</em>", "*$1*")
                .replaceAll("<h1>(.*?)</h1>", "# $1\n")
                .replaceAll("<h2>(.*?)</h2>", "## $1\n")
                .replaceAll("<h3>(.*?)</h3>", "### $1\n")
                .replaceAll("<p>(.*?)</p>", "$1\n\n")
                .replaceAll("<br>", "  \n");
    }

    private String convertMarkdownToHtml(String markdownContent) {
        // Basic markdown to HTML conversion
        // In production, use a proper library
        return markdownContent
                .replaceAll("^### (.*)$", "<h3>$1</h3>")
                .replaceAll("^## (.*)$", "<h2>$1</h2>")
                .replaceAll("^# (.*)$", "<h1>$1</h1>")
                .replaceAll("\\*\\*(.*?)\\*\\*", "<strong>$1</strong>")
                .replaceAll("\\*(.*?)\\*", "<em>$1</em>")
                .replaceAll("  \n", "<br>")
                .replaceAll("\n\n", "</p><p>")
                .replaceAll("^(.+)$", "<p>$1</p>");
    }
}