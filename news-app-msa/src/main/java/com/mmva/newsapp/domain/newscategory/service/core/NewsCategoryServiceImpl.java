package com.mmva.newsapp.domain.newscategory.service.core;

// Configuration imports
import com.mmva.newsapp.domain.newscategory.config.core.NewsCategoryCacheConstants;

// DTO imports
import com.mmva.newsapp.domain.newscategory.dto.core.NewsCategoryRequestDto;
import com.mmva.newsapp.domain.newscategory.dto.core.NewsCategoryResponseDto;
import com.mmva.newsapp.domain.newscategory.dto.audit.NewsCategoryAuditLogDto;

// Enum imports
import com.mmva.newsapp.domain.newscategory.enums.core.NewsCategoryAuditAction;
import com.mmva.newsapp.domain.newscategory.enums.NewsCategoryStatus;

// Exception imports
import com.mmva.newsapp.domain.newscategory.exception.core.NewsCategoryNotFoundException;

// Mapper imports
import com.mmva.newsapp.domain.newscategory.mapper.core.NewsCategoryMapper;

// Model imports
import com.mmva.newsapp.domain.newscategory.model.core.NewsCategory;

// Repository imports
import com.mmva.newsapp.domain.newscategory.repository.core.NewsCategoryRepository;
import com.mmva.newsapp.domain.newscategory.repository.audit.NewsCategoryAuditLogRepository;
import com.mmva.newsapp.domain.newscategory.model.audit.NewsCategoryAuditLog;
import com.mmva.newsapp.domain.newscategory.audit.constants.NewsCategoryAuditActions;

// Specification imports
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import com.mmva.newsapp.infrastructure.common.audit.repository.SoftDeleteSpec;

// Audit imports
import com.mmva.newsapp.infrastructure.common.audit.service.AuditingUtility;
import com.mmva.newsapp.infrastructure.requestanalytics.dto.RequestClientInfoDto;

// Utility imports
import com.mmva.newsapp.infrastructure.common.util.SlugUtils;

// Lombok imports
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// Spring imports
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Java imports
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.time.Instant;

/**
 * Implementation of {@link NewsCategoryService} for managing newsapp
 * categories.
 * <p>
 * Provides CRUD operations with caching support and audit logging.
 * All write operations evict cache entries to maintain data consistency.
 * </p>
 * 
 * @author MMVA Team
 * @version 1.0
 * @since 2024-06-01
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NewsCategoryServiceImpl implements NewsCategoryService {

    private static final String ENTITY_NAME = "NewsCategory";
    private static final String FIELD_ID = "id";

    private final NewsCategoryRepository repository;
    private final NewsCategoryMapper mapper;
    private final AuditingUtility auditingUtility;
    private final NewsCategoryAuditLogRepository auditLogRepository;

    // =========================
    // Create Operations
    // =========================

    @Override
    @Transactional
    @CacheEvict(value = NewsCategoryCacheConstants.CATEGORY_CACHE, allEntries = true)
    public NewsCategoryResponseDto create(NewsCategoryRequestDto dto, RequestClientInfoDto clientInfo) {
        log.info("NewsCategoryService: Creating newscategory - EN='{}', ES='{}' by admindashboard: {}",
                dto.getCategoryNameEn(), dto.getCategoryNameEs(), dto.getAdminId());

        generateSlugIfNotProvided(dto);

        NewsCategory entity = mapper.toEntity(dto);
        try {
            repository.save(entity);
            logAuditAction(entity.getNewsCategoriesId(), NewsCategoryAuditAction.CREATE,
                    "News category created: " + dto.getCategoryNameEn(), dto.getAdminId(), clientInfo);
            log.info("NewsCategoryService: Category created successfully - ID: {} by admindashboard: {}",
                    entity.getNewsCategoriesId(), dto.getAdminId());
            return mapper.toResponseDto(entity);
        } catch (Exception e) {
            log.error("NewsCategoryService: Failed to create newscategory - EN='{}', ES='{}'. Error: {}",
                    dto.getCategoryNameEn(), dto.getCategoryNameEs(), e.getMessage());
            throw e;
        }
    }

    // =========================
    // Read Operations
    // =========================

    @Override
    @Cacheable(value = NewsCategoryCacheConstants.CATEGORY_CACHE, key = "#id")
    public NewsCategoryResponseDto getById(UUID id) {
        log.debug("NewsCategoryService: Fetching newscategory by ID: {}", id);

        NewsCategory entity = findByIdOrThrow(id);

        log.debug("NewsCategoryService: Category fetched successfully - ID: {}", id);
        return mapper.toResponseDto(entity);
    }

    @Override
    @Cacheable(value = NewsCategoryCacheConstants.CATEGORY_CACHE, key = "#root.methodName + '-' + #pageable.pageNumber + '-' + #pageable.pageSize + '-' + #pageable.sort.toString()")
    public Page<NewsCategoryResponseDto> getActiveCategories(Pageable pageable) {
        String cacheKey = "getActiveCategories-" + pageable.getPageNumber() + "-" + pageable.getPageSize() + "-"
                + pageable.getSort().toString();
        log.info("NewsCategoryService: [CACHE KEY] {}", cacheKey);
        log.debug(
                "NewsCategoryService: Fetching ACTIVE categories only - page: {}, size: {}, sort: {}, sort.toString()='{}'",
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort(), pageable.getSort().toString());

        // Create specification combining: not deleted AND status = ACTIVE
        Specification<NewsCategory> spec = (root, query, cb) -> {
            Predicate notDeleted = cb.isNull(root.get("deletedAt"));
            Predicate isActive = cb.equal(root.get("status"), NewsCategoryStatus.ACTIVE);
            return cb.and(notDeleted, isActive);
        };

        Page<NewsCategory> page = repository.findAll(spec, pageable);
        log.debug("NewsCategoryService: Fetched {} ACTIVE categories (page {} of {})",
                page.getNumberOfElements(), page.getNumber() + 1, page.getTotalPages());
        return page.map(mapper::toResponseDto);
    }

    @Override
    @Cacheable(value = NewsCategoryCacheConstants.CATEGORY_CACHE, key = "#root.methodName + '-' + #pageable.pageNumber + '-' + #pageable.pageSize + '-' + #pageable.sort.toString()")
    public Page<NewsCategoryResponseDto> getAllActiveInactiveCategories(Pageable pageable) {
        String cacheKey = "getAllActiveInactiveCategories-" + pageable.getPageNumber() + "-" + pageable.getPageSize()
                + "-" + pageable.getSort().toString();
        log.info("NewsCategoryService: [CACHE KEY] {}", cacheKey);
        log.debug(
                "NewsCategoryService: Fetching ACTIVE/INACTIVE categories - page: {}, size: {}, sort: {}, sort.toString()='{}' (excludes DELETED)",
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort(), pageable.getSort().toString());
        Page<NewsCategory> page = repository.findAll(SoftDeleteSpec.notDeleted(), pageable);
        log.debug("NewsCategoryService: Fetched {} ACTIVE/INACTIVE categories (page {} of {})",
                page.getNumberOfElements(), page.getNumber() + 1, page.getTotalPages());
        return page.map(mapper::toResponseDto);
    }

    @Override
    @Cacheable(value = NewsCategoryCacheConstants.CATEGORY_CACHE, key = "#root.methodName + '-' + #pageable.pageNumber + '-' + #pageable.pageSize + '-' + #pageable.sort.toString()")
    public Page<NewsCategoryResponseDto> getAllIncludingDeleted(Pageable pageable) {
        String cacheKey = "getAllIncludingDeleted-" + pageable.getPageNumber() + "-" + pageable.getPageSize() + "-"
                + pageable.getSort().toString();
        log.info("NewsCategoryService: [CACHE KEY] {}", cacheKey);
        log.debug(
                "NewsCategoryService: Fetching ALL categories INCLUDING DELETED - page: {}, size: {}, sort: {}, sort.toString()='{}'",
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort(), pageable.getSort().toString());
        Page<NewsCategory> page = repository.findAll(pageable);
        log.debug("NewsCategoryService: Fetched {} total categories (page {} of {}, INCLUDING DELETED)",
                page.getNumberOfElements(), page.getNumber() + 1, page.getTotalPages());
        return page.map(mapper::toResponseDto);
    }

    // =========================
    // Update Operations
    // =========================

    @Override
    @Transactional
    @CacheEvict(value = NewsCategoryCacheConstants.CATEGORY_CACHE, allEntries = true)
    public NewsCategoryResponseDto update(UUID id, NewsCategoryRequestDto dto, RequestClientInfoDto clientInfo) {
        log.info("NewsCategoryService: Updating newscategory - ID: {} by admindashboard: {}", id, dto.getAdminId());

        generateSlugIfNotProvided(dto);

        NewsCategory entity = findByIdOrThrow(id);

        try {
            mapper.updateEntityFromDto(dto, entity);
            repository.save(entity);
            logAuditAction(entity.getNewsCategoriesId(), NewsCategoryAuditAction.UPDATE,
                    "News category updated: " + dto.getCategoryNameEn(), dto.getAdminId(), clientInfo);
            log.info("NewsCategoryService: Category updated successfully - ID: {} by admindashboard: {}", id,
                    dto.getAdminId());
            return mapper.toResponseDto(entity);
        } catch (Exception e) {
            log.error("NewsCategoryService: Failed to update newscategory - ID: {}. Error: {}", id, e.getMessage());
            throw e;
        }
    }

    // =========================
    // Delete Operations
    // =========================

    @Override
    @Transactional
    @CacheEvict(value = NewsCategoryCacheConstants.CATEGORY_CACHE, allEntries = true)
    public void delete(UUID id, UUID adminId, RequestClientInfoDto clientInfo) {
        log.info("NewsCategoryService: Soft-deleting newscategory - ID: {} by admindashboard: {}", id, adminId);

        // Verify newscategory exists before deletion
        NewsCategory entity = findByIdOrThrow(id);

        try {
            // SOFT DELETE: Set deleted_at timestamp, preserve all data for audit trail
            entity.setDeletedAt(Instant.now());
            entity.setStatus(NewsCategoryStatus.DELETED); // Mark status as DELETED
            repository.save(entity);

            logAuditAction(id, NewsCategoryAuditAction.DELETE,
                    "News category soft-deleted - preserves referential integrity for existing news",
                    adminId, clientInfo);

            log.info("NewsCategoryService: Category soft-deleted successfully - ID: {} by admindashboard: {}", id,
                    adminId);
        } catch (Exception e) {
            log.error("NewsCategoryService: Failed to soft-delete newscategory - ID: {}. Error: {}", id,
                    e.getMessage());
            throw e;
        }
    }

    // =========================
    // Action Operations
    // =========================

    @Override
    @Transactional
    @CacheEvict(value = NewsCategoryCacheConstants.CATEGORY_CACHE, allEntries = true)
    public NewsCategoryResponseDto activateCategory(UUID id) {
        log.info("NewsCategoryService: Activating category - ID: {}", id);

        NewsCategory entity = findByIdOrThrow(id);
        entity.setStatus(NewsCategoryStatus.ACTIVE);
        entity.setDeletedAt(null); // Clear soft delete
        entity.setUpdatedAt(Instant.now());

        try {
            repository.save(entity);
            logAuditAction(id, NewsCategoryAuditAction.ACTIVATE,
                    "Category activated", null, null);
            log.info("NewsCategoryService: Category activated successfully - ID: {}", id);
            return mapper.toResponseDto(entity);
        } catch (Exception e) {
            log.error("NewsCategoryService: Failed to activate category - ID: {}. Error: {}", id, e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = NewsCategoryCacheConstants.CATEGORY_CACHE, allEntries = true)
    public NewsCategoryResponseDto deactivateCategory(UUID id) {
        log.info("NewsCategoryService: Deactivating category - ID: {}", id);

        NewsCategory entity = findByIdOrThrow(id);
        entity.setStatus(NewsCategoryStatus.INACTIVE);
        entity.setUpdatedAt(Instant.now());

        try {
            repository.save(entity);
            logAuditAction(id, NewsCategoryAuditAction.DEACTIVATE,
                    "Category deactivated", null, null);
            log.info("NewsCategoryService: Category deactivated successfully - ID: {}", id);
            return mapper.toResponseDto(entity);
        } catch (Exception e) {
            log.error("NewsCategoryService: Failed to deactivate category - ID: {}. Error: {}", id, e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = NewsCategoryCacheConstants.CATEGORY_CACHE, allEntries = true)
    public NewsCategoryResponseDto restoreCategory(UUID id) {
        log.info("NewsCategoryService: Restoring category - ID: {}", id);

        NewsCategory entity = findByIdOrThrow(id);
        entity.setStatus(NewsCategoryStatus.ACTIVE);
        entity.setDeletedAt(null); // Clear soft delete marker
        entity.setUpdatedAt(Instant.now());

        try {
            repository.save(entity);
            logAuditAction(id, NewsCategoryAuditAction.RESTORE,
                    "Category restored", null, null);
            log.info("NewsCategoryService: Category restored successfully - ID: {}", id);
            return mapper.toResponseDto(entity);
        } catch (Exception e) {
            log.error("NewsCategoryService: Failed to restore category - ID: {}. Error: {}", id, e.getMessage());
            throw e;
        }
    }

    @Override
    public List<NewsCategoryAuditLogDto> getAuditLogs(UUID id) {
        log.debug("NewsCategoryService: Fetching audit logs - ID: {}", id);

        // Verify category exists
        findByIdOrThrow(id);

        List<NewsCategoryAuditLog> logs = auditLogRepository
                .findByNewsCategoryIdOrderByCreatedAtDesc(id, Pageable.unpaged()).getContent();
        return logs.stream()
                .map(this::convertToAuditLogDto)
                .collect(Collectors.toList());
    }

    // =========================
    // Private Helper Methods
    // =========================

    /**
     * Finds a newsapp newscategory by ID or throws an exception if not found.
     *
     * @param id the newscategory UUID
     * @return the found NewsCategory entity
     * @throws ResourceNotFoundException if newscategory not found
     */
    private NewsCategory findByIdOrThrow(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("NewsCategoryService: Category not found - ID: {}", id);
                    return new NewsCategoryNotFoundException(id);
                });
    }

    /**
     * Generates a URL-friendly slug from the English newscategory name if not
     * provided.
     *
     * @param dto the newscategory request DTO to update
     */
    private void generateSlugIfNotProvided(NewsCategoryRequestDto dto) {
        if (dto.getSlug() == null || dto.getSlug().isBlank()) {
            String generatedSlug = SlugUtils.slugify(dto.getCategoryNameEn());
            dto.setSlug(generatedSlug);
            log.debug("NewsCategoryService: Auto-generated slug: '{}' from name: '{}'",
                    generatedSlug, dto.getCategoryNameEn());
        }
    }

    /**
     * Logs an audit action for the category.
     *
     * @param categoryId the category UUID
     * @param action     the action type enum
     * @param message    the audit message
     * @param adminId    the admin who performed the action
     * @param clientInfo the request client information
     */
    private void logAuditAction(UUID categoryId, NewsCategoryAuditAction action, String message, UUID adminId,
            RequestClientInfoDto clientInfo) {
        try {
            String actionConstant = mapActionToConstant(action);
            String severity = resolveSeverity(actionConstant);

            auditingUtility.audit(
                    "NEWSCATEGORY",
                    adminId,
                    actionConstant,
                    categoryId,
                    "Category: " + categoryId,
                    message,
                    clientInfo,
                    severity,
                    NewsCategoryAuditLog.class,
                    auditLogRepository);
        } catch (Exception e) {
            log.error("NewsCategoryService: Failed to log audit action - Category: {}, Action: {}. Error: {}",
                    categoryId, action, e.getMessage());
        }
    }

    /**
     * Maps legacy NewsCategoryAuditAction enum to unified audit action constants.
     *
     * @param action the legacy action enum
     * @return the unified audit action constant
     */
    private String mapActionToConstant(NewsCategoryAuditAction action) {
        return switch (action) {
            case CREATE -> NewsCategoryAuditActions.CATEGORY_CREATED;
            case UPDATE -> NewsCategoryAuditActions.CATEGORY_UPDATED;
            case DELETE -> NewsCategoryAuditActions.CATEGORY_DELETED;
            case RESTORE -> NewsCategoryAuditActions.CATEGORY_RESTORED;
            case ACTIVATE -> "CATEGORY_ACTIVATED";
            case DEACTIVATE -> "CATEGORY_DEACTIVATED";
        };
    }

    /**
     * Resolves the severity level based on the audit action.
     *
     * @param action the audit action constant
     * @return the severity level (CRITICAL, HIGH, or MEDIUM)
     */
    private String resolveSeverity(String action) {
        return switch (action) {
            case String s when s.equals(NewsCategoryAuditActions.CATEGORY_CREATED) ||
                    s.equals(NewsCategoryAuditActions.CATEGORY_DELETED) ||
                    s.equals(NewsCategoryAuditActions.CATEGORY_RESTORED) ->
                "CRITICAL";
            case String s when s.equals(NewsCategoryAuditActions.CATEGORY_UPDATED) -> "HIGH";
            default -> "MEDIUM";
        };
    }

    /**
     * Converts a NewsCategoryAuditLog entity to NewsCategoryAuditLogDto.
     *
     * @param auditLog the audit log entity
     * @return the audit log DTO
     */
    private NewsCategoryAuditLogDto convertToAuditLogDto(NewsCategoryAuditLog auditLog) {
        return NewsCategoryAuditLogDto.builder()
                .id(auditLog.getId())
                .categoryId(auditLog.getNewsCategoryId())
                .action(auditLog.getAction())
                .actionDescription(auditLog.getReason())
                .details(auditLog.getDetails())
                .createdBy(auditLog.getActorId())
                .createdAt(auditLog.getCreatedAt() != null ? auditLog.getCreatedAt().toString() : null)
                .build();
    }
}
