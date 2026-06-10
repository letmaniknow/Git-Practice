package com.mmva.newsapp.infrastructure.rbac.role.core.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * Response DTO for permission assignment operations on a role.
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response DTO for permission assignment operations on a role")
public class RoleAssignPermissionsResponseDto {

    @Schema(description = "ID of the role to which permissions were assigned", example = "d290f1ee-6c54-4b01-90e6-d701748f0851")
    private UUID roleId;

    @Schema(description = "List of permission IDs assigned to the role", example = "[\"123e4567-e89b-12d3-a456-426614174000\", \"987e6543-e21b-12d3-a456-426614174999\"]")
    private List<UUID> permissionIds;

    @Schema(description = "Result message for the assignment operation", example = "Permissions assigned successfully.")
    private String message;
}
