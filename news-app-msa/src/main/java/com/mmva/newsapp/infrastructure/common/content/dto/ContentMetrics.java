package com.mmva.newsapp.infrastructure.common.content.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data transfer object containing structural content metrics.
 *
 * <p>
 * Provides detailed measurements of content structure including length,
 * word counts, sentence analysis, and readability components.
 * </p>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentMetrics {

    /**
     * Total content length in characters.
     */
    private int contentLength;

    /**
     * Total number of words in the content.
     */
    private int wordCount;

    /**
     * Total number of sentences in the content.
     */
    private int sentenceCount;

    /**
     * Total number of paragraphs in the content.
     */
    private int paragraphCount;

    /**
     * Average number of words per sentence.
     */
    private double averageWordsPerSentence;

    /**
     * Average number of syllables per word.
     */
    private double averageSyllablesPerWord;

    /**
     * Estimated reading time in minutes (based on 200 words per minute).
     */
    private int estimatedReadingTimeMinutes;
}