package com.mmva.newsapp.controller.open.news;

import com.mmva.newsapp.domain.news.dto.elasticsearch.PublicNewsElasticSearchRequestDto;
import com.mmva.newsapp.domain.news.dto.elasticsearch.PublicNewsElasticSearchResponseDto;
import com.mmva.newsapp.domain.news.mapper.elasticsearch.NewsElasticSearchMapper;
import com.mmva.newsapp.domain.news.model.elasticsearch.NewsSearchDocument;
import com.mmva.newsapp.domain.news.service.elasticsearch.NewsElasticSearchService;
import com.mmva.newsapp.domain.news.service.search.PublicNewsSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import java.util.Map;

/**
 * REST controller for advanced news search operations using Elasticsearch.
 *
 * <p>
 * Provides comprehensive search endpoints including full-text search,
 * faceted search, filtering, autocomplete, and search analytics.
 * </p>
 *
 * <h3>Search Features:</h3>
 * <ul>
 * <li>Full-text search across titles, content, and excerpts</li>
 * <li>Faceted search with category, geographic, and content type filters</li>
 * <li>Geographic bounding box search</li>
 * <li>Date range filtering</li>
 * <li>Autocomplete and suggestions</li>
 * <li>Search result aggregations for UI facets</li>
 * <li>Search analytics and performance metrics</li>
 * </ul>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/elastic/search")
@RequiredArgsConstructor
@Slf4j
@Validated
@ConditionalOnProperty(name = "elasticsearch.enabled", havingValue = "true", matchIfMissing = false)
@Tag(name = "News Elastic Search API", description = "Advanced search operations with Elasticsearch")
public class PublicNewsElasticSearchController {

    private final PublicNewsSearchService publicNewsSearchService;
    private final NewsElasticSearchService newsElasticSearchService;
    private final NewsElasticSearchMapper newsElasticSearchMapper;

    /**
     * Perform advanced search with multiple filters.
     *
     * @param request Search request parameters
     * @return Search results with pagination and aggregations
     */
    @PostMapping("/advanced")
    @Operation(summary = "Advanced Search", description = "Perform comprehensive search with multiple filters and faceted results")
    public ResponseEntity<PublicNewsElasticSearchResponseDto> advancedSearch(
            @Parameter(description = "Search request parameters") @Valid @RequestBody PublicNewsElasticSearchRequestDto request) {

        log.info("Advanced search request: query={}, categories={}, countries={}",
                request.getQuery(), request.getCategoryIds(), request.getCountryCodes());

        long startTime = System.currentTimeMillis();

        // Build pageable from request
        Pageable pageable = buildPageable(request);

        // Perform search via resilient public search service (ES-first with DB
        // fallback)
        Page<NewsSearchDocument> searchResults;
        if (request.getBounds() != null) {
            // Geographic search
            searchResults = publicNewsSearchService.searchByLocation(
                    request.getQuery(),
                    request.getBounds().getTopLeftLatitude(),
                    request.getBounds().getTopLeftLongitude(),
                    request.getBounds().getBottomRightLatitude(),
                    request.getBounds().getBottomRightLongitude(),
                    pageable);
        } else {
            // Advanced search with filters
            searchResults = publicNewsSearchService.advancedSearch(
                    request.getQuery(),
                    request.getCategoryIds(),
                    request.getCountryCodes(),
                    request.getTags(),
                    request.getStartDate(),
                    request.getEndDate(),
                    pageable);
        }

        long executionTime = System.currentTimeMillis() - startTime;

        // Convert to response DTO
        PublicNewsElasticSearchResponseDto response = buildSearchResponse(searchResults, request, executionTime);

        // Log search analytics
        newsElasticSearchService.logSearchQuery(request.getQuery(),
                searchResults.getTotalElements(), executionTime);

        log.info("Advanced search completed: {} results in {}ms", searchResults.getTotalElements(), executionTime);

        return ResponseEntity.ok(response);
    }

    /**
     * Perform basic full-text search.
     *
     * @param query Search query string
     * @param page  Page number (0-based)
     * @param size  Page size
     * @return Search results
     */
    @GetMapping("/basic")
    @Operation(summary = "Basic Search", description = "Perform simple full-text search across all content")
    public ResponseEntity<PublicNewsElasticSearchResponseDto> basicSearch(
            @Parameter(description = "Search query string", required = true) @RequestParam String query,

            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") @Min(0) Integer page,

            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") @Min(1) @Max(100) Integer size) {

        log.info("Basic search request: query={}, page={}, size={}", query, page, size);

        long startTime = System.currentTimeMillis();

        Pageable pageable = PageRequest.of(page, size);
        // Use resilient public search service (ES-first with DB fallback)
        Page<NewsSearchDocument> searchResults = publicNewsSearchService.basicSearch(query, pageable);

        long executionTime = System.currentTimeMillis() - startTime;

        PublicNewsElasticSearchRequestDto request = PublicNewsElasticSearchRequestDto.builder()
                .query(query)
                .page(page)
                .size(size)
                .build();

        PublicNewsElasticSearchResponseDto response = buildSearchResponse(searchResults, request, executionTime);

        newsElasticSearchService.logSearchQuery(query, searchResults.getTotalElements(), executionTime);

        log.info("Basic search completed: {} results in {}ms", searchResults.getTotalElements(), executionTime);

        return ResponseEntity.ok(response);
    }

    /**
     * Search by category.
     *
     * @param categoryId Category ID
     * @param query      Optional text query
     * @param page       Page number
     * @param size       Page size
     * @return Category-filtered search results
     */
    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Search by Category", description = "Search news articles within a specific category")
    public ResponseEntity<PublicNewsElasticSearchResponseDto> searchByCategory(
            @Parameter(description = "Category ID", required = true) @PathVariable java.util.UUID categoryId,

            @Parameter(description = "Optional text query") @RequestParam(required = false) String query,

            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") @Min(0) Integer page,

            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") @Min(1) @Max(100) Integer size) {

        log.info("Category search: categoryId={}, query={}, page={}, size={}", categoryId, query, page, size);

        long startTime = System.currentTimeMillis();

        Pageable pageable = PageRequest.of(page, size);
        // Use resilient public search service (ES-first with DB fallback)
        Page<NewsSearchDocument> searchResults = publicNewsSearchService
                .searchByCategory(categoryId, pageable);

        long executionTime = System.currentTimeMillis() - startTime;

        PublicNewsElasticSearchRequestDto request = PublicNewsElasticSearchRequestDto.builder()
                .query(query)
                .categoryIds(List.of(categoryId))
                .page(page)
                .size(size)
                .build();

        PublicNewsElasticSearchResponseDto response = buildSearchResponse(searchResults, request, executionTime);

        log.info("Category search completed: {} results in {}ms", searchResults.getTotalElements(), executionTime);

        return ResponseEntity.ok(response);
    }

    /**
     * Search by country.
     *
     * @param countryCode Country code (ISO 2-letter)
     * @param query       Optional text query
     * @param page        Page number
     * @param size        Page size
     * @return Country-filtered search results
     */
    @GetMapping("/country/{countryCode}")
    @Operation(summary = "Search by Country", description = "Search news articles from a specific country")
    public ResponseEntity<PublicNewsElasticSearchResponseDto> searchByCountry(
            @Parameter(description = "Country code (ISO 2-letter)", required = true) @PathVariable String countryCode,

            @Parameter(description = "Optional text query") @RequestParam(required = false) String query,

            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") @Min(0) Integer page,

            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") @Min(1) @Max(100) Integer size) {

        log.info("Country search: countryCode={}, query={}, page={}, size={}", countryCode, query, page, size);

        long startTime = System.currentTimeMillis();

        Pageable pageable = PageRequest.of(page, size);
        // Use resilient public search service (ES-first with DB fallback)
        Page<NewsSearchDocument> searchResults = publicNewsSearchService
                .searchByCountry(countryCode, pageable);

        long executionTime = System.currentTimeMillis() - startTime;

        PublicNewsElasticSearchRequestDto request = PublicNewsElasticSearchRequestDto.builder()
                .query(query)
                .countryCodes(List.of(countryCode))
                .page(page)
                .size(size)
                .build();

        PublicNewsElasticSearchResponseDto response = buildSearchResponse(searchResults, request, executionTime);

        log.info("Country search completed: {} results in {}ms", searchResults.getTotalElements(), executionTime);

        return ResponseEntity.ok(response);
    }

    /**
     * Get autocomplete suggestions for titles.
     *
     * @param partialTitle   Partial title to complete
     * @param maxSuggestions Maximum number of suggestions
     * @return List of title suggestions
     */
    @GetMapping("/autocomplete/titles")
    @Operation(summary = "Title Autocomplete", description = "Get autocomplete suggestions for news titles")
    public ResponseEntity<List<String>> getTitleSuggestions(
            @Parameter(description = "Partial title to complete", required = true) @RequestParam String partialTitle,

            @Parameter(description = "Maximum number of suggestions") @RequestParam(defaultValue = "10") @Min(1) @Max(50) Integer maxSuggestions) {

        log.debug("Title autocomplete request: partialTitle={}, maxSuggestions={}", partialTitle, maxSuggestions);

        List<String> suggestions = newsElasticSearchService.getTitleSuggestions(partialTitle, maxSuggestions);

        log.debug("Title autocomplete returned {} suggestions", suggestions.size());

        return ResponseEntity.ok(suggestions);
    }

    /**
     * Get search aggregations for building filter UI.
     *
     * @param query Optional query to filter aggregations
     * @return Search aggregations for faceted search UI
     */
    @GetMapping("/aggregations")
    @Operation(summary = "Search Aggregations", description = "Get aggregations for building search filter UI components")
    public ResponseEntity<PublicNewsElasticSearchResponseDto.SearchAggregationsDto> getSearchAggregations(
            @Parameter(description = "Optional query to filter aggregations") @RequestParam(required = false) String query) {

        log.debug("Search aggregations request: query={}", query);

        Map<java.util.UUID, Long> categories = newsElasticSearchService.getCategoryAggregations(query);
        Map<String, Long> countries = newsElasticSearchService.getCountryAggregations(query);
        Map<String, Long> tags = newsElasticSearchService.getTagAggregations(query);

        PublicNewsElasticSearchResponseDto.SearchAggregationsDto aggregations = PublicNewsElasticSearchResponseDto.SearchAggregationsDto
                .builder()
                .categories(categories)
                .countries(countries)
                .tags(tags)
                .build();

        log.debug("Search aggregations returned: categories={}, countries={}, tags={}",
                categories.size(), countries.size(), tags.size());

        return ResponseEntity.ok(aggregations);
    }

    /**
     * Get search performance metrics.
     *
     * @return Search performance metrics
     */
    @GetMapping("/metrics")
    @Operation(summary = "Search Metrics", description = "Get search performance and usage metrics")
    public ResponseEntity<Map<String, Object>> getSearchMetrics() {

        log.debug("Search metrics request");

        Map<String, Object> metrics = newsElasticSearchService.getSearchMetrics();

        return ResponseEntity.ok(metrics);
    }

    // ========================================
    // Helper Methods
    // ========================================

    /**
     * Build Pageable from search request.
     */
    private Pageable buildPageable(PublicNewsElasticSearchRequestDto request) {
        Sort sort = Sort.unsorted();

        if (request.getSortBy() != null && !request.getSortBy().trim().isEmpty()) {
            Sort.Direction direction = "desc".equalsIgnoreCase(request.getSortDirection())
                    ? Sort.Direction.DESC
                    : Sort.Direction.ASC;

            switch (request.getSortBy().toLowerCase()) {
                case "published_at":
                    sort = Sort.by(direction, "newsPublishedAt");
                    break;
                case "title":
                    sort = Sort.by(direction, "newsTitleEn");
                    break;
                case "relevance":
                default:
                    // Elasticsearch handles relevance scoring by default
                    sort = Sort.unsorted();
                    break;
            }
        }

        return PageRequest.of(request.getPage(), request.getSize(), sort);
    }

    /**
     * Build search response from search results.
     */
    private PublicNewsElasticSearchResponseDto buildSearchResponse(
            Page<NewsSearchDocument> searchResults,
            PublicNewsElasticSearchRequestDto request, long executionTime) {

        // Convert search documents to response DTOs
        List<PublicNewsElasticSearchResponseDto.NewsElasticSearchResultDto> results = searchResults.getContent()
                .stream()
                .map(newsElasticSearchMapper::toResultDto)
                .toList();

        // Build pagination metadata
        PublicNewsElasticSearchResponseDto.PaginationMetadataDto pagination = PublicNewsElasticSearchResponseDto.PaginationMetadataDto
                .builder()
                .page(searchResults.getNumber())
                .size(searchResults.getSize())
                .totalElements(searchResults.getTotalElements())
                .totalPages(searchResults.getTotalPages())
                .first(searchResults.isFirst())
                .last(searchResults.isLast())
                .numberOfElements(searchResults.getNumberOfElements())
                .build();

        // Build search metadata
        PublicNewsElasticSearchResponseDto.SearchMetadataDto metadata = PublicNewsElasticSearchResponseDto.SearchMetadataDto
                .builder()
                .query(request.getQuery())
                .executionTimeMs(executionTime)
                .totalResults(searchResults.getTotalElements())
                .appliedFilters(buildAppliedFiltersList(request))
                .sortBy(request.getSortBy())
                .sortDirection(request.getSortDirection())
                .build();

        // Build aggregations (simplified for now)
        PublicNewsElasticSearchResponseDto.SearchAggregationsDto aggregations = PublicNewsElasticSearchResponseDto.SearchAggregationsDto
                .builder()
                .categories(newsElasticSearchService.getCategoryAggregations(request.getQuery()))
                .countries(newsElasticSearchService.getCountryAggregations(request.getQuery()))
                .tags(newsElasticSearchService.getTagAggregations(request.getQuery()))
                .build();

        return PublicNewsElasticSearchResponseDto.builder()
                .results(results)
                .pagination(pagination)
                .metadata(metadata)
                .aggregations(aggregations)
                .build();
    }

    /**
     * Build list of applied filters for metadata.
     */
    private List<String> buildAppliedFiltersList(PublicNewsElasticSearchRequestDto request) {
        List<String> filters = new java.util.ArrayList<>();

        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            filters.add("categories: " + request.getCategoryIds().size());
        }
        if (request.getCountryCodes() != null && !request.getCountryCodes().isEmpty()) {
            filters.add("countries: " + request.getCountryCodes().size());
        }
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            filters.add("tags: " + request.getTags().size());
        }
        if (request.getStartDate() != null || request.getEndDate() != null) {
            filters.add("date_range");
        }
        if (request.getBounds() != null) {
            filters.add("geographic_bounds");
        }
        if (Boolean.TRUE.equals(request.getBreakingNewsOnly())) {
            filters.add("breaking_news_only");
        }
        if (Boolean.TRUE.equals(request.getSponsoredOnly())) {
            filters.add("sponsored_only");
        }
        if (Boolean.TRUE.equals(request.getPremiumOnly())) {
            filters.add("premium_only");
        }

        return filters;
    }
}