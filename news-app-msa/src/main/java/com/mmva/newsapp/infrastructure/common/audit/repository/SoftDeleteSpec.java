package com.mmva.newsapp.infrastructure.common.audit.repository;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;

/**
 * Reusable JPA Specifications for soft-delete filtering.
 * 
 * <h3>Industry Standard Pattern:</h3>
 * <p>
 * This uses the JPA Criteria API Specification pattern, which is:
 * </p>
 * <ul>
 * <li><b>Portable:</b> Works with any JPA provider (Hibernate, EclipseLink,
 * etc.)</li>
 * <li><b>Composable:</b> Can combine with other specifications using
 * and/or</li>
 * <li><b>Testable:</b> Easy to unit test without database</li>
 * <li><b>Type-safe:</b> Compile-time checking of field names</li>
 * <li><b>Explicit:</b> Clear intent in code, no "magic" filtering</li>
 * </ul>
 * 
 * <h3>Usage Examples:</h3>
 * 
 * <pre>{@code
 * // Public API - only active records
 * repository.findAll(SoftDeleteSpec.notDeleted());
 * 
 * // Admin API - all records including deleted
 * repository.findAll(SoftDeleteSpec.includeDeleted());
 * 
 * // Admin API - only deleted records (for restore listing)
 * repository.findAll(SoftDeleteSpec.onlyDeleted());
 * 
 * // Combine with other specs
 * repository.findAll(
 *         SoftDeleteSpec.notDeleted()
 *                 .and(SoftDeleteSpec.isActive())
 *                 .and(otherSpec));
 * }</pre>
 * 
 * <h3>Why This Over Hibernate @Filter:</h3>
 * <ul>
 * <li>No session management complexity</li>
 * <li>No AOP/interceptor overhead</li>
 * <li>Works with Spring Data pagination and sorting</li>
 * <li>Explicit in code - easier to debug and maintain</li>
 * <li>No OSIV dependency</li>
 * </ul>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
public final class SoftDeleteSpec {

    private static final String DELETED_AT = "deletedAt";
    private static final String IS_ACTIVE = "isActive";

    private SoftDeleteSpec() {
        // Utility class - prevent instantiation
    }

    // ========================================
    // Core Soft-Delete Specifications
    // ========================================

    /**
     * Filters to only non-deleted records (deletedAt IS NULL).
     * Use for public-facing APIs and normal user operations.
     * 
     * @param <T> the entity type
     * @return specification that filters out soft-deleted records
     */
    public static <T> Specification<T> notDeleted() {
        return (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> cb.isNull(root.get(DELETED_AT));
    }

    /**
     * No filtering - includes all records (deleted and non-deleted).
     * Use for admindashboard operations that need to see everything.
     * 
     * @param <T> the entity type
     * @return specification that includes all records (returns null predicate = no
     *         WHERE clause)
     */
    public static <T> Specification<T> includeDeleted() {
        // Return a spec that produces no predicate - Spring Data handles null
        // gracefully
        return (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> null;
    }

    /**
     * Filters to only soft-deleted records (deletedAt IS NOT NULL).
     * Use for admindashboard restore listing and audit views.
     * 
     * @param <T> the entity type
     * @return specification that only returns soft-deleted records
     */
    public static <T> Specification<T> onlyDeleted() {
        return (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> cb.isNotNull(root.get(DELETED_AT));
    }

    /**
     * Filters to records deleted before a certain time.
     * Useful for cleanup jobs or retention policies.
     * 
     * @param <T>    the entity type
     * @param before the cutoff time
     * @return specification for records deleted before the given time
     */
    public static <T> Specification<T> deletedBefore(Instant before) {
        return (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> cb.and(
                cb.isNotNull(root.get(DELETED_AT)),
                cb.lessThan(root.get(DELETED_AT), before));
    }

    // ========================================
    // Active Status Specifications
    // ========================================

    /**
     * Filters to only active records (isActive = true).
     * 
     * @param <T> the entity type
     * @return specification for active records
     */
    public static <T> Specification<T> isActive() {
        return (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> cb.isTrue(root.get(IS_ACTIVE));
    }

    /**
     * Filters to only inactive records (isActive = false).
     * 
     * @param <T> the entity type
     * @return specification for inactive records
     */
    public static <T> Specification<T> isInactive() {
        return (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> cb.isFalse(root.get(IS_ACTIVE));
    }

    // ========================================
    // Composite Specifications
    // ========================================

    /**
     * Standard public API filter: not deleted AND active.
     * This is the most common use case for public endpoints.
     * 
     * @param <T> the entity type
     * @return specification for publicly visible records
     */
    public static <T> Specification<T> publicVisible() {
        Specification<T> notDeletedSpec = notDeleted();
        Specification<T> isActiveSpec = isActive();
        return notDeletedSpec.and(isActiveSpec);
    }

    /**
     * Admin filter for restorable records: deleted but was active.
     * 
     * @param <T> the entity type
     * @return specification for restorable records
     */
    public static <T> Specification<T> restorable() {
        return onlyDeleted();
    }
}
