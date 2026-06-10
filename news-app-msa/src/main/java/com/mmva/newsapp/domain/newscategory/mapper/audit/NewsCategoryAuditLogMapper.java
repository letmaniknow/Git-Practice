package com.mmva.newsapp.domain.newscategory.mapper.audit;

import com.mmva.newsapp.domain.newscategory.dto.audit.NewsCategoryAuditLogDto;
import com.mmva.newsapp.domain.newscategory.model.audit.NewsCategoryAuditLog;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * MapStruct mapper for NewsCategoryAuditLog entity.
 * Converts between entity and DTO representations for unified audit logs.
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Mapper(componentModel = "spring")
public interface NewsCategoryAuditLogMapper {

    /**
     * Converts NewsCategoryAuditLog entity to DTO.
     * Maps unified BaseAuditLogEntity fields and domain-specific newsCategoryId
     * field.
     *
     * @param entity the audit log entity
     * @return the DTO
     */
    @Mapping(target = "categoryId", source = "newsCategoryId")
    @Mapping(target = "details", source = "reason")
    @Mapping(target = "createdBy", source = "actorId")
    @Mapping(target = "createdAt", source = "createdAt", qualifiedByName = "instantToString")
    @Mapping(target = "actionDescription", ignore = true)
    NewsCategoryAuditLogDto toDto(NewsCategoryAuditLog entity);

    /**
     * Converts an Instant timestamp to ISO 8601 string format.
     *
     * @param instant the timestamp
     * @return the ISO 8601 formatted string
     */
    @Named("instantToString")
    default String instantToString(Instant instant) {
        if (instant == null) {
            return null;
        }
        return DateTimeFormatter.ISO_INSTANT.format(instant);
    }
}
