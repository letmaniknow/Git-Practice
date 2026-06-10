package com.mmva.newsapp.infrastructure.rbac.permission.core.dto;

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
public class PermissionResponseDto {
    @Schema(description = "Unique identifier of the permission", example = "123e4567-e89b-12d3-a456-426614174000")
    private java.util.UUID permissionId;

    @Schema(description = "Name of the permission", example = "NEWS_EDIT")
    private String permissionName;

    @Schema(description = "Description of the permission", example = "Allows editing of newsapp articles")
    private String permissionDescription;

    @Schema(description = "Whether the permission is active", example = "true")
    private Boolean isActive;

    @Schema(description = "Creation timestamp (ISO 8601)", example = "2024-06-01T12:34:56Z")
    private String createdAt;

    @Schema(description = "Last update timestamp (ISO 8601)", example = "2024-06-02T08:00:00Z")
    private String updatedAt;

    @Schema(description = "User ID of the creator", example = "987e6543-e21b-12d3-a456-426614174999")
    private java.util.UUID createdBy;

    @Schema(description = "User ID of the last updater", example = "987e6543-e21b-12d3-a456-426614174999")
    private java.util.UUID updatedBy;

    @Schema(description = "Deletion timestamp (ISO 8601)", example = "2024-06-03T09:00:00Z")
    private String deletedAt;

    @Schema(description = "User ID of the deleter", example = "987e6543-e21b-12d3-a456-426614174999")
    private java.util.UUID deletedBy;
}
