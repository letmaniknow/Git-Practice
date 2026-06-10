package com.mmva.newsapp.domain.newsengagement.likes.mapper;

import com.mmva.newsapp.domain.newsengagement.likes.dto.NewsLikeRequestDto;
import com.mmva.newsapp.domain.newsengagement.likes.dto.NewsLikeResponseDto;
import com.mmva.newsapp.domain.newsengagement.likes.model.NewsLike;
import org.mapstruct.*;

import java.time.Instant;

/**
 * MapStruct mapper for NewsLike entity.
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Mapper(componentModel = "spring")
public interface NewsLikeMapper {

    @Mappings({
            @Mapping(target = "newsLikesId", ignore = true),
            @Mapping(target = "news", ignore = true),
            @Mapping(target = "newsLikesLikedAt", expression = "java(java.time.Instant.now())"),
            // Client context fields - set by service
            @Mapping(target = "newsLikesDeviceType", ignore = true),
            @Mapping(target = "newsLikesDeviceFingerprint", ignore = true),
            @Mapping(target = "newsLikesCountryCode", ignore = true),
            @Mapping(target = "newsLikesCity", ignore = true),
            @Mapping(target = "newsLikesChannel", ignore = true)
    })
    NewsLike toEntity(NewsLikeRequestDto dto);

    @Mappings({
            @Mapping(target = "newsLikesLikedAt", expression = "java(instantToString(entity.getNewsLikesLikedAt()))"),
            @Mapping(target = "updatedLikeCount", ignore = true)
    })
    NewsLikeResponseDto toResponseDto(NewsLike entity);

    default String instantToString(Instant instant) {
        return instant != null ? instant.toString() : null;
    }
}
