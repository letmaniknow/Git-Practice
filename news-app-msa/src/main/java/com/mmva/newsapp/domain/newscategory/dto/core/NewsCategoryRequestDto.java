package com.mmva.newsapp.domain.newscategory.dto.core;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsCategoryRequestDto {

    @Schema(description = "Admin user performing the action (set by server from JWT)", hidden = true)
    private UUID adminId;

    @Schema(description = "Category name in English", example = "World News", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Category name (English) is required")
    @Size(min = 2, max = 100, message = "Category name (English) must be between 2 and 100 characters")
    private String categoryNameEn;

    @Schema(description = "Category name in Spanish", example = "Noticias del Mundo", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Category name (Spanish) is required")
    @Size(min = 2, max = 100, message = "Category name (Spanish) must be between 2 and 100 characters")
    private String categoryNameEs;

    @Schema(description = "Slug for the newscategory (lowercase, hyphen-separated). If not provided, will be auto-generated from categoryNameEn", example = "world-newsapp", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Size(max = 100, message = "Slug must not exceed 100 characters")
    @Pattern(regexp = "^$|^[a-z0-9]+(-[a-z0-9]+)*$", message = "Slug must be lowercase alphanumeric with hyphens only")
    private String slug;

    @Schema(description = "Description of the newscategory", example = "Latest updates and headlines from around the world.")
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String categoryDescription;
}
