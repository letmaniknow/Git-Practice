package com.mmva.newsapp.domain.newscategory.mapper.core;

import com.mmva.newsapp.domain.newscategory.dto.core.NewsCategoryRequestDto;
import com.mmva.newsapp.domain.newscategory.dto.core.NewsCategoryResponseDto;
import com.mmva.newsapp.domain.newscategory.model.core.NewsCategory;
import org.mapstruct.*;

import java.time.Instant;

/**
 * MapStruct mapper for NewsCategory entity.
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Mapper(componentModel = "spring")
public interface NewsCategoryMapper {

        @Mappings({
                        @Mapping(target = "newsCategoriesId", ignore = true),
                        @Mapping(target = "newsCategoriesSlug", source = "slug"),
                        @Mapping(target = "newsCategoriesNameEn", source = "categoryNameEn"),
                        @Mapping(target = "newsCategoriesNameEs", source = "categoryNameEs"),
                        @Mapping(target = "newsCategoriesDescription", source = "categoryDescription"),
                        @Mapping(target = "createdAt", ignore = true),
                        @Mapping(target = "createdBy", ignore = true),
                        @Mapping(target = "updatedAt", ignore = true),
                        @Mapping(target = "updatedBy", ignore = true),
                        @Mapping(target = "deletedAt", ignore = true),
                        @Mapping(target = "deletedBy", ignore = true),
                        @Mapping(target = "status", ignore = true)
        })
        NewsCategory toEntity(NewsCategoryRequestDto dto);

        @Mappings({
                        @Mapping(target = "createdAt", expression = "java(instantToString(entity.getCreatedAt()))"),
                        @Mapping(target = "updatedAt", expression = "java(instantToString(entity.getUpdatedAt()))"),
                        @Mapping(target = "status", expression = "java(entity.getStatus() != null ? entity.getStatus().name() : null)")
        })
        NewsCategoryResponseDto toResponseDto(NewsCategory entity);

        @Mappings({
                        @Mapping(target = "newsCategoriesId", ignore = true),
                        @Mapping(target = "newsCategoriesSlug", source = "slug"),
                        @Mapping(target = "newsCategoriesNameEn", source = "categoryNameEn"),
                        @Mapping(target = "newsCategoriesNameEs", source = "categoryNameEs"),
                        @Mapping(target = "newsCategoriesDescription", source = "categoryDescription"),
                        @Mapping(target = "createdAt", ignore = true),
                        @Mapping(target = "createdBy", ignore = true),
                        @Mapping(target = "updatedAt", ignore = true),
                        @Mapping(target = "updatedBy", ignore = true),
                        @Mapping(target = "deletedAt", ignore = true),
                        @Mapping(target = "deletedBy", ignore = true),
                        @Mapping(target = "status", ignore = true)
        })
        void updateEntityFromDto(NewsCategoryRequestDto dto, @MappingTarget NewsCategory entity);

        default String instantToString(Instant instant) {
                return instant != null ? instant.toString() : null;
        }
}
