package com.mmva.newsapp.domain.newsengagement.shares.mapper;

import com.mmva.newsapp.domain.newsengagement.shares.dto.NewsShareRequestDto;
import com.mmva.newsapp.domain.newsengagement.shares.dto.NewsShareResponseDto;
import com.mmva.newsapp.domain.newsengagement.shares.model.NewsShare;
import org.mapstruct.*;

import java.time.Instant;

/**
 * MapStruct mapper for NewsShare entity.
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Mapper(componentModel = "spring")
public interface NewsShareMapper {

    @Mappings({
            @Mapping(target = "newsSharesId", ignore = true),
            @Mapping(target = "news", ignore = true),
            @Mapping(target = "newsSharesSharedAt", expression = "java(dto.getNewsSharesSharedAt() != null ? java.time.Instant.parse(dto.getNewsSharesSharedAt()) : java.time.Instant.now())"),
            // Client context fields - set by service (except infrastructure which comes
            // from DTO)
            @Mapping(target = "newsSharesDeviceType", ignore = true),
            @Mapping(target = "newsSharesDeviceFingerprint", ignore = true),
            @Mapping(target = "newsSharesCountryCode", ignore = true),
            @Mapping(target = "newsSharesCity", ignore = true),
            @Mapping(target = "newsSharesChannel", ignore = true)
    })
    NewsShare toEntity(NewsShareRequestDto dto);

    @Mappings({
            @Mapping(target = "newsSharesSharedAt", expression = "java(instantToString(entity.getNewsSharesSharedAt()))"),
            @Mapping(target = "updatedShareCount", ignore = true)
    })
    NewsShareResponseDto toResponseDto(NewsShare entity);

    default String instantToString(Instant instant) {
        return instant != null ? instant.toString() : null;
    }
}
