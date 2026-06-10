package com.mmva.newsapp.infrastructure.rbac.role.core.dto;

import lombok.*;
import java.util.UUID;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO for creating or updating a role")
public class RoleRequestDto {
    @NotBlank(message = "Role name is required")
    @Size(min = 2, max = 50, message = "Role name must be between 2 and 50 characters")
    @Schema(description = "Role name", example = "EDITOR")
    private String roleName;

    @NotNull(message = "Role description is required")
    @Size(max = 255, message = "Role description must be at most 255 characters")
    @Schema(description = "Role description", example = "Editor role with content management permissions")
    private String roleDescription;

    @Schema(description = "Whether the role is active", example = "true")
    private Boolean isActive;

    @Schema(description = "Admin user ID performing the operation (set by server, not required in request)", hidden = true)
    private UUID adminId;
}
