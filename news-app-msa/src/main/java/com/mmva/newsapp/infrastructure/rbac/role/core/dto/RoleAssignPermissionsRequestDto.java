package com.mmva.newsapp.infrastructure.rbac.role.core.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * Request DTO for assigning permissions to a role.
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO for assigning permissions to a role")
public class RoleAssignPermissionsRequestDto {

    @NotEmpty(message = "Permission IDs list cannot be empty")
    @Schema(description = "List of permission IDs to assign to the role")
    private List<UUID> permissionIds;
}
