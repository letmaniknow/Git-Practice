package com.mmva.newsapp.infrastructure.rbac.role.core.dto;

import lombok.*;
import java.util.Set;
import java.util.UUID;
import io.swagger.v3.oas.annotations.media.Schema;
import com.mmva.newsapp.infrastructure.rbac.permission.core.dto.PermissionResponseDto;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Role Response DTO")
public class RoleResponseDto {
    @Schema(description = "Role ID", example = "d290f1ee-6c54-4b01-90e6-d701748f0851")
    private UUID roleId;

    @Schema(description = "Role name", example = "ADMIN")
    private String roleName;

    @Schema(description = "Role description", example = "Administrator role")
    private String roleDescription;

    @Schema(description = "Is role active", example = "true")
    private Boolean isActive;

    @Schema(description = "Permissions assigned to this role")
    @Builder.Default
    private Set<PermissionResponseDto> permissions = new java.util.HashSet<>();

    @Schema(description = "Role deleted at timestamp")
    private String deletedAt;

    @Schema(description = "Role created at timestamp")
    private String createdAt;

    @Schema(description = "Role updated at timestamp")
    private String updatedAt;

    @Schema(description = "Role created by user ID")
    private UUID createdBy;

    @Schema(description = "Role updated by user ID")
    private UUID updatedBy;

    @Schema(description = "Role deleted by user ID")
    private UUID deletedBy;
}
