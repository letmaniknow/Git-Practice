package com.mmva.newsapp.domain.news.service.elasticsearch;

import com.mmva.newsapp.domain.news.model.elasticsearch.NewsSearchDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Service interface for advanced news search functionality using Elasticsearch.
 *
 * <p>
 * Provides comprehensive search capabilities including full-text search,
 * faceted search, filtering, and advanced query operations for news articles.
 * </p>
 *
 * <h3>Key Features:</h3>
 * <ul>
 * <li>Full-text search across titles, content, and excerpts in multiple
 * languages</li>
 * <li>Faceted search with category, geographic, and content type filters</li>
 * <li>Date range and geographic bounding box filtering</li>
 * <li>Autocomplete and suggestion functionality</li>
 * <li>Search result aggregation for UI facets</li>
 * <li>Index management (create, update, delete documents)</li>
 * <li>Search analytics and performance monitoring</li>
 * </ul>
 *
 * <h3>Search Strategies:</h3>
 * <ul>
 * <li><b>Basic Search:</b> Simple keyword search across all content</li>
 * <li><b>Advanced Search:</b> Multi-field search with filters and
 * operators</li>
 * <li><b>Faceted Search:</b> Category-based filtering with result counts</li>
 * <li><b>Geographic Search:</b> Location-based news discovery</li>
 * <li><b>Autocomplete:</b> Real-time suggestions as users type</li>
 * </ul>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
public interface NewsElasticSearchService {

        // ========================================
        // Index Management Operations
        // ========================================

        /**
         * Index a news article for search.
         * Creates or updates the search document in Elasticsearch.
         *
         * @param newsId The UUID of the news article to index
         */
        void indexNewsArticle(UUID newsId);

        /**
         * Index multiple news articles for search.
         * Batch operation for efficient indexing of multiple articles.
         *
         * @param newsIds List of news article UUIDs to index
         */
        void indexNewsArticles(List<UUID> newsIds);

        /**
         * Remove a news article from the search index.
         *
         * @param newsId The UUID of the news article to remove
         */
        void removeFromIndex(UUID newsId);

        /**
         * Remove multiple news articles from the search index.
         *
         * @param newsIds List of news article UUIDs to remove
         */
        void removeFromIndex(List<UUID> newsIds);

        /**
         * Reindex all published news articles.
         * Useful for initial setup or after schema changes.
         */
        void reindexAllPublishedNews();

        // ========================================
        // Basic Search Operations
        // ========================================

        /**
         * Perform a basic full-text search across all searchable content.
         *
         * @param query    The search query string
         * @param pageable Pagination information
         * @return Page of search results
         */
        Page<NewsSearchDocument> search(String query, Pageable pageable);

        /**
         * Search news by title only.
         *
         * @param titleQuery The title search query
         * @param pageable   Pagination information
         * @return Page of search results
         */
        Page<NewsSearchDocument> searchByTitle(String titleQuery, Pageable pageable);

        /**
         * Search news by content only.
         *
         * @param contentQuery The content search query
         * @param pageable     Pagination information
         * @return Page of search results
         */
        Page<NewsSearchDocument> searchByContent(String contentQuery, Pageable pageable);

        // ========================================
        // Advanced Search Operations
        // ========================================

        /**
         * Perform advanced search with multiple filters.
         *
         * @param query        The main search query
         * @param categoryIds  List of category IDs to filter by
         * @param countryCodes List of country codes to filter by
         * @param tags         List of tags to filter by
         * @param startDate    Start date for publication date filter
         * @param endDate      End date for publication date filter
         * @param pageable     Pagination information
         * @return Page of filtered search results
         */
        Page<NewsSearchDocument> advancedSearch(String query, List<UUID> categoryIds,
                        List<String> countryCodes, List<String> tags,
                        Instant startDate, Instant endDate, Pageable pageable);

        /**
         * Search news within a geographic bounding box.
         *
         * @param query          The search query
         * @param topLeftLat     Top-left latitude of bounding box
         * @param topLeftLon     Top-left longitude of bounding box
         * @param bottomRightLat Bottom-right latitude of bounding box
         * @param bottomRightLon Bottom-right longitude of bounding box
         * @param pageable       Pagination information
         * @return Page of location-filtered search results
         */
        Page<NewsSearchDocument> searchByLocation(String query, double topLeftLat, double topLeftLon,
                        double bottomRightLat, double bottomRightLon,
                        Pageable pageable);

        // ========================================
        // Faceted Search Operations
        // ========================================

        /**
         * Search news by category with optional text query.
         *
         * @param categoryId The category ID
         * @param query      Optional text query to combine with category filter
         * @param pageable   Pagination information
         * @return Page of category-filtered results
         */
        Page<NewsSearchDocument> searchByCategory(UUID categoryId, String query, Pageable pageable);

        /**
         * Search news by country.
         *
         * @param countryCode The country code (ISO 2-letter)
         * @param query       Optional text query to combine with country filter
         * @param pageable    Pagination information
         * @return Page of country-filtered results
         */
        Page<NewsSearchDocument> searchByCountry(String countryCode, String query, Pageable pageable);

        /**
         * Search news by tags.
         *
         * @param tags     List of tags to search for
         * @param query    Optional text query to combine with tag filter
         * @param pageable Pagination information
         * @return Page of tag-filtered results
         */
        Page<NewsSearchDocument> searchByTags(List<String> tags, String query, Pageable pageable);

        // ========================================
        // Content Type Filtering
        // ========================================

        /**
         * Search breaking news.
         *
         * @param query    Optional text query
         * @param pageable Pagination information
         * @return Page of breaking news results
         */
        Page<NewsSearchDocument> searchBreakingNews(String query, Pageable pageable);

        /**
         * Search sponsored content.
         *
         * @param query    Optional text query
         * @param pageable Pagination information
         * @return Page of sponsored content results
         */
        Page<NewsSearchDocument> searchSponsoredContent(String query, Pageable pageable);

        /**
         * Search premium content.
         *
         * @param query    Optional text query
         * @param pageable Pagination information
         * @return Page of premium content results
         */
        Page<NewsSearchDocument> searchPremiumContent(String query, Pageable pageable);

        // ========================================
        // Autocomplete and Suggestions
        // ========================================

        /**
         * Get autocomplete suggestions for titles.
         *
         * @param partialTitle   The partial title to complete
         * @param maxSuggestions Maximum number of suggestions to return
         * @return List of title suggestions
         */
        List<String> getTitleSuggestions(String partialTitle, int maxSuggestions);

        /**
         * Get popular search terms.
         *
         * @param limit Maximum number of terms to return
         * @return List of popular search terms
         */
        List<String> getPopularSearchTerms(int limit);

        // ========================================
        // Aggregation Operations (for UI facets)
        // ========================================

        /**
         * Get category aggregations for current search results.
         * Used to build dynamic category filters in the search UI.
         *
         * @param query Optional query to filter aggregations
         * @return Map of category ID to count
         */
        java.util.Map<UUID, Long> getCategoryAggregations(String query);

        /**
         * Get country aggregations for current search results.
         *
         * @param query Optional query to filter aggregations
         * @return Map of country code to count
         */
        java.util.Map<String, Long> getCountryAggregations(String query);

        /**
         * Get tag aggregations for current search results.
         *
         * @param query Optional query to filter aggregations
         * @return Map of tag to count
         */
        java.util.Map<String, Long> getTagAggregations(String query);

        // ========================================
        // Search Analytics
        // ========================================

        /**
         * Get search performance metrics.
         *
         * @return Map containing search metrics (response time, result count, etc.)
         */
        java.util.Map<String, Object> getSearchMetrics();

        /**
         * Log a search query for analytics.
         *
         * @param query          The search query
         * @param resultCount    Number of results returned
         * @param responseTimeMs Response time in milliseconds
         */
        void logSearchQuery(String query, long resultCount, long responseTimeMs);
}