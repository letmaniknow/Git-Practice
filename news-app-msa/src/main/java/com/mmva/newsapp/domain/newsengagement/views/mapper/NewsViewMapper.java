package com.mmva.newsapp.domain.newsengagement.views.mapper;

import com.mmva.newsapp.domain.newsengagement.views.dto.NewsViewRequestDto;
import com.mmva.newsapp.domain.newsengagement.views.dto.NewsViewResponseDto;
import com.mmva.newsapp.domain.newsengagement.views.model.NewsView;
import org.mapstruct.*;

import java.time.Instant;

/**
 * MapStruct mapper for NewsView entity.
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Mapper(componentModel = "spring")
public interface NewsViewMapper {

    @Mappings({
            @Mapping(target = "newsViewsId", ignore = true),
            @Mapping(target = "news", ignore = true),
            @Mapping(target = "newsViewsViewedAt", expression = "java(java.time.Instant.now())"),
            // Client context fields - set by service
            @Mapping(target = "newsViewsDeviceType", ignore = true),
            @Mapping(target = "newsViewsDeviceFingerprint", ignore = true),
            @Mapping(target = "newsViewsBrowserName", ignore = true),
            @Mapping(target = "newsViewsBrowserVersion", ignore = true),
            @Mapping(target = "newsViewsOsName", ignore = true),
            @Mapping(target = "newsViewsOsVersion", ignore = true),
            @Mapping(target = "newsViewsCountryCode", ignore = true),
            @Mapping(target = "newsViewsCity", ignore = true),
            @Mapping(target = "newsViewsTimezone", ignore = true),
            @Mapping(target = "newsViewsIsBot", ignore = true),
            @Mapping(target = "newsViewsIsAnonymized", ignore = true),
            @Mapping(target = "newsViewsChannel", ignore = true),
            @Mapping(target = "newsViewsLanguage", ignore = true),
            @Mapping(target = "newsViewsRefererDomain", ignore = true)
    })
    NewsView toEntity(NewsViewRequestDto dto);

    @Mappings({
            @Mapping(target = "newsViewsViewedAt", expression = "java(instantToString(entity.getNewsViewsViewedAt()))"),
            @Mapping(target = "updatedViewCount", ignore = true)
    })
    NewsViewResponseDto toResponseDto(NewsView entity);

    default String instantToString(Instant instant) {
        return instant != null ? instant.toString() : null;
    }
}
