package com.mmva.newsapp.domain.newssourceagency.service.core;

// DTO imports
import com.mmva.newsapp.domain.newssourceagency.dto.core.NewsSourceAgencyRequestDto;
import com.mmva.newsapp.domain.newssourceagency.dto.core.NewsSourceAgencyResponseDto;

// Enum imports
import com.mmva.newsapp.domain.newssourceagency.enums.core.NewsSourceAgencyAuditAction;

// Exception imports
import com.mmva.newsapp.domain.newssourceagency.exception.core.NewsSourceAgencyNotFoundException;
import com.mmva.newsapp.infrastructure.common.exception.InvalidRequestException;

// Mapper imports
import com.mmva.newsapp.domain.newssourceagency.mapper.core.NewsSourceAgencyMapper;

// Model imports
import com.mmva.newsapp.domain.newssourceagency.model.core.NewsSourceAgency;

// Repository imports
import com.mmva.newsapp.domain.newssourceagency.repository.core.NewsSourceAgencyRepository;

// Service imports
import com.mmva.newsapp.domain.newssourceagency.service.audit.NewsSourceAgencyAuditLogService;

// Specification imports
import com.mmva.newsapp.infrastructure.common.audit.repository.SoftDeleteSpec;

// Lombok imports
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// Spring imports
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Java imports
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of {@link NewsSourceAgencyService} for managing newsapp source
 * agencies.
 * 
 * <p>
 * Provides CRUD operations for external newsapp sources including wire
 * services,
 * partner publications, and other syndicated content providers.
 * </p>
 * 
 * @author MMVA Team
 * @version 1.0
 * @since 1.0.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NewsSourceAgencyServiceImpl implements NewsSourceAgencyService {

    private static final String ENTITY_NAME = "NewsSourceAgency";
    private static final String FIELD_ID = "id";
    private static final String FIELD_CODE = "agencyCode";

    private final NewsSourceAgencyRepository repository;
    private final NewsSourceAgencyMapper mapper;
    private final NewsSourceAgencyAuditLogService auditLogService;

    // =========================
    // Create Operations
    // =========================

    @Override
    @Transactional
    public NewsSourceAgencyResponseDto create(NewsSourceAgencyRequestDto dto) {
        log.info("NewsSourceAgencyService: Creating agency - code='{}', name='{}' by admindashboard: {}",
                dto.getAgencyCode(), dto.getAgencyName(), dto.getAdminId());

        // Validate agency code uniqueness
        validateAgencyCodeUnique(dto.getAgencyCode(), null);

        // Normalize agency code to uppercase
        dto.setAgencyCode(dto.getAgencyCode().toUpperCase());

        NewsSourceAgency entity = mapper.toEntity(dto);
        entity.setCreatedBy(dto.getAdminId());

        // Set defaults if not provided
        if (entity.getIsTrusted() == null) {
            entity.setIsTrusted(true);
        }
        if (entity.getIsActive() == null) {
            entity.setIsActive(true);
        }

        try {
            repository.save(entity);

            // Log audit action
            auditLogService.logAction(
                    entity.getAgencyId(),
                    NewsSourceAgencyAuditAction.CREATE,
                    String.format("Agency created: %s (%s)", entity.getAgencyName(), entity.getAgencyCode()),
                    dto.getAdminId());

            log.info("NewsSourceAgencyService: Agency created successfully - ID: {}, code: '{}' by admindashboard: {}",
                    entity.getAgencyId(), entity.getAgencyCode(), dto.getAdminId());
            return mapper.toResponseDto(entity);
        } catch (Exception e) {
            log.error("NewsSourceAgencyService: Failed to create agency - code='{}'. Error: {}",
                    dto.getAgencyCode(), e.getMessage());
            throw e;
        }
    }

    // =========================
    // Read Operations
    // =========================

    @Override
    public NewsSourceAgencyResponseDto getById(UUID id) {
        log.debug("NewsSourceAgencyService: Fetching agency by ID: {}", id);

        NewsSourceAgency entity = findActiveByIdOrThrow(id);

        log.debug("NewsSourceAgencyService: Agency fetched successfully - ID: {}", id);
        return mapper.toResponseDto(entity);
    }

    @Override
    public NewsSourceAgencyResponseDto getByCode(String agencyCode) {
        log.debug("NewsSourceAgencyService: Fetching agency by code: {}", agencyCode);

        NewsSourceAgency entity = repository.findActiveByAgencyCode(agencyCode.toUpperCase())
                .orElseThrow(() -> {
                    log.warn("NewsSourceAgencyService: Agency not found - code: {}", agencyCode);
                    return new NewsSourceAgencyNotFoundException(agencyCode, true);
                });

        log.debug("NewsSourceAgencyService: Agency fetched successfully - code: {}", agencyCode);
        return mapper.toResponseDto(entity);
    }

    @Override
    public List<NewsSourceAgencyResponseDto> getAll() {
        log.debug("NewsSourceAgencyService: Fetching all active agencies");

        List<NewsSourceAgency> agencies = repository.findAllActive();

        log.debug("NewsSourceAgencyService: Fetched {} agencies", agencies.size());
        return agencies.stream()
                .map(mapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public Page<NewsSourceAgencyResponseDto> getAll(Pageable pageable) {
        log.debug("NewsSourceAgencyService: Fetching agencies - page: {}, size: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        Page<NewsSourceAgency> page = repository.findAll(SoftDeleteSpec.notDeleted(), pageable);

        log.debug("NewsSourceAgencyService: Fetched {} agencies (page {} of {})",
                page.getNumberOfElements(), page.getNumber() + 1, page.getTotalPages());
        return page.map(mapper::toResponseDto);
    }

    @Override
    public List<NewsSourceAgencyResponseDto> getAllEnabled() {
        log.debug("NewsSourceAgencyService: Fetching all active and enabled agencies");

        List<NewsSourceAgency> agencies = repository.findAllActiveAndEnabled();

        log.debug("NewsSourceAgencyService: Fetched {} enabled agencies", agencies.size());
        return agencies.stream()
                .map(mapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<NewsSourceAgencyResponseDto> getAllTrusted() {
        log.debug("NewsSourceAgencyService: Fetching all trusted agencies");

        List<NewsSourceAgency> agencies = repository.findAllTrusted();

        log.debug("NewsSourceAgencyService: Fetched {} trusted agencies", agencies.size());
        return agencies.stream()
                .map(mapper::toResponseDto)
                .collect(Collectors.toList());
    }

    // =========================
    // Update Operations
    // =========================

    @Override
    @Transactional
    public NewsSourceAgencyResponseDto update(UUID id, NewsSourceAgencyRequestDto dto) {
        log.info("NewsSourceAgencyService: Updating agency - ID: {} by admindashboard: {}", id, dto.getAdminId());

        NewsSourceAgency entity = findActiveByIdOrThrow(id);

        // Validate agency code uniqueness if changed
        String newCode = dto.getAgencyCode().toUpperCase();
        if (!entity.getAgencyCode().equals(newCode)) {
            validateAgencyCodeUnique(newCode, id);
        }
        dto.setAgencyCode(newCode);

        try {
            mapper.updateEntityFromDto(dto, entity);
            entity.setUpdatedBy(dto.getAdminId());
            repository.save(entity);

            // Log audit action
            auditLogService.logAction(
                    entity.getAgencyId(),
                    NewsSourceAgencyAuditAction.UPDATE,
                    String.format("Agency updated: %s (%s)", entity.getAgencyName(), entity.getAgencyCode()),
                    dto.getAdminId());

            log.info("NewsSourceAgencyService: Agency updated successfully - ID: {} by admindashboard: {}", id,
                    dto.getAdminId());
            return mapper.toResponseDto(entity);
        } catch (Exception e) {
            log.error("NewsSourceAgencyService: Failed to update agency - ID: {}. Error: {}", id, e.getMessage());
            throw e;
        }
    }

    // =========================
    // Delete Operations
    // =========================

    @Override
    @Transactional
    public void delete(UUID id, UUID adminId) {
        log.info("NewsSourceAgencyService: Soft-deleting agency - ID: {} by admindashboard: {}", id, adminId);

        NewsSourceAgency entity = findActiveByIdOrThrow(id);

        try {
            entity.setDeletedAt(Instant.now());
            entity.setDeletedBy(adminId);
            repository.save(entity);

            // Log audit action
            auditLogService.logAction(
                    entity.getAgencyId(),
                    NewsSourceAgencyAuditAction.DELETE,
                    String.format("Agency soft-deleted: %s (%s)", entity.getAgencyName(), entity.getAgencyCode()),
                    adminId);

            log.info("NewsSourceAgencyService: Agency soft-deleted successfully - ID: {} by admindashboard: {}", id,
                    adminId);
        } catch (Exception e) {
            log.error("NewsSourceAgencyService: Failed to delete agency - ID: {}. Error: {}", id, e.getMessage());
            throw e;
        }
    }

    // =========================
    // Status Operations
    // =========================

    @Override
    @Transactional
    public NewsSourceAgencyResponseDto toggleActive(UUID id, UUID adminId) {
        log.info("NewsSourceAgencyService: Toggling active status - ID: {} by admindashboard: {}", id, adminId);

        NewsSourceAgency entity = findActiveByIdOrThrow(id);
        boolean newStatus = !Boolean.TRUE.equals(entity.getIsActive());

        entity.setIsActive(newStatus);
        entity.setUpdatedBy(adminId);
        repository.save(entity);

        // Log audit action
        auditLogService.logAction(
                entity.getAgencyId(),
                NewsSourceAgencyAuditAction.STATUS_CHANGED,
                String.format("Agency %s: %s (%s)", newStatus ? "activated" : "deactivated",
                        entity.getAgencyName(), entity.getAgencyCode()),
                adminId);

        log.info("NewsSourceAgencyService: Agency active status toggled to {} - ID: {} by admindashboard: {}",
                newStatus, id, adminId);
        return mapper.toResponseDto(entity);
    }

    @Override
    @Transactional
    public NewsSourceAgencyResponseDto toggleTrusted(UUID id, UUID adminId) {
        log.info("NewsSourceAgencyService: Toggling trusted status - ID: {} by admindashboard: {}", id, adminId);

        NewsSourceAgency entity = findActiveByIdOrThrow(id);
        boolean newStatus = !Boolean.TRUE.equals(entity.getIsTrusted());

        entity.setIsTrusted(newStatus);
        entity.setUpdatedBy(adminId);
        repository.save(entity);

        // Log audit action
        auditLogService.logAction(
                entity.getAgencyId(),
                NewsSourceAgencyAuditAction.TRUST_CHANGED,
                String.format("Agency trust status changed to %s: %s (%s)", newStatus,
                        entity.getAgencyName(), entity.getAgencyCode()),
                adminId);

        log.info("NewsSourceAgencyService: Agency trusted status toggled to {} - ID: {} by admindashboard: {}",
                newStatus, id, adminId);
        return mapper.toResponseDto(entity);
    }

    // =========================
    // Validation Operations
    // =========================

    @Override
    public boolean existsById(UUID id) {
        return repository.findActiveById(id).isPresent();
    }

    @Override
    public boolean existsByCode(String agencyCode) {
        return repository.existsActiveByAgencyCode(agencyCode.toUpperCase());
    }

    // =========================
    // Private Helper Methods
    // =========================

    /**
     * Finds an active (non-deleted) agency by ID or throws an exception.
     *
     * @param id the agency UUID
     * @return the found NewsSourceAgency entity
     * @throws NewsSourceAgencyNotFoundException if agency not found or deleted
     */
    private NewsSourceAgency findActiveByIdOrThrow(UUID id) {
        return repository.findActiveById(id)
                .orElseThrow(() -> {
                    log.warn("NewsSourceAgencyService: Agency not found - ID: {}", id);
                    return new NewsSourceAgencyNotFoundException(id);
                });
    }

    /**
     * Validates that the agency code is unique.
     *
     * @param agencyCode the code to validate
     * @param excludeId  optional ID to exclude (for updates)
     * @throws InvalidRequestException if code already exists
     */
    private void validateAgencyCodeUnique(String agencyCode, UUID excludeId) {
        String normalizedCode = agencyCode.toUpperCase();
        boolean exists;

        if (excludeId != null) {
            exists = repository.existsActiveByAgencyCodeExcludingId(normalizedCode, excludeId);
        } else {
            exists = repository.existsActiveByAgencyCode(normalizedCode);
        }

        if (exists) {
            log.warn("NewsSourceAgencyService: Agency code already exists: {}", normalizedCode);
            throw new InvalidRequestException(FIELD_CODE, "Agency code '" + normalizedCode + "' already exists");
        }
    }
}
