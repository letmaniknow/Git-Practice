package com.mmva.newsapp.domain.news.service.search;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mmva.newsapp.domain.news.dto.core.NewsCreateResponseDto;
import com.mmva.newsapp.domain.news.mapper.core.NewsMapper;
import com.mmva.newsapp.domain.news.model.core.NewsMasterEntity;
import com.mmva.newsapp.domain.news.model.elasticsearch.NewsSearchDocument;
import com.mmva.newsapp.domain.news.repository.core.NewsRepository;
import com.mmva.newsapp.domain.news.service.elasticsearch.NewsElasticSearchService;
import com.mmva.newsapp.domain.news.service.elasticsearch.AdminNewsElasticSearchBatchService;

import jakarta.persistence.criteria.Predicate;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of {@link AdminNewsSearchService} providing server-side search
 * and filtering capabilities for admin newsmanagement.
 *
 * <p>
 * Uses JPA Specifications pattern for building dynamic queries based on
 * multiple
 * filter criteria. This allows flexible combining of search filters (query,
 * status,
 * category, date range) without exponential growth of custom @Query methods.
 * </p>
 *
 * <h3>Key Design Patterns:</h3>
 * <ul>
 * <li>JPA Specifications: Dynamic query building</li>
 * <li>Transactional: Read-only queries for consistency</li>
 * <li>DTO Mapping: Entity → DTO conversion</li>
 * <li>Null-Safe Filtering: Optional parameters handled gracefully</li>
 * </ul>
 *
 * <h3>Workflow Statuses Supported:</h3>
 * <ul>
 * <li>DRAFT - Unpublished, not yet submitted</li>
 * <li>SUBMITTED - Awaiting review</li>
 * <li>REVIEWED - Reviewed by moderator</li>
 * <li>APPROVED - Ready to publish</li>
 * <li>SCHEDULED - Scheduled for future publication</li>
 * <li>PUBLISHED - Currently live</li>
 * <li>ARCHIVED - Archived news (not public)</li>
 * </ul>
 *
 * @author MMVA Team
 * @version 1.0
 * @since 2026-03-04
 * @see com.mmva.newsapp.domain.news.service.search.NewsSearchSpecifications
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminNewsSearchServiceImpl implements AdminNewsSearchService {

    private final NewsRepository newsRepository;
    private final NewsMapper newsMapper; // PROFESSIONAL: Single source of truth for DTO conversions
    private final Optional<NewsElasticSearchService> newsElasticSearchService; // Elasticsearch search (optional)
    private final AdminNewsElasticSearchBatchService adminNewsElasticSearchBatchService; // Batch fetch optimization

    /**
     * Advanced search with workflow status filtering for admin users.
     *
     * <p>
     * Hybrid approach: Tries Elasticsearch first (if query provided), then falls
     * back to database.
     * Delegates to {@link NewsSearchSpecifications#createSearchSpecification}
     * to build dynamic query based on all provided criteria.
     * </p>
     */
    @Override
    public Page<NewsCreateResponseDto> searchWithStatusFilter(
            String query,
            List<String> workflowStatuses,
            String categoryId,
            LocalDate fromDate,
            LocalDate toDate,
            Pageable pageable) {

        log.debug(
                "Searching with filter - query: {}, statuses: {}, categoryId: {}, fromDate: {}, toDate: {}",
                query, workflowStatuses, categoryId, fromDate, toDate);

        // Only perform ES search if query is provided AND ES is available
        if (isValidQuery(query) && newsElasticSearchService.isPresent()) {
            try {
                // Step 1: TRY ELASTICSEARCH FIRST with Circuit Breaker (fast, full-text)
                log.debug("Attempting Elasticsearch status-filtered search with Circuit Breaker for query: '{}'",
                        query);
                return elasticsearchSearchWithCircuitBreaker(query, pageable);
            } catch (Exception esError) {
                // Step 2: ES FAILED - FALLBACK TO DATABASE
                log.warn(
                        "Elasticsearch status-filtered search failed (circuit breaker fallback): {} - Falling back to database search",
                        esError.getMessage());
            }
        }

        // Step 3: FALLBACK TO DATABASE SEARCH with all filters
        log.debug(
                "Performing database fallback search with all filters: query={}, statuses={}, categoryId={}, from={}, to={}",
                query, workflowStatuses, categoryId, fromDate, toDate);

        // Use hybrid approach:
        // - For CLOB query searches: use Specifications to avoid CLOB casting issues
        // - Combine with status/category/date filters

        Specification<NewsMasterEntity> specification = NewsSearchSpecifications.createSearchSpecification(
                query,
                workflowStatuses,
                categoryId,
                fromDate,
                toDate,
                null, // dynamicFields
                null // authorId
        );

        // Execute query with pagination and convert to DTO
        try {
            Page<NewsMasterEntity> results = newsRepository.findAll(specification, pageable);
            log.debug("Database fallback found {} results for advanced search", results.getTotalElements());
            return results.map(this::convertToDto);
        } catch (Exception dbError) {
            log.error("Database fallback search failed", dbError);
            throw new RuntimeException("Status filter search operation failed: " + dbError.getMessage(), dbError);
        }
    }

    /**
     * Search only by workflow status(es).
     *
     * <p>
     * Convenience method combining multiple statuses in a single query.
     * </p>
     */
    @Override
    public Page<NewsCreateResponseDto> searchByWorkflowStatuses(
            List<String> workflowStatuses,
            Pageable pageable) {

        log.debug("Searching by workflow statuses: {}", workflowStatuses);

        // Use dedicated specification for status-only filtering
        Specification<NewsMasterEntity> specification = NewsSearchSpecifications.byWorkflowStatuses(workflowStatuses);

        Page<NewsMasterEntity> results = newsRepository.findAll(specification, pageable);
        log.debug("Found {} news items with statuses {}", results.getTotalElements(), workflowStatuses);

        return results.map(this::convertToDto);
    }

    /**
     * Search by query with optional status filter.
     *
     * <p>
     * ENHANCED: Implements ES-first search with automatic DB fallback.
     * </p>
     * <ul>
     * <li>Step 1: Try Elasticsearch with Circuit Breaker (fast full-text search if
     * enabled)</li>
     * <li>Step 2: If ES fails or circuit open → Fallback to database search</li>
     * <li>Step 3: If both fail → Return error</li>
     * </ul>
     */
    @Override
    public Page<NewsCreateResponseDto> searchByQuery(
            String query,
            List<String> workflowStatuses,
            Pageable pageable) {

        log.debug("Searching by query: '{}' with statuses: {}", query, workflowStatuses);

        // Only perform ES search if query is provided AND ES is available
        if (isValidQuery(query) && newsElasticSearchService.isPresent()) {
            try {
                // Step 1: TRY ELASTICSEARCH FIRST with Circuit Breaker (fast, ~10-50ms for
                // large datasets)
                log.debug("Attempting Elasticsearch search for query: '{}'", query);
                return elasticsearchSearchWithCircuitBreaker(query, pageable);
            } catch (Exception esError) {
                // Step 2: ES FAILED - FALLBACK TO DATABASE
                log.warn("Elasticsearch search failed (circuit breaker fallback): {} - Falling back to database search",
                        esError.getMessage());
            }
        }

        // Step 3: FALLBACK TO DATABASE SEARCH (PostgreSQL FTS with JPA Specifications)
        // Uses Specifications pattern for flexible multi-criteria filtering
        // Falls back to PostgreSQL Full-Text Search when executed
        log.debug("Performing database fallback search for query: '{}'", query);
        Specification<NewsMasterEntity> specification = NewsSearchSpecifications.createSearchSpecification(
                query,
                workflowStatuses,
                null, // no category filter
                null, // fromDate
                null, // toDate
                null, // dynamicFields
                null // authorId
        );

        try {
            Page<NewsMasterEntity> dbResults = newsRepository.findAll(specification, pageable);
            log.info("✓ Database fallback search successful: {} results for query '{}'",
                    dbResults.getTotalElements(), query);
            return dbResults.map(this::convertToDto);
        } catch (Exception dbError) {
            // Both failed
            log.error("Both Elasticsearch and database search failed for query: '{}'", query, dbError);
            throw new RuntimeException("Search operation failed: " + dbError.getMessage(), dbError);
        }
    }

    @Override
    public Page<NewsCreateResponseDto> searchByDateRange(
            LocalDate fromDate,
            LocalDate toDate,
            List<String> workflowStatuses,
            Pageable pageable) {

        log.debug("Searching by date range: {} to {} with statuses: {}",
                fromDate, toDate, workflowStatuses);

        // Only perform ES search if ES is available
        if (newsElasticSearchService.isPresent()) {
            try {
                // Convert LocalDate to Instant for ES search using UTC for consistency
                Instant fromInstant = convertLocalDateToInstant(fromDate);
                Instant toInstant = convertLocalDateToInstant(toDate);

                log.debug("Attempting Elasticsearch date range search with Circuit Breaker: {} to {}",
                        fromDate, toDate);
                // Step 1: TRY ELASTICSEARCH FIRST with Circuit Breaker (fast)
                return elasticsearchAdvancedSearchWithCircuitBreaker(fromInstant, toInstant, workflowStatuses,
                        pageable);
            } catch (Exception esError) {
                // Step 2: ES FAILED - FALLBACK TO DATABASE
                log.warn(
                        "Elasticsearch date range search failed (circuit breaker fallback): {} - Falling back to database",
                        esError.getMessage());
            }
        }

        // Step 3: FALLBACK TO DATABASE SEARCH
        log.debug("Performing database fallback date range search: {} to {}", fromDate, toDate);
        Specification<NewsMasterEntity> specification = NewsSearchSpecifications.createSearchSpecification(
                null, // no text query
                workflowStatuses,
                null, // no category filter
                fromDate,
                toDate,
                null, // dynamicFields
                null // authorId
        );

        try {
            Page<NewsMasterEntity> results = newsRepository.findAll(specification, pageable);
            log.info("✓ Database fallback date range search successful: {} results",
                    results.getTotalElements());
            return results.map(this::convertToDto);
        } catch (Exception dbError) {
            // Both failed
            log.error("Both Elasticsearch and database date range search failed", dbError);
            throw new RuntimeException("Date range search failed: " +
                    dbError.getMessage(), dbError);
        }
    }

    /**
     * INDUSTRY STANDARD: Multi-field search with ES-first/DB-fallback architecture.
     * 
     * <p>
     * Implements professional-grade search across multiple content fields:
     * newsSlug, newsTitleEn, newsTitleEs, newsContentEn, newsContentEs,
     * newsKeywords, newsMetaDescription.
     * </p>
     * 
     * <p>
     * <strong>Search Strategy:</strong>
     * <ol>
     * <li>If ES enabled: Try Elasticsearch first (fast full-text search)</li>
     * <li>If ES unavailable or returns 0 results: Fall back to database</li>
     * <li>Combine search with status, category, and date filters</li>
     * </ol>
     * </p>
     */
    @Override
    public Page<NewsCreateResponseDto> searchWithMultipleFields(
            String query,
            List<String> workflowStatuses,
            String categoryId,
            String createdBy,
            LocalDate fromDate,
            LocalDate toDate,
            Pageable pageable) {

        log.debug("Multi-field search - query: {}, statuses: {}, categoryId: {}, createdBy: {}, from: {}, to: {}",
                query, workflowStatuses, categoryId, createdBy, fromDate, toDate);

        // Only perform ES search if query is provided AND ES is available
        if (isValidQuery(query) && newsElasticSearchService.isPresent()) {
            try {
                // Step 1: TRY ELASTICSEARCH FIRST with Circuit Breaker (fast, full-text)
                log.debug("Attempting Elasticsearch multi-field search with Circuit Breaker for query: '{}'", query);
                return elasticsearchSearchWithCircuitBreaker(query, pageable);
            } catch (Exception esError) {
                // Step 2: ES FAILED - FALLBACK TO DATABASE
                log.warn(
                        "Elasticsearch multi-field search failed (circuit breaker fallback): {} - Falling back to database search",
                        esError.getMessage());
            }
        }

        // Step 3: FALLBACK TO DATABASE SEARCH with multi-field LIKE
        log.debug("Performing database fallback multi-field search for query: '{}'", query);

        // Define searchable fields for PostgreSQL multi-field search
        // PROFESSIONAL STRATEGY: Include all user-facing content fields (VARCHARs +
        // safe LOBs)
        // EXCLUDE: HTML-encoded content, binary/media data, external URLs
        List<String> searchFields = java.util.List.of(
                // Core content fields
                "newsSlug", // URL slug
                "newsTitleEn", // Article title (English)
                "newsTitleEs", // Article title (Spanish)
                "newsContentEn", // Article body (English)
                "newsContentEs", // Article body (Spanish)
                "newsExcerptEn", // Summary/excerpt (English, up to 500 chars)
                "newsExcerptEs", // Summary/excerpt (Spanish, up to 500 chars)
                // SEO/metadata fields
                "newsKeywords", // Keywords (comma-separated)
                "newsMetaTitle", // SEO title
                "newsMetaDescription", // SEO description
                "newsTags" // Tags (comma-separated)
        );

        Specification<NewsMasterEntity> specification = NewsSearchSpecifications.createSearchSpecification(
                query,
                workflowStatuses,
                categoryId,
                fromDate,
                toDate,
                searchFields, // Enable multi-field search (PostgreSQL only)
                createdBy != null && !createdBy.isBlank() ? UUID.fromString(createdBy) : null);

        try {
            Page<NewsMasterEntity> dbResults = newsRepository.findAll(specification, pageable);
            log.info(
                    "✓ Database fallback multi-field search: {} results [query='{}', createdBy='{}', statuses={}, from={}, to={}]",
                    dbResults.getTotalElements(),
                    query != null ? query : "(none)",
                    createdBy != null ? createdBy : "(none)",
                    workflowStatuses != null ? workflowStatuses : "(all)",
                    fromDate, toDate);
            return dbResults.map(this::convertToDto);
        } catch (Exception dbError) {
            // Both failed
            log.error("Both Elasticsearch and database multi-field search failed for query: '{}'", query, dbError);
            throw new RuntimeException("Multi-field search operation failed: " + dbError.getMessage(), dbError);
        }
    }

    /**
     * INDUSTRY STANDARD: Search by title only (exact article lookup).
     *
     * <p>
     * Searches ONLY title fields for precise article matching.
     * Hybrid approach: Tries Elasticsearch first, then falls back to database.
     * Admin can find ONE specific article to edit/delete by title.
     * </p>
     */
    @Override
    public Page<NewsCreateResponseDto> searchByTitleOnly(
            String query,
            List<String> workflowStatuses,
            String categoryId,
            String createdBy,
            LocalDate fromDate,
            LocalDate toDate,
            Pageable pageable) {

        log.debug("Title-only search - query: {}, statuses: {}, categoryId: {}, createdBy: {}, from: {}, to: {}",
                query, workflowStatuses, categoryId, createdBy, fromDate, toDate);

        // Only perform ES search if query is provided AND ES is available
        if (isValidQuery(query) && newsElasticSearchService.isPresent()) {
            try {
                // Step 1: TRY ELASTICSEARCH FIRST with Circuit Breaker (fast, title matching)
                log.debug("Attempting Elasticsearch title-only search with Circuit Breaker for query: '{}'", query);
                return elasticsearchSearchWithCircuitBreaker(query, pageable);
            } catch (Exception esError) {
                // Step 2: ES FAILED - FALLBACK TO DATABASE
                log.warn(
                        "Elasticsearch title-only search failed (circuit breaker fallback): {} - Falling back to database search",
                        esError.getMessage());
            }
        }

        // Step 3: FALLBACK TO DATABASE SEARCH with title-only filter
        log.debug("Performing database fallback title-only search for query: '{}'", query);

        // Build specification with title-only search
        Specification<NewsMasterEntity> specification = (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Always exclude soft-deleted records
            predicates.add(cb.isNull(root.get("deletedAt")));

            // Add title-only search if provided
            if (isValidQuery(query)) {
                predicates.add(NewsSearchSpecifications.byTitleOnly(query).toPredicate(root, cq, cb));
            }

            // Add status filter if provided
            if (workflowStatuses != null && !workflowStatuses.isEmpty()) {
                predicates.add(NewsSearchSpecifications.byWorkflowStatuses(workflowStatuses).toPredicate(root, cq, cb));
            }

            // Add category filter if provided
            if (categoryId != null && !categoryId.isBlank()) {
                predicates.add(NewsSearchSpecifications.byCategoryId(categoryId).toPredicate(root, cq, cb));
            }

            // Add date range filter if provided
            if (fromDate != null || toDate != null) {
                predicates.add(NewsSearchSpecifications.byDateRange(fromDate, toDate).toPredicate(root, cq, cb));
            }

            // Add createdBy filter if provided
            if (createdBy != null && !createdBy.isBlank()) {
                predicates
                        .add(NewsSearchSpecifications.byAuthorId(UUID.fromString(createdBy)).toPredicate(root, cq, cb));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        try {
            Page<NewsMasterEntity> results = newsRepository.findAll(specification, pageable);
            log.info(
                    "\u2713 Database fallback title-only search: {} results [query='{}', createdBy='{}', statuses={}, from={}, to={}]",
                    results.getTotalElements(),
                    query != null ? query : "(none)",
                    createdBy != null ? createdBy : "(none)",
                    workflowStatuses != null ? workflowStatuses : "(all)",
                    fromDate, toDate);
            return results.map(this::convertToDto);
        } catch (Exception error) {
            log.error("Title-only search failed for query: '{}'", query, error);
            throw new RuntimeException("Title search operation failed: " + error.getMessage(), error);
        }
    }

    /**
     * INDUSTRY STANDARD: Search by content/metadata fields only (topic discovery).
     *
     * <p>
     * Searches content, metadata, keywords, tags (everything except title).
     * Hybrid approach: Tries Elasticsearch first, then falls back to database.
     * Admin can find related articles by topic without exact title matches.
     * </p>
     */
    @Override
    public Page<NewsCreateResponseDto> searchByContentFieldsOnly(
            String query,
            List<String> workflowStatuses,
            String categoryId,
            String createdBy,
            LocalDate fromDate,
            LocalDate toDate,
            Pageable pageable) {

        log.debug("Content-only search - query: {}, statuses: {}, categoryId: {}, createdBy: {}, from: {}, to: {}",
                query, workflowStatuses, categoryId, createdBy, fromDate, toDate);

        // Only perform ES search if query is provided AND ES is available
        if (isValidQuery(query) && newsElasticSearchService.isPresent()) {
            try {
                // Step 1: TRY ELASTICSEARCH FIRST with Circuit Breaker (fast, content matching)
                log.debug("Attempting Elasticsearch content-only search with Circuit Breaker for query: '{}'", query);
                return elasticsearchSearchWithCircuitBreaker(query, pageable);
            } catch (Exception esError) {
                // Step 2: ES FAILED - FALLBACK TO DATABASE
                log.warn(
                        "Elasticsearch content-only search failed (circuit breaker fallback): {} - Falling back to database search",
                        esError.getMessage());
            }
        }

        // Step 3: FALLBACK TO DATABASE SEARCH with content-only filter
        log.debug("Performing database fallback content-only search for query: '{}'", query);

        // Build specification with content-only search
        Specification<NewsMasterEntity> specification = (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Always exclude soft-deleted records
            predicates.add(cb.isNull(root.get("deletedAt")));

            // Add content-only search if provided
            if (isValidQuery(query)) {
                predicates.add(NewsSearchSpecifications.byContentFieldsOnly(query).toPredicate(root, cq, cb));
            }

            // Add status filter if provided
            if (workflowStatuses != null && !workflowStatuses.isEmpty()) {
                predicates.add(NewsSearchSpecifications.byWorkflowStatuses(workflowStatuses).toPredicate(root, cq, cb));
            }

            // Add category filter if provided
            if (categoryId != null && !categoryId.isBlank()) {
                predicates.add(NewsSearchSpecifications.byCategoryId(categoryId).toPredicate(root, cq, cb));
            }

            // Add date range filter if provided
            if (fromDate != null || toDate != null) {
                predicates.add(NewsSearchSpecifications.byDateRange(fromDate, toDate).toPredicate(root, cq, cb));
            }

            // Add createdBy filter if provided
            if (createdBy != null && !createdBy.isBlank()) {
                predicates
                        .add(NewsSearchSpecifications.byAuthorId(UUID.fromString(createdBy)).toPredicate(root, cq, cb));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        try {
            Page<NewsMasterEntity> results = newsRepository.findAll(specification, pageable);
            log.info(
                    "\u2713 Database fallback content-only search: {} results [query='{}', createdBy='{}', statuses={}, from={}, to={}]",
                    results.getTotalElements(),
                    query != null ? query : "(none)",
                    createdBy != null ? createdBy : "(none)",
                    workflowStatuses != null ? workflowStatuses : "(all)",
                    fromDate, toDate);
            return results.map(this::convertToDto);
        } catch (Exception error) {
            log.error("Content-only search failed for query: '{}'", query, error);
            throw new RuntimeException("Content search operation failed: " + error.getMessage(), error);
        }
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
    private Page<NewsCreateResponseDto> elasticsearchSearchWithCircuitBreaker(String query, Pageable pageable) {
        log.debug("Circuit breaker: Attempting Elasticsearch search for query: '{}'", query);
        Page<NewsSearchDocument> esResults = newsElasticSearchService.get().search(query, pageable);

        if (esResults.getTotalElements() > 0) {
            log.info("✓ Elasticsearch search successful: {} results for query '{}'",
                    esResults.getTotalElements(), query);
            // INDUSTRY STANDARD: Batch fetch all entities from DB
            // Single query with IN clause instead of N+1 findById calls
            return adminNewsElasticSearchBatchService.batchFetchAdminDtos(esResults, pageable);
        } else {
            throw new RuntimeException("No ES results, triggering fallback");
        }
    }

    /**
     * Fallback method when circuit breaker opens for simple search (ES
     * unavailable).
     * Called automatically when ES fails or circuit is OPEN.
     * 
     * <p>
     * NOTE: This fallback is invoked by Circuit Breaker and cannot receive
     * additional parameters
     * like workflowStatuses due to @CircuitBreaker annotation limitations. For
     * searches that need
     * status filtering, the try-catch in the public method handles fallback
     * separately.
     * </p>
     */
    public Page<NewsCreateResponseDto> elasticsearchSearchFallback(String query, Pageable pageable, Exception ex) {
        log.warn("Circuit breaker fallback triggered for simple search (ES unavailable): {}", ex.getMessage());

        // Fallback to database search (note: status filters not available in Circuit
        // Breaker fallback)
        Specification<NewsMasterEntity> specification = NewsSearchSpecifications.createSearchSpecification(
                query,
                null, // no status filter - limitation of Circuit Breaker architecture
                null, // no category filter
                null, // fromDate
                null, // toDate
                null, // dynamicFields
                null // authorId
        );

        try {
            Page<NewsMasterEntity> dbResults = newsRepository.findAll(specification, pageable);
            log.info("✓ Circuit breaker fallback search successful: {} results for query '{}'",
                    dbResults.getTotalElements(), query);
            return dbResults.map(this::convertToDto);
        } catch (Exception dbError) {
            log.error("Database fallback search failed in circuit breaker", dbError);
            throw new RuntimeException("Fallback search operation failed: " + dbError.getMessage(), dbError);
        }
    }

    /**
     * Elasticsearch advanced search wrapped with Circuit Breaker protection.
     * 
     * <p>
     * Used by searchByDateRange() to search by date range with workflow status
     * filters.
     * Circuit Breaker status:
     * - CLOSED: ES working normally → uses Elasticsearch
     * - OPEN: ES failing → skips attempt, goes to PostgreSQL
     * - HALF_OPEN: Testing recovery → tries one request
     * </p>
     */
    @CircuitBreaker(name = "elasticsearchSearch", fallbackMethod = "elasticsearchAdvancedSearchFallback")
    private Page<NewsCreateResponseDto> elasticsearchAdvancedSearchWithCircuitBreaker(
            Instant fromDate, Instant toDate, List<String> workflowStatuses, Pageable pageable) {
        log.debug("Circuit breaker: Attempting Elasticsearch advanced search for date range: {} to {}",
                fromDate, toDate);
        Page<NewsSearchDocument> esResults = newsElasticSearchService.get().advancedSearch(
                "", // empty query - search all
                null, // no category filter
                null, // no tags
                workflowStatuses,
                fromDate,
                toDate,
                pageable);

        if (esResults.getTotalElements() > 0) {
            log.info("✓ Elasticsearch advanced search successful: {} results for date range {}-{}",
                    esResults.getTotalElements(), fromDate, toDate);
            // INDUSTRY STANDARD: Batch fetch all entities from DB
            // Single query with IN clause instead of N+1 findById calls
            return adminNewsElasticSearchBatchService.batchFetchAdminDtos(esResults, pageable);
        } else {
            throw new RuntimeException("No ES results, triggering fallback");
        }
    }

    /**
     * Fallback method when circuit breaker opens for advanced search (ES
     * unavailable).
     * Called automatically when advanced ES search fails or circuit is OPEN.
     */
    public Page<NewsCreateResponseDto> elasticsearchAdvancedSearchFallback(
            Instant fromDate, Instant toDate, List<String> workflowStatuses, Pageable pageable, Exception ex) {
        log.warn("Circuit breaker fallback triggered for advanced search (ES unavailable): {}", ex.getMessage());

        // Convert Instant back to LocalDate for database search
        LocalDate fromLocalDate = convertInstantToLocalDate(fromDate);
        LocalDate toLocalDate = convertInstantToLocalDate(toDate);

        // Fallback to database search with date range and status filter
        Specification<NewsMasterEntity> specification = NewsSearchSpecifications.createSearchSpecification(
                null, // no text query
                workflowStatuses,
                null, // no category filter
                fromLocalDate,
                toLocalDate,
                null, // dynamicFields
                null // authorId
        );

        try {
            Page<NewsMasterEntity> dbResults = newsRepository.findAll(specification, pageable);
            log.info("✓ Circuit breaker fallback advanced search successful: {} results for date range {}-{}",
                    dbResults.getTotalElements(), fromLocalDate, toLocalDate);
            return dbResults.map(this::convertToDto);
        } catch (Exception dbError) {
            log.error("Database fallback advanced search failed in circuit breaker", dbError);
            throw new RuntimeException("Fallback advanced search operation failed: " + dbError.getMessage(), dbError);
        }
    }

    /**
     * Helper method to convert Instant to LocalDate in UTC timezone.
     * Centralizes temporal conversion logic to avoid duplication.
     */
    private LocalDate convertInstantToLocalDate(Instant instant) {
        return instant != null ? instant.atZone(java.time.ZoneId.of("UTC")).toLocalDate() : null;
    }

    /**
     * Helper method to convert LocalDate to Instant at start of day in UTC
     * timezone.
     * Centralizes temporal conversion logic to avoid duplication.
     */
    private Instant convertLocalDateToInstant(LocalDate localDate) {
        return localDate != null ? localDate.atStartOfDay(java.time.ZoneId.of("UTC")).toInstant() : null;
    }

    /**
     * Helper method to check if query parameter is valid and not empty.
     * Centralizes validation logic to avoid duplication across methods.
     */
    private boolean isValidQuery(String query) {
        return query != null && !query.trim().isEmpty();
    }

    /**
     * PROFESSIONAL: Simplified delegation to centralized NewsMapper.
     * 
     * MapStruct generates an optimized implementation at compile time.
     * All conversions route through the single source of truth mapper.
     */
    private NewsCreateResponseDto convertToDto(NewsMasterEntity entity) {
        return newsMapper.toNewsCreateResponseDto(entity);
    }
}
