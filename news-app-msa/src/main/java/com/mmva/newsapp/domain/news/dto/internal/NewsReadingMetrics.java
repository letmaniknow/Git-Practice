package com.mmva.newsapp.domain.news.dto.internal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data transfer object containing news reading metrics.
 *
 * <p>
 * Provides detailed metrics about content readability and consumption
 * patterns specific to news articles, including reading time estimates
 * and comprehension indicators.
 * </p>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsReadingMetrics {

    /**
     * Estimated reading time in minutes for average adult reader.
     */
    private int estimatedReadingTimeMinutes;

    /**
     * Estimated reading time in minutes for slow reader.
     */
    private int slowReaderTimeMinutes;

    /**
     * Estimated reading time in minutes for fast reader.
     */
    private int fastReaderTimeMinutes;

    /**
     * Words per minute used for calculation (default: 200).
     */
    private int wordsPerMinute;

    /**
     * Total word count in the content.
     */
    private int wordCount;

    /**
     * Reading difficulty level (Beginner, Intermediate, Advanced).
     */
    private String difficultyLevel;

    /**
     * Whether the content is suitable for skim reading.
     */
    private boolean suitableForSkimReading;

    /**
     * Content density score (0-100, higher means more dense/information-heavy).
     */
    private double contentDensityScore;

    /**
     * Average sentence length in words.
     */
    private double averageSentenceLength;

    /**
     * Percentage of complex words (3+ syllables).
     */
    private double complexWordsPercentage;
}