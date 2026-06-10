package com.mmva.newsapp.domain.newssourceagency.mapper.audit;

import com.mmva.newsapp.domain.newssourceagency.dto.audit.NewsSourceAgencyAuditLogDto;
import com.mmva.newsapp.domain.newssourceagency.enums.core.NewsSourceAgencyAuditAction;
import com.mmva.newsapp.domain.newssourceagency.model.audit.NewsSourceAgencyAuditLog;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.Instant;

/**
 * MapStruct mapper for NewsSourceAgencyAuditLog entity.
 * Converts between entity and DTO representations.
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Mapper(componentModel = "spring")
public interface NewsSourceAgencyAuditLogMapper {

    @Mapping(target = "actionDescription", source = "action", qualifiedByName = "actionToDescription")
    @Mapping(target = "createdAt", source = "createdAt", qualifiedByName = "instantToString")
    NewsSourceAgencyAuditLogDto toDto(NewsSourceAgencyAuditLog entity);

    /**
     * Converts action string to human-readable description.
     *
     * @param action the action name (e.g., "CREATE", "UPDATE")
     * @return the human-readable description
     */
    @Named("actionToDescription")
    default String actionToDescription(String action) {
        if (action == null) {
            return null;
        }
        try {
            return NewsSourceAgencyAuditAction.valueOf(action).getDescription();
        } catch (IllegalArgumentException e) {
            return action; // Return as-is if not a valid enum value
        }
    }

    /**
     * Converts Instant to ISO 8601 string.
     *
     * @param instant the instant to convert
     * @return ISO 8601 formatted string
     */
    @Named("instantToString")
    default String instantToString(Instant instant) {
        return instant != null ? instant.toString() : null;
    }
}
