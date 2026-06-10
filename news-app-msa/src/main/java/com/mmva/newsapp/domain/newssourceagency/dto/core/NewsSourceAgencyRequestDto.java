package com.mmva.newsapp.domain.newssourceagency.dto.core;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request DTO for creating or updating a News Source Agency.
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsSourceAgencyRequestDto {

    @Schema(description = "Admin user performing the action (set by server from JWT)", hidden = true)
    private UUID adminId;

    @Schema(description = "Unique agency code (uppercase, alphanumeric)", example = "REUTERS", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Agency code is required")
    @Size(min = 2, max = 50, message = "Agency code must be between 2 and 50 characters")
    @Pattern(regexp = "^[A-Z0-9_]+$", message = "Agency code must be uppercase alphanumeric with underscores only")
    private String agencyCode;

    @Schema(description = "Display name of the agency", example = "Reuters", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Agency name is required")
    @Size(min = 2, max = 255, message = "Agency name must be between 2 and 255 characters")
    private String agencyName;

    @Schema(description = "URL to the agency's logo", example = "https://cdn.example.com/logos/reuters.png", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Size(max = 2048, message = "Logo URL must not exceed 2048 characters")
    private String agencyLogoUrl;

    @Schema(description = "Agency's official website URL", example = "https://www.reuters.com", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Size(max = 2048, message = "Website URL must not exceed 2048 characters")
    private String agencyWebsiteUrl;

    @Schema(description = "Whether this agency is a trusted/verified newsapp source", example = "true", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Boolean isTrusted;

    @Schema(description = "Whether this agency is currently active", example = "true", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Boolean isActive;

    @Schema(description = "Description of the agency", example = "International newsapp agency headquartered in London", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;
}
