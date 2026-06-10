package com.mmva.newsapp.domain.newsengagement.comments.mapper;

import com.mmva.newsapp.domain.newsengagement.comments.dto.NewsCommentRequestDto;
import com.mmva.newsapp.domain.newsengagement.comments.dto.NewsCommentResponseDto;
import com.mmva.newsapp.domain.newsengagement.comments.dto.NewsCommentUpdateDto;
import com.mmva.newsapp.domain.newsengagement.comments.model.NewsComment;
import org.mapstruct.*;

import java.time.Instant;

/**
 * MapStruct mapper for NewsComment entity.
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface NewsCommentMapper {

        @Mappings({
                        @Mapping(target = "newsCommentsId", ignore = true),
                        @Mapping(target = "news", ignore = true),
                        @Mapping(target = "newsCommentsParentId", ignore = true),
                        @Mapping(target = "newsCommentsStatus", ignore = true),
                        @Mapping(target = "createdAt", ignore = true),
                        @Mapping(target = "createdBy", ignore = true),
                        @Mapping(target = "updatedAt", ignore = true),
                        @Mapping(target = "updatedBy", ignore = true),
                        @Mapping(target = "deletedAt", ignore = true),
                        @Mapping(target = "deletedBy", ignore = true),
                        @Mapping(target = "newsCommentsCommentedAt", expression = "java(java.time.Instant.now())"),
                        // Client context fields - set by service
                        @Mapping(target = "newsCommentsDeviceType", ignore = true),
                        @Mapping(target = "newsCommentsDeviceFingerprint", ignore = true),
                        @Mapping(target = "newsCommentsBrowserName", ignore = true),
                        @Mapping(target = "newsCommentsOsName", ignore = true),
                        @Mapping(target = "newsCommentsCountryCode", ignore = true),
                        @Mapping(target = "newsCommentsCity", ignore = true),
                        @Mapping(target = "newsCommentsIsBot", ignore = true),
                        @Mapping(target = "newsCommentsIsAnonymized", ignore = true),
                        @Mapping(target = "newsCommentsRiskScore", ignore = true),
                        @Mapping(target = "newsCommentsChannel", ignore = true),
                        @Mapping(target = "newsCommentsLanguage", ignore = true)
        })
        NewsComment toEntity(NewsCommentRequestDto dto);

        @Mappings({
                        @Mapping(target = "newsCommentsCommentedAt", expression = "java(instantToString(entity.getNewsCommentsCommentedAt()))"),
                        @Mapping(target = "newsCommentsStatus", expression = "java(entity.getNewsCommentsStatus() != null ? entity.getNewsCommentsStatus().name() : null)"),
                        @Mapping(target = "createdAt", expression = "java(instantToString(entity.getCreatedAt()))"),
                        @Mapping(target = "updatedAt", expression = "java(instantToString(entity.getUpdatedAt()))"),
                        @Mapping(target = "replies", ignore = true),
                        @Mapping(target = "updatedCommentCount", ignore = true),
                        @Mapping(target = "updatedReplyCount", ignore = true)
        })
        NewsCommentResponseDto toResponseDto(NewsComment entity);

        @Mappings({
                        @Mapping(target = "newsCommentsId", ignore = true),
                        @Mapping(target = "news", ignore = true),
                        @Mapping(target = "newsCommentsNewsId", ignore = true),
                        @Mapping(target = "newsCommentsUserId", ignore = true),
                        @Mapping(target = "newsCommentsParentId", ignore = true),
                        @Mapping(target = "newsCommentsIpAddress", ignore = true),
                        @Mapping(target = "newsCommentsCommentedAt", ignore = true),
                        @Mapping(target = "newsCommentsStatus", ignore = true),
                        @Mapping(target = "createdAt", ignore = true),
                        @Mapping(target = "createdBy", ignore = true),
                        @Mapping(target = "updatedAt", ignore = true),
                        @Mapping(target = "updatedBy", ignore = true),
                        @Mapping(target = "deletedAt", ignore = true),
                        @Mapping(target = "deletedBy", ignore = true),
                        // Client context fields - immutable after creation
                        @Mapping(target = "newsCommentsDeviceType", ignore = true),
                        @Mapping(target = "newsCommentsDeviceFingerprint", ignore = true),
                        @Mapping(target = "newsCommentsBrowserName", ignore = true),
                        @Mapping(target = "newsCommentsOsName", ignore = true),
                        @Mapping(target = "newsCommentsCountryCode", ignore = true),
                        @Mapping(target = "newsCommentsCity", ignore = true),
                        @Mapping(target = "newsCommentsIsBot", ignore = true),
                        @Mapping(target = "newsCommentsIsAnonymized", ignore = true),
                        @Mapping(target = "newsCommentsRiskScore", ignore = true),
                        @Mapping(target = "newsCommentsChannel", ignore = true),
                        @Mapping(target = "newsCommentsLanguage", ignore = true),
                        @Mapping(source = "newsCommentsComment", target = "newsCommentsComment")
        })
        void updateEntityFromDto(NewsCommentUpdateDto dto, @MappingTarget NewsComment entity);

        default String instantToString(Instant instant) {
                return instant != null ? instant.toString() : null;
        }
}
