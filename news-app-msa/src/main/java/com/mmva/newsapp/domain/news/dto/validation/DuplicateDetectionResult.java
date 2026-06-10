package com.mmva.newsapp.domain.news.dto.validation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Data transfer object containing duplicate content detection results.
 *
 * <p>
 * Encapsulates results of duplicate content analysis including similarity
 * scores,
 * potential duplicate articles, and recommendations for handling duplicates.
 * </p>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DuplicateDetectionResult {

    /**
     * Whether potential duplicates were found.
     */
    private boolean hasDuplicates;

    /**
     * List of potential duplicate article IDs.
     */
    private List<Long> duplicateArticleIds;

    /**
     * List of potential duplicate article titles.
     */
    private List<String> duplicateTitles;

    /**
     * Similarity scores for each potential duplicate (0-100).
     */
    private List<Double> similarityScores;

    /**
     * Whether the content is a clear duplicate (high confidence).
     */
    private boolean isClearDuplicate;

    /**
     * Recommended action for handling potential duplicates.
     */
    private String recommendedAction;

    /**
     * Detection confidence score (0-100).
     */
    private double confidenceScore;
}