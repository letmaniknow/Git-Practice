package com.mmva.newsapp.domain.newscategory.service.core;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.mmva.newsapp.domain.newscategory.dto.core.NewsCategoryRequestDto;
import com.mmva.newsapp.domain.newscategory.dto.core.NewsCategoryResponseDto;
import com.mmva.newsapp.domain.newscategory.dto.audit.NewsCategoryAuditLogDto;
import com.mmva.newsapp.infrastructure.requestanalytics.dto.RequestClientInfoDto;
import com.mmva.newsapp.infrastructure.common.exception.ResourceNotFoundException;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for News Category management operations.
 * <p>
 * Provides business logic for creating, retrieving, updating, and deleting
 * newsapp categories. All operations support caching for improved performance.
 * </p>
 * 
 * <h3>Features:</h3>
 * <ul>
 * <li>Full CRUD operations</li>
 * <li>Pagination support</li>
 * <li>Audit logging integration</li>
 * <li>Cache management</li>
 * </ul>
 * 
 * @author MMVA Team
 * @version 1.0
 * @since 2024-06-01
 * @see NewsCategoryServiceImpl
 */
public interface NewsCategoryService {

    // =========================
    // Create Operations
    // =========================

    /**
     * Creates a new newsapp newscategory.
     * <p>
     * Automatically generates a URL-friendly slug from the English newscategory
     * name
     * if not provided. Logs the creation action for audit purposes.
     * </p>
     *
     * @param dto        the newscategory creation request containing name and
     *                   optional slug
     * @param clientInfo the request client information for audit logging
     * @return the created newscategory response with generated ID and timestamps
     * @throws IllegalArgumentException if the request data is invalid
     */
    NewsCategoryResponseDto create(NewsCategoryRequestDto dto, RequestClientInfoDto clientInfo);

    // =========================
    // Read Operations
    // =========================

    /**
     * Retrieves a newscategory by its unique identifier.
     * <p>
     * Results are cached for improved performance on repeated requests.
     * </p>
     *
     * @param id the unique identifier of the newscategory
     * @return the newscategory response
     * @throws ResourceNotFoundException if no newscategory exists with the given ID
     */
    NewsCategoryResponseDto getById(UUID id);

    /**
     * Retrieves only ACTIVE categories with pagination support.
     * <p>
     * Intended for news creation/editing forms. Returns only ACTIVE categories
     * to ensure news articles are tagged with valid, available categories.
     * Soft-deleted and inactive categories are excluded.
     * </p>
     *
     * @param pageable pagination and sorting parameters
     * @return paginated list of ACTIVE categories only
     */
    Page<NewsCategoryResponseDto> getActiveCategories(Pageable pageable);

    /**
     * Retrieves all ACTIVE and INACTIVE categories with pagination support.
     * <p>
     * Intended for public/client-facing category browsing and listing screens.
     * Returns ACTIVE and INACTIVE categories but excludes soft-deleted ones.
     * Recommended for API responses that need to display available categories
     * while maintaining pagination efficiency.
     * </p>
     *
     * @param pageable pagination and sorting parameters
     * @return paginated list of ACTIVE and INACTIVE categories (excludes deleted)
     */
    Page<NewsCategoryResponseDto> getAllActiveInactiveCategories(Pageable pageable);

    /**
     * Retrieves ALL categories including soft-deleted ones with pagination support.
     * <p>
     * Intended exclusively for administrator management views. Returns ACTIVE,
     * INACTIVE, and DELETED categories to provide complete category history and
     * management capabilities. This method bypasses soft-delete filtering.
     * </p>
     *
     * @param pageable pagination and sorting parameters
     * @return paginated list of ALL categories (ACTIVE, INACTIVE, and DELETED)
     */
    Page<NewsCategoryResponseDto> getAllIncludingDeleted(Pageable pageable);

    // =========================
    // Update Operations
    // =========================

    /**
     * Updates an existing newscategory.
     * <p>
     * Evicts the newscategory from cache after successful update.
     * Logs the update action for audit purposes.
     * </p>
     *
     * @param id         the unique identifier of the newscategory to update
     * @param dto        the updated newscategory data
     * @param clientInfo the request client information for audit logging
     * @return the updated newscategory response
     * @throws ResourceNotFoundException if no newscategory exists with the given ID
     */
    NewsCategoryResponseDto update(UUID id, NewsCategoryRequestDto dto, RequestClientInfoDto clientInfo);

    // =========================
    // Delete Operations
    // =========================

    /**
     * Deletes a newscategory by its unique identifier.
     * <p>
     * Evicts the newscategory from cache after deletion.
     * Logs the deletion action with the admindashboard who performed it.
     * </p>
     *
     * @param id         the unique identifier of the newscategory to delete
     * @param adminId    the UUID of the admindashboard performing the deletion
     * @param clientInfo the request client information for audit logging
     * @throws ResourceNotFoundException if no newscategory exists with the given ID
     */
    void delete(UUID id, UUID adminId, RequestClientInfoDto clientInfo);

    // =========================
    // Action Operations
    // =========================

    /**
     * Activates a news category (sets status to ACTIVE and clears deletedAt).
     * <p>
     * Evicts cache after successful activation.
     * Logs the activation action for audit purposes.
     * </p>
     *
     * @param id the category ID to activate
     * @return the activated category response
     * @throws ResourceNotFoundException if category not found
     */
    NewsCategoryResponseDto activateCategory(UUID id);

    /**
     * Deactivates a news category (sets status to INACTIVE).
     * <p>
     * Evicts cache after successful deactivation.
     * Logs the deactivation action for audit purposes.
     * </p>
     *
     * @param id the category ID to deactivate
     * @return the deactivated category response
     * @throws ResourceNotFoundException if category not found
     */
    NewsCategoryResponseDto deactivateCategory(UUID id);

    /**
     * Restores a soft-deleted news category (sets status to ACTIVE, clears
     * deletedAt).
     * <p>
     * Evicts cache after successful restoration.
     * Logs the restoration action for audit purposes.
     * </p>
     *
     * @param id the category ID to restore
     * @return the restored category response
     * @throws ResourceNotFoundException if category not found
     */
    NewsCategoryResponseDto restoreCategory(UUID id);

    /**
     * Gets audit logs for a specific category.
     *
     * @param id the category ID
     * @return list of audit log entries
     * @throws ResourceNotFoundException if category not found
     */
    List<NewsCategoryAuditLogDto> getAuditLogs(UUID id);
}
