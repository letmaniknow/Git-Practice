package com.mmva.newsapp.domain.newssourceagency.service.core;

import com.mmva.newsapp.domain.newssourceagency.dto.core.NewsSourceAgencyRequestDto;
import com.mmva.newsapp.domain.newssourceagency.dto.core.NewsSourceAgencyResponseDto;
import com.mmva.newsapp.infrastructure.common.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for News Source Agency management operations.
 * 
 * <p>
 * Provides business logic for managing external newsapp sources including
 * wire services (AP, Reuters, AFP), partner publications, and other
 * syndicated content providers.
 * </p>
 * 
 * <h3>Features:</h3>
 * <ul>
 * <li>Full CRUD operations</li>
 * <li>Pagination support</li>
 * <li>Audit logging integration</li>
 * <li>Trust and status management</li>
 * </ul>
 * 
 * @author MMVA Team
 * @version 1.0
 * @since 1.0.0
 * @see NewsSourceAgencyServiceImpl
 */
public interface NewsSourceAgencyService {

    // =========================
    // Create Operations
    // =========================

    /**
     * Creates a new newsapp source agency.
     * 
     * <p>
     * Validates that the agency code is unique before creation.
     * Logs the creation action for audit purposes.
     * </p>
     *
     * @param dto the agency creation request
     * @return the created agency response with generated ID and timestamps
     * @throws IllegalArgumentException if agency code already exists
     */
    NewsSourceAgencyResponseDto create(NewsSourceAgencyRequestDto dto);

    // =========================
    // Read Operations
    // =========================

    /**
     * Retrieves an agency by its unique identifier.
     *
     * @param id the unique identifier of the agency
     * @return the agency response
     * @throws ResourceNotFoundException if no agency exists with the given ID
     */
    NewsSourceAgencyResponseDto getById(UUID id);

    /**
     * Retrieves an agency by its code.
     *
     * @param agencyCode the unique agency code (e.g., "REUTERS", "AP")
     * @return the agency response
     * @throws ResourceNotFoundException if no agency exists with the given code
     */
    NewsSourceAgencyResponseDto getByCode(String agencyCode);

    /**
     * Retrieves all agencies without pagination.
     * 
     * <p>
     * Use this method when the complete list is needed (e.g., for dropdowns).
     * </p>
     *
     * @return list of all active agencies
     */
    List<NewsSourceAgencyResponseDto> getAll();

    /**
     * Retrieves all agencies with pagination support.
     *
     * @param pageable pagination and sorting parameters
     * @return paginated list of agencies
     */
    Page<NewsSourceAgencyResponseDto> getAll(Pageable pageable);

    /**
     * Retrieves all active and enabled agencies.
     * 
     * <p>
     * Use this for assignment dropdowns where only enabled agencies should appear.
     * </p>
     *
     * @return list of active and enabled agencies
     */
    List<NewsSourceAgencyResponseDto> getAllEnabled();

    /**
     * Retrieves all trusted agencies.
     *
     * @return list of trusted agencies
     */
    List<NewsSourceAgencyResponseDto> getAllTrusted();

    // =========================
    // Update Operations
    // =========================

    /**
     * Updates an existing agency.
     * 
     * <p>
     * Validates that the new agency code (if changed) is unique.
     * Logs the update action for audit purposes.
     * </p>
     *
     * @param id  the unique identifier of the agency to update
     * @param dto the updated agency data
     * @return the updated agency response
     * @throws ResourceNotFoundException if no agency exists with the given ID
     * @throws IllegalArgumentException  if new agency code conflicts with existing
     */
    NewsSourceAgencyResponseDto update(UUID id, NewsSourceAgencyRequestDto dto);

    // =========================
    // Delete Operations
    // =========================

    /**
     * Soft-deletes an agency by its unique identifier.
     * 
     * <p>
     * Logs the deletion action with the admindashboard who performed it.
     * </p>
     *
     * @param id      the unique identifier of the agency to delete
     * @param adminId the UUID of the admindashboard performing the deletion
     * @throws ResourceNotFoundException if no agency exists with the given ID
     */
    void delete(UUID id, UUID adminId);

    // =========================
    // Status Operations
    // =========================

    /**
     * Toggles the active status of an agency.
     *
     * @param id      the unique identifier of the agency
     * @param adminId the UUID of the admindashboard performing the action
     * @return the updated agency response
     * @throws ResourceNotFoundException if no agency exists with the given ID
     */
    NewsSourceAgencyResponseDto toggleActive(UUID id, UUID adminId);

    /**
     * Toggles the trusted status of an agency.
     *
     * @param id      the unique identifier of the agency
     * @param adminId the UUID of the admindashboard performing the action
     * @return the updated agency response
     * @throws ResourceNotFoundException if no agency exists with the given ID
     */
    NewsSourceAgencyResponseDto toggleTrusted(UUID id, UUID adminId);

    // =========================
    // Validation Operations
    // =========================

    /**
     * Checks if an agency exists with the given ID.
     *
     * @param id the agency ID to check
     * @return true if agency exists and is not deleted
     */
    boolean existsById(UUID id);

    /**
     * Checks if an agency code is already in use.
     *
     * @param agencyCode the code to check
     * @return true if code is already in use
     */
    boolean existsByCode(String agencyCode);
}
