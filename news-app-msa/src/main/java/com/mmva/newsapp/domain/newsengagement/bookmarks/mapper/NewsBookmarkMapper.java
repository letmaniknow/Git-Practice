package com.mmva.newsapp.domain.newsengagement.bookmarks.mapper;

import com.mmva.newsapp.domain.newsengagement.bookmarks.dto.NewsBookmarkRequestDto;
import com.mmva.newsapp.domain.newsengagement.bookmarks.dto.NewsBookmarkResponseDto;
import com.mmva.newsapp.domain.newsengagement.bookmarks.dto.NewsBookmarkUpdateDto;
import com.mmva.newsapp.domain.newsengagement.bookmarks.model.NewsBookmark;
import org.mapstruct.*;

import java.time.Instant;

/**
 * MapStruct mapper for NewsBookmark entity.
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface NewsBookmarkMapper {

        @Mappings({
                        @Mapping(target = "newsBookmarksId", ignore = true),
                        @Mapping(target = "newsBookmarksBookmarkedAt", expression = "java(dto.getNewsBookmarksBookmarkedAt() != null ? java.time.Instant.parse(dto.getNewsBookmarksBookmarkedAt()) : java.time.Instant.now())"),
                        // Client context fields - set by service
                        @Mapping(target = "newsBookmarksDeviceType", ignore = true),
                        @Mapping(target = "newsBookmarksDeviceFingerprint", ignore = true),
                        @Mapping(target = "newsBookmarksCountryCode", ignore = true),
                        @Mapping(target = "newsBookmarksCity", ignore = true),
                        @Mapping(target = "newsBookmarksChannel", ignore = true),
                        @Mapping(target = "news", ignore = true)
        })
        NewsBookmark toEntity(NewsBookmarkRequestDto dto);

        @Mappings({
                        @Mapping(target = "newsBookmarksBookmarkedAt", expression = "java(instantToString(entity.getNewsBookmarksBookmarkedAt()))"),
                        @Mapping(target = "updatedBookmarkCount", ignore = true)
        })
        NewsBookmarkResponseDto toResponseDto(NewsBookmark entity);

        @Mappings({
                        @Mapping(target = "newsBookmarksId", ignore = true),
                        @Mapping(target = "newsBookmarksUserId", ignore = true),
                        @Mapping(target = "newsBookmarksNewsId", ignore = true),
                        @Mapping(target = "newsBookmarksBookmarkedAt", ignore = true),
                        // Client context fields - immutable after creation
                        @Mapping(target = "newsBookmarksDeviceType", ignore = true),
                        @Mapping(target = "newsBookmarksDeviceFingerprint", ignore = true),
                        @Mapping(target = "newsBookmarksCountryCode", ignore = true),
                        @Mapping(target = "newsBookmarksCity", ignore = true),
                        @Mapping(target = "newsBookmarksChannel", ignore = true),
                        @Mapping(target = "news", ignore = true)
        })
        void updateEntityFromDto(NewsBookmarkUpdateDto dto, @MappingTarget NewsBookmark entity);

        default String instantToString(Instant instant) {
                return instant != null ? instant.toString() : null;
        }
}
