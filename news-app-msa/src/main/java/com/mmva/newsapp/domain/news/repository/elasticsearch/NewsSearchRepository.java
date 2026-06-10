package com.mmva.newsapp.domain.news.repository.elasticsearch;

import com.mmva.newsapp.domain.news.model.elasticsearch.NewsSearchDocument;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Elasticsearch repository for advanced news search functionality.
 *
 * <p>
 * Provides full-text search, faceted search, and filtering capabilities
 * for news articles stored in Elasticsearch.
 * </p>
 *
 * <h3>Search Capabilities:</h3>
 * <ul>
 * <li>Full-text search across titles, content, and excerpts</li>
 * <li>Multi-language support (English/Spanish)</li>
 * <li>Faceted search by category, country, region, tags</li>
 * <li>Date range filtering</li>
 * <li>Geographic filtering with location-based search</li>
 * <li>Content type filtering (breaking, sponsored, premium)</li>
 * <li>Urgency level and audience targeting</li>
 * <li>Source agency filtering</li>
 * <li>Autocomplete and suggestion features</li>
 * </ul>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Repository
@ConditionalOnProperty(name = "elasticsearch.enabled", havingValue = "true", matchIfMissing = false)
public interface NewsSearchRepository extends ElasticsearchRepository<NewsSearchDocument, UUID> {

    // ========================================
    // Basic Search Operations
    // ========================================

    /**
     * Full-text search across all searchable content fields.
     * Searches titles, content, excerpts, and combined searchable content.
     */
    Page<NewsSearchDocument> findBySearchableContent(String searchableContent, Pageable pageable);

    /**
     * Search by title in English.
     */
    Page<NewsSearchDocument> findByNewsTitleEn(String title, Pageable pageable);

    /**
     * Search by title in Spanish.
     */
    Page<NewsSearchDocument> findByNewsTitleEs(String title, Pageable pageable);

    /**
     * Search by content in English.
     */
    Page<NewsSearchDocument> findByNewsContentEn(String content, Pageable pageable);

    /**
     * Search by content in Spanish.
     */
    Page<NewsSearchDocument> findByNewsContentEs(String content, Pageable pageable);

    // ========================================
    // Faceted Search Operations
    // ========================================

    /**
     * Find news by category.
     */
    Page<NewsSearchDocument> findByNewsNewsCategoryId(UUID categoryId, Pageable pageable);

    /**
     * Find news by country.
     */
    Page<NewsSearchDocument> findByNewsCountryCode(String countryCode, Pageable pageable);

    /**
     * Find news by region.
     */
    Page<NewsSearchDocument> findByNewsRegion(String region, Pageable pageable);

    /**
     * Find news by city.
     */
    Page<NewsSearchDocument> findByNewsCity(String city, Pageable pageable);

    /**
     * Find news by tags (contains any of the specified tags).
     */
    @Query("{\"bool\": {\"must\": [{\"terms\": {\"newsTags\": ?0}}]}}")
    Page<NewsSearchDocument> findByNewsTags(List<String> tags, Pageable pageable);

    /**
     * Find news by keywords (contains any of the specified keywords).
     */
    @Query("{\"bool\": {\"must\": [{\"terms\": {\"newsKeywords\": ?0}}]}}")
    Page<NewsSearchDocument> findByNewsKeywords(List<String> keywords, Pageable pageable);

    // ========================================
    // Content Type Filtering
    // ========================================

    /**
     * Find breaking news.
     */
    Page<NewsSearchDocument> findByNewsIsBreakingTrue(Pageable pageable);

    /**
     * Find sponsored content.
     */
    Page<NewsSearchDocument> findByNewsIsSponsoredTrue(Pageable pageable);

    /**
     * Find premium content.
     */
    Page<NewsSearchDocument> findByNewsIsPremiumTrue(Pageable pageable);

    /**
     * Find news by urgency level.
     */
    Page<NewsSearchDocument> findByNewsUrgencyLevel(String urgencyLevel, Pageable pageable);

    /**
     * Find news by target audience.
     */
    Page<NewsSearchDocument> findByNewsTargetAudience(String targetAudience, Pageable pageable);

    // ========================================
    // Source Filtering
    // ========================================

    /**
     * Find news by source agency.
     */
    Page<NewsSearchDocument> findByNewsSourceAgencyId(UUID sourceAgencyId, Pageable pageable);

    /**
     * Find news by source agency name.
     */
    Page<NewsSearchDocument> findByNewsSourceAgencyName(String sourceAgencyName, Pageable pageable);

    // ========================================
    // Date Range Filtering
    // ========================================

    /**
     * Find news published after a specific date.
     */
    Page<NewsSearchDocument> findByNewsPublishedAtAfter(Instant publishedAfter, Pageable pageable);

    /**
     * Find news published before a specific date.
     */
    Page<NewsSearchDocument> findByNewsPublishedAtBefore(Instant publishedBefore, Pageable pageable);

    /**
     * Find news published within a date range.
     */
    Page<NewsSearchDocument> findByNewsPublishedAtBetween(Instant startDate, Instant endDate, Pageable pageable);

    // ========================================
    // Geographic Search
    // ========================================

    /**
     * Find news within a geographic bounding box.
     * Note: This requires geo_point mapping in Elasticsearch.
     */
    @Query("{\"bool\": {\"filter\": {\"geo_bounding_box\": {\"location\": {\"top_left\": [?0, ?1], \"bottom_right\": [?2, ?3]}}}}}")
    Page<NewsSearchDocument> findByLocationWithinBoundingBox(double topLeftLon, double topLeftLat,
            double bottomRightLon, double bottomRightLat,
            Pageable pageable);

    // ========================================
    // Advanced Search Queries
    // ========================================

    /**
     * Advanced search with multiple filters using Elasticsearch query DSL.
     * This method allows complex queries combining full-text search with filters.
     */
    @Query("{\"bool\": {\"must\": [{\"multi_match\": {\"query\": \"?0\", \"fields\": [\"newsTitleEn^3\", \"newsTitleEs^3\", \"newsContentEn^2\", \"newsContentEs^2\", \"newsExcerptEn\", \"newsExcerptEs\", \"searchableContent\"]}}], \"filter\": [?1]}}")
    Page<NewsSearchDocument> advancedSearch(String query, String filterJson, Pageable pageable);

    /**
     * Autocomplete search for titles.
     * Returns suggestions based on partial title matches.
     */
    @Query("{\"multi_match\": {\"query\": \"?0\", \"fields\": [\"newsTitleEn\", \"newsTitleEs\"], \"type\": \"phrase_prefix\"}}")
    Page<NewsSearchDocument> autocompleteTitles(String partialTitle, Pageable pageable);

    // ========================================
    // Aggregation Queries (for faceted search UI)
    // ========================================

    /**
     * Get category aggregation for search results.
     * Used to build category filter facets in the UI.
     */
    @Query("{\"aggs\": {\"categories\": {\"terms\": {\"field\": \"newsNewsCategoryId\"}}}}")
    List<NewsSearchDocument> getCategoryAggregations();

    /**
     * Get country aggregation for search results.
     */
    @Query("{\"aggs\": {\"countries\": {\"terms\": {\"field\": \"newsCountryCode\"}}}}")
    List<NewsSearchDocument> getCountryAggregations();

    /**
     * Get tag aggregation for search results.
     */
    @Query("{\"aggs\": {\"tags\": {\"terms\": {\"field\": \"newsTags\"}}}}")
    List<NewsSearchDocument> getTagAggregations();
}