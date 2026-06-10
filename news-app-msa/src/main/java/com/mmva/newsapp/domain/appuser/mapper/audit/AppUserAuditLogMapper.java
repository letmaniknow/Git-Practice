package com.mmva.newsapp.domain.appuser.mapper.audit;

import com.mmva.newsapp.domain.appuser.dto.audit.AppUserAuditLogDto;
import com.mmva.newsapp.domain.appuser.enums.core.AppUserAuditAction;
import com.mmva.newsapp.domain.appuser.model.audit.AppUserAuditLog;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

/**
 * MapStruct mapper for AppUserAuditLog entity.
 * 
 * <p>
 * Handles conversions between AppUserAuditLog entity and DTOs.
 * </p>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AppUserAuditLogMapper {

    // ========================================
    // Entity → DTO
    // ========================================

    @Mapping(target = "appUsersAuditLogActionDescription", source = "appUsersAuditLogAction", qualifiedByName = "actionToDescription")
    AppUserAuditLogDto toDto(AppUserAuditLog entity);

    /**
     * Converts a list of entities to a list of DTOs.
     *
     * @param entities the list of AppUserAuditLog entities
     * @return the list of AppUserAuditLogDto objects
     */
    List<AppUserAuditLogDto> toDtoList(List<AppUserAuditLog> entities);

    // ========================================
    // DTO → Entity (for potential future use)
    // ========================================

    @Mapping(target = "appUsersAuditLogId", ignore = true)
    @Mapping(target = "appUsersAuditLogCreatedAt", ignore = true)
    AppUserAuditLog toEntity(AppUserAuditLogDto dto);

    // ========================================
    // Custom Mappings
    // ========================================

    /**
     * Converts an action string to its human-readable description.
     *
     * @param action the action string
     * @return the human-readable description
     */
    @Named("actionToDescription")
    default String actionToDescription(String action) {
        if (action == null) {
            return null;
        }
        try {
            AppUserAuditAction auditAction = AppUserAuditAction.valueOf(action);
            return auditAction.getDescription();
        } catch (IllegalArgumentException e) {
            return action;
        }
    }
}
