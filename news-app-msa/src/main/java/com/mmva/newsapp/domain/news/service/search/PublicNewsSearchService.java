package com.mmva.newsapp.domain.news.service.search;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.mmva.newsapp.domain.news.model.elasticsearch.NewsSearchDocument;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Service interface for public news search operations.
 *
 * <p>
 * Provides comprehensive search functionality for public users,
 * including full-text search, faceted filtering, and geographic search
 * capabilities. All searches are restricted to PUBLISHED content only.
 * </p>
 *
 * <h3>Key Features:</h3>
 * <ul>
 * <li>Full-text search across title, content, and metadata (35-field public subset)</li>
 * <li>Category and country filtering</li>
 * <li>Tag-based search</li>
 * <li>Date range filtering</li>
 * <li>Geographic bounding box search</li>
 * <li>Content type filtering (breaking news, sponsored, premium)</li>
 * <li>Resilient search with automatic DB fallback if Elasticsearch fails</li>
 * </ul>
 *
 * <h3>Search Resilience:</h3>
 * <ul>
 * <li>Primary: Elasticsearch search (fast, ~50ms)</li>
 * <li>Fallback: Database search via JPA Specifications (reliable, ~100-200ms)</li>
 * <li>Result Mapping: NewsSearchDocument → PublicNewsElasticSearchResponseDto (35 public fields)</li>
 * </ul>
 *
 * <h3>Public Content Restrictions:</h3>
 * <ul>
 * <li>Only PUBLISHED articles returned</li>
 * <li>No draft, archived, or unpublished content</li>
 * <li>No workflow status information exposed</li>
 * <li>Limited metadata exposure (35 fields max)</li>
 * </ul>
 *
 * <p>
 * For admin search capabilities with all statuses, use {@code AdminNewsSearchService}.
 * </p>
 *
 * @author MMVA Team
 * @version 1.0
 * @since 2026-03-07
 * @see com.mmva.newsapp.domain.news.service.search.AdminNewsSearchService
 */
public interface PublicNewsSearchService {

    /**
     * Perform basic full-text search across all searchable content.
     *
     * <p>
     * Searches across titles, content, and excerpts in multiple languages.
     * Implements ES-first search with automatic DB fallback.
     * </p>
     *
     * @param query    Search query string
     * @param pageable Pagination information
     * @return Page of search results (35 public fields only)
     */
    Page<NewsSearchDocument> basicSearch(String query, Pageable pageable);

    /**
     * Perform advanced search with multiple filters.
     *
     * <p>
     * Combines text search with category, country, tag, and date filters.
     * Implements ES-first search with automatic DB fallback.
     * </p>
     *
     * @param query        Search query (optional, for pure filtering set to null or empty)
     * @param categoryIds  List of category IDs to filter by (optional)
     * @param countryCodes List of country codes to filter by (optional)
     * @param tags         List of tags to filter by (optional)
     * @param startDate    Start date for publication date filter (optional)
     * @param endDate      End date for publication date filter (optional)
     * @param pageable     Pagination information
     * @return Page of filtered search results
     */
    Page<NewsSearchDocument> advancedSearch(
        String query,
        List<UUID> categoryIds,
        List<String> countryCodes,
        List<String> tags,
        Instant startDate,
        Instant endDate,
        Pageable pageable
    );

    /**
     * Search news within a geographic bounding box.
     *
     * <p>
     * Returns news articles within specified geographic boundaries.
     * Implements ES-first geo-spatial search with automatic DB fallback.
     * </p>
     *
     * @param query          Optional search query to combine with location filter
     * @param topLeftLat     Top-left latitude of bounding box
     * @param topLeftLon     Top-left longitude of bounding box
     * @param bottomRightLat Bottom-right latitude of bounding box
     * @param bottomRightLon Bottom-right longitude of bounding box
     * @param pageable       Pagination information
     * @return Page of location-filtered results
     */
    Page<NewsSearchDocument> searchByLocation(
        String query,
        double topLeftLat,
        double topLeftLon,
        double bottomRightLat,
        double bottomRightLon,
        Pageable pageable
    );

    /**
     * Search only by category.
     *
     * <p>
     * Convenience method for category-specific browsing.
     * </p>
     *
     * @param categoryId The category ID to search within
     * @param pageable   Pagination information
     * @return Page of articles in the specified category
     */
    Page<NewsSearchDocument> searchByCategory(UUID categoryId, Pageable pageable);

    /**
     * Search only by country.
     *
     * <p>
     * Convenience method for country-specific news discovery.
     * </p>
     *
     * @param countryCode Country code (ISO 2-letter)
     * @param pageable    Pagination information
     * @return Page of articles from the specified country
     */
    Page<NewsSearchDocument> searchByCountry(String countryCode, Pageable pageable);

    /**
     * Search for breaking news articles.
     *
     * <p>
     * Returns only articles marked as breaking news.
     * </p>
     *
     * @param pageable Pagination information
     * @return Page of breaking news articles
     */
    Page<NewsSearchDocument> searchBreakingNews(Pageable pageable);

    /**
     * Search for sponsored content.
     *
     * <p>
     * Returns only articles marked as sponsored.
     * </p>
     *
     * @param pageable Pagination information
     * @return Page of sponsored content articles
     */
    Page<NewsSearchDocument> searchSponsoredContent(Pageable pageable);

    /**
     * Search for premium content.
     *
     * <p>
     * Returns only articles marked as premium.
     * </p>
     *
     * @param pageable Pagination information
     * @return Page of premium content articles
     */
    Page<NewsSearchDocument> searchPremiumContent(Pageable pageable);

    /**
     * Search by date range only.
     *
     * <p>
     * Returns articles published within the specified date range.
     * </p>
     *
     * @param startDate Start date (inclusive)
     * @param endDate   End date (inclusive)
     * @param pageable  Pagination information
     * @return Page of articles within the date range
     */
    Page<NewsSearchDocument> searchByDateRange(
        Instant startDate,
        Instant endDate,
        Pageable pageable
    );
}
