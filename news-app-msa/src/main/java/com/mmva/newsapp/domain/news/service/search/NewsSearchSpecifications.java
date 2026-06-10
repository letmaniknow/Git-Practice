
package com.mmva.newsapp.domain.news.service.search;

import org.springframework.data.jpa.domain.Specification;

import com.mmva.newsapp.domain.news.model.core.NewsMasterEntity;
import com.mmva.newsapp.domain.news.model.core.NewsMasterEntity.WorkflowStatus;

import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Specification builders for dynamic JPA Criteria queries on
 * {@link NewsMasterEntity}.
 *
 * <p>
 * This utility class provides reusable Specification builders for constructing
 * complex queries dynamically based on variable filter criteria. Uses the JPA
 * Criteria API to avoid SQL injection and maintain type safety.
 * </p>
 *
 * <h3>JPA Specifications Pattern Benefits:</h3>
 * <ul>
 * <li><strong>Type-Safe:</strong> All queries verified at compile-time</li>
 * <li><strong>Dynamic:</strong> Build queries conditionally without @Query
 * duplication</li>
 * <li><strong>Composable:</strong> Combine multiple specifications with
 * and()/or()</li>
 * <li><strong>Maintainable:</strong> Centralized query logic separate from
 * service</li>
 * </ul>
 *
 * <h3>PostgreSQL TEXT Column Strategy:</h3>
 * <p>
 * This application supports both MSSQL (development) and PostgreSQL
 * (production).
 * The following strategies ensure cross-database compatibility:
 * </p>
 * <ul>
 * <li><strong>VARCHAR Fields (slug, etc):</strong> Can use lower() function
 * safely - applied before LIKE matching</li>
 * <li><strong>PostgreSQL TEXT Fields (title, content, excerpt, keywords,
 * etc):</strong>
 * </strong> Use plain LIKE without lower() - both MSSQL and PostgreSQL are
 * case-insensitive for LIKE by default</li>
 * <li><strong>Equality Checks:</strong> Always use cb.equal() for UUIDs,
 * enums - works on all databases</li>
 * <li><strong>Boolean/null Checks:</strong> Use cb.isTrue(), cb.isNull(),
 * cb.isNotNull() - database-agnostic</li>
 * <li><strong>Temporal Comparisons:</strong> Use cb.greaterThanOrEqualTo(),
 * cb.lessThan() on Instant fields - works on all databases</li>
 * <li><strong>Full-Text Search:</strong> For advanced search on CLOB fields,
 * Elasticsearch integration recommended (future enhancement)</li>
 * </ul>
 *
 * <h3>CLOB Handling Rationale:</h3>
 * <p>
 * MSSQL's CAST function applies strict type checking: lower(cast(clobField as
 * varchar)) fails because lower() parameter expects STRING type but receives
 * CLOB
 * type. Solution: Plain LIKE is case-insensitive on both MSSQL and PostgreSQL
 * by
 * default, so no need for lower() function.
 * </p>
 *
 * <h3>Usage Example:</h3>
 * 
 * <pre>
 * Specification&lt;NewsMasterEntity&gt; spec = NewsSearchSpecifications
 *     .createSearchSpecification("breaking", ["PUBLISHED"], categoryId, from, to);
 * Page&lt;NewsMasterEntity&gt; results = repository.findAll(spec, pageable);
 * </pre>
 *
 * @author MMVA Team
 * @version 1.0
 * @since 2026-03-04
 * @see org.springframework.data.jpa.domain.Specification
 * @see org.springframework.data.jpa.repository.JpaSpecificationExecutor
 */
@Slf4j
public final class NewsSearchSpecifications {

    /**
     * Dynamic multi-field LIKE search (works on all databases).
     *
     * <p>
     * This specification allows searching across a dynamic set of fields
     * (e.g., slug, title, content, keywords, etc.) using LIKE.
     * Uses plain LIKE without lower() since MSSQL and PostgreSQL are both
     * case-insensitive by default for LIKE operations.
     * </p>
     *
     * <p>
     * <strong>MSSQL Compatibility:</strong> Handles both VARCHAR and CLOB fields
     * without type casting errors.
     * </p>
     *
     * @param query  text to search for
     * @param fields list of entity field names to search
     * @return specification matching news with any field containing the query
     */
    public static Specification<NewsMasterEntity> byQueryDynamicFields(String query, List<String> fields) {
        return (root, cq, cb) -> {
            if (query == null || query.isBlank() || fields == null || fields.isEmpty()) {
                return cb.conjunction();
            }

            // PostgreSQL TEXT fields: native support for case-insensitive search
            // Pattern: lowercase both column and search term with lower()
            String queryPattern = "%" + query.trim().toLowerCase() + "%";
            List<Predicate> orPredicates = new ArrayList<>();
            for (String field : fields) {
                // TEXT type supports lower() function natively - no casting needed
                orPredicates.add(cb.like(cb.lower(root.get(field)), queryPattern));
            }
            return cb.or(orPredicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Multi-field LIKE search (works on all databases).
     *
     * <p>
     * This specification searches across multiple fields (slug, title,
     * content, keywords, etc.) using LIKE.
     * Uses plain LIKE without lower() since both MSSQL and PostgreSQL are
     * case-insensitive by default for LIKE operations.
     * </p>
     *
     * <p>
     * Includes all user-facing content fields (titles, content, summary, keywords,
     * metadata). Excludes: HTML-encoded content, binary/media data, external URLs
     * </p>
     *
     * @param query text to search for
     * @return specification matching news with any field containing the query
     */
    public static Specification<NewsMasterEntity> byQueryMultiFieldPostgres(String query) {
        return (root, cq, cb) -> {
            if (query == null || query.isBlank()) {
                return cb.conjunction();
            }

            // PostgreSQL TEXT fields: native support for case-insensitive search
            // Pattern: lowercase both column and search term with lower()
            String queryPattern = "%" + query.trim().toLowerCase() + "%";

            List<Predicate> orPredicates = new ArrayList<>();

            // Core content fields - TEXT type supports lower() function natively
            orPredicates.add(cb.like(cb.lower(root.get("newsSlug")), queryPattern));
            orPredicates.add(cb.like(cb.lower(root.get("newsTitleEn")), queryPattern));
            orPredicates.add(cb.like(cb.lower(root.get("newsTitleEs")), queryPattern));
            orPredicates.add(cb.like(cb.lower(root.get("newsContentEn")), queryPattern));
            orPredicates.add(cb.like(cb.lower(root.get("newsContentEs")), queryPattern));
            orPredicates.add(cb.like(cb.lower(root.get("newsExcerptEn")), queryPattern));
            orPredicates.add(cb.like(cb.lower(root.get("newsExcerptEs")), queryPattern));

            // SEO/metadata fields - TEXT type supports lower() function natively
            orPredicates.add(cb.like(cb.lower(root.get("newsKeywords")), queryPattern));
            orPredicates.add(cb.like(cb.lower(root.get("newsMetaTitle")), queryPattern));
            orPredicates.add(cb.like(cb.lower(root.get("newsMetaDescription")), queryPattern));
            orPredicates.add(cb.like(cb.lower(root.get("newsTags")), queryPattern));
            return cb.or(orPredicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Private constructor to prevent instantiation of utility class.
     */
    private NewsSearchSpecifications() {
        throw new AssertionError("Utility class cannot be instantiated");
    }

    /**
     * Creates a combined specification from multiple search criteria.
     *
     * <p>
     * This is the primary method for building complex search queries. All criteria
     * are optional (null-safe) and are combined with AND logic. If all criteria are
     * null, returns an all-matching specification.
     * </p>
     *
     * <h3>Filter Logic (AND-based):</h3>
     * 
     * <pre>
     * WHERE (title OR content LIKE query) [OR]
     *   AND status IN (statuses) [OR]
     *   AND categoryId = categoryId [OR]
     *   AND createdAt BETWEEN fromDate AND toDate [OR]
     * </pre>
     *
     * @param query            optional text search (searches title and content)
     * @param workflowStatuses optional list of statuses (e.g., ["PUBLISHED",
     *                         "SCHEDULED"])
     * @param categoryId       optional category UUID (as string)
     * @param fromDate         optional start date for created date range
     * @param toDate           optional end date for created date range
     * @return combined Specification with all provided criteria
     */
    /**
     * Creates a combined specification from multiple search criteria, supporting
     * dynamic field and author search.
     *
     * <p>
     * This is the primary method for building complex search queries. All criteria
     * are optional (null-safe) and are combined with AND logic.
     * If all criteria are null, returns an all-matching specification.
     * </p>
     *
     * <h3>Filter Logic (AND-based):</h3>
     *
     * <pre>
     * WHERE (dynamic fields LIKE query) [OR]
     *   AND status IN (statuses) [OR]
     *   AND categoryId = categoryId [OR]
     *   AND createdAt BETWEEN fromDate AND toDate [OR]
     *   AND createdBy = authorId [OR]
     * </pre>
     *
     * @param query            optional text search (searches specified fields)
     * @param workflowStatuses optional list of statuses (e.g., ["PUBLISHED",
     *                         "SCHEDULED"])
     * @param categoryId       optional category UUID (as string)
     * @param fromDate         optional start date for created date range
     * @param toDate           optional end date for created date range
     * @param dynamicFields    optional list of field names to search (PostgreSQL
     *                         only, safe VARCHAR/TEXT fields)
     * @param authorId         optional author UUID (for createdBy filter)
     * @return combined Specification with all provided criteria
     */
    public static Specification<NewsMasterEntity> createSearchSpecification(
            String query,
            List<String> workflowStatuses,
            String categoryId,
            LocalDate fromDate,
            LocalDate toDate,
            List<String> dynamicFields,
            UUID authorId) {

        return (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Always exclude soft-deleted records
            predicates.add(cb.isNull(root.get("deletedAt")));

            // Add dynamic multi-field text search if provided (PostgreSQL only)
            if (query != null && !query.isBlank() && dynamicFields != null && !dynamicFields.isEmpty()) {
                predicates.add(byQueryDynamicFields(query, dynamicFields).toPredicate(root, cq, cb));
            } else if (query != null && !query.isBlank()) {
                // Fallback to default (slug-only) search for SQL Server
                predicates.add(byQuery(query).toPredicate(root, cq, cb));
            }

            // Add status filter if provided
            if (workflowStatuses != null && !workflowStatuses.isEmpty()) {
                predicates.add(byWorkflowStatuses(workflowStatuses).toPredicate(root, cq, cb));
            }

            // Add category filter if provided
            if (categoryId != null && !categoryId.isBlank()) {
                predicates.add(byCategoryId(categoryId).toPredicate(root, cq, cb));
            }

            // Add date range filter if provided
            if (fromDate != null || toDate != null) {
                predicates.add(byDateRange(fromDate, toDate).toPredicate(root, cq, cb));
            }

            // Add author filter if provided
            if (authorId != null) {
                predicates.add(byAuthorId(authorId).toPredicate(root, cq, cb));
            }

            // Combine all predicates with AND
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Filters by workflow status only.
     *
     * <p>
     * Supports multiple statuses with OR logic (matches ANY status in the list).
     * </p>
     *
     * @param statuses list of status strings (e.g., "PUBLISHED", "DRAFT")
     * @return specification matching news in any of the specified statuses
     */
    public static Specification<NewsMasterEntity> byWorkflowStatuses(List<String> statuses) {
        return (root, cq, cb) -> {
            if (statuses == null || statuses.isEmpty()) {
                return cb.conjunction(); // Always true if empty
            }

            List<Predicate> statusPredicates = new ArrayList<>();
            for (String status : statuses) {
                try {
                    WorkflowStatus workflowStatus = WorkflowStatus.valueOf(status.toUpperCase());
                    statusPredicates.add(cb.equal(root.get("newsWorkflowStatus"), workflowStatus));
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid workflow status: {}", status);
                }
            }

            if (statusPredicates.isEmpty()) {
                return cb.conjunction(); // Always true if no valid statuses
            }

            // Match any status (OR)
            return cb.or(statusPredicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Filters by text query (searches slug field only).
     *
     * <p>
     * Searches only VARCHAR field (slug) for basic fallback search.
     * Safe for all databases. For full-text search across large content
     * (CLOB fields), use Elasticsearch which handles this natively.
     * </p>
     *
     * <p>
     * <strong>Database Compatibility:</strong> Works on MSSQL, PostgreSQL, Oracle.
     * Works with VARCHAR, NVARCHAR (SQL Server), TEXT (PostgreSQL), CLOB (Oracle).
     * Uses plain LIKE without lower() - query pattern lowercased in Java layer.
     * LIKE is case-insensitive by default on all supported databases.
     * </p>
     *
     * @param query text to search for
     * @return specification matching news with slug containing query
     */
    public static Specification<NewsMasterEntity> byQuery(String query) {
        return (root, cq, cb) -> {
            if (query == null || query.isBlank()) {
                return cb.conjunction(); // Always true if empty
            }

            // Use lower() + LIKE for portable case-insensitive search across all databases
            String queryPattern = "%" + query.trim().toLowerCase() + "%";

            // Search slug field - case-insensitive pattern matching
            return cb.like(cb.function("lower", String.class, root.get("newsSlug")), queryPattern);
        };
    }

    /**
     * Filters by title fields only (exact article lookup).
     *
     * <p>
     * Searches ONLY article titles (English and Spanish).
     * Used for finding specific articles by exact/partial title match.
     * No content, metadata, or other fields included.
     * </p>
     *
     * <p>
     * <strong>Use Case:</strong> Admin searches "Elon introduced new car model"
     * to find ONE specific article to edit/delete.
     * </p>
     *
     * <p>
     * <strong>Database Compatibility:</strong> Works on MSSQL, PostgreSQL, Oracle.
     * Title fields are TEXT (PostgreSQL native), so we use LIKE with
     * case-insensitive search.
     * Both MSSQL and PostgreSQL are case-insensitive by default for LIKE.
     * </p>
     *
     * @param query text to search for (title match)
     * @return specification matching news with query in title fields only
     */
    public static Specification<NewsMasterEntity> byTitleOnly(String query) {
        return (root, cq, cb) -> {
            if (query == null || query.isBlank()) {
                return cb.conjunction();
            }

            // PostgreSQL TEXT fields: native support for case-insensitive search
            // Pattern: lowercase both column and search term with lower()
            String queryPattern = "%" + query.trim().toLowerCase() + "%";

            List<Predicate> titlePredicates = new ArrayList<>();
            // TEXT type supports lower() function natively - no casting needed
            titlePredicates.add(cb.like(cb.lower(root.get("newsTitleEn")), queryPattern));
            titlePredicates.add(cb.like(cb.lower(root.get("newsTitleEs")), queryPattern));

            return cb.or(titlePredicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Filters by content fields only (topic/discovery search).
     *
     * <p>
     * Searches content, metadata, keywords, and SEO fields (everything except
     * title). Used for finding related articles by topic/keywords without exact
     * title matches. Includes: content, excerpts, keywords, tags, metadata, slug.
     * </p>
     *
     * <p>
     * <strong>Use Case:</strong> Admin searches "electric vehicles" to find
     * all related articles discussing the topic (separate from title search).
     * </p>
     *
     * <p>
     * <strong>Database Compatibility:</strong> Works on MSSQL, PostgreSQL, Oracle.
     * Content, excerpts, keywords are TEXT (PostgreSQL native), so we use LIKE with
     * case-insensitive matching.
     * lower().
     * Both MSSQL and PostgreSQL are case-insensitive by default for LIKE.
     * </p>
     *
     * @param query text to search for (content/topic match)
     * @return specification matching news with query in content/metadata fields
     *         only
     */
    public static Specification<NewsMasterEntity> byContentFieldsOnly(String query) {
        return (root, cq, cb) -> {
            if (query == null || query.isBlank()) {
                return cb.conjunction();
            }

            // PostgreSQL TEXT fields: native support for case-insensitive search
            // Pattern: lowercase both column and search term with lower()
            String queryPattern = "%" + query.trim().toLowerCase() + "%";

            List<Predicate> contentPredicates = new ArrayList<>();

            // Content fields - TEXT type supports lower() function natively
            contentPredicates.add(cb.like(cb.lower(root.get("newsSlug")), queryPattern));
            contentPredicates.add(cb.like(cb.lower(root.get("newsContentEn")), queryPattern));
            contentPredicates.add(cb.like(cb.lower(root.get("newsContentEs")), queryPattern));
            contentPredicates.add(cb.like(cb.lower(root.get("newsExcerptEn")), queryPattern));
            contentPredicates.add(cb.like(cb.lower(root.get("newsExcerptEs")), queryPattern));

            // SEO/metadata fields - TEXT type supports lower() function natively
            contentPredicates.add(cb.like(cb.lower(root.get("newsKeywords")), queryPattern));
            contentPredicates.add(cb.like(cb.lower(root.get("newsMetaTitle")), queryPattern));
            contentPredicates.add(cb.like(cb.lower(root.get("newsMetaDescription")), queryPattern));
            contentPredicates.add(cb.like(cb.lower(root.get("newsTags")), queryPattern));

            return cb.or(contentPredicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Filters by category ID.
     *
     * <p>
     * Matches news articles belonging to a specific category.
     * </p>
     *
     * @param categoryId UUID of the category (as string)
     * @return specification matching news in the specified category
     */
    public static Specification<NewsMasterEntity> byCategoryId(String categoryId) {
        return (root, cq, cb) -> {
            if (categoryId == null || categoryId.isBlank()) {
                return cb.conjunction(); // Always true if empty
            }

            try {
                UUID categoryUuid = UUID.fromString(categoryId);
                return cb.equal(root.get("newsNewsCategoryId"), categoryUuid);
            } catch (IllegalArgumentException e) {
                log.warn("Invalid category UUID: {}", categoryId);
                return cb.conjunction(); // Always true on invalid UUID
            }
        };
    }

    /**
     * Filters by date range (creation date).
     *
     * <p>
     * Matches news articles created within the specified date range (inclusive).
     * Supports partial ranges (fromDate only, toDate only, or both).
     * </p>
     *
     * @param fromDate start date (inclusive), or null for no lower bound
     * @param toDate   end date (inclusive), or null for no upper bound
     * @return specification matching news within the date range
     */
    public static Specification<NewsMasterEntity> byDateRange(LocalDate fromDate, LocalDate toDate) {
        return (root, cq, cb) -> {
            List<Predicate> datePredicates = new ArrayList<>();

            if (fromDate != null) {
                // Convert LocalDate to Instant using UTC for consistent date range filtering
                Instant fromInstant = fromDate
                        .atStartOfDay(ZoneId.of("UTC"))
                        .toInstant();
                datePredicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), fromInstant));
            }

            if (toDate != null) {
                // Convert LocalDate to Instant using UTC (end of day in UTC zone)
                // Add 1 day to the toDate and start from the beginning of that day
                Instant toInstant = toDate
                        .plusDays(1)
                        .atStartOfDay(ZoneId.of("UTC"))
                        .toInstant();
                datePredicates.add(cb.lessThan(root.get("createdAt"), toInstant));
            }

            if (datePredicates.isEmpty()) {
                return cb.conjunction(); // Always true if no dates specified
            }

            // Combine with AND (both conditions must match if specified)
            return cb.and(datePredicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Filters by creation author (user ID).
     *
     * <p>
     * Matches news articles created by a specific user/author.
     * </p>
     *
     * <p>
     * <strong>Database Compatibility:</strong> Works on MSSQL, PostgreSQL, Oracle.
     * Uses UUID equality comparison (database-agnostic).
     * </p>
     *
     * @param authorId UUID of the author
     * @return specification matching news created by the specified author
     */
    public static Specification<NewsMasterEntity> byAuthorId(UUID authorId) {
        return (root, cq, cb) -> {
            if (authorId == null) {
                return cb.conjunction(); // Always true if empty
            }
            return cb.equal(root.get("createdBy"), authorId);
        };
    }

    /**
     * Filters featured news only.
     *
     * <p>
     * Matches news articles marked as featured.
     * </p>
     *
     * @return specification matching featured news articles
     */
    public static Specification<NewsMasterEntity> isFeatured() {
        return (root, cq, cb) -> cb.isTrue(root.get("newsIsFeatured"));
    }

    /**
     * Filters breaking news only.
     *
     * <p>
     * Matches news articles marked as breaking news.
     * </p>
     *
     * @return specification matching breaking news articles
     */
    public static Specification<NewsMasterEntity> isBreakingNews() {
        return (root, cq, cb) -> cb.isTrue(root.get("newsIsBreakingnews"));
    }

    /**
     * Filters active news only.
     *
     * <p>
     * Matches news articles with newsIsActive = true.
     * </p>
     *
     * @return specification matching active news articles
     */
    public static Specification<NewsMasterEntity> isActive() {
        return (root, cq, cb) -> cb.isTrue(root.get("newsIsActive"));
    }

    /**
     * Filters deleted news only (soft-deleted).
     *
     * <p>
     * Matches news articles with non-null deletedAt timestamp.
     * </p>
     *
     * @return specification matching soft-deleted news articles
     */
    public static Specification<NewsMasterEntity> isDeleted() {
        return (root, cq, cb) -> cb.isNotNull(root.get("deletedAt"));
    }

    /**
     * Filters non-deleted news only.
     *
     * <p>
     * Matches news articles with null deletedAt timestamp.
     * </p>
     *
     * @return specification matching non-deleted news articles
     */
    public static Specification<NewsMasterEntity> isNotDeleted() {
        return (root, cq, cb) -> cb.isNull(root.get("deletedAt"));
    }
}
