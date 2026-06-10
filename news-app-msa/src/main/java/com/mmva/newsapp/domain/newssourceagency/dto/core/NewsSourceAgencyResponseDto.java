package com.mmva.newsapp.domain.newssourceagency.dto.core;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Response DTO for News Source Agency.
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NewsSourceAgencyResponseDto {

    @Schema(description = "Unique identifier of the agency", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID agencyId;

    @Schema(description = "Unique agency code", example = "REUTERS")
    private String agencyCode;

    @Schema(description = "Display name of the agency", example = "Reuters")
    private String agencyName;

    @Schema(description = "URL to the agency's logo", example = "https://cdn.example.com/logos/reuters.png")
    private String agencyLogoUrl;

    @Schema(description = "Agency's official website URL", example = "https://www.reuters.com")
    private String agencyWebsiteUrl;

    @Schema(description = "Whether this agency is a trusted/verified newsapp source", example = "true")
    private Boolean isTrusted;

    @Schema(description = "Whether this agency is currently active", example = "true")
    private Boolean isActive;

    @Schema(description = "Description of the agency", example = "International newsapp agency headquartered in London")
    private String description;

    @Schema(description = "Timestamp when the agency was created (ISO 8601)", example = "2025-12-14T12:00:00Z")
    private String createdAt;

    @Schema(description = "ID of the user who created this agency")
    private UUID createdBy;

    @Schema(description = "Timestamp when the agency was last updated (ISO 8601)", example = "2025-12-14T12:00:00Z")
    private String updatedAt;

    @Schema(description = "ID of the user who last updated this agency")
    private UUID updatedBy;
}
