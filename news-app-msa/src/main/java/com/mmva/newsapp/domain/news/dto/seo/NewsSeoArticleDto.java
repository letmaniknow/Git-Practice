package com.mmva.newsapp.domain.news.dto.seo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * Article-specific meta tags DTO.
 *
 * <p>
 * Contains meta tags specific to news articles for enhanced
 * search engine understanding and social media sharing.
 * </p>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsSeoArticleDto {

    /**
     * Article author name.
     */
    private String author;

    /**
     * Article section/category.
     */
    private String section;

    /**
     * Article tags/keywords.
     */
    private List<String> tags;

    /**
     * Article published time.
     */
    private Instant publishedTime;

    /**
     * Article modified time.
     */
    private Instant modifiedTime;

    /**
     * Article word count.
     */
    private Integer wordCount;

    /**
     * Estimated reading time in minutes.
     */
    private Integer readingTime;

    /**
     * Article content rating (for editorial content).
     */
    private String contentRating;

    /**
     * Whether this is breaking news.
     */
    private Boolean isBreaking;

    /**
     * Article urgency level.
     */
    private String urgencyLevel;

    /**
     * Target audience.
     */
    private String targetAudience;
}