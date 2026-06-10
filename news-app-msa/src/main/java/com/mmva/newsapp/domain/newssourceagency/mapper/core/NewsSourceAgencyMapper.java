package com.mmva.newsapp.domain.newssourceagency.mapper.core;

import com.mmva.newsapp.domain.newssourceagency.dto.core.NewsSourceAgencyRequestDto;
import com.mmva.newsapp.domain.newssourceagency.dto.core.NewsSourceAgencyResponseDto;
import com.mmva.newsapp.domain.newssourceagency.model.core.NewsSourceAgency;
import org.mapstruct.*;

import java.time.Instant;

/**
 * MapStruct mapper for NewsSourceAgency entity.
 * 
 * <p>
 * Provides mapping between DTOs and entity for newsapp source agency
 * operations.
 * </p>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Mapper(componentModel = "spring")
public interface NewsSourceAgencyMapper {

        /**
         * Converts request DTO to entity for creation.
         * 
         * @param dto the request DTO
         * @return the entity
         */
        @Mappings({
                        @Mapping(target = "agencyId", ignore = true),
                        @Mapping(target = "createdAt", ignore = true),
                        @Mapping(target = "createdBy", ignore = true),
                        @Mapping(target = "updatedAt", ignore = true),
                        @Mapping(target = "updatedBy", ignore = true),
                        @Mapping(target = "deletedAt", ignore = true),
                        @Mapping(target = "deletedBy", ignore = true),
                        @Mapping(target = "isTrusted", defaultValue = "true"),
                        @Mapping(target = "isActive", defaultValue = "true")
        })
        NewsSourceAgency toEntity(NewsSourceAgencyRequestDto dto);

        /**
         * Converts entity to response DTO.
         * 
         * @param entity the entity
         * @return the response DTO
         */
        @Mappings({
                        @Mapping(target = "createdAt", expression = "java(instantToString(entity.getCreatedAt()))"),
                        @Mapping(target = "updatedAt", expression = "java(instantToString(entity.getUpdatedAt()))")
        })
        NewsSourceAgencyResponseDto toResponseDto(NewsSourceAgency entity);

        /**
         * Updates existing entity from request DTO.
         * 
         * @param dto    the request DTO with updated values
         * @param entity the target entity to update
         */
        @Mappings({
                        @Mapping(target = "agencyId", ignore = true),
                        @Mapping(target = "createdAt", ignore = true),
                        @Mapping(target = "createdBy", ignore = true),
                        @Mapping(target = "updatedAt", ignore = true),
                        @Mapping(target = "updatedBy", ignore = true),
                        @Mapping(target = "deletedAt", ignore = true),
                        @Mapping(target = "deletedBy", ignore = true)
        })
        void updateEntityFromDto(NewsSourceAgencyRequestDto dto, @MappingTarget NewsSourceAgency entity);

        /**
         * Converts Instant to ISO-8601 string.
         * 
         * @param instant the instant to convert
         * @return the ISO-8601 formatted string, or null
         */
        default String instantToString(Instant instant) {
                return instant != null ? instant.toString() : null;
        }
}
