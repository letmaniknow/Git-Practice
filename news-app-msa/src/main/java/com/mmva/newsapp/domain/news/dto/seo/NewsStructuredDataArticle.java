package com.mmva.newsapp.domain.news.dto.seo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * DTO for Schema.org NewsArticle structured data.
 *
 * <p>
 * Represents the complete structured data object for a news article
 * following Schema.org NewsArticle specification for enhanced SEO.
 * </p>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Data
@Builder
public class NewsStructuredDataArticle {

    /**
     * Schema.org context URL.
     */
    private String context;

    /**
     * Schema.org type (NewsArticle).
     */
    private String type;

    /**
     * Article headline.
     */
    private String headline;

    /**
     * Article description.
     */
    private String description;

    /**
     * Article images.
     */
    private List<String> image;

    /**
     * Publication date in ISO 8601 format.
     */
    private String datePublished;

    /**
     * Last modified date in ISO 8601 format.
     */
    private String dateModified;

    /**
     * Article author.
     */
    private NewsStructuredDataPerson author;

    /**
     * Article publisher.
     */
    private NewsStructuredDataOrganization publisher;

    /**
     * Main entity of page.
     */
    private NewsStructuredDataWebPage mainEntityOfPage;

    /**
     * Article section.
     */
    private String articleSection;

    /**
     * Article keywords.
     */
    private List<String> keywords;

    /**
     * Speakable specification for voice search.
     */
    private NewsStructuredDataSpeakableSpec speakable;
}