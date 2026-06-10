package com.mmva.newsapp.domain.news.model.elasticsearch;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Elasticsearch document for news articles search functionality.
 *
 * <p>
 * This document represents the searchable version of news articles stored in
 * Elasticsearch.
 * It includes all text fields optimized for full-text search, faceted search,
 * and filtering.
 * </p>
 *
 * <h3>Search Features:</h3>
 * <ul>
 * <li>Full-text search across titles, content, and excerpts in both
 * languages</li>
 * <li>Faceted search by category, country, region, city, tags</li>
 * <li>Date range filtering (published date)</li>
 * <li>Geographic filtering (latitude, longitude)</li>
 * <li>Content type filtering (breaking news, sponsored, premium)</li>
 * <li>Urgency level filtering</li>
 * <li>Source agency filtering</li>
 * </ul>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "news-articles")
@Setting(settingPath = "/elasticsearch/settings.json")
public class NewsSearchDocument {

    // ========================================
    // Primary Key (matches database entity)
    // ========================================

    @Id
    @Field(type = FieldType.Keyword)
    private UUID newsNewsId;

    @Field(type = FieldType.Keyword)
    private String newsSlug;

    // ========================================
    // Content - English (Searchable)
    // ========================================

    @Field(type = FieldType.Text, analyzer = "standard")
    private String newsTitleEn;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String newsContentEn;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String newsExcerptEn;

    // ========================================
    // Content - Spanish (Searchable)
    // ========================================

    @Field(type = FieldType.Text, analyzer = "standard")
    private String newsTitleEs;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String newsContentEs;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String newsExcerptEs;

    // ========================================
    // Metadata (Filterable & Searchable)
    // ========================================

    @Field(type = FieldType.Keyword)
    private UUID newsNewsCategoryId;

    @Field(type = FieldType.Keyword)
    private String newsCategoryName;

    @Field(type = FieldType.Keyword)
    private List<String> newsTags;

    @Field(type = FieldType.Keyword)
    private List<String> newsKeywords;

    // ========================================
    // Geographic Data (Filterable)
    // ========================================

    @Field(type = FieldType.Keyword)
    private String newsCountryCode;

    @Field(type = FieldType.Keyword)
    private String newsRegion;

    @Field(type = FieldType.Keyword)
    private String newsCity;

    @Field(type = FieldType.Double)
    private Double newsLatitude;

    @Field(type = FieldType.Double)
    private Double newsLongitude;

    // ========================================
    // Publishing & Status (Filterable)
    // ========================================

    @Field(type = FieldType.Keyword)
    private String newsWorkflowStatus;

    @Field(type = FieldType.Date)
    private Instant newsPublishedAt;

    @Field(type = FieldType.Boolean)
    private Boolean newsIsBreaking;

    @Field(type = FieldType.Boolean)
    private Boolean newsIsSponsored;

    @Field(type = FieldType.Boolean)
    private Boolean newsIsPremium;

    @Field(type = FieldType.Keyword)
    private String newsUrgencyLevel;

    @Field(type = FieldType.Keyword)
    private String newsTargetAudience;

    // ========================================
    // Source Information (Filterable)
    // ========================================

    @Field(type = FieldType.Keyword)
    private UUID newsSourceAgencyId;

    @Field(type = FieldType.Keyword)
    private String newsSourceAgencyName;

    @Field(type = FieldType.Text)
    private String newsSourceUrl;

    // ========================================
    // Media Information
    // ========================================

    @Field(type = FieldType.Keyword)
    private String newsMediaType;

    @Field(type = FieldType.Text)
    private String newsThumbnailUrl;

    // ========================================
    // Search Optimization Fields
    // ========================================

    /**
     * Combined searchable content from all text fields.
     * Used for general full-text search across the entire article.
     */
    @Field(type = FieldType.Text, analyzer = "standard")
    private String searchableContent;

    /**
     * Auto-generated excerpt if manual excerpt is not available.
     */
    @Field(type = FieldType.Boolean)
    private Boolean excerptAutoGenerated;

    // ========================================
    // Timestamps
    // ========================================

    @Field(type = FieldType.Date)
    private Instant createdAt;

    @Field(type = FieldType.Date)
    private Instant updatedAt;
}