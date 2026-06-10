package com.mmva.newsapp.infrastructure.rbac.role.core.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO for cloning a role")
public class RoleCloneRequestDto {

    @NotBlank(message = "New role name is required")
    @Size(min = 2, max = 50, message = "Role name must be between 2 and 50 characters")
    @Schema(description = "Name for the cloned role", example = "EDITOR_CLONE")
    private String newRoleName;

    @Size(max = 255, message = "Description must not exceed 255 characters")
    @Schema(description = "Description for the cloned role", example = "Cloned editor role with same permissions")
    private String newRoleDescription;

    @Schema(description = "Admin user performing the clone operation (set by server from JWT)", hidden = true)
    private UUID adminId;

    @Schema(description = "Whether to copy permissions from source role", example = "true")
    @Builder.Default
    private Boolean copyPermissions = true;
}
