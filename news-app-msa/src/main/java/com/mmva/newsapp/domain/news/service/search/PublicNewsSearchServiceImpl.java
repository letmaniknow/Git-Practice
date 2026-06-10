package com.mmva.newsapp.domain.news.service.search;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mmva.newsapp.domain.news.model.core.NewsMasterEntity;
import com.mmva.newsapp.domain.news.model.core.NewsMasterEntity.WorkflowStatus;
import com.mmva.newsapp.domain.news.model.elasticsearch.NewsSearchDocument;
import com.mmva.newsapp.domain.news.repository.core.NewsRepository;
import com.mmva.newsapp.domain.news.service.elasticsearch.NewsElasticSearchService;

import jakarta.persistence.criteria.Predicate;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of {@link PublicNewsSearchService} providing public-facing
 * search
 * and filtering capabilities.
 *
 * <p>
 * Implements industry-standard ES-first search pattern with automatic database
 * fallback.
 * All searches are restricted to PUBLISHED content only.
 * </p>
 *
 * <h3>Search Architecture:</h3>
 * <ul>
 * <li><b>Step 1:</b> Try Elasticsearch search (fast, ~50ms)</li>
 * <li><b>Step 2:</b> If ES fails → Fallback to database search via JPA
 * Specifications</li>
 * <li><b>Step 3:</b> Map results to NewsSearchDocument (35 public fields
 * only)</li>
 * <li><b>Step 4:</b> Return results with graceful error handling</li>
 * </ul>
 *
 * <h3>Key Design Patterns:</h3>
 * <ul>
 * <li>ES-First: Leverage Elasticsearch for fast full-text search</li>
 * <li>Graceful Fallback: Database search when ES unavailable</li>
 * <li>JPA Specifications: Dynamic query building for complex filters</li>
 * <li>Null-Safe Filtering: Optional parameters handled gracefully</li>
 * <li>Public Content Only: Automatic filtering for PUBLISHED status</li>
 * </ul>
 *
 * @author MMVA Team
 * @version 1.0
 * @since 2026-03-07
 * @see PublicNewsSearchService
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PublicNewsSearchServiceImpl implements PublicNewsSearchService {

    private final Optional<NewsElasticSearchService> newsElasticSearchService;
    private final NewsRepository newsRepository;

    /**
     * Perform basic full-text search across all searchable content.
     *
     * <p>
     * Implements ES-first pattern with Circuit Breaker:
     * 1. Circuit breaker attempts Elasticsearch (fast full-text search)
     * 2. If ES fails or circuit open: automatic fallback to database
     * 3. Filter for PUBLISHED content only
     * </p>
     */
    @Override
    public Page<NewsSearchDocument> basicSearch(String query, Pageable pageable) {
        log.debug("Public basic search requested: query='{}', page={}, size={}",
                query, pageable.getPageNumber(), pageable.getPageSize());

        // ============================================================
        // VALIDATION: Check query length to prevent DOS attacks
        // ============================================================
        if (query != null && query.length() > 500) {
            log.warn("Search query exceeds max length: {} chars (max 500)", query.length());
            throw new IllegalArgumentException("Search query must not exceed 500 characters");
        }

        // ============================================================
        // VALIDATION: Check pagination size to prevent memory exhaustion
        // ============================================================
        if (pageable.getPageSize() > 100) {
            log.warn("Page size {} exceeds maximum 100, clamping", pageable.getPageSize());
            pageable = org.springframework.data.domain.PageRequest.of(
                    pageable.getPageNumber(), 100, pageable.getSort());
        }
        if (pageable.getPageSize() <= 0) {
            log.warn("Invalid page size {}, using default 20", pageable.getPageSize());
            pageable = org.springframework.data.domain.PageRequest.of(
                    pageable.getPageNumber(), 20, pageable.getSort());
        }

        if (query == null || query.trim().isEmpty()) {
            log.debug("Empty query provided, returning empty results");
            return Page.empty(pageable);
        }

        // ============================================================
        // STEP 1: TRY ELASTICSEARCH FIRST with Circuit Breaker protection
        // ============================================================
        if (newsElasticSearchService.isPresent()) {
            try {
                // Circuit breaker wraps this call
                return elasticsearchSearchWithCircuitBreaker(query, pageable);
            } catch (Exception esError) {
                // ============================================================
                // STEP 2: ELASTICSEARCH FAILED - FALLBACK TO DATABASE
                // ============================================================
                log.warn("Elasticsearch search failed (circuit breaker fallback): {}",
                        esError.getMessage());
            }
        }

        // Fallback to database search
        return databaseSearchByQuery(query, pageable);
    }

    /**
     * Elasticsearch search wrapped with Circuit Breaker protection.
     * 
     * <p>
     * Circuit Breaker status:
     * - CLOSED: ES working normally → uses Elasticsearch
     * - OPEN: ES failing → skips attempt, goes to PostgreSQL
     * - HALF_OPEN: Testing recovery → tries one request
     * </p>
     */
    @CircuitBreaker(name = "elasticsearchSearch", fallbackMethod = "elasticsearchSearchFallback")
    private Page<NewsSearchDocument> elasticsearchSearchWithCircuitBreaker(String query, Pageable pageable) {
        log.debug("Circuit breaker: Attempting Elasticsearch search for query: '{}'", query);
        Page<NewsSearchDocument> esResults = newsElasticSearchService.get().search(query, pageable);

        if (esResults.getTotalElements() > 0) {
            log.info("✓ Elasticsearch search successful: {} results for '{}'",
                    esResults.getTotalElements(), query);
            return esResults;
        } else {
            log.debug("ES returned 0 results, using database");
            throw new RuntimeException("No ES results, triggering fallback");
        }
    }

    /**
     * Fallback method when circuit breaker opens (ES unavailable).
     * Called automatically when ES fails or circuit is OPEN.
     */
    public Page<NewsSearchDocument> elasticsearchSearchFallback(String query, Pageable pageable, Exception ex) {
        log.warn("Circuit breaker fallback triggered (ES unavailable): {}", ex.getMessage());
        return databaseSearchByQuery(query, pageable);
    }

    /**
     * Perform advanced search with multiple filters.
     *
     * <p>
     * Delegates to Elasticsearch advancedSearch with DB fallback.
     * </p>
     */
    @Override
    public Page<NewsSearchDocument> advancedSearch(
            String query,
            List<UUID> categoryIds,
            List<String> countryCodes,
            List<String> tags,
            Instant startDate,
            Instant endDate,
            Pageable pageable) {

        log.debug("Public advanced search: query='{}', categories={}, countries={}, tags={}, dates={}-{}",
                query, categoryIds, countryCodes, tags, startDate, endDate);

        // ============================================================
        // VALIDATION: Check query length to prevent DOS attacks
        // ============================================================
        if (query != null && query.length() > 500) {
            log.warn("Search query exceeds max length: {} chars (max 500)", query.length());
            throw new IllegalArgumentException("Search query must not exceed 500 characters");
        }

        // ============================================================
        // VALIDATION: Check pagination size to prevent memory exhaustion
        // ============================================================
        if (pageable.getPageSize() > 100) {
            log.warn("Page size {} exceeds maximum 100, clamping", pageable.getPageSize());
            pageable = org.springframework.data.domain.PageRequest.of(
                    pageable.getPageNumber(), 100, pageable.getSort());
        }
        if (pageable.getPageSize() <= 0) {
            log.warn("Invalid page size {}, using default 20", pageable.getPageSize());
            pageable = org.springframework.data.domain.PageRequest.of(
                    pageable.getPageNumber(), 20, pageable.getSort());
        }

        // ============================================================
        // STEP 1: TRY ELASTICSEARCH FIRST
        // ============================================================
        if (newsElasticSearchService.isPresent()) {
            try {
                log.debug("Attempting ES advanced search");
                Page<NewsSearchDocument> esResults = newsElasticSearchService.get().advancedSearch(
                        query, categoryIds, countryCodes, tags, startDate, endDate, pageable);

                if (esResults.getTotalElements() > 0) {
                    log.info("✓ Public ES advanced search successful: {} results",
                            esResults.getTotalElements());
                    return esResults;
                } else {
                    log.debug("ES returned 0 results for advanced search");
                }
            } catch (Exception esError) {
                log.warn("Elasticsearch advanced search failed (circuit breaker fallback): {}",
                        esError.getMessage());
            }
        }

        // ============================================================
        // STEP 2: FALLBACK TO DATABASE
        // ============================================================
        return databaseAdvancedSearch(query, categoryIds, countryCodes, tags, startDate, endDate, pageable);
    }

    /**
     * Elasticsearch advanced search wrapped with Circuit Breaker.
     */
    @CircuitBreaker(name = "elasticsearchSearch", fallbackMethod = "elasticsearchAdvancedSearchFallback")
    private Page<NewsSearchDocument> elasticsearchAdvancedSearchWithCircuitBreaker(
            String query,
            List<UUID> categoryIds,
            List<String> countryCodes,
            List<String> tags,
            Instant startDate,
            Instant endDate,
            Pageable pageable) {
        log.debug("Circuit breaker: Attempting ES advanced search");
        Page<NewsSearchDocument> esResults = newsElasticSearchService.get().advancedSearch(
                query, categoryIds, countryCodes, tags, startDate, endDate, pageable);

        if (esResults.getTotalElements() > 0) {
            log.info("✓ Elasticsearch advanced search successful: {} results",
                    esResults.getTotalElements());
            return esResults;
        } else {
            throw new RuntimeException("No ES results, triggering fallback");
        }
    }

    /**
     * Fallback for advanced search when circuit breaker opens.
     */
    public Page<NewsSearchDocument> elasticsearchAdvancedSearchFallback(
            String query,
            List<UUID> categoryIds,
            List<String> countryCodes,
            List<String> tags,
            Instant startDate,
            Instant endDate,
            Pageable pageable,
            Exception ex) {
        log.warn("Circuit breaker fallback triggered for advanced search (ES unavailable): {}", ex.getMessage());
        return databaseAdvancedSearch(query, categoryIds, countryCodes, tags, startDate, endDate, pageable);
    }

    /**
     * Search news within a geographic bounding box.
     *
     * <p>
     * Delegates to Elasticsearch searchByLocation with DB fallback.
     * </p>
     */
    @Override
    public Page<NewsSearchDocument> searchByLocation(
            String query,
            double topLeftLat,
            double topLeftLon,
            double bottomRightLat,
            double bottomRightLon,
            Pageable pageable) {

        log.debug("Public geographic search: query='{}', bounds=[({},{})-({},{})]",
                query, topLeftLat, topLeftLon, bottomRightLat, bottomRightLon);

        // ============================================================
        // STEP 1: TRY ELASTICSEARCH GEOGRAPHIC SEARCH
        // ============================================================
        if (newsElasticSearchService.isPresent()) {
            try {
                log.debug("Attempting ES geographic search");
                Page<NewsSearchDocument> esResults = newsElasticSearchService.get().searchByLocation(
                        query, topLeftLat, topLeftLon, bottomRightLat, bottomRightLon, pageable);

                if (esResults.getTotalElements() > 0) {
                    log.info("✓ Public ES geographic search successful: {} results",
                            esResults.getTotalElements());
                    return esResults;
                } else {
                    log.debug("ES geographic search returned 0 results");
                }
            } catch (Exception esError) {
                log.warn("ES geographic search failed (non-critical, falling back): {}",
                        esError.getMessage(), esError);
            }
        }

        // ============================================================
        // STEP 2: FALLBACK TO DATABASE GEOGRAPHIC SEARCH
        // ============================================================
        log.debug("Falling back to database geographic search");

        Specification<NewsMasterEntity> spec = (root, cq, cb) -> {
            List<Predicate> predicates = new java.util.ArrayList<>();

            // Only published content
            predicates.add(cb.equal(root.get("newsWorkflowStatus"), WorkflowStatus.PUBLISHED));

            // Geographic bounding box filter
            predicates.add(cb.between(root.get("newsLatitude"), bottomRightLat, topLeftLat));
            predicates.add(cb.between(root.get("newsLongitude"), topLeftLon, bottomRightLon));

            if (query != null && !query.trim().isEmpty()) {
                String searchPattern = "%" + query.toLowerCase() + "%";
                // Use plain LIKE for CLOB fields (case-insensitive by default)
                // Avoid cb.lower() on CLOB fields - creates type casting issues and timeouts
                predicates.add(cb.or(
                        cb.like(root.get("newsTitleEn"), searchPattern),
                        cb.like(root.get("newsTitleEs"), searchPattern)));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        try {
            Page<NewsMasterEntity> dbResults = newsRepository.findAll(spec, pageable);
            log.info("✓ Database geographic fallback successful: {} results",
                    dbResults.getTotalElements());

            return dbResults.map(this::convertToSearchDocument);
        } catch (Exception dbError) {
            log.error("Database geographic search failed", dbError);
            throw new RuntimeException("Geographic search failed: " + dbError.getMessage(), dbError);
        }
    }

    /**
     * Search news by category only.
     */
    @Override
    public Page<NewsSearchDocument> searchByCategory(UUID categoryId, Pageable pageable) {
        log.debug("Public category search: categoryId={}", categoryId);

        // ============================================================
        // STEP 1: TRY ELASTICSEARCH
        // ============================================================
        if (newsElasticSearchService.isPresent()) {
            try {
                log.debug("Attempting ES category search");
                Page<NewsSearchDocument> esResults = newsElasticSearchService.get()
                        .searchByCategory(categoryId, null, pageable);

                if (esResults.getTotalElements() > 0) {
                    log.info("✓ Public ES category search successful: {} results",
                            esResults.getTotalElements());
                    return esResults;
                }
            } catch (Exception esError) {
                log.warn("ES category search failed (non-critical): {} - Using DB fallback",
                        esError.getMessage());
            }
        }

        // ============================================================
        // STEP 2: FALLBACK TO DATABASE
        // ============================================================
        log.debug("Falling back to database category search");
        Specification<NewsMasterEntity> spec = (root, cq, cb) -> cb.and(
                cb.equal(root.get("newsWorkflowStatus"), WorkflowStatus.PUBLISHED),
                cb.equal(root.get("newsNewsCategoryId"), categoryId));

        try {
            Page<NewsMasterEntity> dbResults = newsRepository.findAll(spec, pageable);
            log.info("✓ Database category search successful: {} results",
                    dbResults.getTotalElements());
            return dbResults.map(this::convertToSearchDocument);
        } catch (Exception dbError) {
            log.error("Database category search failed", dbError);
            throw new RuntimeException("Category search failed: " + dbError.getMessage(), dbError);
        }
    }

    /**
     * Search news by country only.
     */
    @Override
    public Page<NewsSearchDocument> searchByCountry(String countryCode, Pageable pageable) {
        log.debug("Public country search: countryCode={}", countryCode);

        // ============================================================
        // STEP 1: TRY ELASTICSEARCH
        // ============================================================
        if (newsElasticSearchService.isPresent()) {
            try {
                log.debug("Attempting ES country search");
                Page<NewsSearchDocument> esResults = newsElasticSearchService.get()
                        .searchByCountry(countryCode, null, pageable);

                if (esResults.getTotalElements() > 0) {
                    log.info("✓ Public ES country search successful: {} results",
                            esResults.getTotalElements());
                    return esResults;
                }
            } catch (Exception esError) {
                log.warn("ES country search failed (non-critical): {} - Using DB fallback",
                        esError.getMessage());
            }
        }

        // ============================================================
        // STEP 2: FALLBACK TO DATABASE
        // ============================================================
        log.debug("Falling back to database country search");
        Specification<NewsMasterEntity> spec = (root, cq, cb) -> cb.and(
                cb.equal(root.get("newsWorkflowStatus"), WorkflowStatus.PUBLISHED),
                cb.equal(root.get("newsCountryCode"), countryCode));

        try {
            Page<NewsMasterEntity> dbResults = newsRepository.findAll(spec, pageable);
            log.info("✓ Database country search successful: {} results",
                    dbResults.getTotalElements());
            return dbResults.map(this::convertToSearchDocument);
        } catch (Exception dbError) {
            log.error("Database country search failed", dbError);
            throw new RuntimeException("Country search failed: " + dbError.getMessage(), dbError);
        }
    }

    /**
     * Search for breaking news articles.
     */
    @Override
    public Page<NewsSearchDocument> searchBreakingNews(Pageable pageable) {
        log.debug("Public breaking news search");

        // ============================================================
        // STEP 1: TRY ELASTICSEARCH
        // ============================================================
        if (newsElasticSearchService.isPresent()) {
            try {
                log.debug("Attempting ES breaking news search");
                Page<NewsSearchDocument> esResults = newsElasticSearchService.get()
                        .searchBreakingNews(null, pageable);

                if (esResults.getTotalElements() > 0) {
                    log.info("✓ Public ES breaking news search successful: {} results",
                            esResults.getTotalElements());
                    return esResults;
                }
            } catch (Exception esError) {
                log.warn("ES breaking news search failed (non-critical): {} - Using DB fallback",
                        esError.getMessage());
            }
        }

        // ============================================================
        // STEP 2: FALLBACK TO DATABASE
        // ============================================================
        log.debug("Falling back to database breaking news search");
        Specification<NewsMasterEntity> spec = (root, cq, cb) -> cb.and(
                cb.equal(root.get("newsWorkflowStatus"), WorkflowStatus.PUBLISHED),
                cb.isTrue(root.get("newsIsBreaking")));

        try {
            Page<NewsMasterEntity> dbResults = newsRepository.findAll(spec, pageable);
            log.info("✓ Database breaking news search successful: {} results",
                    dbResults.getTotalElements());
            return dbResults.map(this::convertToSearchDocument);
        } catch (Exception dbError) {
            log.error("Database breaking news search failed", dbError);
            throw new RuntimeException("Breaking news search failed: " + dbError.getMessage(), dbError);
        }
    }

    /**
     * Search for sponsored content.
     */
    @Override
    public Page<NewsSearchDocument> searchSponsoredContent(Pageable pageable) {
        log.debug("Public sponsored content search");

        // ============================================================
        // STEP 1: TRY ELASTICSEARCH
        // ============================================================
        if (newsElasticSearchService.isPresent()) {
            try {
                log.debug("Attempting ES sponsored content search");
                Page<NewsSearchDocument> esResults = newsElasticSearchService.get()
                        .searchSponsoredContent(null, pageable);

                if (esResults.getTotalElements() > 0) {
                    log.info("✓ Public ES sponsored search successful: {} results",
                            esResults.getTotalElements());
                    return esResults;
                }
            } catch (Exception esError) {
                log.warn("ES sponsored search failed (non-critical): {} - Using DB fallback",
                        esError.getMessage());
            }
        }

        // ============================================================
        // STEP 2: FALLBACK TO DATABASE
        // ============================================================
        log.debug("Falling back to database sponsored content search");
        Specification<NewsMasterEntity> spec = (root, cq, cb) -> cb.and(
                cb.equal(root.get("newsWorkflowStatus"), WorkflowStatus.PUBLISHED),
                cb.isTrue(root.get("newsIsSponsored")));

        try {
            Page<NewsMasterEntity> dbResults = newsRepository.findAll(spec, pageable);
            log.info("✓ Database sponsored search successful: {} results",
                    dbResults.getTotalElements());
            return dbResults.map(this::convertToSearchDocument);
        } catch (Exception dbError) {
            log.error("Database sponsored search failed", dbError);
            throw new RuntimeException("Sponsored content search failed: " + dbError.getMessage(), dbError);
        }
    }

    /**
     * Search for premium content.
     */
    @Override
    public Page<NewsSearchDocument> searchPremiumContent(Pageable pageable) {
        log.debug("Public premium content search");

        // ============================================================
        // STEP 1: TRY ELASTICSEARCH
        // ============================================================
        if (newsElasticSearchService.isPresent()) {
            try {
                log.debug("Attempting ES premium content search");
                Page<NewsSearchDocument> esResults = newsElasticSearchService.get()
                        .searchPremiumContent(null, pageable);

                if (esResults.getTotalElements() > 0) {
                    log.info("✓ Public ES premium search successful: {} results",
                            esResults.getTotalElements());
                    return esResults;
                }
            } catch (Exception esError) {
                log.warn("ES premium search failed (non-critical): {} - Using DB fallback",
                        esError.getMessage());
            }
        }

        // ============================================================
        // STEP 2: FALLBACK TO DATABASE
        // ============================================================
        log.debug("Falling back to database premium content search");
        Specification<NewsMasterEntity> spec = (root, cq, cb) -> cb.and(
                cb.equal(root.get("newsWorkflowStatus"), WorkflowStatus.PUBLISHED),
                cb.isTrue(root.get("newsIsPremium")));

        try {
            Page<NewsMasterEntity> dbResults = newsRepository.findAll(spec, pageable);
            log.info("✓ Database premium search successful: {} results",
                    dbResults.getTotalElements());
            return dbResults.map(this::convertToSearchDocument);
        } catch (Exception dbError) {
            log.error("Database premium search failed", dbError);
            throw new RuntimeException("Premium content search failed: " + dbError.getMessage(), dbError);
        }
    }

    /**
     * Search by date range only.
     */
    @Override
    public Page<NewsSearchDocument> searchByDateRange(
            Instant startDate,
            Instant endDate,
            Pageable pageable) {

        log.debug("Public date range search: {}-{}", startDate, endDate);

        // ============================================================
        // STEP 1: TRY ELASTICSEARCH
        // ============================================================
        if (newsElasticSearchService.isPresent()) {
            try {
                log.debug("Attempting ES date range search");
                Page<NewsSearchDocument> esResults = newsElasticSearchService.get().advancedSearch(
                        null, // no text query
                        null, // no categories
                        null, // no countries
                        null, // no tags
                        startDate,
                        endDate,
                        pageable);

                if (esResults.getTotalElements() > 0) {
                    log.info("✓ Public ES date range search successful: {} results",
                            esResults.getTotalElements());
                    return esResults;
                }
            } catch (Exception esError) {
                log.warn("ES date range search failed (non-critical): {} - Using DB fallback",
                        esError.getMessage());
            }
        }

        // ============================================================
        // STEP 2: FALLBACK TO DATABASE
        // ============================================================
        log.debug("Falling back to database date range search");
        Specification<NewsMasterEntity> spec = (root, cq, cb) -> {
            List<Predicate> predicates = new java.util.ArrayList<>();

            predicates.add(cb.equal(root.get("newsWorkflowStatus"), WorkflowStatus.PUBLISHED));

            if (startDate != null && endDate != null) {
                predicates.add(cb.between(root.get("newsPublishedAt"), startDate, endDate));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        try {
            Page<NewsMasterEntity> dbResults = newsRepository.findAll(spec, pageable);
            log.info("✓ Database date range search successful: {} results",
                    dbResults.getTotalElements());
            return dbResults.map(this::convertToSearchDocument);
        } catch (Exception dbError) {
            log.error("Database date range search failed", dbError);
            throw new RuntimeException("Date range search failed: " + dbError.getMessage(), dbError);
        }
    }

    // =========================================================================
    // PRIVATE HELPER METHODS
    // =========================================================================

    private Page<NewsSearchDocument> databaseSearchByQuery(String query, Pageable pageable) {
        log.debug("Performing database FTS search: '{}'", query);

        // ============================================================
        // VALIDATION: Double-check query length (defensive programming)
        // ============================================================
        if (query != null && query.length() > 500) {
            log.warn("Search query exceeds max length: {} chars (max 500)", query.length());
            throw new IllegalArgumentException("Search query must not exceed 500 characters");
        }

        // ============================================================
        // VALIDATION: Double-check pagination size (defensive programming)
        // ============================================================
        if (pageable.getPageSize() > 100) {
            log.warn("Page size {} exceeds maximum 100, clamping", pageable.getPageSize());
            pageable = org.springframework.data.domain.PageRequest.of(
                    pageable.getPageNumber(), 100, pageable.getSort());
        }

        // Use PostgreSQL Full-Text Search vector with JPA Specifications
        // Ranking and filtering logic all in Java code (no SQL logic buried in DB)
        try {
            Specification<NewsMasterEntity> spec = NewsSearchSpecifications.isNotDeleted()
                    .and((root, cq, cb) -> cb.equal(root.get("newsWorkflowStatus"), WorkflowStatus.PUBLISHED))
                    .and(NewsSearchSpecifications.isActive())
                    .and(NewsSearchSpecifications.byQuery(query));

            Page<NewsMasterEntity> dbResults = newsRepository.findAll(spec, pageable);

            log.info("✓ PostgreSQL FTS search successful: {} results for '{}'",
                    dbResults.getTotalElements(), query);

            return dbResults.map(this::convertToSearchDocument);
        } catch (Exception dbError) {
            log.error("PostgreSQL FTS search failed", dbError);
            throw new RuntimeException("FTS search failed: " + dbError.getMessage(), dbError);
        }
    }

    /**
     * Database fallback for advanced search with multiple filters.
     */
    private Page<NewsSearchDocument> databaseAdvancedSearch(
            String query,
            List<UUID> categoryIds,
            List<String> countryCodes,
            List<String> tags,
            Instant startDate,
            Instant endDate,
            Pageable pageable) {

        log.debug("Performing database advanced search");

        // ============================================================
        // VALIDATION: Check query length to prevent DOS attacks
        // ============================================================
        if (query != null && query.length() > 500) {
            log.warn("Search query exceeds max length: {} chars (max 500)", query.length());
            throw new IllegalArgumentException("Search query must not exceed 500 characters");
        }

        // ============================================================
        // VALIDATION: Check pagination size to prevent memory exhaustion
        // ============================================================
        if (pageable.getPageSize() > 100) {
            log.warn("Page size {} exceeds maximum 100, clamping", pageable.getPageSize());
            pageable = org.springframework.data.domain.PageRequest.of(
                    pageable.getPageNumber(), 100, pageable.getSort());
        }
        if (pageable.getPageSize() <= 0) {
            log.warn("Invalid page size {}, using default 20", pageable.getPageSize());
            pageable = org.springframework.data.domain.PageRequest.of(
                    pageable.getPageNumber(), 20, pageable.getSort());
        }

        Specification<NewsMasterEntity> spec = (root, cq, cb) -> {
            List<Predicate> predicates = new java.util.ArrayList<>();

            // Only published content for public
            predicates.add(cb.equal(root.get("newsWorkflowStatus"), WorkflowStatus.PUBLISHED));

            if (query != null && !query.trim().isEmpty()) {
                String searchPattern = "%" + query.toLowerCase() + "%";
                // Use plain LIKE for CLOB fields (case-insensitive by default)
                // Avoid cb.lower() on CLOB fields - creates type casting issues and timeouts
                predicates.add(cb.or(
                        cb.like(root.get("newsTitleEn"), searchPattern),
                        cb.like(root.get("newsTitleEs"), searchPattern),
                        cb.like(root.get("newsContentEn"), searchPattern),
                        cb.like(root.get("newsContentEs"), searchPattern)));
            }

            if (categoryIds != null && !categoryIds.isEmpty()) {
                predicates.add(root.get("newsNewsCategoryId").in(categoryIds));
            }

            if (countryCodes != null && !countryCodes.isEmpty()) {
                predicates.add(root.get("newsCountryCode").in(countryCodes));
            }

            if (tags != null && !tags.isEmpty()) {
                // Simple tag search - tags stored as comma-separated
                // Escape special LIKE characters (%, _) to prevent SQL injection via wildcards
                Predicate tagPredicate = cb.disjunction();
                for (String tag : tags) {
                    // Escape % and _ characters in tag to prevent SQL wildcard injection
                    String escapedTag = tag.toLowerCase()
                            .replace("\\", "\\\\") // Escape backslash first
                            .replace("%", "\\%") // Escape % wildcard
                            .replace("_", "\\_"); // Escape _ wildcard
                    String tagPattern = "%" + escapedTag + "%";

                    // Use plain LIKE without cb.lower() on CLOB field
                    tagPredicate = cb.or(tagPredicate,
                            cb.like(root.get("newsTags"), tagPattern, '\\')); // Specify escape character
                }
                predicates.add(tagPredicate);
            }

            if (startDate != null && endDate != null) {
                // Use UTC for consistent date range filtering across timezones
                LocalDate fromLocalDate = startDate.atZone(ZoneId.of("UTC")).toLocalDate();
                LocalDate toLocalDate = endDate.atZone(ZoneId.of("UTC")).toLocalDate();
                predicates.add(cb.between(root.get("newsPublishedAt"),
                        fromLocalDate.atStartOfDay(ZoneId.of("UTC")).toInstant(),
                        toLocalDate.plusDays(1).atStartOfDay(ZoneId.of("UTC")).toInstant()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        try {
            Page<NewsMasterEntity> dbResults = newsRepository.findAll(spec, pageable);
            log.info("✓ Database advanced search successful: {} results",
                    dbResults.getTotalElements());
            return dbResults.map(this::convertToSearchDocument);
        } catch (Exception dbError) {
            log.error("Database advanced search failed", dbError);
            throw new RuntimeException("Advanced search failed: " + dbError.getMessage(), dbError);
        }
    }

    /**
     * Convert NewsMasterEntity to NewsSearchDocument (35 public fields).
     * 
     * <p>
     * Creates a simplified document containing only publicly safe fields
     * for response to public API consumers.
     * </p>
     */
    private NewsSearchDocument convertToSearchDocument(NewsMasterEntity entity) {
        if (entity == null) {
            return null;
        }

        return NewsSearchDocument.builder()
                .newsNewsId(entity.getNewsNewsId())
                .newsSlug(entity.getNewsSlug())
                .newsTitleEn(entity.getNewsTitleEn())
                .newsTitleEs(entity.getNewsTitleEs())
                .newsExcerptEn(entity.getNewsExcerptEn())
                .newsExcerptEs(entity.getNewsExcerptEs())
                .newsContentEn(entity.getNewsContentEn())
                .newsContentEs(entity.getNewsContentEs())
                .newsNewsCategoryId(entity.getNewsNewsCategoryId())
                .newsCountryCode(entity.getNewsCountryCode())
                .newsLatitude(entity.getNewsLatitude())
                .newsLongitude(entity.getNewsLongitude())
                .newsIsBreaking(entity.getNewsIsBreaking() != null && entity.getNewsIsBreaking())
                .newsIsSponsored(entity.getNewsIsSponsored() != null && entity.getNewsIsSponsored())
                .newsIsPremium(entity.getNewsIsPremium() != null && entity.getNewsIsPremium())
                .newsThumbnailUrl(entity.getNewsThumbnailUrl())
                .newsMediaType(entity.getNewsMediaType())
                .newsSourceUrl(entity.getNewsSourceUrl())
                .newsPublishedAt(entity.getNewsPublishedAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
