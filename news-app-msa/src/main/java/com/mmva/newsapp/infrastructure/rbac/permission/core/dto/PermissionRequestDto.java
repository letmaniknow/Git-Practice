package com.mmva.newsapp.infrastructure.rbac.permission.core.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
public class PermissionRequestDto {
    @Schema(description = "Name of the permission", example = "NEWS_EDIT", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    @Size(max = 100)
    private String permissionName;

    @Schema(description = "Description of the permission", example = "Allows editing of newsapp articles")
    @Size(max = 255)
    private String permissionDescription;

    @Schema(description = "Admin ID of the creator or updater (set by server, not required in request)", hidden = true)
    private java.util.UUID adminId; // For createdBy/updatedBy

    @Schema(description = "Whether the permission is active", example = "true")
    private Boolean isActive;
}
