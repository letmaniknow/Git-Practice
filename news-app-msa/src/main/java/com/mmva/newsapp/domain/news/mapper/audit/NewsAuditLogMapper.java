package com.mmva.newsapp.domain.news.mapper.audit;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import com.mmva.newsapp.domain.news.dto.audit.NewsAuditLogDto;
import com.mmva.newsapp.domain.news.enums.core.NewsAuditAction;
import com.mmva.newsapp.domain.news.audit.model.NewsAuditLog;

import java.util.List;

/**
 * MapStruct mapper for NewsAuditLog entity.
 * 
 * <p>
 * Provides mapping between NewsAuditLog entity and NewsAuditLogDto
 * with support for action description lookup from NewsAuditAction enum.
 * </p>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Mapper(componentModel = "spring")
public interface NewsAuditLogMapper {

    /**
     * Converts NewsAuditLog entity to NewsAuditLogDto.
     * 
     * @param entity the audit log entity
     * @return the DTO
     */
    NewsAuditLogDto toDto(NewsAuditLog entity);

    /**
     * Converts a list of NewsAuditLog entities to DTOs.
     * 
     * @param entities the list of audit log entities
     * @return the list of DTOs
     */
    List<NewsAuditLogDto> toDtoList(List<NewsAuditLog> entities);

    /**
     * Converts action string to human-readable description.
     * Falls back to the action string if enum lookup fails.
     * 
     * @param action the action string
     * @return the description
     */
    @Named("actionToDescription")
    default String actionToDescription(String action) {
        if (action == null) {
            return null;
        }
        try {
            NewsAuditAction auditAction = NewsAuditAction.valueOf(action.toUpperCase());
            return auditAction.getDescription();
        } catch (IllegalArgumentException e) {
            // Fallback: return the raw action string if not found in enum
            return action;
        }
    }
}
