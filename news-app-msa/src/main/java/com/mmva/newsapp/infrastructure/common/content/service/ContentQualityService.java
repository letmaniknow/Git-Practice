package com.mmva.newsapp.infrastructure.common.content.service;

import com.mmva.newsapp.domain.news.enums.core.ContentFormat;
import com.mmva.newsapp.infrastructure.common.content.dto.ContentQualityMetrics;
import com.mmva.newsapp.infrastructure.common.content.dto.ContentMetrics;

/**
 * Service interface for analyzing and calculating content quality metrics.
 *
 * <p>
 * Provides comprehensive content quality analysis including readability scores,
 * sentiment analysis, structural metrics, and quality assessments for news
 * content.
 * </p>
 *
 * <h3>Key Features:</h3>
 * <ul>
 * <li>Readability score calculation (Flesch-Kincaid, etc.)</li>
 * <li>Sentiment analysis for content tone</li>
 * <li>Structural content metrics (word count, sentence count, etc.)</li>
 * <li>Overall content quality scoring</li>
 * <li>Readability level assessment</li>
 * </ul>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
public interface ContentQualityService {

    /**
     * Calculates comprehensive content quality metrics.
     *
     * @param content the content to analyze
     * @param format  the content format
     * @return ContentQualityMetrics object with all calculated metrics
     */
    ContentQualityMetrics analyzeContentQuality(String content, ContentFormat format);

    /**
     * Calculates readability score using Flesch-Kincaid formula.
     *
     * @param content the content to analyze
     * @param format  the content format
     * @return readability score (0-100, higher is better)
     */
    double calculateReadabilityScore(String content, ContentFormat format);

    /**
     * Performs sentiment analysis on content.
     *
     * @param content the content to analyze
     * @param format  the content format
     * @return sentiment score (-1.0 to 1.0, negative=negative, positive=positive)
     */
    double calculateSentimentScore(String content, ContentFormat format);

    /**
     * Calculates overall content quality score.
     *
     * @param content the content to analyze
     * @param format  the content format
     * @return quality score (0-100, higher is better)
     */
    double calculateQualityScore(String content, ContentFormat format);

    /**
     * Determines readability level based on content analysis.
     *
     * @param content the content to analyze
     * @param format  the content format
     * @return readability level (e.g., "Elementary", "Intermediate", "Advanced")
     */
    String determineReadabilityLevel(String content, ContentFormat format);

    /**
     * Calculates structural metrics for content.
     *
     * @param content the content to analyze
     * @param format  the content format
     * @return ContentMetrics object with structural data
     */
    ContentMetrics calculateContentMetrics(String content, ContentFormat format);
}