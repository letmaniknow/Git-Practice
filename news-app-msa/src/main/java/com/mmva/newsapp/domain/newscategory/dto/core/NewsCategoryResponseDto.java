package com.mmva.newsapp.domain.newscategory.dto.core;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NewsCategoryResponseDto {

    @Schema(description = "Unique identifier of the newsapp newscategory", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID newsCategoriesId;

    @Schema(description = "Category name in English", example = "World News")
    private String newsCategoriesNameEn;

    @Schema(description = "Category name in Spanish", example = "Noticias del Mundo")
    private String newsCategoriesNameEs;

    @Schema(description = "Slug for the newscategory (lowercase, hyphen-separated)", example = "world-newsapp")
    private String newsCategoriesSlug;

    @Schema(description = "Description of the newscategory", example = "Latest updates and headlines from around the world.")
    private String newsCategoriesDescription;

    @Schema(description = "Timestamp when the newscategory was created (ISO 8601)", example = "2025-12-14T12:00:00Z")
    private String createdAt;

    @Schema(description = "ID of the user who created this newscategory")
    private UUID createdBy;

    @Schema(description = "Timestamp when the newscategory was last updated (ISO 8601)", example = "2025-12-14T12:00:00Z")
    private String updatedAt;

    @Schema(description = "ID of the user who last updated this newscategory")
    private UUID updatedBy;

    @Schema(description = "Status of the category (ACTIVE, INACTIVE, DELETED)", example = "ACTIVE")
    private String status;
}
