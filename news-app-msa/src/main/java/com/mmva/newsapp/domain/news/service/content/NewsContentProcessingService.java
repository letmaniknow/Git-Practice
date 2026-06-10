package com.mmva.newsapp.domain.news.service.content;

import com.mmva.newsapp.domain.news.enums.core.ContentFormat;
import com.mmva.newsapp.domain.news.dto.validation.NewsContentValidationResult;
import com.mmva.newsapp.domain.news.dto.internal.NewsReadingMetrics;

/**
 * Domain service interface for news content processing and transformation.
 *
 * <p>
 * Provides specialized content processing operations for news articles,
 * including excerpt generation, content optimization, and format-specific
 * transformations tailored to news publishing requirements.
 * </p>
 *
 * <h3>Key Features:</h3>
 * <ul>
 * <li>News excerpt generation with smart truncation</li>
 * <li>Content optimization for different platforms</li>
 * <li>News-specific content validation</li>
 * <li>Headline processing and optimization</li>
 * <li>Content formatting for news display</li>
 * </ul>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
public interface NewsContentProcessingService {

    /**
     * Generates a smart excerpt from news content.
     *
     * @param content         the full content
     * @param format          the content format
     * @param maxLength       maximum excerpt length in characters
     * @param includeEllipsis whether to include ellipsis at the end
     * @return generated excerpt
     */
    String generateExcerpt(String content, ContentFormat format, int maxLength, boolean includeEllipsis);

    /**
     * Optimizes news content for web display.
     *
     * @param content the source content
     * @param format  the content format
     * @return web-optimized content
     */
    String optimizeForWebPublishing(String content, ContentFormat format);

    /**
     * Processes and optimizes news headline.
     *
     * @param headline the original headline
     * @return optimized headline
     */
    String processHeadline(String headline);

    /**
     * Validates news content structure and format.
     *
     * @param content the content to validate
     * @param format  the content format
     * @return validation result with any issues found
     */
    NewsContentValidationResult validateNewsContent(String content, ContentFormat format);

    /**
     * Extracts keywords from news content using news-specific algorithms.
     *
     * @param title       the news title
     * @param content     the news content
     * @param format      the content format
     * @param maxKeywords maximum number of keywords to extract
     * @return array of news-relevant keywords
     */
    String[] extractNewsKeywords(String title, String content, ContentFormat format, int maxKeywords);

    /**
     * Calculates content reading metrics specific to news consumption.
     *
     * @param content the news content
     * @param format  the content format
     * @return reading metrics including time estimates
     */
    NewsReadingMetrics calculateReadingMetrics(String content, ContentFormat format);
}