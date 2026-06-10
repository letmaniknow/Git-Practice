package com.mmva.newsapp.domain.newssourceagency.repository.core;

import com.mmva.newsapp.domain.newssourceagency.model.core.NewsSourceAgency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for NewsSourceAgency entity.
 * 
 * <p>
 * Provides data access operations for newsapp source agencies including
 * wire services, partner publications, and other external sources.
 * </p>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Repository
public interface NewsSourceAgencyRepository
                extends JpaRepository<NewsSourceAgency, UUID>, JpaSpecificationExecutor<NewsSourceAgency> {

        // ========================================
        // Existence Checks
        // ========================================

        /**
         * Check if agency code exists (includes soft-deleted).
         * 
         * @param agencyCode the agency code to check
         * @return true if exists
         */
        boolean existsByAgencyCode(String agencyCode);

        /**
         * Check if active (non-deleted) agency exists with given code.
         * Use for uniqueness validation.
         * 
         * @param agencyCode the agency code to check
         * @return true if active agency exists with this code
         */
        @Query("SELECT COUNT(a) > 0 FROM NewsSourceAgency a WHERE a.deletedAt IS NULL AND a.agencyCode = :agencyCode")
        boolean existsActiveByAgencyCode(@Param("agencyCode") String agencyCode);

        /**
         * Check if active agency exists with given code excluding a specific ID.
         * Useful for update validation.
         * 
         * @param agencyCode the agency code to check
         * @param excludeId  the agency ID to exclude from check
         * @return true if another active agency exists with this code
         */
        @Query("SELECT COUNT(a) > 0 FROM NewsSourceAgency a WHERE a.deletedAt IS NULL AND a.agencyCode = :agencyCode AND a.agencyId != :excludeId")
        boolean existsActiveByAgencyCodeExcludingId(@Param("agencyCode") String agencyCode,
                        @Param("excludeId") UUID excludeId);

        // ========================================
        // Find Operations
        // ========================================

        /**
         * Find active agency by code.
         * 
         * @param agencyCode the agency code
         * @return the agency if found and not deleted
         */
        @Query("SELECT a FROM NewsSourceAgency a WHERE a.deletedAt IS NULL AND a.agencyCode = :agencyCode")
        Optional<NewsSourceAgency> findActiveByAgencyCode(@Param("agencyCode") String agencyCode);

        /**
         * Find active agency by ID.
         * 
         * @param agencyId the agency ID
         * @return the agency if found and not deleted
         */
        @Query("SELECT a FROM NewsSourceAgency a WHERE a.deletedAt IS NULL AND a.agencyId = :agencyId")
        Optional<NewsSourceAgency> findActiveById(@Param("agencyId") UUID agencyId);

        /**
         * Find all active agencies.
         * 
         * @return list of all non-deleted agencies
         */
        @Query("SELECT a FROM NewsSourceAgency a WHERE a.deletedAt IS NULL ORDER BY a.agencyName ASC")
        List<NewsSourceAgency> findAllActive();

        /**
         * Find all active and enabled agencies.
         * 
         * @return list of active and enabled agencies
         */
        @Query("SELECT a FROM NewsSourceAgency a WHERE a.deletedAt IS NULL AND a.isActive = true ORDER BY a.agencyName ASC")
        List<NewsSourceAgency> findAllActiveAndEnabled();

        /**
         * Find all trusted agencies.
         * 
         * @return list of trusted agencies
         */
        @Query("SELECT a FROM NewsSourceAgency a WHERE a.deletedAt IS NULL AND a.isActive = true AND a.isTrusted = true ORDER BY a.agencyName ASC")
        List<NewsSourceAgency> findAllTrusted();
}
