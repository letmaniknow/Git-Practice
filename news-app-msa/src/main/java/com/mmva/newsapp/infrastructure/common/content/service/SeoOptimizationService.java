package com.mmva.newsapp.infrastructure.common.content.service;

import com.mmva.newsapp.domain.news.enums.core.ContentFormat;
import com.mmva.newsapp.infrastructure.common.content.dto.OpenGraphMetadata;

/**
 * Service interface for SEO optimization and metadata generation.
 *
 * <p>
 * Provides comprehensive SEO analysis and optimization features including
 * meta description generation, keyword extraction, URL slug creation,
 * and search engine optimization recommendations.
 * </p>
 *
 * <h3>Key Features:</h3>
 * <ul>
 * <li>Meta description generation from content</li>
 * <li>Keyword extraction and analysis</li>
 * <li>URL-friendly slug generation</li>
 * <li>SEO score calculation</li>
 * <li>Title optimization suggestions</li>
 * <li>Open Graph metadata generation</li>
 * </ul>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
public interface SeoOptimizationService {

    /**
     * Generates SEO-optimized meta description from content.
     *
     * @param title   the content title
     * @param content the content body
     * @param format  the content format
     * @return optimized meta description (150-160 characters)
     */
    String generateMetaDescription(String title, String content, ContentFormat format);

    /**
     * Extracts relevant keywords from content for SEO.
     *
     * @param title       the content title
     * @param content     the content body
     * @param format      the content format
     * @param maxKeywords maximum number of keywords to extract
     * @return array of relevant keywords
     */
    String[] extractKeywords(String title, String content, ContentFormat format, int maxKeywords);

    /**
     * Generates URL-friendly slug from title.
     *
     * @param title the title to convert to slug
     * @return URL-friendly slug
     */
    String generateSlug(String title);

    /**
     * Calculates overall SEO score for content.
     *
     * @param title   the content title
     * @param content the content body
     * @param format  the content format
     * @return SEO score (0-100, higher is better)
     */
    double calculateSeoScore(String title, String content, ContentFormat format);

    /**
     * Generates Open Graph metadata for social media sharing.
     *
     * @param title       the content title
     * @param description the meta description
     * @param imageUrl    the featured image URL
     * @return OpenGraphMetadata object
     */
    OpenGraphMetadata generateOpenGraphMetadata(String title, String description, String imageUrl);

    /**
     * Optimizes title for SEO and readability.
     *
     * @param title the original title
     * @return optimized title
     */
    String optimizeTitle(String title);

    /**
     * Analyzes keyword density in content.
     *
     * @param content the content to analyze
     * @param format  the content format
     * @param keyword the keyword to check
     * @return keyword density percentage
     */
    double analyzeKeywordDensity(String content, ContentFormat format, String keyword);
}